package com.pt.ptdataapp;

import android.app.Application;

import com.pt.ptdataapp.utils.Utils;
import com.pt.ptdataapp.utils.usb.USBUtil;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Utils.init(this);
        USBUtil.getInstance().init(this);
    }
}
