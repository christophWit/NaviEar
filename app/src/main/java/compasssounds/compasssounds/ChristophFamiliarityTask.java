package compasssounds.compasssounds;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;

/**
 * Created by james on 26/02/15.
 */
public class ChristophFamiliarityTask extends ChristophTask {

    public static final String FAMILIARITY = "FAMILIARITY";
    public static final String ORIENTATION_ABILITY = "ORIENTATION_ABILITY";
    public static final String AMBIENT_LOUDNESS = "AMBIENT_LOUDNESS";

    public static int lastPicker1Value = 3;
    public static int lastPicker2Value = 3;
    public static int lastPicker3Value = 3;

    NumberPicker picker;
    NumberPicker picker2;
    NumberPicker picker3;

    Button okButton;
    ChristophFamiliarityTask ts = this;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.christoph_familiarity_task);
        picker = (NumberPicker)findViewById(R.id.familiarityPicker);
        picker.setMaxValue(5);
        picker.setMinValue(1);
        picker.setValue(lastPicker1Value);
        picker.setWrapSelectorWheel(false);

        picker2 = (NumberPicker)findViewById(R.id.orientComfPicker);
        picker2.setMaxValue(5);
        picker2.setMinValue(1);
        picker2.setValue(lastPicker2Value);
        picker2.setWrapSelectorWheel(false);

        picker3 = (NumberPicker)findViewById(R.id.loudnessPicker);
        picker3.setMaxValue(5);
        picker3.setMinValue(1);
        picker3.setValue(lastPicker3Value);
        picker3.setWrapSelectorWheel(false);

        okButton = (Button)findViewById(R.id.button_QA);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                lastPicker1Value = picker.getValue();
                lastPicker2Value = picker2.getValue();
                lastPicker3Value = picker3.getValue();
                ParticipantLog.writeLogMessageLine("Familiarity task result: Familiarity: " + picker.getValue() + " Orientation: " + picker2.getValue() + " Loudness: " + picker3.getValue(), ts, ParticipantLog.PARTICIPANT_LOG);
                returnIntent.putExtra(FAMILIARITY,picker.getValue());
                returnIntent.putExtra(ORIENTATION_ABILITY, picker2.getValue());
                returnIntent.putExtra(AMBIENT_LOUDNESS, picker3.getValue());
                endTask(ChristophTaskSet.FAMILIARITY_COMPLETE);
            }
        });
    }


}
