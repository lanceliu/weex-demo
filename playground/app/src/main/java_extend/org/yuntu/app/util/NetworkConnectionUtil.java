package org.yuntu.app.util;

import android.net.NetworkInfo;
import android.util.Log;

/**
 * Created by liufei on 16/9/26.
 */
public class NetworkConnectionUtil {

    public static final String WIFI = "wifi";
    public static final String MOBILE = "mobile";
    public static final String GSM = "gsm";
    public static final String GPRS = "gprs";
    public static final String EDGE = "edge";
    public static final String CDMA = "cdma";
    public static final String UMTS = "umts";
    public static final String HSPA = "hspa";
    public static final String HSUPA = "hsupa";
    public static final String HSDPA = "hsdpa";
    public static final String ONEXRTT = "1xrtt";
    public static final String EHRPD = "ehrpd";
    public static final String LTE = "lte";
    public static final String UMB = "umb";
    public static final String HSPA_PLUS = "hspa+";
    public static final String TYPE_UNKNOWN = "unknown";
    public static final String TYPE_WIFI = "wifi";
    public static final String TYPE_2G = "2g";
    public static final String TYPE_3G = "3g";
    public static final String TYPE_4G = "4g";
    public static final String TYPE_NONE = "none";

    public static  String getConnectionInfo(NetworkInfo info) {
        String type = TYPE_NONE;
        if(info != null) {
            if(!info.isConnected()) {
                type = TYPE_NONE;
            } else {
                type = getType(info);
            }
        }

        Log.d("CordovaNetworkManager", "Connection Type: " + type);
        return type;
    }

    private static String getType(NetworkInfo info) {
        if(info != null) {
            String type = info.getTypeName();
            if(type.toLowerCase().equals(WIFI)) {
                return TYPE_WIFI;
            } else {
                if(type.toLowerCase().equals(MOBILE)) {
                    type = info.getSubtypeName();
                    if(type.toLowerCase().equals(GSM) || type.toLowerCase().equals(GPRS) || type.toLowerCase().equals(EDGE)) {
                        return TYPE_2G;
                    }

                    if(type.toLowerCase().startsWith(CDMA) || type.toLowerCase().equals(UMTS) || type.toLowerCase().equals(ONEXRTT) || type.toLowerCase().equals(EHRPD) || type.toLowerCase().equals(HSUPA) || type.toLowerCase().equals(HSDPA) || type.toLowerCase().equals(HSPA)) {
                        return TYPE_3G;
                    }

                    if(type.toLowerCase().equals(LTE) || type.toLowerCase().equals(UMB) || type.toLowerCase().equals(HSPA_PLUS)) {
                        return TYPE_4G;
                    }
                }

                return TYPE_UNKNOWN;
            }
        } else {
            return TYPE_NONE;
        }
    }
}
