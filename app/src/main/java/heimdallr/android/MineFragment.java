package heimdallr.android;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import heimdallr.android.R;

import static com.android.volley.VolleyLog.TAG;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MineFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MineFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MineFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public MineFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MineFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MineFragment newInstance(String param1, String param2) {
        MineFragment fragment = new MineFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    private Context mContext;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        this.mContext = getActivity();
    }


    /*
    * 获取从activity从后端查到的用户信息
    * */
    private String account;
    private String result;
    private String name;
    private String sex;
    private String station;
    private String imgURL;
    private String password;
    /*
    * 需要动态显示个人信息的文本框
    * */
    private TextView mName;
    private TextView mSex;
    private TextView mAccount;
    private TextView mStation;
    private ImageView mPicture;

    //在消息队列中实现对控件的更改
    private Handler handle = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    System.out.println("111");
                    Bitmap bmp=(Bitmap)msg.obj;
                    mPicture.setImageBitmap(bmp);
                    break;
            }
        };
    };
    /*
     * 设置退出按钮
     * */
    private Button mExit;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_mine, container, false);
        Bundle bundle = getArguments();
        //这里就拿到了之前传递的参数
        account = bundle.getString("account");
        password = bundle.getString("password");
        //result = bundle.getString("result");
        name = bundle.getString("name");
        sex = bundle.getString("sex");
        station = bundle.getString("station");
        imgURL = bundle.getString("imgURL");
        //if(result.equals("用户信息不存在")){Toast T = Toast.makeText(mContext, "用户信息不存在",Toast.LENGTH_LONG);T.show() ;}
        //设置界面显示
        mName = view.findViewById(R.id.my_name);
        mSex = view.findViewById(R.id.my_sex);
        mAccount = view.findViewById(R.id.my_account);
        mStation = view.findViewById(R.id.my_station);//Toast T = Toast.makeText(mContext, name,Toast.LENGTH_LONG);T.show() ;
        mPicture = view.findViewById(R.id.my_picture);
        mName.setText(name);
        mSex.setText(sex);
        mAccount.setText(account);
        mStation.setText(station);
        try {
            //新建线程加载图片信息，发送到消息队列中
            new Thread(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    Bitmap bmp = getURLimage(imgURL);
                    Message msg = new Message();
                    msg.what = 0;
                    msg.obj = bmp;
                    System.out.println("000");
                    handle.sendMessage(msg);
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }

        /*退出登录*/
        mExit = view.findViewById(R.id.exit);
        mExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ExitRequest(account,password);
                {Toast T = Toast.makeText(view.getContext(), "bye (from Heimdallr)",Toast.LENGTH_LONG);T.show();}
                ActivityCollector.finishAll();
                getActivity().onBackPressed();
            }
        });

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
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
    public  void ExitRequest(final String account,final String password) {
        //请求地址
        String url = "http://39.105.175.211:8080/loginweb/ResetLoginStatusServlet?account="+account+"&password="+password+"&loginStatus=offline";
        String tag = "exit";
        //System.out.print("this!!!!");
        //取得请求队列
        RequestQueue requestQueue = Volley.newRequestQueue(this.getActivity());

        //防止重复请求，所以先取消tag标识的请求队列
        requestQueue.cancelAll(tag);

        //创建StringRequest，定义字符串请求的请求方式为POST(省略第一个参数会默认为GET方式)
        final StringRequest request = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    //@Override
                    public void onResponse(String response) {
                        try {
                            //Toast.makeText(LoginActivity.this,"here is that",Toast.LENGTH_LONG).show();
                            JSONObject jsonObject = (JSONObject) new JSONObject(response).get("params");
                            if(jsonObject.getString("Result").equals("修改成功"));


                        } catch (JSONException e) {
                            //做自己的请求异常操作，如Toast提示（“无网络连接”等）
                            Log.e("TAG", e.getMessage(), e);
                        }
                    }
                }, new Response.ErrorListener() {
            //@Override
            public void onErrorResponse(VolleyError error) {
                //做自己的响应错误操作，如Toast提示（“请稍后重试”等）
                Log.e("TAG", error.getMessage(), error);
            }
        }) {
            @Override
            protected Map<String, String> getParams()  {
                Map<String, String> params = new HashMap<>();
                params.put("Account", account);

                return params;
            }
        };

        //设置Tag标签
        request.setTag(tag);

        //将请求添加到队列中
        requestQueue.add(request);
    }


    /**
     * 从服务器获取图片信息并将图片保存在SDCard上
     */

    /**
     * 从服务器取图片
     *http://bbs.3gstdy.com
     * @param url
     * @return
     */
    //加载图片
    public Bitmap getURLimage(String url) {
        Bitmap bmp = null;
        try {
            URL myurl = new URL(url);
            // 获得连接
            HttpURLConnection conn = (HttpURLConnection) myurl.openConnection();
            conn.setConnectTimeout(6000);//设置超时
            conn.setDoInput(true);
            conn.setUseCaches(false);//不缓存
            conn.connect();
            InputStream is = conn.getInputStream();//获得图片的数据流
            bmp = BitmapFactory.decodeStream(is);
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bmp;
    }
    public void down_file(String url, String path) throws Exception {

        String filename = url.substring(url.lastIndexOf("/") + 1);
        /**
         * 处理中文路径 ：由于URLEncoder.encode会对'/'和':'进行转码，通过下面的代码可以避免这种错误
         */
        String[] strList = url.split("\\/");
        url = "";
        for (String mstr : strList) {
            if (mstr.contains(":")) {
                url = url + mstr + "/";
            } else {
                url = url + URLEncoder.encode(mstr, "utf-8") + '/';
            }
        }
        url = url.substring(0, url.length() - 1);
        Log.d("MineFragment", url);
        URL myURL = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) myURL.openConnection();
        conn.setConnectTimeout(5000);
        conn.setRequestMethod("GET");
        if (conn.getResponseCode() == 200) {
            InputStream inputStream = conn.getInputStream();
            File file = new File(path);
            if (!file.exists()) {
                file.mkdir();
            }
            FileOutputStream out = new FileOutputStream(path + filename);
            // 把数据存入路径+文件名
            byte buf[] = new byte[1024];
            do {
                // 循环读取
                int numread = inputStream.read(buf);
                if (numread == -1) {
                    break;
                }
                out.write(buf, 0, numread);
            } while (true);
            inputStream.close();
        }

    }




}
