package compasssounds.compasssounds;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by james on 20/03/15.
 */
public class ChristophTest1  extends ActionBarActivity {

    private void setControls() {
        SharedPreferences prefs = this.getSharedPreferences(ParticipantLog.PARTICIPANT_PREFS, 0);
        txt_azimuthConstant.setText(Float.toString(prefs.getFloat("azimuthConstant",0f)));
        txt_azimuthMultiplier.setText(Float.toString(prefs.getFloat("azimuthMultiplier",1f)));
        txt_distortionAmplitude.setText(Float.toString(prefs.getFloat("distortionAmplitude", 0f)));
        txt_distortionCentre.setText(Float.toString(prefs.getFloat("distortionCentre", 0f)));
        txt_distortionWidth.setText(Float.toString(prefs.getFloat("distortionWidth", 90f)));
    }

    private EditText txt_azimuthConstant;
    private EditText txt_azimuthMultiplier;
    private EditText txt_distortionAmplitude;
    private EditText txt_distortionCentre;
    private EditText txt_distortionWidth;



    public static void Update() {
        if (current != null) {
            current.setControls();
        }
    }
    public static ChristophTest1 current;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        current = this;
        setContentView(R.layout.christoph_test_1);
        txt_azimuthConstant = (EditText)findViewById(R.id.ct1_azimuthConstant);
        txt_azimuthMultiplier = (EditText)findViewById(R.id.ct1_azimuthMultiplier);
        txt_distortionAmplitude = (EditText)findViewById(R.id.ct1_distortionAmplitude);
        txt_distortionCentre = (EditText)findViewById(R.id.ct1_distortionCentre);
        txt_distortionWidth = (EditText)findViewById(R.id.ct1_distortionWidth);
        setControls();
        findViewById(R.id.ct1_start_tasks).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SharedPreferences prefs = getSharedPreferences(ParticipantLog.PARTICIPANT_PREFS, 0);
                SharedPreferences.Editor edit = prefs.edit();
                float constant = Float.valueOf(txt_azimuthConstant.getText().toString());
                float mult = Float.valueOf(txt_azimuthMultiplier.getText().toString());
                float distortionAmplitude = Float.valueOf(txt_distortionAmplitude.getText().toString());
                float distortionCentre = Float.valueOf(txt_distortionCentre.getText().toString());
                float distortionWidth = Float.valueOf(txt_distortionWidth.getText().toString());
                ParticipantLog.writeLogMessageLine("Starting Twisted Signal Test with constant: " + String.valueOf(constant) +
                        " mult : "+ String.valueOf(mult) + " amp: " + String.valueOf(distortionAmplitude) +
                        " centre: " + String.valueOf(distortionCentre) +
                        " width: " + String.valueOf(distortionWidth), current, ParticipantLog.TASK_LOG) ;
                edit.putFloat("testAzimuthConstant", constant);//azimuthConstant,
                edit.putFloat("testAzimuthMultiplier", mult);
                edit.putFloat("testDistortionAmplitude", distortionAmplitude);
                edit.putFloat("testDistortionCentre", distortionCentre);
                edit.putFloat("testDistortionWidth", distortionWidth);
                SoundGenerator.getInstance().setDistortionParameters(constant,mult,distortionAmplitude,distortionCentre,distortionWidth);

                String line = ParticipantLog.getDateTime() + ",1," + String.valueOf(distortionAmplitude) + "," +
                        String.valueOf(distortionCentre) + "," + String.valueOf(distortionWidth);

                ParticipantLog.writeLogMessageLine(line, current, ParticipantLog.TEST_LOG);
                Intent intent = new Intent(current, ChristophTaskSet.class);
                long ISI = 2000;
                intent.putExtra("ISI", ISI);

                startActivity(intent);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        current = null;
    }

    @Override
    public void onBackPressed() {
        //do nothing
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}
