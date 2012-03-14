/*
 * Copyright (C) 2010 Habzy Huang
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

public class MySurface extends SurfaceView
{
    
    protected static final String TAG = "MySurface";
    
    private Camera mCamera;
    
    private SurfaceHolder holder;
    
    public MySurface(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        holder = getHolder();
        holder.addCallback(mCallback);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }
    
    /**
     * Need to call this method before initialize the view.
     * 
     * @param camera
     */
    public void setCamera(Camera camera)
    {
        mCamera = camera;
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
    
    private Callback mCallback = new Callback()
    {
        
        @Override
        public void surfaceCreated(SurfaceHolder holder)
        {
            Log.d(TAG, "surfaceCreated");
            if (mCamera == null)
            {
                int defaultCameraId = 1;
                int numberOfCameras = Camera.getNumberOfCameras();
                CameraInfo cameraInfo = new CameraInfo();
                for (int i = 0; i < numberOfCameras; i++)
                {
                    Camera.getCameraInfo(i, cameraInfo);
                    if (cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT)
                    {
                        defaultCameraId = i;
                    }
                }
                mCamera = Camera.open(defaultCameraId);
            }
            
            try
            {
                mCamera.setPreviewDisplay(holder);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            
            try
            {
                mCamera.setDisplayOrientation(90);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        
        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width,
                int height)
        {
            mCamera.startPreview();
        }
        
        @Override
        public void surfaceDestroyed(SurfaceHolder holder)
        {
            Log.d(TAG, "surfaceChanged");
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    };
    
}
