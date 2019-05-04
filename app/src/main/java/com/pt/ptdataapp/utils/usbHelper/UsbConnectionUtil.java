package com.pt.ptdataapp.utils.usbHelper;

import android.app.Activity;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.util.Log;
import android.widget.Toast;

import com.pt.ptdataapp.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static com.pt.ptdataapp.utils.usbHelper.USBBroadCastReceiver.ACTION_USB_PERMISSION;

public class UsbConnectionUtil {
    private static UsbConnectionUtil instance;

    private PendingIntent mPermissionIntent;
    private UsbManager usbManager;
    private USBBroadCastReceiver usbReceiver;
    private UsbInterface usbInterface;
    private UsbEndpoint usbEndpointIn;
    private UsbEndpoint usbEndpointOut;
    private UsbDeviceConnection usbConnection;
    //回调
    private USBBroadCastReceiver.UsbListener usbListener;

    private UsbConnectionUtil() {
    }

    public static UsbConnectionUtil getInstance() {
        if (instance == null) {
            synchronized (UsbConnectionUtil.class) {
                if (instance == null) {
                    instance = new UsbConnectionUtil();
                }
            }
        }
        return instance;
    }

    public UsbManager getUsbManager() {
        return usbManager;
    }

    public void init(Context context, USBBroadCastReceiver.UsbListener usbListener) {
        usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        this.usbListener = usbListener;
        usbReceiver = new USBBroadCastReceiver();
        usbReceiver.setUsbListener(this.usbListener);
        registerReceiver(context);
    }

    /**
     * 获取 USB 设备列表
     */
    public List<UsbDevice> getDeviceList() {
        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        List<UsbDevice> usbDevices = new ArrayList<>();
        while (deviceIterator.hasNext()) {
            UsbDevice device = deviceIterator.next();
            usbDevices.add(device);
            Log.e("USBUtil", "getDeviceList: " + device.getDeviceName());
        }
        return usbDevices;
    }

    /**
     *
     * @param vendorId  厂商ID
     * @param productId 产品ID
     * @return device
     */
    public UsbDevice getUsbDevice(int vendorId, int productId) {
        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        while (deviceIterator.hasNext()) {
            UsbDevice device = deviceIterator.next();
            if (device.getVendorId() == vendorId && device.getProductId() == productId) {
                Log.e("USBUtil", "getDeviceList: " + device.getDeviceName());
                return device;
            }
        }
        Toast.makeText(Utils.getContext(), "没有发现对应的USB打印设备", Toast.LENGTH_SHORT).show();
        return null;
    }

    /**
     * 判断对应 USB 设备是否有权限
     */
    public boolean hasPermission(UsbDevice device) {
        return usbManager.hasPermission(device);
    }

    /**
     * 请求获取指定 USB 设备的权限
     */
    public void requestPermission(UsbDevice device) {
        if (device != null) {
            if (!usbManager.hasPermission(device)){
                if (mPermissionIntent != null) {
                    usbManager.requestPermission(device, mPermissionIntent);
//                    Toast.makeText(Utils.getContext(), "请求USB权限", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(Utils.getContext(), "请注册USB广播", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    /**
     * 打开通信端口
     */
    public boolean openPort(UsbDevice device) {
        //获取设备接口，一般只有一个，多个的自己研究去
        usbInterface = device.getInterface(0);

        // 判断是否有权限
        if (hasPermission(device)) {
            // 打开设备，获取 UsbDeviceConnection 对象，连接设备，用于后面的通讯
            usbConnection = usbManager.openDevice(device);

            if (usbConnection == null) {
                return false;
            }
            if (usbConnection.claimInterface(usbInterface, true)) {
                Toast.makeText(Utils.getContext(), "找到 USB 设备接口", Toast.LENGTH_SHORT).show();
            } else {
                usbConnection.close();
                Toast.makeText(Utils.getContext(), "没有找到 USB 设备接口", Toast.LENGTH_SHORT).show();
                return false;
            }
        } else {
            Toast.makeText(Utils.getContext(), "没有 USB 权限", Toast.LENGTH_SHORT).show();
            return false;
        }

        //获取接口上的两个端点，分别对应 OUT 和 IN
        for (int i = 0; i < usbInterface.getEndpointCount(); ++i) {
            UsbEndpoint end = usbInterface.getEndpoint(i);
            if (end.getDirection() == UsbConstants.USB_DIR_IN) {
                usbEndpointIn = end;
            } else {
                usbEndpointOut = end;
            }
        }
        return true;
    }

    public int sendMessage(byte[] bytes) {
        return usbConnection.bulkTransfer(usbEndpointOut, bytes, bytes.length, 500);
    }

    public void closeport(int timeout) {
        if (usbConnection == null) {
            return;
        }
        try {
            Thread.sleep((long) timeout);
        } catch (InterruptedException var4) {
            var4.printStackTrace();
        }
        try {
            usbConnection.close();
            usbConnection.releaseInterface(usbInterface);
            usbConnection = null;
            usbEndpointIn = null;
            usbEndpointOut = null;
            usbManager = null;
            usbInterface = null;
            Log.d("DemoKit", "Device closed. ");
        } catch (Exception var3) {
            Log.e("DemoKit", "Exception: " + var3.getMessage());
        }
    }

    /**
     * 注册广播
     */
    public void registerReceiver(Context context) {
        mPermissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        context.registerReceiver(usbReceiver, filter);
    }

    public void unRegisterReceiver(Activity context) {
        context.unregisterReceiver(usbReceiver);
        mPermissionIntent = null;
    }
}
