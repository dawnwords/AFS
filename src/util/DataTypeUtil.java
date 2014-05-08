package util;

/**
 * Data Type Transforming Util Class
 * <p/>
 * Created by Dawnwords on 2014/5/5.
 */
public class DataTypeUtil {

    public static byte[] hexString2ByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public static String byteArray2HexString(byte[] bytes) {
        String result = "";
        for (byte b : bytes) {
            result += Integer.toHexString((b & 0xff) + 0x100).substring(1);
        }
        return result;
    }

    public static int byteArray2Int(byte[] bytes) {
        int val = 0;
        for (int i = 0; i < bytes.length; i++) {
            val <<= 8;
            val |= bytes[i] & 0xff;
        }
        return val;
    }

    public static byte[] int2ByteArray(int bytes) {
        return new byte[]{
                (byte) ((bytes >>> 24) & 0xff),
                (byte) ((bytes >>> 16) & 0xff),
                (byte) ((bytes >>> 8) & 0xff),
                (byte) (bytes & 0xff)
        };
    }

    public static long byteArray2Long(byte[] bytes) {
        long val = 0;
        for (int i = 0; i < bytes.length; i++) {
            val <<= 8;
            val |= bytes[i] & 0xff;
        }
        return val;
    }

    public static byte[] long2ByteArray(long bytes) {
        return new byte[]{
                (byte) ((bytes >>> 56) & 0xff),
                (byte) ((bytes >>> 48) & 0xff),
                (byte) ((bytes >>> 40) & 0xff),
                (byte) ((bytes >>> 32) & 0xff),
                (byte) ((bytes >>> 24) & 0xff),
                (byte) ((bytes >>> 16) & 0xff),
                (byte) ((bytes >>> 8) & 0xff),
                (byte) (bytes & 0xff)
        };
    }

    public static boolean byteArrayEquals(byte[] a1, byte[] a2) {
        if (a1.length != a2.length) {
            return false;
        }
        for (int i = 0; i < a1.length; i++) {
            if (a1[i] != a2[i]) {
                return false;
            }
        }
        return true;
    }

    public static long longHash(String string) {
        long h = 1125899906842597L; // prime
        int len = string.length();
        for (int i = 0; i < len; i++) {
            h = 31 * h + string.charAt(i);
        }
        return h;
    }

    public static byte[] subArray(byte[] array, int start, int length) {
        byte[] result = new byte[length];
        System.arraycopy(array, start, result, 0, length);
        return result;
    }

    public static void arrayWrite(byte[] tar, int pos, byte[] src) {
        System.arraycopy(src, 0, tar, pos, src.length);
    }

    public static byte[] arrayCombine(byte[] a1, byte[] a2) {
        byte[] result = new byte[a1.length + a2.length];
        arrayWrite(result, 0, a1);
        arrayWrite(result, a1.length, a2);
        return result;
    }

    public static String byteArray2String(byte[] fileName) {
        int i = 0;
        for (; i < fileName.length; i++) {
            if (fileName[i] == 0) {
                return new String(subArray(fileName, 0, i));
            }
        }
        return new String(fileName);
    }
}
