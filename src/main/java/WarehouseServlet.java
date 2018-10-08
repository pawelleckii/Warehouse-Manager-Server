import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javafx.util.Pair;
import org.json.JSONObject;
import org.json.JSONArray;
//import sun.security.krb5.internal.ccache.FileCredentialsCache;


public class WarehouseServlet extends HttpServlet {
    //private static final long serialVersionUID = 1L;

    public void doGet(HttpServletRequest request,
                      HttpServletResponse response) throws ServletException, IOException {

        String jsonBody = getJsonBody(request);

        JSONObject jsonMain = new JSONObject(jsonBody);

        int version = jsonMain.getInt("version");
        String username = jsonMain.getString("username");
        String password = jsonMain.getString("password");

        //check if user is in database
        if (ItemDAO.checkIfExists("username", username, "User")) {

            String passwordDatabase = ItemDAO.getUserPassword(username);

            if (Objects.equals(passwordDatabase, password)) {
                int userId = ItemDAO.getUserId(username);
                List<Item> dataBaseList = ItemDAO.getItems(userId);

                response.setStatus(HttpURLConnection.HTTP_OK);

                JSONObject root = createJsonItemList(dataBaseList);

                PrintWriter writer = response.getWriter();
                writer.write(root.toString());
            }
        } else {
            response.setStatus(HttpURLConnection.HTTP_FORBIDDEN);
        }

    }

    public void doPost(HttpServletRequest request,
                       HttpServletResponse response) throws ServletException, IOException {

        String jsonBody = getJsonBody(request);

        JSONObject jsonMain = new JSONObject(jsonBody);
        System.out.println(jsonMain);

        int version = jsonMain.getInt("version");
        String username = jsonMain.getString("username");
        String password = jsonMain.getString("password");
        String deviceId = jsonMain.getString("device_id");
        int receivedCounter = jsonMain.getInt("comm_counter");


        //check if user is in database
        if (ItemDAO.checkIfExists("username", username, "User")) {
            String passwordDatabase = ItemDAO.getUserPassword(username);

            if (Objects.equals(passwordDatabase, password)) {
                int userId = ItemDAO.getUserId(username);
                List<Item> dataBaseList = ItemDAO.getItems(userId);

                int dataBaseCounter = ItemDAO.getCommunicationCounter(username, deviceId);
                boolean takeDeltasAmountsSent;
                if(dataBaseCounter < receivedCounter) takeDeltasAmountsSent = false;
                else takeDeltasAmountsSent = true;

                // ANDROID SENT HIS STATE AND WANTS UP-TO-DATE ONE
                if (jsonMain.has("items")) {
                    JSONArray itemsArray = jsonMain.getJSONArray("items");
                    List<Item> receivedList = getItems(itemsArray);
                    System.out.println("------ " + username + " -------");
                    System.out.println("RECEIVED: ("+ (receivedList!=null ? receivedList.size() : "NULL") + ") " + receivedList);
                    System.out.println("DATABASE: ("+ (dataBaseList!=null ? dataBaseList.size() : "NULL") + ") " +dataBaseList);
                    List<Item> updatedList = Item.compareItemLists(receivedList, dataBaseList, takeDeltasAmountsSent);
                    System.out.println("UPDATED: ("+ (updatedList!=null ? updatedList.size() : "NULL") + ") " +updatedList);
                    // Server noticed a conflict and sent up-to-date list in request body
                    if (updatedList != null) {
                        ItemDAO.updateDatabase(userId, updatedList);

                        response.setStatus(HttpURLConnection.HTTP_OK);

                        JSONObject root = createJsonItemList(updatedList);

                        PrintWriter writer = response.getWriter();
                        System.out.println("SENT: " + root.toString());
                        writer.write(root.toString());
                    }
                    // Server and Application states are the same, no need to update Android
                    else {
                        ItemDAO.updateDatabase(userId, receivedList);

                        response.setStatus(HttpURLConnection.HTTP_NO_CONTENT);
                    }
                    ItemDAO.setCommunicationCounter(username, deviceId, receivedCounter);
                }
                // ANDROID SENT BLANK JSON - IT JUST DOWNLOADS DATABASE STATE
                else {
                    response.setStatus(HttpURLConnection.HTTP_OK);

                    JSONObject root = createJsonItemList(dataBaseList);

                    PrintWriter writer = response.getWriter();
                    writer.write(root.toString());
                }
            }
            // Password was wrong
            else {
                response.setStatus(HttpURLConnection.HTTP_UNAUTHORIZED);
            }
        }
        // There's no such user
        else {
            response.setStatus(HttpURLConnection.HTTP_FORBIDDEN);
        }


    }

    private List<Item> getItems(JSONArray itemsArray) {
        List<Item> receivedList = new ArrayList<>();
        try {
            for (int i = 0; i < itemsArray.length(); i++) {
                JSONObject object = itemsArray.getJSONObject(i);
                String name = object.getString("name");
                String store = object.getString("store");
                BigDecimal price = object.getBigDecimal("price");
                int amount = object.getInt("amount");
                int deltaAmount = object.getInt("deltaAmount");
                int deltaAmountSent = object.getInt("deltaAmountSent");
                Item.Status status = Item.Status.fromString(object.getString("status"));
                Item item = new Item(name, store, price, amount, deltaAmount, deltaAmountSent, status);

                receivedList.add(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return receivedList;
    }

    private JSONObject createJsonItemList(List<Item> updatedList) {
        JSONObject root = new JSONObject();

        JSONArray itemJsonArray = new JSONArray();
        for (Item item : updatedList) {
            JSONObject itemJson = item.createJson();
            itemJsonArray.put(itemJson);
        }

        root.put("items", itemJsonArray);
        return root;
    }

    public static String getJsonBody(HttpServletRequest request) throws IOException {
        StringBuilder buffer = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            buffer.append(line);
        }
        return buffer.toString();
    }

    public void doDelete(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException, IOException {

        //Item fridgeitem = fridgeitemparser.parseItemFromRequest(request);
        //itemDAO.save(fridgeitem);
    }

}
