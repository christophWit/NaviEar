package compasssounds.compasssounds;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;

import javax.xml.transform.Result;

/**
 * Created by james on 26/02/15.
 * deals with loading the task state, and executing the activities in the right order
 */
public class ChristophTaskSet extends ActionBarActivity {

    public static final String taskName = "ChristophTaskSet";
    public static final long DEFAULT_TIME_BETWEEN_TASKS = 20000;
    public class ResultStruct {
        public int order;
        public float testAzimuth;
        public float responseAzimuth;
        public long presentationTime;
        public long responseTime;
    }

    static final int TASK_ONE = 1;
    static final int TASK_TWO = 2;
    static final int TASK_THREE = 3;
    static final int TASK_FOUR = 4;
    static final int TASK_FIVE = 5;
    static final int TASK_WAIT = 0;

    static final int TASK_INCOMPLETE = 0;
    static final int TASK_COMPLETE = 1;
    static final int FAMILIARITY_COMPLETE = 2;
    static final int WAIT_COMPLETE = 3;

    static final int N_TASKS = 4; //number of task types
    static final int[] TASKS_ARRAY = {TASK_ONE, TASK_TWO,TASK_THREE,TASK_FOUR}; //types of tasks
    static final int[] TASKS_NUMBER = {8,8,8,2}; //number per set
    static final int[] TASKS_AZIMUTHS = {2,2,3,0};


    public static final String[] directions = {"N","S","E","W","NW","NE","SE","SW"};
    public static final float[] azimuths = {0,180,90,270,315,45,135,225};
    public static float directionToAzimuth(String direction) {
        for(int i = 0; i < directions.length; i++) {
            if (directions[i].equalsIgnoreCase(direction)) {
                return azimuths[i];
            }
        }
        return -1;
    }

    static final boolean[] FAM_TASK = {true,true,true,false}; //whether fam task is done after

    static final String STATE_TIME_LAST_COMPLETED = "time_last_completed";
    static final String STATE_TASKS_STARTED = "started";


    private Bundle taskState;
    private Random rng;
    private List<ResultStruct> results = new ArrayList<ResultStruct>();

    //this is a bit hacky but works
    private static int[][] fillWithRandomNumbers(int[][] orders, int t) {

        Random rng = new Random(System.currentTimeMillis());
        int shuffles = 0;

        //first [] - azimuth , second [] - number
        for (int currAzimuth = 0; currAzimuth < TASKS_AZIMUTHS[t]; ++currAzimuth) {
            //create new list of 1 to 8 x azimuth
            List<Integer> indexes = new ArrayList<Integer>();
            for (int k = 0; k < TASKS_NUMBER[t]; ++k) {
                indexes.add(k);
            }

            boolean ok = false;
            while(!ok) {
                Collections.shuffle(indexes,rng);
                shuffles++;
                if (shuffles > 1000) {
                    //perhaps this is impossible, start again.
                    return fillWithRandomNumbers(orders,t);
                }

                ok = true; //maybe this shuffle is good?
                //lets check for repititions
                if (currAzimuth > 0) {

                    for (int prevAzimuth = 0; prevAzimuth < currAzimuth; ++prevAzimuth) {
                        for (int prevOrder = 0; prevOrder < TASKS_NUMBER[t]; ++prevOrder) {
                            if (orders[prevAzimuth][prevOrder] == indexes.get(prevOrder)) {
                                ok = false; //not ok so go back and get another set
                                break;
                            }
                        }
                        if (!ok) break;
                    }
                }
            }

            //this order is ok random order
            for (int prevOrder = 0; prevOrder < TASKS_NUMBER[t]; ++prevOrder) {
                orders[currAzimuth][prevOrder] = indexes.get(prevOrder);
            }

        }
        return orders;
    }

    public static void schedule(Context context) {
        Bundle arguments = new Bundle(); //place holder for now, but will fill with useful info
        arguments.putBoolean("started",false);



        //list of tasks left
        for (int t = 0; t < N_TASKS; ++t) {
            String taskName = "task" + TASKS_ARRAY[t];
            arguments.putInt(taskName, TASKS_NUMBER[t]);

            int[][] orders = new int[TASKS_AZIMUTHS[t]][TASKS_NUMBER[t]];

            orders = fillWithRandomNumbers(orders,t);

            for (int k = 0; k < TASKS_NUMBER[t]; ++k) {
                for (int j = 0; j < TASKS_AZIMUTHS[t]; ++j) {
                    String key = taskName + "_" + (k+1) + "_"+j;
                    arguments.putString(key, directions[orders[j][k]]);
                }
            }
        }

        arguments.putBoolean("task5done",false);

        TaskScheduler.storeNewTask(context,ChristophTaskSet.taskName, arguments);
    }

