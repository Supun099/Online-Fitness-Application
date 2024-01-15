package ahmux.nutritionpoint.adapter;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

/**
 * 类说明:adapter的基类
 */
public abstract class CustomBaseAdapter<T> extends BaseAdapter {


    public Activity mContext;
    private List<T> mList;
    public List<T> getDataList() {
        return mList;
    }

    public CustomBaseAdapter(Activity context, List<T> list) {
        super();
        this.mContext = context;
        this.mList = list;
    }

    public void setData(List<T> list) {
        if (list != null && list.size() > 0) {
            mList.clear();
            mList.addAll(list);
            notifyDataSetChanged();
        }
    }

    public void addData(List<T> list) {
        if (list != null && list.size() > 0) {
            mList.addAll(list);
            notifyDataSetChanged();
        }
    }

    @Override
    public int getCount() {
        return mList == null ? 0 : mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public abstract View getView(int position, View convertView, ViewGroup parent);

    public void startActivity(Intent intent) {
        if (intent != null) {
            mContext.startActivity(intent);
        }
    }

}
