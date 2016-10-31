package org.yuntu.app.extend.module;

import android.app.AlertDialog;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.taobao.weex.WXSDKManager;
import com.taobao.weex.common.WXModule;
import com.taobao.weex.common.WXModuleAnno;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import cn.aigestudio.datepicker.cons.DPMode;
import cn.aigestudio.datepicker.views.DatePicker;


public class DatePickerModule extends WXModule {

    @WXModuleAnno(moduleMethod = true, runOnUIThread = true)
    public void calendar(String jsonParams, final String callbackId) {
        JSONObject job = (JSONObject) JSON.parse(jsonParams);

        final AlertDialog dialog = new AlertDialog.Builder(mWXSDKInstance.getContext()).create();
        dialog.show();
        DatePicker picker = new DatePicker(mWXSDKInstance.getContext());

        final Calendar date = Calendar.getInstance();
        date.setTimeInMillis(job.getLongValue("value") != 0l ? job.getLongValue("value"): System.currentTimeMillis());
        picker.setDate(date.get(Calendar.YEAR), date.get(Calendar.MONTH)+1);
        picker.setMode(DPMode.SINGLE);

        final SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
        final String dateMin,  dateMax;
        if ( job.getLongValue("min") != 0l ) {
            date.setTimeInMillis(job.getLongValue("min") );
            dateMin = sf.format( date.getTime()  );
        } else
            dateMin = null;

        if ( job.getLongValue("max") != 0l ) {
            date.setTimeInMillis(job.getLongValue("max") );
            dateMax = sf.format( date.getTime()  );
        } else
            dateMax = null;
        picker.setOnDatePickedListener(new DatePicker.OnDatePickedListener() {
            @Override
            public void onDatePicked(String dateStr) {
                try {
                    String dateReFormatStr = sf.format( sf.parse( dateStr));
                    if ( dateMin != null && dateReFormatStr.compareTo(dateMin) < 0 ) {
                        Toast.makeText(mWXSDKInstance.getContext(), "所选日期不小于"+dateMin, Toast.LENGTH_LONG).show();
                        return;
                    }
                    if ( dateMax != null && dateReFormatStr.compareTo(dateMax) > 0 ) {
                        Toast.makeText(mWXSDKInstance.getContext(), "所选日期不大于"+dateMax, Toast.LENGTH_LONG ).show();
                        return;
                    }

                    dialog.dismiss();
                    Map<String, Object> map = new HashMap<String,Object>();
                    map.put("value", sf.parse( dateStr).getTime());
                    WXSDKManager.getInstance().callback(mWXSDKInstance.getInstanceId(), callbackId, map);
                } catch (ParseException e) {
                }
            }
        });
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setContentView(picker, params);
        dialog.getWindow().setGravity(Gravity.CENTER);
    }
}