    private String getDirection(int task, int trial, int azimuthn) {
        String key = "task" + task + "_" +trial+ "_"+azimuthn;

        String s = taskState.getString(key);
        Log.i("ChristophTaskSet","Retrieving key: "+key+ " = " + s);
        return s;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getIntent().getExtras().getLong("ISI", DEFAULT_TIME_BETWEEN_TASKS);
        //check if has actually been scheduled?
        List<String> tasks = TaskScheduler.getPendingTasks(this);
        if (!tasks.contains(taskName)) {
            showToast("Voluntarily started new task");
            schedule(this);
        }

        //otherwise task has already been scheduled and we are either starting or resuming it...
        rng = new Random(System.nanoTime());
        taskState = TaskScheduler.loadTaskState(this, taskName);

        //check if started - if not, then add header
        if (!taskState.getBoolean(STATE_TASKS_STARTED,false)) {
            LogResultsHeader();
        }

        taskState.putBoolean(STATE_TASKS_STARTED, true); //definitely started now
        doNextTask();

    }

    private void saveState() {
        TaskScheduler.saveTaskState(this,taskName,taskState);
    }

    private void doWait() {
        Intent intent = new Intent(this, ChristophWaitTask.class);
        //add parameters
        long lastCompleted = taskState.getLong(STATE_TIME_LAST_COMPLETED, 0); //get the time since last task was done
        intent.putExtra(STATE_TIME_LAST_COMPLETED, lastCompleted);
        startActivityForResult(intent, TASK_WAIT);
    }

    private void doNextTask() {

        saveState();


        int tasksLeft = getTotalTasksLeft();

        if (tasksLeft == 0) {
            //done all major tasks
            if (!taskState.getBoolean("task5done",false)) {
                startTaskFive();
            } else {
                TaskScheduler.taskComplete(this,taskName);
                Intent intent = new Intent(this, ChristophSessionComplete.class);
                startActivity(intent);
                finish();
            }
        }


        long lastCompleted = taskState.getLong(STATE_TIME_LAST_COMPLETED, 0); //get the time since last task was done
        long timeNow = System.currentTimeMillis();

        SharedPreferences prefs = getSharedPreferences(ParticipantLog.PARTICIPANT_PREFS, 0);
        long TIME_BETWEEN_TASKS = prefs.getLong("TIME_BETWEEN_TASKS",20000);

        if (timeNow < (lastCompleted + TIME_BETWEEN_TASKS)) {
            doWait(); //we have to wait
            return;
        }

        if (BuildConfig.DEBUG) {
            Log.i("ChristophTaskSet", "Tasks left: " + tasksLeft);
            TaskScheduler.dump(this);
        }

        //chose random order to query tasks for x left
        List<Integer> order = new ArrayList<Integer>();
        for(int i = 0; i < N_TASKS; ++i) {
            order.add(new Integer( TASKS_ARRAY[i]));
        }
        Collections.shuffle(order,rng);

        for(int i = 0; i < N_TASKS; ++i) {
            int left = taskState.getInt("task"+order.get(i),0);
            if (left > 0) {
                Log.i("ChristophTaskSet", "Launching task " + order.get(i) + " which has " + left + " runs left");
                launchTask(order.get(i));
                return;
            }
        }


    }

    public int getTotalTasksLeft() {
        int total = 0;
        for(int i = 0; i < N_TASKS; ++i) {
            int left = taskState.getInt("task"+TASKS_ARRAY[i],0);
            total += left;
        }
        return total;
    }

    private void launchTask(int task) {
        switch(task) {
            case TASK_ONE:
                startTaskOne();
                break;
            case TASK_TWO:
                startTaskTwo();
                break;
            case TASK_THREE:
                startTaskThree();
                break;
            case TASK_FOUR:
                startTaskFour();
                break;
        }
    }



    private void showToast(String msg) {
        Toast error = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        error.show();
    }

    private void initResults(int n) {
        results.clear();
        for(int i = 0; i < n; i++) {
            ResultStruct rs = new ResultStruct();
            rs.order = i+1;
            results.add(rs);
        }
    }

