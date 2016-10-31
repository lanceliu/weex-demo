package org.yuntu.app.extend.activity.clippicture;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import org.yuntu.app.R;

public class PreviewActivity extends Activity {
	ImageView preview;
	public static Bitmap bitmap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_preview);

		Log.i("hello", bitmap.getWidth()+"x"+bitmap.getHeight());
		preview = (ImageView) this.findViewById(R.id.preview);
		if (bitmap != null) {
			preview.setImageBitmap(bitmap);
		}
	}

}
