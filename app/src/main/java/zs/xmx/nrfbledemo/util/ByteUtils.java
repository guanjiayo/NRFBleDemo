package zs.xmx.nrfbledemo.util;

import java.io.UnsupportedEncodingException;

public class ByteUtils {

    /**
     * @param data 要转换的字节数组
     *             只适用于一到三个字节
     *             如:
     *             byte[] bytes =new byte[15] //比方这个是需要解析的 15个字节得数组
     *             那么传  getResultByLen(new byte[]{bytes[0],byte[1]})
     *             就相当于解析了第一个和第二个字节得数据
     */
    public static int getResultByLen(byte[] data) {
        int length = data.length;
        int result = 0x00000000;
        for (int i = 0; i < length; i++) {
            result |= (data[i] & 0x000000ff) << (length - i - 1) * 8;
        }
        return result & (0xffffffff >>> ((4 - length) * 8));
    }

    public static int byteToInt22(byte[] data) {
        return (data[0] | (8 << data[1]) | (16 << data[2]) | (24 << data[3])) & 0x00000000ffffffff;
    }

    /**
     * 高字节在前,byte转int
     */
    public static int byteToInt33(byte[] data) {
        return (data[3] | (8 << data[2]) | (16 << data[1]) | (24 << data[0])) & 0x00000000ffffffff;
    }

    /**
     * 低字节在前,4byte转int
     */
    public static int bytes2IntLow(byte[] bytes) {
        int value = 0;
        value = ((bytes[3] & 0xff) << 24) |
                ((bytes[2] & 0xff) << 16) |
                ((bytes[1] & 0xff) << 8) |
                (bytes[0] & 0xff);
        return value;
    }

    /**
     * 高字节在前,4byte转int
     */
    public static int bytes2Int(byte[] bytes) {
        int value = 0;
        value = ((bytes[0] & 0xff) << 24) |
                ((bytes[1] & 0xff) << 16) |
                ((bytes[2] & 0xff) << 8) |
                (bytes[3] & 0xff);
        return value;
    }

    /**
     * 高字节在前, int 转 4个byte
     */
    public static byte[] intTo4Byte(int n) {
        byte[] b = new byte[4];
        b[3] = (byte) (n & 0xff);
        b[2] = (byte) (n >> 8 & 0xff);
        b[1] = (byte) (n >> 16 & 0xff);
        b[0] = (byte) (n >> 24 & 0xff);
        return b;
    }

    /**
     * 高字节在前, int 转 2个byte
     */
    public static byte[] intTo2Byte(int n) {
        byte[] b = new byte[2];
        b[1] = (byte) (n & 0xff);
        b[0] = (byte) (n >> 8 & 0xff);
        return b;
    }

    /**
     * 高字节在前, int 转 1个byte
     */
    public static byte[] intToByte(int n) {
        byte[] b = new byte[1];
        b[0] = (byte) (n & 0xff);
        return b;
    }

    /**
     * 16进制转String
     */
    public static String hexStr2Str(String s) {
        byte[] baKeyword = new byte[s.length() / 2];
        for (int i = 0; i < baKeyword.length; i++) {
            try {
                baKeyword[i] = (byte) (0xff & Integer.parseInt(s.substring(
                        i * 2, i * 2 + 2), 16));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            s = new String(baKeyword, "utf-8");// UTF-16le:Not
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return s;
    }

    /**
     * 字符串转16进制
     */
    public static String str2HexStr(String str) {
        char[] chars = "0123456789ABCDEF".toCharArray();
        StringBuilder sb = new StringBuilder("");
        byte[] bs = new byte[0];
        try {
            bs = str.getBytes("UTF-8");
            int bit;
            for (int i = 0; i < bs.length; i++) {
                bit = (bs[i] & 0x0f0) >> 4;
                sb.append(chars[bit]);
                bit = bs[i] & 0x0f;
                sb.append(chars[bit]);
                // sb.append(' ');
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return sb.toString().trim();
    }

}
