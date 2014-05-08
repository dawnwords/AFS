package util;

import data.FID;
import data.FileAttributes;
import data.FileHandler;
import data.Parameter;

import java.io.*;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * File System Operation Util Class
 * </p>
 * Created by Dawnwords on 2014/5/5.
 */
public class FileSystemUtil {

    /**
     * Init root Dir
     *
     * @param rootDir VICE or VENUS root dir
     */
    public static void initRootDir(String rootDir) {
        File serverDir = new File(rootDir);
        if (!serverDir.exists()) {
            serverDir.mkdir();
        }
    }

    /**
     * Close a Closeable instance
     *
     * @param closeable to be close
     */
    public static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Read a file of given fid and get its data as file handler
     *
     * @param fid     fid of the file
     * @param rootDir VICE or VENUS root dir
     * @return the file handler of the corresponding file
     */
    public static FileHandler readFile(FID fid, String rootDir) {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(rootDir + fid.toString());
            byte[] file = new byte[fis.available()];
            fis.read(file);

            FileAttributes attributes = FileAttributes.createFileAttributes(file);
            byte[] data = new byte[file.length - Parameter.FILE_ITEM_LEN];
            if (file.length > Parameter.FILE_ITEM_LEN) {
                System.arraycopy(file, Parameter.FILE_ITEM_LEN, data, 0, data.length);
            }
            return FileHandler.createFileHandler(attributes, data);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close(fis);
        }

        return null;
    }

    /**
     * Save file handler to the file of given fid
     *
     * @param fid     fid of the file
     * @param handler file handler holding data
     * @param rootDir VICE or VENUS root dir
     */
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

    /**
     * Write the file of given fid with data if data are different form its origin
     *
     * @param fid  fid of the file
     * @param data
     * @return if data not change return false, otherwise return true
     */
    public static boolean writeWithChecksum(FID fid, byte[] data) {
        DigestInputStream dis = null;
        FileOutputStream fos = null;
        String fileName = Parameter.VENUS_DIR + fid.toString();
        byte[] newMD5, oldMD5;
        try {
            if (new File(fileName).exists()) {
                MessageDigest md = MessageDigest.getInstance("MD5");
                newMD5 = md.digest(data);

                dis = new DigestInputStream(new FileInputStream(fileName), md);
                dis.read(new byte[dis.available()]);
                oldMD5 = md.digest();

                if (DataTypeUtil.byteArrayEquals(newMD5, oldMD5)) {
                    return false;
                }
            }

            fos = new FileOutputStream(fileName);
            fos.write(data);
            fos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close(dis);
            close(fos);
        }
        return true;
    }

    /**
     * Vice Server use this method to get the max uniquifier
     * based on FID of files already exists
     *
     * @return max uniqifier on server side
     */
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

    /**
     * Create File of given fid at given dir
     *
     * @param fid       fid of the file
     * @param parentDir fid of the dir
     */
    public static void createFile(FID fid, FID parentDir) {
        File file = new File(Parameter.VICE_DIR + fid.toString());

        if (!file.exists()) {
            FileOutputStream fos = null;
            try {
                file.createNewFile();
                long userId = fid.getUserId();
                long time = System.currentTimeMillis();
                FileAttributes attr = FileAttributes.createFileAttributes(time, time, 0, userId, userId, parentDir);
                FileHandler handler = FileHandler.createFileHandler(attr);
                fos = new FileOutputStream(file);
                fos.write(handler.getBytes());
                fos.flush();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                close(fos);
            }
        }
    }

    /**
     * Resolve data part of file handler of a directory
     * to get a file name -> FID mapping of its child files
     *
     * @param handler file handler of a file
     * @return file name -> FID mapping of child files in the directory
     */
    public static Map<String, FID> getNameFIDMap(FileHandler handler) {
        int i = 0;
        byte[] data = handler.getData();
        Map<String, FID> map = new LinkedHashMap<String, FID>();
        while (i < data.length) {
            byte[] fileName = new byte[Parameter.FILE_NAME_LEN];
            byte[] fid = new byte[Parameter.FILE_BLOCK_NAME_LEN];
            System.arraycopy(data, i, fileName, 0, fileName.length);
            System.arraycopy(data, i + fileName.length, fid, 0, fid.length);
            map.put(DataTypeUtil.byteArray2String(fileName), new FID(fid));
            i += Parameter.FILE_ITEM_LEN;
        }
        return map;
    }

    /**
     * @param fid     fid of the file
     * @param rootDir VICE or VENUS root dir
     * @return whether the file exists
     */
    public static boolean fileExist(FID fid, String rootDir) {
        return new File(rootDir + fid.toString()).exists();
    }

    /**
     * Remove file with given fid
     *
     * @param fid     fid of the file to remove
     * @param rootDir VICE or VENUS root dir
     * @return remove successfully or not
     */
    public static boolean removeFile(FID fid, String rootDir) {
        return new File(rootDir + fid.toString()).delete();
    }

    /**
     * Vice Server record callback promises of each file with '[fid].cp' file
     * this method add a callback promise for the user to the given fid
     *
     * @param fid    fid of the file
     * @param userId user id
     */
    public static void addCallbackPromise(FID fid, long userId) {
        List<Long> origin = getCallbackPromiseList(fid);
        if (!origin.contains(userId)) {
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(Parameter.VICE_DIR + fid.toString() + Parameter.CALLBACK_PROMISE_EXT, true);
                fos.write(DataTypeUtil.long2ByteArray(userId));
                fos.flush();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                close(fos);
            }
        }
    }

    /**
     * Vice Server record callback promises of each file with '[fid].cp' file
     * this method return the list of user containing in the .cp file of given fid
     *
     * @param fid fid of the file
     * @return the list of user containing in the .cp file of given fid
     */
    public static List<Long> getCallbackPromiseList(FID fid) {
        FileInputStream fis = null;
        List<Long> result = new LinkedList<Long>();
        try {
            fis = new FileInputStream(Parameter.VICE_DIR + fid.toString() + Parameter.CALLBACK_PROMISE_EXT);
            byte[] item = new byte[8];
            while ((fis.read(item)) > 0) {
                result.add(DataTypeUtil.byteArray2Long(item));
            }
        } catch (Exception ignored) {
        } finally {
            close(fis);
        }
        return result;
    }

    /**
     * Venus Client record callback promise in '.cp' file in its directory
     * this method read this file and get the mapping of FID -> Valid|Canceled
     *
     * @return the mapping of FID -> Valid|Canceled
     */
    public static Map<FID, Boolean> getLocalCallbackPromise() {
        Map<FID, Boolean> map = new LinkedHashMap<FID, Boolean>();
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(Parameter.VENUS_DIR + Parameter.CALLBACK_PROMISE_EXT);
            byte[] fid = new byte[12];
            byte[] valid = new byte[1];
            while ((fis.read(fid)) > 0) {
                fis.read(valid);
                map.put(new FID(fid), valid[0] != 0);
            }
        } catch (Exception ignored) {
        } finally {
            close(fis);
        }
        return map;
    }

    /**
     * Venus Client record callback promise in '.cp' file in its directory
     * this method write the mapping of FID -> Valid|Canceled back to '.cp'
     *
     * @param map mapping of FID -> Valid|Canceled
     */
    public static void storeLocalCallbackPromise(Map<FID, Boolean> map) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(Parameter.VENUS_DIR + Parameter.CALLBACK_PROMISE_EXT);
            for (FID fid : map.keySet()) {
                fos.write(fid.getBytes());
                fos.write(new byte[]{(byte) (map.get(fid) ? 1 : 0)});
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close(fos);
        }
    }
}
