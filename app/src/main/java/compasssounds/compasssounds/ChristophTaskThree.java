package compasssounds.compasssounds;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ViewFlipper;
import android.os.Vibrator;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by james on 27/02/15.
 * present the observer a NaviEar signal (i.e. a sound combination),
 * and ask them to point with their head to the direction that corresponds to that signal.
 */
public class ChristophTaskThree extends ChristophTask {
    public static final String FIRST_PROBE = "FIRST_PROBE";
    public static final String SECOND_PROBE = "SECOND_PROBE";
    public static final String THIRD_PROBE = "THIRD_PROBE";

    public static final String FIRST_DIRECTION = "FIRST_DIRECTION";
    public static final String SECOND_DIRECTION = "SECOND_DIRECTION";
    public static final String THIRD_DIRECTION = "THIRD_DIRECTION";

    public static final String FIRST_PT = "FIRST_PT";
    public static final String SECOND_PT = "SECOND_PT";
    public static final String THIRD_PT = "THIRD_PT";

    public static final String FIRST_RT = "FIRST_RT";
    public static final String SECOND_RT = "SECOND_RT";
    public static final String THIRD_RT = "THIRD_RT";

    private ViewFlipper viewFlipper;
    private TextView trialCounter;
    private TextView t3_p2probe;
    private TextView t3_p4probe;
    private TextView t3_p6probe;


    private int[] page2DirectionIds = {
        R.id.button_p2n,
        R.id.button_p2s,
        R.id.button_p2e,
        R.id.button_p2w,
        R.id.button_p2nw,
        R.id.button_p2ne,
        R.id.button_p2se,
        R.id.button_p2sw};

    private int[] page4DirectionIds = {
            R.id.button_p4n,
            R.id.button_p4s,
            R.id.button_p4e,
            R.id.button_p4w,
            R.id.button_p4nw,
            R.id.button_p4ne,
            R.id.button_p4se,
            R.id.button_p4sw};
    private int[] page6DirectionIds = {
            R.id.button_p6n,
            R.id.button_p6s,
            R.id.button_p6e,
            R.id.button_p6w,
            R.id.button_p6nw,
            R.id.button_p6ne,
            R.id.button_p6se,
            R.id.button_p6sw};

    private static final int N_BUTTONS = 8;
    private Button[] page2DirectionButtons = new Button[N_BUTTONS];
    private Button[] page4DirectionButtons = new Button[N_BUTTONS];
    private Button[] page6DirectionButtons = new Button[N_BUTTONS];

    private TextView page6WaitDialog;
    //page 1 navi ear off display instructions, "listen to tone, then orient" wait for touch
    //page 2 play tone constantly ask for direction, then wait for button press
    // on press-> confirmation alert (with navi ear signal on), log orientation
    //page 3 instructions 2, navi ear off wait for touch
    //page 4 200ms silence, 500ms tone, silence 200ms, then navi ear + wait for button press
    // on press-> confirmation alert (naviear still on), log orientation
    //page 5 instructions 3, navi ear off, wait for touch
    //page 6 200ms silence, 500ms tone, silence 2500ms (+ wait), then wait for button press
    // on press-> confirmation alert (with navi ear signal on), log orientation


    //results:
    private String first_direction;
    private String second_direction;
    private String third_direction;
    private long first_pt = -1;
    private long second_pt = -1;
    private long third_pt = -1;
    private long first_rt = -1;
    private long second_rt = -1;
    private long third_rt = -1;

    private float first_probe;
    private float second_probe;
    private float third_probe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        first_probe = getIntent().getExtras().getFloat(FIRST_PROBE);
        second_probe = getIntent().getExtras().getFloat(SECOND_PROBE);
        third_probe = getIntent().getExtras().getFloat(THIRD_PROBE);
        String message = "Started task 3 with azimuths: " + first_probe + " " + second_probe + " " +third_probe;
        ParticipantLog.writeLogMessageLine(message, this, ParticipantLog.PARTICIPANT_LOG);


        setContentView(R.layout.christoph_task_three);
        viewFlipper = (ViewFlipper)findViewById(R.id.viewFlipper);
        trialCounter = (TextView)findViewById(R.id.t3_p1txtTrial);
        trialCounter.setText("Trial "+currentTrial+" / 8");
        t3_p2probe = (TextView)findViewById(R.id.t3_p2probe);
        t3_p4probe = (TextView)findViewById(R.id.t3_p4probe);
        t3_p6probe = (TextView)findViewById(R.id.t3_p6probe);


