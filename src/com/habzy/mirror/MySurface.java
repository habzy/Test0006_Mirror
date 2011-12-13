package com.habzy.mirror;

import java.io.IOException;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

public class MySurface extends SurfaceView {

	protected static final String TAG = "MySurface";
	Camera mCamera;
	private SurfaceHolder holder;

	public MySurface(Context context, AttributeSet attrs) {
		super(context, attrs);		
		holder = getHolder();
        holder.addCallback(mCallback);    
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	private Callback mCallback = new Callback() {

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			Log.d(TAG, "surfaceCreated");
			if (mCamera == null) {
				int defaultCameraId = 1;
				int numberOfCameras = Camera.getNumberOfCameras();
				 CameraInfo cameraInfo = new CameraInfo();
		            for (int i = 0; i < numberOfCameras; i++) {
		                Camera.getCameraInfo(i, cameraInfo);
		                if (cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT) {
		                    defaultCameraId = i;
		                }
		            }
				
				mCamera = Camera.open(defaultCameraId);
				try {
					mCamera.setPreviewDisplay(holder);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				try {
					mCamera.setDisplayOrientation(90);
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
//			Log.d(TAG, "surfaceChanged");
//			Camera.Parameters params = mCamera.getParameters();
//			params.setPictureFormat(PixelFormat.JPEG);
//			params.setPreviewSize(640, 480);
//			mCamera.setParameters(params);
			mCamera.startPreview();
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			Log.d(TAG, "surfaceChanged");
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
		}
	};
	
	

}
