package org.yuntu.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.weex.commons.util.ScreenUtil;
import com.taobao.weex.IWXRenderListener;
import com.taobao.weex.WXSDKEngine;
import com.taobao.weex.WXSDKInstance;
import com.taobao.weex.appfram.navigator.IActivityNavBarSetter;
import com.taobao.weex.appfram.storage.IWXStorageAdapter;
import com.taobao.weex.common.WXRenderStrategy;
import com.taobao.weex.utils.WXFileUtils;
import com.taobao.weex.utils.WXLogUtils;

import org.yuntu.app.constants.Constants;
import org.yuntu.app.extend.broadcast.NetworkStateReceiver;
import org.yuntu.app.extend.module.CallNativeModule;
import org.yuntu.app.https.HotRefreshManager;
import org.yuntu.app.https.WXHttpManager;
import org.yuntu.app.https.WXHttpTask;
import org.yuntu.app.https.WXRequestListener;
import org.yuntu.app.util.UrlParamUtil;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;


public class WXPageActivity extends WXBaseActivity implements IWXRenderListener, android.os.Handler.Callback {

    private class NavigatorAdapter implements IActivityNavBarSetter {

        @Override
        public boolean push(String param) {
            return false;
        }

        @Override
        public boolean pop(String param) {
            return false;
        }

        @Override
        public boolean setNavBarRightItem(String param) {
            return false;
        }

        @Override
        public boolean clearNavBarRightItem(String param) {
            return false;
        }

        @Override
        public boolean setNavBarLeftItem(String param) {
            return false;
        }

        @Override
        public boolean clearNavBarLeftItem(String param) {
            return false;
        }

        @Override
        public boolean setNavBarMoreItem(String param) {
            return false;
        }

        @Override
        public boolean clearNavBarMoreItem(String param) {
            return false;
        }

        @Override
        public boolean setNavBarTitle(String param) {
            return false;
        }
    }

    private static final String TAG = "WXPageActivity";
    public static Activity wxPageActivityInstance;

    private ViewGroup mContainer;
    private View mWAView;

    private WXSDKInstance mInstance;
    private Handler mWXHandler;
    private BroadcastReceiver mReceiver;

    private Uri mUri;
    private HashMap mConfigMap = new HashMap<String, Object>();

    private static final String DEFAULT_IP = "your_current_IP";

      // 仿真环境
//    private static String CURRENT_IP = "app.91cfx.com"; // your_current_IP
//    private static final String WEEX_INDEX_URL = "https://" + CURRENT_IP + "/dist/main.js";

      // 生产环境
    private static String CURRENT_IP = "app.91cfx.com"; // your_current_IP
    private static final String WEEX_INDEX_URL = "https://" + CURRENT_IP + "/dist/main.js";

    // 测试环境
//    private static String CURRENT_IP = "192.168.8.26"; // your_current_IP
//    private static final String WEEX_INDEX_URL = "http://" + CURRENT_IP + ":8002/dist/main.js";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wxpage);
        setCurrentWxPageActivity(this);
        WXSDKEngine.setActivityNavBarSetter(new NavigatorAdapter());

        mUri = getIntent().getData();
        Bundle bundle = getIntent().getExtras();
        if (mUri == null && bundle == null) {
            mUri = Uri.parse(WEEX_INDEX_URL);
        }
        if (bundle != null) {
            String bundleUrl = bundle.getString("bundleUrl");
            Log.e(TAG, "bundleUrl==" + bundleUrl);

            if (bundleUrl != null) {
                mConfigMap.put("bundleUrl", bundleUrl + Constants.WEEX_SAMPLES_KEY);
                mUri = Uri.parse(bundleUrl + Constants.WEEX_SAMPLES_KEY);

            }
        } else {
            mConfigMap.put("bundleUrl", mUri.toString() + Constants.WEEX_SAMPLES_KEY);
        }

        if (mUri == null) {
            Toast.makeText(this, "the uri is empty!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Log.e("TestScript_Guide mUri==", mUri.toString());
        initUIAndData();

        if (TextUtils.equals("http", mUri.getScheme()) || TextUtils.equals("https", mUri.getScheme())) {
            //      if url has key "_wx_tpl" then get weex bundle js
            String weexTpl = mUri.getQueryParameter(Constants.WEEX_TPL_KEY);
            String url = TextUtils.isEmpty(weexTpl) ? mUri.toString() : weexTpl;
            loadWXfromService(url);
            startHotRefresh();
        } else {
            loadWXfromLocal(false);
        }
        mInstance.onActivityCreate();
        registerBroadcastReceiver();
    }
    private void registerBroadcastReceiver() {
        mReceiver = new NetworkStateReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mReceiver, filter);
    }