    private void startTaskOne() {
        initResults(2);
        Intent intent = new Intent(this, ChristophTaskOne.class);
        //add parameters
        intent.putExtra("TEST", false);
        int trialN = trialNumber(TASK_ONE);
        String first_label = getDirection(TASK_ONE, trialN, 0);
        String second_label = getDirection(TASK_ONE, trialN, 1);
        intent.putExtra(ChristophTaskOne.FIRST_LABEL,first_label);
        intent.putExtra(ChristophTaskOne.SECOND_LABEL, second_label);
        intent.putExtra(ChristophTask.CURRENT_TRIAL,trialN);
        results.get(0).testAzimuth = directionToAzimuth(first_label);
        results.get(1).testAzimuth = directionToAzimuth(second_label);
        startActivityForResult(intent, TASK_ONE);
    }
    private void startTaskTwo() {
        initResults(2);
        int trialN = trialNumber(TASK_TWO);
        float first_probe = directionToAzimuth(getDirection(TASK_TWO,trialN,0));
        float second_probe = directionToAzimuth(getDirection(TASK_TWO,trialN,1));
        results.get(0).testAzimuth = first_probe;
        results.get(1).testAzimuth = second_probe;

        Intent intent = new Intent(this, ChristophTaskTwo.class);
        //add parameters
        intent.putExtra("TEST", false);
        intent.putExtra(ChristophTaskThree.FIRST_PROBE, first_probe);
        intent.putExtra(ChristophTaskThree.SECOND_PROBE, second_probe);
        intent.putExtra(ChristophTask.CURRENT_TRIAL,trialN);
        startActivityForResult(intent, TASK_TWO);
    }



    private void startTaskThree() {
        initResults(3);
        int trialN = trialNumber(TASK_THREE);
        float first_probe = directionToAzimuth(getDirection(TASK_THREE,trialN,0));
        float second_probe = directionToAzimuth(getDirection(TASK_THREE,trialN,1));
        float third_probe = directionToAzimuth(getDirection(TASK_THREE,trialN,2));
        results.get(0).testAzimuth = first_probe;
        results.get(1).testAzimuth = second_probe;
        results.get(2).testAzimuth = third_probe;

        Intent intent = new Intent(this, ChristophTaskThree.class);
        //add parameters
        intent.putExtra("TEST", false);
        intent.putExtra(ChristophTaskThree.FIRST_PROBE, first_probe);
        intent.putExtra(ChristophTaskThree.SECOND_PROBE, second_probe);
        intent.putExtra(ChristophTaskThree.THIRD_PROBE, third_probe);
        intent.putExtra(ChristophTask.CURRENT_TRIAL,trialN);
        startActivityForResult(intent, TASK_THREE);
    }

    private void startTaskFour() {
        initResults(1);
        int trialN = trialNumber(TASK_FOUR);
        Intent intent = new Intent(this, ChristophTaskFour.class);
        intent.putExtra(ChristophTask.CURRENT_TRIAL,trialN);
        startActivityForResult(intent, TASK_FOUR);
    }

    private void startTaskFive() {
        //task five writes its own results
        Intent intent = new Intent(this, ChristophTaskFive.class);
        intent.putExtra(ChristophTask.CURRENT_TRIAL,1);
        startActivityForResult(intent, TASK_FIVE);
    }

    private void startFamiliarityTask(int requestCode) {
        Intent intent = new Intent(this, ChristophFamiliarityTask.class);
        intent.putExtra("TEST", false);
        startActivityForResult(intent, requestCode);
    }

