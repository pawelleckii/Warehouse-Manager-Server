import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Objects;

@WebServlet(name = "LoginServlet")
public class LoginServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String jsonBody = WarehouseServlet.getJsonBody(request);

        JSONObject jsonMain = new JSONObject(jsonBody);

        String username = jsonMain.getString("username");
        String password = jsonMain.getString("password");

        if (ItemDAO.checkIfExists("username", username, "User")) {
            String passwordDatabase = ItemDAO.getUserPassword(username);
            if (Objects.equals(passwordDatabase, password)) {
                //login went well
                response.setStatus(HttpURLConnection.HTTP_OK);
            } else {
                //wrong password
                response.setStatus(HttpURLConnection.HTTP_FORBIDDEN);
            }
        //creating new user
        } else {
            ItemDAO.createNewUser(username, password);
            response.setStatus(HttpURLConnection.HTTP_CREATED);
        }

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }


}
