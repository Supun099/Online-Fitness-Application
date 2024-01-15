package ahmux.nutritionpoint.adapter;


import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import ahmux.nutritionpoint.R;
import ahmux.nutritionpoint.retrofit.NearByPlacesBean;

/*
Modified by Dhanushka Silva
On behalf of ICan Lanka Mobile team
14-11-2022
*/

public class PoiInfoAdapter extends CustomBaseAdapter<NearByPlacesBean.Result> {
    private int select = 0;
    private String selectUid = "";
    private List<NearByPlacesBean.Result> mList;
    private onChoiceListener choiceListener;

    public PoiInfoAdapter(Activity context, List<NearByPlacesBean.Result> list) {
        super(context, list);
        this.mList = list;
    }

    public class ViewHolder {
        TextView tvName;
        TextView tvAddress;
        ImageView ivSelect;
        RelativeLayout rlItem;

        ViewHolder(View view) {
            tvName = view.findViewById(R.id.tv_name);
            tvAddress = view.findViewById(R.id.tv_address);
            ivSelect = view.findViewById(R.id.iv_select);
            rlItem = view.findViewById(R.id.rl_item);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.item_poi_search, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        NearByPlacesBean.Result item = (NearByPlacesBean.Result) getItem(position);
        holder.tvName.setText(item.getName());
        holder.tvAddress.setText(item.getVicinity());
        holder.ivSelect.setEnabled(select == position);

        holder.rlItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                select = position;
                if (choiceListener != null) {
                    choiceListener.onChoice(position, item);
                }
                notifyDataSetChanged();
            }
        });

        return convertView;
    }

    public void setOnChoiceListener(onChoiceListener choiceListener) {
        this.choiceListener = choiceListener;
    }

    public void setSelectUid(String uid) {
        this.selectUid = uid;
    }

    public String getSelectUid() {
        return selectUid;
    }

    public interface onChoiceListener {
        void onChoice(int position, NearByPlacesBean.Result item);
    }

    public void setSelect(int select) {
        this.select = select;
    }

    public int getSelect() {
        return select;
    }

    public String getTitle() {
        return mList.get(select).getName();
    }

    public String getAddress() {
        NearByPlacesBean.Result item = mList.get(select);
        return item.getVicinity();
    }

    public double getLatitude() {
        if (mList.get(select).getGeometry().getLocation().getLat() != 0) {
            return mList.get(select).getGeometry().getLocation().getLat();
        } else {
            return 0;
        }
    }

    public double getLongitude() {
        if (mList.get(select).getGeometry().getLocation().getLng() != 0) {
            return mList.get(select).getGeometry().getLocation().getLng();
        } else {
            return 0;
        }
    }
}
