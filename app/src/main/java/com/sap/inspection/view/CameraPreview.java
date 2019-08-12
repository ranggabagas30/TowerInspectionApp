package com.sap.inspection.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

@SuppressLint("NewApi")
public class CameraPreview  extends SurfaceView implements SurfaceHolder.Callback {
	private static final String TAG = "Preview";

	public SurfaceHolder mHolder;
	public Camera camera;

	public CameraPreview(Context context) {
		super(context);

		// Install a SurfaceHolder.Callback so we get notified when the
		// underlying surface is created and destroyed.
		mHolder = getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	public void surfaceCreated(SurfaceHolder holder) {
		// The Surface has been created, acquire the camera and tell it where
		// to draw.
		camera = Camera.open(1);
		try {
			camera.setPreviewDisplay(holder);

			camera.setPreviewCallback(new PreviewCallback() {

				public void onPreviewFrame(byte[] data, Camera arg1) {
					//                                                           
					CameraPreview.this.invalidate();
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		// Surface will be destroyed when we return, so stop the preview.
		// Because the CameraDevice object is not a shared resource, it's very
		// important to release it when the activity is paused.
		camera.stopPreview();
		camera.release();
		camera = null;
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		// Now that the size is known, set up the camera parameters and begin
		// the preview.
//		Camera.Parameters parameters = camera.getParameters();
//		parameters.setPreviewSize(w, h);
//		camera.setParameters(parameters);
//		camera.startPreview();
		
//		Camera.Parameters parameters = camera.getParameters();
//	    List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
//	    Log.d(getClass().getName(), "size : "+previewSizes.size());
//	    for (Camera.Size size : previewSizes) {
//	    	Log.d(getClass().getName(), "w : "+size.width+" h : "+size.height);
//	    }

	    // You need to choose the most appropriate previewSize for your app
//	    Camera.Size previewSize = previewSizes.get(0); // .... select one of previewSizes here
//	    parameters.setPreviewSize(previewSize.width, previewSize.height);
//	    parameters.setPreviewSize(480, 480);
//	    parameters.setPreviewSize(1280,720);
//	    camera.setParameters(parameters);
		camera.startPreview();
	}

}