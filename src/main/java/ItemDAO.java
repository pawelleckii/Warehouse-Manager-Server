import java.util.*;
import java.math.BigDecimal;
import java.sql.*;

public class ItemDAO {

    private final static String DBURL = "jdbc:mysql://127.0.0.1:3306/fridgeDB";
    private final static String DBUSER = "root";
    private final static String DBPASS = "lodoweczka";
    private final static String DBDRIVER = "com.mysql.jdbc.Driver";


    public static List<Item> getItems(int userId) {
        //String query = SQLItemParser.createImportQuery();
        String query = "SELECT name, store, price, amount FROM Item WHERE user_Id = " + userId;
        List<Item> items = new ArrayList<>();
        try {
            //Class.forName(DBDRIVER).newInstance();
            Connection connection = DriverManager.getConnection(DBURL, DBUSER, DBPASS);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                String name = resultSet.getString("name");
                String store = resultSet.getString("store");
                BigDecimal price = resultSet.getBigDecimal("price");
                int amount = resultSet.getInt("amount");
                Item item = new Item(name, store, price, amount, 0, 0, Item.Status.ON_SERVER);
                items.add(item);
            }
            statement.close();
            connection.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return items;
    }

    public static int getUserId(String username) {
        String query = SQLItemParser.createImportQueryGet("id", username);
        try {
            //Class.forName(DBDRIVER).newInstance();
            Connection connection = DriverManager.getConnection(DBURL, DBUSER, DBPASS);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            int id = -1;
            if (resultSet.next()) {
                id = resultSet.getInt("id");
            }
            statement.close();
            connection.close();
            return id;
        } catch (Exception ex) {
            ex.printStackTrace();
            return -1;
        }
    }

    public static String getUserPassword(String username) {
        String query = SQLItemParser.createImportQueryGet("password", username);
        try {
            //Class.forName(DBDRIVER).newInstance();
            String password = null;
            Connection connection = DriverManager.getConnection(DBURL, DBUSER, DBPASS);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            if (resultSet.next()) {
                password = resultSet.getString("password");
            }
            statement.close();
            connection.close();
            return password;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static boolean checkIfExists(String type, String value, String tableName) {

        String queryCheck = "SELECT count(*) FROM " + tableName + " WHERE " + type + " = ?";
        try {
            Class.forName(DBDRIVER).newInstance();
            int count = -1;
            Connection connection = DriverManager.getConnection(DBURL, DBUSER, DBPASS);
            final PreparedStatement preparedStatement = connection.prepareStatement(queryCheck);
            preparedStatement.setString(1, value);
            final ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                count = resultSet.getInt(1);
            }
            preparedStatement.close();
            connection.close();
            return (count == 1);
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public static void updateDatabase(int userId, List<Item> updatedList) {
        try {
            //Class.forName(DBDRIVER).newInstance();
            String queryInsert;
            Connection connection = DriverManager.getConnection(DBURL, DBUSER, DBPASS);
            connection.setAutoCommit(false);
            Statement statement = connection.createStatement();
            String queryDelete = "DELETE FROM Item WHERE User_id = " + userId;
            statement.executeUpdate(queryDelete);

            for (Item item : updatedList) {
                queryInsert = SQLItemParser.createInsertQuery(item, userId);
                statement.executeUpdate(queryInsert);
            }
            connection.commit();
            statement.close();
            connection.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void createNewUser(String username, String password) {
        try {
            Class.forName(DBDRIVER).newInstance();
            String queryInsert;
            Connection connection = DriverManager.getConnection(DBURL, DBUSER, DBPASS);
            Statement statement = connection.createStatement();
            queryInsert = "INSERT INTO User (username, password) VALUES ('" + username + "', '" + password + "')";
            statement.executeUpdate(queryInsert);
            statement.close();
            connection.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public static int getCommunicationCounter(String username, String deviceId) {

        String commName = username + "_" + deviceId;
        if(checkIfExists("comm_name", commName, "comm")) {
            String query = "SELECT comm_counter FROM comm WHERE comm_name = '" + commName + "'";
            try {
                String receivedCounter = null;
                Connection connection = DriverManager.getConnection(DBURL, DBUSER, DBPASS);
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(query);
                if (resultSet.next()) {
                    receivedCounter = resultSet.getString("comm_counter");
                }
                statement.close();
                connection.close();
                if (receivedCounter == null) return -1;
                return Integer.parseInt(receivedCounter);
            } catch (Exception ex) {
                ex.printStackTrace();
                return -1;
            }
        }else{
            try {
                String queryInsert;
                Connection connection = DriverManager.getConnection(DBURL, DBUSER, DBPASS);
                Statement statement = connection.createStatement();
                queryInsert = "INSERT INTO comm (comm_name, comm_counter) VALUES ('" + commName + "', " + 0 + ")";
                statement.executeUpdate(queryInsert);
                statement.close();
                connection.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return 0;
        }


    }

    public static void setCommunicationCounter(String username, String deviceId, int receivedCounter) {
        try {
            String commName = username + "_" + deviceId;
            String queryUpdate;
            Connection connection = DriverManager.getConnection(DBURL, DBUSER, DBPASS);
            Statement statement = connection.createStatement();
            queryUpdate = "UPDATE comm SET comm_counter="+ receivedCounter + " WHERE comm_name='" + commName + "'";
            statement.executeUpdate(queryUpdate);
            statement.close();
            connection.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
