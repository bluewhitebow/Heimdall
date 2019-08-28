package heimdallr.android;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SearchView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

/**
 * 待做部分：
 * 1.记录对象生成的地点
 * 2.一个List用于存储拍照的结果并动态显示,序列化存储(做了,还新增了删除按钮)
 * 3.数据库交互
 * 4.暴露接口
 */

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link NotificationFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link NotificationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NotificationFragment extends Fragment implements HeimdallrAdapter.Callback {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    //内部私有变量
    private SearchView searchView;
    private ListView listView;
    private View view;
    private HeimdallrAdapter adapter = null;
    private Noti_Functions noti_functions;
    private static String TAG = "MainMenuActivity";
    //共有变量
    //本地存储对象
    private static ArrayList<Time_and_Card> data_local = new ArrayList<>();

    public NotificationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment NotificationFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static NotificationFragment newInstance(String param1, String param2) {
        NotificationFragment fragment = new NotificationFragment();
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

        EventBus.getDefault().register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_notification, container, false);
        Init();
        return view;
    }

    private void Init() {

        searchView = (SearchView) view.findViewById(R.id.Noti_searchView);
        listView = (ListView) view.findViewById(R.id.Noti_listView);

        noti_functions = new Noti_Functions(getActivity(),TAG,this);
        adapter = new HeimdallrAdapter(getActivity(), data_local, this);

        //每次生成碎片先加载本地文件
        Object judg = noti_functions.Dserizable();
        if (judg != null)
            for (Time_and_Card t : noti_functions.Dserizable())
                data_local.add(t);
        //绑定数据
        listView.setAdapter(adapter);
        //listview启动过滤
        listView.setTextFilterEnabled(true);
        //一开始不显示
        //listView.setVisibility(View.GONE);
        //搜索框不自动缩小为一个搜索图标，而是match_parent
        searchView.setIconifiedByDefault(false);
        //显示搜索按钮
        searchView.setSubmitButtonEnabled(true);
        //默认提示文本
        searchView.setQueryHint("查找");
        // 设置搜索文本监听
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            // 当点击搜索按钮时触发该方法
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            // 当搜索内容改变时触发该方法
            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                listView.setVisibility(View.VISIBLE);
                return false;
            }
        });

    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
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
        if(EventBus.getDefault().isRegistered(this))
        {
            EventBus.getDefault().unregister(this);
        }
    }

    //接口方法，响应ListView Delete按钮点击事件
    @Override
    public void click(View v) {
        if (adapter == null)
            return;
        Time_and_Card delete;
        delete = (Time_and_Card) adapter.getItem((Integer) v.getTag());
        Iterator<Time_and_Card> it = data_local.iterator();
        while(it.hasNext()) {
            Time_and_Card value = it.next();
            if(value.getCard_num().equals(delete.getCard_num()) && value.getTime().equals(delete.getTime())) {
                it.remove();
            }
        }
        if(noti_functions != null)
            noti_functions.serizable(data_local);
        adapter.notifyDataSetChanged();
    }

    //Bus订阅处理
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void Event(MessageEvent messageEvent)
    {
        DealMessageFromCamera(messageEvent.getMessage());
    }

    //当主摄像头字符串来了，就执行功能
    private void DealMessageFromCamera(String cardNums) {
        if(noti_functions == null)
            return;
        //此处应执行SQL查询
        //若车被通缉,修改状态
        String[] plates = cardNums.split(",");
        for(String plate : plates) {
            noti_functions.QueryRequestViolation(plate);
            Log.d(TAG, "coming plates is "+plate);
        }
    }

    public void Triger_inllegal(String cardNum)
    {
        if(cardNum == null)
            return;

        Time_and_Card  comingcar =  new Time_and_Card(cardNum,noti_functions.getBestLocation());
        //JugeIsIn
        comingcar.SetPenal(true);
        noti_functions.Notificate(comingcar.getCard_num(), comingcar.getTime());

        if(noti_functions != null) {
            noti_functions.CheckList(data_local, comingcar);
            noti_functions.serizable(data_local);
        }

        if (adapter != null)
            adapter.notifyDataSetChanged();
    }

    public void Triger_llegal(String cardNum)
    {
        if(cardNum == null)
            return;

        Time_and_Card  comingcar =  new Time_and_Card(cardNum,noti_functions.getBestLocation());
        //JugeIsIn
        if(noti_functions != null) {
            noti_functions.CheckList(data_local, comingcar);
            noti_functions.serizable(data_local);
        }

        if (adapter != null)
            adapter.notifyDataSetChanged();
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

//该类用来记录对应车辆出现的时间
//传入参数只有车牌,自动记录时间
//传入参数还包括了汽车行使过的经度和纬度
class Time_and_Card implements Serializable {
    private String card_num;
    private String time;
    private String IsPenal;
    private String Position;

    public Time_and_Card(String card_num,String Position) {
        this.card_num = card_num;
        this.time = getCurrentTime();
        this.Position = Position;
        IsPenal = "";
    }

    private String getCurrentTime() {
        Calendar calendar = Calendar.getInstance();
        //年
        int year = calendar.get(Calendar.YEAR);
        //月
        int month = calendar.get(Calendar.MONTH) + 1;
        //日
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        //小时
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        //分钟
        int minute = calendar.get(Calendar.MINUTE);
        //秒
        int second = calendar.get(Calendar.SECOND);

        return year + "-" + month + "-" + day + "-" + hour + ":" + minute + ":" + second;

    }

    public void SetPenal(boolean value) {
        if (value)
            IsPenal = "!!";
        else
            IsPenal = "";
    }

    public String getCard_num() {
        return card_num;
    }

    public String getTime() {
        return time;
    }

    public String isPenal() {
        return IsPenal;
    }

    public String getPosition() {return Position; }

    public  void ResetAll(Time_and_Card value)
    {
        this.Position = value.getPosition();
        this.time = value.getTime();
        this.IsPenal =  value.isPenal();
    }

}

/**
 * 1.实现本地序列化和反序列化
 * 2.实现定位功能
 */
class Noti_Functions {
    private static String mSDCARD = null;
    private Context context;
    private String TAG;
    private NotificationFragment NotiFragment;

    public Noti_Functions(Context context,String TAG,NotificationFragment NotiFragment)
    {
        this.context = context;
        this.TAG = TAG;
        this.NotiFragment = NotiFragment;
    }

    public void CheckList(List<Time_and_Card> arrayList,Time_and_Card value)
    {
        Iterator<Time_and_Card> it = arrayList.iterator();
        for(Time_and_Card val:arrayList)
        {
            if(val.getCard_num().equals(value.getCard_num()))
            {
                val.ResetAll(value);
                return;
            }
        }
        arrayList.add(value);
    }

    public void serizable(List<Time_and_Card> arrayList) {
        String filenameString = getHcExternalFile("resouces", "/object");
        final File file = new File(filenameString);
        try {
            FileOutputStream fis = new FileOutputStream(file);
            ObjectOutputStream ois = new ObjectOutputStream(fis);
            ois.writeObject(arrayList);
            ois.close();
            fis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.d(TAG, "store File NotFound");
        } catch (StreamCorruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Time_and_Card> Dserizable() {
        String filenameString = getHcExternalFile("resouces", "/object");
        final File file = new File(filenameString);
        try {
            FileInputStream fis = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(fis);
            ArrayList<Time_and_Card> arrayList = (ArrayList<Time_and_Card>) ois.readObject();
            ois.close();
            fis.close();
            if (arrayList == null) {
                Log.d(TAG, "restore Error");
            }

            return arrayList;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.d(TAG, "restore File NotFound");
        } catch (StreamCorruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    public String getHcExternalFile(String subFolder, String fileName) {
        try {
            String subFolderPath = getSDCardPath() + "/Heimdallr/"
                    + subFolder + "/";
            File fi = new File(subFolderPath);
            if (!fi.exists()) {
                fi.mkdirs();
                Log.d(TAG, "Create Folder :" + fi.getAbsolutePath());
            }
            return fi.getAbsolutePath() + fileName;
        } catch (Exception exp) {
            return null;
        }
    }

    public String getSDCardPath() {
        if (mSDCARD == null) {
            mSDCARD = Environment.getExternalStorageDirectory().toString();
            if (mSDCARD == null) {
                mSDCARD = "/sdcard";
            }
        }
        return mSDCARD;
    }

    /**
     * 通过GPS获取定位信息
     */
    public String getGPSLocation() {
        String result;
        Location gps = LocationUtils.getGPSLocation(context);
        if (gps == null) {
            //设置定位监听，因为GPS定位，第一次进来可能获取不到，通过设置监听，可以在有效的时间范围内获取定位信息
            LocationUtils.addLocationListener(context, LocationManager.GPS_PROVIDER, new LocationUtils.ILocationListener() {
                @Override
                public void onSuccessLocation(Location location) {
                    if (location != null) {
                        Log.d(TAG,  "gps onSuccessLocation location:  lat==" + location.getLatitude() + "     lng==" + location.getLongitude());
                    } else {
                        Log.d(TAG, "gps location is null");
                    }
                }
            });
            result = null;
        } else {
            double Latitube,Longitude;
            Latitube = gps.getLatitude();
            Longitude = gps.getLongitude();
            Log.d(TAG, "gps location: lat==" + Latitube + "  lng==" + Longitude);
            result = "经度:" + String.format("%.3f", Latitube) + "  "+"纬度：" + String.format("%.3f", Longitude);
        }

        return result;
    }

    /**
     * 通过网络等获取定位信息
     */
    public String getNetworkLocation() {
        String result;
        Location net = LocationUtils.getNetWorkLocation(context);
        if (net == null) {
            result = null;
            Log.d(TAG, "net location is null");
        } else {
            double Latitube,Longitude;
            Latitube = net.getLatitude();
            Longitude = net.getLongitude();
            Log.d(TAG, "network location: lat==" + Latitube + "  lng==" + Longitude);
            result = "经度:" + String.format("%.3f", Latitube) + "  "+"纬度：" + String.format("%.3f", Longitude);
        }
        return result;
    }

    /**
     * 采用最好的方式获取定位信息
     */
    public String getBestLocation() {
        String result;
        Criteria c = new Criteria();//Criteria类是设置定位的标准信息（系统会根据你的要求，匹配最适合你的定位供应商），一个定位的辅助信息的类
        c.setPowerRequirement(Criteria.POWER_LOW);//设置低耗电
        c.setAltitudeRequired(true);//设置需要海拔
        c.setBearingAccuracy(Criteria.ACCURACY_COARSE);//设置COARSE精度标准
        c.setAccuracy(Criteria.ACCURACY_LOW);//设置低精度
        //... Criteria 还有其他属性，就不一一介绍了
        Location best = LocationUtils.getBestLocation(context, c);
        if (best == null) {
            Log.d(TAG, " best location is null");
            result = null;
        } else {
            double Latitube,Longitude;
            Latitube = best.getLatitude();
            Longitude = best.getLongitude();
            Log.d(TAG, "best location: lat==" + Latitube + " lng==" + Longitude);
            result = "经度:" + String.format("%.3f", Latitube) + "  "+"纬度：" + String.format("%.3f", Longitude);
        }
        return result;
    }

    //此处用来进行消息通知
    public void Notificate(String CardNum,String time)
    {
        //点击通知栏消息跳转页
        //Intent intent = new Intent(context, NotificationClickReceiver.class);
        //PendingIntent pendingIntent =PendingIntent.getBroadcast(context, 0, intent, 0);
        //创建通知消息管理类
        Notification notification;
        NotificationCompat.Builder builder;
        NotificationManager manager;

        /**Android 8.0 消息栏通知解决
         * */
        String ChannelId =null;
        manager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            ChannelId ="JoJo";
            NotificationChannel channel =new NotificationChannel(ChannelId,
                    "Channel1", NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(channel);
            builder = new NotificationCompat.Builder(context,ChannelId);//创建通知消息实例
        }else {
            builder = new NotificationCompat.Builder(context);//创建通知消息实例
        }

        builder.setContentTitle("出现可疑车辆");
        builder.setContentText(CardNum+":"+" "+time);
        builder.setWhen(System.currentTimeMillis());//通知栏显示时间
        builder.setSmallIcon(R.mipmap.ic_launcher);//通知栏小图标
        builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher));//通知栏下拉是图标
        builder.setPriority(NotificationCompat.PRIORITY_MAX);//设置通知消息优先级
        builder.setAutoCancel(true);//设置点击通知栏消息后，通知消息自动消失
        builder.setVibrate(new long[]{0, 1000, 1000, 1000}); //通知栏消息震动
        builder.setLights(Color.GREEN, 1000, 2000) ;//通知栏消息闪灯(亮一秒间隔两秒再亮)
        builder.setDefaults(NotificationCompat.DEFAULT_ALL);//通知栏提示音、震动、闪灯等都设置为默认
        //builder.setSound(Uri.parse("")) ; 铃声,传入铃声的 Uri（可以本地或网上）

        notification = builder.build();
        manager.notify(1, notification);
    }


    //SQL查询
    public void QueryRequestViolation(final String cardNum) {
        //请求地址
        String url = "http://39.105.175.211:8080/HeimdallrWeb/QueryViolationRecordServlet?&numberPlate="+cardNum;
        String tag = "QueryVoilation";
        //取得请求队列
        RequestQueue requestQueue = Volley.newRequestQueue(context);

        //防止重复请求，所以先取消tag标识的请求队列
        requestQueue.cancelAll(tag);

        //创建StringRequest，定义字符串请求的请求方式为POST(省略第一个参数会默认为GET方式)
        final StringRequest request = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    //@Override
                    public void onResponse(String response) {
                        try {
                            //Toast.makeText(LoginActivity.this,"here is that",Toast.LENGTH_LONG).show();
                            JSONObject jsonObject = (JSONObject) new JSONObject(response).get("1");
                            String result = jsonObject.getString("result");
                            Log.d(TAG, result);
                            if(result.equals("记录不存在")){
                                NotiFragment.Triger_llegal(cardNum);
                                Log.d(TAG, "unViolation cardNum is "+cardNum);
                            }
                            else if(result.equals("记录存在")) {
                                NotiFragment.Triger_inllegal(cardNum);
                                Log.d(TAG, "Violation cardNum is "+cardNum);
                            }
                        } catch (JSONException e) {
                            //做自己的请求异常操作，如Toast提示（“无网络连接”等）
                            Log.d(TAG, e.getMessage(), e);
                        }
                    }
                }, new Response.ErrorListener() {
            //@Override
            public void onErrorResponse(VolleyError error) {
                //做自己的响应错误操作，如Toast提示（“请稍后重试”等）
                Log.d(TAG, error.getMessage(), error);
            }
        });
        //设置Tag标签
        request.setTag(tag);

        //将请求添加到队列中
        requestQueue.add(request);
    }

    //SQL更新
    public void UpdateViolation(Time_and_Card card) {
        //请求地址
        String url = "http://yanhuoduiyingshang.art:8080/HeimdallrWeb/UpdataPathInformation?&numberPlate="+card.getCard_num()+"&time="+card.getTime()+"&longitudeLatitude="+card.getPosition();
        String tag = "UpdataVoilation";
        //取得请求队列
        RequestQueue requestQueue = Volley.newRequestQueue(context);

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
                            String result = jsonObject.getString("result");
                            if(result.equals("添加成功")) {
                                Log.d(TAG, "更新数据库 ");
                            }
                            else {
                            }

                        } catch (JSONException e) {
                            //做自己的请求异常操作，如Toast提示（“无网络连接”等）
                            Log.d(TAG, e.getMessage(), e);
                        }
                    }
                }, new Response.ErrorListener() {
            //@Override
            public void onErrorResponse(VolleyError error) {
                //做自己的响应错误操作，如Toast提示（“请稍后重试”等）
                Log.d(TAG, error.getMessage(), error);
            }
        });
        //设置Tag标签
        request.setTag(tag);

        //将请求添加到队列中
        requestQueue.add(request);
    }
}

