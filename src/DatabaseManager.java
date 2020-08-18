import uz.alexander.utils.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicInteger;

public class DatabaseManager {
    private static final String DB_NAME="archdata.db";
    public static final String TABLE_LOGS="log_data";
    public static final String TABLE_MAIN="main_data";
    public static final String TABLE_SMS="sms_pool";
    private AtomicInteger mOpenCounter = new AtomicInteger();
    private static DatabaseManager instance;
    private Connection mDatabase;


    public static synchronized void initializeInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        try {
            Connection connection = DriverManager.getConnection("jdbc:sqlite:" + DB_NAME);
            Statement statement = connection.createStatement();
            statement.execute("CREATE TABLE IF NOT EXISTS "+TABLE_MAIN+ " " +
                    "(_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT, "+
                    "sended INTEGER DEFAULT 0, "+
                    "datereaded INTEGER, " +
                    "status TEXT, " +
                    "errors TEXT, " +
                    "v1 TEXT, v2 TEXT, v3 TEXT, v4 TEXT, v5 TEXT, v6 TEXT, v7 TEXT, v8 TEXT, v9 TEXT, v10 TEXT, " +
                    "v11 TEXT, v12 TEXT, v13 TEXT, v14 TEXT, v15 TEXT, v16 TEXT, v17 TEXT, v18 TEXT, v19 TEXT, v20 TEXT, " +
                    "v21 TEXT, v22 TEXT, v23 TEXT, v24 TEXT, v25 TEXT, v26 TEXT, v27 TEXT, v28 TEXT, v29 TEXT, v30 TEXT, " +
                    "v31 TEXT, v32 TEXT )");
            statement.execute("CREATE TABLE IF NOT EXISTS "+TABLE_SMS+ " " +
                    "(_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "smstext TEXT, "+
                    "smsnum TEXT, "+
                    "dateadd INTEGER, " +
                    "status TEXT, " +
                    "sended INTEGER DEFAULT 0)");
            connection.close();
        } catch (Exception e) {
            Logger.handleException(e);
        }
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("DB is not initialized, call initializeInstance(..) method first.");
        }
        return instance;
    }

    public synchronized Connection openDatabase() {
        try {
            int openCounter = mOpenCounter.incrementAndGet();
                System.out.println( "openDatabase->mOpenCounter:" + openCounter);
            if (openCounter == 1)
                mDatabase = DriverManager.getConnection("jdbc:sqlite:"+DB_NAME);
        } catch (Exception e) {
            Logger.handleException(e);
        }
        return mDatabase;
    }

    public synchronized void closeDatabase() {
        try {
            int openCounter = mOpenCounter.decrementAndGet();
                System.out.println("closeDatabase->openCounter:" + openCounter);
            if (openCounter == 0)
                mDatabase.close();
        } catch (Exception e) {
            Logger.handleException(e);
        }
    }

    public void clearDatabase()
    {
        //TODO periodicaly clear old reccords sended to main server
    }

}
