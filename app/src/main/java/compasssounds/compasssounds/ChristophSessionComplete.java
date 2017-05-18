package compasssounds.compasssounds;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.NumberPicker;

/**
 * Created by james on 11/03/15.
 */
public class ChristophSessionComplete extends ActionBarActivity {

    ChristophSessionComplete ts = this;
    View root;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.christoph_session_complete);
        root = (View) findViewById(R.id.sc_rootView);
        AdjustsCodingActivity.setCoding(this); //resets coding...
        root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CompassSoundService.pause(false);
                Intent intent = new Intent(ts, ParticipantActivity.class);
                startActivity(intent);
                finish();//finishing activity
            }
        });

    }
    @Override
    public void onBackPressed() {  //disable back button pressing
    }
}
