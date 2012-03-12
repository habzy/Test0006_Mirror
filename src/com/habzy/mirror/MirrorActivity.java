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
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

public class MirrorActivity extends Activity
{
    
    private static final String TAG = "MirrorActivity";
    
    private static final int DEFAULT_LEFT = 100;
    
    private static final int DEFAUT_TOP = 200;
    
    private RelativeLayout mPreviewLayout;
    
    private LayoutParams layoutParams;
    
    private RelativeLayout mPreviewParent;
    
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
        public void onAccuracyChanged(Sensor arg0, int arg1)
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
                
                changeScreenProperty();
            }
        }
        
    };
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mPreviewParent = (RelativeLayout) findViewById(R.id.preview_parent);
        addPreviewFromXml();
        
        mHasLightSensors = getLightSensors();
        
        Display display = getWindowManager().getDefaultDisplay();
        Log.d(TAG, "height:" + display.getHeight());
        Log.d(TAG, "width:" + display.getWidth());
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
        
        layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        layoutParams.leftMargin = DEFAULT_LEFT;
        layoutParams.rightMargin = DEFAULT_LEFT / 2;
        layoutParams.topMargin = DEFAUT_TOP;
        layoutParams.bottomMargin = DEFAUT_TOP;
        
        mPreviewParent.addView(mPreviewLayout, layoutParams);
    }
    
    private void changeScreenProperty()
    {
        // TODO Auto-generated method stub
        
    }
    
    public void updateWindowSize()
    {
        mPreviewParent.updateViewLayout(mPreviewLayout, layoutParams);
    }
    
}