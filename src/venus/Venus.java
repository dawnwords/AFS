package venus;

import data.FID;
import data.FileHandler;
import data.Lock;
import data.Parameter;
import interfaces.VenusInterface;
import interfaces.ViceInterface;
import util.DataTypeUtil;
import util.FileSystemUtil;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;

/**
 * Created by Dawnwords on 2014/5/6.
 */
public class Venus extends UnicastRemoteObject implements VenusInterface {
    private ViceInterface vice;
    private long userId;

    private FID currentDir, currentFile;
    private FileHandler openFile;
    private Lock.LockMode mode;

    public static enum OpenError {
        CURRENT_DIR_NOT_EXISIT("Current dir not exists"),
        NO_SUCH_FILE("No such file"),
        FILE_LOCKED("File has locked"),
        SUCCESS("Success");

        private String errorMsg;

        OpenError(String errorMsg) {
            this.errorMsg = errorMsg;
        }

        public String getErrorMsg() {
            return errorMsg;
        }
    }

    public Venus(ViceInterface vice, String venusRMI) throws RemoteException {
        this.vice = vice;
        this.userId = DataTypeUtil.longHash(venusRMI);
        this.currentDir = Parameter.ROOT_FID;
    }

    @Override
    public void breakCallBack(FID fid) throws RemoteException {
        cancelCallback(fid);
    }

