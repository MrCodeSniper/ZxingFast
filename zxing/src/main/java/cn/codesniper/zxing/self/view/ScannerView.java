package cn.codesniper.zxing.self.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.camera.open.CameraFacing;
import com.google.zxing.common.detector.MathUtils;
import com.google.zxing.self.callback.OnScannerCompletionListener;
import com.google.zxing.self.core.Scanner;
import com.google.zxing.self.core.ScannerOptions;
import com.google.zxing.self.manager.BeepManager;


/**
 * Created by hupei on 2016/7/1.
 */
public class ScannerView extends RelativeLayout {

    private GestureDetector mGestureDetector;

    private static final String TAG = ScannerView.class.getSimpleName();

    private CameraSurfaceView mSurfaceView;
    private MyViewfinderView mViewfinderView;

    private BeepManager mBeepManager;
    private OnScannerCompletionListener mScannerCompletionListener;

    private ScannerOptions mScannerOptions;
    private ScannerOptions.Builder mScannerOptionsBuilder;

    public ScannerView(Context context) {
        this(context, null);
    }

    public ScannerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    //findviewbyID就初始化了
    public ScannerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {

        mGestureDetector =new GestureDetector(new gestureListener());

        //addview默认全部view宽高 整个相机view 用surfaceview 一帧帧的画
        mSurfaceView = new CameraSurfaceView(context, this);
        mSurfaceView.setId(android.R.id.list);
        addView(mSurfaceView);

        //扫描框的视图
        mViewfinderView = new MyViewfinderView(context, attrs);
        LayoutParams layoutParams = new LayoutParams(context, attrs);
        layoutParams.addRule(RelativeLayout.ALIGN_TOP, mSurfaceView.getId());
        layoutParams.addRule(RelativeLayout.ALIGN_BOTTOM, mSurfaceView.getId());
        addView(mViewfinderView, layoutParams);

        //装配扫描框视图的配置
        mScannerOptionsBuilder = new ScannerOptions.Builder();
        mScannerOptions = mScannerOptionsBuilder.build();
    }

    public void onResume() {
        mSurfaceView.onResume(mScannerOptions);
        mViewfinderView.setCameraManager(mSurfaceView.getCameraManager());
        mViewfinderView.setScannerOptions(mScannerOptions);
        mViewfinderView.setVisibility(mScannerOptions.isViewfinderHide() ? View.GONE : View.VISIBLE);
        if (mBeepManager != null) mBeepManager.updatePrefs();
    }

    public void onPause() {
        mSurfaceView.onPause();
        if (mBeepManager != null) mBeepManager.close();
        mViewfinderView.laserLineBitmapRecycle();
    }


    /**
     * A valid barcode has been found, so give an indication of success and show
     * the results.
     *
     * @param rawResult   The contents of the barcode.
     * @param scaleFactor amount by which thumbnail was scaled
     * @param barcode     A greyscale bitmap of the camera data which was decoded.
     */
    void handleDecode(Result rawResult, Bitmap barcode, float scaleFactor) {
        //扫描成功
        if (mScannerCompletionListener != null) {
            //转换结果
            mScannerCompletionListener.onScannerCompletion(rawResult, Scanner.parseResult(rawResult), barcode);
        }
        if (mScannerOptions.getMediaResId() != 0) {
            if (mBeepManager == null) {
                mBeepManager = new BeepManager(getContext());
                mBeepManager.setMediaResId(mScannerOptions.getMediaResId());
            }
            mBeepManager.playBeepSoundAndVibrate();
        }

        if (barcode != null && mScannerOptions.isShowQrThumbnail()) {
            mViewfinderView.drawResultBitmap(barcode);
            drawResultPoints(barcode, scaleFactor, rawResult);
        }
    }

