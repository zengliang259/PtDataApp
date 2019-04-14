package com.pt.ptdataapp.Model;

import android.os.Environment;
import android.util.Log;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocalFileModel {
    private static final String TAG = "LocalFileModel";
    public static final String DATA_PATH = "PtData/";
    public final String FILE_TYPE = "txt";
    private static LocalFileModel _instance;
    public static LocalFileModel getInstance() {
        if (_instance == null)
        {
            _instance = new LocalFileModel();
        }
        return _instance;
    }
    // 文件索引map, key：文件名， value:文件路径
    private Map<String, String> fileMap = new HashMap<String, String>();
    // 文件目录索引 key是一级目录，value 是目录下的文件列表
    // 注意，文件系统只有两级目录
    private Map<String, List<String>> fileDirMap = new HashMap<String, List<String>>();

    public Map<String, String> getFileMap()
    {
        return fileMap;
    }

    public Map<String, List<String>> getDirMap()
    {
        return fileDirMap;
    }
    public void ReadLocalFiles(String dirPath) {
        File rootDir = new File(Environment.getExternalStorageDirectory(), dirPath);
        if (!rootDir.exists()) {//判断路径是否存在
            return ;
        }
        File[] dirs = rootDir.listFiles();
        if(dirs == null){//判断权限
            return ;
        }
        List<File> list = Arrays.asList(dirs);
        Log.d(TAG,"len =" + dirs.length);
        for (File dir : dirs) {//遍历一级目录
            if(dir.isDirectory())
            {
                ArrayList<String> fileList = new ArrayList<String>();
                fileDirMap.put(dir.getName(),fileList);
                File[] files = dir.listFiles();
                if(files == null){//判断权限
                    continue;
                }
                for (File _file : files) {
                    if (_file.isFile() && _file.getName().endsWith(FILE_TYPE)) {
                        String _name = _file.getName();
                        String filePath = _file.getAbsolutePath();//获取文件路径
                        String fileName = _file.getName().substring(0, _name.length() - 4);//获取文件名
                        Log.d(TAG, "fileName:" + fileName);
                        Log.d(TAG, "filePath:" + filePath);
                        try {
                            fileList.add(fileName);
                            fileMap.put(fileName,filePath);
                        } catch (Exception e) {

                        }
                    } else if (_file.isDirectory()) {//查询子目录
                        Log.w(TAG, _file + "is dir, expect file");
                    } else {
                        Log.w(TAG, _file + "is not txt file");
                    }
                }
            }
        }
    }
}
