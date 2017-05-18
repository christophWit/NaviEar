package compasssounds.compasssounds;

import android.content.Context;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


/**
 * Created by Annika on 02/03/2015.
 */
public class ChristophTaskFour extends ChristophTask {

    Button startButton;
    TextView trialCounter;
    PowerManager.WakeLock wl;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.christoph_task_four);

        // Acquire a wake lock to improve processing
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "Task4");
        wl.acquire();

        trialCounter = (TextView)findViewById(R.id.t4p1txtTrial);
        trialCounter.setText("Trial "+currentTrial+" / 2");

        startButton = (Button)findViewById(R.id.t4p1buttonStart);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (wl != null && wl.isHeld()) {
                    wl.release();
                }
                ParticipantLog.writeLogMessageLine("Task four, orientation: " + System.currentTimeMillis(), ts, ParticipantLog.PARTICIPANT_LOG);
                endTask(ChristophTaskSet.TASK_COMPLETE);
            }
        });

    }

    @Override
    protected void onPause() {
        if (wl != null && wl.isHeld()) {
            wl.release();
        }
        super.onPause();
    }
}
