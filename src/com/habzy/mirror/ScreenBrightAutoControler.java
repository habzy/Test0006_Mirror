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

import android.app.Activity;
import android.content.ContentResolver;
import android.net.Uri;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.WindowManager;

/**
 * Most of these codes were copied from website:
 * "http://www.itivy.com/android/archive/2011/7/3/android-adjust-screen-brightness.html"
 * 
 * @author Habzy Huang
 * 
 */
public class ScreenBrightAutoControler
{
    private static final String TAG = "ScreenBrightAutoControler";
    
    private static final String SCREEN_BRIGHTNESS = "screen_brightness";
    
    /**
     * 
     * @param contentResolver
     * @return True, when system settings open SCREEN_BRIGHTNESS_MODE_AUTOMATIC
     */
    public static boolean isAutoBrightness(Activity activity)
    {
        boolean automicBrightness = false;
        try
        {
            automicBrightness = Settings.System.getInt(activity.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
        }
        catch (SettingNotFoundException e)
        {
            e.printStackTrace();
        }
        Log.d(TAG, "is Auto Brightness ?=" + automicBrightness);
        return automicBrightness;
    }
    
    /**
     * Stop auto-brightness.
     * 
     * @param activity
     * 
     * 
     */
    public static void stopAutoBrightness(Activity activity)
    {
        Settings.System.putInt(activity.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
    }
    
    /**
     * Start auto-brightness.
     * 
     * @param activity
     */
    public static void startAutoBrightness(Activity activity)
    {
        Settings.System.putInt(activity.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
    }
    
    /**
     * Get the value of screen brightness.
     * 
     * @param activity
     * @return
     */
    public static int getScreenBrightness(Activity activity)
    {
        int nowBrightnessValue = 0;
        ContentResolver resolver = activity.getContentResolver();
        try
        {
            nowBrightnessValue = android.provider.Settings.System.getInt(resolver,
                    Settings.System.SCREEN_BRIGHTNESS);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
        Log.d(TAG, "The screen brightness is ?=" + nowBrightnessValue);
        return nowBrightnessValue;
    }
    
    /**
     * Set value for the screen brightness.
     * 
     * @param activity
     * @param brightness
     */
    public static void setBrightness(Activity activity, int brightness)
    {
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        lp.screenBrightness = Float.valueOf(brightness) * (1f / 255f);
        activity.getWindow().setAttributes(lp);
    }
    
    /**
     * Save the brightness status.
     * 
     * @param resolver
     * @param brightness
     */
    public static void saveBrightness(ContentResolver resolver, int brightness)
    {
        Uri uri = android.provider.Settings.System.getUriFor(SCREEN_BRIGHTNESS);
        android.provider.Settings.System.putInt(resolver,
                SCREEN_BRIGHTNESS,
                brightness);
        resolver.notifyChange(uri, null);
    }
    
}
