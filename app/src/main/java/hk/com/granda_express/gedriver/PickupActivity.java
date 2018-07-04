package hk.com.granda_express.gedriver;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.hardware.camera2.TotalCaptureResult;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.KeyListener;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

import hk.com.granda_express.gedriver.databinding.ActivityPickupBinding;

import static android.R.attr.id;

public class PickupActivity extends AppCompatActivity {
    DriverOrderInfo orders;
    String driverName;
    String deliveryDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final ActivityPickupBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_pickup);
        binding.orderList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                DriverOrder order = orders.getOrder(i);
            }
        });
        binding.cartonid.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 37) {
                    processOrderId(charSequence.toString());
                    binding.cartonid.setText("");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        Bundle extras = getIntent().getExtras();
        driverName = extras.getString("driverName");
        deliveryDate = extras.getString("deliveryDate");

        if (!DataHolder.getInstance().isEmpty()) {
            orders = new DriverOrderInfo(DataHolder.getInstance().getDriverOrders(), driverName);
            binding.setInfo(orders);
            ((TextView)findViewById(R.id.totalQty)).setText("總件數：" + Integer.toString(orders.getTotalQty()));
            ((TextView)findViewById(R.id.totalWeight)).setText("總重量：" + Float.toString(orders.getTotalWeight()) + "kg,  總體積重：" + Float.toString(orders.getTotalCubicWeight()) + "kg");
        } else {
            finish();
        }

        setTitle("取貨：" + deliveryDate);
    }

    private void processOrderId(String s) {
        if (orders.checkPicked(s.toLowerCase())) {
            Vibrator v = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(500);
        } else {
            new RetrieveOrder().execute(s.substring(0, 36), s.substring(37, 38));
        }
    }

    class RetrieveOrder extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL(new ServiceUrl().REST_SERVICE_URL + "?orderid=" + params[0] + "&cartonno=" + params[1]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();
                    return stringBuilder.toString();
                } catch (Exception e) {
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
            } catch (IOException e) {
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
                        Intent intent = new Intent(PickupActivity.this, CartonActivity.class);
                        intent.putExtra("Order", result);
                        startActivity(intent);
                    }
                });
            }
        }
    }

    public void submit(View view) {
        new saveOrders().execute(orders.getCartons());
    }

    class saveOrders extends AsyncTask<ArrayList<Carton>, Integer, String> {
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
