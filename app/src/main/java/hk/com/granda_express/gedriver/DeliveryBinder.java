package hk.com.granda_express.gedriver;

import android.databinding.BindingAdapter;
import android.databinding.ObservableArrayList;
import android.widget.ListView;

/**
 * Created by keith on 10/28/2016.
 */

public class DeliveryBinder {

    @BindingAdapter("bind:delivery_items")
    public static void bindList(ListView view, ObservableArrayList<DriverOrder> list) {
        DeliveryAdapter adapter = new DeliveryAdapter(list);
        view.setAdapter(adapter);
    }}
