package com.pt.ptdataapp.Model;

import com.pt.ptdataapp.fileUtil.FileDataReader;
import com.pt.ptdataapp.fileUtil.FileUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class DataManager {

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
        LocalFileModel.getInstance().ReadLocalFiles(LocalFileModel.DATA_PATH);
        Map<String, String> fileMap = LocalFileModel.getInstance().getFileMap();
        Collection<String> filePaths = fileMap.values();
        for (String filePath : filePaths)
        {
            String content = FileUtil.getFile(filePath);
            if (content != null)
            {
                patients.add(FileDataReader.Read(content));
            }

        }
    }

    public int GetPatientListCount()
    {
        return patients.size();
    }

    public PatientInfo GetPatientInfoByIndex(int index)
    {
        return patients.get(index);
    }
}
