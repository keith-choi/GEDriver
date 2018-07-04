package hk.com.granda_express.gedriver;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableArrayList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/**
 * Created by keith on 10/28/2016.
 */

public class DeliveryAdapter extends BaseAdapter {
    private ObservableArrayList<DriverOrder> list;
    private LayoutInflater inflater;

    public DeliveryAdapter(ObservableArrayList<DriverOrder> l) {
        list = l;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (inflater == null) {
            inflater = (LayoutInflater) viewGroup.getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        hk.com.granda_express.gedriver.databinding.DeliveryItemBinding binding = DataBindingUtil.inflate(inflater, R.layout.delivery_item, viewGroup, false);
        binding.setOrder(list.get(i));

        return binding.getRoot();
    }
}
