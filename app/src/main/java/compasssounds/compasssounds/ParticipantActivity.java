package compasssounds.compasssounds;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AppKeyPair;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.String;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class ParticipantActivity extends ActionBarActivity {

    // Dropbox API stuff
    final static private String APP_KEY = "fdsppv7k65jst0c";
    final static private String APP_SECRET = "ry67miavc1pe5su";
    private static final String ACCESS_KEY_NAME = "ACCESS_KEY";
    private static final String ACCESS_SECRET_NAME = "ACCESS_SECRET";
    private static final String MASTER_PASS = "w6upjjjfmztq5za"; //password if you accidentally set wrong one
    private boolean mLoggedInDropBox = false;
    private DropboxAPI<AndroidAuthSession> mDBApi;
    private boolean mLoggingInDropBox = false;

    public ParticipantActivity ts = this;

    private Button btnStartTasks;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CompassSoundService.AssertServiceRunning(this);
        setContentView(R.layout.activity_participant);
        btnStartTasks = (Button)findViewById(R.id.button_start_tasks);
        getTasksState();
        btnStartTasks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ts, ChristophTaskSet.class);
                intent.putExtra("TEST", false);
                long ISI = ChristophTaskSet.DEFAULT_TIME_BETWEEN_TASKS;
                intent.putExtra("ISI", ISI);
                startActivity(intent);
            }
        });
        initDropBoxConnection();
    }

    private String taskName;
    private Bundle taskState;

    private void getTasksState() {
        List<String> tasks = TaskScheduler.getPendingTasks(this);
        if (tasks.size() > 0) {
            taskName = tasks.get(0); //first one (could do button populating in cases of multiple)
            taskState = TaskScheduler.loadTaskState(this, taskName);


            int total = 1; //extra one is task5
            for(int i = 0; i < ChristophTaskSet.N_TASKS; ++i) {
                int left = taskState.getInt("task"+ChristophTaskSet.TASKS_ARRAY[i],0);
                total += left;
            }




            if (taskState.getBoolean("started",false)){
                btnStartTasks.setText("Resume Tasks: " + String.valueOf(total) + " left");
                btnStartTasks.setEnabled(true);
            } else {
                btnStartTasks.setText("Start Tasks");
                btnStartTasks.setEnabled(true);
            }
        } else {
            btnStartTasks.setText("Start Tasks");
            btnStartTasks.setEnabled(true);
        }
    }


    private void initDropBoxConnection() {
        AppKeyPair appKeyPair = new AppKeyPair(APP_KEY, APP_SECRET);
        AndroidAuthSession session = new AndroidAuthSession(appKeyPair);
        SharedPreferences prefs = getSharedPreferences("prefs", 0);
        String key = prefs.getString(ACCESS_KEY_NAME, null);
        String secret = prefs.getString(ACCESS_SECRET_NAME, null);
        if (key != null && secret != null && key.length() != 0 && secret.length() != 0) {
            session.setOAuth2AccessToken(secret);
        }
        mDBApi = new DropboxAPI<AndroidAuthSession>(session);

        mLoggedInDropBox = mDBApi.getSession().isLinked();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getTasksState();
        if (mLoggingInDropBox) {
            sendDropBoxData();
            mLoggingInDropBox = false;
        }
    }

    private void logOut() {
        // Remove credentials from the session
        mDBApi.getSession().unlink();

        // Clear our stored keys
        SharedPreferences prefs = getSharedPreferences("prefs", 0);
        SharedPreferences.Editor edit = prefs.edit();
        edit.clear();
        edit.commit();

        // Change UI state to display logged out version
        mLoggedInDropBox = false;
    }

    private void sendDropBoxData() {
        AndroidAuthSession session = mDBApi.getSession();

        // The next part must be inserted in the onResume() method of the
        // activity from which session.startAuthentication() was called, so
        // that Dropbox authentication completes properly.
        if (session.authenticationSuccessful()) {
            try {
                // Mandatory call to complete the auth
                session.finishAuthentication();

                // Store it locally in our app for later use
                // Store the OAuth 2 access token, if there is one.
                String oauth2AccessToken = session.getOAuth2AccessToken();
                if (oauth2AccessToken != null) {
                    SharedPreferences prefs = getSharedPreferences("prefs", 0);
                    SharedPreferences.Editor edit = prefs.edit();
                    edit.putString(ACCESS_KEY_NAME, "oauth2:");
                    edit.putString(ACCESS_SECRET_NAME, oauth2AccessToken);
                    edit.commit();
                }

                mLoggedInDropBox = true;
            } catch (IllegalStateException e) {
                showToast("Couldn't authenticate with Dropbox:" + e.getLocalizedMessage());
                Log.i("StartUpActivity", "Error authenticating", e);
            }
        }

        if (mLoggedInDropBox) {
            //upload a file
            File file = ParticipantLog.getLog(this, ParticipantLog.PARTICIPANT_LOG);
            UploadFile upload = new UploadFile(this, mDBApi, "", file);
            upload.execute();

            //upload a file
            File file2 = ParticipantLog.getLog(this, ParticipantLog.LOCATION_LOG);
            UploadFile upload2 = new UploadFile(this, mDBApi, "", file2);
            upload2.execute();

            //upload a file
            File file3 = ParticipantLog.getLog(this, ParticipantLog.TASK_LOG);
            UploadFile upload3 = new UploadFile(this, mDBApi, "", file3);
            upload3.execute();

            //upload a file
            File file4 = ParticipantLog.getLog(this, ParticipantLog.TEST_LOG);
            UploadFile upload4 = new UploadFile(this, mDBApi, "", file4);
            upload4.execute();

        } else {
            showToast("Error logging into dropbox");
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_participant, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_audioCompass) {
            if (!CompassSoundService.isRunning()) {
                if (CompassSoundService.AssertServiceRunning(this)) {
                    item.setTitle("Stop AudioCompass Engine");
                }
            } else {
                if (CompassSoundService.isRunning()) {
                    CompassSoundService.stopSounds();
                    stopService(new Intent(getBaseContext(), CompassSoundService.class));
                    item.setTitle("Start AudioCompass Engine");
                }
            }

            return true;
        } else if (id == R.id.action_sendLog) {
            mLoggedInDropBox = mDBApi.getSession().isLinked();
            if (mLoggedInDropBox) {
                sendDropBoxData(); //already logged in
            } else {
                // Start the remote authentication
                mDBApi.getSession().startOAuth2Authentication(ParticipantActivity.this);
                mLoggingInDropBox = true;
            }

            return true;
        } else if (id == R.id.action_setup) {
            openExperimenterActivity();
            return true;
        } else if (id == R.id.action_tareSensor) {
            CompassSoundService.sensor.tare();
        } /*else if (id == R.id.action_testTask5) {
            Intent intent = new Intent(this, ChristophTaskFive.class);
            intent.putExtra(ChristophTask.CURRENT_TRIAL,1);
            startActivityForResult(intent, 5);
        }*/
        return super.onOptionsItemSelected(item);
    }


    private void openExperimenterActivity() {

        SharedPreferences sp = getSharedPreferences("prefs",0);
        final String pw = sp.getString("ExperimenterActivityPW", "");
        AlertDialog.Builder alert=new AlertDialog.Builder(this);

        if (pw.equalsIgnoreCase("")) {
            Intent intent = new Intent(ts, ExperimenterActivity.class);
            startActivity(intent);
        } else {
            alert.setTitle("Password");
            alert.setMessage("Please enter experiment password:");

            // Set an EditText view to get user input
            final EditText input = new EditText(this);
            alert.setView(input);
            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int whichButton) {
                    if (input.getText().toString().equalsIgnoreCase(pw) || input.getText().toString().equalsIgnoreCase(MASTER_PASS)) {
                        Intent intent = new Intent(ts, ExperimenterActivity.class);
                        startActivity(intent);
                    } else {
                        showToast("Password incorrect. :(");
                    }
                }
            });

            alert.show();
        }
    }

    private void showToast(String msg) {
        Toast error = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        error.show();
    }

    @Override
    public void onBackPressed() {

    }
}
