package org.yuntu.app.extend.module;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.taobao.weex.WXSDKManager;
import com.taobao.weex.common.WXModule;
import com.taobao.weex.common.WXModuleAnno;

import org.yuntu.app.WXPageActivity;

import java.util.HashMap;
import java.util.Map;


public class CallNativeModule extends WXModule {

    public static Intent intent = null;
    @WXModuleAnno(moduleMethod = true, runOnUIThread = true)
    public void open(String jsonParams, final String callbackId) {
        JSONObject job = (JSONObject) JSON.parse(jsonParams);

        String callType = job.getString("action");
        String value = job.getString("value");
        Map<String, Object> map = new HashMap<String, Object>();
        switch (callType) {
            case "tel":
                intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + value));
                callDial();
                break;
            case "browser":
                Uri uri = Uri.parse(value);
                intent = new Intent(Intent.ACTION_VIEW, uri);
                mWXSDKInstance.getContext().startActivity(intent);
                break;
        }

        WXSDKManager.getInstance().callback(mWXSDKInstance.getInstanceId(), callbackId, map);
    }

    public void callDial() {
        if (ContextCompat.checkSelfPermission(mWXSDKInstance.getContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(WXPageActivity.wxPageActivityInstance, Manifest.permission.CALL_PHONE)) {
                new AlertDialog.Builder(mWXSDKInstance.getContext())
                        .setMessage("app需要开启权限才能使用此功能")
                        .setPositiveButton("设置", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                intent.setData(Uri.parse("package:" + mWXSDKInstance.getContext().getPackageName()));
                                mWXSDKInstance.getContext().startActivity(intent);
                            }
                        }).setNegativeButton("取消", null).create().show();
            } else {
                //申请权限
                ActivityCompat.requestPermissions(WXPageActivity.wxPageActivityInstance, new String[]{Manifest.permission.CALL_PHONE}, WXPageActivity.REQUESTCODE);
            }

        } else {
            //已经拥有权限进行拨打
            mWXSDKInstance.getContext().startActivity(intent);
        }
    }


}
