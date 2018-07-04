package hk.com.granda_express.gedriver;

import android.databinding.BindingAdapter;
import android.databinding.ObservableArrayList;
import android.widget.ListView;

/**
 * Created by keith on 10/21/2016.
 */

public class PickBinder {

    @BindingAdapter("bind:pick_items")
    public static void bindList(ListView view, ObservableArrayList<DriverOrder> list) {
        PickAdapter adapter = new PickAdapter(list);
        view.setAdapter(adapter);
    }
}
