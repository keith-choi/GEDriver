package hk.com.granda_express.gedriver;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class CartonActivity extends AppCompatActivity {
    private Ringtone ringtone;
    private String orderId;
    private String cartonNo;
    private Order order;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carton);

        this.setTitle(R.string.title_activity_carton);

        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        ringtone= RingtoneManager.getRingtone(getApplicationContext(), notification);
        ringtone.play();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create();
            this.order = gson.fromJson(extras.getString("Order"), Order.class);

            StringBuilder sb = new StringBuilder(this.order.SenderName);
            if (!TextUtils.isEmpty(this.order.SenderCompany)) {
                sb.append("\n" + this.order.SenderCompany);
            }
            if (!TextUtils.isEmpty(this.order.SenderPhoneNo)) {
                sb.append("\n" + this.order.SenderPhoneNo);
            }
            if (!TextUtils.isEmpty(this.order.SenderAddress)) {
                sb.append("\n" + this.order.SenderAddress);
            }
            ((TextView)findViewById(R.id.senderView)).setText(sb.toString());

            ((TextView)findViewById(R.id.descriptionView)).setText(this.order.Description);

            sb = new StringBuilder(this.order.DeliveryAddress);
            if (!TextUtils.isEmpty(this.order.DeliveryCompany)) {
                sb.append("\n" + this.order.DeliveryCompany);
            }
            if (!TextUtils.isEmpty(this.order.DeliveryContact)) {
                sb.append("\n" + this.order.DeliveryContact);
            }
            if (!TextUtils.isEmpty(this.order.DeliveryPhoneNo)) {
                sb.append("\n" + this.order.DeliveryPhoneNo);
            }
            if (!TextUtils.isEmpty(this.order.CustomerCode)) {
                sb.append("\n" + this.order.CustomerCode);
            }
            ((TextView)findViewById(R.id.deliveryView)).setText(sb.toString());

            TextView qtyView = (TextView)findViewById(R.id.qtyView);
            qtyView.setText(Integer.toString(this.order.Qty));

            TextView deliveryMethodView = (TextView)findViewById(R.id.deliveryMethodView);
            deliveryMethodView.setText(this.order.PickupMethod == 1 ? "自提" : "送货");

            TextView remarksView = (TextView)findViewById(R.id.remarksView);
            remarksView.setText(this.order.Remarks);

            TextView weightView = (TextView)findViewById(R.id.weightView);
            weightView.setText(Float.toString(this.order.Weight));

            TextView actualWeightView = (TextView)findViewById(R.id.actualWeightView);
            actualWeightView.setText(Float.toString(this.order.Weight));

            String status = "";
            if (this.order.WeightTime != null) {
                status = "貨件于" + android.text.format.DateFormat.format("yyyy-MM-dd hh:mm", this.order.WeightTime) + ", 由" + this.order.WeightBy + "磅重";
                ((TextView)findViewById(R.id.statusView)).setText(status);
            }
        }
    }

    @Override
    protected void onDestroy() {
        ringtone.stop();
        super.onDestroy();
    }
}
