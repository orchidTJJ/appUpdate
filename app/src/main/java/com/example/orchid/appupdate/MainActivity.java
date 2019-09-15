package com.example.orchid.appupdate;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.example.orchid.appupdate.update.AppUpDater;
import com.example.orchid.appupdate.update.bean.DownloadBean;
import com.example.orchid.appupdate.update.net.INetCallBack;
import com.example.orchid.appupdate.update.net.INetDownLoadCallBack;
import com.example.orchid.appupdate.update.ui.UpdateVersionShowDialog;
import com.example.orchid.appupdate.update.util.AppUtils;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void update(View view) {
        AppUpDater.getInstance().getNetManager().get("http://59.110.162.30/app_updater_version.json", new INetCallBack() {
            @Override
            public void success(String response) {
                Log.d("tjj", response);
                //1.解析json
                DownloadBean downloadBean = JSON.parseObject(response, DownloadBean.class);
                //2.做版本匹配

                if (downloadBean == null) {
                    Toast.makeText(MainActivity.this, "接口错误", Toast.LENGTH_SHORT).show();
                    return;
                }

                long versionCode = Long.parseLong(downloadBean.getVersionCode());
                if (versionCode <= AppUtils.getVersionCode(MainActivity.this)) {
                    //不需要更新
                    return;
                }

                //3 如果需要更新 弹框 点击下载
                UpdateVersionShowDialog.show(MainActivity.this, downloadBean);


            }

            @Override
            public void failed(Throwable throwable) {
                throwable.printStackTrace();
            }
        }, this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AppUpDater.getInstance().getNetManager().cancle(this);
    }
}