    private int trialNumber(int task) {
        //get trial number
        int trialsLeft = taskState.getInt("task"+task,1) - 1;

        int trialNumber = 0;
        for(int i = 0; i < N_TASKS; ++i) {
            if (TASKS_ARRAY[i] == task) {
                trialNumber = TASKS_NUMBER[i] - trialsLeft;
                break;
            }
        }
        return trialNumber;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == TASK_COMPLETE) {  //store results and launch familiarity task

            if (requestCode == TASK_ONE) {
                results.get(0).responseAzimuth = data.getFloatExtra(ChristophTaskOne.FIRST_AZIMUTH, -1);
                results.get(1).responseAzimuth = data.getFloatExtra(ChristophTaskOne.SECOND_AZIMUTH, -1);
                results.get(0).responseTime = data.getLongExtra(ChristophTaskOne.FIRST_RT, -1);
                results.get(1).responseTime = data.getLongExtra(ChristophTaskOne.SECOND_RT, -1);
                results.get(0).presentationTime = data.getLongExtra(ChristophTaskOne.FIRST_PT, -1);
                results.get(1).presentationTime = data.getLongExtra(ChristophTaskOne.SECOND_PT, -1);
            }else if (requestCode == TASK_TWO) {
                results.get(0).responseAzimuth = data.getFloatExtra(ChristophTaskTwo.FIRST_AZIMUTH, -1);
                results.get(1).responseAzimuth = data.getFloatExtra(ChristophTaskTwo.SECOND_AZIMUTH, -1);
                results.get(0).responseTime = data.getLongExtra(ChristophTaskTwo.FIRST_RT, -1);
                results.get(1).responseTime = data.getLongExtra(ChristophTaskTwo.SECOND_RT, -1);
                results.get(0).presentationTime = data.getLongExtra(ChristophTaskTwo.FIRST_PT, -1);
                results.get(1).presentationTime = data.getLongExtra(ChristophTaskTwo.SECOND_PT, -1);
            } else if (requestCode == TASK_THREE) {
                results.get(0).responseAzimuth = directionToAzimuth(data.getStringExtra(ChristophTaskThree.FIRST_DIRECTION));
                results.get(1).responseAzimuth = directionToAzimuth(data.getStringExtra(ChristophTaskThree.SECOND_DIRECTION));
                results.get(2).responseAzimuth = directionToAzimuth(data.getStringExtra(ChristophTaskThree.THIRD_DIRECTION));
                results.get(0).responseTime = data.getLongExtra(ChristophTaskThree.FIRST_RT, -1);
                results.get(1).responseTime = data.getLongExtra(ChristophTaskThree.SECOND_RT, -1);
                results.get(2).responseTime = data.getLongExtra(ChristophTaskThree.THIRD_RT, -1);
                results.get(0).presentationTime = data.getLongExtra(ChristophTaskThree.FIRST_PT, -1);
                results.get(1).presentationTime = data.getLongExtra(ChristophTaskThree.SECOND_PT, -1);
                results.get(2).presentationTime = data.getLongExtra(ChristophTaskThree.THIRD_PT, -1);
            } else if (requestCode == TASK_FOUR) {

            } else if (requestCode == TASK_FIVE) {
                taskState.putBoolean("task5done",true);
                doNextTask();
            }
            startFamiliarityTask(requestCode);
        } else if (resultCode == FAMILIARITY_COMPLETE) {
            //this means we finished a whole task.. so can go to next one after wait
            long timeNow = System.currentTimeMillis();
            taskState.putLong(STATE_TIME_LAST_COMPLETED, timeNow);
            int familiarity = data.getIntExtra(ChristophFamiliarityTask.FAMILIARITY, -1);
            int orientationAbility = data.getIntExtra(ChristophFamiliarityTask.ORIENTATION_ABILITY, -1);
            int ambientLoudness = data.getIntExtra(ChristophFamiliarityTask.AMBIENT_LOUDNESS, -1);

            //store data

            //get trial number
            int trialNumber = trialNumber(requestCode);


            //log the results
            for(int i = 0; i < results.size(); ++i) {
                LogResults(requestCode, trialNumber,results.get(i),Integer.toString(familiarity),Integer.toString(orientationAbility),Integer.toString(ambientLoudness));
            }

            //update trials left
            int trialsLeft = taskState.getInt("task"+requestCode,1) - 1;
            taskState.putInt("task" + requestCode, trialsLeft);

            doNextTask();
        } else if (resultCode == ChristophTaskSet.WAIT_COMPLETE) {
            Intent intent = new Intent(this, ParticipantActivity.class);
            startActivity(intent);
        } else if (resultCode == ChristophTaskSet.TASK_INCOMPLETE) {
            long timeNow = System.currentTimeMillis();
            taskState.putLong(STATE_TIME_LAST_COMPLETED, timeNow); //still have to wait
            doNextTask();
        }
    }

    private void LogResultsHeader() {
        String header = "datetime, lat, long, acc, set_nr, task_id, trial_nr, task_subtype, test_azimuth, response_azimuth, difference_azimuth, presentation_time, response_time, familiarity, orientationAbility, ambientLoudness, azimuthConstant, azimuthMultiplier,azimuthDistortionAmplitude, azimuthDistortionCentre, azimuthDistortionWidth";
        ParticipantLog.writeLogMessageLine(header, this, ParticipantLog.TASK_LOG);
    }

    private void LogResults(int taskID, int trialNumber, ResultStruct result, String familiarity, String orientationAbility, String ambientLoudness) {
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

        message += TaskScheduler.getTrialRuns(this, taskName) + ",";

        if (taskID == TASK_ONE) {
            message += "1, ";
        } else if (taskID == TASK_TWO) {
            message += "2, ";
        } else if (taskID == TASK_THREE) {
            message += "3, ";
        } else if (taskID == TASK_FOUR) {
            message += "4, ";
        }

        message += trialNumber + ", ";
        message += result.order + ", ";
        message += result.testAzimuth + ", ";
        message += result.responseAzimuth + ", ";

        message += ChristophTask.differenceInAzimuth(result.testAzimuth, result.responseAzimuth) + ", ";

        message += result.presentationTime + ", ";
        message += result.responseTime + ", ";
        message += familiarity + ", ";
        message += orientationAbility + ", ";
        message += ambientLoudness + ", ";
        message += String.valueOf(SoundGenerator.getInstance().getAzimuthConstant()) + ", ";
        message += String.valueOf(SoundGenerator.getInstance().getAzimuthMultiplier()) + ", ";

        float[] params = SoundGenerator.getInstance().getParams();
        message += String.valueOf(params[0]) + ", ";
        message += String.valueOf(params[1]) + ", ";
        message += String.valueOf(params[2]);

        ParticipantLog.writeLogMessageLine(message, this, ParticipantLog.TASK_LOG);
    }


}
