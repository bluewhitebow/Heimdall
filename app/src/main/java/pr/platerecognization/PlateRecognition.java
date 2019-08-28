package pr.platerecognization;


//public class PlateRecognition {
//    static {
//        System.loadLibrary("hyperlpr");
//    }
//    public static native long InitPlateRecognizer(String casacde_detection,
//                                           String finemapping_prototxt,String finemapping_caffemodel,
//                                           String segmentation_prototxt,String segmentation_caffemodel,
//                                           String charRecognization_proto,String charRecognization_caffemodel);
//
//    public static native void ReleasePlateRecognizer(long  object);
//    public static native String SimpleRecognization(long  inputMat,long object);
//
//}


import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * 车牌识别处理
 */

public class PlateRecognition {
    static {
        System.loadLibrary("hyperlpr");
    }

    private final static String TAG = "PlateRecognition";
    public final static int MSG_RESULT = 100;
    private Handler mHandler;
    private Context context;
    private long handle;

    public PlateRecognition(Context context, Handler handler){
        this.context = context;
        this.mHandler = handler;
    }

    private void copyFilesFromAssets(Context context, String oldPath, String newPath) {
        try {
            String[] fileNames = context.getAssets().list(oldPath);

            Log.d(TAG, "copyFilesFromAssets: ++++++++++++");

            for(String line :fileNames){
                Log.d(TAG, "copyFilesFromAssets:  " + line);
            }


            if (fileNames.length > 0) {
                File file = new File(newPath);
                if (!file.mkdir()) {
                    Log.d(TAG, "can't make directory");
                }
                for (String fileName : fileNames) {
                    copyFilesFromAssets(context, oldPath + "/" + fileName, newPath + "/" + fileName);
                }
            } else {
                InputStream is = context.getAssets().open(oldPath);
                FileOutputStream fos = new FileOutputStream(new File(newPath));
                byte[] buffer = new byte[1024];
                int byteCount;
                while ((byteCount = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, byteCount);
                }
                fos.flush();
                is.close();
                fos.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isFolderExist(String filePath){
        if (!TextUtils.isEmpty(filePath)) {
            File mFile = new File(filePath);
            return mFile.exists();
        }
        return false;
    }

    /**
     * init recognizer with model files
     */
    public void initRecognizer(String assetPath) {
        String sdcardPath = Environment.getExternalStorageDirectory() + File.separator + assetPath;
        if(!isFolderExist(sdcardPath)){//copy files to external storage when folder not exist
            copyFilesFromAssets(context, assetPath, sdcardPath);
        }
        String cascade_filename  =  sdcardPath
                + File.separator+"cascade.xml";
        String fine_mapping_prototxt  =  sdcardPath
                + File.separator+"HorizonalFinemapping.prototxt";
        String fine_mapping_caffemodel  =  sdcardPath
                + File.separator+"HorizonalFinemapping.caffemodel";
        String segmentation_prototxt =  sdcardPath
                + File.separator+"Segmentation.prototxt";
        String segmentation_caffemodel =  sdcardPath
                + File.separator+"Segmentation.caffemodel";
        String character_prototxt =  sdcardPath
                + File.separator+"CharacterRecognization.prototxt";
        String character_caffemodel=  sdcardPath
                + File.separator+"CharacterRecognization.caffemodel";
        String segmentation_free_prototxt =  sdcardPath
                + File.separator+"SegmentationFree.prototxt";
        String segmentation_free_caffemodel =  sdcardPath
                + File.separator+"SegmentationFree.caffemodel";
        handle  =  InitPlateRecognizer(
                cascade_filename,
                fine_mapping_prototxt,fine_mapping_caffemodel,
                segmentation_prototxt,segmentation_caffemodel,
                character_prototxt,character_caffemodel,
                segmentation_free_prototxt,segmentation_free_caffemodel
        );

        Log.i(TAG, "handle=" + handle);
    }


    public void doPlateRecognize(Mat mat){
        Mat dst = resizeMat(mat, 7);
        plateRecognize(dst);
    }

    private Mat bitmapToMat(Bitmap bmp, int dp){
        float ratio  = dp/10.f;
        Mat srcMat = new Mat(bmp.getWidth(), bmp.getHeight(), CvType.CV_8UC4);
        float new_w = bmp.getWidth() * ratio;
        float new_h = bmp.getHeight() * ratio;
        Size sz = new Size(new_w, new_h);
        Utils.bitmapToMat(bmp, srcMat);
        Imgproc.resize(srcMat, srcMat, sz);
        return srcMat;
    }

    private Mat resizeMat(Mat srcMat, int dp){
        float ratio  = dp/10.f;
        float new_w = srcMat.cols() * ratio;
        float new_h = srcMat.rows() * ratio;
        Size sz = new Size(new_w, new_h);
        Imgproc.resize(srcMat, srcMat, sz);
        return srcMat;
    }

    /**
     * running plate recognition with the target bitmap
     * rcMat srcMat
     */
    private void plateRecognize(Mat srcMat) {
        long currentTime = System.currentTimeMillis();
        String result = StartRecognize(srcMat.getNativeObjAddr(),handle);
        long diff = System.currentTimeMillis() - currentTime;
        if (!TextUtils.isEmpty(result)) {
            //send message to main thread
            Message msg = Message.obtain();
            msg.obj = result;
            msg.arg1 = (int)diff;
            msg.what = MSG_RESULT;
            mHandler.sendMessage(msg);
        }
    }

    /**
     * release the plate recognizer
     */
    public void releaseRecognizer(){
        ReleasePlateRecognizer(handle);
    }

    public static native long InitPlateRecognizer(String cascade_detection,
                                                   String fine_mapping_prototxt,String fine_mapping_caffemodel,
                                                   String segmentation_prototxt,String segmentation_caffemodel,
                                                   String recognition_proto,String recognition_caffemodel,
                                                   String segmentation_free_prototxt,String segmentation_free_caffemodel);

    public static native void ReleasePlateRecognizer(long  object);
    public static native String StartRecognize(long  inputMat,long object);

}