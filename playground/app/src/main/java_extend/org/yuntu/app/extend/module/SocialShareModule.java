package org.yuntu.app.extend.module;

import android.app.Activity;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.taobao.weex.WXSDKManager;
import com.taobao.weex.common.WXModule;
import com.taobao.weex.common.WXModuleAnno;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.utils.Log;

import org.yuntu.app.R;

import java.util.HashMap;
import java.util.Map;


public class SocialShareModule extends WXModule {

    private static final String WEEX_CATEGORY = "org.yuntu.android.intent.category.SOCIAL_SHARE";
    private static Long clickTime = 0L;
    private String callbackId;

    @WXModuleAnno(moduleMethod = true, runOnUIThread = true)
    public void share(String params, String callbackId) {
        this.callbackId = callbackId;

        JSONObject job = (JSONObject) JSON.parse( params );
        UMImage img = new UMImage(mWXSDKInstance.getContext(),  R.mipmap.ic_launcher );
        if ( job.getString("thumb") != null )
            img = new UMImage(mWXSDKInstance.getContext(),  job.getString("thumb") );
        new ShareAction((Activity) mWXSDKInstance.getContext()).setDisplayList(SHARE_MEDIA.QQ,SHARE_MEDIA.WEIXIN,SHARE_MEDIA.WEIXIN_CIRCLE)
                .withTitle( job.getString("title") )
                .withText( job.getString("desc") )
                .withMedia( img )
                .withTargetUrl( job.getString("link") )
                .setCallback(umShareListener)
                .open();
    }

    private UMShareListener umShareListener = new UMShareListener() {
        @Override
        public void onResult(SHARE_MEDIA platform) {
            callback(platform);
            if(platform.name().equals("WEIXIN_FAVORITE")){
                Toast.makeText(mWXSDKInstance.getContext(),platform + " 收藏成功啦",Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(mWXSDKInstance.getContext(), platform + " 分享成功啦", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onError(SHARE_MEDIA platform, Throwable t) {
            callback(platform);
            Toast.makeText(mWXSDKInstance.getContext(),platform + " 分享失败啦", Toast.LENGTH_SHORT).show();
            if(t!=null){
                Log.d("throw","throw:"+t.getMessage());
            }
        }

        @Override
        public void onCancel(SHARE_MEDIA platform) {
            callback(platform);
            Toast.makeText(mWXSDKInstance.getContext(),platform + " 分享取消了", Toast.LENGTH_SHORT).show();
        }

        private void callback( SHARE_MEDIA platform ) {
            Log.d("share", platform.name());
            Map<String, Object> map = new HashMap<String,Object>();
            map.put("type", platform.name());
            WXSDKManager.getInstance().callback(mWXSDKInstance.getInstanceId(), callbackId, map);
        }
     };

}
