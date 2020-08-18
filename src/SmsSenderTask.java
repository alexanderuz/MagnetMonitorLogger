import uz.alexander.utils.Logger;
import uz.alexander.utils.ThreadUtils;

import javax.swing.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.TimerTask;

public class SmsSenderTask extends TimerTask {

    public static boolean addToQueue(String number, String message)
    {
        try {
            Connection dbConnection = DatabaseManager.getInstance().openDatabase();
            Statement statement = dbConnection.createStatement();
            statement.executeUpdate("INSERT INTO "+DatabaseManager.TABLE_SMS+" (smsnum, smstext, dateadd) VALUES " +
                    "('"+number+"', '"+message+"', "+System.currentTimeMillis()+")");
            statement.close();
        } catch (Exception e) {
            Logger.handleException(e);
            logOut.append("Sms add error: " + e.getMessage()+"\n");
            return false;
        } finally {
            DatabaseManager.getInstance().closeDatabase();
        }

        return true;
    }

    private static JTextArea logOut;

    public SmsSenderTask(JTextArea LogOut){
        logOut = LogOut;
    }

    @Override
    public void run() {
        try {
            boolean needReconnect = false;
            Connection dbConnection = DatabaseManager.getInstance().openDatabase();
            Statement statement = dbConnection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM "+DatabaseManager.TABLE_SMS+ " WHERE sended = 0 or sended is null");
            while (resultSet.next()) {
                String smsNumber = resultSet.getString("smsnum");
                String smsText = resultSet.getString("smstext");
                int smsId = resultSet.getInt("_id");
                if (MainForm.userPrefs.getBoolean(Constants.PREF_MODEM_DIALUPEN, false)) {
                    DialUpManager.Disconnect();
                    needReconnect = true;
                    ThreadUtils.Sleep(10);
                }
                if (sendSms(smsNumber, smsText))
                    statement.executeUpdate("UPDATE "+DatabaseManager.TABLE_SMS+ " SET sended =1 WHERE _id="+smsId);

            }
            if (needReconnect) {
                ThreadUtils.Sleep(10);
                DialUpManager.Connect();
            }
            resultSet.close();
            statement.close();
        } catch (Exception e) {
            logOut.append("Sms send error: " + e.getMessage()+"\n");
            Logger.handleException(e);
        } finally {
            DatabaseManager.getInstance().closeDatabase();
        }
    }

    public static boolean sendSms(String number, String text)
    {

        return false;
    }
}
