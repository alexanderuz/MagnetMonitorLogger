import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpServer;
//import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.prefs.Preferences;

    public class MainForm extends TrayFrame{
        public static boolean serverIsLife;
        private CustomTable tableMain;
        public static DefaultTableModel tableModel = new DefaultTableModel();
        private JButton AddButton;
        private JTabbedPane TabPane1;
        private JButton deleteButton;
        //private JButton checkConnectionButton;
        private JTextArea textArea1;
        private JPanel loglist;
        private JSpinner spinner1;
        private JTextField textField1;
        private JTextField textField2;
        private JTextField textField3;
        private JButton saveButton;
        private JButton reloadButton;
        private JButton reloadNowButton;
        private JButton openWebServiceButton;
        private JTextField modemPortTf;
        private JTextField modemSpeedTf;
        private JTextField smsCenterTf;
        private JTextField smsPhoneTf;
        private JButton smsSaveButton;
        private JTextField hePressMinTf;
        private JTextField heLevMinTf;
        private JTextField dialUpNameTf;
        private JCheckBox dialUpConnCb;
        private JTextField hePressMaxTf;
        private JTextField heLevMaxTf;
        private JButton sendTestButton;
        public static Preferences userPrefs;
        public static JsonParser parser = new JsonParser();
        JsonObject buffJson;
        public static ArrayList<MagMonRec> MagMonList = new ArrayList<>();
        public Integer timeToMagMonUpdate;
        public Integer WebPort;
        public String remoteServer, remoteAuth;
        TimerTask timerTask, sendTimerTask, smsSendTask;
        Timer timer, sendTimer, smsTimer;
        MagMonHttpServer webServer;
        HttpServer server;

        private static long lastDataAcquired = 0;

    public  MainForm() throws IOException {
        super();
        userPrefs = Preferences.userRoot().node("prefgemm");
        refrashTable();
        setToPreferredData();
        setContentPane(TabPane1);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(250,250,800,300);
        setLocationRelativeTo(null);
        TabPane1.setBackground(Color.orange);
        TabPane1.setForeground(Color.white);
        Font font = new Font("Verdana", Font.BOLD, 18);
        TabPane1.setFont(font);
        setTitle("Magnet Monitor 3 Logger v1.1");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        Initialisation();
        refrashTable();
        setVisible(true);

        AddButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new AddMagMon();
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(tableMain.getSelectedRow()>=0){
                    String buf = userPrefs.get("JsonMagMonList", "{}");
                    buffJson = MainForm.parser.parse(buf).getAsJsonObject();
                    JsonEdit.DeleteRec(buffJson,tableMain.getSelectedRow());
                    tableModel.removeRow(tableMain.getSelectedRow());
                    //Initialisation();
                    //refrashTable();
                }
            }
        });

        tableMain.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (e.getClickCount() == 2) {
                    if(tableMain.getSelectedRow()>=0) {
                        new MagMonStatus(MagMonList.get(tableMain.getSelectedRow()));
                    }
                } else {

                }
            }
        });
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MainForm.userPrefs.putInt("WebPort",Integer.parseInt(textField1.getText()));
                MainForm.userPrefs.put(SendTask.PREF_SERVER_ADDRESS,textField2.getText());
                MainForm.userPrefs.put(SendTask.PREF_SERVER_AUTH,textField3.getText());
                MainForm.userPrefs.putInt("TimeToUpdate", ((Integer) spinner1.getValue()));
            }
        });

        sendTestButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SmsSenderTask.sendSms(smsPhoneTf.getText(), "Test message, Тестовое сообщение");
            }
        });

        smsSaveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MainForm.userPrefs.put(Constants.PREF_MODEM_PORT, modemPortTf.getText());
                MainForm.userPrefs.putInt(Constants.PREF_MODEM_SPEED, Integer.parseInt(modemSpeedTf.getText()));
                MainForm.userPrefs.put(Constants.PREF_MODEM_SMSCEN, smsCenterTf.getText());
                MainForm.userPrefs.put(Constants.PREF_MODEM_SMSNUMBER, smsPhoneTf.getText());
                MainForm.userPrefs.put(Constants.PREF_MODEM_DIALUPNAME, dialUpNameTf.getText());
                MainForm.userPrefs.putBoolean(Constants.PREF_MODEM_DIALUPEN, dialUpConnCb.isSelected());
                MainForm.userPrefs.putDouble(Constants.PREF_NOTICE_HELEVMIN, Double.parseDouble(heLevMinTf.getText()));
                MainForm.userPrefs.putDouble(Constants.PREF_NOTICE_HELEVMAX, Double.parseDouble(heLevMaxTf.getText()));
                MainForm.userPrefs.putDouble(Constants.PREF_NOTICE_HEPRESSMIN, Double.parseDouble(hePressMinTf.getText()));
                MainForm.userPrefs.putDouble(Constants.PREF_NOTICE_HEPRESSMAX, Double.parseDouble(hePressMaxTf.getText()));
            }
        });

        reloadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                reloadTimeAndWeb();
            }
        });
        reloadNowButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                reloadTimeAndWeb();
            }
        });
        openWebServiceButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI("http://localhost:"+WebPort));
                } catch (IOException ex) {
                    ex.printStackTrace();
                } catch (URISyntaxException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    public void Initialisation(){
        DatabaseManager.initializeInstance();
        PlainDocument doc = (PlainDocument) textField1.getDocument();
        doc.setDocumentFilter(new DigitFilter());
        String buf = userPrefs.get("JsonMagMonList", "{}");
        buffJson = MainForm.parser.parse(buf).getAsJsonObject();
        MagMonList = JsonEdit.GetMagMonList(buffJson);
        tableModel.setRowCount(0);
        Object[] columnsHeader = new String[]{"Name", "HePress",
                "He%","Water Temp 1", "Water Flow 1","Water Temp 2", "Water Flow 2", "Status", "Time"};
        tableModel.setColumnIdentifiers(columnsHeader);
        TableColumnModel tableColumnModel = tableMain.getColumnModel();
        TableColumn tableColumn = tableColumnModel.getColumn(0);
        tableColumn.setCellRenderer(new CustomCellRender());
        reloadTimeAndWeb();
        textField1.setText(WebPort.toString());
        textField2.setText(remoteServer);
        textField3.setText(remoteAuth);

        modemPortTf.setText(userPrefs.get(Constants.PREF_MODEM_PORT, "COM10"));
        modemSpeedTf.setText(Integer.toString(userPrefs.getInt(Constants.PREF_MODEM_SPEED, 115200)));
        smsCenterTf.setText(userPrefs.get(Constants.PREF_MODEM_SMSCEN, "000"));
        smsPhoneTf.setText(userPrefs.get(Constants.PREF_MODEM_SMSNUMBER, ""));
        dialUpNameTf.setText(userPrefs.get(Constants.PREF_MODEM_DIALUPNAME, "Internet"));
        dialUpConnCb.setSelected(userPrefs.getBoolean(Constants.PREF_MODEM_DIALUPEN,false));
        hePressMinTf.setText(Double.toString(userPrefs.getDouble(Constants.PREF_NOTICE_HEPRESSMIN,4.100)));
        hePressMaxTf.setText(Double.toString(userPrefs.getDouble(Constants.PREF_NOTICE_HEPRESSMAX,5.250)));
        heLevMinTf.setText(Double.toString(userPrefs.getDouble(Constants.PREF_NOTICE_HELEVMIN,4.100)));
        heLevMaxTf.setText(Double.toString(userPrefs.getDouble(Constants.PREF_NOTICE_HELEVMAX,5.250)));

        SpinnerModel spinnerModel = new SpinnerNumberModel(1,1,44,1);//Model[arr];
        spinner1.setModel(spinnerModel);
        spinnerModel.setValue(timeToMagMonUpdate);
//        lastDataAcquired = System.currentTimeMillis();
    };

    public static void setDataAcquired() {
        lastDataAcquired = System.currentTimeMillis();
    }
    public static long getLastDataAcquiredInterval() {
        return System.currentTimeMillis() - lastDataAcquired;
    }

    public void reloadTimeAndWeb(){
        timeToMagMonUpdate = userPrefs.getInt("TimeToUpdate", 15);
        WebPort = userPrefs.getInt("WebPort", 8765);
        remoteServer = userPrefs.get(SendTask.PREF_SERVER_ADDRESS,"");
        remoteAuth = userPrefs.get(SendTask.PREF_SERVER_AUTH,"");
        timerStart();
        WebServerStart();
    }

    public void timerStart(){
        timerTask = null;
        timer = null;
        timerTask = new MainTask(textArea1);
        timer = new Timer(true);
        timer.scheduleAtFixedRate(timerTask, 0, timeToMagMonUpdate *60*1000);

        sendTimerTask = null;
        sendTimer = null;
        sendTimerTask = new SendTask(textArea1);
        sendTimer = new Timer(true);
        sendTimer.scheduleAtFixedRate(sendTimerTask, 0, timeToMagMonUpdate *60*1000);

        smsSendTask = null;
        smsTimer = null;
        smsSendTask = new SmsSenderTask(textArea1);
        smsTimer = new Timer(true);
        smsTimer.scheduleAtFixedRate(smsSendTask, 0, timeToMagMonUpdate *60*1000);
    }

    public void WebServerStart(){
        webServer = new MagMonHttpServer();
        try {
            webServer.main(WebPort,server);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void refrashTable(){
        //настройка вывода и вида таблицы мониторов
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment( JLabel.CENTER );
        Object[][] array = new String[MagMonList.size()][9];
        for (int i = 0; i <= MagMonList.size()-1; i++){
            array[i][0]=MagMonList.get(i).getName();
        }
        for (Object[] objects : array) tableModel.addRow(objects);
        tableMain.setFillsViewportHeight(true);
        tableMain.setDefaultRenderer(String.class, centerRenderer);
        tableMain.setModel(tableModel);
        for(int i=0; i <= tableMain.getColumnCount()-1; i++){
            tableMain.getColumn(tableMain.getColumnName(i)).setCellRenderer(centerRenderer);
        }
    }

        class DigitFilter extends DocumentFilter {
            private static final String DIGITS = "\\d+";
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                if (string.matches(DIGITS)) {
                    super.insertString(fb, offset, string, attr);
                }
            }
            @Override
            public void replace(FilterBypass fb, int offset, int length, String string, AttributeSet attrs) throws BadLocationException {
                if (string.matches(DIGITS)) {
                    super.replace(fb, offset, length, string, attrs);
                }
            }
        }

    public void setToPreferredData()
    {
        String buf = userPrefs.get("JsonMagMonList", "{}");
    }

    public static void main(String[] args) throws IOException {
        final MainForm MainFormNew = new  MainForm();
        Image icon =ImageIO.read(MainForm.class.getResourceAsStream("/Ikonka.png"));
        MainFormNew.setIconImage(icon);
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }


    }
