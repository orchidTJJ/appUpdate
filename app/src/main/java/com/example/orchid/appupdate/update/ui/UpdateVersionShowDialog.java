package com.example.orchid.appupdate.update.ui;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.orchid.appupdate.MainActivity;
import com.example.orchid.appupdate.R;
import com.example.orchid.appupdate.update.AppUpDater;
import com.example.orchid.appupdate.update.bean.DownloadBean;
import com.example.orchid.appupdate.update.net.INetDownLoadCallBack;
import com.example.orchid.appupdate.update.util.AppUtils;

import java.io.File;
import java.io.Serializable;

public class UpdateVersionShowDialog extends DialogFragment {

    public static final String KEY_DOWNLOAD_BEAN = "download_bean";
    private DownloadBean downloadBean;

    public UpdateVersionShowDialog() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            downloadBean = (DownloadBean) bundle.getSerializable(KEY_DOWNLOAD_BEAN);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Window window = getDialog().getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.gravity = Gravity.CENTER;
        //params.width = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(params);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_updater, container, false);
        bindEvent(view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    private void bindEvent(View view) {
        TextView tvTitle = view.findViewById(R.id.tv_dialog_title);
        TextView tvContent = view.findViewById(R.id.tv_dialog_content);
        final TextView btUpdate = view.findViewById(R.id.bt_dialog);

        tvTitle.setText(downloadBean.getTitle());
        tvContent.setText(downloadBean.getContent());
        btUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                view.setEnabled(false);
                File targetFile = new File(getActivity().getCacheDir(), "target.apk");
                AppUpDater.getInstance().getNetManager().download(downloadBean.getUrl(), targetFile, new INetDownLoadCallBack() {
                    @Override
                    public void success(File apkFile) {
                        view.setEnabled(true);
                        Log.d("tjj", apkFile.getAbsolutePath());
                        dismiss();
                        //安装apk check md5
                       String fileMd5 =  AppUtils.getFileMd5(apkFile);
                       Log.d("tjj",fileMd5);
                       if (fileMd5!=null&&fileMd5.equals(downloadBean.getMd5())){
                           AppUtils.installApp(getActivity(),apkFile);
                       }else {
                           Toast.makeText(getActivity(), "MD5校验失败" , Toast.LENGTH_SHORT).show();
                            return;
                       }

                    }

                    @Override
                    public void progress(int progress) {
                        Log.d("tjj", "进度: " + progress);
                        btUpdate.setText(progress + "%");
                    }

                    @Override
                    public void failed(Throwable throwable) {
                        view.setEnabled(true);
                        throwable.printStackTrace();
                        Toast.makeText(getActivity(), "下载失败", Toast.LENGTH_SHORT).show();
                    }
                },UpdateVersionShowDialog.this);
            }
        });

    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        Log.d("tjj","onDismiss");
        AppUpDater.getInstance().getNetManager().cancle(this);

    }

    public static void show(FragmentActivity activity, DownloadBean downloadBean) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(KEY_DOWNLOAD_BEAN, downloadBean);
        UpdateVersionShowDialog dialog = new UpdateVersionShowDialog();
        dialog.setArguments(bundle);
        dialog.show(activity.getSupportFragmentManager(), "updateVersionShowDialog");
    }
}
