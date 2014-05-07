package venus;

import data.FID;
import data.FileHandler;
import data.Parameter;
import interfaces.VenusInterface;
import interfaces.ViceInterface;
import util.DataTypeUtil;
import util.FileSystemUtil;

import java.io.File;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedHashMap;

/**
 * Created by Dawnwords on 2014/5/6.
 */
public class Venus extends UnicastRemoteObject implements VenusInterface {
    private ViceInterface vice;
    private long userId;

    private FID currentDir;

    public Venus(ViceInterface vice, String venusRMI) throws RemoteException {
        this.vice = vice;
        this.userId = DataTypeUtil.longHash(venusRMI);
        this.currentDir = Parameter.ROOT_FID;
    }

    @Override
    public void breakCallBack(FID fid) throws RemoteException {

    }

    public String[] listFile() {
        FileHandler currentDirHandler = fetch(currentDir);
        LinkedHashMap<String, FID> map = FileSystemUtil.getNameFIDMap(currentDirHandler);
        return map.keySet().toArray(new String[map.size()]);
    }

    public boolean remove(String name) {
        FileHandler currentDirHandler = fetch(currentDir);
        LinkedHashMap<String, FID> map = FileSystemUtil.getNameFIDMap(currentDirHandler);
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
                LinkedHashMap<String, FID> map = FileSystemUtil.getNameFIDMap(currentDirHandler);
                map.put(name, fid);
                updateDirectoryHandler(currentDirHandler, map);
                store(currentDir, currentDirHandler);
                fetch(fid);
                return true;
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
                vice.store(fid, handler, userId);
            } catch (Exception e) {
                e.printStackTrace();
            }
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
