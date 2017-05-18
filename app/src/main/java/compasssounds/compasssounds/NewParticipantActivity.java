package compasssounds.compasssounds;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class NewParticipantActivity extends ActionBarActivity {


    EditText txtName;
    EditText txtID;
    Button btnStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_participant);
        txtName = (EditText) findViewById(R.id.txtName);
        txtID = (EditText) findViewById(R.id.txtID);
        btnStart = (Button) findViewById(R.id.btnStart);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startNewParticipant();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_new_participant, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_cancel) {
            Intent intent = new Intent(this, ExperimenterActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startNewParticipant() {
        String name = txtName.getText().toString();
        String ID = txtID.getText().toString();
        if (!ParticipantLog.eraseLog(this,ParticipantLog.TASK_LOG)) {
            Toast error = Toast.makeText(this, "Could not create new task file.", Toast.LENGTH_LONG);
            error.show();
            return;
        }

        SharedPreferences prefs = getSharedPreferences(ParticipantLog.PARTICIPANT_PREFS, 0);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString("Name", name);
        edit.putString("ID", ID);
        edit.commit();
        TaskScheduler.resetTrialsCompleted(this);
        long millis = System.currentTimeMillis();
        ParticipantLog.writeLogMessageLine("New Participant: "+ name + " ID" + ID + " Time: " + millis, this, ParticipantLog.PARTICIPANT_LOG);
        ParticipantLog.writeLogMessageLine("DateTime,Latitude,Longitude,LocationAccuracy,LocationTime,Azimuth,Pitch,Roll", this, ParticipantLog.LOCATION_LOG);
        ParticipantLog.writeLogMessageLine("DateTime,Test,Path,DistortionType,NoticedChange,DistortionAmplitude,DistortionCentre,DistortionWidth", this, ParticipantLog.TEST_LOG);

        Toast error = Toast.makeText(this, "New participant details logged.", Toast.LENGTH_LONG);
        error.show();

        Log.i("NewParticipantActivity","Launching participant activity");
        Intent intent = new Intent(this, ParticipantActivity.class);
        startActivity(intent);
    }
}
