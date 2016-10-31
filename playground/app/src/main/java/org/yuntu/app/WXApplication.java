package org.yuntu.app;

import android.app.Application;

import com.alibaba.weex.commons.adapter.ImageAdapter;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.taobao.weex.InitConfig;
import com.taobao.weex.WXEnvironment;
import com.taobao.weex.WXSDKEngine;
import com.taobao.weex.common.WXException;
import com.umeng.socialize.PlatformConfig;

import org.yuntu.app.extend.PlayDebugAdapter;
import org.yuntu.app.extend.component.RichText;
import org.yuntu.app.extend.module.CallNativeModule;
import org.yuntu.app.extend.module.DatePickerModule;
import org.yuntu.app.extend.module.MyModule;
import org.yuntu.app.extend.module.NetworkModule;
import org.yuntu.app.extend.module.PDFPreviewModule;
import org.yuntu.app.extend.module.PhotoPickModule;
import org.yuntu.app.extend.module.RenderModule;
import org.yuntu.app.extend.module.SocialShareModule;
import org.yuntu.app.extend.module.TrackModule;
import org.yuntu.app.extend.module.WXEventModule;

public class WXApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
//    initDebugEnvironment(false, "DEBUG_SERVER_HOST");
        WXSDKEngine.addCustomOptions("appName", "WXSample");
        WXSDKEngine.addCustomOptions("appGroup", "WXApp");
        WXSDKEngine.initialize(this,
                new InitConfig.Builder()
                        .setImgAdapter(new ImageAdapter())
                        .setDebugAdapter(new PlayDebugAdapter())
                        .build()
        );

        try {
            Fresco.initialize(this);
            WXSDKEngine.registerComponent("richtext", RichText.class);
            WXSDKEngine.registerModule("render", RenderModule.class);
            WXSDKEngine.registerModule("event", WXEventModule.class);
            WXSDKEngine.registerModule("pdfpreview", PDFPreviewModule.class);
            WXSDKEngine.registerModule("yt-share", SocialShareModule.class);
            WXSDKEngine.registerModule("yt-date", DatePickerModule.class);
            WXSDKEngine.registerModule("yt-networkConnection", NetworkModule.class);
            WXSDKEngine.registerModule("yt-callNative", CallNativeModule.class);
            WXSDKEngine.registerModule("yt-track", TrackModule.class);
            WXSDKEngine.registerModule("photopick", PhotoPickModule.class);

            WXSDKEngine.registerModule("myModule", MyModule.class);
            //微信 wx12342956d1cab4f9,a5ae111de7d9ea137e88a5e02c07c94d  TODO: 微信对接
            PlatformConfig.setWeixin("wx12342956d1cab4f9","a5ae111de7d9ea137e88a5e02c07c94d");
            PlatformConfig.setQQZone("wahahahhahahha", "sadfasdfasdfasdf");
        } catch (WXException e) {
            e.printStackTrace();
        }


    }

    /**
     * @param enable enable remote debugger. valid only if host not to be "DEBUG_SERVER_HOST".
     *               true, you can launch a remote debugger and inspector both.
     *               false, you can  just launch a inspector.
     * @param host   the debug server host, must not be "DEBUG_SERVER_HOST", a ip address or domain will be OK.
     *               for example "127.0.0.1".
     */
    private void initDebugEnvironment(boolean enable, String host) {
        if (!"DEBUG_SERVER_HOST".equals(host)) {
            WXEnvironment.sRemoteDebugMode = enable;
            WXEnvironment.sRemoteDebugProxyUrl = "ws://" + host + ":8088/debugProxy/native";
        }
    }

}
