package com.example.orchid.appupdate.update.net;

public interface INetCallBack {
    void success(String response);
    void failed(Throwable throwable);
}
