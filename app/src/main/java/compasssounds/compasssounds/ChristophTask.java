package compasssounds.compasssounds;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;


//This is the base task for the activity, we will have code such as sending the results to the log
//and being able to start again etc here, that is common to all the tasks - no need to work on this
//one.

public class ChristophTask extends ActionBarActivity {
    public static final String CURRENT_TRIAL = "CURRENT_TRIAL";

    public ChristophTask ts = this;
    protected int currentTrial = 1;
    protected String name = "Christoph Task";

    protected boolean test = false; //to decide what to do at end of task
    protected Intent returnIntent = new Intent();
    protected boolean paused = false; //can tell if task has started.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        currentTrial = getIntent().getExtras().getInt(CURRENT_TRIAL,-1);

        paused = false;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            test = extras.getBoolean("TEST");
            //if testing, make sure engine is on
            if (test) {
                CompassSoundService.AssertServiceRunning(this);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //resume naviear
        paused = true;
        CompassSoundService.pause(false);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //resume naviear
        paused = true;
        CompassSoundService.pause(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (paused) {
            endTask(ChristophTaskSet.TASK_INCOMPLETE); //cannot resume into these tasks, have to start again
        }
    }

    protected void endTask(int returnCode) {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //log results
        if (test) {
            Intent intent = new Intent(this, ParticipantActivity.class);
            startActivity(intent);
            finish();
        } else {
            setResult(returnCode, returnIntent);
            finish();//finishing activity
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_christoph_task, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_cancel) {
            endTask(ChristophTaskSet.TASK_INCOMPLETE);
        }
        return super.onOptionsItemSelected(item);
    }

    public void taskComplete() {
        showToast(name + " task complete.");
    }

    private void showToast(String msg) {
        Toast error = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        error.show();
    }

    protected void showConfirmationDialog(DialogInterface.OnClickListener yesClickListener) {

        DialogInterface.OnClickListener noClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) { //no action
                endTask(ChristophTaskSet.TASK_INCOMPLETE);
            }
        };
        showConfirmationDialog(yesClickListener, noClickListener);
    }

    protected void showConfirmationDialog(DialogInterface.OnClickListener yesClickListener, DialogInterface.OnClickListener noClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you happy with your response?").setPositiveButton("Yes",yesClickListener).setNegativeButton("No",noClickListener).show();
    }

    static public float differenceInAzimuth(float desired_az, float given_az) {
        float c_desired_az = desired_az % 360;
        float c_given_az = given_az % 360;
        float diff = c_desired_az - c_given_az;
        if (diff > 180) { diff -= 360; }
        if (diff > 180) { diff -= 360; }

        if (diff < -180) { diff += 360; }
        if (diff < -180) { diff += 360; }
        //if (diff < 180) { diff += 360; }

        return diff;
    }

    protected String getFeedbackText(float desired_az, float given_az){

        float diff = differenceInAzimuth(desired_az, given_az);
        int absdiff = (int)Math.abs(diff);
        String direc = diff < 0 ? "right" : "left";
        if (diff == 0) {
            return "You were correct";
        } else if (diff == 180 || diff == -180) {
            return "You were off by 180°";
        }

        Log.i("FEEDBACK TEXT","Desired: " + String.valueOf(desired_az) +" Given: " + String.valueOf(given_az) + " AbsDiff: " + absdiff + " direc: " + direc);
        return "You were off by " + absdiff + "° to the " + direc + "!";
    }

    @Override
    public void onBackPressed() {  //disable back button pressing
    }

}
