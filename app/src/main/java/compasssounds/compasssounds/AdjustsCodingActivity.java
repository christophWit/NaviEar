package compasssounds.compasssounds;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by james on 02/03/15.
 */
public class AdjustsCodingActivity extends ActionBarActivity {

    public static void setCoding(Context context) {

        SharedPreferences prefs = context.getSharedPreferences(ParticipantLog.PARTICIPANT_PREFS, 0);
        SoundGenerator.getInstance().setParameters(
                prefs.getFloat("slopeA",30f),//slopeA,
                prefs.getFloat("posA",0.42f),//posA,
                prefs.getFloat("slopeB",14f),//slopeB,
                prefs.getFloat("posB",0.2f),//posB,
                prefs.getFloat("freqLeft",220f),//freqLeft,
                prefs.getFloat("freqRight",269f),//freqRight
                prefs.getFloat("volLeft",1.0f),
                prefs.getFloat("volRight",1.0f),
                prefs.getFloat("rampLeft",880.0f),
                prefs.getFloat("rampRight",880.0f)
                );

        SoundGenerator.getInstance().setDistortionParameters(
                prefs.getFloat("azimuthConstant",0f),
                prefs.getFloat("azimuthMultiplier",1f),
                prefs.getFloat("distortionAmplitude",0f),
                prefs.getFloat("distortionCentre",0f),
                prefs.getFloat("distortionWidth", 90f)
        );
    }

    private void setPrefs() {
        SharedPreferences prefs = getSharedPreferences(ParticipantLog.PARTICIPANT_PREFS, 0);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putFloat("slopeA", Float.valueOf(txt_slopeA.getText().toString()));//slopeA,
        edit.putFloat("posA", Float.valueOf(txt_posA.getText().toString()));//posA,
        edit.putFloat("slopeB", Float.valueOf(txt_slopeB.getText().toString()));//slopeB,
        edit.putFloat("posB", Float.valueOf(txt_posB.getText().toString()));//posB,
        edit.putFloat("freqLeft", Float.valueOf(txt_freqLeft.getText().toString()));//freqLeft,
        edit.putFloat("freqRight", Float.valueOf(txt_freqRight.getText().toString()));//freqRight
        edit.putFloat("volLeft", Float.valueOf(txt_volLeft.getText().toString()));//volLeft,
        edit.putFloat("volRight", Float.valueOf(txt_volRight.getText().toString()));//volRight
        edit.putFloat("rampLeft", Float.valueOf(txt_rampLeft.getText().toString()));//rampLeft,
        edit.putFloat("rampRight", Float.valueOf(txt_rampRight.getText().toString()));//rampRight
        edit.putFloat("azimuthConstant", Float.valueOf(txt_azimuthConstant.getText().toString()));
        edit.putFloat("azimuthMultiplier", Float.valueOf(txt_azimuthMultiplier.getText().toString()));
        edit.putFloat("distortionAmplitude", Float.valueOf(txt_distortionAmplitude.getText().toString()));
        edit.putFloat("distortionCentre", Float.valueOf(txt_distortionCentre.getText().toString()));
        edit.putFloat("distortionWidth", Float.valueOf(txt_distortionWidth.getText().toString()));
        edit.commit();
        setCoding(this);
    }

    private void setControls() {
        SharedPreferences prefs = this.getSharedPreferences(ParticipantLog.PARTICIPANT_PREFS, 0);
        txt_slopeA.setText(Float.toString(prefs.getFloat("slopeA",30f)));
        txt_posA.setText(Float.toString(prefs.getFloat("posA",0.42f)));
        txt_slopeB.setText(Float.toString(prefs.getFloat("slopeB",14f)));
        txt_posB.setText(Float.toString(prefs.getFloat("posB",0.2f)));
        txt_freqLeft.setText(Float.toString(prefs.getFloat("freqLeft",440f)));
        txt_freqRight.setText(Float.toString(prefs.getFloat("freqRight",880f)));
        txt_volLeft.setText(Float.toString(prefs.getFloat("volLeft",1.0f)));
        txt_volRight.setText(Float.toString(prefs.getFloat("volRight",1.0f)));
        txt_rampLeft.setText(Float.toString(prefs.getFloat("rampLeft",880.0f)));
        txt_rampRight.setText(Float.toString(prefs.getFloat("rampRight",880.0f)));
        txt_azimuthConstant.setText(Float.toString(prefs.getFloat("azimuthConstant",0f)));
        txt_azimuthMultiplier.setText(Float.toString(prefs.getFloat("azimuthMultiplier",1f)));
        txt_distortionAmplitude.setText(Float.toString(prefs.getFloat("distortionAmplitude",0f)));
        txt_distortionCentre.setText(Float.toString(prefs.getFloat("distortionCentre",0f)));
        txt_distortionWidth.setText(Float.toString(prefs.getFloat("distortionWidth",90f)));
    }

    private EditText txt_slopeA;
    private EditText txt_posA;
    private EditText txt_slopeB;
    private EditText txt_posB;
    private EditText txt_freqLeft;
    private EditText txt_freqRight;
    private EditText txt_volLeft;
    private EditText txt_volRight;
    private EditText txt_rampLeft;
    private EditText txt_rampRight;
    private EditText txt_azimuthConstant;
    private EditText txt_azimuthMultiplier;
    private EditText txt_distortionAmplitude;
    private EditText txt_distortionCentre;
    private EditText txt_distortionWidth;

    private Button btnSet;
    private Button btnDone;

    public static void Update() {
        if (current != null) {
            current.setControls();
        }
    }
    public static AdjustsCodingActivity current;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        current = this;
        setContentView(R.layout.activity_adjust_coding);
        txt_slopeA = (EditText)findViewById(R.id.ac_slopeA);
        txt_posA = (EditText)findViewById(R.id.ac_posA);
        txt_slopeB = (EditText)findViewById(R.id.ac_slopeB);
        txt_posB = (EditText)findViewById(R.id.ac_posB);
        txt_freqLeft = (EditText)findViewById(R.id.ac_leftFreq);
        txt_freqRight = (EditText)findViewById(R.id.ac_rightFreq);
        txt_volLeft = (EditText)findViewById(R.id.ac_leftVol);
        txt_volRight = (EditText)findViewById(R.id.ac_rightVol);
        txt_rampLeft = (EditText)findViewById(R.id.ac_leftRamp);
        txt_rampRight = (EditText)findViewById(R.id.ac_rightRamp);
        txt_azimuthConstant = (EditText)findViewById(R.id.ac_azimuthConstant);
        txt_azimuthMultiplier = (EditText)findViewById(R.id.ac_azimuthMultiplier);
        txt_distortionAmplitude = (EditText)findViewById(R.id.ac_distortionAmplitude);
        txt_distortionCentre = (EditText)findViewById(R.id.ac_distortionCentre);
        txt_distortionWidth = (EditText)findViewById(R.id.ac_distortionWidth);

        setControls();
        btnSet = (Button)findViewById(R.id.ac_btn_set);
        btnSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPrefs();
            }
        });
        btnDone = (Button)findViewById(R.id.ac_btn_done);
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPrefs();
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        current = null;
    }
}
