package data;

import java.io.File;

/**
 * Created by Dawnwords on 2014/5/4.
 */
public class Parameter {
    public static final int PORT = 2157;
    public static final String IP = "127.0.0.1";
    public static final String RMI_URL = String.format("rmi://%s:%d/vice", IP, PORT);

    public static final int FILE_ATTR_LEN = 28;
    public static final int ALIGN_LEN = 16;
    public static final int FILE_BLOCK_NAME_LEN = 12;
    public static final int FILE_ITEM_LEN = 64;
    public static final int FILE_NAME_LEN = FILE_ITEM_LEN - FILE_BLOCK_NAME_LEN;
    public static final int FILE_SIZE = 64 * 1024;

    public static final String VICE_DIR = "D:" + File.separator + "afs.server" + File.separator;
    public static final FID ROOT_FID = new FID(0, 0, 1);
    public static final FID NULL_FID = new FID(0, 0, 0);
    public static final String KEY = "19920526";
    public static final byte[] ALIGNMENT = new byte[Parameter.ALIGN_LEN + Parameter.FILE_BLOCK_NAME_LEN];

    public static String VENUS_DIR = "D:" + File.separator + "afs.client.%d" + File.separator;
}
