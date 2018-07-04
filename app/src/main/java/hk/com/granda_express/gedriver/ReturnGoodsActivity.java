package hk.com.granda_express.gedriver;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ReturnGoodsActivity extends AppCompatActivity {
    EditText qrCode;
    TextView messageView;
    Button returnButton;
    CartonOrder carton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_return_goods);

        qrCode = (EditText) findViewById(R.id.editQRCode);
        qrCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 37) {
                    processCartonId(charSequence.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        messageView = (TextView)findViewById(R.id.messageView);
        returnButton = (Button)findViewById(R.id.btnReturn);
        returnButton.setVisibility(View.GONE);

        setTitle("退回寄件");
    }

    private void processCartonId(String cartonId) {
        String cartonNo = cartonId.substring(37);
        String orderId = cartonId.substring(0, 36);
        new RetrieveOrder().execute(orderId, cartonNo);
        qrCode.setText("");
    }

    public void returnCarton(View view) {
        if (carton != null) {
            new saveOrder().execute(carton.id);
        }
    }

    class RetrieveOrder extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... params) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    messageView.setText("请稍候，下载中　．．．");
                }
            });
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
                        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create();
                        carton = gson.fromJson(result, CartonOrder.class);

                        StringBuilder sb = new StringBuilder(carton.CustomerCode);
                        sb.append("\n" + carton.DeliveryAddress);
                        if (!TextUtils.isEmpty(carton.DeliveryCompany)) {
                            sb.append("\n" + carton.DeliveryCompany);
                        }
                        if (!TextUtils.isEmpty(carton.DeliveryContact)) {
                            sb.append("\n" + carton.DeliveryContact);
                        }
                        if (!TextUtils.isEmpty(carton.DeliveryPhoneNo)) {
                            sb.append("\n" + carton.DeliveryPhoneNo);
                        }
                        if (!TextUtils.isEmpty(carton.Description)) {
                            sb.append("\n" + carton.Description);
                        }
                        sb.append("\n箱號" + Integer.toString(carton.CartonNo));
                        messageView.setText(sb.toString());

                        returnButton.setVisibility(View.VISIBLE);
                    }
                });
            }
        }
    }


    class saveOrder extends AsyncTask<Integer, Integer, String> {
        @Override
        protected String doInBackground(Integer... params) {
            try {
                URL url = new URL(new ServiceUrl().REST_SERVICE_URL + "/" + Integer.toString(params[0]));
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                try {
                    connection.setRequestMethod("DELETE");
                    connection.setDoOutput(true);
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setRequestProperty("Accept", "application/json");
                    int responseCode = connection.getResponseCode();
                    if (responseCode > 299) {
                        throw new Exception(connection.getResponseMessage());
                    }
                    return "ok";
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
                carton = null;
                messageView.setText("退回處理成功");
                returnButton.setVisibility(View.GONE);
            }
        }
    }
}