        page6WaitDialog = (TextView)findViewById(R.id.t3_p6listen);

        for(int i = 0; i < N_BUTTONS; i++) {
            page2DirectionButtons[i] = (Button)findViewById(page2DirectionIds[i]);
            page4DirectionButtons[i] = (Button)findViewById(page4DirectionIds[i]);
            page6DirectionButtons[i] = (Button)findViewById(page6DirectionIds[i]);
        }


        //prevent clicking
        viewFlipper.setOnClickListener(null);
        viewFlipper.setClickable(false);
        startTaskPage1();
    }


    private void setDirectionButtonsHidden(boolean hidden, Button[] buttons, View.OnClickListener ocl) {
        for(int i = 0; i < buttons.length; i++) {
            buttons[i].setOnClickListener(ocl);
            buttons[i].setVisibility(hidden ? View.INVISIBLE : View.VISIBLE);
            buttons[i].setEnabled(!hidden);
        }
    }

    private String getDirection(int id, int[] buttons) {
        for(int i = 0; i < buttons.length; i++) {
            if (id == buttons[i]) {
                return ChristophTaskSet.directions[i];
            }
        }
        return "-";
    }

    //page 1 navi ear off display instructions, "listen to tone, then orient" wait for touch
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

//page 2 play tone constantly ask for direction, then wait for button press
// on press-> confirmation alert (with navi ear signal on), log orientation


    private void startTaskPage2() {
        viewFlipper.setClickable(false);
        viewFlipper.setOnClickListener(null);

        goToPage(2);
        //play tone
        CompassSoundService.playTone(first_probe, -1);
        t3_p2probe.setText("PROBE");

        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate to indicate probe presentation start:

        // Start without a delay
        // Vibrate for 100ms
        long[] pattern = {0, 100};
        v.vibrate(pattern, -1); //-1 is important

        //show buttons
        setDirectionButtonsHidden(false, page2DirectionButtons, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //store first_direction
                first_direction = getDirection(v.getId(), page2DirectionIds);
                //restart naviear
                CompassSoundService.pause(false);
                //confirm alert
                showConfirmationDialog(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) { //yes action
                        first_rt = System.currentTimeMillis();
                        t3_p2probe.setText(" ");
                        startTaskPage3();
                    }
                });
            }
        });
        first_pt = System.currentTimeMillis();
    }

    //page 3 instructions 2, navi ear off wait for touch
    private void startTaskPage3() {
        float direction = ChristophTaskSet.directionToAzimuth(first_direction);
        ((CompassFeedbackView)findViewById(R.id.t3p3feedbackView)).setArrows(first_probe,direction);
        ((TextView)findViewById(R.id.t3p3fbDegree)).setText(getFeedbackText(first_probe,direction));

        CompassSoundService.pause(true);
        goToPage(3);
        viewFlipper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                startTaskPage4();
            }
        });
        viewFlipper.setClickable(true);
    }

    //page 4 300ms silence, 1500ms tone, silence 500ms, then navi ear + wait for button press
    // on press-> confirmation alert (naviear still on), log orientation
    private void startTaskPage4() {

        viewFlipper.setClickable(false);
        viewFlipper.setOnClickListener(null);
        //hide buttons
        setDirectionButtonsHidden(true, page4DirectionButtons, null);
        goToPage(4);
        Handler timerHandler = new Handler();
        timerHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //play tone
                CompassSoundService.playTone(second_probe,1500);
                t3_p4probe.setText("PROBE");

                Vibrator vv = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                // Vibrate for 750 ms during probe presentation would be: vv.vibrate(750);

                // Start without a delay + vibrate for 100ms
                long[] pattern = {0, 100};
                vv.vibrate(pattern, -1); //-1 is important

                //Wait while probe is played
                Handler timerHandler2 = new Handler();
                timerHandler2.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // take away probe cue + wait to proceed
                        t3_p4probe.setText(" ");

                        // indicate end of probe by vibration:
                        Vibrator vvv = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                        // Vibrate for 750 ms during probe presentation would be: vv.vibrate(750);

                        // Start without a delay + vibrate for 100ms
                        long[] pattern = {0, 100};
                        vvv.vibrate(pattern, -1); //-1 is important

                        Handler timerHandler3 = new Handler();
                        timerHandler3.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                //turn on signal
                                CompassSoundService.pause(false);
                                //turn on buttons
                                // show buttons
                                setDirectionButtonsHidden(false, page4DirectionButtons, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        //store first_direction
                                        second_rt = System.currentTimeMillis();
                                        second_direction = getDirection(v.getId(), page4DirectionIds);
                                        //confirm alert
                                        showConfirmationDialog(new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) { //yes action
                                                startTaskPage5();
                                            }
                                        });
                                    }
                                });
                                second_pt = System.currentTimeMillis();
                            }
                        }, 500);
                    }
                }, 1500);
            }
        }, 300);
    }

