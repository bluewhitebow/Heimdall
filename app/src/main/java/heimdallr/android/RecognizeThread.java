package heimdallr.android;

import android.util.Log;

import org.opencv.core.Mat;

import java.util.LinkedList;

import pr.platerecognization.PlateRecognition;

import static org.opencv.core.Core.flip;
import static org.opencv.core.Core.transpose;

/***
 * 识别线程 识别队列中的mat对象
 */
public class RecognizeThread extends  Thread{

    private PlateRecognition plateRecognition;
    private LinkedList<Mat> matQueue;
    private final static Object lock = new Object();
    private boolean isRunning;
    private static final String TAG = "Recognition";
    public RecognizeThread(PlateRecognition plateRecognition){
        this.plateRecognition = plateRecognition;
        matQueue = new LinkedList<>();
        isRunning = true;
    }

    public void addMat(Mat mat){
        synchronized (lock){
            if(matQueue != null){
                matQueue.add(mat);
            }
        }
    }

    public void setRunning(boolean running){
        this.isRunning = running;
    }

    @Override
    public void run() {
        while (isRunning){
            Mat mat = null;
            synchronized (lock){
                if(matQueue != null && matQueue.size() > 0){
                    mat = matQueue.poll();//从队列中的到一个mat
                }
            }
            if(mat != null && plateRecognition != null){
                plateRecognition.doPlateRecognize(matRotateClockWise90(mat));
            }
        }
    }


    Mat matRotateClockWise90(Mat src)
    {
        if (src.empty())
        {
            Log.d(TAG, "matRotateClockWise90:error");
        }
        // 矩阵转置
        transpose(src, src);
        //0: 沿X轴翻转； >0: 沿Y轴翻转； <0: 沿X轴和Y轴翻转
        flip(src, src, 1);// 翻转模式，flipCode == 0垂直翻转（沿X轴翻转），flipCode>0水平翻转（沿Y轴翻转），flipCode<0水平垂直翻转（先沿X轴翻转，再沿Y轴翻转，等价于旋转180°）
        return src;
    }

    Mat matRotateClockWise180(Mat src)//顺时针180
    {
        if (src.empty())
        {
            Log.d(TAG, "matRotateClockWise180: error");
        }

        //0: 沿X轴翻转； >0: 沿Y轴翻转； <0: 沿X轴和Y轴翻转
        flip(src, src, 0);// 翻转模式，flipCode == 0垂直翻转（沿X轴翻转），flipCode>0水平翻转（沿Y轴翻转），flipCode<0水平垂直翻转（先沿X轴翻转，再沿Y轴翻转，等价于旋转180°）
        flip(src, src, 1);
        return src;
        //transpose(src, src);// 矩阵转置
    }

    public Mat matRotateClockWise270(Mat src)//顺时针270
    {
        if (src.empty())
        {
            Log.d(TAG, "matRotateClockWise270: error");
        }
        // 矩阵转置
        //transpose(src, src);
        //0: 沿X轴翻转； >0: 沿Y轴翻转； <0: 沿X轴和Y轴翻转
        transpose(src, src);// 翻转模式，flipCode == 0垂直翻转（沿X轴翻转），flipCode>0水平翻转（沿Y轴翻转），flipCode<0水平垂直翻转（先沿X轴翻转，再沿Y轴翻转，等价于旋转180°）
        flip(src, src, 0);
        return src;
    }

    public Mat myRotateAntiClockWise90(Mat src){
        if (src.empty()) {
            Log.d(TAG, "myRotateAntiClockWise90: error");
        }
        transpose(src, src);
        flip(src, src, 0);


        return src;
    }

}
