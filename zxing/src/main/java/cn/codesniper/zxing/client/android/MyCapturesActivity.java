package cn.codesniper.zxing.client.android;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.Result;
import com.google.zxing.client.result.ParsedResult;
import com.google.zxing.self.callback.OnScannerCompletionListener;
import com.google.zxing.self.core.ParsedQrCodeType;
import com.google.zxing.self.core.ScannerOptions;
import com.google.zxing.self.decode.QRDecode;
import com.google.zxing.self.event.CaptureEvent;
import com.google.zxing.self.handler.MyCaptureHandler;
import com.google.zxing.self.manager.LightSensorManager;
import com.google.zxing.self.util.ImageUtils;
import com.google.zxing.self.util.NetWorkUtils;
import com.google.zxing.self.util.StatusBarUtil;
import com.google.zxing.self.view.ScannerView;
import com.hrz.qrcode.R;

import org.greenrobot.eventbus.EventBus;

import static com.google.zxing.self.consts.CaptureConsts.PHOTO_REQUEST_GALLERY;

/**
 *
 */
public class MyCapturesActivity extends Activity implements OnScannerCompletionListener, View.OnClickListener,
		SensorEventListener, LightSensorManager.LightStateCallback
{

	public static final String	TAG			= "MyCapturesActivity";

	private ScannerView			mScannerView;
	private ImageView			tv_back;
	private TextView			tv_album;
	private TextView			tv_light;
	private TextView			tv_mycode;
	private LinearLayout		ll_click_light;
	private ImageView			iv_light;

	private boolean				isTourch	= false;

	private SensorManager		sensorManager;

	// private SureDialog rxDialogSure;

	private MyCaptureHandler	myCaptureHandler;

	public static void routeToCaptureActivity(Activity context, ParsedQrCodeType type, String from)
	{
		Intent intent = new Intent(context, MyCapturesActivity.class);
		intent.putExtra("routeFrom", type);
		intent.putExtra("fromWhere", from);

		context.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		StatusBarUtil.setStatusBarTransparent(this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_myqrcode);
		initSensor();
		initPreView();
		initScanOption();
	}

	private void initSensor()
	{
		sensorManager = (SensorManager) getApplicationContext().getSystemService(SENSOR_SERVICE);
		Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
		sensorManager.registerListener(this, sensor, Sensor.TYPE_GRAVITY);
	}

	private void initPreView()
	{
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

	private void initScanOption()
	{
		ScannerOptions.Builder builder = new ScannerOptions.Builder();
		builder.setTipTextColor(R.color.transparent);
		builder.setTipTextSize(0);
		builder.setFrameCornerColor(getResources().getColor(R.color.main_color));
		builder.setLaserLineColor(getResources().getColor(R.color.main_color));
		mScannerView.setScannerOptions(builder.build());
	}

	@Override
	public void onSensorChanged(SensorEvent event)
	{

	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy)
	{

	}

	@Override
	protected void onResume()
	{
		super.onResume();
		mScannerView.onResume();// 这个时候就显示surfaceview并聚焦了
		LightSensorManager.getInstance(this).start(this);
	}

	@Override
	protected void onStart()
	{
		super.onStart();
		mScannerView.onResume();
		LightSensorManager.getInstance(this).start(this);
	}

	@Override
	protected void onPause()
	{
		sensorManager.unregisterListener(this);
		super.onPause();
		mScannerView.onPause();
		LightSensorManager.getInstance(this).stop();
	}

	/**
	 * 扫码完成回调
	 * 
	 * @param result
	 * @param parsedResult
	 *            抽象类，结果转换成目标类型
	 * @param bitmap
	 */
	@Override
	public void onScannerCompletion(Result result, ParsedResult parsedResult, Bitmap bitmap)
	{

		if (result == null || TextUtils.isEmpty(result.getText()))
		{
			showDialog("识别不到/二维码无效", "重新识别");
			return;
		}
		Log.e("扫码结果", result.toString());

		if (!NetWorkUtils.checkNetWork(this))
		{
			showDialog("联网超时,请检查网络", "确定");
			return;
		}

		switchQrCode(result.getText());
	}

	private void switchQrCode(String qrCode)
	{

		ParsedQrCodeType parsedQrCodeType = (ParsedQrCodeType) getIntent().getSerializableExtra("routeFrom");
		String whereFrom = getIntent().getStringExtra("fromWhere");
		CaptureEvent captureEvent = new CaptureEvent();

		// if(qrCode.contains("https://image.hdyl.net.cn/payQRCode/")||qrCode.contains("http://img.lexj.com//payQRCode/")){
		// showDialog("该二维码已经被激活", "重新识别");
		// finish();
		// return;
		// }

		// 1.商家入驻二维码 扫描结果格式 http://hdyl.lexj.com/abutment?pid=5541890
		if (qrCode.contains("abutment") && parsedQrCodeType == ParsedQrCodeType.MERCHANT_IN)
		{
			Intent intent = new Intent();
			intent.putExtra("pid", Uri.parse(qrCode).getQueryParameter("pid"));
			intent.putExtra("type", ParsedQrCodeType.MERCHANT_IN);

			intent.putExtra("whereFrom", whereFrom);
			captureEvent.setIntent(intent);
			Log.e(TAG, "商家入驻二维码");
			// 2.激活商家二维码 https://qr.hdyl.net.cn?sid=119247550656236&ver=1.0&from=hrz
		}
		else if (qrCode.contains("http://qr.hongrenzhuang.com?pid=")
				&& parsedQrCodeType == ParsedQrCodeType.MERCHANT_IN)
		{
			Intent intent = new Intent();
			intent.putExtra("pid", Uri.parse(qrCode).getQueryParameter("pid"));
			intent.putExtra("type", ParsedQrCodeType.MERCHANT_IN);
			intent.putExtra("whereFrom", whereFrom);

			captureEvent.setIntent(intent);
			Log.e(TAG, "商家入驻二维码");
			// 2.激活商家二维码 https://qr.hdyl.net.cn?sid=119247550656236&ver=1.0&from=hrz
		}
		// else if (parsedQrCodeType == ParsedQrCodeType.ACTIVE_MERCHANT_QRCODE && qrCode.contains("qr.hdyl.net.cn"))
		// {
		//
		// }
		else if (!qrCode.contains("abutment") && parsedQrCodeType == ParsedQrCodeType.MERCHANT_IN)
		{
			Toast.makeText(this, "该用户未申请成为对接人", Toast.LENGTH_SHORT).show();
			this.finish();
			return;
		}

		else
		{
			Intent intent = new Intent();
			intent.putExtra("qrcodeContent", qrCode);
			intent.putExtra("type", ParsedQrCodeType.ACTIVE_MERCHANT_QRCODE);
			intent.putExtra("whereFrom", whereFrom);

			captureEvent.setIntent(intent);
			Log.e(TAG, "激活商家二维码");
		}
		// else
		// {
		// showDialog("识别不到/二维码无效", "重新识别");
		// return;
		//
		// }

		EventBus.getDefault().post(captureEvent);

		finish();
	}

	@Override
	public void onClick(View v)
	{
		int id = v.getId();
		if (id == R.id.tv_scan_back)
		{
			finish();
		}
		else if (id == R.id.tv_scan_alblum)
		{
			gallery();
		}
		else if (id == R.id.ll_click_light)
		{
			controllFlash();
		}
		else if (id == R.id.zxing_myqrcode)
		{

		}
	}

	private void controllFlash()
	{
		if (isTourch)
		{
			mScannerView.toggleLight(false);
			iv_light.setSelected(false);
			tv_light.setText("轻触照亮");
			isTourch = false;
		}
		else
		{
			iv_light.setSelected(true);
			tv_light.setText("轻触熄灭");
			mScannerView.toggleLight(true);
			isTourch = true;
		}
	}

	/**
	 * 从相册获取
	 */
	public void gallery()
	{
		// 激活系统图库，选择一张图片
		Intent intent = new Intent(Intent.ACTION_PICK);
		intent.setType("image/*");
		// 开启一个带有返回值的Activity，请求码为PHOTO_REQUEST_GALLERY
		startActivityForResult(intent, PHOTO_REQUEST_GALLERY);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (requestCode == PHOTO_REQUEST_GALLERY)
		{
			// 从相册返回的数据
			if (data != null)
			{
				// 得到图片的全路径 解析图片 得到的是图片地址
				Uri uri = data.getData();
				QRDecode.decodeQR(ImageUtils.getRealPathFromUri(MyCapturesActivity.this, uri), this);
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	public void noNetDialog()
	{
		showDialog("联网超时,请检查网络", "确定");
	}

	public void requestOutTime()
	{
		showDialog("识别超时", "重新识别");
	}

	private void showDialog(String content, String check)
	{
		Log.e(TAG, content);
		Toast.makeText(this.getApplicationContext(), content, Toast.LENGTH_SHORT).show();
		// if(rxDialogSure==null){
		// rxDialogSure = new SureDialog(this);//提示弹窗
		// }
		// rxDialogSure.getLogoView().setVisibility(View.GONE);
		// rxDialogSure.getTitleView().setVisibility(View.GONE);
		// rxDialogSure.setContent(content);
		// rxDialogSure.setSure(check);
		// rxDialogSure.getSureView().setOnClickListener(v -> {
		// rxDialogSure.cancel();
		// mScannerView.onResume();
		// });
		// rxDialogSure.show();

	}

	@Override
	public void lightWeak()
	{
		ll_click_light.setVisibility(View.VISIBLE);
	}

	@Override
	public void lightStrong()
	{
		ll_click_light.setVisibility(View.GONE);
	}

}