    /**
     * Superimpose a line for 1D or dots for 2D to highlight the key features of
     * the barcode.
     *
     * @param barcode     A bitmap of the captured image.
     * @param scaleFactor amount by which thumbnail was scaled
     * @param rawResult   The decoded results which contains the points to draw.
     */
    private void drawResultPoints(Bitmap barcode, float scaleFactor, Result rawResult) {
        ResultPoint[] points = rawResult.getResultPoints();
        if (points != null && points.length > 0) {
            Canvas canvas = new Canvas(barcode);
            Paint paint = new Paint();
            paint.setColor(Scanner.color.RESULT_POINTS);
            if (points.length == 2) {
                paint.setStrokeWidth(4.0f);
                drawLine(canvas, paint, points[0], points[1], scaleFactor);
            } else if (points.length == 4
                    && (rawResult.getBarcodeFormat() == BarcodeFormat.UPC_A || rawResult.getBarcodeFormat() == BarcodeFormat.EAN_13)) {
                // Hacky special case -- draw two lines, for the barcode and
                // metadata
                drawLine(canvas, paint, points[0], points[1], scaleFactor);
                drawLine(canvas, paint, points[2], points[3], scaleFactor);
            } else {
                paint.setStrokeWidth(10.0f);
                for (ResultPoint point : points) {
                    if (point != null) {
                        canvas.drawPoint(scaleFactor * point.getX(), scaleFactor * point.getY(), paint);
                    }
                }
            }
        }
    }

    private static void drawLine(Canvas canvas, Paint paint, ResultPoint a, ResultPoint b, float scaleFactor) {
        if (a != null && b != null) {
            canvas.drawLine(scaleFactor * a.getX(), scaleFactor * a.getY(), scaleFactor * b.getX(), scaleFactor * b.getY(), paint);
        }
    }

    /**
     * 设置扫描成功监听器
     *
     * @param listener
     * @return
     */
    public ScannerView setOnScannerCompletionListener(OnScannerCompletionListener listener) {
        this.mScannerCompletionListener = listener;
        return this;
    }

    public void setScannerOptions(ScannerOptions scannerOptions) {
        this.mScannerOptions = scannerOptions;
    }

    /**
     * 切换闪光灯
     *
     * @param mode true开；false关
     */
    public ScannerView toggleLight(boolean mode) {
        mSurfaceView.setTorch(mode);
        return this;
    }

    /**
     * 在经过一段延迟后重置相机以进行下一次扫描。 成功扫描过后可调用此方法立刻准备进行下次扫描
     *
     * @param delayMS 毫秒
     */
    public void restartPreviewAfterDelay(long delayMS) {
        mSurfaceView.restartPreviewAfterDelay(delayMS);
    }

    /**
     * 设置扫描线颜色
     *
     * @param color
     */
    @Deprecated
    public ScannerView setLaserColor(int color) {
        mScannerOptionsBuilder.setLaserStyle(ScannerOptions.LaserStyle.COLOR_LINE, color);
        return this;
    }

    /**
     * 设置线形扫描线资源
     *
     * @param resId resId
     */
    @Deprecated
    public ScannerView setLaserLineResId(int resId) {
        mScannerOptionsBuilder.setLaserStyle(ScannerOptions.LaserStyle.RES_LINE, resId);
        return this;
    }

    /**
     * 设置网格扫描线资源
     *
     * @param resId resId
     */
    @Deprecated
    public ScannerView setLaserGridLineResId(int resId) {
        mScannerOptionsBuilder.setLaserStyle(ScannerOptions.LaserStyle.RES_GRID, resId);
        return this;
    }

    /**
     * 设置扫描线高度
     *
     * @param height dp
     */
    @Deprecated
    public ScannerView setLaserLineHeight(int height) {
        mScannerOptionsBuilder.setLaserLineHeight(height);
        return this;
    }

    /**
     * 设置扫描框4角颜色
     *
     * @param color
     */
    @Deprecated
    public ScannerView setLaserFrameBoundColor(int color) {
        mScannerOptionsBuilder.setFrameCornerColor(color);
        return this;
    }

    /**
     * 设置扫描框4角长度
     *
     * @param length dp
     */
    @Deprecated
    public ScannerView setLaserFrameCornerLength(int length) {
        mScannerOptionsBuilder.setFrameCornerLength(length);
        return this;
    }

    /**
     * 设置扫描框4角宽度
     *
     * @param width dp
     */
    @Deprecated
    public ScannerView setLaserFrameCornerWidth(int width) {
        mScannerOptionsBuilder.setFrameCornerWidth(width);
        return this;
    }

    /**
     * 设置文字颜色
     *
     * @param color 文字颜色
     */
    @Deprecated
    public ScannerView setDrawTextColor(int color) {
        mScannerOptionsBuilder.setTipTextColor(color);
        return this;
    }

    /**
     * 设置文字大小
     *
     * @param size 文字大小 sp
     */
    @Deprecated
    public ScannerView setDrawTextSize(int size) {
        mScannerOptionsBuilder.setTipTextSize(size);
        return this;
    }

