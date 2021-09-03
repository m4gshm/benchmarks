package m4gshm.benchmark.json;

import java.util.Date;
import java.util.List;

public class TestBean {

    static class Item {
        public Integer id;
        public Double price;
        public Integer quantity;
    }

    static class FullList {
        public static class Item {
            public Integer id;
            public Double price;
            public Integer quantity;
            public Date deliveryDateFrom;
            public Date deliveryDateTo;
            public Integer marketplaceSellerId;
            public String deliverySchema;
        }

        public Date deliverDateFrom;
        public Date deliveryDateTo;
        public Integer deliveryVariantId;
        public String Address;
        public String deliveryType;
        public Double postingTotalItemPrice;
        public Double postingDeliveryPrice;
        public Double postingTotalPrice;
        public Double postingPriceToPay;
        public Double postingScore;
        public Double postingId;
        public String postingNumber;
        public String externalNumber;
        public String cancelReasons;
        public List<Item> items;
        public String PhoneNumber;
        public String status;
        public Boolean notificationEnabled;
        public String PostCode;
        public Boolean SelfDelivery;
        public Date StoreDeadLine;
        public Boolean HasCompensationPoints;
    }

    public String status;
    public String clientName;
    public Date storeDeadLine;
    public Integer paymentTypeId;
    public String paymentType;
    public Integer postingPriceToPay;
    public Double postingScore;
    public Integer postingId;
    public String postingNumber;
    public Integer deliveryVariantId;
    public String externalNumber;
    public String postCode;
    public Boolean prepay;
    public String deliveryType;
    public String deliveryProvider;
    public List<Item> items;
    public Integer mskTimeOffset;
    public List<FullList> fullList;
    public String phoneNumber;
    public Boolean notificationEnabled;
    public Boolean selfDelivery;
    public Integer orderId;
    public String number;
}
