package util;

import data.FID;
import data.FileAttributes;
import data.FileHandler;
import data.Parameter;
import vice.Log;

import java.io.*;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by Dawnwords on 2014/5/5.
 */
public class FileSystemUtil {

    public static void initRootDir() {
        File serverDir = new File(Parameter.VICE_DIR);
        if (!serverDir.exists()) {
            serverDir.mkdir();
        }
        createFile(Parameter.ROOT_FID, Parameter.ROOT_FID);
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

    public static synchronized FID createFile(long userId, int uniquifier, FID parentDir) {
        FID fid = new FID(userId, uniquifier);
        createFile(fid, parentDir);
        return fid;
    }

    public static FileHandler readFile(FID fid, String rootDir) {
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

        if (file != null) {
            FileAttributes attributes = FileAttributes.createFileAttributes(file);
            byte[] data = new byte[file.length - Parameter.FILE_ITEM_LEN];
            if (file.length > Parameter.FILE_ITEM_LEN) {
                System.arraycopy(file, Parameter.FILE_ITEM_LEN, data, 0, data.length);
            }
            return FileHandler.createFileHandler(attributes, data);
        }
        return null;
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


    private static void createFile(FID fid, FID parentDir) {
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
            //TODO callback promise
        }
    }

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

    public static boolean fileExist(FID fid, String rootDir) {
        return new File(rootDir + fid.toString()).exists();
    }


    public static boolean removeFile(FID fid, String rootDir) {
        return new File(rootDir + fid.toString()).delete();
    }

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
//        Log.getInstance().i("read local cp");
//        printCPMap(map);
        return map;
    }

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
//        Log.getInstance().i("write local cp");
//        printCPMap(map);
    }

    private static void printCPMap(Map<FID, Boolean> map) {
        Log.getInstance().i("-----------");
        for (FID fid : map.keySet()) {
            Log.getInstance().i("%s\t%s", map.get(fid) ? "Valid" : "Canceled", fid.toString());
        }
        Log.getInstance().i("-----------");
    }

    public static void main(String[] args) {
        Parameter.VENUS_DIR = String.format(Parameter.VENUS_DIR, 3309);
        Map<FID, Boolean> map = getLocalCallbackPromise();
        map.put(new FID(0, -1), true);
        storeLocalCallbackPromise(map);
        map = getLocalCallbackPromise();
        map.put(new FID(0, -1), false);
        storeLocalCallbackPromise(map);
    }


}
