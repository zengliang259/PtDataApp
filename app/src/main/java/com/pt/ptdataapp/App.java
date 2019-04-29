package com.pt.ptdataapp;

import android.app.Application;

import com.pt.ptdataapp.utils.Utils;
import com.pt.ptdataapp.utils.usbHelper.UsbConnectionUtil;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Utils.init(this);
        UsbConnectionUtil.getInstance().init(this);
    }
}
