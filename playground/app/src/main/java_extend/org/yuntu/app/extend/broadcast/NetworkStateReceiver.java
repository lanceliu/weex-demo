package org.yuntu.app.extend.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import org.yuntu.app.util.NetworkConnectionUtil;


/**
 * Created by liufei on 16/9/27.
 */
public class NetworkStateReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {

        Log.d("app","Network connectivity change");
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        String type = NetworkConnectionUtil.getConnectionInfo( networkInfo );
        switch ( type ) {
            case NetworkConnectionUtil.TYPE_NONE:
                Toast.makeText(context, "没有网络", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }
}
