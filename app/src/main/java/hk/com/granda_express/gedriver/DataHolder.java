package hk.com.granda_express.gedriver;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by keith on 10/20/2016.
 */

public class DataHolder {

    private ArrayList<DriverOrder> driverOrders;
    public ArrayList<DriverOrder> getDriverOrders() { return this.driverOrders; }
    public void setDriverOrders(ArrayList<DriverOrder> data) {
        this.driverOrders = data;
    }

    public boolean isEmpty() {
        return this.driverOrders == null;
    }

    private static final DataHolder holder = new DataHolder();
    public static DataHolder getInstance() { return holder; }
}