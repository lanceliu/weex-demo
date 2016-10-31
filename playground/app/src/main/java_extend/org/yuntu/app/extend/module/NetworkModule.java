package org.yuntu.app.extend.module;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.taobao.weex.WXSDKManager;
import com.taobao.weex.common.WXModule;
import com.taobao.weex.common.WXModuleAnno;

import org.yuntu.app.util.NetworkConnectionUtil;

import java.util.HashMap;
import java.util.Map;


public class NetworkModule extends WXModule {
    @WXModuleAnno(moduleMethod = true, runOnUIThread = true)
    public void getState( final String callbackId) {
        ConnectivityManager connectivityManager = (ConnectivityManager) mWXSDKInstance.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        Map<String, Object> map = new HashMap<String,Object>();
        map.put("type", NetworkConnectionUtil.getConnectionInfo(networkInfo));
        WXSDKManager.getInstance().callback(mWXSDKInstance.getInstanceId(), callbackId, map);
    }
}
