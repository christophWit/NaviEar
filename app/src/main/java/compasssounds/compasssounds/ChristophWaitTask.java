package compasssounds.compasssounds;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;

/**
 * Created by james on 02/03/15.
 */
public class ChristophWaitTask extends ChristophTask {

    long lastCompleted = 0;
    long TIME_BETWEEN_TASKS = 20000;
    TextView txtTimeLeft;
    ChristophWaitTask ts = this;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.christoph_task_wait);
        txtTimeLeft = (TextView)findViewById(R.id.tw_secs);
        lastCompleted = getIntent().getLongExtra(ChristophTaskSet.STATE_TIME_LAST_COMPLETED, 0);


        SharedPreferences prefs = getSharedPreferences(ParticipantLog.PARTICIPANT_PREFS, 0);
        TIME_BETWEEN_TASKS = prefs.getLong("TIME_BETWEEN_TASKS",20000);

        //to post GUI updates and end task when done
        timerHandler.post(timerRunnable);

    }


    //runs without a timer by reposting this handler at the end of the runnable
    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {
            long timeNow = System.currentTimeMillis();
            if (timeNow < (lastCompleted + TIME_BETWEEN_TASKS)) {
                long timeLeft = ((lastCompleted + TIME_BETWEEN_TASKS) - timeNow) / 1000;
                txtTimeLeft.setText(timeLeft + "s");
                timerHandler.postDelayed(this, 1000);
            } else {
                endTask(ChristophTaskSet.WAIT_COMPLETE);
            }
        }
    };
}
