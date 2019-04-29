package com.pt.ptdataapp.Model;

import android.os.Environment;
import android.util.Log;

import com.pt.ptdataapp.fileUtil.FileUtil;

import java.io.File;
import java.util.ArrayList;
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
    private Map<String, List<String>> fileDirMap = new HashMap<String, List<String>>();

    public Map<String, String> getFileMap()
    {
        return fileMap;
    }

    public Map<String, List<String>> getDirMap()
    {
        return fileDirMap;
    }

    private List<String> idFilePaths = new ArrayList<>();
    public Map<String, String> idFileMap = new HashMap<String, String>();
    public void ReadLocalFiles(String dirPath) {
        File rootDir = new File(Environment.getExternalStorageDirectory(), dirPath);
        if (!rootDir.exists()) {//判断路径是否存在
            return ;
        }
        idFilePaths.clear();
        idFileMap.clear();
        ReadFilesLoop(rootDir);
        for (String idfilePath: idFilePaths)
        {
            String content = FileUtil.getFile(idfilePath);
            if (content != null)
            {
                File file = new File(idfilePath);
                idFileMap.put(content, file.getParent());
            }
        }
    }

    private ArrayList<String> ReadFilesLoop(File rootDir)
    {
        File[] dirs = rootDir.listFiles();
        if(dirs == null){//判断权限
            return null;
        }
        Log.d(TAG,"len =" + dirs.length);
        ArrayList<String> fileList = new ArrayList<String>();
        for (File childFile : dirs) {//遍历一级目录
            if(childFile.isDirectory())
            {
                fileDirMap.put(childFile.getName(),ReadFilesLoop(childFile));
            }
            else
            {
                if (childFile.isFile() && childFile.getName().endsWith(FILE_TYPE)) {
                    String _name = childFile.getName();
                    String filePath = childFile.getAbsolutePath();//获取文件路径
                    String fileName = childFile.getName().substring(0, _name.length() - 4);//获取文件名
                    Log.d(TAG, "fileName:" + fileName);
                    Log.d(TAG, "filePath:" + filePath);
                    try {
                        fileList.add(fileName);
                        fileMap.put(fileName,filePath);
                        if (fileName.equals( "id"))
                        {
                            idFilePaths.add(filePath);
                        }
                    } catch (Exception e) {

                    }
                }
            }
        }
        return fileList;
    }
}
