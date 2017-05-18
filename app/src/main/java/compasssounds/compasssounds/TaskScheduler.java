package compasssounds.compasssounds;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by james on 24/02/15.
 * add a task with preferences (telling you trial info, results, and which ones been done for
 * example) and update these preferences in onPause and onDestroy in order to resume
 */
public class TaskScheduler {

    //store a new task in the scheduler to be done.
    public static void storeNewTask(Context context, String taskName, Bundle arguments) {
        SharedPreferences prefs = context.getSharedPreferences("tasks", 0);


        //add to list of classes to check
        Set<String> classes = getTasks(context);
        if (!classes.contains(taskName)) {
            classes.add(taskName);
        }




        SharedPreferences.Editor edit = prefs.edit();
        edit.putStringSet("Classes", classes);

        //increase number of times it has been stored (to get set number)
        int nTimes = getTrialRuns(context, taskName) + 1;
        edit.putInt(taskName+"_runs", nTimes);

        edit.commit();

        saveTaskState(context, taskName, arguments);

        long millis = System.currentTimeMillis();
        ParticipantLog.writeLogMessageLine("Task " + taskName + " scheduled. Time: " + millis, context, ParticipantLog.PARTICIPANT_LOG);
    }

    public static int getTrialRuns(Context context, String taskName) {
        SharedPreferences prefs = context.getSharedPreferences("tasks", 0);
        return prefs.getInt(taskName + "_runs", 0);
    }

    public static Set<String> getTasks(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("tasks", 0);
        return new HashSet<String>(prefs.getStringSet("Classes", new HashSet<String>()));
    }

    public static List<String> getPendingTasks(Context context) {
        Set<String> classes = getTasks(context);
        SharedPreferences prefs = context.getSharedPreferences("tasks", 0);
        List<String> list = new ArrayList <String>();
        for(String s : classes) {
            if (prefs.getBoolean(s,false)) {
                list.add(s);
            }
        }
        return list;
    }

    public static void dump(Context context) {
        dumpPrefs(context,"tasks");
        Set<String> classes = getTasks(context);
        for(String s : classes) {
            dumpPrefs(context,s +"prefs");
        }
    }

    public static void dumpPrefs(Context context, String prefID) {
        SharedPreferences prefs = context.getSharedPreferences(prefID, Context.MODE_PRIVATE);

        StringBuilder output = new StringBuilder("");
        List<String> contents = new ArrayList<String>();
        output.append("Dumping " + prefID+ " shared preferences...\n");

        for (Map.Entry<String, ?> entry : prefs.getAll().entrySet()) {
            Object val = entry.getValue();
            if (val == null) {
                contents.add(String.format("%s = <null>%n", entry.getKey()));
            } else {
                contents.add(String.format("%s = %s (%s)%n", entry.getKey(), String.valueOf(val), val.getClass()
                        .getSimpleName()));
            }
        }

        java.util.Collections.sort(contents);
        for (String s : contents) {
            output.append(s);
        }
        output.append("Dump complete\n");
        Log.i("TaskScheduler",output.toString());
    }


    public static void saveTaskState(Context context, String taskName, Bundle state) {

        SharedPreferences prefs = context.getSharedPreferences("tasks", 0);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putBoolean(taskName, true);
        edit.commit();


        //save state
        SharedPreferences stateprefs = context.getSharedPreferences(taskName + "prefs", 0);
        SharedPreferences.Editor editprefs = stateprefs.edit();
        savePreferencesBundle(editprefs, "state", state);
        editprefs.commit();
    }

    public static Bundle loadTaskState(Context context, String taskName) {

        SharedPreferences stateprefs = context.getSharedPreferences(taskName + "prefs", 0);
        Bundle ret = loadPreferencesBundle(stateprefs, "state");
        return ret;
    }

    //called when starting a new participant to reset trial counts
    public static void resetTrialsCompleted(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("tasks", 0);
        Set<String> classes = getTasks(context);
        for(String s : classes) {
            SharedPreferences stateprefs = context.getSharedPreferences(s + "prefs", 0);
            SharedPreferences.Editor editprefs = stateprefs.edit();
            editprefs.clear();
            editprefs.commit();
        }

        SharedPreferences.Editor edit = prefs.edit();
        edit.clear();
        edit.commit();
    }

    //register that a task was completed
    public static void taskComplete(Context context, String taskName){
        SharedPreferences prefs = context.getSharedPreferences("tasks", 0);

        SharedPreferences.Editor edit = prefs.edit();
        edit.putBoolean(taskName, false);
        edit.commit();

        //save state
        SharedPreferences stateprefs = context.getSharedPreferences(taskName + "prefs", 0);
        SharedPreferences.Editor editprefs = stateprefs.edit();
        editprefs.clear();
        editprefs.commit();


        long millis = System.currentTimeMillis();
        ParticipantLog.writeLogMessageLine("Task " + taskName + " completed. Time: " + millis, context, ParticipantLog.PARTICIPANT_LOG);

        NotificationManager nm = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancelAll();
    }

