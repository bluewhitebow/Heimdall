package heimdallr.android;

import org.opencv.core.Mat;

/***
 * 预览帧数据回调监听
 */
public interface OnNewFrameListener {

    void onNewFrame(Mat newFrame);
}
