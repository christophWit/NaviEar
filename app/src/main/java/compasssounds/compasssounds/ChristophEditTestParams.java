package compasssounds.compasssounds;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;

/**
 * Created by james on 24/03/15.
 */
public class ChristophEditTestParams extends ActionBarActivity {

    public static void setSoundCondition(Context context, String conditionName, int selectedCondition) {


        SharedPreferences prefs = context.getSharedPreferences(ParticipantLog.PARTICIPANT_PREFS, 0);
        float constant = prefs.getFloat(getToken("azimuthConstant",selectedCondition,conditionName),0f);
        float mult = prefs.getFloat(getToken("azimuthMultiplier",selectedCondition,conditionName),1f);
        float amp = prefs.getFloat(getToken("distortionAmplitude",selectedCondition,conditionName),0f);
        float cen = prefs.getFloat(getToken("distortionCentre",selectedCondition,conditionName),0f);
        float wid = prefs.getFloat(getToken("distortionWidth",selectedCondition,conditionName), 90f);
        SoundGenerator.getInstance().setDistortionParameters(constant,mult,amp,cen,wid);
    }

    public static int CONDITION_ZERO = 0;
    public static int CONDITION_ONE = 1;
    public static int CONDITION_TWO = 2;
    public static int CONDITION_THREE = 3;
    public static int CONDITION_FOUR = 4;

    private static String getToken(String name, int condition, String conditionName) {
        return conditionName + String.valueOf(condition) + name;
    }

    private int selectedCondition = 1;

    public void selectCondition(int condition) {
        selectedCondition = condition;
        setControls();
    }

    private void setPrefs() {
        SharedPreferences prefs = getSharedPreferences(ParticipantLog.PARTICIPANT_PREFS, 0);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putFloat(getToken("azimuthConstant",selectedCondition,currentConditionName), Float.valueOf(txt_azimuthConstant.getText().toString()));
        edit.putFloat(getToken("azimuthMultiplier",selectedCondition,currentConditionName), Float.valueOf(txt_azimuthMultiplier.getText().toString()));
        edit.putFloat(getToken("distortionAmplitude",selectedCondition,currentConditionName), Float.valueOf(txt_distortionAmplitude.getText().toString()));
        edit.putFloat(getToken("distortionCentre",selectedCondition,currentConditionName), Float.valueOf(txt_distortionCentre.getText().toString()));
        edit.putFloat(getToken("distortionWidth",selectedCondition,currentConditionName), Float.valueOf(txt_distortionWidth.getText().toString()));


        edit.commit();
        Toast.makeText(this, "Preferences set", Toast.LENGTH_LONG).show();
    }

    private void setControls() {
        SharedPreferences prefs = this.getSharedPreferences(ParticipantLog.PARTICIPANT_PREFS, 0);
        txt_azimuthConstant.setText(Float.toString(prefs.getFloat(getToken("azimuthConstant",selectedCondition,currentConditionName),0f)));
        txt_azimuthMultiplier.setText(Float.toString(prefs.getFloat(getToken("azimuthMultiplier",selectedCondition,currentConditionName),1f)));
        txt_distortionAmplitude.setText(Float.toString(prefs.getFloat(getToken("distortionAmplitude",selectedCondition,currentConditionName),0f)));
        txt_distortionCentre.setText(Float.toString(prefs.getFloat(getToken("distortionCentre",selectedCondition,currentConditionName),0f)));
        txt_distortionWidth.setText(Float.toString(prefs.getFloat(getToken("distortionWidth",selectedCondition,currentConditionName),90f)));
    }

    private EditText txt_azimuthConstant;
    private EditText txt_azimuthMultiplier;
    private EditText txt_distortionAmplitude;
    private EditText txt_distortionCentre;
    private EditText txt_distortionWidth;

    private Button btnSet;
    private Button btnDone;
    private String currentConditionName = "";

    public static void Update() {
        if (current != null) {
            current.setControls();
        }
    }
    public static ChristophEditTestParams current;
    public static String CONDITION_NAME_EXTRA = "CONDITION_NAME_EXTRA";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        current = this;
        setContentView(R.layout.christoph_edit_test_params);
        currentConditionName = getIntent().getStringExtra(CONDITION_NAME_EXTRA);

        if (currentConditionName == "" || currentConditionName == null) {
            Toast.makeText(this, "No condition name set", Toast.LENGTH_LONG).show();
            finish();
        }

        ((TextView)findViewById(R.id.ctc_text_condition_name)).setText("Parameters for Condition: " + currentConditionName);

        txt_azimuthConstant = (EditText)findViewById(R.id.ctc_azimuthConstant);
        txt_azimuthMultiplier = (EditText)findViewById(R.id.ctc_azimuthMultiplier);
        txt_distortionAmplitude = (EditText)findViewById(R.id.ctc_distortionAmplitude);
        txt_distortionCentre = (EditText)findViewById(R.id.ctc_distortionCentre);
        txt_distortionWidth = (EditText)findViewById(R.id.ctc_distortionWidth);

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
                Intent intent = new Intent(current, ChristophEditTestParamsChoice.class);
                startActivity(intent);
                finish();
            }
        });

        ((RadioButton)findViewById(R.id.radioButton0)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectCondition(CONDITION_ZERO);
            }
        });
        ((RadioButton)findViewById(R.id.radioButton1)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectCondition(CONDITION_ONE);
            }
        });
        ((RadioButton)findViewById(R.id.radioButton2)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectCondition(CONDITION_TWO);
            }
        });
        ((RadioButton)findViewById(R.id.radioButton3)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectCondition(CONDITION_THREE);
            }
        });

        ((RadioButton)findViewById(R.id.radioButton4)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectCondition(CONDITION_FOUR);
            }
        });

        selectCondition(CONDITION_ONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        current = null;
    }
}