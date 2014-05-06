package util;

import java.net.InetAddress;

/**
 * Created by Dawnwords on 2014/5/5.
 */
public class IPUtil {
    public static int string2Int(String ip) {
        try {
            byte[] address = InetAddress.getByName(ip).getAddress();
            return DataTypeUtil.byteArray2Int(address);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static String int2String(int ip) {
        try {
            byte[] addr = DataTypeUtil.int2ByteArray(ip);
            return InetAddress.getByAddress(addr).getHostAddress();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        int x = IPUtil.string2Int("192.168.0.2");
        System.out.println(x);
        System.out.println(IPUtil.int2String(x));
    }

}
