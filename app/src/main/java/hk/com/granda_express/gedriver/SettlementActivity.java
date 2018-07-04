package hk.com.granda_express.gedriver;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;

public class SettlementActivity extends AppCompatActivity {
    int driverId;
    String driverName;
    String deliveryDate;
    Settlement summary;
    DriverOrderInfo ordersInfo;
    EditText otherCost;
    DecimalFormat nf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settlement);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            driverId = extras.getInt("driverId");
            driverName = extras.getString("driverName");
            deliveryDate = extras.getString("deliveryDate");

            setTitle("結算：" + deliveryDate);

            ordersInfo = new DriverOrderInfo(DataHolder.getInstance().getDriverOrders(), driverName);

            nf = (DecimalFormat) NumberFormat.getNumberInstance();
            nf.setMinimumFractionDigits(2);
            nf.setMaximumFractionDigits(2);
            nf.setDecimalSeparatorAlwaysShown(true);
            otherCost = (EditText) findViewById(R.id.otherCost);
            otherCost.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    float amt = 0;
                    try {
                        amt = Float.parseFloat(charSequence.toString());
                    } catch (Exception e) {
                    } finally {
                        summary.OtherCost = amt;
                        Float subTotal = summary.Amount + summary.AdditionalAmount - summary.DriverCost - summary.OtherCost - summary.CarparkCost;
                        ((TextView) findViewById(R.id.subTotal)).setText("HK$" + nf.format(subTotal));
                        ((TextView) findViewById(R.id.returnAmount)).setText("HK$" + nf.format(summary.CollectAmount2 + subTotal));
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });
            new RetrieveOrders().execute(Integer.toString(driverId), deliveryDate);
        }
    }

    public void transferOrders(View view) {

    }

    public void saveSummary(View view) {
        if (summary.id == 0 && summary.DeliveryDate != null) {
            new saveSettlement().execute(summary);
        }
        finish();
    }

    class RetrieveOrders extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... params) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                }
            });
            try {
                URL url = new URL(new ServiceUrl().REST_ORDER_URL + "?driverId=" + params[0] + "&deliveryDate=" + params[1]);
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
            } catch (MalformedURLException e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
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
                Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create();
                ArrayList<DriverOrder> orders = gson.fromJson(result, new TypeToken<ArrayList<DriverOrder>>(){}.getType());
                DataHolder.getInstance().setDriverOrders(orders);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "資料下載成功", Toast.LENGTH_LONG).show();
                        new RetrieveSettlement().execute(Integer.toString(driverId), deliveryDate);
                    }
                });
            }
        }
    }

    class RetrieveSettlement extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL(new ServiceUrl().REST_SETTLEMENT_URL + "?driverId=" + params[0] + "&date=" + params[1]);
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
            } catch (MalformedURLException e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
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
                Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create();
                final Settlement settlement = gson.fromJson(result, Settlement.class);
                if (settlement != null) {
                    ordersInfo.setSummary(settlement);
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        summary = ordersInfo.getSummary(driverId);
                        Float subTotal = summary.Amount + summary.AdditionalAmount - summary.DriverCost - summary.OtherCost - summary.CarparkCost - summary.RegistrationCost - summary.RxCompensation - summary.OverSizeAmount;
                        ((TextView) findViewById(R.id.undelivered)).setText(summary.UndeliveryNotes);
                        ((TextView) findViewById(R.id.amount)).setText(nf.format(summary.Amount));
                        ((TextView) findViewById(R.id.additionAmount)).setText(nf.format(summary.AdditionalAmount));
                        ((TextView) findViewById(R.id.driverCost)).setText(nf.format(summary.DriverCost));
                        ((TextView) findViewById(R.id.overSizeAmount)).setText(nf.format(summary.OverSizeAmount));
                        ((TextView) findViewById(R.id.carparkCost)).setText(nf.format(summary.CarparkCost));
                        ((TextView) findViewById(R.id.registrationCost)).setText(nf.format(summary.RegistrationCost));
                        ((TextView) findViewById(R.id.InStoreCost)).setText(nf.format(summary.InStoreCost));
                        ((TextView) findViewById(R.id.compensation)).setText(nf.format(summary.RxCompensation));
                        ((TextView) findViewById(R.id.subTotal)).setText("HK$" + nf.format(subTotal));
                        ((TextView) findViewById(R.id.collectAmount)).setText("RMB" + nf.format(summary.CollectAmount1) + "\n" + "HK$" + nf.format(summary.CollectAmount2));
                        ((TextView) findViewById(R.id.returnAmount)).setText("HK$" + nf.format(summary.CollectAmount2 + subTotal));
                        ((TextView) findViewById(R.id.cash)).setText(nf.format(summary.RxCash - summary.DriverCost - summary.OtherCost - summary.CarparkCost - summary.RegistrationCost - summary.RxCompensation - summary.OverSizeAmount));
                        ((TextView) findViewById(R.id.cheque)).setText(nf.format(summary.RxCheque));
                        ((TextView) findViewById(R.id.yen)).setText(nf.format(summary.RxCollectAmount));
                        otherCost.setText(nf.format(summary.OtherCost));
                        if (summary.id > 0) {
                            otherCost.setEnabled(false);
                        }
                    }
                });
            }
        }
    }

    class saveSettlement extends AsyncTask<Settlement, Integer, String> {
        @Override
        protected String doInBackground(Settlement... params) {
            try {
                Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create();
                String json = gson.toJson(params[0]);

                URL url = new URL(new ServiceUrl().REST_SETTLEMENT_URL);
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
