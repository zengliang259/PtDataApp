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
        if (rootView == null)
        {
            rootView = inflater.inflate(R.layout.fragment_print_page, container, false);
            IDLabel = rootView.findViewById(R.id.printIDLabel);
            titleLabel = rootView.findViewById(R.id.printTitleLabel);
            patientNameLabel = rootView.findViewById(R.id.printPatientNameLabel);
            resultLabel = rootView.findViewById(R.id.printResultLabel);
            doctorNameLabel = rootView.findViewById(R.id.printDoctorNameLabel);
            checkDateLabel = rootView.findViewById(R.id.printCheckDateLabel);
            reportDateLabel = rootView.findViewById(R.id.printReportDateLabel);
        }

        ViewGroup parent = (ViewGroup) rootView.getParent();
        if (parent != null) {
            parent.removeView(rootView);
        }
        InitView();
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (usbUtil != null)
        {
            usbUtil.closeport(1000);
        }
    }

    private UsbDevice mDevice;
    private UsbConnectionUtil usbUtil;

    private void InitView()
    {
        usbUtil = UsbConnectionUtil.getInstance();
        List<UsbDevice> deviceList = UsbConnectionUtil.getInstance().getDeviceList();
        if (deviceList.size() > 0)
        {
            // 这里需要判断一下打印机的型号
            // 先默认只连了打印机一台设备
            for (UsbDevice device : deviceList)
            {
                if (device.getProductId() == UsbProductID)
                {
                    mDevice = device;
                    break;
                }
            }
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
        titleLabel.setText(printList.get(0));
        IDLabel.setText(printList.get(1));
        patientNameLabel.setText(printList.get(2));
        resultLabel.setText(printList.get(3));
        doctorNameLabel.setText(printList.get(4));
        checkDateLabel.setText(printList.get(5));
        reportDateLabel.setText(printList.get(6));
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
    }

    private void TestPrint()
    {
        Toast.makeText(Utils.getContext(), "开始打印...", Toast.LENGTH_SHORT).show();
        List<String> printList = new ArrayList<>();
        printList.add(titleLabel.getText().toString());
        printList.add(IDLabel.getText().toString());
        printList.add(patientNameLabel.getText().toString());
        printList.add(resultLabel.getText().toString());
        printList.add(doctorNameLabel.getText().toString());
        printList.add(checkDateLabel.getText().toString());
        printList.add(reportDateLabel.getText().toString());
        byte[] bytes = TSCUtils.StartPrint(printList);
        usbUtil.sendMessage(bytes);
    }

    @Override
    public void insertUsb(UsbDevice device_add)
    {
        mDevice = device_add;
        Toast.makeText(Utils.getContext(), "Usb设备 " + device_add.getDeviceName() + " 插入", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void removeUsb(UsbDevice device_remove) {
        if (device_remove.getProductId() == UsbProductID)
        {
            Log.d(TAG, device_remove.getDeviceName()+" remove");
            usbUtil.closeport(1000);
            Toast.makeText(Utils.getContext(), "Usb设备 " + device_remove.getDeviceName() + " 拔出", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void getReadUsbPermission(UsbDevice usbDevice) {
        if (usbDevice.getProductId() == UsbProductID) {
            Toast.makeText(Utils.getContext(), "成功获取Usb设备 " + usbDevice.getDeviceName() + " 权限", Toast.LENGTH_SHORT).show();
            mDevice = usbDevice;
        }
    }

    @Override
    public void failedReadUsb(UsbDevice usbDevice) {
        if (usbDevice.getProductId() == UsbProductID) {
            Toast.makeText(Utils.getContext(), "读取Usb设备 " + usbDevice.getDeviceName() + " 失败", Toast.LENGTH_SHORT).show();
        }
    }
}
