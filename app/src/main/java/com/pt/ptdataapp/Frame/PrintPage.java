package com.pt.ptdataapp.Frame;


import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.pt.ptdataapp.Model.DataManager;
import com.pt.ptdataapp.R;
import com.pt.ptdataapp.utils.TSCUtils;
import com.pt.ptdataapp.utils.Utils;
import com.pt.ptdataapp.utils.usbHelper.USBBroadCastReceiver;
import com.pt.ptdataapp.utils.usbHelper.UsbConnectionUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class PrintPage extends Fragment implements USBBroadCastReceiver.UsbListener{
    private String TAG = "PrintPage";

    private Context activityContext;
    private View rootView;
    TextView IDLabel;
    TextView titleLabel;
    TextView patientNameLabel;
    TextView resultLabel;
    TextView doctorNameLabel;
    TextView checkDateLabel;
    TextView reportDateLabel;
    int UsbProductID = 13624;
    int UsbVendorID = 19267;
    public PrintPage() {
        // Required empty public constructor
    }
    public void SetContext(Context context)
    {
        activityContext = context;
        UsbConnectionUtil.getInstance().init(context, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        rootView = inflater.inflate(R.layout.fragment_print_page, container, false);
        IDLabel = rootView.findViewById(R.id.printIDLabel);
        titleLabel = rootView.findViewById(R.id.printTitleLabel);
        patientNameLabel = rootView.findViewById(R.id.printPatientNameLabel);
        resultLabel = rootView.findViewById(R.id.printResultLabel);
        doctorNameLabel = rootView.findViewById(R.id.printDoctorNameLabel);
        checkDateLabel = rootView.findViewById(R.id.printCheckDateLabel);
        reportDateLabel = rootView.findViewById(R.id.printReportDateLabel);
        rootView.findViewById(R.id.testPrintBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDevice != null)
                {
                    if (usbUtil.hasPermission(mDevice))
                    {
                        if (usbUtil.openPort(mDevice)) {
                            TestPrint();
                        }
                    }
                    else
                    {
                        Toast.makeText(Utils.getContext(), "正在获取Usb设备 " + mDevice.getDeviceName() + " 权限中，请稍候再试...", Toast.LENGTH_SHORT).show();
                        usbUtil.requestPermission(mDevice);
                    }
                }
                else
                {
                    Toast.makeText(Utils.getContext(), "未找到USB打印设备 ", Toast.LENGTH_SHORT).show();
                }
            }
        });
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (usbUtil != null)
        {
            usbUtil.closeport(1000);
            usbUtil = null;
        }
    }
    @Override
    public void onResume()
    {
        super.onResume();
        InitView();
    }

    private UsbDevice mDevice;
    private UsbConnectionUtil usbUtil;

    private void InitView()
    {
        usbUtil = UsbConnectionUtil.getInstance();
        if (mDevice == null)
        {
            mDevice = UsbConnectionUtil.getInstance().getUsbDevice(UsbVendorID, UsbProductID);
            // 获取权限
            if (mDevice != null)
            {
                if (!usbUtil.hasPermission(mDevice))
                {
                    usbUtil.requestPermission(mDevice);
                }
            }
        }

        List<String> printList = DataManager.getInstance().getPrintContentListCache();
        if (printList.size() >= 7)
        {
            titleLabel.setText(printList.get(0));
            IDLabel.setText(printList.get(1));
            patientNameLabel.setText(printList.get(2));
            resultLabel.setText(printList.get(3));
            doctorNameLabel.setText(printList.get(4));
            checkDateLabel.setText(printList.get(5));
            reportDateLabel.setText(printList.get(6));
        }
        else
        {
            titleLabel.setText("岭南医院INR检测报告单");
            IDLabel.setText("");
            patientNameLabel.setText("");
            resultLabel.setText("");
            doctorNameLabel.setText("");
            checkDateLabel.setText("");
            reportDateLabel.setText("");
        }

    }

    private boolean IsTargetPrintUSB(UsbDevice device)
    {
        if (device != null && device.getProductId() == UsbProductID && device.getVendorId() == UsbVendorID)
        {
            return true;
        }
        return false;
    }

    private void TestPrint()
    {
        Toast.makeText(Utils.getContext(), "开始打印...", Toast.LENGTH_SHORT).show();
        List<String> printList = new ArrayList<>();
        printList.add(titleLabel.getText().toString());
        printList.add("ID:  " + IDLabel.getText().toString());
        printList.add("姓名:   " + patientNameLabel.getText().toString());
        printList.add("INR:   " + resultLabel.getText().toString());
        printList.add("报告医生:   " + doctorNameLabel.getText().toString());
        printList.add("检测日期:   " + checkDateLabel.getText().toString());
        printList.add("报告日期:   " + reportDateLabel.getText().toString());
        byte[] bytes = TSCUtils.StartPrint(printList);
        usbUtil.sendMessage(bytes);
    }

    public void EnterPaperPrint()
    {
        Toast.makeText(Utils.getContext(), "开始打印...", Toast.LENGTH_SHORT).show();
        byte[] bytes = TSCUtils.EnterPaper(10);
        usbUtil.sendMessage(bytes);
    }

    @Override
    public void insertUsb(UsbDevice device_add)
    {
        if (IsTargetPrintUSB(device_add))
        {
            mDevice = device_add;
            Toast.makeText(Utils.getContext(), "Usb设备 " + device_add.getDeviceName() + " 插入", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void removeUsb(UsbDevice device_remove) {
        if (IsTargetPrintUSB(device_remove))
        {
            Log.d(TAG, device_remove.getDeviceName()+" remove");
            if (usbUtil != null)
            {
                usbUtil.closeport(1000);
            }
            mDevice = null;
            Toast.makeText(Utils.getContext(), "Usb设备 " + device_remove.getDeviceName() + " 拔出", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void getReadUsbPermission(UsbDevice usbDevice) {
        if (IsTargetPrintUSB(usbDevice)) {
            Toast.makeText(Utils.getContext(), "成功获取Usb设备 " + usbDevice.getDeviceName() + " 权限", Toast.LENGTH_SHORT).show();
            mDevice = usbDevice;
        }
    }

    @Override
    public void failedReadUsb(UsbDevice usbDevice) {
        if (IsTargetPrintUSB(usbDevice)) {
            mDevice = null;
            Toast.makeText(Utils.getContext(), "读取Usb设备 " + usbDevice.getDeviceName() + " 失败", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void mountUsbFolder(String mountPath) {

    }

    @Override
    public void unMountUsbFolder(String unMountPath) {

    }
}
