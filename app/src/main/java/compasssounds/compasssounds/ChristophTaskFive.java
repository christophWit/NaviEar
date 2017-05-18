package compasssounds.compasssounds;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ViewFlipper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;

/**
 * Created by james on 02/04/15.
 */
public class ChristophTaskFive extends ChristophTask {

    public class Task5ResultStruct {
        public int firstAzimuth;
        public float secondAzimuth;
        public float responseAngle;
        public long presentationTime;
        public long responseTime;
    }

    private TextView trialsLeft;
    private CompassFeedbackView diagramView;
    private ViewFlipper viewFlipper;
    private SeekBar seekBar;
    private Button doneButton;
    private int selectedAngle;
    private TextView t5p2Inst2;

    private static int[] angles = {0, 45, 90, 135, 180, 215, 270, 315};
    private static int n_rounds = 2;


    private List<Integer> first_angles;
    private List<Integer> second_angles;
    private int trialNumber = 0;

    private Task5ResultStruct currentResult;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Random rng = new Random(System.currentTimeMillis());
        first_angles = new ArrayList<Integer>();
        second_angles = new ArrayList<Integer>();
        //first [] - azimuth , second [] - number
        for (int a = 0; a < angles.length; ++a) {
            //create new list of 1 to 8 x azimuth

            for (int k = 0; k < n_rounds; ++k) {
                first_angles.add(angles[a]);
                second_angles.add(rng.nextInt(321)+20);
            }
        }
        Collections.shuffle(first_angles, rng);

        String message = "Started task 5";
        ParticipantLog.writeLogMessageLine(message, this, ParticipantLog.PARTICIPANT_LOG);



        setContentView(R.layout.christoph_task_five);
        viewFlipper = (ViewFlipper)findViewById(R.id.viewFlipper);
        diagramView = (CompassFeedbackView)findViewById(R.id.t5p3feedbackView);
        seekBar = (SeekBar)findViewById(R.id.t5_seekBar);
        t5p2Inst2 = (TextView)findViewById(R.id.t5p2Inst2);
        trialsLeft = (TextView)findViewById(R.id.t5p4trialsLeft);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                selectedAngle = progress - 190;
                diagramView.setArrows(0,selectedAngle);
                diagramView.invalidate();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        doneButton = (Button)findViewById(R.id.t5_button_done);


        ((Button)findViewById(R.id.t5p4_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTaskPage2();
            }
        });

        //prevent clicking
        viewFlipper.setOnClickListener(null);
        viewFlipper.setClickable(false);
        startTaskPage1();
    }

    private void goToPage(int newPage) {
        viewFlipper.setDisplayedChild(newPage - 1);
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

    //page 2 - listen to tones
    //1 sec
    //300ms tone1
    //100ms tone2
    private void startTaskPage2() {
        CompassSoundService.pause(true);
        t5p2Inst2.setText("...");
        currentResult = new Task5ResultStruct();
        currentResult.firstAzimuth = first_angles.get(trialNumber);
        currentResult.secondAzimuth = first_angles.get(trialNumber)+second_angles.get(trialNumber);

        viewFlipper.setClickable(false);
        viewFlipper.setOnClickListener(null);

        goToPage(2);
        //wait one sec
        final Handler timerHandler = new Handler();
        timerHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //play first tone
                CompassSoundService.playTone(first_angles.get(trialNumber), 1500);
                t5p2Inst2.setText("1st PROBE");

                Vibrator w = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                // Vibrate to indicate probe presentation start:

                // Start without a delay
                // Vibrate for 100ms
                long[] pattern = {0, 100};
                w.vibrate(pattern, -1); //-1 is important

                //play next tone 400ms after
                timerHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //play second tone
                        CompassSoundService.playTone(first_angles.get(trialNumber) + second_angles.get(trialNumber), 1500);
                        t5p2Inst2.setText("2nd PROBE");

                        Vibrator ww = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                        // Vibrate to indicate probe presentation start:

                        // Start without a delay
                        // Vibrate for 100ms
                        long[] pattern = {0, 100};
                        ww.vibrate(pattern, -1); //-1 is important

                        timerHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                //go to third page
                                startTaskPage3();
                            }
                        }, 2500);
                    }
                }, 2500);
            }
        }, 1000);
    }

    //page 3 offer response widget (+ confirmation)
    private void startTaskPage3() {
        currentResult.presentationTime = System.currentTimeMillis();
        selectedAngle = 0;
        seekBar.setProgress(180);
        diagramView.setArrows(0,selectedAngle);
        diagramView.invalidate();

        goToPage(3);
        viewFlipper.setClickable(false);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConfirmationDialog( new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) { //yes action
                        //log trial
                        currentResult.responseAngle = selectedAngle;
                        currentResult.responseTime = System.currentTimeMillis();
                        LogResults(trialNumber, currentResult);

                        nextTrialOrEnd();
                    }
                },
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) { //no action
                        trialNumber--;
                        nextTrialOrEnd();
                    }
                });
            }
        });
    }

    private void nextTrialOrEnd() {
        trialNumber++;
        if (trialNumber >= n_rounds * angles.length) {
            endTask(ChristophTaskSet.TASK_COMPLETE);
        } else {
            startTaskPage4();
        }
    }

    //page 4 navi ear remains off in-between trials - press button to go to page 1
    private void startTaskPage4() {
        int nTrialsLeft = (n_rounds * angles.length) - trialNumber;
        trialsLeft.setText(String.valueOf(nTrialsLeft) + " trials Left");
        goToPage(4);
        viewFlipper.setClickable(false);
        viewFlipper.setOnClickListener(null);

    }

    private void LogResults(int trialNumber, Task5ResultStruct result) {
        Calendar calendar = new GregorianCalendar(TimeZone.getDefault());
        Date trialTime = new Date();
        calendar.setTime(trialTime);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String strDate = sdf.format(calendar.getTime());

        LocationLogger locationLogger = CompassSoundService.getInstance().locationLogger;
        String message = strDate + ", ";
        if (locationLogger != null) {
            message += locationLogger.mLatitude + ", " + locationLogger.mLongitude + ", " + locationLogger.mLocationAccuracy + ",";
        } else {
            message += "0,0,0,";
        }

        message += ",";

        message += "5, ";
        message += trialNumber + ", ";
        message += result.firstAzimuth + ", ";
        message += result.secondAzimuth + ", ";
        message += result.responseAngle + ", ";
        message += result.presentationTime + ", ";
        message += result.responseTime + ", ";
        message += "0" + ", "; //familiarty
        message += "0" + ", "; //orientationability
        message += "0" + ", "; //ambientLoudness
        message += String.valueOf(SoundGenerator.getInstance().getAzimuthConstant()) + ", ";
        message += String.valueOf(SoundGenerator.getInstance().getAzimuthMultiplier()) + ", ";

        float[] params = SoundGenerator.getInstance().getParams();
        message += String.valueOf(params[0]) + ", ";
        message += String.valueOf(params[1]) + ", ";
        message += String.valueOf(params[2]);

        ParticipantLog.writeLogMessageLine(message, this, ParticipantLog.TASK_LOG);
    }


}