    private void unregisterBroadcastReceiver() {
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
    }

    private void loadWXfromLocal(boolean reload) {
        if (reload && mInstance != null) {
            mInstance.destroy();
            mInstance = null;
        }
        if (mInstance == null) {
            mInstance = new WXSDKInstance(this);
            //        mInstance.setImgLoaderAdapter(new ImageAdapter(this));
            mInstance.registerRenderListener(this);
        }
        mContainer.post(new Runnable() {
            @Override
            public void run() {
                Activity ctx = WXPageActivity.this;
                Rect outRect = new Rect();
                ctx.getWindow().getDecorView().getWindowVisibleDisplayFrame(outRect);
                mConfigMap.put("bundleUrl", mUri.toString());
                String path = mUri.getScheme().equals("file") ? assembleFilePath(mUri) : mUri.toString();
                mInstance.render(TAG, WXFileUtils.loadAsset(path, WXPageActivity.this),
                        mConfigMap, null,
                        ScreenUtil.getDisplayWidth(WXPageActivity.this), ScreenUtil
                                .getDisplayHeight(WXPageActivity.this),
                        WXRenderStrategy.APPEND_ASYNC);
            }
        });
    }

    private String assembleFilePath(Uri uri) {
        if (uri != null && uri.getPath() != null) {
            return uri.getPath().replaceFirst("/", "");
        }
        return "";
    }

    private void initUIAndData() {
        mContainer = (ViewGroup) findViewById(R.id.container);
        mWXHandler = new Handler(this);
        HotRefreshManager.getInstance().setHandler(mWXHandler);
        addOnListener();
    }

