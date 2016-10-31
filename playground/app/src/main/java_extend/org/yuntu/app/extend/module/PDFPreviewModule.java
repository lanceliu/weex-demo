package org.yuntu.app.extend.module;

import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.taobao.weex.common.WXModule;
import com.taobao.weex.common.WXModuleAnno;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;


public class PDFPreviewModule extends WXModule {
    private static final String WEEX_CATEGORY = "org.yuntu.android.intent.category.PDF_PREVIEW";

    @WXModuleAnno(moduleMethod = true, runOnUIThread = true)
    public void previewPdf(String params) {

        com.alibaba.fastjson.JSONObject job = (com.alibaba.fastjson.JSONObject) JSON.parse( params );
        String url = job.getString("url");
        String title =  job.getString("title");
        if (TextUtils.isEmpty(url)) {
            return;
        }
        String scheme = Uri.parse(url).getScheme();
        StringBuilder builder = new StringBuilder();
        if (TextUtils.equals("http", scheme) || TextUtils.equals("https", scheme) || TextUtils.equals("file", scheme)) {
            builder.append(url);
        } else {
            builder.append("http:");
            builder.append(url);
        }

        Uri uri = Uri.parse(builder.toString());
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.putExtra("url", url);
        intent.putExtra("title", title);
        intent.addCategory(WEEX_CATEGORY);
        mWXSDKInstance.getContext().startActivity(intent);
    }
}
