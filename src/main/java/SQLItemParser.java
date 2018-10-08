

public class SQLItemParser {

    private final static String ITEMTABLE = "Item";
    private final static String USERTABLE = "User";

    public static String createInsertQuery(Item item, int userId) {
        return String.format("INSERT INTO %s (name, store, price, amount, User_Id) VALUES ('%s', '%s', '%.2f', %d, %d);",
                ITEMTABLE, item.getName(), item.getStore(), item.getPrice(), item.getAmount(), userId);
    }

    public static String createImportQuery() {
        return "SELECT name, store, price, amount, id FROM " + ITEMTABLE;
    }

    public static String createImportQueryGet(String get, String username){
        return "SELECT " + get +" FROM " + USERTABLE + " WHERE username = '" + username + "'";
    }

}
