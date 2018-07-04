package hk.com.granda_express.gedriver;

import java.util.ArrayList;

/**
 * Created by keith on 10/21/2016.
 */

public class DriverOrderItem {
    public int id;
    public String Shop;
    public int Quantity;
    public float Weight;
    public String OrderId;
    public ArrayList<Carton> Cartons;
}
