package com.pt.ptdataapp.utils;
import java.io.UnsupportedEncodingException;
import java.util.List;

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
    public static String Paper_EnterLine = "1B64";
    public static String Paper_EnterPoint = "1B4A";
    public static String Bold = "1B4501";
    public static String Bold_Cancel = "1B4500";
    public static String Set_Chinese = "1C26";
    public static String Set_UTF8 = "1B3901";
    public static String Horizontal_Line = "1D27030000C800C900900191013F02";
    public static String Cut_Paper = "1D5642";
    public static String Align_Left = "1B6100";
    public static String Align_Center = "1B6101";
    public static String Font_Size = "1D21";
    /**
     * 打印机初始化
     * printContentList 固定长度7，包含要打印的内容
     */
    public static byte[] StartPrint(List<String> printContentList) {
        String cmdStr = "";
        // 打印机初始化
        cmdStr += Init_Cmd;
        // 设置打印浓度（先默认）

        // 进纸两行
        cmdStr += Paper_EnterLine;
        cmdStr += StringUtil.ByteToHexString((byte) 2);
        // 测试
        for(int i = 0, len = printContentList.size(); i < len; i ++)
        {
            String content = printContentList.get(i);
            // 标题特殊处理
            if (i == 0)
            {
                cmdStr += Align_Center;
                cmdStr += Bold;
                cmdStr += Font_Size;
                cmdStr += StringUtil.ByteToHexString((byte) 17);
                cmdStr += GetSingleTextCmdStr(content);
                cmdStr += Paper_EnterPoint;
                cmdStr += StringUtil.ByteToHexString((byte) 10);
                cmdStr += Bold_Cancel;
            }
            else
            {
                cmdStr += Align_Left;
                cmdStr += Font_Size;
                cmdStr += StringUtil.ByteToHexString((byte) 0);
                cmdStr += GetSingleTextCmdStr(content);
                cmdStr += Paper_EnterPoint;
                cmdStr += StringUtil.ByteToHexString((byte) 10);
                cmdStr += Horizontal_Line;
                cmdStr += Horizontal_Line;
                cmdStr += Horizontal_Line;
            }
            cmdStr += Paper_EnterLine;
            cmdStr += StringUtil.ByteToHexString((byte) 2);
        }
        // 切纸
        cmdStr += Cut_Paper;
        cmdStr += StringUtil.ByteToHexString((byte) 2);
        return StringUtil.hexString2Bytes(cmdStr);
    }

    public static byte[] EnterPaper(int n)
    {
        String cmdStr = "";
        // 打印机初始化
        cmdStr += Init_Cmd;
        // 设置打印浓度（先默认）

        // 进纸两行
        cmdStr += Paper_EnterLine;
        cmdStr += StringUtil.ByteToHexString((byte) n);

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
