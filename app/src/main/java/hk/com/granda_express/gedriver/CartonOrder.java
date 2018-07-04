package hk.com.granda_express.gedriver;

import java.util.Date;

/**
 * Created by keith on 9/4/2017.
 */

public class CartonOrder {
    public int id;
    public String OrderId;
    public int CartonNo;
    public float Weight;
    public float ActualWeight;
    public String PickupBy;
    public Date PickupTime;
    public String DeliverBy;
    public Date DeliverTime;
    public String WeightBy;
    public Date WeightTime;

    public String SenderId;
    public String SenderName;
    public String SenderPhoneNo;
    public String SenderAddress;
    public Date OrderTime;
    public String Description;
    public int Qty;
    public String DeliveryContact;
    public String DeliveryPhoneNo;
    public String DeliveryAddress;
    public float CollectAmount;
    public Date CollectTime;
    public String CollectedBy;
    public String CustomerCode;
    public int PaymentMethod;
    public int PickupMethod;
    public int OrderNo;
    public String Remarks;
    public String SenderCompany;
    public String DeliveryCompany;
    public String DeliveryItemId;
    public int Status;
    public String PosNo;
}
