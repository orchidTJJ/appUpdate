package com.example.orchid.appupdate.update.net;

import java.io.File;

public interface INetManager {
    void get(String url,INetCallBack callback,Object tag);
    void download(String url, File targetFile, INetDownLoadCallBack callback,Object tag);
    void cancle(Object tag);
}
