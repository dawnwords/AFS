package venus;

import data.FID;
import data.FileHandler;
import data.Parameter;
import interfaces.VenusInterface;
import interfaces.ViceInterface;
import util.DataTypeUtil;
import util.FileSystemUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedHashMap;

/**
 * Created by Dawnwords on 2014/5/6.
 */
public class Venus extends UnicastRemoteObject implements VenusInterface {
    private ViceInterface vice;
    private int userId;

    private FID currentDir;

    public Venus(ViceInterface vice, String venusRMI) throws RemoteException {
        this.vice = vice;
        this.userId = venusRMI.hashCode();
        this.currentDir = Parameter.ROOT_FID;
    }

    @Override
    public void breakCallBack(FID fid) throws RemoteException {

    }

    public String[] listFile() {
        FileHandler currentDirHandler = fetch(currentDir);
        LinkedHashMap<String, FID> map = getNameFIDMap(currentDirHandler);
        return map.keySet().toArray(new String[map.size()]);
    }

    public boolean remove(String name) {
        FileHandler currentDirHandler = fetch(currentDir);
        LinkedHashMap<String, FID> map = getNameFIDMap(currentDirHandler);
        FID toRemove = map.remove(name);
        if (toRemove != null) {
            updateDirectoryHandler(currentDirHandler, map);
            store(currentDir, currentDirHandler);
            try {
                vice.remove(toRemove, userId);
                return true;
            } catch (RemoteException e) {
                e.printStackTrace();
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

    private int getFilePartNum(FileHandler handler) {
        int fileSize = handler.getAttributes().getFileSize();
        return fileSize / Parameter.FILE_SIZE + (fileSize % Parameter.FILE_SIZE == 0 ? 0 : 1);
    }

    private LinkedHashMap<String, FID> getNameFIDMap(FileHandler handler) {
        int i = 0;
        byte[] data = handler.getBytes();
        LinkedHashMap<String, FID> map = new LinkedHashMap<String, FID>();
        while (i < data.length) {
            byte[] fileName = new byte[Parameter.FILE_NAME_LEN];
            byte[] fid = new byte[Parameter.FILE_BLOCK_NAME_LEN];
            System.arraycopy(data, i, fileName, 0, fileName.length);
            System.arraycopy(data, i + fileName.length, fid, 0, fid.length);
            map.put(new String(fileName), new FID(fid));
            i += Parameter.FILE_ITEM_LEN;
        }
        return map;
    }

    private void updateDirectoryHandler(FileHandler dir, LinkedHashMap<String, FID> map) {
        if (dir.getAttributes().isFile()) {
            return;
        }
        byte[] newBytes = new byte[Parameter.FILE_ITEM_LEN * map.size()];
        int start = 0;
        for (String name : map.keySet()) {
            FID fid = map.get(name);
            DataTypeUtil.arrayWrite(newBytes, start, name.getBytes());
            DataTypeUtil.arrayWrite(newBytes, start + Parameter.FILE_NAME_LEN, fid.getBytes());
            start += Parameter.FILE_ITEM_LEN;
        }
        dir.setData(newBytes);
        dir.getAttributes().modify(userId, newBytes.length + Parameter.FILE_ITEM_LEN);
    }


    private boolean create(String name, boolean isDir) {
        try {
            FileHandler currentDirHandler = fetch(currentDir);

            FID fid = isDir ? vice.makeDir(userId) : vice.create(userId);

            if (currentDirHandler != null) {
                int part = getFilePartNum(currentDirHandler);
                int i = 0;
                while (i < part) {
                    FileHandler otherParts = fetch(new FID(fid.getUserId(), i++, fid.getUniquifier()));
                    byte[] newData = DataTypeUtil.arrayCombine(currentDirHandler.getData(), otherParts.getData());
                    currentDirHandler.setData(newData);
                }
                LinkedHashMap<String, FID> map = getNameFIDMap(currentDirHandler);
                map.put(name, fid);
                updateDirectoryHandler(currentDirHandler, map);
                store(currentDir, currentDirHandler);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void store(FID fid, FileHandler handler) {
        byte[] data = handler.getBytes();
        FileOutputStream fos = null;
        try {
            FID next = fid;
            int start = 0;
            int i = 1;
            do {
                // Store local
                byte[] part = DataTypeUtil.subArray(data, start, Math.min(data.length - start, Parameter.FILE_SIZE));
                // need to update remote?
                if (!FileSystemUtil.writeWithChecksum(next, part)) {
                    // update remote
                    vice.store(next, handler, userId);
                }
                start += Parameter.FILE_SIZE;
                next = new FID(fid.getUserId(), i++, fid.getUniquifier());
            } while (start < data.length);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            FileSystemUtil.close(fos);
        }
    }

    private FileHandler fetch(FID fid) {
        File file = new File(Parameter.VENUS_DIR + fid);
        if (file.exists()) {
            //TODO check callback promise
            return FileSystemUtil.readFile(fid, Parameter.VENUS_DIR);
        }

        FileHandler handler = null;
        try {
            handler = vice.fetch(fid, userId);
            FileSystemUtil.writeFile(fid, handler, Parameter.VENUS_DIR);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return handler;
    }

}
