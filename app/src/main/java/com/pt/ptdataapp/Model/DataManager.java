package com.pt.ptdataapp.Model;

import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.pt.ptdataapp.fileUtil.FileDataReader;
import com.pt.ptdataapp.fileUtil.FileUtil;
import com.pt.ptdataapp.utils.Utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class DataManager {

    private String TAG = "DataManager";
    private static DataManager _instance;
    public static DataManager getInstance() {
        if (_instance == null)
        {
            _instance = new DataManager();
        }
        return _instance;
    }
    private List<PatientInfo> patients= new ArrayList<PatientInfo>();
    public DataManager()
    {
        // test data
//        for (int i = 0; i < 5; i ++)
//        {
//            PatientInfo test1 = new PatientInfo();
//            test1.ID = i;
//            test1.checkResult = "INR:"+ i;
//            test1.patientName = "病人" + i;
//            test1.doctorName = "医生" + i;
//            test1.checkDate = "20190407";
//            test1.reportDate = "20190407";
//            patients.add(test1);
//        }

    }

    public void InitFromLocalFile()
    {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            LocalFileModel.getInstance().InitLocalFiles(LocalFileModel.DATA_PATH);
            UpdatePatientList();
        }
        else
        {
            Toast.makeText(Utils.getContext(), "请检查SDcard是否正确插入 ", Toast.LENGTH_SHORT).show();
        }
    }

    public void UpdatePatientList()
    {
        Log.d(TAG, "UpdatePatientList Start ...");
        patients.clear();
        List<Map.Entry<String, Long>> sortedFileList = LocalFileModel.getInstance().getSortedFileList();
        for (Map.Entry<String, Long> kv : sortedFileList)
        {
            String content = FileUtil.getEncryptFile(kv.getKey());
            Log.d(TAG,content);
            if (content != null)
            {
                patients.add(FileDataReader.Read(content));
            }
        }
        Log.d(TAG, "UpdatePatientList End");
    }

    public List<PatientInfo> GetPatientList()
    {
        return patients;
    }

    public PatientInfo GetPatientInfoByIndex(int index)
    {
        return patients.get(index);
    }


    private List<String> printContentListCache = new ArrayList<>();
    public void SavePrintContentList(List<String> contentList)
    {
        printContentListCache = contentList;
    }
    public List<String> getPrintContentListCache(){return printContentListCache;}

    private List<String> fileDetailContentListCache = new ArrayList<>();
    public void SaveFileDetailContentList(List<String> contentList)
    {
        fileDetailContentListCache = contentList;
    }
    public List<String> getFileDetailContentListCache(){return fileDetailContentListCache;}
}
