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

import com.github.mjdev.libaums.UsbMassStorageDevice;
import com.github.mjdev.libaums.fs.UsbFile;
import com.pt.ptdataapp.Frame.FileExplorerView;
import com.pt.ptdataapp.Frame.MainPage;
import com.pt.ptdataapp.Frame.PrintPage;
import com.pt.ptdataapp.Model.DataManager;
import com.pt.ptdataapp.Model.LocalFileModel;
import com.pt.ptdataapp.fileUtil.FileUtil;
import com.pt.ptdataapp.utils.usbHelper.USBBroadCastReceiver;
import com.pt.ptdataapp.utils.usbHelper.UsbHelper;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class MainActivity extends AppCompatActivity implements USBBroadCastReceiver.UsbListener{
    private static final String TAG = "MainActivity";
    private FragmentTransaction mTransaction;

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
    private ArrayList<UsbFile> rootUsbFileList;


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
                    }
                    return true;
                case R.id.navigation_print:
                    if (temp_position_index != VIEW_PRINT_PAGE_INDEX) {
                        mTransaction = getSupportFragmentManager().beginTransaction();
                        mTransaction.replace(R.id.id_fragment_content, printPageFragment);
                        mTransaction.commit();
                        temp_position_index = VIEW_PRINT_PAGE_INDEX;
                    }
                    return true;
                case R.id.navigation_file_explorer:
                    if (temp_position_index != VIEW_FILE_EXPLORE_INDEX) {
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
            DataManager.getInstance().InitFromLocalFile();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 124) {
            if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED))
            {
                Log.d(TAG,"获取到权限了！");
                DataManager.getInstance().InitFromLocalFile();
            } else { Log.d(TAG,"搞不定啊！");
            }
        }
    }

    private void InitUSB()
    {
        initUsbFile();
        // 数据读取并存储本地
        if (rootUsbFileList.size() > 0)
        {
            this.openUsbFile();
        }
    }
    /**
     * 初始化 USB文件列表
     */
    private void initUsbFile() {
        usbHelper = new UsbHelper(this, this);
        rootUsbFileList = new ArrayList<>();
        updateUsbFile(0);
    }

    /**
     * 更新 USB 文件列表
     */
    private void updateUsbFile(int position) {
        UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        UsbMassStorageDevice[] usbMassStorageDevices = usbHelper.getDeviceList();
        if (usbMassStorageDevices.length > 0) {
            //存在USB
            rootUsbFileList.clear();
            UsbMassStorageDevice device = usbMassStorageDevices[position];
            if (usbManager.hasPermission(device.getUsbDevice()))
            {
                List<UsbFile> usbFiles = usbHelper.readDevice(device);
                for (UsbFile file : usbFiles)
                {
                    if (file.getName().contains(PtDataFilePathPrefix))
                    {
                        rootUsbFileList.add(file);
                    }
                }
            }
        } else {
            Log.e(TAG, "No Usb Device");
            rootUsbFileList.clear();
        }
    }

    /**
     * 打开 USB File
     *
     */
    private void openUsbFile() {
        // 只有一个根目录
        if (rootUsbFileList.size() == 1)
        {
            UsbFile file = rootUsbFileList.get(0);
            // 首先读取该目录下的id.txt
            ArrayList<UsbFile> usbFiles = new ArrayList<>();
            try {
                Collections.addAll(usbFiles, file.listFiles());
            } catch (IOException e) {
                e.printStackTrace();
            }
            String IDStr = "";
            for (UsbFile childFile : usbFiles)
            {
                if (childFile.getName() == "id.txt")
                {
                    ByteBuffer byteBuffer = ByteBuffer.allocate(512);
                    try {
                        childFile.read(0,byteBuffer);
                        IDStr = new String(byteBuffer.array());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
            String filePath = LocalFileModel.getInstance().idFileMap.get(IDStr);
            if (filePath == null) // 不存在则新建，存在则直接覆盖
            {
                // 本地文件目录命名规则
                File localRootFile = new File(Environment.getExternalStorageDirectory(), LocalFileModel.DATA_PATH);
                int childFileNum = localRootFile.listFiles().length;
                String newFileName = (childFileNum + 1) + "";
                filePath = LocalFileModel.DATA_PATH + File.separator + newFileName;
            }

            if (file.isDirectory()) {
                FileUtil.createDir(filePath);
                try
                {
                    CopyUsbDir(file, filePath);
                }
                catch (Exception e)
                {
                    Log.e(TAG, "CopyUsbDir error");
                }
            } else {
                //开启线程，将文件复制到本地
                copyUSbFile(file, filePath);
            }
        }

    }

    private void CopyUsbDir(final UsbFile rootFile, String parentPath) throws IOException {
        ArrayList<UsbFile> usbFiles = new ArrayList<>();
        Collections.addAll(usbFiles, rootFile.listFiles());
        if (usbFiles != null)
        {
            for (UsbFile file : usbFiles)
            {
                final String filePath = parentPath + File.separator + file.getName();
                if (file.isDirectory()) {
                    FileUtil.createDir(filePath);
                    CopyUsbDir(file,filePath);
                } else {
                    //开启线程，将文件复制到本地
                    copyUSbFile(file, filePath);
                }
            }
        }
    }

    /**
     * 复制 USB 文件到本地
     *
     * @param file USB文件
     */
    private void copyUSbFile(final UsbFile file, String targetPath) {
        //复制结果
        final boolean result = usbHelper.saveUSbFileToLocal(file, targetPath, new UsbHelper.DownloadProgressListener() {
            @Override
            public void downloadProgress(final int progress) {
                String text = "From Usb " + usbHelper.getCurrentFolder().getName()
                        + "\nTo Local " + LocalFileModel.DATA_PATH
                        + "\n Progress : " + progress;
            }
        });
    }



    @Override
    public void insertUsb(UsbDevice device_add) {
        if (rootUsbFileList.size() == 0) {
            updateUsbFile(0);
            openUsbFile();
        }
    }

    @Override
    public void removeUsb(UsbDevice device_remove) {
        Log.d(TAG, device_remove.getDeviceName()+" remove");
    }

    @Override
    public void getReadUsbPermission(UsbDevice usbDevice) {
        if (rootUsbFileList.size() == 0) {
            updateUsbFile(0);
            openUsbFile();
        }
    }

    @Override
    public void failedReadUsb(UsbDevice usbDevice) {

    }
}
