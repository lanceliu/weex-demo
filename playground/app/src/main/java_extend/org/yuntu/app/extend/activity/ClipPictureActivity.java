package org.yuntu.app.extend.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.taobao.weex.WXSDKManager;

import org.yuntu.app.R;
import org.yuntu.app.extend.activity.clippicture.ClipView;
import org.yuntu.app.extend.activity.clippicture.PreviewActivity;
import org.yuntu.app.extend.activity.clippicture.YTPhotoPicker;
import org.yuntu.app.util.BitmapUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import me.iwf.photopicker.PhotoPicker;
import me.iwf.photopicker.PhotoPreview;

public class ClipPictureActivity extends Activity implements OnTouchListener,
        OnClickListener {
    private ImageView srcPic;
    private View sure;
    private View choose;
    private ClipView clipview;

    private Matrix matrix = new Matrix();
    private Matrix savedMatrix = new Matrix();

    /**
     * 动作标志：无
     */
    private static final int NONE = 0;
    /**
     * 动作标志：拖动
     */
    private static final int DRAG = 1;
    /**
     * 动作标志：缩放
     */
    private static final int ZOOM = 2;
    /**
     * 初始化动作标志
     */
    private int mode = NONE;

    /**
     * 记录起始坐标
     */
    private PointF start = new PointF();
    /**
     * 记录缩放时两指中间点坐标
     */
    private PointF mid = new PointF();
    private float oldDist = 1f;

    private Bitmap bitmap;
    private String callbackId;
    private String instanceId;
    private RelativeLayout mTtitleLayout;
    private ProgressBar mProgressBar;
    private boolean isFinish = false;
    private String fileName = "";

    private String fileParam;

    private boolean needCut;

    private String uploadUrl;

    private Set<String> headSet;

    private JSONObject headJson;

    private Set<String> formSet;

    private JSONObject formJson;


    public ClipPictureActivity() {

    }

    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    mProgressBar.setVisibility(View.INVISIBLE);
                    getActivity().finish();
                    WXSDKManager.getInstance().callback(instanceId, callbackId, (Map<String, Object>) msg.obj);
                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clip_picture);

        srcPic = (ImageView) this.findViewById(R.id.src_pic);
        srcPic.setOnTouchListener(this);

        ViewTreeObserver observer = srcPic.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

            @SuppressWarnings("deprecation")
            public void onGlobalLayout() {
                srcPic.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });

        mProgressBar = (ProgressBar)findViewById(R.id.pdf_progressBar);
        mTtitleLayout = (RelativeLayout)findViewById(R.id.title_barLayout);
        sure = (View) this.findViewById(R.id.sure);
        sure.setOnClickListener(this);
        choose = (View) this.findViewById(R.id.choose);
        choose.setOnClickListener(this);

        initIntentData(getIntent().getStringExtra("parms"));
        callbackId = getIntent().getStringExtra("callbackId");
        instanceId = getIntent().getStringExtra("instanceId");

        YTPhotoPicker.builder().setPreviewEnabled(false).setPhotoCount(1).start(this);
    }


    /**
     * 初始化数据
     *
     * @param jsonStr
     */
    private void initIntentData(String jsonStr) {
        JSONObject json = JSON.parseObject(jsonStr);

        fileParam = json.getString("fileParam");
        needCut = json.getBoolean("needCut");
        uploadUrl = json.getString("uploadUrl");
        headSet = json.getJSONObject("header").keySet();
        headJson = json.getJSONObject("header");
        formSet = json.getJSONObject("form") == null ? null : json.getJSONObject("form").keySet();
        formJson = json.getJSONObject("form");
    }

    /**
     * 初始化截图区域，并将源图按裁剪框比例缩放
     *
     * @param top
     */
    private void initClipView(int top) {

        bitmap = ((BitmapDrawable) srcPic.getDrawable()).getBitmap();
        srcPic.setImageBitmap(bitmap);

        if (!needCut) {
            return;
        }

        clipview = new ClipView(ClipPictureActivity.this);
        clipview.setCustomTopBarHeight(top);

        clipview.removeOnDrawCompleteListener();

        clipview.addOnDrawCompleteListener(new ClipView.OnDrawListenerComplete() {

            public void onDrawCompelete() {

                int clipHeight = clipview.getClipHeight();
                int clipWidth = clipview.getClipWidth();

                Log.d("oo==","clipHeight=="+clipHeight+"==clipWidth="+clipWidth);
                int midX = clipview.getClipLeftMargin() + (clipWidth / 2);
                int midY = clipview.getClipTopMargin() + (clipHeight / 2);

                int imageWidth = bitmap.getWidth();
                int imageHeight = bitmap.getHeight();
                // 按裁剪框求缩放比例
                float scale = (clipWidth * 1.0f) / imageWidth;
                if (imageWidth > imageHeight) {
                    scale = (clipHeight * 1.0f) / imageHeight;
                }

                // 起始中心点
                float imageMidX = imageWidth * scale / 2;
                float imageMidY = clipview.getCustomTopBarHeight()
                        + imageHeight * scale / 2;
                srcPic.setScaleType(ScaleType.MATRIX);

                // 缩放
//                matrix.postScale(scale, scale);
                // 平移
//                matrix.postTranslate(midX - imageMidX, midY - imageMidY);

                srcPic.setImageMatrix(matrix);
                //srcPic.setImageBitmap(bitmap);
            }
        });

        this.addContentView(clipview, new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    }

    public boolean onTouch(View v, MotionEvent event) {
        ImageView view = (ImageView) v;
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                savedMatrix.set(matrix);
                // 设置开始点位置
                start.set(event.getX(), event.getY());
                mode = DRAG;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = spacing(event);
                if (oldDist > 10f) {
                    savedMatrix.set(matrix);
                    midPoint(mid, event);
                    mode = ZOOM;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                break;
            case MotionEvent.ACTION_MOVE:
                if (mode == DRAG) {
                    matrix.set(savedMatrix);
                    matrix.postTranslate(event.getX() - start.x, event.getY()
                            - start.y);
                } else if (mode == ZOOM) {
                    float newDist = spacing(event);
                    if (newDist > 10f) {
                        matrix.set(savedMatrix);
                        float scale = newDist / oldDist;
                        matrix.postScale(scale, scale, mid.x, mid.y);
                    }
                }
                break;
        }
        view.setImageMatrix(matrix);
        return true;
    }

    /**
     * 多点触控时，计算最先放下的两指距离
     *
     * @param event
     * @return
     */
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    /**
     * 多点触控时，计算最先放下的两指中心坐标
     *
     * @param point
     * @param event
     */
    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    public void onClick(View v) {
        if (v.getId() == R.id.sure) {
            mProgressBar.setVisibility(View.VISIBLE);
            Bitmap clipBitmap = BitmapUtils.imageZoom(getBitmap());
            saveBitmp(clipBitmap);

            upLoadFile(Bitmap2Bytes(clipBitmap));
        } else {
            isFinish = true;
            YTPhotoPicker.builder().setPreviewEnabled(false).setPhotoCount(1).start(this);
        }
    }

    private void saveBitmp(Bitmap bm){
        File imageFile = new File("sdcard/temp.png");
        if (imageFile.exists()){
            imageFile.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(imageFile);
            bm.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private static final String IMGUR_CLIENT_ID = "9199fdef135c122";
    private static final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/jpg");

    private final OkHttpClient client = new OkHttpClient();

    /**
     * 上传文件
     */
    public void upLoadFile(final byte[] file) {


        new Thread() {
            public void run() {
                MultipartBuilder  multipartBuilder = new MultipartBuilder()
                                                        .type(MultipartBuilder.FORM)
                                                        .addFormDataPart(fileParam, fileName,
                                                            RequestBody.create(MEDIA_TYPE_PNG, file));
                if (formSet != null) {
                    for (String item : formSet) {
                        multipartBuilder.addFormDataPart(item, formJson.getString(item));
                    }
                }

                RequestBody requestBody = multipartBuilder.build();

                Request.Builder builder = new Request.Builder();
                for (String item : headSet) {
                    builder.header(item, headJson.getString(item));
                }

                Request request = builder
                        .url(uploadUrl)
                        .post(requestBody)
                        .build();

                Response response = null;
                try {
                    response = client.newCall(request).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (!response.isSuccessful()) try {
                    throw new IOException("Unexpected code " + response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Map<String, Object> map = new HashMap<>();
                try {
                    JSONObject jsonObject = (JSONObject) JSONObject.parse(response.body().string());
                    map.put("data", jsonObject.getJSONObject("data"));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Message message = new Message();
                message.what = 1;
                message.obj = map;
                handler.sendMessage(message);
            }
        }.start();
    }

    public byte[] Bitmap2Bytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    /**
     * bitmap转为base64
     *
     * @param bitmap
     * @return
     */
    public static String bitmapToBase64(Bitmap bitmap) {

        String result = null;
        ByteArrayOutputStream baos = null;
        try {
            if (bitmap != null) {
                baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

                baos.flush();
                baos.close();

                byte[] bitmapBytes = baos.toByteArray();
                result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.flush();
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK &&
                (requestCode == YTPhotoPicker.REQUEST_CODE || requestCode == PhotoPreview.REQUEST_CODE)) {
            this.isFinish = true;
            List<String> photos = null;
            if (data != null) {
                photos = data.getStringArrayListExtra(YTPhotoPicker.KEY_SELECTED_PHOTOS);
                if (photos != null && photos.size() > 0) {
                    srcPic.setImageURI(Uri.parse(photos.get(0)));
                    Log.i("oo==", Uri.parse(photos.get(0)).toString());
                    fileName = Uri.parse(photos.get(0)).toString().substring(Uri.parse(photos.get(0)).toString().lastIndexOf("/") + 1);
                    initClipView(srcPic.getTop()== 0 ? 148 : srcPic.getTop());
                }
            }
            Log.i("hello", photos.toString());
        } else {
            if (!isFinish) {
                this.finish();
            }

        }
    }

    /**
     * 获取裁剪框内截图
     *
     * @return
     */
    private Bitmap getBitmap() {
        Bitmap finalBitmap = null;
        // 获取截屏
        View view = this.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();

        // 获取状态栏高度
        Rect frame = new Rect();
        this.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        int statusBarHeight = frame.top;

        if (needCut) {
            finalBitmap = Bitmap.createBitmap(view.getDrawingCache(),
                    clipview.getClipLeftMargin(), clipview.getClipTopMargin()
                            + statusBarHeight, clipview.getClipWidth(),
                    clipview.getClipHeight());
        } else {
            BitmapDrawable mDrawable =  (BitmapDrawable) srcPic.getDrawable();
            finalBitmap = Bitmap.createBitmap(mDrawable.getBitmap());
        }
        // 释放资源
        view.destroyDrawingCache();
        return finalBitmap;
    }

    public ClipPictureActivity getActivity() {
        return this;
    }
}