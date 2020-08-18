package uz.alexander.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {

    public static void handleMessage(String message, String text)
    {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        System.out.println(dateFormat.format(date));
        System.out.println(message);
        System.out.println(text);
    }

    public static void handleException(Throwable exception)
    {
        StringWriter sw = new StringWriter();
        exception.printStackTrace(new PrintWriter(sw));
        String exceptionString = sw.toString();
        handleMessage("Exception",exceptionString);
    }

    public static void handleRequest(int status, String request, String result)
    {
        String writetext = "";
        writetext += "REQUEST\r\n"+request+"\r\n";
        writetext += "RESPONSE\r\n"+result+"\r\n";
        handleMessage("REQUEST STATUS: "+Integer.toString(status), writetext);
    }
}
