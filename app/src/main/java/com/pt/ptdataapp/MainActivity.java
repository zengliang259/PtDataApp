package com.pt.ptdataapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresPermission;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.pt.ptdataapp.Frame.FileExplorerView;
import com.pt.ptdataapp.Frame.MainPage;
import com.pt.ptdataapp.Frame.PrintPage;
import com.pt.ptdataapp.Model.DataManager;
import com.pt.ptdataapp.adapter.USBDeviceRecAdapter;
import com.pt.ptdataapp.fileUtil.FileUtil;
import com.pt.ptdataapp.utils.usb.USBUtil;

import java.io.UnsupportedEncodingException;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private FragmentTransaction mTransaction;
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


    private void InitUSB()
    {
        // 查找设备
        List<UsbDevice> deviceList = USBUtil.getInstance().getDeviceList();
        // 连接设备

        // 数据读取并存储本地
    }
    private void InitUI()
    {
        initView();
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setSelectedItemId(navigation.getMenu().getItem(1).getItemId());
    }

    @Override
    protected void onResume() {
        super.onResume();
        USBUtil.getInstance().registerReceiver(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        USBUtil.getInstance().unRegisterReceiver(this);
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
}
