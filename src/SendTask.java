import com.google.gson.JsonObject;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;

import javax.swing.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;

public class SendTask extends TimerTask {

    public static final String PREF_SERVER_ADDRESS = "serveraddress";
    public static final String PREF_SERVER_AUTH = "serverauth";

    JTextArea logOut;

    public SendTask(JTextArea LogOut){
        this.logOut = LogOut;
    }

    @Override
    public void run() {
        try {
            SimpleDateFormat formatForDateNow = new SimpleDateFormat();
            Date currentDate = new Date();
            String serverAddress = MainForm.userPrefs.get(PREF_SERVER_ADDRESS, "");
            String serverAuth = MainForm.userPrefs.get(PREF_SERVER_AUTH, "");
            if (serverAddress != null && serverAddress.length() > 0) {
                Connection dbConnection = DatabaseManager.getInstance().openDatabase();
                CloseableHttpClient httpClient = HttpClientBuilder.create().build();
                try {
                    Statement statement = dbConnection.createStatement();
                    ResultSet resultSet = statement.executeQuery("SELECT * from " + DatabaseManager.TABLE_MAIN + " where sended <> 1 or sended is null");
                    while (resultSet.next()) {
                        JsonObject jsonObject = new JsonObject();
                        for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
                            if (!resultSet.getMetaData().getColumnName(i).equals("_id") && !resultSet.getMetaData().getColumnName(i).equals("sended"))
                                jsonObject.addProperty(resultSet.getMetaData().getColumnName(i), resultSet.getString(i));
                        }
                        HttpPost httpPost = new HttpPost(serverAddress);
                        httpPost.setEntity(new StringEntity(jsonObject.toString(), "UTF-8"));
                        if (serverAuth != null && serverAuth.length() > 0)
                            httpPost.addHeader(HttpHeaders.AUTHORIZATION, serverAuth);

                        CloseableHttpResponse response = httpClient.execute(httpPost);
                        if (response.getStatusLine().getStatusCode() == 200) {
                            Statement statement2 = dbConnection.createStatement();
                            statement2.executeUpdate("UPDATE " + DatabaseManager.TABLE_MAIN + " SET sended=1 WHERE _id=" + resultSet.getString("_id"));
                            statement2.close();
                            logOut.append(formatForDateNow.format(new Date()) + " send#" + resultSet.getString("_id") + " is ok"+ "\n");
                        } else
                            logOut.append(formatForDateNow.format(new Date()) + " send#" + resultSet.getString("_id") + " ERROR. Status code:" + response.getStatusLine().getStatusCode() + " Sever response:" + response.getEntity().toString()+ "\n");
                    }
                } catch (Exception e) {
                    logOut.append(formatForDateNow.format(new Date()) + " send ERROR:" + e.getMessage()+ "\n");
                    System.out.println(e.getMessage());
                } finally {
                    if (DatabaseManager.getInstance() != null)
                        DatabaseManager.getInstance().closeDatabase();
                    try {
                        if (httpClient != null)
                            httpClient.close();
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                }

            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
