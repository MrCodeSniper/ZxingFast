package cn.google.zxing;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.google.zxing.client.result.ParsedResult;
import cn.google.zxing.self.callback.OnScannerCompletionListener;
import cn.google.zxing.self.core.ScannerOptions;
import cn.google.zxing.self.decode.QRDecode;
import cn.google.zxing.self.handler.MyCaptureHandler;
import cn.google.zxing.self.manager.LightSensorManager;
import cn.google.zxing.self.util.ImageUtils;
import cn.google.zxing.self.util.StatusBarUtil;
import cn.google.zxing.self.view.ScannerView;
import cn.google.zxing.R;


import static cn.google.zxing.self.consts.CaptureConsts.PHOTO_REQUEST_GALLERY;

/**
 *  fast-zxing 提供的默认的扫码功能和界面 点击聚焦 两指缩放放大 手电筒 光照强度传感监听
 */
public class MyCapturesActivity extends Activity implements OnScannerCompletionListener, View.OnClickListener, SensorEventListener, LightSensorManager.LightStateCallback {

    public static final String TAG = "MyCapturesActivity";

    private ScannerView mScannerView;
    private ImageView tv_back;
    private TextView tv_album;
    private TextView tv_light;
    private TextView tv_mycode;
    private LinearLayout ll_click_light;
    private ImageView iv_light;

    private boolean isTourch = false;

    private SensorManager sensorManager;


    private MyCaptureHandler myCaptureHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StatusBarUtil.setStatusBarTransparent(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myqrcode);
        initSensor();
        initPreView();
        initScanOption();
    }

    private void initSensor() {
        sensorManager = (SensorManager) getApplicationContext().getSystemService(SENSOR_SERVICE);
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        sensorManager.registerListener(this, sensor, Sensor.TYPE_GRAVITY);
    }

    private void initPreView() {
        mScannerView = findViewById(R.id.scanner_view);
        tv_back = findViewById(R.id.tv_scan_back);
        tv_album = findViewById(R.id.tv_scan_alblum);
        tv_light = findViewById(R.id.touch_light_view);
        iv_light = findViewById(R.id.iv_light);
        tv_mycode = findViewById(R.id.zxing_myqrcode);
        ll_click_light = findViewById(R.id.ll_click_light);
        mScannerView.setOnScannerCompletionListener(this);
        tv_back.setOnClickListener(this);
        tv_album.setOnClickListener(this);
        tv_mycode.setOnClickListener(this);
        ll_click_light.setOnClickListener(this);
    }

    private void initScanOption() {
        ScannerOptions.Builder builder = new ScannerOptions.Builder();
        builder.setTipTextColor(R.color.transparent);
        builder.setTipTextSize(0);
        builder.setFrameCornerColor(getResources().getColor(R.color.main_color));
        builder.setLaserLineColor(getResources().getColor(R.color.main_color));
        mScannerView.setScannerOptions(builder.build());
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        mScannerView.onResume();// 这个时候就显示surfaceview并聚焦了
        LightSensorManager.getInstance(this).start(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mScannerView.onResume();
        LightSensorManager.getInstance(this).start(this);
    }

    @Override
    protected void onPause() {
        sensorManager.unregisterListener(this);
        super.onPause();
        mScannerView.onPause();
        LightSensorManager.getInstance(this).stop();
    }

    /**
     * 扫码完成回调
     *
     * @param result
     * @param parsedResult 抽象类，结果转换成目标类型
     * @param bitmap
     */
    @Override
    public void onScannerCompletion(Result result, ParsedResult parsedResult, Bitmap bitmap) {
        if(result!=null){
            Log.e("扫码结果", result.toString());
        }
    }



    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.tv_scan_back) {
            finish();
        } else if (id == R.id.tv_scan_alblum) {
            gallery();
        } else if (id == R.id.ll_click_light) {
            controllFlash();
        } else if (id == R.id.zxing_myqrcode) {

        }
    }

    private void controllFlash() {
        if (isTourch) {
            mScannerView.toggleLight(false);
            iv_light.setSelected(false);
            tv_light.setText("轻触照亮");
            isTourch = false;
        } else {
            iv_light.setSelected(true);
            tv_light.setText("轻触熄灭");
            mScannerView.toggleLight(true);
            isTourch = true;
        }
    }

    /**
     * 从相册获取
     */
    public void gallery() {
        // 激活系统图库，选择一张图片
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        // 开启一个带有返回值的Activity，请求码为PHOTO_REQUEST_GALLERY
        startActivityForResult(intent, PHOTO_REQUEST_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PHOTO_REQUEST_GALLERY) {
            // 从相册返回的数据
            if (data != null) {
                // 得到图片的全路径 解析图片 得到的是图片地址
                Uri uri = data.getData();
                QRDecode.decodeQR(ImageUtils.getRealPathFromUri(MyCapturesActivity.this, uri), this);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void lightWeak() {
        ll_click_light.setVisibility(View.VISIBLE);
    }

    @Override
    public void lightStrong() {
        ll_click_light.setVisibility(View.GONE);
    }

}
