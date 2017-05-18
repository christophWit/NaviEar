
package compasssounds.compasssounds;

        import android.content.DialogInterface;
        import android.os.Bundle;
        import android.view.View;
        import android.widget.TextView;
        import android.widget.ViewFlipper;

/**
 * Created by annika_james on 27/02/15.
 * present the observer a direction label (i.e. e.g. NORTH, NORTH-EAST, WEST),
 * and ask them to point with their head to the direction that corresponds to that signal.
 */
public class ChristophTaskOne extends ChristophTask {

    // do we need to initialise/define these strings as well? Or not, because they are part of what is 'fed into' the task?
    public static final String FIRST_LABEL = "FIRST_LABEL";
    public static final String SECOND_LABEL = "SECOND_LABEL";

    public static final String FIRST_AZIMUTH = "FIRST_DIRECTION";
    public static final String SECOND_AZIMUTH = "SECOND_DIRECTION";

    public static final String FIRST_PT = "FIRST_PT";
    public static final String SECOND_PT = "SECOND_PT";

    public static final String FIRST_RT = "FIRST_RT";
    public static final String SECOND_RT = "SECOND_RT";

    // so it is ok to use the same names within each task, as they are private? Or do they need to be different?
    private ViewFlipper viewFlipper1;


    private TextView t1p1Instr1;  // txt with trial counter in it
    private TextView t1p2firstLabel;  // the FIRST_LABEL
    private TextView t1p4secondLabel;   // the SECOND_LABEL


    private int currentPage = 0;



    //page 1 display instructions, "Orient toward direction displayed"; wait for touch to flip page
    //page 2 display FIRST_LABEL ask orientation phone; wait for touch to: record data, switch on NaviEar, go to confirm-interface (if yes: flip page; if no, 'DISCARD' routine)
    //page 3 displays instructions; wait for touch to flip page
    //page 4 display SECOND_LABEL ask orientation phone; wait for touch to: record data, go to confirm-interface (if yes: go to familiarity interface; if no, 'DISCARD' routine)

    //stimuli:
    private String first_label;
    private String second_label;

    //results:
    private float first_azimuth;
    private float second_azimuth;
    private long first_pt = -1;
    private long second_pt = -1;
    private long first_rt = -1;
    private long second_rt = -1;

    // label is displayed (until response touch/page-flip)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        first_label = getIntent().getExtras().getString(FIRST_LABEL);
        second_label = getIntent().getExtras().getString(SECOND_LABEL);

        setContentView(R.layout.christoph_task_one);
        viewFlipper1 = (ViewFlipper)findViewById(R.id.viewFlipper1);


        t1p2firstLabel = (TextView)findViewById(R.id.t1p2firstLabel);
        t1p4secondLabel = (TextView)findViewById(R.id.t1p4secondLabel);
        t1p2firstLabel.setText(first_label);
        t1p4secondLabel.setText(second_label);

        t1p1Instr1 = (TextView)findViewById(R.id.t1p1Instr1);
        t1p1Instr1.setText("Trial "+currentTrial+" / 8");


        //prevent clicking
        viewFlipper1.setOnClickListener(null);
        viewFlipper1.setClickable(false);

        startTaskPage1();
    }

    private void startTaskPage1() {

        //ensure engine is in correct state...
        CompassSoundService.pause(true);
        goToPage(1);
        viewFlipper1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                startTaskPage2();
            }
        });
        viewFlipper1.setClickable(true);

    }

    private void startTaskPage2() {

        viewFlipper1.setClickable(false);
        viewFlipper1.setOnClickListener(null);

        goToPage(2);

        viewFlipper1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                //record direction
                first_rt = System.currentTimeMillis();
                first_azimuth = CompassSoundService.getInstance().getCurrentAzimuth();
                CompassSoundService.pause(false);
                showConfirmationDialog(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) { //yes action
                        startTaskPage3();
                    }
                });
            }
        });
        viewFlipper1.setClickable(true);
        first_pt = System.currentTimeMillis();
    }

    private void startTaskPage3() {
        float desired_azimuth = ChristophTaskSet.directionToAzimuth(first_label);
        ((CompassFeedbackView)findViewById(R.id.t1p3feedbackView)).setArrows(desired_azimuth,first_azimuth);
        ((TextView)findViewById(R.id.t1p3fbDegree)).setText(getFeedbackText(desired_azimuth,first_azimuth));
        goToPage(3);

        viewFlipper1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                startTaskPage4();
            }
        });
        viewFlipper1.setClickable(true);
    }

    private void startTaskPage4() {
        viewFlipper1.setClickable(false);
        viewFlipper1.setOnClickListener(null);
        goToPage(4);
        second_pt = System.currentTimeMillis();

        //display click to go to next screen

        viewFlipper1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                //record direction
                second_rt = System.currentTimeMillis();
                second_azimuth = CompassSoundService.getInstance().getCurrentAzimuth();
                showConfirmationDialog(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) { //yes action
                        returnIntent.putExtra(FIRST_AZIMUTH,first_azimuth);
                        returnIntent.putExtra(SECOND_AZIMUTH, second_azimuth);
                        returnIntent.putExtra(FIRST_RT,first_rt);
                        returnIntent.putExtra(SECOND_RT, second_rt);
                        returnIntent.putExtra(FIRST_PT,first_pt);
                        returnIntent.putExtra(SECOND_PT, second_pt);
                        ParticipantLog.writeLogMessageLine("Task three compete: " + second_rt, ts, ParticipantLog.PARTICIPANT_LOG);
                        startTaskPage5();
                    }
                });

            }
        });
        viewFlipper1.setClickable(true);

    }

    private void startTaskPage5() {
        float desired_azimuth = ChristophTaskSet.directionToAzimuth(second_label);
        ((CompassFeedbackView)findViewById(R.id.t1p5feedbackView)).setArrows(desired_azimuth,second_azimuth);
        ((TextView)findViewById(R.id.t1p5fbDegree)).setText(getFeedbackText(desired_azimuth,second_azimuth));
        goToPage(5);

        viewFlipper1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endTask(ChristophTaskSet.TASK_COMPLETE);
            }
        });
    }



    private void goToPage(int newPage) {
        viewFlipper1.setDisplayedChild(newPage - 1);
    }

    @Override
    protected void onPause() {
        viewFlipper1.setOnClickListener(null);
        super.onPause();
    }
}


