package com.pt.ptdataapp.fileUtil;

import com.pt.ptdataapp.Model.PatientInfo;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class FileDataReader
{
    public enum DataNameEnum
    {
        PatientID("Patient ID"),
        INR("INR"),
        TestDate("Test Date"),
        ErrorCode("Error Code"),
        TestID("Test ID"),
        Pt("PT(s)");

        private String text;

        DataNameEnum(String text) {
            this.text = text;
        }

        public String getText() {
            return this.text;
        }

        // Implementing a fromString method on an enum type
        private static final Map<String, DataNameEnum> stringToEnum = new HashMap<String, DataNameEnum>();
        static {
            // Initialize map from constant name to enum constant
            for(DataNameEnum item : values()) {
                stringToEnum.put(item.toString(), item);
            }
        }

        // Returns enum for string, or null if string is invalid
        public static DataNameEnum fromString(String symbol) {
            return stringToEnum.get(symbol);
        }

        @Override
        public String toString() {
            return text;
        }
    }
    public static PatientInfo Read(String content)
    {
        PatientInfo info = new PatientInfo();
        String[] splitStrs = content.split("\n");
        int matchCode = 0;
        for (int i = 0,len = splitStrs.length; i < len; i ++)
        {
            matchCode = MatchImportantData(splitStrs[i], info, matchCode);
        }
        // 报告时间获取系统时间
        Date dt = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        info.reportDate = sdf.format(dt);

        return info;
    }

    public static String Write(PatientInfo info)
    {
        return "";
    }
    private static int MatchImportantData(String itemStr, PatientInfo info, int curMatchCode)
    {
        String tempStr = itemStr.replace("\r", "");
        String[] splitStr = tempStr.split("\t");
        if (splitStr.length >= 2)
        {
            if ((curMatchCode >> 0 & 0x01) <= 0 && (splitStr[0].indexOf(DataNameEnum.PatientID.toString()) >= 0))
            {
                curMatchCode = curMatchCode | 1 << 0;
                info.ID = splitStr[1];
            }
            else if ((curMatchCode >> 1 & 0x01) <= 0 && (splitStr[0].indexOf(DataNameEnum.INR.toString()) >= 0))
            {
                curMatchCode = curMatchCode | 1 << 1;

                info.checkResult = splitStr[1];

            }
            else if ((curMatchCode >> 2 & 0x01) <= 0 && (splitStr[0].indexOf(DataNameEnum.TestDate.toString()) >= 0))
            {
                curMatchCode = curMatchCode | 1 << 2;
                info.checkDate = splitStr[1];
            }
            else if ((curMatchCode >> 3 & 0x01) <= 0 && (splitStr[0].indexOf(DataNameEnum.Pt.toString()) >= 0))
            {
                curMatchCode = curMatchCode | 1 << 3;
                info.Pt = splitStr[1];
            }
            else if ((curMatchCode >> 4 & 0x01) <= 0 && (splitStr[0].indexOf(DataNameEnum.ErrorCode.toString()) >= 0))
            {
                curMatchCode = curMatchCode | 1 << 4;
                info.errorCode = splitStr[1];
            }
            else if ((curMatchCode >> 5 & 0x01) <= 0 && (splitStr[0].indexOf(DataNameEnum.TestID.toString()) >= 0))
            {
                curMatchCode = curMatchCode | 1 << 5;
                info.testID = splitStr[1];
            }
        }

        return curMatchCode;
    }
}
