package com.pt.ptdataapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.github.mjdev.libaums.UsbMassStorageDevice;
import com.pt.ptdataapp.Frame.FileExplorerView;
import com.pt.ptdataapp.Frame.MainPage;
import com.pt.ptdataapp.Model.DataManager;
import com.pt.ptdataapp.Model.FileEntity;
import com.pt.ptdataapp.Model.LocalFileModel;
import com.pt.ptdataapp.fileUtil.FileUtil;
import com.pt.ptdataapp.fileUtil.SDCardUtil;
import com.pt.ptdataapp.uiUtils.LoadingDialog;
import com.pt.ptdataapp.utils.TSCUtils;
import com.pt.ptdataapp.utils.Utils;
import com.pt.ptdataapp.utils.usbHelper.USBBroadCastReceiver;
import com.pt.ptdataapp.utils.usbHelper.UsbConnectionUtil;
import com.pt.ptdataapp.utils.usbHelper.UsbHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements USBBroadCastReceiver.UsbListener{
    private static final String TAG = "MainActivity";
    private FragmentTransaction mTransaction;
    private LoadingDialog loadingDialog;
    // Pt设备厂商和产品ID
    private int ptUsbProductID = 22304;
    int ptUsbVendorID = 1155;
    // 打印机设备厂商和产品ID
    int printerUsbProductID = 13624;
    int printerUsbVendorID = 19267;
    // pt设备数据根目录
    private String PtDataFilePathPrefix = "REC";
    /**
     * 3个Fragments
     */
    MainPage mainPageFragment;
    FileExplorerView fileExploreFragment;
    private View currentButton;
    ImageButton filePageBtn;
    ImageButton homePageBtn;
    ImageButton printPageBtn;
    public static final int VIEW_MAIN_PAGE_INDEX = 0;
    public static final int VIEW_FILE_EXPLORE_INDEX = 1;
    private int temp_position_index = -1;
    private UsbHelper usbHelper;
    private ArrayList<File> rootMountFileList;

    private UsbDevice m_printUsbDevice;

    private int showDialogCount = 0;
    private Handler mHandler;

    public boolean IsPtMounted = false;
    public String PtMountPath = "";

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Utils.statusBarHide(this);
        UsbConnectionUtil.getInstance().init(this, this);
        loadingDialog = new LoadingDialog(this);
        InitHandler();
        // 1 读取本地目录数据,并初始化DataManager
        getPermission();
        // 2 USB读取数据
        InitUSB();
        // 3 UI初始化
        InitUI();
    }

    private void InitHandler()
    {
        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 1:
                        homePageBtn.performClick();
                        break;
                    default:
                        break;
                }
            }
        };
    }

    @Override
    public void onBackPressed() {
        System.out.println("onBackPressed...");
        if (temp_position_index == VIEW_FILE_EXPLORE_INDEX)
        {
            if (fileExploreFragment != null)
            {
                fileExploreFragment.onBackPressed();
            }
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (UsbConnectionUtil.getInstance() != null)
        {
            UsbConnectionUtil.getInstance().closeport(1000);
        }
    }
    private void InitUI()
    {
        initView();
        initBtns();
    }

    private void initView() {
        mainPageFragment = new MainPage();
        mainPageFragment.SetContext(this);

        fileExploreFragment = new FileExplorerView();
        fileExploreFragment.SetContext(this);
        fileExploreFragment.setOnFileClick(new FileExplorerView.OnFileClick() {
            //3、实现接口对象的方法，
            @Override
            public void onClick(File clickFile) {
                if (temp_position_index != VIEW_MAIN_PAGE_INDEX) {
                    mTransaction = getSupportFragmentManager().beginTransaction();
                    mTransaction.replace(R.id.id_fragment_content, mainPageFragment);
                    mTransaction.commit();
                    temp_position_index = VIEW_MAIN_PAGE_INDEX;
                    File parentFile = clickFile.getParentFile();
                    if (parentFile != null && parentFile.getParentFile() != null)
                    {
                        LocalFileModel.getInstance().AddLocalFiles(parentFile.getParent());
                        List<Map.Entry<String, Long>> sortedFileList = LocalFileModel.getInstance().getSortedFileList();
                        int findIndex = 0;
                        for (int i = 0,len = sortedFileList.size(); i < len ; i ++)
                        {
                            if (sortedFileList.get(i).getKey().equals(clickFile.getAbsolutePath()))
                            {
                                findIndex = i;
                                break;
                            }
                        }
                        mainPageFragment.SafeScrollToIndex(findIndex);
                    }

                }
                setButton(homePageBtn);
            }
        });
    }

    private void initBtns() {
        filePageBtn = (ImageButton) findViewById(R.id.file_page_btn);
        homePageBtn = (ImageButton) findViewById(R.id.home_page_btn);
        printPageBtn = (ImageButton) findViewById(R.id.print_btn);

        filePageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (temp_position_index != VIEW_FILE_EXPLORE_INDEX) {
                    mTransaction = getSupportFragmentManager().beginTransaction();
                    mTransaction.replace(R.id.id_fragment_content, fileExploreFragment);
                    mTransaction.commit();
                    temp_position_index = VIEW_FILE_EXPLORE_INDEX;
                }
                setButton(v);
            }
        });

        homePageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (temp_position_index != VIEW_MAIN_PAGE_INDEX) {
                    mTransaction = getSupportFragmentManager().beginTransaction();
                    mTransaction.replace(R.id.id_fragment_content, mainPageFragment);
                    mTransaction.commit();
                    temp_position_index = VIEW_MAIN_PAGE_INDEX;
                    mainPageFragment.SafeScrollToIndex(0);
                }
                setButton(v);
            }
        });


        printPageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OnPrintClick();
            }
        });

        filePageBtn.performClick();

    }

    private void OnPrintClick()
    {
        if (temp_position_index == VIEW_MAIN_PAGE_INDEX)
        {
            mainPageFragment.SaveEditData();
        }
       else if(temp_position_index == VIEW_FILE_EXPLORE_INDEX)
        {
            fileExploreFragment.SaveEditData();
            // Toast.makeText(Utils.getContext(), "主页才可以打印数据", Toast.LENGTH_SHORT).show();
        }

        List<String> printList = DataManager.getInstance().getPrintContentListCache();
        if(printList.size() > 0)
        {
            if (m_printUsbDevice != null)
            {
                if (UsbConnectionUtil.getInstance().hasPermission(m_printUsbDevice))
                {
                    if (UsbConnectionUtil.getInstance().openPort(m_printUsbDevice)) {
                        Toast.makeText(Utils.getContext(), "开始打印...", Toast.LENGTH_SHORT).show();
                        byte[] bytes = TSCUtils.StartPrint(printList);
                        UsbConnectionUtil.getInstance().sendMessage(bytes);
                    }
                }
                else
                {
                    Toast.makeText(Utils.getContext(), "正在获取Usb设备 " + m_printUsbDevice.getDeviceName() + " 权限中，请稍候再试...", Toast.LENGTH_SHORT).show();
                    UsbConnectionUtil.getInstance().requestPermission(m_printUsbDevice);
                }
            }
            else
            {
                Toast.makeText(Utils.getContext(), "未找到USB打印设备 ", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setButton(View v) {
        if (currentButton != null && currentButton.getId() != v.getId()) {
            currentButton.setEnabled(true);
        }
        v.setEnabled(false);
        currentButton = v;
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
        ShowDialog("读取本地数据...");
        new Thread(){
            @Override
            public void run() {
                super.run();
                DataManager.getInstance().InitFromLocalFile();
                if (temp_position_index == VIEW_MAIN_PAGE_INDEX)
                {
                    mainPageFragment.NotifyListDataRefresh();
                }
                HideDialog();
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
        initPrintUsb();
        // 数据读取并存储本地
        AsyncCopyMountFile();
    }
    /**
     * 初始化 USB文件列表
     */
    private void initUsbFile() {
        usbHelper = new UsbHelper(this, this);
        rootMountFileList = new ArrayList<>();
        updateUsbFile();
    }

    private void initPrintUsb()
    {
        if (m_printUsbDevice == null)
        {
            m_printUsbDevice = UsbConnectionUtil.getInstance().getUsbDevice(printerUsbVendorID, printerUsbProductID);
            // 获取权限
            if (m_printUsbDevice != null)
            {
                if (!UsbConnectionUtil.getInstance().hasPermission(m_printUsbDevice))
                {
                    UsbConnectionUtil.getInstance().requestPermission(m_printUsbDevice);
                }
            }
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
            if (IsTargetPtUSB(device.getUsbDevice()))
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
                    if (IsPtUsbFolder(file)) {
                        //满足该条件的文件夹就是u盘在手机上的目录,有且只能插入一个
                        Log.d(TAG, "Usb File dir " + file.getName());
                        File[] subFiles = file.listFiles();

                        usbRootFile = file;
                        break;
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

    /**
     * 新的usb设备挂载成功，进行处理
     * 有且只有一个usb设备挂载时进行处理
     * @param mountUsbFolder
     */
    private void MountUSBFolderProcess(String mountUsbFolder)
    {
        if (rootMountFileList.size() == 0)
        {
            File mountFile = new File(mountUsbFolder);
            if (IsPtUsbFolder(mountFile)) {
                Log.d(TAG, "Usb Mount File dir " + mountFile.getName());
                File[] files = mountFile.listFiles();
                for (File file : files)
                {
                    if (file.getName().contains(PtDataFilePathPrefix))
                    {
                        IsPtMounted = true;
                        PtMountPath = mountUsbFolder;
                        rootMountFileList.add(file);
                        break;
                    }
                }
            }
            AsyncCopyMountFile();
        }
    }

    private boolean IsPtUsbFolder(File usbFolder)
    {
        if (usbFolder.canRead()) {
            if (usbFolder.getName().contains("usb")) {
                File[] subFiles = usbFolder.listFiles();
                for (int i = 0,len = subFiles.length; i < len; i ++)
                {
                    if (subFiles[i].getName().contains(PtDataFilePathPrefix))
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    private boolean IsTargetPrintUSB(UsbDevice device)
    {
        if (device != null && device.getProductId() == printerUsbProductID && device.getVendorId() == printerUsbVendorID)
        {
            return true;
        }
        return false;
    }

    private boolean IsTargetPtUSB(UsbDevice device)
    {
        if (device != null && device.getProductId() == ptUsbProductID && device.getVendorId() == ptUsbVendorID)
        {
            return true;
        }
        return false;
    }

    private void OpenMountFile()
    {
        if (rootMountFileList.size() == 1)
        {
            File originalFile = rootMountFileList.get(0);
            Log.d(TAG,"准备复制" + originalFile.getAbsolutePath());
            // 首先读取该目录下的id.txt
            ArrayList<File> files = new ArrayList<>();
            File[] usbFiles = originalFile.listFiles();
            if (usbFiles == null)
            {
                Log.e(TAG, originalFile.getAbsolutePath() + " is not exist when copy usb mount file path");
                return;
            }
            Collections.addAll(files, usbFiles);
            String IDStr = "";
            for (File childFile : files)
            {
                if (childFile.getName().equals("id.txt"))
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
                if (!localRootFile.exists())
                {
                    FileUtil.createDir(localRootFile.getAbsolutePath());
                }

                String newFileName = IDStr;
                destFilePath = Environment.getExternalStorageDirectory() + File.separator + LocalFileModel.DATA_PATH + File.separator + newFileName;
            }

            // 根目录一定是目录
            if (originalFile.isDirectory()) {
                try
                {
                    Log.d(TAG,"开始复制" + originalFile.getAbsolutePath());
                    List<FileEntity> sourceList = FileUtil.FindAllFile(originalFile.getAbsolutePath(), true);
                   FileUtil.copyFolder(originalFile.getAbsolutePath(), destFilePath, this);
                    Log.d(TAG,"复制完成" + originalFile.getAbsolutePath());
                    List<FileEntity> dstList = FileUtil.FindAllFile(destFilePath, true);
                    if (sourceList.size() == dstList.size())
                    {
                        LocalFileModel.getInstance().AddLocalFiles(destFilePath);
                        if (temp_position_index == VIEW_MAIN_PAGE_INDEX)
                        {
                            mainPageFragment.NotifyListDataRefresh();
                            mainPageFragment.SafeScrollToIndex(0);
                        }
                        else
                        {
                            mHandler.sendEmptyMessage(1);
                        }
                    }
                    else
                    {
                        if (LocalFileModel.getInstance().idFileMap.get(IDStr) == null)
                        {
                            Log.d(TAG,"执行删除" + originalFile.getAbsolutePath());
                            FileUtil.deleteDirectory(destFilePath);
                        }
                    }
                }
                catch (Exception e)
                {
                    Log.e(TAG, "OpenMountFile CopyUsbDir error");
                }
            }
        }
    }

    public void AsyncCopyMountFile()
    {
        if (rootMountFileList.size() == 0)
        {
            return;
        }
        if (SDCardUtil.GetAvailablePercent() < 0.1)
        {
            Toast.makeText(Utils.getContext(), "内存空间不够，无法复制USB设备数据... ", Toast.LENGTH_SHORT).show();
            return;
        }
        ShowDialog("复制USB数据中，请勿拔出USB设备...");
        new Thread(){
            @Override
            public void run() {
                if(IsPtMounted)
                {
                    OpenMountFile();
                }
                HideDialog();
            }
        }.start();
    }

    private void ShowDialog(String text)
    {
        loadingDialog.showDialog(text);
        showDialogCount ++;
    }

    public void HideDialog()
    {
        showDialogCount -- ;
        if (showDialogCount <= 0)
        {
            showDialogCount = 0;
            loadingDialog.closeDialog();
        }

    }

    @Override
    public void insertUsb(UsbDevice device_add) {
        Toast.makeText(Utils.getContext(), "检测到USB设备插入... ", Toast.LENGTH_SHORT).show();
        if (IsTargetPrintUSB(device_add))
        {
            m_printUsbDevice = device_add;
        }
    }

    @Override
    public void removeUsb(UsbDevice device_remove) {
        if (IsTargetPrintUSB(device_remove)) {
            Toast.makeText(Utils.getContext(), "检测到USB设备拔出... ", Toast.LENGTH_SHORT).show();
            Log.d(TAG, device_remove.getDeviceName()+" remove");
            if (UsbConnectionUtil.getInstance() != null)
            {
                UsbConnectionUtil.getInstance().closeport(1000);
            }
            m_printUsbDevice = null;
        }
    }

    @Override
    public void getReadUsbPermission(UsbDevice usbDevice) {
        Toast.makeText(Utils.getContext(), "获取USB设备权限成功 ", Toast.LENGTH_SHORT).show();
        if (IsTargetPrintUSB(usbDevice))
        {
            m_printUsbDevice = usbDevice;
        }
        else if(IsTargetPtUSB(usbDevice))
        {
            rootMountFileList.clear();
            // 列举Storage下面的文件夹
            File storage = new File("/storage");
            File[] files = storage.listFiles();
            File usbRootFile = null;
            for (final File file : files) {
                if (IsPtUsbFolder(file)) {
                    //满足该条件的文件夹就是u盘在手机上的目录,有且只能插入一个
                    Log.d(TAG, "Usb File dir " + file.getName());
                    File[] subFiles = file.listFiles();

                    usbRootFile = file;
                    break;
                }
            }
            if (usbRootFile != null)
            {
                files = usbRootFile.listFiles();
                for (File file : files)
                {
                    if (file.getName().contains(PtDataFilePathPrefix))
                    {
                        IsPtMounted = true;
                        PtMountPath = usbRootFile.getAbsolutePath();
                        rootMountFileList.add(file);
                        break;
                    }
                }
            }
            AsyncCopyMountFile();
        }
    }

    @Override
    public void failedReadUsb(UsbDevice usbDevice) {

    }

    @Override
    public void mountUsbFolder(String mountPath) {
        Log.d(TAG, mountPath + " mount");
        if (rootMountFileList.size() == 0) {
            MountUSBFolderProcess(mountPath);
        }
        else
        {
            Toast.makeText(Utils.getContext(), "当前存在多个USB挂载设备，同时只能一个usb挂载设备 ", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void unMountUsbFolder(String unMountPath) {
        Log.d(TAG, unMountPath + " unMount");
        if (unMountPath == PtMountPath)
        {
            rootMountFileList.clear();
            IsPtMounted = false;
            HideDialog();
        }

    }
}
