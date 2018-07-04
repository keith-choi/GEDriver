package hk.com.granda_express.gedriver;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements
    DriverFragment.OnFragmentInteractionListener {
    private final String GE_PREFS = "GE_PREFS";
    private final String KEY_DRIVER = "driver";

    private ArrayList<Driver> drivers;
    int driverId;
    EditText editDate;
    TextView driverName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        new RetrieveDrivers().execute();
        SharedPreferences prefs = getSharedPreferences(GE_PREFS, MODE_PRIVATE);
        driverId = prefs.getInt(KEY_DRIVER, 0);

        editDate = (EditText) findViewById(R.id.editDate);
        android.text.format.DateFormat df = new android.text.format.DateFormat();
        editDate.setText(df.format("yyyy-MM-dd", new Date()));

        driverName = (TextView) findViewById(R.id.driverName);

        loadOrders(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_driver) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("請輸入密碼");

            // Set up the input
            final EditText input = new EditText(this);
            // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            builder.setView(input);

            // Set up the buttons
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (input.getText().toString().equals("97710428")) {
                        DialogFragment settingsFragment = DriverFragment.newInstance();
                        settingsFragment.show(getSupportFragmentManager(), "settings_driver");
                    }
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();
        }

        return super.onOptionsItemSelected(item);
    }

    public int getDriverId() {
        return driverId;
    }

    public void setDriverId(int id) {
        driverId = id;

        SharedPreferences.Editor editor = getSharedPreferences(GE_PREFS, MODE_PRIVATE).edit();
        editor.putInt(KEY_DRIVER, driverId);
        editor.commit();

        showDriverName();
    }

    public ArrayList<Driver> getDrivers() {
        return drivers;
    }

    public void setDrivers(ArrayList<Driver> entities) {
        drivers = entities;
        showDriverName();
    }

    private void showDriverName() {
        if (driverId > 0) {
            for (int i = 0; i < drivers.size(); i++) {
                if (drivers.get(i).id == driverId) {
                    TextView view = (TextView) findViewById(R.id.driverName);
                    view.setText(drivers.get(i).Title);
                }
            }
        }

    }

    public void loadOrders(View view) {
        if (this.driverId > 0) {
            new RetrieveOrders().execute(Integer.toString(this.driverId), editDate.getText().toString());
        }
    }

    public void pickupGoods(View view) {
        Intent intent = new Intent(this, PickupActivity.class);
        intent.putExtra("driverName", driverName.getText().toString());
        intent.putExtra("deliveryDate", editDate.getText().toString());
        startActivity(intent);
    }

    public void returnGoods(View view) {
        Intent intent = new Intent(this, ReturnGoodsActivity.class);
        startActivity(intent);
    }

    public void deliveryGoods(View view) {
        Intent intent = new Intent(this, DeliveryActivity.class);
        intent.putExtra("driverName", driverName.getText().toString());
        intent.putExtra("deliveryDate", editDate.getText().toString());
        startActivity(intent);
    }

    public void settlement(View view) {
        Intent intent = new Intent(this, SettlementActivity.class);
        intent.putExtra("driverId", driverId);
        intent.putExtra("driverName", driverName.getText().toString());
        intent.putExtra("deliveryDate", editDate.getText().toString());
        startActivity(intent);
    }

    class RetrieveDrivers extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            try {
                URL url = new URL(new ServiceUrl().REST_DRIVER_URL);
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
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Gson gson = new Gson();
                        ArrayList<Driver> drivers = (ArrayList<Driver>) gson.fromJson(result, new TypeToken<ArrayList<Driver>>(){}.getType());
                        MainActivity.this.setDrivers(drivers);
                    }
                });

            }
        }
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
                    }
                });
            }
        }
    }
}
