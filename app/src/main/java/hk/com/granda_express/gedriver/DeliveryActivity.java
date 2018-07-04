package hk.com.granda_express.gedriver;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;

import hk.com.granda_express.gedriver.databinding.ActivityDeliveryBinding;

public class DeliveryActivity extends AppCompatActivity {
    DriverOrderInfo orders;
    String driverName;
    String deliveryDate;
    ListView orderList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final ActivityDeliveryBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_delivery);
        orderList = binding.orderList;
        binding.noteid.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                 orderList.setSelection(orders.getOrderPos(charSequence.toString()));
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        binding.orderList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getApplicationContext(), ReceiptActivity.class);
                intent.putExtra("orderIndex", i);
                intent.putExtra("driverName", driverName);
                startActivity(intent);
            }
        });
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        Bundle extras = getIntent().getExtras();
        driverName = extras.getString("driverName");
        deliveryDate = extras.getString("deliveryDate");

        if (!DataHolder.getInstance().isEmpty()) {
            orders = new DriverOrderInfo(DataHolder.getInstance().getDriverOrders(), driverName);
            binding.setInfo(orders);
        } else {
            finish();
        }
        setTitle("送貨：" + deliveryDate);
    }

    @Override
    protected void onResume() {
        super.onResume();
        orderList.invalidateViews();
    }
}