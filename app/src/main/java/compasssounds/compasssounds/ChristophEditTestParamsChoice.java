package compasssounds.compasssounds;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;

/**
 * Created by james on 31/03/15.
 */
public class ChristophEditTestParamsChoice extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SoundGenerator.getInstance().pauseEngine(true);
        setContentView(R.layout.christoph_edit_test_params_choice);


        ((Button)findViewById(R.id.button_path_1)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startEditActivity(ChristophTest2.CONDITION_NAME_PATH1);
            }
        });
        ((Button)findViewById(R.id.button_path_2)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startEditActivity(ChristophTest2.CONDITION_NAME_PATH2);
            }
        });
        ((Button)findViewById(R.id.button_path_3)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startEditActivity(ChristophTest2.CONDITION_NAME_PATH3);
            }
        });
        ((Button)findViewById(R.id.button_path_4)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startEditActivity(ChristophTest2.CONDITION_NAME_PATH4);
            }
        });


        ((Button)findViewById(R.id.button_interference_1)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startEditActivity(ChristophTest3.CONDITION_NAME_INT1);
            }
        });
        ((Button)findViewById(R.id.button_interference_2)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startEditActivity(ChristophTest3.CONDITION_NAME_INT2);
            }
        });
        ((Button)findViewById(R.id.button_interference_3)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startEditActivity(ChristophTest3.CONDITION_NAME_INT3);
            }
        });
        ((Button)findViewById(R.id.button_interference_4)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startEditActivity(ChristophTest3.CONDITION_NAME_INT4);
            }
        });


        ((Button)findViewById(R.id.button_done)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                done();
            }
        });

    }

    void startEditActivity(String conditionName) {
        Intent intent = new Intent(this, ChristophEditTestParams.class);
        intent.putExtra(ChristophEditTestParams.CONDITION_NAME_EXTRA,conditionName);
        startActivity(intent);
    }

    void done() {
        Intent intent = new Intent(this, ExperimenterActivity.class);
        startActivity(intent);
    }
}
