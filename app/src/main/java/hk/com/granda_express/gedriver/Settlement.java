package hk.com.granda_express.gedriver;

import java.util.Date;

/**
 * Created by keith on 11/5/2016.
 */

public class Settlement {
    public int id;
    public Date DeliveryDate;
    public int DriverId;
    public float CollectAmount1;
    public float CollectAmount2;
    public float Amount;
    public float AdditionalAmount;
    public float DriverCost;
    public float OtherCost;
    public float CarparkCost;
    public float RegistrationCost;
    public float InStoreCost;
    public float OverSizeAmount;
    public String UndeliveryNotes;

    public float RxCash;
    public float RxCheque;
    public float RxCollectAmount;
    public float RxCompensation;
}
