package util;

import data.FID;
import data.FileAttributes;
import data.FileHandler;
import data.Parameter;

import java.io.*;

/**
 * Created by Dawnwords on 2014/5/5.
 */
public class FileSystemUtil {

    public static void initRootDir() {
        File serverDir = new File(Parameter.VICE_DIR);
        if (!serverDir.exists()) {
            serverDir.mkdir();
        }
        createFile(Parameter.ROOT_FID, true);
    }

    public static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static synchronized FID createFile(int userId, int uniquifier, boolean isDir) {
        FID fid = new FID(userId, 0, uniquifier);
        createFile(fid, isDir);
        return fid;
    }

    public static FileHandler readFile(FID fid, String rootDir) {
        // TODO callback promise
        byte[] file = read(fid, rootDir);

        if (fid.getPart() == 0) {
            FileAttributes attributes = FileAttributes.createFileAttributes(file);
            byte[] data = new byte[file.length - Parameter.FILE_ITEM_LEN];
            if (file.length > Parameter.FILE_ITEM_LEN) {
                System.arraycopy(file, Parameter.FILE_ITEM_LEN, data, 0, data.length);
            }
            return FileHandler.createFileHandler(attributes, data);
        } else {
            return FileHandler.createFileHandler(file);
        }
    }

    private static byte[] read(FID fid, String rootDir) {
        FileInputStream fis = null;
        byte[] file = null;
        try {
            fis = new FileInputStream(rootDir + fid.toString());
            file = new byte[fis.available()];
            fis.read(file);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close(fis);
        }
        return file;
    }

    public static void writeFile(FID fid, FileHandler handler, String rootDir) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(rootDir + fid);
            fos.write(handler.getBytes());
            fos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close(fos);
        }
    }

    public static int getStartUniquifier() {
        String[] uniquifiers = new File(Parameter.VICE_DIR).list();
        int maxUniquifier = Parameter.ROOT_FID.getUniquifier();
        for (String uniquifier : uniquifiers) {
            if (uniquifier.indexOf('.') < 0) {
                FID fid = new FID(uniquifier);
                if (fid.getUniquifier() > maxUniquifier) {
                    maxUniquifier = fid.getUniquifier();
                }
            }
        }
        return maxUniquifier + 1;
    }


    private static void createFile(FID fid, boolean isDir) {
        File file = new File(Parameter.VICE_DIR + fid.toString());

        if (!file.exists()) {
            FileOutputStream fos = null;
            try {
                file.createNewFile();
                int userId = fid.getUserId();
                long time = System.currentTimeMillis();
                FileHandler handler = FileHandler.createFileHandler(isDir ?
                        FileAttributes.createDirectoryAttributes(time, time, userId, userId)
                        : FileAttributes.createFileAttributes(time, time, 0, userId, userId));
                fos = new FileOutputStream(file);
                fos.write(handler.getBytes());
                fos.flush();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                close(fos);
            }
            //TODO callback promise
        }
    }


}