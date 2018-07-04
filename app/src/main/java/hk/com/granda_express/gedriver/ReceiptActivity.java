package hk.com.granda_express.gedriver;

import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Vibrator;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat;
import android.text.Editable;
import android.text.Layout;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class ReceiptActivity extends AppCompatActivity {
    Spinner phoneNos;
    EditText qrCode;
    TextView totalDelivered;
    Ringtone rt;

    DriverOrderInfo ordersInfo;
    DriverOrder order;

    String driverName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            driverName = extras.getString("driverName");
            int itemIndex = extras.getInt("orderIndex");
            order = DataHolder.getInstance().getDriverOrders().get(itemIndex);

            setTitle("客號：" + order.CustomerCode);
            StringBuilder sb = new StringBuilder(order.Address);
            if (!TextUtils.isEmpty(order.CompanyName)) {
                sb.append("\n" + order.CompanyName);
            }
            if (!TextUtils.isEmpty(order.Contact)) {
                sb.append("\n" + order.Contact);
            }
            ((TextView) findViewById(R.id.customerInfo)).setText(sb.toString());
            phoneNos = (Spinner) findViewById(R.id.phoneNos);
            ArrayList<String> noList = new ArrayList<String>(Arrays.asList(order.PhoneNo.split(",")));
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item, noList);
            phoneNos.setAdapter(adapter);

            float amount = order.OverSizeAmount;
            if (order.PaymentMethod == 0) {
                amount += order.Amount + order.AdditionalAmount;
            }
            if (amount > 0) {
                ((TextView) findViewById(R.id.orderAmount)).setText("$" + Float.toString(amount));
            } else {
                ((LinearLayout) findViewById(R.id.amountRow)).setVisibility(LinearLayout.GONE);
            }

            DecimalFormat nf = (DecimalFormat)NumberFormat.getNumberInstance();
            nf.setDecimalSeparatorAlwaysShown(true);
            nf.setMinimumFractionDigits(2);
            nf.setMaximumFractionDigits(2);
            if (order.CollectAmount != 0) {
                ((TextView) findViewById(R.id.collectAmount)).setText("人民幣：" + nf.format(order.CollectAmount) + "\n或港幣：" + nf.format(order.CollectAmount2));
            } else {
                ((LinearLayout) findViewById(R.id.collectAmountRow)).setVisibility(LinearLayout.GONE);
            }

            ((TextView) findViewById(R.id.totalCount)).setText(Integer.toString(order.Picked));
            totalDelivered = (TextView) findViewById(R.id.totalDelivered);
            totalDelivered.setText(Integer.toString(order.Delivered));
            ((TextView)findViewById(R.id.tvOverSizeAmount)).setText(Float.toString(order.OverSizeAmount));

            if (order.RxCash + order.RxCheque + order.RxYen == 0) {
                ((EditText) findViewById(R.id.edtCash)).setText(Float.toString(order.Amount + order.AdditionalAmount + order.CollectAmount2));
            } else {
                ((EditText) findViewById(R.id.edtCash)).setText(Float.toString(order.RxCash));
            }
            ((EditText)findViewById(R.id.edtCheque)).setText(Float.toString(order.RxCheque));
            ((EditText)findViewById(R.id.edtYen)).setText(Float.toString(order.RxYen));
            ((EditText)findViewById(R.id.edtCompensate)).setText(Float.toString(order.RxCompensation));
            ((EditText)findViewById(R.id.edtCarpark)).setText(Float.toString(order.CarparkCost));
            ((EditText)findViewById(R.id.edtRegistration)).setText(Float.toString(order.RegistrationCost));
            ((EditText)findViewById(R.id.edtInStore)).setText(Float.toString(order.InStoreCost));

            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

            ordersInfo = new DriverOrderInfo(DataHolder.getInstance().getDriverOrders(), driverName);

            qrCode = (EditText) findViewById(R.id.editQRCode);
            qrCode.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    if (charSequence.length() > 37) {
                        if (rt.isPlaying()) {
                            rt.stop();
                        }
                        processCartonId(charSequence.toString());
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });
        }

        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        rt = RingtoneManager.getRingtone(getApplicationContext(), notification);
    }

    private void processCartonId(String cartonId) {
        if (ordersInfo.checkDelivered(cartonId)) {
            totalDelivered.setText(Integer.toString(order.Delivered));
            qrCode.setHint("@string/qr_hint");
        } else {
            qrCode.setHint("取錯貨件!");
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(1000);
            rt.play();
        }
        qrCode.setText("");
    }

    public void makeCall(View view) {
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + phoneNos.getSelectedItem().toString()));
        startActivity(intent);
    }

    public void payCash(View view) {
        EditText editText1 = (EditText)findViewById(R.id.edtCash);
        EditText editText2 = (EditText)findViewById(R.id.edtCheque);
        editText1.setText(Float.toString(order.Amount + order.AdditionalAmount + order.OverSizeAmount));
        editText2.setText("0.0");
    }

    public void payCheque(View view) {
        EditText editText1 = (EditText)findViewById(R.id.edtCash);
        EditText editText2 = (EditText)findViewById(R.id.edtCheque);
        editText1.setText("0.0");
        editText2.setText(Float.toString(order.Amount + order.AdditionalAmount + order.OverSizeAmount));
    }

    public void payCollectYen(View view) {
        EditText editText = (EditText)findViewById(R.id.edtYen);
        editText.setText(Float.toString(order.CollectAmount));
    }

    public void payCollectHK(View view) {
        EditText editText1 = (EditText)findViewById(R.id.edtCash);
        EditText editText2 = (EditText)findViewById(R.id.edtYen);
        Float cash = Float.parseFloat(editText1.getText().toString());
        editText1.setText(Float.toString(cash + order.CollectAmount2));
        editText2.setText("0.0");
    }

    public void payCollectCheque(View view) {
        EditText editText1 = (EditText)findViewById(R.id.edtCheque);
        EditText editText2 = (EditText)findViewById(R.id.edtYen);
        Float cash = Float.parseFloat(editText1.getText().toString());
        editText1.setText(Float.toString(cash + order.CollectAmount2));
        editText2.setText("0.0");
    }

    public void saveOrder(View view) {
        order.RxCompensation = Float.parseFloat(((EditText) findViewById(R.id.edtCompensate)).getText().toString());
        order.CarparkCost = Float.parseFloat(((EditText) findViewById(R.id.edtCarpark)).getText().toString());
        order.RegistrationCost = Float.parseFloat(((EditText) findViewById(R.id.edtRegistration)).getText().toString());
        order.InStoreCost = Float.parseFloat(((EditText) findViewById(R.id.edtInStore)).getText().toString());
        order.RxCash = Float.parseFloat(((EditText)findViewById(R.id.edtCash)).getText().toString());
        order.RxCheque = Float.parseFloat(((EditText)findViewById(R.id.edtCheque)).getText().toString());
        order.RxYen = Float.parseFloat(((EditText)findViewById(R.id.edtYen)).getText().toString());
        order.RxCompensation = Float.parseFloat(((EditText)findViewById(R.id.edtCompensate)).getText().toString());
        order.DeliverTime = new Date();

        ArrayList<Carton> cartons = new ArrayList<Carton>();
        for (int i = 0; i < order.DeliveryItems.size(); i++) {
            for (int j = 0; j < order.DeliveryItems.get(i).Cartons.size(); j++) {
                DriverOrderItem deliveryItem = order.DeliveryItems.get(i);
                Carton carton = deliveryItem.Cartons.get(j);
                carton.Cash = order.RxCash;
                carton.Cheque = order.RxCheque;
                carton.CollectAmount = order.RxYen;
                carton.Compensation = order.RxCompensation;
                carton.CarparkCost = order.CarparkCost;
                carton.RegistrationCost = order.RegistrationCost;
                carton.InStoreCost = order.InStoreCost;
                carton.DeliverTime = new Date();
                cartons.add(carton);
            }
        }
        if (cartons.size() == 0) {
            Carton ctn = new Carton();
            ctn.id = 0;
            ctn.OrderId = order.id;
            ctn.Cash = order.RxCash;
            ctn.Cheque = order.RxCheque;
            ctn.CollectAmount = order.RxYen;
            ctn.Compensation = order.RxCompensation;
            ctn.CarparkCost = order.CarparkCost;
            ctn.RegistrationCost = order.RegistrationCost;
            ctn.InStoreCost = order.InStoreCost;
            ctn.DeliverTime = new Date();
            cartons.add(ctn);
        }
        new saveOrder().execute(cartons);
    }

    public void cancelOrder(View view) {
        ArrayList<Carton> cartons = new ArrayList<Carton>();
        for (int i = 0; i < order.DeliveryItems.size(); i++) {
            for (int j = 0; j < order.DeliveryItems.get(i).Cartons.size(); j++) {
                DriverOrderItem deliveryItem = order.DeliveryItems.get(i);
                Carton carton = deliveryItem.Cartons.get(j);
                carton = ordersInfo.getCarton(deliveryItem.OrderId + "-" + carton.CartonNo);
                carton.DeliverBy = null;
                carton.DeliverTime = null;
                carton.Cash = 0F;
                carton.Cheque = 0F;
                carton.CollectAmount = 0F;
                carton.Compensation = 0F;
                carton.CarparkCost = 0F;
                carton.RegistrationCost = 0F;
                carton.InStoreCost = 0F;
                cartons.add(carton);
            }
        }
        order.DeliverTime = null;
        new saveOrder().execute(cartons);
    }

    class saveOrder extends AsyncTask<ArrayList<Carton>, Integer, String> {
        @Override
        protected String doInBackground(ArrayList<Carton>... params) {
            try {
                Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create();
                String json = gson.toJson(params[0]);

                URL url = new URL(new ServiceUrl().REST_DRIVER_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                try {
                    connection.setRequestMethod("POST");
                    connection.setDoOutput(true);
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setRequestProperty("Accept", "application/json");
                    OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
                    out.write(json);
                    out.flush();
                    out.close();
                    int responseCode = connection.getResponseCode();
                    if (responseCode > 299) {
                        throw new Exception(connection.getResponseMessage());
                    }
                    return json;
                }
                catch (Exception e) {
                    final String msg = e.getMessage().toString();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                        }
                    });
                } finally {
                    connection.disconnect();
                }
            } catch (Exception e) {
                final String msg = e.getMessage().toString();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                    }
                });
            }
            return null;
        }

        @Override
        protected void onPostExecute(final String result) {
            super.onPostExecute(result);
            if (result != null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "更新成功", Toast.LENGTH_LONG).show();
                    }
                });
                finish();
            }
        }
    }
}
