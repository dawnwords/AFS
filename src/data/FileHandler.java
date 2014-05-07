package data;

import util.DataTypeUtil;

import java.io.Serializable;

/**
 * Created by Dawnwords on 2014/5/4.
 */
public class FileHandler implements Serializable {
    private FileAttributes attributes;
    private byte[] data;

    public FileHandler() {
        data = new byte[0];
    }

    public FileAttributes getAttributes() {
        return attributes;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public byte[] getBytes() {
        byte[] result = new byte[data.length + Parameter.FILE_ITEM_LEN];
        DataTypeUtil.arrayWrite(result, 0, attributes.getBytes());
        DataTypeUtil.arrayWrite(result, Parameter.FILE_ITEM_LEN, data);
        return result;
    }

    public static FileHandler createFileHandler(FileAttributes attributes) {
        return createFileHandler(attributes, new byte[0]);
    }

    public static FileHandler createFileHandler(byte[] data) {
        return createFileHandler(null, data);
    }

    public static FileHandler createFileHandler(FileAttributes attributes, byte[] data) {
        FileHandler handler = new FileHandler();
        handler.attributes = attributes;
        handler.data = data;
        return handler;
    }
}
