package org.yuntu.app.extend.module;

import android.content.Intent;
import android.util.Log;
import com.taobao.weex.common.WXModule;
import com.taobao.weex.common.WXModuleAnno;

public class PhotoPickModule extends WXModule {

    private static final String WEEX_CATEGORY = "org.yuntu.android.intent.category.PHOTO_PICK";

    /**
     *
     * @param parms
     *
     */
    @WXModuleAnno
    public void pickPhoto(String parms, String callbackId) {
        Log.d("oo==", parms);
        Intent intent = new Intent( Intent.ACTION_VIEW );
        intent.addCategory( WEEX_CATEGORY );
        intent.putExtra("parms", parms);
        intent.putExtra("callbackId", callbackId);
        intent.putExtra("instanceId", mWXSDKInstance.getInstanceId());

        mWXSDKInstance.getContext().startActivity(intent);
    }
}
