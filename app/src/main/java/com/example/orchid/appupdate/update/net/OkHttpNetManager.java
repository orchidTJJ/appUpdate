package com.example.orchid.appupdate.update.net;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OkHttpNetManager implements INetManager {

    private static OkHttpClient sOkHttpClient;

    private static Handler sHandler = new Handler(Looper.myLooper());

    static {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(30, TimeUnit.SECONDS);
        sOkHttpClient = builder.build();
    }


    @Override
    public void get(String url, final INetCallBack callback,Object tag) {
        Request.Builder builder = new Request.Builder();
        Request request = builder.url(url).get().tag(tag).build();
        Call call = sOkHttpClient.newCall(request);
        //call.execute()//同步操作
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                //非ui线程
                sHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.failed(e);
                    }
                });
            }

            @Override
            public void onResponse(final Call call, Response response) throws IOException {
                try {
                    final String string = response.body().string();
                    sHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.success(string);
                        }
                    });
                } catch (Throwable throwable) {
                    callback.failed(throwable);
                }

            }
        });
    }

    @Override
    public void download(String url, final File targetFile, final INetDownLoadCallBack callback,Object tag) {
        if (!targetFile.exists()) {
            targetFile.getParentFile().mkdirs();
        }
        Request.Builder builder = new Request.Builder();
        Request request = builder.url(url).get().tag(tag).build();
        Call call = sOkHttpClient.newCall(request);
        //call.execute()//同步操作
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                //非ui线程
                sHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.failed(e);
                    }
                });
            }

            @Override
            public void onResponse(final Call call, Response response) throws IOException {
                InputStream is = null;
                OutputStream os = null;
                try {
                    final long totalLen = response.body().contentLength();

                    is = response.body().byteStream();
                    os = new FileOutputStream(targetFile);

                    byte[] buffer = new byte[8 * 1024];
                    long curlen = 0;
                    int bufferLen = 0;

                    while (!call.isCanceled()&&(bufferLen = is.read(buffer)) != -1) {
                        os.write(buffer, 0, bufferLen);
                        os.flush();
                        curlen += bufferLen;
                        final long finalCurlen = curlen;
                        sHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.progress((int) (finalCurlen * 1.0F / totalLen*100));
                            }
                        });
                    }
                    if (call.isCanceled()) {
                        return;
                    }
                    try {
                        targetFile.setExecutable(true, false);
                        targetFile.setReadable(true, false);
                        targetFile.setWritable(true, false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    sHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.success(targetFile);
                        }
                    });
                } catch (final Throwable e) {
                    if (call.isCanceled()) {
                        return;
                    }
                    e.printStackTrace();
                    sHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.failed(e);
                        }
                    });
                } finally {
                    if (is != null) {
                        is.close();
                    }
                    if (os != null) {
                        os.close();
                    }
                }

            }
        });
    }


    @Override
    public void cancle(Object tag) {
        List<Call> queuedCalls = sOkHttpClient.dispatcher().queuedCalls();
        if (queuedCalls != null) {
            for (Call call : queuedCalls) {
                if (tag.equals(call.request().tag())){
                    call.cancel();
                }
            }
        }

        List<Call> runningCalls = sOkHttpClient.dispatcher().runningCalls();
        if (runningCalls != null) {
            for (Call call : runningCalls) {
                if (tag.equals(call.request().tag())){
                    call.cancel();
                }
            }
        }
    }
}
