package compasssounds.compasssounds;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;

/**
 * Created by james on 20/03/15.
 */
public class ChristophTestChoicePage extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SoundGenerator.getInstance().pauseEngine(true);
        setContentView(R.layout.christoph_test_choice_page);

        ((Button)findViewById(R.id.button_start_test_1)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTest1();
            }
        });
        ((Button)findViewById(R.id.button_start_test_2)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTest2();
            }
        });
        ((Button)findViewById(R.id.button_start_test_3)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTest3();
            }
        });

        ((Button)findViewById(R.id.button_end_tests)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endTests();
            }
        });
    }

    void endTests() {
        AdjustsCodingActivity.setCoding(this); //resets coding...
        SoundGenerator.getInstance().pauseEngine(false);
        Intent intent = new Intent(this, ExperimenterActivity.class);
        startActivity(intent);
    }

    void startTest1() {
        Intent intent = new Intent(this, ChristophTest1.class);
        startActivity(intent);
    }

    void startTest2() {
        Intent intent = new Intent(this, ChristophTest2.class);
        startActivity(intent);
    }

    void startTest3() {
        Intent intent = new Intent(this, ChristophTest3.class);
        startActivity(intent);
    }
}
