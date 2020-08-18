import uz.alexander.utils.Logger;

import java.util.Properties;

public class DialUpManager {

    public static void Connect() {
        String coname = MainForm.userPrefs.get(Constants.PREF_MODEM_DIALUPNAME, "");
        Connect(coname);
    }

    public static void Connect(String connname) {
        executeCommand(connname);
    }

    public static void Disconnect() {
        executeCommand("/d");
    }

    public static void Reconnect() {
        String coname = MainForm.userPrefs.get(Constants.PREF_MODEM_DIALUPNAME, "");
        Reconnect(coname);
    }

    public static void Reconnect(String connname) {
        Disconnect();
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            Logger.handleException(e);
        }
        Connect(connname);
    }

    private static void executeCommand(String params) {
        try {
            Properties properties = System.getProperties();
            String osName = properties.getProperty("os.name");
            String command = "rasdial.exe ";

            if(osName.equals("Windows Me")||osName.equals("Windows 98")||osName.equals("Windows 95")){
                command = "start /wait C:\\Windows\\Rundll32.exe rnaui.dll,RnaDial ";
            }

            command = command + params;
            Process process = Runtime.getRuntime().exec(command);
            process.waitFor();


        } catch (Exception e) {
            Logger.handleException(e);
        }
    }
}
