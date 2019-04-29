package com.pt.ptdataapp.utils;

public class StringUtil {

    public static byte[] GetUTF8Byte(String content){
        try
        {
            return content.getBytes("UTF-8");
        }
        catch (Exception e)
        {
            return null;
        }

    }

    public static String StringToHexString(String content)
    {
        byte[] bytes = GetUTF8Byte(content);
        if (bytes != null)
        {
            return bytes2HexString(bytes);
        }
        else
        {
            return "";
        }
    }

    public static String ByteToHexString(byte src)
    {
        return String.format("%02X",src);
    }
    /**
     * @Title:bytes2HexString
     * @Description:字节数组转16进制字符串
     * @param b
     * 字节数组
     * @return 16进制字符串
     * @throws
     */
    public static String bytes2HexString(byte[] b) {
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < b.length; i++) {
            result.append(ByteToHexString(b[i]));
        }
        return result.toString();
    }

    /**
     * @Title:hexString2Bytes
     * @Description:16进制字符串转字节数组
     * @param src
     * 16进制字符串
     * @return 字节数组
     * @throws
     */
    public static byte[] hexString2Bytes(String src) {
        int l = src.length() / 2;
        byte[] ret = new byte[l];
        for (int i = 0; i < l; i++) {
            ret[i] = Integer.valueOf(src.substring(i * 2, i * 2 + 2), 16).byteValue();
        }
        return ret;
    }
}