//page 5 instructions 3, navi ear off, wait for touch
    private void startTaskPage5() {
        float direction = ChristophTaskSet.directionToAzimuth(second_direction);
        ((CompassFeedbackView)findViewById(R.id.t3p5feedbackView)).setArrows(second_probe,direction);
        ((TextView)findViewById(R.id.t3p5fbDegree)).setText(getFeedbackText(second_probe,direction));

        CompassSoundService.pause(true);
        goToPage(5);
        viewFlipper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                startTaskPage6();
            }
        });
        viewFlipper.setClickable(true);
    }

    //page 6 300ms silence, 750ms tone, silence 2500ms (+ wait), then wait for button press
    // on press-> confirmation alert (with navi ear signal on), log orientation
    private void startTaskPage6() {

        viewFlipper.setClickable(false);
        viewFlipper.setOnClickListener(null);
        //hide buttons
        setDirectionButtonsHidden(true, page6DirectionButtons, null);
        page6WaitDialog.setText(getString(R.string.t3_p6wait));

        goToPage(6);
        Handler timerHandler = new Handler();
        timerHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //play tone
                CompassSoundService.playTone(third_probe,1500);
                t3_p6probe.setText("PROBE");

                Vibrator vvv = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                // Vibrate for 750 ms during probe presentation would be: vvv.vibrate(750);

                // Start without a delay
                // Vibrate for 100ms
                long[] pattern = {0, 100};
                vvv.vibrate(pattern, -1); //-1 is important

                //show wait message
                Handler timerHandler2 = new Handler();
                timerHandler2.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // take away probe-cue
                        t3_p6probe.setText(" ");

                        // indicate end of probe by vibration:
                        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                        // Vibrate for 750 ms during probe presentation would be: vv.vibrate(750);

                        // Start without a delay + vibrate for 100ms
                        long[] pattern = {0, 100};
                        v.vibrate(pattern, -1); //-1 is important

                        Handler timerHandler3 = new Handler();
                        timerHandler3.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                //turn on signal
                                CompassSoundService.pause(false);
                                //show appropriate label
                                page6WaitDialog.setText(getString(R.string.t3_p6select));
                                //show buttons
                                setDirectionButtonsHidden(false, page6DirectionButtons, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        //store first_direction
                                        third_rt = System.currentTimeMillis();
                                        third_direction = getDirection(v.getId(), page6DirectionIds);
                                        //confirm alert
                                        showConfirmationDialog(new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) { //yes action
                                                returnIntent.putExtra(FIRST_DIRECTION, first_direction);
                                                returnIntent.putExtra(SECOND_DIRECTION, second_direction);
                                                returnIntent.putExtra(THIRD_DIRECTION, third_direction);

                                                returnIntent.putExtra(FIRST_RT, first_rt);
                                                returnIntent.putExtra(SECOND_RT, second_rt);
                                                returnIntent.putExtra(THIRD_RT, third_rt);

                                                returnIntent.putExtra(FIRST_PT, first_pt);
                                                returnIntent.putExtra(SECOND_PT, second_pt);
                                                returnIntent.putExtra(THIRD_PT, third_pt);
                                                ParticipantLog.writeLogMessageLine("Task three compete: " + third_rt, ts, ParticipantLog.PARTICIPANT_LOG);
                                                startTaskPage7();
                                            }
                                        });
                                    }
                                });
                                third_pt = System.currentTimeMillis();
                            }
                        }, 2500);
                    }
                },1500);
            }
        }, 300);
    }

    private void startTaskPage7() {
        float direction = ChristophTaskSet.directionToAzimuth(third_direction);
        ((CompassFeedbackView)findViewById(R.id.t3p7feedbackView)).setArrows(third_probe,direction);
        ((TextView)findViewById(R.id.t3p7fbDegree)).setText(getFeedbackText(third_probe,direction));

        goToPage(7);
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
