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
public class ChristophTest2 extends ActionBarActivity {

    public static String CONDITION_NAME_PATH1 = "Distortion detection (path 1)";
    public static String CONDITION_NAME_PATH2 = "Distortion detection (path 2)";
    public static String CONDITION_NAME_PATH3 = "Distortion detection (path 3)";
    public static String CONDITION_NAME_PATH4 = "Distortion detection (path 4)";
    public static String[] CONDITION_NAMES = {  CONDITION_NAME_PATH1, CONDITION_NAME_PATH2,
                                                CONDITION_NAME_PATH3, CONDITION_NAME_PATH4};

    private ViewFlipper viewFlipper;

    private PowerManager.WakeLock wl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        selectedPath = -1;
        SoundGenerator.getInstance().pauseEngine(true);
        setContentView(R.layout.christoph_test_2);
        viewFlipper = (ViewFlipper)findViewById(R.id.ct2_viewFlipper);
        //prevent clicking
        viewFlipper.setOnClickListener(null);
        viewFlipper.setClickable(false);
        directionUpdateTextView = (TextView)findViewById(R.id.ct2_headingText);
        updatingDirection = true;
        directionUpdateHandler.post(timerRunnable);


        ((RadioButton)findViewById(R.id.ct2_radioButton0)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectCondition(ChristophEditTestParams.CONDITION_ZERO);
            }
        });
        ((RadioButton)findViewById(R.id.ct2_radioButton1)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectCondition(ChristophEditTestParams.CONDITION_ONE);
            }
        });
        ((RadioButton)findViewById(R.id.ct2_radioButton2)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectCondition(ChristophEditTestParams.CONDITION_TWO);
            }
        });
        ((RadioButton)findViewById(R.id.ct2_radioButton3)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectCondition(ChristophEditTestParams.CONDITION_THREE);
            }
        });
        ((RadioButton)findViewById(R.id.ct2_radioButton4)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectCondition(ChristophEditTestParams.CONDITION_FOUR);
            }
        });


        findViewById(R.id.ct2_path1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectPath(1);
            }
        });
        findViewById(R.id.ct2_path2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectPath(2);
            }
        });
        findViewById(R.id.ct2_path3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectPath(3);
            }
        });
        findViewById(R.id.ct2_path4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectPath(4);
            }
        });
    }


    TextView directionUpdateTextView;
    Handler directionUpdateHandler = new Handler();
    boolean updatingDirection = false;
    Runnable timerRunnable = new Runnable() {
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

    void selectCondition(int condition) {
        distortionCondition = condition;
    }

    private int selectedPath = -1;
    private int distortionCondition = 1;
    void selectPath(int path) {
        //select one out of three distortion conditions
        updatingDirection = false;
        ChristophEditTestParams.setSoundCondition(this, CONDITION_NAMES[path - 1], distortionCondition);
        selectedPath = path;
        ((TextView)findViewById(R.id.ct2_path_chosen)).setText("Path Selected: " + String.valueOf(path));
        viewFlipper.setDisplayedChild(1);
        findViewById(R.id.ct2_start_test).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTest();
            }
        });
    }


    void startTest() {


        // Acquire a wake lock to improve processing
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "Test2");
        wl.acquire();

        SoundGenerator.getInstance().pauseEngine(false);
        viewFlipper.setDisplayedChild(2);
        findViewById(R.id.ct2_test_finished).setOnClickListener(new View.OnClickListener() {
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
        SoundGenerator.getInstance().pauseEngine(true);
        viewFlipper.setDisplayedChild(3);
        findViewById(R.id.ct2_yes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noticedDisturbance(true);
            }
        });
        findViewById(R.id.ct2_no).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noticedDisturbance(false);
            }
        });
    }

    void noticedDisturbance(boolean noticed) {
        float[] params = SoundGenerator.getInstance().getParams();
        float distortionAmplitude = params[0];
        float distortionCentre = params[1];
        float distortionWidth = params[2];

        String line = ParticipantLog.getDateTime() +
                ",2," +
                String.valueOf(selectedPath)+","+
                String.valueOf(distortionCondition)+"," +
                String.valueOf(noticed) + ","+
                String.valueOf(distortionAmplitude) + "," +
                String.valueOf(distortionCentre) + "," +
                String.valueOf(distortionWidth);

        ParticipantLog.writeLogMessageLine(line, this, ParticipantLog.TEST_LOG);
        SoundGenerator.getInstance().pauseEngine(false);
        AdjustsCodingActivity.setCoding(this);
        Intent intent = new Intent(this, ChristophTestChoicePage.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        //do nothing
        if (selectedPath == -1) {
            updatingDirection = false;
            super.onBackPressed();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        updatingDirection = false;
        if (wl != null && wl.isHeld()) {
            wl.release();
        }
        finish();
    }
}