    private static final String SAVED_PREFS_BUNDLE_KEY_SEPARATOR = "§§";

    /**
     * Save a Bundle object to SharedPreferences.
     *
     * NOTE: The editor must be writable, and this function does not commit.
     *
     * @param editor SharedPreferences Editor
     * @param key SharedPreferences key under which to store the bundle data. Note this key must
     *            not contain '§§' as it's used as a delimiter
     * @param preferences Bundled preferences
     */
    public static void savePreferencesBundle(SharedPreferences.Editor editor, String key, Bundle preferences) {
        Set<String> keySet = preferences.keySet();
        Iterator<String> it = keySet.iterator();
        String prefKeyPrefix = key + SAVED_PREFS_BUNDLE_KEY_SEPARATOR;

        while (it.hasNext()){
            String bundleKey = it.next();
            Object o = preferences.get(bundleKey);
            if (o == null){
                editor.remove(prefKeyPrefix + bundleKey);
            } else if (o instanceof Integer){
                editor.putInt(prefKeyPrefix + bundleKey, (Integer) o);
            } else if (o instanceof Long){
                editor.putLong(prefKeyPrefix + bundleKey, (Long) o);
            } else if (o instanceof Boolean){
                editor.putBoolean(prefKeyPrefix + bundleKey, (Boolean) o);
            } else if (o instanceof CharSequence){
                editor.putString(prefKeyPrefix + bundleKey, ((CharSequence) o).toString());
            } else if (o instanceof Bundle){
                savePreferencesBundle(editor, prefKeyPrefix + bundleKey, ((Bundle) o));
            }
        }
    }

    /**
     * Load a Bundle object from SharedPreferences.
     * (that was previously stored using savePreferencesBundle())
     *
     *
     * @param sharedPreferences SharedPreferences
     * @param key SharedPreferences key under which to store the bundle data. Note this key must
     *            not contain '§§' as it's used as a delimiter
     *
     * @return bundle loaded from SharedPreferences
     */
    public static Bundle loadPreferencesBundle(SharedPreferences sharedPreferences, String key) {
        Bundle bundle = new Bundle();
        Map<String, ?> all = sharedPreferences.getAll();
        Iterator<String> it = all.keySet().iterator();
        String prefKeyPrefix = key + SAVED_PREFS_BUNDLE_KEY_SEPARATOR;
        Set<String> subBundleKeys = new HashSet<String>();

        while (it.hasNext()) {

            String prefKey = it.next();

            if (prefKey.startsWith(prefKeyPrefix)) {
                String bundleKey = removeStart(prefKey, prefKeyPrefix);

                if (!bundleKey.contains(SAVED_PREFS_BUNDLE_KEY_SEPARATOR)) {

                    Object o = all.get(prefKey);
                    if (o == null) {
                        // Ignore null keys
                    } else if (o instanceof Integer) {
                        bundle.putInt(bundleKey, (Integer) o);
                    } else if (o instanceof Long) {
                        bundle.putLong(bundleKey, (Long) o);
                    } else if (o instanceof Boolean) {
                        bundle.putBoolean(bundleKey, (Boolean) o);
                    } else if (o instanceof CharSequence) {
                        bundle.putString(bundleKey, ((CharSequence) o).toString());
                    }
                }
                else {
                    // Key is for a sub bundle
                    String subBundleKey = substringBefore(bundleKey, SAVED_PREFS_BUNDLE_KEY_SEPARATOR);
                    subBundleKeys.add(subBundleKey);
                }
            }
            else {
                // Key is not related to this bundle.
            }
        }

        // Recursively process the sub-bundles
        for (String subBundleKey : subBundleKeys) {
            Bundle subBundle = loadPreferencesBundle(sharedPreferences, prefKeyPrefix + subBundleKey);
            bundle.putBundle(subBundleKey, subBundle);
        }


        return bundle;
    }

    public static String substringBefore(String str, String separator) {
        if (str.isEmpty() || separator == null) {
            return str;
        }
        if (separator.length() == 0) {
            return "";
        }
        int pos = str.indexOf(separator);
        if (pos == -1) {
            return str;
        }
        return str.substring(0, pos);
    }

    public static String removeStart(String str, String remove) {
        if (str.isEmpty() || remove.isEmpty()) {
            return str;
        }
        if (str.startsWith(remove)){
            return str.substring(remove.length());
        }
        return str;
    }
}
