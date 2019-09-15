package com.example.orchid.appupdate.update.net;

import java.io.File;

public interface INetDownLoadCallBack {
    void success(File apkFile);
    void progress(int progress);
    void failed(Throwable throwable);
}
