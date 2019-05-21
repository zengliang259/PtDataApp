package com.pt.ptdataapp.Model;

import android.os.Environment;
import android.util.Log;

import com.pt.ptdataapp.fileUtil.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocalFileModel {
    private static final String TAG = "LocalFileModel";
    public static final String DATA_PATH = "PtData";
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
    private Map<String,Long> fileMap = new HashMap<String,Long>();
    public Map<String, Long> getFileMap() { return fileMap;}
    // 目录下的设备根文件列表
    private Map<String, Long> fileDirMap = new HashMap<>();
    public Map<String, Long> getFileDirMap() { return fileDirMap; }

    private List<Map.Entry<String, Long>> sortedFileList = new ArrayList<>();
    public List<Map.Entry<String, Long>> getSortedFileList(){return sortedFileList;}

    private File curWorkFolder;

    private List<String> idFilePaths = new ArrayList<>();
    public Map<String, String> idFileMap = new HashMap<String, String>();
    public void InitLocalFiles(String dirPath) {
        File rootDir = new File(Environment.getExternalStorageDirectory(), dirPath);
        if (!rootDir.exists()) {//判断路径是否存在
            return ;
        }
        Log.d(TAG,"InitLocalFiles " + rootDir.getAbsolutePath());
        fileDirMap.clear();
        File[] dirs = rootDir.listFiles();

        long newModifyTime = 0;
        File lastModifyFolder = null;
        if(dirs != null)
        {
            for(File item : dirs)
            {
                long modifyTime = item.lastModified();
                fileDirMap.put(item.getName(), modifyTime);
                if (newModifyTime < modifyTime)
                {
                    newModifyTime = modifyTime;
                    lastModifyFolder = item;
                }
            }
        }
        if (lastModifyFolder != null)
        {
            // 文件id索引
            idFilePaths.clear();
            idFileMap.clear();
            fileMap.clear();
            curWorkFolder = lastModifyFolder;
            ReadFilesLoop(lastModifyFolder);
            SortFileMap();
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

    }

    public void AddLocalFiles(String dirPath)
    {
        File rootDir = new File(dirPath);
        if (!rootDir.exists()) {//判断路径是否存在
            return ;
        }
        Log.d(TAG,"AddLocalFiles " + rootDir.getAbsolutePath());
        if (curWorkFolder == null
                || !curWorkFolder.getAbsolutePath().equals(rootDir.getAbsolutePath()))
        {
            // 先删除改目录之前的文件索引
            fileDirMap.put(rootDir.getName(), rootDir.lastModified());
            fileMap.clear();
            ReadFilesLoop(rootDir);
            SortFileMap();
            for (String idfilePath : idFilePaths) {
                String content = FileUtil.getFile(idfilePath);
                if (content != null) {
                    File file = new File(idfilePath);
                    idFileMap.put(content, file.getParent());
                }
            }
            DataManager.getInstance().UpdatePatientList();
        }
    }

    private void SortFileMap()
    {
        // 通过ArrayList构造函数把map.entrySet()转换成list
        sortedFileList = new ArrayList<Map.Entry<String, Long>>(fileMap.entrySet());
        // 通过比较器实现比较排序
        Collections.sort(sortedFileList, new Comparator<Map.Entry<String, Long>>() {
            public int compare(Map.Entry<String, Long> mapping1, Map.Entry<String, Long> mapping2) {
                long coef = -mapping1.getValue() + mapping2.getValue();
                if (coef > 0)
                {
                    return 1;
                }
                else if (coef < 0)
                {
                    return -1;
                }
                else
                {
                    return 0;
                }
            }
        });
    }

    private ArrayList<String> ReadFilesLoop(File rootDir)
    {
        File[] dirs = rootDir.listFiles();
        if(dirs == null){//判断权限
            return null;
        }
//        Log.d(TAG,"len =" + dirs.length);
        ArrayList<String> fileList = new ArrayList<String>();
        for (File childFile : dirs) {//遍历一级目录
            if (childFile.isDirectory())
            {
                ReadFilesLoop(childFile);
            }
            else
            {
                if (childFile.isFile() && childFile.getName().endsWith(FILE_TYPE)) {
                    String _name = childFile.getName();
                    String filePath = childFile.getAbsolutePath();//获取文件路径
                    String fileName = childFile.getName().substring(0, _name.length() - 4);//获取文件名
                    long lastModifyTime = childFile.lastModified();
//                    Log.d(TAG, "fileName:" + fileName);
//                    Log.d(TAG, "filePath:" + filePath);
//                    Log.d(TAG, "last modify time:" + lastModifyTime);
                    try {
                        fileList.add(fileName);
                        if (fileName.equals( "id")) // id文件不是数据文件
                        {
                            idFilePaths.add(filePath);
                        }
                        else
                        {
                            fileMap.put(filePath,lastModifyTime);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "读取" + fileName +"文件失败");
                    }
                }
            }


        }
        return fileList;
    }
}
