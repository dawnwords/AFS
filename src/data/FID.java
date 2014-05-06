package data;

import util.DataTypeUtil;
import util.FileSystemUtil;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;

/**
 * Created by Dawnwords on 2014/5/5.
 */
public class FID implements Serializable {
    private int userId;
    private int part;
    private int uniquifier;

    public FID() {
    }

    public FID(int userId, int part, int uniquifier) {
        this.userId = userId;
        this.part = part;
        this.uniquifier = uniquifier;
    }

    public FID(String hexString) {
        this(DataTypeUtil.hexString2ByteArray(hexString));
    }

    public FID(byte[] bytes) {
        this.userId = DataTypeUtil.byteArray2Int(DataTypeUtil.subArray(bytes, 0, 4));
        this.part = DataTypeUtil.byteArray2Int(DataTypeUtil.subArray(bytes, 4, 4));
        this.uniquifier = DataTypeUtil.byteArray2Int(DataTypeUtil.subArray(bytes, 8, 4));
    }

    public int getUniquifier() {
        return uniquifier;
    }

    public int getPart() {
        return part;
    }

    public int getUserId() {
        return userId;
    }

    public byte[] getBytes() {
        byte[] result = new byte[12];
        DataTypeUtil.arrayWrite(result, 0, DataTypeUtil.int2ByteArray(userId));
        DataTypeUtil.arrayWrite(result, 4, DataTypeUtil.int2ByteArray(part));
        DataTypeUtil.arrayWrite(result, 8, DataTypeUtil.int2ByteArray(uniquifier));
        return result;
    }

    private byte[] parseFID(int userId, int part, int uniquifier) {
        byte[] result = null;
        ByteArrayOutputStream bos = null;
        try {
            bos = new ByteArrayOutputStream();
            bos.write(DataTypeUtil.int2ByteArray(userId));
            bos.write(DataTypeUtil.int2ByteArray(part));
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
        return DataTypeUtil.byteArray2HexString(parseFID(userId, part, uniquifier));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FID fid = (FID) o;

        if (part != fid.part) return false;
        if (uniquifier != fid.uniquifier) return false;
        if (userId != fid.userId) return false;

        return true;
    }
}
