package compasssounds.compasssounds;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.View;
import android.widget.TextView;
import android.widget.ViewFlipper;

import org.w3c.dom.Text;

/**
 * Created by james on 24/02/15.
 * present the observer a NaviEar signal (i.e. a sound combination),
 * and ask them to point with their head to the direction that corresponds to that signal.
 */
public class ChristophTaskTwo extends ChristophTask {
    public static final String FIRST_PROBE = "FIRST_PROBE";
    public static final String SECOND_PROBE = "SECOND_PROBE";

    public static final String FIRST_AZIMUTH = "FIRST_DIRECTION";
    public static final String SECOND_AZIMUTH = "SECOND_DIRECTION";

    public static final String FIRST_PT = "FIRST_PT";
    public static final String SECOND_PT = "SECOND_PT";

    public static final String FIRST_RT = "FIRST_RT";
    public static final String SECOND_RT = "SECOND_RT";


    //Comment added
    private ViewFlipper viewFlipper;

    private TextView trialCounter;

    private TextView txtPage2PointHeadInstruction;
    private TextView txtPage4PointHeadInstruction;

    private int currentPage = 0;

    //page 1 display instructions, "listen to tone, then orient" wait for touch
    //page 2 play tone ask orientation phone, then wait for touch, log orientation
    //page 3 display instructions, "listen to tone again, then orient with signal back on
    //page 4 play tone (listen) then turn on signal, wait for touch, log orientation2
    //then show familiarity task

    //probe:
    private float first_probe;
    private float second_probe;

    //results:
    private float first_azimuth;
    private float second_azimuth;
    private long first_pt = -1;
    private long second_pt = -1;
    private long first_rt = -1;
    private long second_rt = -1;

    private TextView t2_p2probe;
    private TextView t2_p4probe;

    // tone plays for 1500ms, 300ms break before 'click possible'
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        first_probe = getIntent().getExtras().getFloat(FIRST_PROBE);
        second_probe = getIntent().getExtras().getFloat(SECOND_PROBE);

        setContentView(R.layout.christoph_task_two);
        viewFlipper = (ViewFlipper)findViewById(R.id.viewFlipper);

        //view to change visibility
        txtPage2PointHeadInstruction = (TextView)findViewById(R.id.t2_pointHeadInstruction);
        txtPage4PointHeadInstruction = (TextView) findViewById(R.id.t2_pointHeadInstruction2);

        trialCounter = (TextView)findViewById(R.id.t2_txtTrial);
        trialCounter.setText("Trial "+currentTrial+" / 8");

        t2_p2probe = (TextView)findViewById(R.id.t2_p2probe);
        t2_p4probe = (TextView)findViewById(R.id.t2_p4probe);

        //prevent clicking
        viewFlipper.setOnClickListener(null);
        viewFlipper.setClickable(false);
        startTaskPage1();
    }

    private void startTaskPage1() {

        //ensure engine is in correct state...
        CompassSoundService.pause(true);
        goToPage(1);
        viewFlipper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                startTaskPage2();
            }
        });
        viewFlipper.setClickable(true);

    }

    private void startTaskPage2() {
        txtPage2PointHeadInstruction.setVisibility(View.INVISIBLE);
        viewFlipper.setClickable(false);
        viewFlipper.setOnClickListener(null);
        goToPage(2);
        //play tone
        CompassSoundService.playTone(first_probe,1500);

        t2_p2probe.setText("PROBE");

        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 750 ms during probe presentation would be: vv.vibrate(750);

        // Start without a delay
        // Vibrate for 100ms
        long[] pattern = {0, 100};
        v.vibrate(pattern, -1); //-1 is important

        //wait while tone is played
        Handler timerHandler = new Handler();
        timerHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //display click to go to next screen
                t2_p2probe.setText(" ");
                txtPage2PointHeadInstruction.setVisibility(View.VISIBLE);
                viewFlipper.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        //record direction
                        first_rt = System.currentTimeMillis();
                        first_azimuth = CompassSoundService.getInstance().getCurrentAzimuth();
                        showConfirmationDialog( new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) { //yes action
                                startTaskPage3();
                            }
                        });

                    }
                });
                viewFlipper.setClickable(true);
                first_pt = System.currentTimeMillis();
            }
        }, 1500);
    }

    private void startTaskPage3() {
        ((CompassFeedbackView)findViewById(R.id.t2p3feedbackView)).setArrows(first_probe,first_azimuth);
        ((TextView)findViewById(R.id.t2p3fbDegree)).setText(getFeedbackText(first_probe,first_azimuth));
        goToPage(3);

        viewFlipper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                startTaskPage4();
            }
        });
        viewFlipper.setClickable(true);
    }

    private void startTaskPage4() {
        txtPage4PointHeadInstruction.setVisibility(View.INVISIBLE);
        viewFlipper.setClickable(false);
        viewFlipper.setOnClickListener(null);
        goToPage(4);
        //play tone
        CompassSoundService.playTone(second_probe,1500);
        t2_p4probe.setText("PROBE");
        Vibrator vv = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 750 ms during probe presentation would be: vv.vibrate(750);

        // Start without a delay
        // Vibrate for 100ms
        long[] pattern = {0, 100};
        vv.vibrate(pattern, -1); //-1 is important

        //wait while tone is played
        Handler timerHandler = new Handler();
        timerHandler.postDelayed(new Runnable() {

            @Override
            public void run() {
                //turn on signal
                CompassSoundService.pause(false);
                t2_p4probe.setText(" ");

                //display click to go to next screen
                txtPage4PointHeadInstruction.setVisibility(View.VISIBLE);
                viewFlipper.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        //record direction
                        second_rt = System.currentTimeMillis();
                        second_azimuth = CompassSoundService.getInstance().getCurrentAzimuth();
                        showConfirmationDialog( new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) { //yes action
                                returnIntent.putExtra(FIRST_AZIMUTH,first_azimuth);
                                returnIntent.putExtra(SECOND_AZIMUTH, second_azimuth);
                                returnIntent.putExtra(FIRST_RT,first_rt);
                                returnIntent.putExtra(SECOND_RT, second_rt);
                                returnIntent.putExtra(FIRST_PT,first_pt);
                                returnIntent.putExtra(SECOND_PT, second_pt);
                                ParticipantLog.writeLogMessageLine("Task two compete: " + second_rt, ts, ParticipantLog.PARTICIPANT_LOG);
                                startTaskPage5();
                            }
                        });

                    }
                });
                viewFlipper.setClickable(true);
                second_pt = System.currentTimeMillis();
            }
        }, 2000);
    }

    private void startTaskPage5() {
        ((CompassFeedbackView)findViewById(R.id.t2p5feedbackView)).setArrows(second_probe,second_azimuth);
        ((TextView)findViewById(R.id.t2p5fbDegree)).setText(getFeedbackText(second_probe,second_azimuth));

        goToPage(5);
        viewFlipper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endTask(ChristophTaskSet.TASK_COMPLETE);
            }
        });
    }




    private void goToPage(int newPage) {
        viewFlipper.setDisplayedChild(newPage - 1);
    }

    @Override
    protected void onPause() {
        viewFlipper.setOnClickListener(null);
        super.onPause();
    }
}
