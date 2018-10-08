import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Item {

    private String name;
    private String store;
    private BigDecimal price;
    private int amount;
    private int deltaAmount;
    private int deltaAmountSent;
    public Status status;

    public Status getStatus() {
        return status;
    }

    public int getDeltaAmountSent() {
        return deltaAmountSent;
    }

    public enum Status {
        ON_SERVER,
        CREATED,
        DELETED,;

        public static Status fromString(String string) {
            switch (string) {
                case "ON_SERVER":
                    return ON_SERVER;
                case "CREATED":
                    return CREATED;
                case "DELETED":
                    return DELETED;
            }
            return null;
        }

        public static Status fromInt(int x) {
            switch (x) {
                case 0:
                    return ON_SERVER;
                case 1:
                    return CREATED;
                case 2:
                    return DELETED;
            }
            return null;
        }

        public static class fromInt {
            public fromInt(int status) {
            }
        }
    }

    public Item(String name, String store, BigDecimal price, int amount, int deltaAmount, int deltaAmountSent, Status status) {
        this.name = name;
        this.store = store;
        this.price = price;
        this.amount = amount;
        this.deltaAmount = deltaAmount;
        this.deltaAmountSent = deltaAmountSent;
        this.status = status;
    }

//    public Item(HttpServletRequest request) {
//        this.name = request.getParameter("name");
//        this.store = request.getParameter("store");
//        this.price = new BigDecimal(request.getParameter("price"));
//        this.amount = Integer.parseInt(request.getParameter("amount"));
//    }

    public JSONObject createJson() throws JSONException {
        JSONObject jObject = new JSONObject();

        jObject.put("name", name);
        jObject.put("store", store);
        jObject.put("price", price);
        jObject.put("amount", amount);

        return jObject;
    }

    /**
     * @param receivedList
     * @param dataBaseList
     * @return if there's a conflict, return a list of items that the user has to override,
     * otherwise (the user has up-to-date data), return null
     */
    public static List<Item> compareItemLists(List<Item> receivedList, List<Item> dataBaseList, boolean takeDeltaAmountsSent) {
        boolean isConflict = false;
        List<Item> retList = new ArrayList<>(dataBaseList);

        for (Iterator<Item> iterator = retList.iterator(); iterator.hasNext(); ) {
            Item retItem = iterator.next();
            Item receivedItem = checkIfExists(retItem.getName(), receivedList);
            if (receivedItem != null) {
                //item is on both lists. It has either ON_SERVER or DELETED status
                if (receivedItem.getStatus() != Status.DELETED) {
                    //item is on server and in request. Update it's amount
                    //Integer deltaAmount = getItemDeltaAmount(retItem.getName(), receivedList);
                    int deltaAmount;
                    if (takeDeltaAmountsSent) {
                        deltaAmount = receivedItem.getDeltaAmountSent();
                    } else {
                        deltaAmount = receivedItem.getDeltaAmount();
                    }
                    retItem.setAmount(retItem.getAmount() + deltaAmount);
                    if (deltaAmount != 0) isConflict = true;
                } else {
                    iterator.remove();
                    isConflict = true;
                }
            } else {
                //ELSE: item is in database from another source. Just let it be on retList
                isConflict = true;
            }
        }

        for (Item receivedItem : receivedList) {
            Item databaseItem = checkIfExists(receivedItem.getName(), dataBaseList);
            if (databaseItem == null) {
                if (receivedItem.getStatus() == Status.CREATED) {
                    //there is no such item in database and new one has arrived
                    retList.add(receivedItem);
                    isConflict = true;
                } else if (receivedItem.getStatus() == Status.ON_SERVER) {
                    //there is no such item in database, but there was one in received list. dont add it but set conflict flag
                    isConflict = true;
                }
            }
        }

        return retList;
//        if (isConflict) {
//            return retList;
//        } else {
//            return null;
//        }
    }

    private static int getItemDeltaAmount(String name, List<Item> itemList) {
        for (Item item : itemList) {
            if (item.getName().equals(name)) {
                return item.getDeltaAmount();
            }
        }
        return 0;
    }

    /**
     * @param name
     * @param itemList
     * @return item if exists in itemList, null if does not
     */
    private static Item checkIfExists(String name, List<Item> itemList) {
        for (Item item : itemList) {
            if (item.getName().equals(name)) {
                return item;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return name + " " + amount + " " + status.toString() + " D:" + deltaAmount;
    }

    //Getters and setters

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStore() {
        return store;
    }

    public void setStore(String store) {
        this.store = store;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getDeltaAmount() {
        return deltaAmount;
    }
}
