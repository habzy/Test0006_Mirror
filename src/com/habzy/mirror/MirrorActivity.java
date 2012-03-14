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

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

public class MirrorActivity extends Activity
{
    
    private static final String TAG = "MirrorActivity";
    
    private int mDefaulLeft;
    
    private int mDefaulTop = 100;
    
    private int mDisPlayWidth;
    
    private int mDisPlayHeight;
    
    private Camera mCamera;
    
    private Size mCameraSize;
    
    private RelativeLayout mPreviewLayout;
    
    private MySurface mSurface;
    
    private LayoutParams layoutParams;
    
    private RelativeLayout mPreviewParent;
    
    /**
     * Sensor ...
     */
    private SensorManager mSensorMgr;
    
    private List<Sensor> mLightSensors;
    
    private boolean mHasLightSensors = false;
    
    private boolean mHasRegesteredSensor = false;
    
    private float mLightIntensity;
    
    private boolean mIsAutoBrightness = false;
    
    private int mDefaultBrightness;
    
    private final SensorEventListener mSensorEventListener = new SensorEventListener()
    {
        
        @Override
        public void onAccuracyChanged(Sensor sensor, int arg1)
        {
            // TODO Auto-generated method stub
            
        }
        
        @Override
        public void onSensorChanged(SensorEvent event)
        {
            if (mLightIntensity != event.values[0])
            {
                mLightIntensity = event.values[0];
                Log.d(TAG, "Now, the light intensity is:" + mLightIntensity);
                
                changePreviewProperty();
                mPreviewParent.updateViewLayout(mPreviewLayout, layoutParams);
            }
        }
        
    };
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        sizeInit();
        cameraInit();
        calculatePreviewSize();
        
        mPreviewParent = (RelativeLayout) findViewById(R.id.preview_parent);
        addPreviewFromXml();
        
        mHasLightSensors = getLightSensors();
    }
    
    private void sizeInit()
    {
        Display display = getWindowManager().getDefaultDisplay();
        mDisPlayWidth = display.getWidth();
        mDisPlayHeight = display.getHeight();
        Log.d(TAG, "height:" + mDisPlayHeight + ";width:" + mDisPlayWidth);
    }
    
    private void cameraInit()
    {
        List<Size> supportedSizes = null;
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
            
            supportedSizes = mCamera.getParameters().getSupportedPreviewSizes();
        }
        if (null != supportedSizes)
        {
            mCameraSize = supportedSizes.get(supportedSizes.size() - 1);
            Log.d(TAG, "Camera Sizes:" + mCameraSize);
        }
        
    }
    
    private void calculatePreviewSize()
    {
        float screenScale = mDisPlayWidth / mDisPlayHeight;
        float cameraScale = mCameraSize.width / mCameraSize.height;
        if (screenScale <= cameraScale)
        {
            mDefaulLeft = 0;
            mDefaulTop = (int) (mDisPlayHeight - mDisPlayWidth / cameraScale) / 2;
        }
        else
        {
            mDefaulTop = 0;
            mDefaulLeft = (int) (mDisPlayWidth - mDisPlayHeight * cameraScale) / 2;
        }
        
    }
    
    /**
     * 
     */
    private boolean getLightSensors()
    {
        mSensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
        mLightSensors = mSensorMgr.getSensorList(Sensor.TYPE_LIGHT);
        boolean hasSensors = !mLightSensors.isEmpty();
        Log.d(TAG, "This device has light sensors?=" + hasSensors);
        
        return hasSensors;
    }
    
    @Override
    protected void onResume()
    {
        mIsAutoBrightness = ScreenBrightAutoControler.isAutoBrightness(this);
        if (mIsAutoBrightness)
        {
            ScreenBrightAutoControler.stopAutoBrightness(this);
        }
        else
        {
            mDefaultBrightness = ScreenBrightAutoControler.getScreenBrightness(this);
        }
        ScreenBrightAutoControler.setBrightness(this, 255);
        if (mHasLightSensors && !mHasRegesteredSensor)
        {
            mHasRegesteredSensor = mSensorMgr.registerListener(mSensorEventListener,
                    mLightSensors.get(0),
                    SensorManager.SENSOR_DELAY_FASTEST);
        }
        super.onResume();
    }
    
    @Override
    protected void onPause()
    {
        if (mHasRegesteredSensor)
        {
            mSensorMgr.unregisterListener(mSensorEventListener);
            mHasRegesteredSensor = false;
        }
        if (mIsAutoBrightness)
        {
            ScreenBrightAutoControler.startAutoBrightness(this);
        }
        else
        {
            ScreenBrightAutoControler.setBrightness(this, mDefaultBrightness);
        }
        super.onPause();
    }
    
    private void addPreviewFromXml()
    {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mPreviewLayout = (RelativeLayout) inflater.inflate(R.layout.preview_layout,
                null);
        mSurface = (MySurface) mPreviewLayout.findViewById(R.id.Surface);
        mSurface.setCamera(mCamera);
        
        layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        layoutParams.leftMargin = mDefaulLeft;
        layoutParams.rightMargin = mDefaulLeft / 2;
        layoutParams.topMargin = mDefaulTop;
        layoutParams.bottomMargin = mDefaulTop;
        
        mPreviewParent.addView(mPreviewLayout, layoutParams);
    }
    
    private void changePreviewProperty()
    {
        if (mLightIntensity < 100)
        {
            layoutParams.leftMargin = mDefaulLeft
                    + (mDisPlayWidth - 2 * mDefaulLeft) / 4;
            layoutParams.rightMargin = layoutParams.leftMargin / 2;
            layoutParams.topMargin = mDefaulTop
                    + (mDisPlayHeight - 2 * mDefaulTop) / 4;
            layoutParams.bottomMargin = layoutParams.topMargin;
        }
        else if (mLightIntensity < 800)
        {
            layoutParams.leftMargin = mDefaulLeft
                    + (mDisPlayWidth - 2 * mDefaulLeft) / 8;
            layoutParams.rightMargin = layoutParams.leftMargin / 2;
            layoutParams.topMargin = mDefaulTop
                    + (mDisPlayHeight - 2 * mDefaulTop) / 8;
            layoutParams.bottomMargin = layoutParams.topMargin;
        }
        else
        {
            layoutParams.leftMargin = mDefaulLeft;
            layoutParams.rightMargin = mDefaulLeft / 2;
            layoutParams.topMargin = mDefaulTop;
            layoutParams.bottomMargin = mDefaulTop;
        }
        
    }
    
}