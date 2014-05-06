package data;

import util.DataTypeUtil;
import util.FileSystemUtil;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;

/**
 * Created by Dawnwords on 2014/5/4.
 */
public class FileAttributes implements Serializable {
    public static final int DIRECTORY_SIZE = -1;

    private long createdTime, modifiedTime;
    private int creatorId, modifierId;
    private int fileSize;

    public FileAttributes() {
    }

    public void modify(int userId, int newSize) {
        modifiedTime = System.currentTimeMillis();
        modifierId = userId;
        if (fileSize != DIRECTORY_SIZE) {
            fileSize = newSize;
        }
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public long getModifiedTime() {
        return modifiedTime;
    }

    public int getFileSize() {
        return fileSize;
    }

    public int getCreatorId() {
        return creatorId;
    }

    public int getModifierId() {
        return modifierId;
    }

    public boolean isFile() {
        return fileSize > 0;
    }

    public static FileAttributes createFileAttributes(long createdTime, long modifiedTime, int fileSize, int creatorId, int modifierId) {
        FileAttributes result = new FileAttributes();
        result.creatorId = creatorId;
        result.createdTime = createdTime;
        result.fileSize = fileSize;
        result.modifiedTime = modifiedTime;
        result.modifierId = modifierId;
        return result;
    }

    public static FileAttributes createFileAttributes(byte[] bytes) {
        FileAttributes result = new FileAttributes();
        result.createdTime = DataTypeUtil.byteArray2Long(DataTypeUtil.subArray(bytes, 0, 8));
        result.modifiedTime = DataTypeUtil.byteArray2Long(DataTypeUtil.subArray(bytes, 8, 8));
        result.creatorId = DataTypeUtil.byteArray2Int(DataTypeUtil.subArray(bytes, 16, 4));
        result.modifierId = DataTypeUtil.byteArray2Int(DataTypeUtil.subArray(bytes, 20, 4));
        result.fileSize = DataTypeUtil.byteArray2Int(DataTypeUtil.subArray(bytes, 24, 4));
        return result;
    }

    public static FileAttributes createDirectoryAttributes(long createdTime, long modifiedTime, int creatorId, int modifierId) {
        return createFileAttributes(createdTime, modifiedTime, DIRECTORY_SIZE, creatorId, modifierId);
    }

    public byte[] getBytes() {
        ByteArrayOutputStream bos = null;
        byte[] result = null;
        try {
            bos = new ByteArrayOutputStream();
            bos.write(DataTypeUtil.long2ByteArray(createdTime));
            bos.write(DataTypeUtil.long2ByteArray(modifiedTime));
            bos.write(DataTypeUtil.int2ByteArray(creatorId));
            bos.write(DataTypeUtil.int2ByteArray(modifierId));
            bos.write(DataTypeUtil.int2ByteArray(fileSize));
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

