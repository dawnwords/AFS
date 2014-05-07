package data;

import util.DataTypeUtil;
import util.FileSystemUtil;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;

/**
 * Created by Dawnwords on 2014/5/4.
 */
public class FileAttributes implements Serializable {
    private long createdTime, modifiedTime;
    private long creatorId, modifierId;
    private int fileSize;
    private FID parentDir;

    public FileAttributes() {
    }

    public void modify(long userId, int newSize) {
        modifiedTime = System.currentTimeMillis();
        modifierId = userId;
        fileSize = newSize;
    }

    public FID getParentDir() {
        return parentDir;
    }

    public static FileAttributes createFileAttributes(long createdTime, long modifiedTime,
                                                      int fileSize, long creatorId, long modifierId, FID parentDir) {
        FileAttributes result = new FileAttributes();
        result.creatorId = creatorId;
        result.createdTime = createdTime;
        result.fileSize = fileSize;
        result.modifiedTime = modifiedTime;
        result.modifierId = modifierId;
        result.parentDir = parentDir;
        return result;
    }

    public static FileAttributes createFileAttributes(byte[] bytes) {
        FileAttributes result = new FileAttributes();
        result.createdTime = DataTypeUtil.byteArray2Long(DataTypeUtil.subArray(bytes, 0, 8));
        result.modifiedTime = DataTypeUtil.byteArray2Long(DataTypeUtil.subArray(bytes, 8, 8));
        result.creatorId = DataTypeUtil.byteArray2Long(DataTypeUtil.subArray(bytes, 16, 8));
        result.modifierId = DataTypeUtil.byteArray2Long(DataTypeUtil.subArray(bytes, 24, 8));
        result.fileSize = DataTypeUtil.byteArray2Int(DataTypeUtil.subArray(bytes, 32, 4));
        result.parentDir = new FID(DataTypeUtil.subArray(bytes, 36, 12));
        return result;
    }

    public byte[] getBytes() {
        ByteArrayOutputStream bos = null;
        byte[] result = null;
        try {
            bos = new ByteArrayOutputStream();
            bos.write(DataTypeUtil.long2ByteArray(createdTime));
            bos.write(DataTypeUtil.long2ByteArray(modifiedTime));
            bos.write(DataTypeUtil.long2ByteArray(creatorId));
            bos.write(DataTypeUtil.long2ByteArray(modifierId));
            bos.write(DataTypeUtil.int2ByteArray(fileSize));
            bos.write(parentDir.getBytes());
            bos.flush();
            result = bos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            FileSystemUtil.close(bos);
        }
        return result;
    }
}

