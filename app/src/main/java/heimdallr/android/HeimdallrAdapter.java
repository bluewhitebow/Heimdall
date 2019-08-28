package heimdallr.android;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class HeimdallrAdapter extends BaseAdapter implements Filterable,OnClickListener{
Context context;
List<Time_and_Card> data; //这个数据是会改变的，所以要有个变量来备份一下原始数据
List<Time_and_Card> backData;//用来备份原始数据
MyFilter mFilter ;
private Callback mCallback;

public HeimdallrAdapter(Context context, List<Time_and_Card> data, Callback callback) {
    this.context = context;
    this.data = data;
    backData = data;
    mCallback = callback;
}

public void SetData(List<Time_and_Card> data)
{
    this.data = data;
    backData = data;
}


public interface Callback {
    public void click(View v);
}

//响应按钮点击事件,调用子定义接口，并传入View
@Override
public void onClick(View v) {
    mCallback.click(v);
}

@Override
public int getCount() {
    return data.size();
}

@Override
public Object getItem(int i) { return data.get(i);}

@Override
public long getItemId(int i) {
    return i;
}

@Override
public View getView(int i, View view, ViewGroup viewGroup) {

    if (view ==null){
        view = LayoutInflater.from(context).inflate(R.layout.fragment_notification_item,null);
    }

    TextView Position = (TextView) view.findViewById(R.id.Noti_text4);
    TextView cardNum = (TextView) view.findViewById(R.id.Noti_text1);
    TextView time = (TextView) view.findViewById(R.id.Noti_text2);
    TextView IsPenel = (TextView) view.findViewById(R.id.Noti_text3);
    Button delete = (Button) view.findViewById(R.id.Noti_button);

    delete.setOnClickListener(this);
    delete.setTag(i);
    Position.setText(data.get(i).getPosition());
    IsPenel.setText(data.get(i).isPenal());
    cardNum.setText(data.get(i).getCard_num());
    time.setText(data.get(i).getTime());
    return view;
}

//当ListView调用setTextFilter()方法的时候，便会调用该方法
@Override
public Filter getFilter() {
    if (mFilter ==null){
        mFilter = new MyFilter();
    }
    return mFilter;
}

//需要定义一个过滤器的类来定义过滤规则
class MyFilter extends Filter{
    //在performFiltering(CharSequence charSequence)这个方法中定义过滤规则
    @Override
    protected FilterResults performFiltering(CharSequence charSequence) {
        FilterResults result = new FilterResults();
        List<Time_and_Card> list ;
        if (TextUtils.isEmpty(charSequence)){
            //当过滤的关键字为空的时候，显示所有的数据
            list  = backData;
        }else {
            //否则把符合条件的数据对象添加到集合中
            list = new ArrayList<>();
            for (Time_and_Card item:backData){
                if (item.getCard_num().contains(charSequence)||item.getTime().contains(charSequence)){
                    Log.d("MainMenuActivity","performFiltering:"+item.toString());
                    list.add(item);
                }

            }
        }
        result.values = list; //将得到的集合保存到FilterResults的value变量中
        result.count = list.size();//将集合的大小保存到FilterResults的count变量中

        return result;
    }

    //在publishResults方法中告诉适配器更新界面
    @Override
    protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
        data = (List<Time_and_Card>)filterResults.values;
        Log.d("MainMenuActivity","publishResults:"+filterResults.count);
        if (filterResults.count>0){
            notifyDataSetChanged();//通知数据发生了改变
            Log.d("MainMenuActivity","publishResults:notifyDataSetChanged");
        }else {
            notifyDataSetInvalidated();//通知数据失效
            Log.d("MainMenuActivity","publishResults:notifyDataSetInvalidated");
        }
    }

}
}