    private void loadWXfromService(final String url) {

        if (mInstance != null) {
            mInstance.destroy();
        }

        mInstance = new WXSDKInstance(this);
        mInstance.registerRenderListener(this);

        WXHttpTask httpTask = new WXHttpTask();
        httpTask.url = url;
        httpTask.requestListener = new WXRequestListener() {

            @Override
            public void onSuccess(WXHttpTask task) {
                Log.e(TAG, "into--[http:onSuccess] url:" + url);
                try {
                    mConfigMap.put("bundleUrl", url);
                    mInstance.render(TAG, new String(task.response.data, "utf-8"), mConfigMap, null, ScreenUtil.getDisplayWidth(WXPageActivity.this), ScreenUtil.getDisplayHeight(WXPageActivity.this), WXRenderStrategy.APPEND_ASYNC);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(WXHttpTask task) {
                Log.e(TAG, "into--[http:onError]");
                Toast.makeText(getApplicationContext(), "network error!", Toast.LENGTH_SHORT).show();
            }
        };

        WXHttpManager.getInstance().sendRequest(httpTask);
    }

    /**
     * hot refresh
     */
    private void startHotRefresh() {
//        try {
//            String host = new URL(mUri.toString()).getHost();
//            String wsUrl = "ws://" + host + ":8082";
//            mWXHandler.obtainMessage(Constants.HOT_REFRESH_CONNECT, 0, 0, wsUrl).sendToTarget();
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        }
    }

    private void addOnListener() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mInstance != null) {
            mInstance.onActivityDestroy();
        }
        unregisterBroadcastReceiver();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mInstance != null) {
            mInstance.onActivityResume();
        }
    }

    public Activity getCurrentWxPageActivity() {
        return wxPageActivityInstance;
    }

    public void setCurrentWxPageActivity(Activity activity) {
        wxPageActivityInstance = activity;
    }

    @Override
    public boolean handleMessage(Message msg) {

        switch (msg.what) {
            case Constants.HOT_REFRESH_CONNECT:
                HotRefreshManager.getInstance().connect(msg.obj.toString());
                break;
            case Constants.HOT_REFRESH_DISCONNECT:
                HotRefreshManager.getInstance().disConnect();
                break;
            case Constants.HOT_REFRESH_REFRESH:
                loadWXfromService(mUri.toString());
                break;
            case Constants.HOT_REFRESH_CONNECT_ERROR:
                Toast.makeText(this, "hot refresh connect error!", Toast.LENGTH_SHORT).show();
                break;
        }

        return false;
    }

    @Override
    public void onViewCreated(WXSDKInstance instance, View view) {
        WXLogUtils.e("into--[onViewCreated]");
        if (mWAView != null && mContainer != null && mWAView.getParent() == mContainer) {
            mContainer.removeView(mWAView);
        }

        mWAView = view;
        mContainer.addView(view);
        mContainer.requestLayout();
        Log.d("WARenderListener", "renderSuccess");
    }

    @Override
    public void onException(WXSDKInstance instance, String errCode, String msg) {
        if (!TextUtils.isEmpty(errCode) && errCode.contains("|")) {
            String[] errCodeList = errCode.split("\\|");
            String code = errCodeList[1];
            String codeType = errCode.substring(0, errCode.indexOf("|"));

            if (TextUtils.equals("1", codeType)) {
                String errMsg = "codeType:" + codeType + "\n" + " errCode:" + code + "\n" + " ErrorInfo:" + msg;
                degradeAlert(errMsg);
                return;
            } else {
                Toast.makeText(getApplicationContext(), "errCode:" + errCode + " Render ERROR:" + msg, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void degradeAlert(String errMsg) {
        new AlertDialog.Builder(this)
                .setTitle("Downgrade success")
                .setMessage(errMsg)
                .setPositiveButton("OK", null)
                .show();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!TextUtils.equals("file", mUri.getScheme())) {
            getMenuInflater().inflate(R.menu.refresh, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.action_refresh) {
            String scheme = mUri.getScheme();
            if (mUri.isHierarchical() && (TextUtils.equals(scheme, "http") || TextUtils.equals(scheme, "https"))) {
                String weexTpl = mUri.getQueryParameter(Constants.WEEX_TPL_KEY);
                String url = TextUtils.isEmpty(weexTpl) ? mUri.toString() : weexTpl;
                loadWXfromService(url);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mInstance != null) {
            mInstance.onActivityPause();
        }
    }

    private void exitApp() {
        if ( exit ) {
            finish();
        }
    }
    private static int BACKUP_PRESSED = 0;
    private static Boolean exit = false;
    @Override
    public void onBackPressed(){
        if (mUri.toString().contains("main.js") ||
                mUri.toString().contains("login.js")) {
            if (exit) {
                exitApp();
            } else {
                Toast.makeText(this, "再按一次退出应用", Toast.LENGTH_SHORT).show();
                exit = true;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        exit = false;
                    }
                }, 3 * 1000);
            }
            return;
        }
        super.onBackPressed();
    }
    @Override
    public void finish() {
        BACKUP_PRESSED = 1;
        super.finish();
    }
    /**
     * 页面返回时，需要判断是否是返回显示的
     */
    @Override
    protected void onStart() {
        exitApp();
        super.onStart();
        final Map<String, String> params = UrlParamUtil.parseUrlParam(mUri.toString());
        IWXStorageAdapter mStorageAdapter = WXSDKEngine.getIWXStorageAdapter();
        mStorageAdapter.getItem("tabs", new IWXStorageAdapter.OnResultReceivedListener() {
            @Override
            public void onReceived(Map<String, Object> data) {
                JSONObject job = (JSONObject) JSON.parse(data.get("data").toString());
//                Log.i("aha", data.get("data").toString());

                if ( job == null || job.getString("main") == null ) {
                    params.remove( "tab" );
                } else {
                    params.put("tab", job.getString("main"));
                }

                if ( BACKUP_PRESSED == 1) {
                    BACKUP_PRESSED = 0;
                    String url = UrlParamUtil.composeSearchUrl( mUri.toString(), params );
//                    Log.i("aha--", url);
                    mUri = Uri.parse(url);

                    if ( "1".equals( params.get("refresh") ) ) {
                        mWXHandler.obtainMessage(Constants.HOT_REFRESH_REFRESH).sendToTarget();
                    }
                }
            }
        });
    }

    public static final int REQUESTCODE = 8;
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUESTCODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //用户同意了授权
                    this.startActivity(CallNativeModule.intent);

                } else {
                    //用户拒绝了授权
                    // Toast.makeText(MainActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    @Override
    public void onRenderSuccess(WXSDKInstance instance, int width, int height) {

    }

    @Override
    public void onRefreshSuccess(WXSDKInstance instance, int width, int height) {

    }
}
