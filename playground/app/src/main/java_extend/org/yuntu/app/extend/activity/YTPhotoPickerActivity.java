package org.yuntu.app.extend.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import me.iwf.photopicker.entity.Photo;
import me.iwf.photopicker.event.OnItemCheckListener;
import me.iwf.photopicker.fragment.ImagePagerFragment;
import me.iwf.photopicker.fragment.PhotoPickerFragment;

/**
 * Created by cymin on 16/10/10.
 */
public class YTPhotoPickerActivity extends AppCompatActivity {

    private PhotoPickerFragment pickerFragment;
    private ImagePagerFragment imagePagerFragment;
    private MenuItem menuDoneItem;
    private int maxCount = 9;
    private boolean menuIsInflated = false;
    private boolean showGif = false;
    private int columnNumber = 3;
    private ArrayList<String> originalPhotos = null;

    public YTPhotoPickerActivity() {
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean showCamera = this.getIntent().getBooleanExtra("SHOW_CAMERA", true);
        boolean showGif = this.getIntent().getBooleanExtra("SHOW_GIF", false);
        boolean previewEnabled = this.getIntent().getBooleanExtra("PREVIEW_ENABLED", true);
        this.setShowGif(showGif);
        this.setContentView(me.iwf.photopicker.R.layout.__picker_activity_photo_picker);
        Toolbar mToolbar = (Toolbar)this.findViewById(me.iwf.photopicker.R.id.toolbar);
        this.setSupportActionBar(mToolbar);
        this.setTitle(me.iwf.photopicker.R.string.__picker_title);
        ActionBar actionBar = this.getSupportActionBar();

        assert actionBar != null;

        actionBar.setDisplayHomeAsUpEnabled(true);
        if(Build.VERSION.SDK_INT >= 21) {
            actionBar.setElevation(25.0F);
        }

        this.maxCount = this.getIntent().getIntExtra("MAX_COUNT", 9);
        this.columnNumber = this.getIntent().getIntExtra("column", 3);
        this.originalPhotos = this.getIntent().getStringArrayListExtra("ORIGINAL_PHOTOS");
        this.pickerFragment = (PhotoPickerFragment)this.getSupportFragmentManager().findFragmentByTag("tag");
        if(this.pickerFragment == null) {
            this.pickerFragment = PhotoPickerFragment.newInstance(showCamera, showGif, previewEnabled, this.columnNumber, this.maxCount, this.originalPhotos);
            this.getSupportFragmentManager().beginTransaction().replace(me.iwf.photopicker.R.id.container, this.pickerFragment, "tag").commit();
            this.getSupportFragmentManager().executePendingTransactions();
        }

        this.pickerFragment.getPhotoGridAdapter().setOnItemCheckListener(new OnItemCheckListener() {
            public boolean OnItemCheck(int position, Photo photo, boolean isCheck, int selectedItemCount) {
                int total = selectedItemCount + (isCheck?-1:1);
                YTPhotoPickerActivity.this.menuDoneItem.setEnabled(total > 0);
                if(YTPhotoPickerActivity.this.maxCount <= 1) {
                    List photos = YTPhotoPickerActivity.this.pickerFragment.getPhotoGridAdapter().getSelectedPhotos();
                    if(!photos.contains(photo)) {
                        photos.clear();
                        YTPhotoPickerActivity.this.pickerFragment.getPhotoGridAdapter().notifyDataSetChanged();
                    }

                    return true;
                } else if(total > YTPhotoPickerActivity.this.maxCount) {
                    Toast.makeText(YTPhotoPickerActivity.this.getActivity(), YTPhotoPickerActivity.this.getString(me.iwf.photopicker.R.string.__picker_over_max_count_tips, new Object[]{Integer.valueOf(YTPhotoPickerActivity.this.maxCount)}), Toast.LENGTH_LONG).show();
                    return false;
                } else {
                    YTPhotoPickerActivity.this.menuDoneItem.setTitle(YTPhotoPickerActivity.this.getString(me.iwf.photopicker.R.string.__picker_done_with_count, new Object[]{Integer.valueOf(total), Integer.valueOf(YTPhotoPickerActivity.this.maxCount)}));
                    return true;
                }
            }
        });
    }

    public void onBackPressed() {
        if(this.imagePagerFragment != null && this.imagePagerFragment.isVisible()) {
            this.imagePagerFragment.runExitAnimation(new Runnable() {
                public void run() {
                    if(YTPhotoPickerActivity.this.getSupportFragmentManager().getBackStackEntryCount() > 0) {
                        YTPhotoPickerActivity.this.getSupportFragmentManager().popBackStack();
                    }

                }
            });
        } else {
            super.onBackPressed();
        }

    }

    public void addImagePagerFragment(ImagePagerFragment imagePagerFragment) {
        this.imagePagerFragment = imagePagerFragment;
        this.getSupportFragmentManager().beginTransaction().replace(me.iwf.photopicker.R.id.container, this.imagePagerFragment).addToBackStack((String)null).commit();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        if(this.menuIsInflated) {
            return false;
        } else {
            this.getMenuInflater().inflate(me.iwf.photopicker.R.menu.__picker_menu_picker, menu);
            this.menuDoneItem = menu.findItem(me.iwf.photopicker.R.id.done);
            if(this.originalPhotos != null && this.originalPhotos.size() > 0) {
                this.menuDoneItem.setEnabled(true);
                this.menuDoneItem.setTitle(this.getString(me.iwf.photopicker.R.string.__picker_done_with_count, new Object[]{Integer.valueOf(this.originalPhotos.size()), Integer.valueOf(this.maxCount)}));
            } else {
                this.menuDoneItem.setEnabled(false);
            }

            this.menuIsInflated = true;
            return true;
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == 16908332) {
//            Toast.makeText(YTPhotoPickerActivity.this.getActivity(), "dfdfdfdfdd", Toast.LENGTH_LONG);
//            Log.i("oo---->","dfdfdfdfdfdfd");
            super.onBackPressed();
            return true;
        } else if(item.getItemId() == me.iwf.photopicker.R.id.done) {
            Toast.makeText(YTPhotoPickerActivity.this.getActivity(), "wancheng", Toast.LENGTH_LONG);
            Intent intent = new Intent();
            ArrayList selectedPhotos = this.pickerFragment.getPhotoGridAdapter().getSelectedPhotoPaths();
            intent.putStringArrayListExtra("SELECTED_PHOTOS", selectedPhotos);
            this.setResult(-1, intent);
            this.finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    public YTPhotoPickerActivity getActivity() {
        return this;
    }

    public boolean isShowGif() {
        return this.showGif;
    }

    public void setShowGif(boolean showGif) {
        this.showGif = showGif;
    }
}
