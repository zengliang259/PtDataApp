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
import com.pt.ptdataapp.adapter.USBDeviceRecAdapter;
import com.pt.ptdataapp.utils.usb.USBUtil;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class PrintPage extends Fragment {

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
    private USBDeviceRecAdapter deviceRecAdapter = new USBDeviceRecAdapter(new USBDeviceRecAdapter.OnItemClick() {
        @Override
        public void onItemClick(UsbDevice device) {
            if (USBUtil.getInstance().hasPermission(device)) {
                mDevice = device;
            } else {
                USBUtil.getInstance().requestPermission(device);
            }
        }
    });
    private USBUtil usbUtil;

    private void InitView()
    {
        ((RecyclerView) rootView.findViewById(R.id.usb_devices)).setAdapter(deviceRecAdapter);
        rootView.findViewById(R.id.load_devices).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<UsbDevice> deviceList = USBUtil.getInstance().getDeviceList();
                deviceRecAdapter.setData(deviceList);
            }
        });
        rootView.findViewById(R.id.testPrintBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                usbUtil = USBUtil.getInstance();
                String s;
                if (usbUtil.openPort(mDevice)) {
                    usbUtil.sendMessage(setup(40, 30, 4, 4, 0, 2, 0));
                    s = "HOME\n";
//                    usbUtil.sendMessage(s.getBytes());
                    usbUtil.sendMessage(clearbuffer());
                    try {
                        s = "TEXT 100,200,\"TSS24.BF2\",0,1,1,\"你好打印机\n";
                        usbUtil.sendMessage(s.getBytes("GBK"));
                        s = "TEXT 100,100,\"TSS24.BF2\",0,1,1,\"你好打印机\n";
                        usbUtil.sendMessage(s.getBytes("GBK"));
                    } catch (UnsupportedEncodingException e) {
                        Log.e("MainActivity", "onClick  3" + "失败" + e.toString());
                        return;
                    }
                    usbUtil.sendMessage(barcode(10, 20, "128", 20, 1, 0, 3, 3, "123456789"));
                    usbUtil.sendMessage(printlabel(1, 1));
                }
            }
        });
    }



    public byte[] setup(int width, int height, int speed, int density, int sensor, int sensor_distance, int sensor_offset) {
        String message = "";
        String size = "SIZE " + width + " mm" + ", " + height + " mm";
        String speed_value = "SPEED " + speed;
        String density_value = "DENSITY " + density;
        String sensor_value = "";
        if (sensor == 0) {
            sensor_value = "GAP " + sensor_distance + " mm" + ", " + sensor_offset + " mm";
        } else if (sensor == 1) {
            sensor_value = "BLINE " + sensor_distance + " mm" + ", " + sensor_offset + " mm";
        }

        message = size + "\r\n" + speed_value + "\r\n" + density_value + "\r\n" + sensor_value + "\r\n";
        return message.getBytes();
    }

    public byte[] clearbuffer() {
        String message = "CLS\r\n";
        return message.getBytes();
    }


    public byte[] barcode(int x, int y, String type, int height, int human_readable, int rotation, int narrow, int wide, String string) {
        String message = "";
        String barcode = "BARCODE ";
        String position = x + "," + y;
        String mode = "\"" + type + "\"";
        String height_value = "" + height;
        String human_value = "" + human_readable;
        String rota = "" + rotation;
        String narrow_value = "" + narrow;
        String wide_value = "" + wide;
        String string_value = "\"" + string + "\"";
        message = barcode + position + " ," + mode + " ," + height_value + " ," + human_value + " ," + rota + " ," + narrow_value + " ," + wide_value + " ," + string_value + "\r\n";
        return message.getBytes();
    }

    public byte[] printlabel(int quantity, int copy) {
        String message = "";
        message = "PRINT " + quantity + ", " + copy + "\r\n";
        return message.getBytes();
    }
}
