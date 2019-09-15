package com.example.orchid.appupdate.update;

import com.example.orchid.appupdate.update.net.INetManager;
import com.example.orchid.appupdate.update.net.OkHttpNetManager;

public class AppUpDater {
    private static AppUpDater sInstance = new AppUpDater();

    private INetManager mNetManager = new OkHttpNetManager();

    public void setNetManager(INetManager manager){
        mNetManager = manager;
    }

    public INetManager getNetManager(){
        return mNetManager;
    }


    public  static  AppUpDater getInstance(){
        return  sInstance;
    }

}
