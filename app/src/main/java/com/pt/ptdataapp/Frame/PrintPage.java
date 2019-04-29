package com.pt.ptdataapp.Frame;


import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pt.ptdataapp.R;
import com.pt.ptdataapp.utils.TSCUtils;
import com.pt.ptdataapp.utils.usbHelper.UsbConnectionUtil;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class PrintPage extends Fragment {
    private String TAG = "PrintPage";
    private View rootView;
    public PrintPage() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (rootView == null)
        {
            rootView = inflater.inflate(R.layout.fragment_print_page, container, false);
        }

        ViewGroup parent = (ViewGroup) rootView.getParent();
        if (parent != null) {
            parent.removeView(rootView);
        }
        InitView();
        return rootView;
    }

    private UsbDevice mDevice;
    private UsbConnectionUtil usbUtil;

    private void InitView()
    {
        List<UsbDevice> deviceList = UsbConnectionUtil.getInstance().getDeviceList();
        if (deviceList.size() > 0)
        {
            mDevice = deviceList.get(0);
        }
        rootView.findViewById(R.id.testPrintBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                usbUtil = UsbConnectionUtil.getInstance();
                String s;
                if (usbUtil.openPort(mDevice)) {
                    TestPrint();
                }
            }
        });
    }

    private void TestPrint()
    {
        String[] printLabels = {"岭南医院INR检测报告单","ID:10000", "姓名：李木匠", "INR:100"};
        byte[] bytes = TSCUtils.StartPrint(printLabels);
        usbUtil.sendMessage(bytes);
    }
}
