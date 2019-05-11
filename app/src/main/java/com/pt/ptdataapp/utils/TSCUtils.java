package com.pt.ptdataapp.utils;
import android.util.Log;

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
    public static String Left_Empty = "1D4C"; // 1d 4c nL nH
    public static String UnderLine_Start1 = "1C2D"; // 1C 2D 02 下划线高度02
    public static String UnderLine_Start2 = "1B2D"; // 1C 2D 02 下划线高度02  1C 2D 00 取消
    public static String UnderLine_End = "0D0A";
    public static String Indentation = "1B24"; // 1B 24 29 00 缩进29h

    public static String IDStr = "ID:";
    public static String NameStr = "姓名:";
    public static String INRStr = "INR:";
    public static String DoctorNameStr = "报告医生:";
    public static String CheckDateStr = "检测日期:";
    public static String ReportDateStr = "报告日期:";

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
//        cmdStr += EnterPaperLine(2);
        // 标题
        // 设置字体加粗
        cmdStr += Bold;
        cmdStr += Align_Center;
        cmdStr += "1B2101"; // 字符方式，压缩ascci
        cmdStr += Font_Size;
        cmdStr += StringUtil.ByteToHexString((byte) 17);
        String content = printContentList.get(0);
        cmdStr += GetSingleTextCmdStr("", content, false);

        cmdStr += Horizontal_Line;
        cmdStr += Horizontal_Line;
        cmdStr += Horizontal_Line;
        cmdStr += Horizontal_Line;
        cmdStr += EnterPaperLine(1);

        cmdStr += Bold_Cancel;
        // ID
        cmdStr += Indent(41, 0);
        cmdStr += Align_Left;
        cmdStr += Font_Size;
        cmdStr += StringUtil.ByteToHexString((byte) 17);
        content = printContentList.get(1);
        cmdStr += GetSingleTextCmdStr(IDStr, content, true);

        // 姓名
        cmdStr += Indent(41, 0);
        content = printContentList.get(2);
        cmdStr += GetSingleTextCmdStr(NameStr, content, true);
        cmdStr += EnterPaperLine(1);

        // INR
        cmdStr += Font_Size;
        cmdStr += StringUtil.ByteToHexString((byte)51);
        cmdStr += UnderLine_Start2;
        cmdStr += StringUtil.ByteToHexString((byte) 0); // 取消下划线
        cmdStr += Bold;
        cmdStr += Indent(41, 0);
        content = printContentList.get(3);
        cmdStr += GetSingleTextCmdStr(INRStr, content, false);
        cmdStr += EnterPaperLine(1);

        cmdStr += Bold_Cancel;
        cmdStr += "1B2101"; // 字符方式，压缩ascci
        cmdStr += Font_Size;
        cmdStr += StringUtil.ByteToHexString((byte) 17);
        cmdStr += UnderLine_Start1;
        cmdStr += StringUtil.ByteToHexString((byte) 0);
        cmdStr += Bold_Cancel;

        // 报告医生
        cmdStr += Indent(41,0);
        content = printContentList.get(4);
        cmdStr += GetSingleTextCmdStr(DoctorNameStr,content,true);
        cmdStr += UnderLine_Start1;
        cmdStr += StringUtil.ByteToHexString((byte) 0);

        // 检测日期

        cmdStr += Indent(41,0);
        content = printContentList.get(5);
        cmdStr += GetSingleTextCmdStr(CheckDateStr,content,true);

        // 报告日期

        cmdStr += Indent(41,0);
        content = printContentList.get(6);
        cmdStr += GetSingleTextCmdStr(ReportDateStr,content,true);
        // 切纸
        cmdStr += Cut_Paper;
        cmdStr += StringUtil.ByteToHexString((byte) 2);

        Log.d("Print", cmdStr);
//        cmdStr = TestCmd();
        return StringUtil.hexString2Bytes(cmdStr);
    }

    public static String EnterPaperLine(int n)
    {
        String cmdStr = "";
        // 进纸两行
        cmdStr += Paper_EnterLine;
        cmdStr += StringUtil.ByteToHexString((byte) n);

        return cmdStr;
    }

    public static String Indent(int H, int L)
    {
        String cmdStr = "";
        cmdStr += Indentation;
        cmdStr += StringUtil.ByteToHexString((byte) H); // 0x29
        cmdStr += StringUtil.ByteToHexString((byte) L);
        return cmdStr;
    }

    private static String GetSingleTextCmdStr(String titleText, String printText, boolean needUnderLine)
    {
        String cmdStr = "";
        cmdStr += Set_Chinese;
        cmdStr += Set_UTF8;
        if (titleText.length() > 0)
        {
            cmdStr += StringUtil.StringToHexString(titleText);
        }
        String emptyStr = "";
        if (needUnderLine)
        {
            cmdStr += UnderLine_Start2;
            cmdStr += StringUtil.ByteToHexString((byte) 2);
            if (printText.length() == 0)
            {
                emptyStr = "      ";
            }
        }

        cmdStr += StringUtil.StringToHexString(printText + emptyStr);

        cmdStr += UnderLine_End;
        return cmdStr;
    }

    public static String TestCmd()
    {
        String cmdStr = "1B40"+
                "1B4501"+
                "1B6101"+
                "1b2101"+
                "1D2111"+
                "1C26"+
                "C1EBC4CFD2BDD4BA0D0A"+
                "494E52BCECB2E2B1A8B8E6B5A50D0A"+
                "1D27030000C800C900900191013F02"+
                "1D27030000C800C900900191013F02"+
                "1D27030000C800C900900191013F02"+
                "1D27030000C800C900900191013F02"+
                "1B6401"+
                "1B4500"+
                "1B6100"+
                "1b242900"+
                "1D2111"+
                "49443A1B2D0238303235383336392020200D0A"+
                "1b242900"+
                "D0D5C3FB3A1C2D02C0EED0A1C7BF2020200D0A"+
                "1B6401"+
                "1D2133"+
                "1b2d00"+
                "1B4501"+
                "1b242900"+
                "494E523A312E300D0A"+
                "1B6401"+
                "1b4500"+
                "1b2101"+
                "1D2111"+
                "1C2D00"+
                "1B4500"+
                "1b242900"+
                "B1A8B8E6D2BDC9FA3A1C2D02BAD8B4F3C7BF2020200D0A"+
                "1C2D00"+
                "1b242900"+
                "BCECB2E2C8D5C6DA3A1B2D0232303139303331310D0A"+
                "1b242900"+
                "B1A8B8E6C8D5C6DA3A1B2D0232303139303331310D0A"+
                "1D564200";
        return cmdStr;
    }
}
