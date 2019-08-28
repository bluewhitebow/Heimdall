package heimdallr.android;

import android.Manifest;
import android.app.AppOpsManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.MenuItem;
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

import java.util.HashMap;
import java.util.Map;
//di bian lan
public class MainMenuActivity extends FragmentActivity
        implements HomeFragment.OnFragmentInteractionListener ,
        NotificationFragment.OnFragmentInteractionListener ,
        MineFragment.OnFragmentInteractionListener,ButtonFragment.OnFragmentInteractionListener{

    private TextView mTextMessage;



    /**
     *线程同步锁
     */

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    //mTextMessage.setText("Home");
                    showButtonFragment();
                    return true;
                case R.id.navigation_dashboard:
                    //mTextMessage.setText("Notification");
                    showNotificationsFragment();
                    return true;
                case R.id.navigation_notifications:
                    //mTextMessage.setText("Mine");
                    showMineFragment();
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        Intent intent = this.getIntent();
        account = intent.getStringExtra("account");
        password = intent.getStringExtra("password");

        //mNotificationFragment = new NotificationFragment();
        UserInformationRequest();
        /**
         * 动态申请权限
         */
        if(Build.VERSION.SDK_INT >= 23){
            initPermission();
        }

        NotificationEnabled NotiEnable = new NotificationEnabled();
        if(!NotiEnable.isNotificationEnabled(this))
        {
            Toast.makeText(MainMenuActivity.this,"Please open the perssion of Notification!",Toast.LENGTH_LONG).show();
        }

    }


    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    //fragment之间的切换
    private HomeFragment mHomeFragment;
    private NotificationFragment mNotificationFragment;
    private MineFragment mMineFragment;
    private ButtonFragment mButtonFragment;

    public void showHomeFragment(){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        if(mHomeFragment == null){
            mHomeFragment = new HomeFragment();
            transaction.add(R.id.fragment_container,mHomeFragment);
        }
        hideFragment(transaction);
        transaction.show(mHomeFragment);
        transaction.commit();
    }

    public void showButtonFragment(){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        if(mButtonFragment == null){
            mButtonFragment= new ButtonFragment();

            transaction.add(R.id.fragment_container,mButtonFragment);
        }
        hideFragment(transaction);
        transaction.show(mButtonFragment);
        transaction.commit();
    }

    public void showNotificationsFragment(){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        //transaction.remove(mHomeFragment);
        if(mNotificationFragment == null){
            mNotificationFragment = new NotificationFragment();
            transaction.add(R.id.fragment_container,mNotificationFragment);
        }
        hideFragment(transaction);
        transaction.show(mNotificationFragment);
        transaction.commit();
    }
    public void showMineFragment(){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if(mMineFragment == null){
            mMineFragment = new MineFragment();

            Bundle bundle = new Bundle();
            bundle.putString("account",account);
            bundle.putString("password",password);
            bundle.putString("name",name);
            bundle.putString("sex",sex);
            bundle.putString("station",station);
            bundle.putString("imgURL",imgURL);
            mMineFragment.setArguments(bundle);
            transaction.add(R.id.fragment_container,mMineFragment);
        }
        hideFragment(transaction);
        transaction.show(mMineFragment);
        transaction.commit();
    }


    public void hideFragment(FragmentTransaction transaction){
        if(mHomeFragment != null){
            transaction.hide(mHomeFragment);
        }

        if(mNotificationFragment != null){
            transaction.hide(mNotificationFragment);
        }
        if(mMineFragment != null){
            transaction.hide(mMineFragment);
        }

        if(mButtonFragment!=null){
            transaction.hide(mButtonFragment);
        }
    }




    /*
    * For MineFragment to get UserInformation from Server
    * Including text information and picture
    * */
    private String result;
    private String name ;
    private String sex ;
    private String station ;
    private String imgURL ;
    private String account;
    private String password;
    public  void UserInformationRequest() {
        //请求地址
        String url = "http://39.105.175.211:8080/HeimdallrWeb/QueryUserInformationServlet?&account="+account;
        String tag = "getInformation";
        //System.out.print("this!!!!");
        //取得请求队列
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());

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
                            result = "null";
                            name = "null";
                            sex = "null";
                            station = "null";
                            imgURL = "null";
                            //result = jsonObject.getString("result");
                            name = jsonObject.getString("name");
                            sex = jsonObject.getString("sex");
                            station = jsonObject.getString("station");
                            imgURL = jsonObject.getString("imgURL");
                            //Toast T = Toast.makeText(MainMenuActivity.this, name,Toast.LENGTH_LONG);T.show() ;

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



    @Override
    protected void onDestroy(){
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }

    /**Android 6.0需要动态申请权限
     * 权限的结果回调函数
     */
    private boolean flag;
    private void initPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //检查权限
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(this,Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED
            ) {
                //请求权限
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA}, 1);
            } else {
                flag = true;
            }
        } else {
            flag = true;
        }
    }
    /**
     * 权限的结果回调函数
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            flag = grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED
                    && grantResults[2] == PackageManager.PERMISSION_GRANTED
                    && grantResults[3] == PackageManager.PERMISSION_GRANTED;
        }
    }

    /***
     * 动态获取通知权限
     */



}