    /**
     * 设置文字
     *
     * @param text
     * @param bottom 是否在扫描框下方
     */
    @Deprecated
    public ScannerView setDrawText(String text, boolean bottom) {
        mScannerOptionsBuilder.setTipText(text);
        mScannerOptionsBuilder.setTipTextToFrameTop(!bottom);
        return this;
    }

    /**
     * 设置文字
     *
     * @param text
     * @param bottom 是否在扫描框下方
     * @param margin 离扫描框间距 dp
     */
    @Deprecated
    public ScannerView setDrawText(String text, boolean bottom, int margin) {
        mScannerOptionsBuilder.setTipText(text);
        mScannerOptionsBuilder.setTipTextToFrameTop(!bottom);
        mScannerOptionsBuilder.setTipTextToFrameMargin(margin);
        return this;
    }

    /**
     * 设置文字
     *
     * @param text
     * @param size   文字大小 sp
     * @param color  文字颜色
     * @param bottom 是否在扫描框下方
     * @param margin 离扫描框间距 dp
     */
    @Deprecated
    public ScannerView setDrawText(String text, int size, int color, boolean bottom, int margin) {
        mScannerOptionsBuilder.setTipText(text);
        mScannerOptionsBuilder.setTipTextSize(size);
        mScannerOptionsBuilder.setTipTextColor(color);
        mScannerOptionsBuilder.setTipTextToFrameTop(!bottom);
        mScannerOptionsBuilder.setTipTextToFrameMargin(margin);
        return this;
    }

    /**
     * 设置扫描完成播放声音
     *
     * @param resId
     */
    @Deprecated
    public ScannerView setMediaResId(int resId) {
        mScannerOptionsBuilder.setMediaResId(resId);
        return this;
    }

    /**
     * 设置扫描框大小
     *
     * @param width  dp
     * @param height dp
     */
    @Deprecated
    public ScannerView setLaserFrameSize(int width, int height) {
        mScannerOptionsBuilder.setFrameSize(width, height);
        return this;
    }

    /**
     * 设置扫描框与屏幕距离
     *
     * @param margin
     */
    @Deprecated
    public ScannerView setLaserFrameTopMargin(int margin) {
        mScannerOptionsBuilder.setFrameTopMargin(margin);
        return this;
    }

    /**
     * 设置扫描解码类型（二维码、一维码、商品条码）
     *
     * @param scanMode {@linkplain Scanner.ScanMode mode}
     * @return
     */
    @Deprecated
    public ScannerView setScanMode(String scanMode) {
        mScannerOptionsBuilder.setScanMode(scanMode);
        return this;
    }

    /**
     * 设置扫描解码类型
     *
     * @param barcodeFormat
     * @return
     */
    @Deprecated
    public ScannerView setScanMode(BarcodeFormat... barcodeFormat) {
        mScannerOptionsBuilder.setScanMode(barcodeFormat);
        return this;
    }

    /**
     * 是否显示扫描结果缩略图
     *
     * @param showResThumbnail
     * @return
     */
    @Deprecated
    public ScannerView isShowResThumbnail(boolean showResThumbnail) {
        mScannerOptionsBuilder.setCreateQrThumbnail(showResThumbnail);
        return this;
    }

    /**
     * 设置扫描框线移动间距，每毫秒移动 moveSpeed 像素
     *
     * @param moveSpeed px
     * @return
     */
    @Deprecated
    public ScannerView setLaserMoveSpeed(int moveSpeed) {
        mScannerOptionsBuilder.setLaserMoveSpeed(moveSpeed);
        return this;
    }

    /**
     * 设置扫描摄像头，默认后置
     *
     * @param cameraFacing
     * @return
     */
    @Deprecated
    public ScannerView setCameraFacing(CameraFacing cameraFacing) {
        mScannerOptionsBuilder.setCameraFacing(cameraFacing);
        return this;
    }

    /**
     * 是否全屏扫描
     *
     * @param scanFullScreen
     * @return
     */
    @Deprecated
    public ScannerView isScanFullScreen(boolean scanFullScreen) {
        mScannerOptionsBuilder.setScanFullScreen(scanFullScreen);
        return this;
    }

    /**
     * 设置隐藏取景视图，包括文字
     *
     * @param hide
     * @return
     */
    @Deprecated
    public ScannerView isHideLaserFrame(boolean hide) {
        mScannerOptionsBuilder.setViewfinderHide(hide);
        return this;
    }

