import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
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
                    ResultSet resultSet = statement.executeQuery("SELECT * from " + DatabaseManager.TABLE_NAME + " where sended <> 1 or sended is null");
                    while (resultSet.next()) {
                        List<NameValuePair> paramsList = new ArrayList<>();
                        for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
                            if (!resultSet.getMetaData().getColumnName(i).equals("_id") && !resultSet.getMetaData().getColumnName(i).equals("sended"))
                                paramsList.add(new BasicNameValuePair(resultSet.getMetaData().getColumnName(i), resultSet.getString(i)));
                        }
                        HttpPost httpPost = new HttpPost(serverAddress);
                        httpPost.setEntity(new UrlEncodedFormEntity(paramsList));
                        if (serverAuth != null && serverAuth.length() > 0)
                            httpPost.addHeader(HttpHeaders.AUTHORIZATION, serverAuth);

                        CloseableHttpResponse response = httpClient.execute(httpPost);
                        if (response.getStatusLine().getStatusCode() == 200) {
                            Statement statement2 = dbConnection.createStatement();
                            statement2.executeUpdate("UPDATE " + DatabaseManager.TABLE_NAME + " SET sended=1 WHERE _id=" + resultSet.getString("_id"));
                            statement2.close();
                            logOut.append(formatForDateNow.format(new Date()) + " send#" + resultSet.getString("_id") + " is ");
                        } else
                            logOut.append(formatForDateNow.format(new Date()) + " send#" + resultSet.getString("_id") + " ERROR. Status code:" + response.getStatusLine().getStatusCode() + " Sever response:" + response.getEntity().toString());
                    }
                } catch (Exception e) {
                    logOut.append(formatForDateNow.format(new Date()) + " send ERROR:" + e.getMessage());
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
