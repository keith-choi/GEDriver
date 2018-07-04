package hk.com.granda_express.gedriver;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableArrayList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import hk.com.granda_express.gedriver.databinding.PickItemBinding;

/**
 * Created by keith on 10/21/2016.
 */

public class PickAdapter extends BaseAdapter {
    private ObservableArrayList<DriverOrder> list;
    private LayoutInflater inflater;

    public PickAdapter(ObservableArrayList<DriverOrder> l) {
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

        PickItemBinding binding = DataBindingUtil.inflate(inflater, R.layout.pick_item, viewGroup, false);
        binding.setOrder(list.get(i));

        return binding.getRoot();
    }
}
