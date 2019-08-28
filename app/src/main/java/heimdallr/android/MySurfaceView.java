package heimdallr.android;

import android.content.Context;
import android.util.AttributeSet;


import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.core.Mat;



public class MySurfaceView extends JavaCameraView implements CameraBridgeViewBase.CvCameraViewListener2 {

    private long lastRecognizeTime;
    private OnNewFrameListener onNewFrameListener;

    public MySurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setCvCameraViewListener(this);  //设置摄像头监听器
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        long currentTime = System.currentTimeMillis();
        if((currentTime - lastRecognizeTime) > 2000){
            lastRecognizeTime = currentTime;
            //回调给识车牌别线程处理
            if(onNewFrameListener != null){
                onNewFrameListener.onNewFrame(inputFrame.rgba());  //将回调帧传送给识别算法
            }
        }
        return inputFrame.rgba();  //返回回调帧给摄像界面
    }

    public void setOnNewFrameListener(OnNewFrameListener onNewFrameListener){
        this.onNewFrameListener = onNewFrameListener;
    }


}
