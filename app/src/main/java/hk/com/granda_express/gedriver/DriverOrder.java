package hk.com.granda_express.gedriver;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by keith on 10/20/2016.
 */

public class DriverOrder {
    public String id;
    public Date DeliveryDate;
    public String CustomerCode;
    public String CompanyName;
    public String Address;
    public String PhoneNo;
    public String Contact;
    public int Quantity;
    public float Weight;
    public float CubicWeight;
    public float Amount;
    public float AdditionalAmount;
    public int PaymentMethod;
    public float CollectAmount;
    public float CollectAmount2;
    public float Cost;
    public float OverSizeAmount;
    public ArrayList<DriverOrderItem> DeliveryItems;
    public int Picked;
    public int Delivered;
    public String WeightText;
    public String CubicWeightText;

    public float RxCash;
    public float RxCheque;
    public float RxYen;
    public float RxCompensation;

    public float CarparkCost;
    public float RegistrationCost;
    public float InStoreCost;
    public Date DeliverTime;
    public Date OrigDeliveryDate;

    public String getOrigDeliveryDateText() {
        if (this.OrigDeliveryDate == null) {
            return "";
        } else {
            android.text.format.DateFormat df = new android.text.format.DateFormat();
            String time = (String)df.format("HH:mm", new Date());
            return (String) df.format("yyyy-MM-dd", this.OrigDeliveryDate);
        }
    }
}
