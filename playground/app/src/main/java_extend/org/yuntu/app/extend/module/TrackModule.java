package org.yuntu.app.extend.module;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.taobao.weex.WXSDKManager;
import com.taobao.weex.common.WXModule;
import com.taobao.weex.common.WXModuleAnno;
import com.umeng.analytics.MobclickAgent;

import java.util.HashMap;
import java.util.Map;


public class TrackModule extends WXModule {

    @WXModuleAnno(moduleMethod = true, runOnUIThread = true)
    public void sendTrack(String jsonParams, final String callbackId) {
        JSONObject job = (JSONObject) JSON.parse(jsonParams);
        Map<String,String> map = new HashMap<>();
        map.put("pageName", job.getString("pageName"));
        MobclickAgent.onEvent(mWXSDKInstance.getContext(), job.getString("path"), map);
        Map<String,Object>  retMap = new HashMap<String, Object>();
        WXSDKManager.getInstance().callback(mWXSDKInstance.getInstanceId(), callbackId, retMap);
    }
}
