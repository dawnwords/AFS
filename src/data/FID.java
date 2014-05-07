package data;

import util.DataTypeUtil;
import util.FileSystemUtil;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;

/**
 * Created by Dawnwords on 2014/5/5.
 */
public class FID implements Serializable {
    private long userId;
    private int uniquifier;

    public FID() {
    }

    public FID(long userId, int uniquifier) {
        this.userId = userId;
        this.uniquifier = uniquifier;
    }

    public FID(String hexString) {
        this(DataTypeUtil.hexString2ByteArray(hexString));
    }

    public FID(byte[] bytes) {
        this.userId = DataTypeUtil.byteArray2Long(DataTypeUtil.subArray(bytes, 0, 8));
        this.uniquifier = DataTypeUtil.byteArray2Int(DataTypeUtil.subArray(bytes, 8, 4));
    }

    public int getUniquifier() {
        return Math.abs(uniquifier);
    }

    public long getUserId() {
        return userId;
    }

    public boolean isDirectory() {
        return uniquifier < 0;
    }

    public byte[] getBytes() {
        byte[] result = new byte[12];
        DataTypeUtil.arrayWrite(result, 0, DataTypeUtil.long2ByteArray(userId));
        DataTypeUtil.arrayWrite(result, 8, DataTypeUtil.int2ByteArray(uniquifier));
        return result;
    }

    private byte[] parseFID(long userId, int uniquifier) {
        byte[] result = null;
        ByteArrayOutputStream bos = null;
        try {
            bos = new ByteArrayOutputStream();
            bos.write(DataTypeUtil.long2ByteArray(userId));
            bos.write(DataTypeUtil.int2ByteArray(uniquifier));
            bos.flush();
            result = bos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            FileSystemUtil.close(bos);
        }
        return result;
    }

    @Override
    public String toString() {
        return DataTypeUtil.byteArray2HexString(parseFID(userId, uniquifier));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FID fid = (FID) o;

        if (uniquifier != fid.uniquifier) return false;
        if (userId != fid.userId) return false;

        return true;
    }
}
