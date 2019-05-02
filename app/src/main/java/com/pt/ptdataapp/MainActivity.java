package com.pt.ptdataapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.github.mjdev.libaums.UsbMassStorageDevice;
import com.github.mjdev.libaums.fs.UsbFile;
import com.pt.ptdataapp.Frame.FileExplorerView;
import com.pt.ptdataapp.Frame.MainPage;
import com.pt.ptdataapp.Frame.PrintPage;
import com.pt.ptdataapp.Model.DataManager;
import com.pt.ptdataapp.Model.LocalFileModel;
import com.pt.ptdataapp.fileUtil.FileUtil;
import com.pt.ptdataapp.fileUtil.SDCardUtil;
import com.pt.ptdataapp.uiUtils.LoadingDialog;
import com.pt.ptdataapp.utils.Utils;
import com.pt.ptdataapp.utils.usbHelper.USBBroadCastReceiver;
import com.pt.ptdataapp.utils.usbHelper.UsbHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;


public class MainActivity extends AppCompatActivity implements USBBroadCastReceiver.UsbListener{
    private static final String TAG = "MainActivity";
    private FragmentTransaction mTransaction;
    private LoadingDialog loadingDialog;

    private int usbProductID = 22304;
    // pt设备数据根目录
    private String PtDataFilePathPrefix = "REC";
    /**
     * 3个Fragments
     */
    MainPage mainPageFragemnt;
    PrintPage printPageFragment;
    FileExplorerView fileExploreFragment;
    public static final int VIEW_MAIN_PAGE_INDEX = 0;
    public static final int VIEW_PRINT_PAGE_INDEX = 1;
    public static final int VIEW_FILE_EXPLORE_INDEX = 2;
    private int temp_position_index = -1;
    private UsbHelper usbHelper;
    private ArrayList<File> rootMountFileList;


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    if (temp_position_index != VIEW_MAIN_PAGE_INDEX) {
                        mTransaction = getSupportFragmentManager().beginTransaction();
                        mTransaction.replace(R.id.id_fragment_content, mainPageFragemnt);
                        mTransaction.commit();
                        temp_position_index = VIEW_MAIN_PAGE_INDEX;
                        mainPageFragemnt.ScrollToTop();
                    }
                    return true;
                case R.id.navigation_print:
                    if (temp_position_index != VIEW_PRINT_PAGE_INDEX) {
                        if (temp_position_index == VIEW_MAIN_PAGE_INDEX)
                        {
                            mainPageFragemnt.SaveEditData();
                        }
                        mTransaction = getSupportFragmentManager().beginTransaction();
                        mTransaction.replace(R.id.id_fragment_content, printPageFragment);
                        mTransaction.commit();
                        temp_position_index = VIEW_PRINT_PAGE_INDEX;
                    }
                    return true;
                case R.id.navigation_file_explorer:
                    if (temp_position_index != VIEW_FILE_EXPLORE_INDEX) {
                        if (temp_position_index == VIEW_MAIN_PAGE_INDEX)
                        {
                            mainPageFragemnt.SaveEditData();
                        }
                        mTransaction = getSupportFragmentManager().beginTransaction();
                        mTransaction.replace(R.id.id_fragment_content, fileExploreFragment);
                        mTransaction.commit();
                        temp_position_index = VIEW_FILE_EXPLORE_INDEX;
                    }
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loadingDialog = new LoadingDialog(this);
        // 1 读取本地目录数据,并初始化DataManager
        getPermission();
        // 2 USB读取数据
        InitUSB();
        // 3 UI初始化
        InitUI();
    }

    @Override
    public void onBackPressed() {
        System.out.println("onBackPressed...");
        if (fileExploreFragment != null)
        {
            fileExploreFragment.onBackPressed();
        }
    }



    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void InitUI()
    {
        initView();
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setSelectedItemId(navigation.getMenu().getItem(1).getItemId());
    }

    private void initView() {
        mainPageFragemnt = new MainPage();
        mainPageFragemnt.SetContext(this);

        printPageFragment = new PrintPage();
        printPageFragment.SetContext(this);

        fileExploreFragment = new FileExplorerView();
        fileExploreFragment.SetContext(this);
    }


    // --------------------权限-------------
    void getPermission()
    {
        int permissionCheck1 = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
        int permissionCheck2 = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck1 != PackageManager.PERMISSION_GRANTED || permissionCheck2 != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                    124);
        }
        else
        {
            InitFromLocalFile();
        }
    }

    private void InitFromLocalFile()
    {
        loadingDialog.showDialog("读取本地数据...");
        new Thread(){
            @Override
            public void run() {
                super.run();
                DataManager.getInstance().InitFromLocalFile();
                loadingDialog.closeDialog();
            }
        }.start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 124) {
            if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED))
            {
                Log.d(TAG,"获取到权限了！");
                InitFromLocalFile();
            } else { Log.d(TAG,"搞不定啊！");
            }
        }
    }

    private void InitUSB()
    {
        initUsbFile();
        // 数据读取并存储本地
        if (rootMountFileList.size() > 0)
        {
            AsyncCopyMountFile();
        }
    }
    /**
     * 初始化 USB文件列表
     */
    private void initUsbFile() {
        usbHelper = new UsbHelper(this, this);
        rootMountFileList = new ArrayList<>();
        updateUsbFile();
    }

    private void TestInsertUSB()
    {
        if (rootMountFileList.size() == 0) {
            File testFile = new File(Environment.getExternalStorageDirectory(), "REC");
            rootMountFileList.add(testFile);
            AsyncCopyMountFile();
        }
    }

    /**
     * 更新 USB 文件列表
     */
    private void updateUsbFile() {
        UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        UsbMassStorageDevice[] usbMassStorageDevices = usbHelper.getDeviceList();
        UsbMassStorageDevice targetDevice = null;
        for(UsbMassStorageDevice device : usbMassStorageDevices)
        {
            if (device.getUsbDevice().getProductId() == usbProductID)
            {
                targetDevice = device;
            }
        }
        if (targetDevice != null) {
            //存在USB
            rootMountFileList.clear();
            if (usbManager.hasPermission(targetDevice.getUsbDevice()))
            {
                // 列举Storage下面的文件夹
                File storage = new File("/storage");
                File[] files = storage.listFiles();
                File usbRootFile = null;
                for (final File file : files) {
                    if (file.canRead()) {
                        if (file.getName().contains("usb")) {
                            //满足该条件的文件夹就是u盘在手机上的目录,有且只能插入一个
                            Log.d(TAG, "Usb File dir " + file.getName());
                            usbRootFile = file;
                            break;
                        }
                    }
                }
                if (usbRootFile != null)
                {
                    files = usbRootFile.listFiles();
                    for (File file : files)
                    {
                        if (file.getName().contains(PtDataFilePathPrefix))
                        {
                            rootMountFileList.add(file);
                            break;
                        }
                    }
                }
            }
        } else {
            Log.e(TAG, "No Usb Device");
            rootMountFileList.clear();
        }
    }

    private void OpenMountFile()
    {
        if (rootMountFileList.size() == 1)
        {
            File originalFile = rootMountFileList.get(0);
            // 首先读取该目录下的id.txt
            ArrayList<File> files = new ArrayList<>();
            Collections.addAll(files, originalFile.listFiles());
            String IDStr = "";
            for (File childFile : files)
            {
                if (childFile.getName() == "id.txt")
                {
                    IDStr = FileUtil.getFile(childFile.getAbsolutePath());
                    break;
                }
            }
            String destFilePath = LocalFileModel.getInstance().idFileMap.get(IDStr);
            if (destFilePath == null) // 不存在则新建，存在则直接覆盖
            {
                // 本地文件目录命名规则
                File localRootFile = new File(Environment.getExternalStorageDirectory(), LocalFileModel.DATA_PATH);
                int childFileNum = 0;
                if (!localRootFile.exists())
                {
                    FileUtil.createDir(localRootFile.getAbsolutePath());
                }
                else
                {
                    childFileNum = localRootFile.listFiles().length;
                }

                String newFileName = (childFileNum + 1) + "";
                destFilePath = Environment.getExternalStorageDirectory() + File.separator + LocalFileModel.DATA_PATH + File.separator + newFileName;
            }

            // 根目录一定是目录
            if (originalFile.isDirectory()) {
                try
                {
                    FileUtil.copyFolder(originalFile.getAbsolutePath(), destFilePath);
                    LocalFileModel.getInstance().AddLocalFiles(destFilePath);
                    if (temp_position_index == VIEW_MAIN_PAGE_INDEX)
                    {
                        mainPageFragemnt.NotifyListDataRefresh();
                        mainPageFragemnt.ScrollToTop();
                    }
                }
                catch (Exception e)
                {
                    Log.e(TAG, "CopyUsbDir error");
                }
            }
        }
    }

    public void AsyncCopyMountFile()
    {
        if (SDCardUtil.GetAvailablePercent() < 0.1)
        {
            Toast.makeText(Utils.getContext(), "内存空间不够，无法复制USB设备数据... ", Toast.LENGTH_SHORT).show();
            return;
        }
        loadingDialog.showDialog("复制USB数据中...");
        new Thread(){
            @Override
            public void run() {
                super.run();
                OpenMountFile();
                loadingDialog.closeDialog();
            }
        }.start();
    }

    @Override
    public void insertUsb(UsbDevice device_add) {
        if (device_add.getProductId() == usbProductID)
        {
            Toast.makeText(Utils.getContext(), "检测到USB设备插入... ", Toast.LENGTH_SHORT).show();
            if (rootMountFileList.size() == 0) {
                updateUsbFile();
                AsyncCopyMountFile();
            }
        }

    }

    @Override
    public void removeUsb(UsbDevice device_remove) {
        if (device_remove.getProductId() == usbProductID) {
            Toast.makeText(Utils.getContext(), "检测到USB设备拔出... ", Toast.LENGTH_SHORT).show();
            Log.d(TAG, device_remove.getDeviceName() + " remove");
        }
    }

    @Override
    public void getReadUsbPermission(UsbDevice usbDevice) {
        if (usbDevice.getProductId() == usbProductID)
        {
            Toast.makeText(Utils.getContext(), "获取USB设备权限成功 ", Toast.LENGTH_SHORT).show();
            if (rootMountFileList.size() == 0) {
                updateUsbFile();
                AsyncCopyMountFile();
            }
        }
    }

    @Override
    public void failedReadUsb(UsbDevice usbDevice) {

    }
}