    /**
     * 是否扫描反色二维码（黑底白码）
     *
     * @param invertScan
     * @return
     */
    @Deprecated
    public ScannerView isScanInvert(boolean invertScan) {
        mScannerOptionsBuilder.setScanInvert(invertScan);
        return this;
    }

    void drawViewfinder() {
        mViewfinderView.drawViewfinder();
    }


    private float mOldis;

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        mGestureDetector.onTouchEvent(event);

        int pointCount=event.getPointerCount();
        if(pointCount==1){

        }else if(pointCount>=2){
            switch (event.getAction() & MotionEvent.ACTION_MASK){
                case MotionEvent.ACTION_POINTER_DOWN:
                    mOldis=getFingerSpacing(event);
                break;
                case MotionEvent.ACTION_MOVE:
                    float newDis =getFingerSpacing(event);
                    if(newDis>mOldis){
                        handleZoom(true,mSurfaceView);
                    }else {
                        handleZoom(false,mSurfaceView);
                    }
                    mOldis=newDis;
                    break;
            }
        }
        return true;
    }



    private  float getFingerSpacing(MotionEvent event){
        if(event.getPointerCount()>=2){
            return MathUtils.distance(event.getX(0),event.getY(0),event.getX(1),event.getY(1));
        }
        return -1;
    }

    private void handleZoom(boolean zoomIn,CameraSurfaceView surfaceView){
        if(surfaceView.getCameraManager()==null){
            return;
        }
        Camera camera=surfaceView.getCameraManager().getCamera().getCamera();
        Camera.Parameters parameters=camera.getParameters();
        if(parameters.isZoomSupported()){

            int maxZoom=parameters.getMaxZoom();
            int curZoom=parameters.getZoom();
            if(zoomIn){
                if(curZoom<maxZoom){
                    curZoom++;
                }
            }else {
                if(curZoom>0){
                    curZoom--;
                }
            }
            parameters.setZoom(curZoom);
            camera.setParameters(parameters);
        }
    }


    private class gestureListener implements GestureDetector.OnGestureListener{

        // 用户轻触触摸屏，由1个MotionEvent ACTION_DOWN触发
        public boolean onDown(MotionEvent e) {
            Log.i("MyGesture", "onDown");
            return false;
        }

        /*
         * 用户轻触触摸屏，尚未松开或拖动，由一个1个MotionEvent ACTION_DOWN触发
         * 注意和onDown()的区别，强调的是没有松开或者拖动的状态
         *
         * 而onDown也是由一个MotionEventACTION_DOWN触发的，但是他没有任何限制，
         * 也就是说当用户点击的时候，首先MotionEventACTION_DOWN，onDown就会执行，
         * 如果在按下的瞬间没有松开或者是拖动的时候onShowPress就会执行，如果是按下的时间超过瞬间
         * （这块我也不太清楚瞬间的时间差是多少，一般情况下都会执行onShowPress），拖动了，就不执行onShowPress。
         */
        public void onShowPress(MotionEvent e) {
            Log.i("MyGesture", "onShowPress");
        }

        // 用户（轻触触摸屏后）松开，由一个1个MotionEvent ACTION_UP触发
        ///轻击一下屏幕，立刻抬起来，才会有这个触发
        //从名子也可以看出,一次单独的轻击抬起操作,当然,如果除了Down以外还有其它操作,那就不再算是Single操作了,所以这个事件 就不再响应
        public boolean onSingleTapUp(MotionEvent e) {
            Log.i("MyGesture", "onSingleTapUp");
            return true;
        }

        // 用户按下触摸屏，并拖动，由1个MotionEvent ACTION_DOWN, 多个ACTION_MOVE触发
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                float distanceX, float distanceY) {
            Log.i("MyGesture22", "onScroll:"+(e2.getX()-e1.getX()) +"   "+distanceX);

            return true;
        }

        // 用户长按触摸屏，由多个MotionEvent ACTION_DOWN触发
        public void onLongPress(MotionEvent e) {
            Log.i("MyGesture", "onLongPress");
        }

        // 用户按下触摸屏、快速移动后松开，由1个MotionEvent ACTION_DOWN, 多个ACTION_MOVE, 1个ACTION_UP触发
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                               float velocityY) {
            Log.i("MyGesture", "onFling");
            return true;
        }
    };
}
