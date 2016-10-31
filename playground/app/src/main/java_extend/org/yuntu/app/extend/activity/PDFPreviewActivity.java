package org.yuntu.app.extend.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.github.barteksc.pdfviewer.PDFView;
import com.taobao.weex.adapter.DefaultWXHttpAdapter;
import com.taobao.weex.adapter.IWXHttpAdapter;
import com.taobao.weex.common.WXRequest;
import com.taobao.weex.common.WXResponse;
import org.yuntu.app.R;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;


/**
 * Created by liufei on 16/8/17.
 */
public class PDFPreviewActivity extends AppCompatActivity {

    private static final int STORAGE_PERMISSION_REQUEST_CODE = 0x1;

   private PDFView pdfView;

   private ProgressBar pdfProgressBar;

   private ImageView returnButton;

   private TextView textViewTitle;

    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    pdfProgressBar.setVisibility(View.INVISIBLE);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_preview);

        pdfView = (PDFView) findViewById(R.id.pdfView);
        pdfProgressBar = (ProgressBar)findViewById(R.id.pdf_progressBar);
        returnButton = (ImageView) findViewById(R.id.button_backward);
        textViewTitle = (TextView) findViewById(R.id.title_pdf);
        textViewTitle.setText(getIntent().getStringExtra("title"));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(this, "please give me the permission", Toast.LENGTH_SHORT).show();

            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_REQUEST_CODE);
            }
        } else {
            createfile();
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_backward:
                finish();
            break;
            default:
                break;
            }
    }

    private void createfile() {

        new Thread(new Runnable() {
            @Override
            public void run() {

                File path = Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_DOWNLOADS );
                final File file = new File(path, UUID.randomUUID().toString()+".pdf");
                try {
                    file.createNewFile();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

                DefaultWXHttpAdapter httpAdapter = new DefaultWXHttpAdapter();
                WXRequest wxRequest = new WXRequest();
                wxRequest.method = "GET";
                wxRequest.url = getIntent().getStringExtra("url");

                httpAdapter.sendRequest(wxRequest, new IWXHttpAdapter.OnHttpListener() {
                    @Override
                    public void onHttpStart() {
                        Log.v("down PDF", "start");
                    }

                    @Override
                    public void onHeadersReceived(int statusCode, Map<String, List<String>> headers) {

                    }

                    @Override
                    public void onHttpUploadProgress(int uploadProgress) {

                    }

                    @Override
                    public void onHttpResponseProgress(int loadedLength) {

                    }

                    @Override
                    public void onHttpFinish(WXResponse response) {
                        try {
                            FileOutputStream outputStream = new FileOutputStream(file);
                            outputStream.write(response.originalData);
                            pdfView.fromFile(file).defaultPage(0).load();

                            Message message = Message.obtain();
                            message.what = 1;
                            handler.sendMessage(message);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                });
            }
        }).start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            createfile();
        } else {
            Toast.makeText(this, "request storage permission fail!", Toast.LENGTH_SHORT).show();
        }
    }

}
