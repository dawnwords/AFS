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

    private void appendNewFileItem(String name, FID fid, FileHandler handler) {
        byte[] origin = handler.getData();
        byte[] newBytes = new byte[origin.length + Parameter.FILE_ITEM_LEN];
        DataTypeUtil.arrayWrite(newBytes, 0, origin);
        DataTypeUtil.arrayWrite(newBytes, origin.length, name.getBytes());
        DataTypeUtil.arrayWrite(newBytes, origin.length + Parameter.FILE_NAME_LEN, fid.getBytes());

        handler.setData(newBytes);
        handler.getAttributes().modify(userId, newBytes.length);
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
                appendNewFileItem(name, fid, currentDirHandler);
                store(currentDir, currentDirHandler);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void store(FID fid, FileHandler handler) throws Exception {
        byte[] data = handler.getBytes();
        FileOutputStream fos = null;
        try {
            FID next = fid;
            int start = 0;
            int i = 1;
            do {
                // Store local
                fos = new FileOutputStream(Parameter.VENUS_DIR + next.toString());
                fos.write(data, start, Math.min(data.length - start, Parameter.FILE_SIZE));
                fos.flush();
                fos.close();

                // update remote
                vice.store(next, handler, userId);

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
