import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicInteger;

public class DatabaseManager {
    private static final String DB_NAME="archdata.db";
    public static final String TABLE_NAME="log_data";
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
            statement.execute("CREATE TABLE IF NOT EXISTS "+TABLE_NAME+ " " +
                    "(_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT, "+
                    "hepress TEXT, " +
                    "heprec TEXT, " +
                    "wt1 TEXT, " +
                    "wt2 TEXT, " +
                    "wf1 TEXT, " +
                    "wf2 TEXT, " +
                    "status TEXT, " +
                    "error TEXT, " +
                    "dateupdate INTEGER, " +
                    "sended INTEGER DEFAULT 0 )");
            connection.close();
        } catch (Exception e) {
            System.out.println("DB ERROR: "+e.getMessage());
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
            System.out.println(e.getMessage());
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
            System.out.println(e.getMessage());
        }
    }

    public void clearDatabase()
    {
//        SQLiteDatabase sqLiteDatabase = getInstance().openDatabase();
//        sqLiteDatabase.execSQL("DELETE FROM "+ Drivers.TABLE_NAME);
//        sqLiteDatabase.execSQL("DELETE FROM "+ Passengers.TABLE_NAME);
//        sqLiteDatabase.execSQL("DELETE FROM "+ Points.TABLE_NAME);
//        sqLiteDatabase.execSQL("DELETE FROM "+ Routes.TABLE_NAME);
//        sqLiteDatabase.execSQL("DELETE FROM "+ Tickets.TABLE_NAME);
//        sqLiteDatabase.execSQL("DELETE FROM "+ Trips.TABLE_NAME);
//        getInstance().closeDatabase();
    }

}
