package heimdallr.android;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import pr.platerecognization.PlateRecognition;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HomeFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment implements OnNewFrameListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private PlateRecognition plateRecognition;
    private RecognizeThread recognizeThread;
    private MySurfaceView recognizerView;
    private TextView res_box;
    private TextView runtime_box;
    private Mat dstMat;
    private OnFragmentInteractionListener mListener;
    private static  final String TAG = "HomeFragment";

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }


    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case PlateRecognition.MSG_RESULT://recognize finish
                    String result = (String) msg.obj;
                    Toast.makeText(getContext(), result, Toast.LENGTH_SHORT).show();

                    EventBus.getDefault().post(new MessageEvent(result));
                    break;
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);
        plateRecognition = new PlateRecognition(getContext(), mHandler);
        //init plate recognizer
        new Thread(new Runnable() {
            @Override
            public void run() {
                plateRecognition.initRecognizer("pr");
            }
        }).start();

        recognizerView = (MySurfaceView) view.findViewById(R.id.ho_surface);

//        res_box = (TextView)view.findViewById(R.id.ho_res_box);
//        runtime_box = (TextView)view.findViewById(R.id.ho_runtime_box);
        recognizerView.setOnNewFrameListener(this);
        recognizeThread = new RecognizeThread(plateRecognition);
        recognizeThread.start();
        initOpenCV();


        return view;
    }


    private void initOpenCV(){

        recognizerView.enableView();

        boolean result = OpenCVLoader.initDebug();
        if(result){
            Log.i(TAG, "initOpenCV success...");
            recognizerView.enableView();
        }else {
            Log.e(TAG, "initOpenCV fail...");
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;

        if(plateRecognition != null){
            //release plate recognizer
            plateRecognition.releaseRecognizer();
        }
        if(recognizeThread != null){
            recognizeThread.setRunning(false);
            recognizeThread.interrupt();
            recognizeThread = null;
        }
    }

    @Override
    public void onNewFrame(Mat newFrame) {
        if(dstMat == null){
            dstMat = new Mat(newFrame.rows(), newFrame.cols(), CvType.CV_8UC4);
        }
        newFrame.copyTo(dstMat);
        if(recognizeThread != null){
            recognizeThread.addMat(dstMat);
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
