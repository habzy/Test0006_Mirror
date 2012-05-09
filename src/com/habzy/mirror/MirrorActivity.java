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
import android.hardware.Camera.Parameters;
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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

public class MirrorActivity extends Activity implements OnClickListener
{
    
    private static final String TAG = "MirrorActivity";
    
    private int mDisPlayWidth;
    
    private int mDisPlayHeight;
    
    private int mDefaulLeft;
    
    private int mDefaulTop = 100;
    
    private Camera mCamera;
    
    private Size mCameraSize;
    
    private RelativeLayout mPreviewParent;
    
    private RelativeLayout mPreviewLayout;
    
    private MySurface mSurface;
    
    private LayoutParams layoutParams;
    
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
    
    /**
     * Buttons
     */
    
//    private Button mMirrorBt;
//    
//    private Button mLightBt;
    
    private boolean mIsMirrorOn = true;
    
    private boolean mIsLightOn = false;
    
    private final SensorEventListener mSensorEventListener = new SensorEventListener()
    {
        
        @Override
        public void onAccuracyChanged(Sensor sensor, int arg1)
        {
            // Do nothing.
            
        }
        
        @Override
        public void onSensorChanged(SensorEvent event)
        {
            if (mLightIntensity != event.values[0])
            {
                mLightIntensity = event.values[0];
//                Log.d(TAG, "Now, the light intensity is:" + mLightIntensity);
                if (mIsMirrorOn)
                {
                    changePreviewProperty();
                    mPreviewParent.updateViewLayout(mPreviewLayout,
                            layoutParams);
                }
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
        mPreviewParent.setKeepScreenOn(true);
        addPreviewFromXml();
        
        mHasLightSensors = getLightSensors();
        
//        initButton();
    }
    
    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.mirror:
            {
                if (mIsMirrorOn)
                {
                    
                }
                else
                {
                    
                }
                mIsMirrorOn = !mIsMirrorOn;
                break;
            }
            case R.id.light:
            {
                Log.d(TAG, "Light:" + !mIsLightOn);
                Parameters parameters = mCamera.getParameters();
                if (!mIsLightOn)
                {
                    // parameters.setFlashMode(Parameters.FLASH_MODE_ON);
                    parameters.setFlashMode(Parameters.FLASH_MODE_TORCH);
                }
                else
                {
                    parameters.setFlashMode(Parameters.FLASH_MODE_OFF);
                }
                mCamera.setParameters(parameters);
                mIsLightOn = !mIsLightOn;
                break;
            }
        }
        
    }
    
    @Override
    protected void onResume()
    {
        mIsAutoBrightness = ScreenBrightAutoControler.isAutoBrightness(this);
        if (!mIsAutoBrightness)
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
        
        if (!mIsAutoBrightness)
        {
            ScreenBrightAutoControler.setBrightness(this, mDefaultBrightness);
        }
        super.onPause();
    }
    
    @Override
    protected void onDestroy()
    {
        mCamera = null;
        super.onDestroy();
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
            // mCamera = Camera.open();
            
            supportedSizes = mCamera.getParameters().getSupportedPreviewSizes();
        }
        if (null != supportedSizes)
        {
            mCameraSize = supportedSizes.get(0);
            Log.d(TAG, "Camera Sizes:" + mCameraSize.width + " & "
                    + mCameraSize.height);
        }
        
    }
    
    private void calculatePreviewSize()
    {
        float screenScale = mDisPlayWidth / mDisPlayHeight;
        float cameraScale = mCameraSize.width / mCameraSize.height;
        if (screenScale <= cameraScale)
        {
            mDefaulTop = (int) (mDisPlayHeight - (mDisPlayWidth / cameraScale)) / 4;
            mDefaulLeft = 0;
        }
        else
        {
            mDefaulTop = 0;
            mDefaulLeft = (int) (mDisPlayWidth - (mDisPlayHeight * cameraScale)) / 2;
        }
        
        Log.d(TAG, "Calculate top and left:" + mDefaulTop + " & " + mDefaulLeft);
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
        
        // mPreviewLayout.setVisibility(View.INVISIBLE);
        // mPreviewParent.setBackgroundColor(Color.BLACK);
    }
    
//    private void initButton()
//    {
//        mMirrorBt = (Button) findViewById(R.id.mirror);
//        mLightBt = (Button) findViewById(R.id.light);
//        
//        mMirrorBt.setOnClickListener(this);
//        mLightBt.setOnClickListener(this);
//        
//    }
    
    private void changePreviewProperty()
    {
        Camera.Parameters param = null;
        if(null != mCamera)
        {
            param = mCamera.getParameters();
        }else
        {
            return;
        }
        if (mLightIntensity < 100)
        {
            layoutParams.leftMargin = mDefaulLeft
                    + (mDisPlayWidth - 2 * mDefaulLeft) / 4;
            layoutParams.rightMargin = layoutParams.leftMargin / 2;
            layoutParams.topMargin = mDefaulTop
                    + (mDisPlayHeight - 2 * mDefaulTop) / 4;
            layoutParams.bottomMargin = layoutParams.topMargin;
            
            param.setExposureCompensation(param.getMaxExposureCompensation());
        }
        else if (mLightIntensity < 800)
        {
            layoutParams.leftMargin = mDefaulLeft
                    + (mDisPlayWidth - 2 * mDefaulLeft) / 8;
            layoutParams.rightMargin = layoutParams.leftMargin / 2;
            layoutParams.topMargin = mDefaulTop
                    + (mDisPlayHeight - 2 * mDefaulTop) / 8;
            layoutParams.bottomMargin = layoutParams.topMargin;
            
            param.setExposureCompensation(0);
        }
        else
        {
            layoutParams.leftMargin = mDefaulLeft;
            layoutParams.rightMargin = mDefaulLeft / 2;
            layoutParams.topMargin = mDefaulTop;
            layoutParams.bottomMargin = mDefaulTop;
            
            param.setExposureCompensation(-1);
        }
        mCamera.setParameters(param);
    }
    
}