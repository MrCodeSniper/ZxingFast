package cn.google.zxing.self.manager;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * 光照传感器管理类
 */
public class LightSensorManager {

    private static LightSensorManager instance;
    private SensorManager mSensorManager;

    private boolean mHasStarted = false;

    private LightSensorListener mLightSensorListener;
    private LightStateCallback callback;

    private LightSensorManager(LightStateCallback callback) {
        this.callback = callback;
    }

    public static LightSensorManager getInstance(LightStateCallback callback) {
        if (instance == null) {
            instance = new LightSensorManager(callback);
        }
        return instance;
    }

    public void start(Context context) {
        if (mHasStarted) {
            return;
        }
        mHasStarted = true;
        mSensorManager = (SensorManager) context.getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
        Sensor lightSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT); // 获取光线传感器
        if (lightSensor != null) { // 光线传感器存在时
            mLightSensorListener = new LightSensorListener();
            mSensorManager.registerListener(mLightSensorListener, lightSensor, SensorManager.SENSOR_DELAY_NORMAL); // 注册事件监听
        }
    }

    public void stop() {
        if (!mHasStarted || mSensorManager == null) {
            return;
        }
        mHasStarted = false;
        mSensorManager.unregisterListener(mLightSensorListener);
    }

    /**
     * 获取光线强度
     */
    public float getLux() {
        if (mLightSensorListener != null) {
            return mLightSensorListener.lux;
        }
        return -1.0f; // 默认返回-1，表示设备无光线传感器或者为调用start()方法
    }

    private class LightSensorListener implements SensorEventListener {

        private float lux; // 光线强度
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
                // 获取光线强度
                lux = event.values[0];
                if(lux < 120){
                    callback.lightWeak();
                }else {
                    callback.lightStrong();
                }
            }
        }
    }

    public interface LightStateCallback{
        void lightWeak();
        void lightStrong();
    }
}
