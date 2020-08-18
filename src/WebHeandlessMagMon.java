import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import uz.alexander.utils.Logger;

import javax.swing.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class WebHeandlessMagMon {

    public static Boolean autorise(Integer NumberMagMonList, JTextArea LogOut) throws IOException {
        Boolean result = false;
        try {
            MagMonRec MagMon = MainForm.MagMonList.get(NumberMagMonList);
            final WebClient webClient = new WebClient();
            //Загружаем нужную страницу
            final HtmlPage page1 = webClient.getPage("http://" + MagMon.IP + ":" + MagMon.Port);
            // Выбираем нужную форму,
            // находим кнопку отправки и поле, которое нужно заполнить.
            final HtmlForm form = page1.getFormByName("login");

            final HtmlTextInput textLogin = form.getInputByName("UserName");
            final HtmlPasswordInput textPass = form.getInputByName("PassWord");
            final HtmlSubmitInput button = form.getInputByValue("Submit");// .getInputByName("submitbutton");

            // Записывает в найденное поле нужное значение.
            textLogin.setValueAttribute(MagMon.Login);
            textPass.setValueAttribute(MagMon.Pass);
            // Теперь «кликаем» на кнопку и переходим на новую страницу.
            final HtmlPage page2 = button.click();
            webClient.close();
            result = true;
        }catch (Exception e){
            result = false;
        }
        return result;
    }

    public static Boolean getData(Integer NumberMagMonList, JTextArea LogOut) throws IOException {
        Boolean result = false;
        MagMonRec MagMon = MainForm.MagMonList.get(NumberMagMonList);
        final WebClient webClient = new WebClient();

        HtmlPage page3 = webClient.getPage("http://"+MagMon.IP+":"+MagMon.Port+"/cur_a_vals.html");
        HtmlForm form2 = page3.getFormByName("curVal");

        String[] paramsList = new String[32];

        for (int i =0; i< 32; i++)
        {
            HtmlTextInput textInput = form2.getInputByName("Ch"+String.valueOf(i+1));
            String readedValue = textInput.getText();
            paramsList[i] = readedValue;
        }
        long currentTime = System.currentTimeMillis()/1000;

        String status = "ok";

        Date date = new Date();
        SimpleDateFormat formatForDateNow = new SimpleDateFormat("HH:mm:ss");

        MainForm.MagMonList.get(NumberMagMonList).setStatus(status);
        MainForm.MagMonList.get(NumberMagMonList).setHeLevel(paramsList[Constants.HE_LEVEL_INDEX]);
        MainForm.MagMonList.get(NumberMagMonList).setHePress(paramsList[Constants.HE_PRESSURE_INDEX]);
        MainForm.MagMonList.get(NumberMagMonList).setLastTime(formatForDateNow.format(date));
        MainForm.MagMonList.get(NumberMagMonList).setWaterFlow1(paramsList[15]);
        MainForm.MagMonList.get(NumberMagMonList).setWaterTemp1(paramsList[16]);
        MainForm.MagMonList.get(NumberMagMonList).setWaterFlow2(paramsList[24]);
        MainForm.MagMonList.get(NumberMagMonList).setWaterTemp2(paramsList[25]);

        page3 = webClient.getPage("http://"+MagMon.IP+":"+MagMon.Port+"/alarms.html");
        WebResponse response = page3.getWebResponse();
        String content = response.getContentAsString();
        Document doc = (Document) Jsoup.parseBodyFragment(content);
        Elements fullHtml = doc.getElementsByTag("pre");
        //System.out.println(fullHtml.toString());
        ArrayList<String> bufList = ErrParse(fullHtml.toString());
        String buf;
        if(bufList.size()<1){
            buf = "OK";
        }
        else{
            buf = bufList.size()+" Error";
        }
        MainForm.MagMonList.get(NumberMagMonList).setStatus(buf);
        status = buf;
        MainForm.MagMonList.get(NumberMagMonList).setErrors(bufList);
        String errors = String.join("|", bufList);

        //write data to database
        try {
            Connection connection = DatabaseManager.getInstance().openDatabase();
            Statement statement = connection.createStatement();
            String mMName = MainForm.MagMonList.get(NumberMagMonList).getName();
            StringBuilder quotedValues = new StringBuilder();

            for (int i=0; i<32; i++)
            {
                if (quotedValues.length() > 0)
                    quotedValues.append(",");
                quotedValues.append("\"").append(paramsList[i]).append("\"");
            }
            String query = "INSERT INTO "+DatabaseManager.TABLE_MAIN+
                    " (name, datereaded, status, errors, v1,v2,v3,v4,v5,v6,v7,v8,v9,v10,v11,v12,v13,v14,v15,v16,v17,v18,v19,v20,v21,v22,v23,v24,v25,v26,v27,v28,v29,v30,v31,v32) VALUES ("
                    +"\""+mMName+"\", \""+currentTime+"\", " +
                    " \""+status+"\", \""+errors+"\", "+quotedValues.toString()+" )";
            statement.executeUpdate(query);


            //TODO читать раз в сутки страницу конфига и писать ее целиком /cfgSummary.html

        } catch (Exception e) {
            Logger.handleException(e);
        } finally {
            if (DatabaseManager.getInstance() != null)
                DatabaseManager.getInstance().closeDatabase();
        }

        //send sms if needed
        if (!"".equals(MainForm.userPrefs.get(Constants.PREF_MODEM_SMSNUMBER,""))) {
            try {
                double minlevel = MainForm.userPrefs.getDouble(Constants.PREF_NOTICE_HELEVMIN, 0);
                double maxlevel = MainForm.userPrefs.getDouble(Constants.PREF_NOTICE_HELEVMAX, 0);
                double minpress = MainForm.userPrefs.getDouble(Constants.PREF_NOTICE_HEPRESSMIN, 0);
                double maxpress = MainForm.userPrefs.getDouble(Constants.PREF_NOTICE_HEPRESSMAX, 0);

                if (Double.parseDouble(paramsList[Constants.HE_LEVEL_INDEX]) > maxlevel || Double.parseDouble(paramsList[Constants.HE_LEVEL_INDEX]) < minlevel)
                    SmsSenderTask.addToQueue(MainForm.userPrefs.get(Constants.PREF_MODEM_SMSNUMBER, ""), "He Level is out of range:" + paramsList[Constants.HE_LEVEL_INDEX]);
                if (Double.parseDouble(paramsList[Constants.HE_PRESSURE_INDEX]) > maxpress || Double.parseDouble(paramsList[Constants.HE_PRESSURE_INDEX]) < minpress)
                    SmsSenderTask.addToQueue(MainForm.userPrefs.get(Constants.PREF_MODEM_SMSNUMBER, ""), "Pressure is out of range:" + paramsList[Constants.HE_PRESSURE_INDEX]);

            } catch (Exception e) {
                Logger.handleException(e);
            }
        }
        webClient.close();
        return result;
    }

    public static ArrayList<String> ErrParse(String sourcetext){
        ArrayList<String> result = null;
        ArrayList<String> ListStr = new ArrayList<String>();
        ArrayList<String> ListBuf = new ArrayList<String>();
        for (String retval : sourcetext.split("\\n")) {
            ListStr.add(retval);
        }
        for(int i=0; i<=ListStr.size()-1;i++){
            if(ListStr.get(i).contains("<")|(ListStr.get(i).length()<2)|(ListStr.get(i).contains("-"))){

            }
            else {
                ListBuf.add(ListStr.get(i));
            }
        }
        return ListBuf;
    }
}
