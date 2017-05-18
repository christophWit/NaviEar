package compasssounds.compasssounds;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.ViewFlipper;

/**
 * Created by james on 20/03/15.
 */
public class ChristophTest3  extends ActionBarActivity {

    public static String CONDITION_NAME_INT1 = "Interference (Int 1: NE)";
    public static String CONDITION_NAME_INT2 = "Interference (Int 2: SE)";
    public static String CONDITION_NAME_INT3 = "Interference (Int 3: SW)";
    public static String CONDITION_NAME_INT4 = "Interference (Int 4: NW)";

    public static String[] CONDITION_NAMES = {  CONDITION_NAME_INT1, CONDITION_NAME_INT2,
            CONDITION_NAME_INT3, CONDITION_NAME_INT4 };

    private ViewFlipper viewFlipper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selectedCondition = -1;
        SoundGenerator.getInstance().pauseEngine(true);
        setContentView(R.layout.christoph_test_3);
        viewFlipper = (ViewFlipper)findViewById(R.id.ct3_viewFlipper);
        //prevent clicking
        viewFlipper.setOnClickListener(null);
        viewFlipper.setClickable(false);
        directionUpdateTextView = (TextView)findViewById(R.id.ct3_headingText);
        updatingDirection = true;
        directionUpdateHandler.post(directionUpdateRunnable);



        ((RadioButton)findViewById(R.id.ct3_radioButton1)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectDistortionCondition(ChristophEditTestParams.CONDITION_ONE);
            }
        });
        ((RadioButton)findViewById(R.id.ct3_radioButton2)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectDistortionCondition(ChristophEditTestParams.CONDITION_TWO);
            }
        });
        ((RadioButton)findViewById(R.id.ct3_radioButton3)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectDistortionCondition(ChristophEditTestParams.CONDITION_THREE);
            }
        });

        findViewById(R.id.ct3_condition1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectCondition(1);
            }
        });
        findViewById(R.id.ct3_condition2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectCondition(2);
            }
        });
        findViewById(R.id.ct3_condition3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectCondition(3);
            }
        });
        findViewById(R.id.ct3_condition4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectCondition(4);
            }
        });

    }

    TextView directionUpdateTextView;
    Handler directionUpdateHandler = new Handler();
    boolean updatingDirection = false;
    Runnable directionUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            if (updatingDirection) {
                int currentAzimuth = (int) CompassSoundService.getInstance().getCurrentAzimuth();
                if (directionUpdateTextView != null) {
                    directionUpdateTextView.setText("Current direction: " + String.valueOf(currentAzimuth) + " degrees");
                }
                directionUpdateHandler.postDelayed(this, 100);
            }
        }
    };

    void selectDistortionCondition(int condition) {
        distortionCondition = condition;
    }

    private int selectedCondition = -1;
    private int distortionCondition = 1;
    private PowerManager.WakeLock wl;

    void selectCondition(int path) {
        //select one out of three distortion conditions
        updatingDirection = false;
        ChristophEditTestParams.setSoundCondition(this, CONDITION_NAMES[path - 1], distortionCondition);
        selectedCondition = path;
        testing = true;
        timerHandler.postDelayed(timerRunnable, 0);
        SoundGenerator.getInstance().pauseEngine(false);


        // Acquire a wake lock to improve processing
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "Test3");
        wl.acquire();

        viewFlipper.setDisplayedChild(1);
        findViewById(R.id.ct3_test_finished).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopTest();
            }
        });
    }


    void stopTest() {
        if (wl != null && wl.isHeld()) {
            wl.release();
        }
        testing = false;
        float[] params = SoundGenerator.getInstance().getParams();
        float distortionAmplitude = params[0];
        float distortionCentre = params[1];
        float distortionWidth = params[2];

        String line = ParticipantLog.getDateTime() +
                ",3," +
                String.valueOf(selectedCondition)+","+
                String.valueOf(distortionCondition)+"," +
                String.valueOf("") + ","+     // James? still need to get the 'true' out of the log... and in case of not true, maybe put a zero - empty entries shift stuff around in matlab.. and just always put a zero for test 2 and 1 here...
                String.valueOf(distortionAmplitude) + "," +
                String.valueOf(distortionCentre) + "," +
                String.valueOf(distortionWidth);

        ParticipantLog.writeLogMessageLine(line, this, ParticipantLog.TEST_LOG);

        AdjustsCodingActivity.setCoding(this);
        Intent intent = new Intent(this, ChristophTestChoicePage.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        //do nothing
        if (selectedCondition == -1) {
            updatingDirection = false;
            super.onBackPressed();
        }
    }

    @Override
    protected void onPause() {
        if (wl != null && wl.isHeld()) {
            wl.release();
        }
        updatingDirection = false;
        super.onPause();
        testing = false;
        finish();
    }

    //runs without a timer by reposting this handler at the end of the runnable
    private static boolean testing = false;
    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {
            if (testing && CompassSoundService.isRunning()) {
                CompassSoundService.getInstance().locationLogger.logMessageLine();
                timerHandler.postDelayed(this, 200); // 5 times a scond
            }
        }

    };

}