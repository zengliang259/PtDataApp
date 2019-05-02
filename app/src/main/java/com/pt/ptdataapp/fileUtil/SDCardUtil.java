package com.pt.ptdataapp.fileUtil;

import android.os.Environment;
import android.os.StatFs;

import java.io.File;
import java.text.DecimalFormat;

public class SDCardUtil {
    public static double GetAvailablePercent() {
        double percent = 0;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File path = Environment.getExternalStorageDirectory();
            //取得sdcard文件路径
            StatFs statfs = new StatFs(path.getPath());
            //获取block的SIZE
            long blocSize = statfs.getBlockSizeLong();
            //获取BLOCK数量
            long totalBlocks = statfs.getBlockCountLong();
            //己使用的Block的数量
            long availaBlock = statfs.getAvailableBlocksLong();
            percent = availaBlock * 1.0 / totalBlocks;
        }
        return percent;
    }
    static String[] filesize(long size){
        String str="";
        if(size>=1024){
            str="KB";
            size/=1024;
            if(size>=1024){
                str="MB";
                size/=1024;
                }
            }
        DecimalFormat formatter=new DecimalFormat();
        formatter.setGroupingSize(3);
        String result[] =new String[2];
        result[0]=formatter.format(size);
        result[1]=str;
        return result;
    }
}