    public OpenError open(String filename, Lock.LockMode mode) {
        FileHandler currentDirHandler = fetch(currentDir);
        if (currentDirHandler == null) {
            return OpenError.CURRENT_DIR_NOT_EXISIT;
        }
        Map<String, FID> map = FileSystemUtil.getNameFIDMap(currentDirHandler);
        FID fid = map.get(filename);
        if (fid == null || fid.isDirectory()) {
            return OpenError.NO_SUCH_FILE;
        }
        currentFile = fid;
        try {
            if (vice.setLock(fid, mode, userId)) {
                openFile = fetch(fid);
                if (openFile != null) {
                    this.mode = mode;
                    return OpenError.SUCCESS;
                }
            } else {
                return OpenError.FILE_LOCKED;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return OpenError.NO_SUCH_FILE;
    }

    public String read() {
        if (openFile != null) {
            return new String(openFile.getData());
        }
        return null;
    }

    public boolean write(String content, boolean append) {
        if (openFile == null || mode != Lock.LockMode.EXCLUSIVE) {
            return false;
        }
        byte[] newBytes = content.getBytes();
        if (append) {
            newBytes = DataTypeUtil.arrayCombine(openFile.getData(), content.getBytes());
        }
        updateFileHandler(openFile, newBytes);
        return true;
    }

    public void close() {
        if (currentFile != null && openFile != null) {
            try {
                store(currentFile, openFile);
                vice.releaseLock(currentFile, userId);
                openFile = null;
                currentFile = null;
                mode = null;
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public String[] listFile() {
        FileHandler currentDirHandler = fetch(currentDir);
        if (currentDirHandler != null) {
            Map<String, FID> map = FileSystemUtil.getNameFIDMap(currentDirHandler);
            String[] result = new String[map.size()];
            int i = 0;
            for (String name : map.keySet()) {
                result[i++] = map.get(name).isDirectory() ? String.format("F{%s}", name) : name;
            }
            return result;
        }
        return new String[0];
    }

    public boolean changeDir(String name) {
        if (Parameter.ROOT_DIR.equals(name)) {
            currentDir = Parameter.ROOT_FID;
            return true;
        }
        FileHandler currentDirHandler = fetch(currentDir);
        if (currentDirHandler != null) {
            if (Parameter.PARENT_DIR.equals(name)) {
                currentDir = currentDirHandler.getAttributes().getParentDir();
                return true;
            }
            Map<String, FID> map = FileSystemUtil.getNameFIDMap(currentDirHandler);
            FID newCurrentDir = map.get(name);
            if (newCurrentDir != null && newCurrentDir.isDirectory()) {
                currentDir = newCurrentDir;
                return true;
            }
        }
        return false;
    }

    public boolean remove(String name) {
        FileHandler currentDirHandler = fetch(currentDir);
        if (currentDirHandler != null) {
            Map<String, FID> map = FileSystemUtil.getNameFIDMap(currentDirHandler);
            FID toRemove = map.remove(name);
            if (toRemove != null) {
                updateDirectoryHandler(currentDirHandler, map);
                store(currentDir, currentDirHandler);
                remove(toRemove);
                return true;
            }
        }
        return false;
    }

    public boolean createFile(String name) {
        return create(name, false);
    }

    public boolean makeDir(String name) {
        return create(name, true);
    }

    private void updateDirectoryHandler(FileHandler dir, Map<String, FID> map) {
        byte[] newBytes = new byte[Parameter.FILE_ITEM_LEN * map.size()];
        int start = 0;
        for (String name : map.keySet()) {
            FID fid = map.get(name);
            DataTypeUtil.arrayWrite(newBytes, start, name.getBytes());
            DataTypeUtil.arrayWrite(newBytes, start + Parameter.FILE_NAME_LEN, fid.getBytes());
            start += Parameter.FILE_ITEM_LEN;
        }
        updateFileHandler(dir, newBytes);
    }

    private void updateFileHandler(FileHandler handler, byte[] newBytes) {
        handler.setData(newBytes);
        handler.getAttributes().modify(userId, newBytes.length);
    }

    private boolean create(String name, boolean isDir) {
        try {
            FileHandler currentDirHandler = fetch(currentDir);
            FID fid = isDir ? vice.makeDir(currentDir, userId) : vice.create(userId);
            if (currentDirHandler != null) {
                Map<String, FID> map = FileSystemUtil.getNameFIDMap(currentDirHandler);
                if (!map.containsKey(name)) {
                    map.put(name, fid);
                    updateDirectoryHandler(currentDirHandler, map);
                    store(currentDir, currentDirHandler);
                    fetch(fid);
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void store(FID fid, FileHandler handler) {
        // Store Local & Check need to update remote?
        if (FileSystemUtil.writeWithChecksum(fid, handler.getBytes())) {
            // update remote
            try {
                handler.encrypt();
                vice.store(fid, handler, userId);
                vice.removeCallback(fid, userId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private FileHandler fetch(FID fid) {
        if (getCallbackPromise(fid)) {
            return FileSystemUtil.readFile(fid, Parameter.VENUS_DIR);
        }

        FileHandler handler = null;
        try {
            handler = vice.fetch(fid, userId);
            if (handler != null) {
                handler.decrypt();
                FileSystemUtil.writeFile(fid, handler, Parameter.VENUS_DIR);
                validCallback(fid);
            } else {
                FileSystemUtil.removeFile(fid, Parameter.VENUS_DIR);
                removeCallbackPromise(fid);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return handler;
    }

    private void remove(FID fid) {
        if (FileSystemUtil.fileExist(fid, Parameter.VENUS_DIR)) {
            if (fid.isDirectory()) {
                FileHandler handler = fetch(fid);
                if (handler != null) {
                    for (FID child : FileSystemUtil.getNameFIDMap(handler).values()) {
                        remove(child);
                    }
                }
            }
            FileSystemUtil.removeFile(fid, Parameter.VENUS_DIR);
        }
        try {
            vice.remove(fid, userId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void cancelCallback(FID fid) {
        saveCallbackPromise(fid, false);
    }

    private void validCallback(FID fid) {
        saveCallbackPromise(fid, true);
    }

    private boolean getCallbackPromise(FID fid) {
        Boolean result = FileSystemUtil.getLocalCallbackPromise().get(fid);
        return result != null && result;
    }

    private void removeCallbackPromise(FID fid) {
        Map<FID, Boolean> map = FileSystemUtil.getLocalCallbackPromise();
        map.remove(fid);
        FileSystemUtil.storeLocalCallbackPromise(map);
    }

    private void saveCallbackPromise(FID fid, boolean b) {
        Map<FID, Boolean> map = FileSystemUtil.getLocalCallbackPromise();
        map.put(fid, b);
        FileSystemUtil.storeLocalCallbackPromise(map);
    }
}
