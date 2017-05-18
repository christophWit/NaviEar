package compasssounds.compasssounds;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;


/**
 * Created by james on 24/02/15.
 */
public class ParticipantLog {
    public final static String PARTICIPANT_LOG = "participantLog";
    public final static String LOCATION_LOG = "locationLog";
    public final static String TASK_LOG = "taskLog";
    public final static String TEST_LOG = "testLog";

    public final static String PARTICIPANT_PREFS = "participant";

    public static boolean eraseLog(Context context, String logfile) {
        //rename (or delete)
        File old = getLogFile(context, logfile);
        File backup = getLogFile(context, logfile + System.currentTimeMillis());
        if (old.exists()){
            return old.renameTo(backup);
        }
        return true;
    }

    public static String getDateTime() {
        Calendar calendar = new GregorianCalendar(TimeZone.getDefault());
        Date trialTime = new Date();
        calendar.setTime(trialTime);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String strDate = sdf.format(calendar.getTime());
        return strDate;
    }

    public static void writeLogMessageLine(String message, Context context, String logFile) {
        Log.i("ParticipantLog", "Logging: " + message);

        try {
            FileWriter fw = new FileWriter(getLogFile(context,logFile), true);
            PrintWriter out = new PrintWriter(new BufferedWriter(fw));
            out.println(message);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static File getLog(Context context, String logFile) {
        return getLogFile(context, logFile);
    }

    private static File getLogFile(Context context, String logFile) {
        String fn = buildFileName(context, logFile);
        return new File(context.getFilesDir(),fn);
    }

    private static String buildFileName(Context context, String logFile) {
        SharedPreferences prefs = context.getSharedPreferences(ParticipantLog.PARTICIPANT_PREFS, 0);
        String fileName = logFile + "-" + prefs.getString("Name","Noname") + "-" + prefs.getString("ID","NoID") + ".csv";
        return fileName;
    }
}
