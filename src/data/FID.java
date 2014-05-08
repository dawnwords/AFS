package data;

import util.DataTypeUtil;

import java.io.Serializable;

/**
 * File Identifier Class
 * </p>
 * +--------------------+---------------+
 * |        userId      |   uniquifier  |
 * +--------------------+---------------+
 * 0                    64              96
 * </p>
 * Created by Dawnwords on 2014/5/5.
 */
public class FID implements Serializable {
    private long userId;
    private int uniquifier;

    /**
     * Constructors
     */
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

    /**
     * Getter
     */
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

    @Override
    public String toString() {
        return DataTypeUtil.byteArray2HexString(getBytes());
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

    @Override
    public int hashCode() {
        int result = (int) (userId ^ (userId >>> 32));
        result = 31 * result + uniquifier;
        return result;
    }
}
