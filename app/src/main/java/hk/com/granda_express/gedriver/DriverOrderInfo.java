package hk.com.granda_express.gedriver;

import android.databinding.ObservableArrayList;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by keith on 10/21/2016.
 */

public class DriverOrderInfo {
    private String driverName;
    private HashMap<String, OrderControl> cartons = new HashMap<String, OrderControl>();
    private Settlement summary;
    private int totalQty;
    private float totalWeight;
    private float totalCubicWeight;
    public ObservableArrayList<DriverOrder> list = new ObservableArrayList<>();

    public DriverOrderInfo(ArrayList<DriverOrder> orders, String driverName) {
        this.summary = null;
        this.totalQty = 0;
        this.driverName = driverName;
        this.list.addAll(orders);
        for (int i = 0; i < orders.size(); i++) {
            DriverOrder order = orders.get(i);
            order.WeightText = "重量：" + Float.toString(order.Weight) + "kg";
            if (order.Weight != order.CubicWeight) {
                order.CubicWeightText = "體積重：" + Float.toString(order.CubicWeight) + "kg";
            } else {
                order.CubicWeightText = "";
            }
            this.totalQty += order.Quantity;
            this.totalWeight += order.Weight;
            this.totalCubicWeight += order.CubicWeight;
            for (int j = 0; j < order.DeliveryItems.size(); j++) {
                DriverOrderItem item = order.DeliveryItems.get(j);
                for (int k = 0; k < item.Cartons.size(); k++) {
                    Carton carton = item.Cartons.get(k);
                    cartons.put(item.OrderId + "-" + Integer.toString(carton.CartonNo),
                            new OrderControl(carton.id, i, j, carton.PickupBy, carton.PickupTime, carton.DeliverBy, carton.DeliverTime));
                }
            }
        }
    }

    public DriverOrder getOrder(int i) {
        return list.get(i);
    }

    public int getOrderPos(String customerCode) {
        int pos = -1;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).CustomerCode.toLowerCase().startsWith(customerCode.toLowerCase())) {
                pos = i;
                break;
            }
        }
        return pos;
    }

    public boolean checkPicked(String qrcode) {
        OrderControl ctrl = this.cartons.get(qrcode);
        if (ctrl != null) {
            int cartonNo = Integer.parseInt(qrcode.substring(37)) - 1;
            ctrl.PickupTime = new Date();
            ctrl.PickupBy = this.driverName;
            if (!ctrl.Picked) {
                ctrl.Picked = true;

                DriverOrder order = list.get(ctrl.Position);
                order.Picked++;
                list.set(ctrl.Position, order);
            }
            return true;
        }
        return false;
    }

    public boolean checkDelivered(String qrcode) {
        OrderControl ctrl = this.cartons.get(qrcode);
        if (ctrl != null) {
            int cartonNo = Integer.parseInt(qrcode.substring(37)) - 1;
            ctrl.DeliverTime = new Date();
            ctrl.DeliverBy = this.driverName;
            if (!ctrl.Delivered) {
                ctrl.Delivered = true;

                DriverOrder order = list.get(ctrl.Position);
                order.Delivered++;
                list.set(ctrl.Position, order);
            }
            return true;
        }
        return false;
    }

    public Carton getCarton(String qrcode) {
        OrderControl ctrl = this.cartons.get(qrcode);
        Carton carton = new Carton();

        carton.id = ctrl.id;
        carton.OrderId = qrcode.substring(0, 36);
        carton.CartonNo = Integer.parseInt(qrcode.substring(37));
        carton.PickupTime = ctrl.PickupTime;
        carton.PickupBy = ctrl.PickupBy;
        carton.DeliverTime = ctrl.DeliverTime;
        carton.DeliverBy = ctrl.DeliverBy;
        return carton;
    }

    public ArrayList<Carton> getCartons() {
        ArrayList<Carton> _cartons = new ArrayList<Carton>();
        ArrayList<String> keys = new ArrayList<String>(this.cartons.keySet());
        for (int i = 0; i < keys.size(); i++) {
            OrderControl ctrl = this.cartons.get(keys.get(i));
            Carton carton = new Carton();

            carton.id = ctrl.id;
            carton.OrderId = keys.get(i).substring(0, 36);
            carton.CartonNo = Integer.parseInt(keys.get(i).substring(37));
            carton.PickupTime = ctrl.PickupTime;
            carton.PickupBy = ctrl.PickupBy;
            carton.DeliverTime = ctrl.DeliverTime;
            carton.DeliverBy = ctrl.DeliverBy;
            _cartons.add(i, carton);
        }
        return _cartons;
    }

    public int getTotalQty() {
        return this.totalQty;
    }

    public float getTotalWeight() { return this.totalWeight; }

    public float getTotalCubicWeight() { return this.totalCubicWeight; }

    public Settlement getSummary(int driverId) {
        Settlement summary = new Settlement();
        if (this.summary == null) {
            summary.UndeliveryNotes = "";
            for (int i = 0; i < list.size(); i++) {
                DriverOrder order = list.get(i);
                summary.DriverId = driverId;
                summary.DeliveryDate = order.DeliveryDate;
                if (order.DeliverTime == null) {
                    summary.UndeliveryNotes += (TextUtils.isEmpty(order.CustomerCode) || TextUtils.isEmpty(summary.UndeliveryNotes) ? "" : " ") + order.CustomerCode;
                } else {
                    summary.Amount += order.Amount;
                    summary.AdditionalAmount += order.AdditionalAmount;
                    summary.CollectAmount1 += order.CollectAmount;
                    summary.CollectAmount2 += order.CollectAmount2;

                    summary.DriverCost += order.Cost;
                    summary.CarparkCost += order.CarparkCost;
                    summary.RegistrationCost += order.RegistrationCost;
                    summary.InStoreCost += order.InStoreCost;
                    summary.OverSizeAmount += order.OverSizeAmount;

                    summary.RxCash += order.RxCash;
                    summary.RxCheque += order.RxCheque;
                    summary.RxCollectAmount += order.RxYen;
                    summary.RxCompensation += order.RxCompensation;
                }
            }
        } else {
            summary = this.summary;
        }
        return summary;
    }

    public void setSummary(Settlement summary) {
        this.summary = summary;
    }

    private class OrderControl
    {
        public int id;
        public int Position;
        public int ItemNo;
        public String PickupBy;
        public Date PickupTime;
        public boolean Picked;
        public String DeliverBy;
        public Date DeliverTime;
        public boolean Delivered;

        public OrderControl(int id, int pos, int itemNo, String pickupBy, Date pickupTime, String deliverBy, Date deliverTime) {
            this.id = id;
            this.Position = pos;
            this.ItemNo = itemNo;
            this.PickupBy = pickupBy;
            this.PickupTime = pickupTime;
            this.Picked = (pickupTime != null);
            this.DeliverBy = deliverBy;
            this.DeliverTime = deliverTime;
            this.Delivered = (deliverTime != null);
        }
    }
}
