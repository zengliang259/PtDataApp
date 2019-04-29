package com.pt.ptdataapp.utils;
import java.io.UnsupportedEncodingException;

/**
 * TSC 指令工具类
 * 打印机不支持位图打印
 * 打印内容，全靠指令拼凑
 * 一个打印界面，都要一个一个命令的拼凑
 * 实在鸡肋
 */

public class TSCUtils {
    public static String Init_Cmd = "1B40";
    public static String Set_Print_Density = "1B37";
    public static String Paper_Enter = "1B64";
    public static String Align_Left = "1B6100";
    public static String Bold = "1B4501";
    public static String Bold_Cancel = "1B4500";
    public static String Set_Chinese = "1C26";
    public static String Set_UTF8 = "1B3901";
    public static String Horizontal_Line = "1D270100007800";
    public static String Cut_Paper = "1D564200";
    /**
     * 打印机初始化
     * printContentList 固定长度7，包含要打印的内容
     */
    public static byte[] StartPrint(String[] printContentList) {
        String cmdStr = "";
        // 打印机初始化
        cmdStr += Init_Cmd;
        // 设置打印浓度（先默认）

        // 进纸两行
        cmdStr += Paper_Enter;
        cmdStr += StringUtil.ByteToHexString((byte) 2);
        // 测试

        for(int i = 0, len = printContentList.length; i < len ;i ++)
        {
            cmdStr += GetSingleTextCmdStr(printContentList[i]);
            cmdStr += Horizontal_Line;
            cmdStr += Horizontal_Line;
            cmdStr += Horizontal_Line;
            cmdStr += Paper_Enter;
            cmdStr += StringUtil.ByteToHexString((byte) 2);
        }
        // 切纸
        cmdStr += Cut_Paper;
        return StringUtil.hexString2Bytes(cmdStr);
    }

    private static String GetSingleTextCmdStr(String printText)
    {
        String cmdStr = "";
        cmdStr += Set_Chinese;
        cmdStr += Set_UTF8;
        cmdStr += StringUtil.StringToHexString(printText);
        return cmdStr;
    }
}
