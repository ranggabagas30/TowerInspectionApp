//package com.sap.inspection;
//
//
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.IOException;
//
//import com.sap.inspection.view.CameraPreview;
//
//import android.app.Activity;
//import android.graphics.Bitmap;
//import android.graphics.Bitmap.CompressFormat;
//import android.graphics.BitmapFactory;
//import android.hardware.Camera;
//import android.hardware.Camera.PictureCallback;
//import android.hardware.Camera.ShutterCallback;
//import android.os.Bundle;
//import android.os.Environment;
//import android.util.Log;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.widget.Button;
//import android.widget.FrameLayout;
//
//public class CameraActivity extends Activity  {
//	private static final String TAG = "CameraDemo";
////	Camera camera;
//	CameraPreview preview;
//	Button buttonClick;
//
//	/** Called when the activity is first created. */
//	@Override
//	public void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		setContentView(R.layout.activity_main);
//
//		preview = new CameraPreview(this);
//		((FrameLayout) findViewById(R.id.preview)).addView(preview);
//
//		buttonClick = (Button) findViewById(R.id.buttonClick);
//		buttonClick.setOnClickListener(new OnClickListener() {
//			public void onClick(View v) {
//				preview.camera.takePicture(shutterCallback, rawCallback,jpegCallback);
//			}
//		});
//
//		Log.d(TAG, "onCreate'd");
//	}
//	
////	protected void onPause() {
////		super.onPause();
////	};
//
//	ShutterCallback shutterCallback = new ShutterCallback() {
//		public void onShutter() {
//			Log.d(TAG, "onShutter'd");
//		}
//	};
//
//	/** Handles data for raw picture */
//	PictureCallback rawCallback = new PictureCallback() {
//		public void onPictureTaken(byte[] data, Camera camera) {
//			Log.d(TAG, "onPictureTaken - raw");
//		}
//	};
//	
//	/** Handles data for jpeg picture */
//	PictureCallback jpegCallback = new PictureCallback() {
//		public void onPictureTaken(byte[] data, Camera camera) {
//			FileOutputStream outStream = null;
//			try {
//				String root = Environment.getExternalStorageDirectory().toString();
//				outStream = new FileOutputStream(String.format(
//						root+"/%d.jpg", System.currentTimeMillis()));
////				outStream.write(data);
////				outStream.close();
//				
//				Bitmap bitmap = BitmapFactory.decodeByteArray(data , 0, data.length);
//				bitmap.compress(CompressFormat.JPEG, 20, outStream);
//
//			} catch (FileNotFoundException e) {
//				e.printStackTrace();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//
//		}
//	};
//
//}