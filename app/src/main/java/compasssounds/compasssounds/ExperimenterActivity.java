package compasssounds.compasssounds;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ViewFlipper;



import android.widget.Toast;

public class ExperimenterActivity extends ActionBarActivity {

    private ViewFlipper viewFlipper;
    private static ExperimenterActivity ts;
    //Visual compass display
    private TextView txtHeadingView;
    private CompassView compassView;
    private boolean pauseSetting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ts = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_experimenter);
        viewFlipper = (ViewFlipper) findViewById(R.id.viewflipper);
        txtHeadingView = (TextView)findViewById(R.id.txtHeadingView);
        compassView = (CompassView)findViewById(R.id.compassView);



        //Connect to the service if already running
        if (CompassSoundService.isRunning()) {
            doBindService();
        }


        SoundGenerator.getInstance().pauseEngine(false);

    }

    public void orientationUpdate(float azimuth, float pitch, float roll) {
        compassView.setAzimuth(azimuth);
        compassView.invalidate();
        txtHeadingView.setText("Heading: " + (int) azimuth + " Sound sent: " + String.valueOf((int) SoundGenerator.getInstance().lastAzimuthSent));
    }



    private void showToast(String msg) {
        Toast error = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        error.show();
    }

    void doBindService() {
        if (!mIsBound) {
            bindService(new Intent(this, CompassSoundService.class), mConnection, Context.BIND_AUTO_CREATE);
            mIsBound = true;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (CompassSoundService.mDataService != null) {
            CompassSoundService.mDataService.stop();
        }

        try {
            doUnbindService();
        }
        catch (Throwable t) {
            Log.e("StartUpActivity", "Failed to unbind from the service", t);
        }
    }

    void doUnbindService() {
        if (mIsBound) {
            // If we have received the service, and hence registered with it, then now is the time to unregister.
            if (mService != null) {
                try {
                    Message msg = Message.obtain(null, CompassSoundService.MSG_UNREGISTER_CLIENT);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                }
                catch (RemoteException e) {
                    // There is nothing special we need to do if the service has crashed.
                }
            }
            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_experimenter, menu);
        if (CompassSoundService.isRunning()) {
            menu.findItem(R.id.action_audioCompass).setTitle("Stop AudioCompass");
        } else {
            menu.findItem(R.id.action_audioCompass).setTitle("Test AudioCompass");
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_audioCompass) {
            if (!CompassSoundService.isRunning()) {
                if (CompassSoundService.AssertServiceRunning(this)) {
                    item.setTitle("Stop AudioCompass Engine");
                    doBindService();
                }
            } else {
                doUnbindService();
                if (CompassSoundService.isRunning()) {
                    CompassSoundService.stopSounds();
                    stopService(new Intent(getBaseContext(), CompassSoundService.class));
                    item.setTitle("Start AudioCompass Engine");
                }
            }

            return true;
        } else if (id == R.id.action_pauseAudioCompass) {
            pauseSetting = !pauseSetting;
            CompassSoundService.pause(pauseSetting);
            if (pauseSetting) {
                item.setTitle("Send Unpause command");
            } else {
                item.setTitle("Send Pause command");
            }
        } else if (id == R.id.action_newParticipant) {
            Intent intent = new Intent(this, NewParticipantActivity.class);
            startActivity(intent);
        } else if (id == R.id.action_setPass) {
            setExperimenterActivityPassWord();
        } else if (id == R.id.action_adjustCoding) {
            Intent intent = new Intent(this, AdjustsCodingActivity.class);
            startActivity(intent);
        } else if (id == R.id.action_latencyTest) {
            if (SoundGenerator.getInstance().testModeActive) {
                SoundGenerator.getInstance().testMode(false);
                item.setTitle("Latency Test Mode");
            } else {
                SoundGenerator.getInstance().testMode(true);
                item.setTitle("Turn off Latency Test Mode");
            }
        } else if (id == R.id.action_backToParticipantPage) {
            Intent intent = new Intent(this, ParticipantActivity.class);
            startActivity(intent);
        } else if (id == R.id.action_tareSensor) {
            CompassSoundService.sensor.tare();
        } /*else if (id == R.id.action_startBT) {
            startBluetooth();
        } else if (id == R.id.action_discoverable){
            // Ensure this device is discoverable by others
            ensureDiscoverable();
        } */else if (id == R.id.action_test_page) {
            Intent intent = new Intent(this, ChristophTestChoicePage.class);
            startActivity(intent);
        } else if (id == R.id.action_end_current_task) {
            AdjustsCodingActivity.setCoding(this);
            TaskScheduler.taskComplete(this,ChristophTaskSet.taskName);
            showToast("Current task set ended");
        } else if (id == R.id.action_setConditions) {
            Intent intent = new Intent(this, ChristophEditTestParamsChoice.class);
            startActivity(intent);
        } else if (id == R.id.action_ITI) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);

            alert.setTitle("Choose ITI");
            alert.setMessage("Choose ITI in ms");

            // Set an EditText view to get user input
            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_NUMBER);
            alert.setView(input);

            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    long value = Integer.parseInt(input.getText().toString());
                    SharedPreferences prefs = getSharedPreferences(ParticipantLog.PARTICIPANT_PREFS, 0);
                    prefs.edit().putLong("TIME_BETWEEN_TASKS", value).commit();

                }
            });

            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // Canceled.
                }
            });

            alert.show();
        }



        return super.onOptionsItemSelected(item);
    }

    private void setExperimenterActivityPassWord() {


        AlertDialog.Builder alert=new AlertDialog.Builder(this);

        alert.setTitle("Password");
        alert.setMessage("Please enter new password:");

        // Set an EditText view to get user input
        final EditText input=new EditText(this);
        alert.setView(input);
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int whichButton) {
                SharedPreferences sp = getSharedPreferences("prefs",0);
                SharedPreferences.Editor edit = sp.edit();
                edit.putString("ExperimenterActivityPW",input.getText().toString());
                edit.commit();
                showToast("Password changed.");
            }
        });

        alert.show();
    }



    //Messaging Stuff
    Messenger mService = null;
    boolean mIsBound;
    final Messenger mMessenger = new Messenger(new IncomingHandler());
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CompassSoundService.MSG_SET_ORIENTATION:
                    float azimuth = msg.getData().getFloat("azimuth");
                    float pitch = msg.getData().getFloat("pitch");
                    float roll = msg.getData().getFloat("roll");
                    orientationUpdate(azimuth, pitch, roll);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);
            //textStatus.setText("Attached.");
            try {
                Message msg = Message.obtain(null, CompassSoundService.MSG_REGISTER_CLIENT);
                msg.replyTo = mMessenger;
                mService.send(msg);
            }
            catch (RemoteException e) {
                // In this case the service has crashed before we could even do anything with it
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been unexpectedly disconnected - process crashed.
            mService = null;
            //textStatus.setText("Disconnected.");
        }
    };

    @Override
    public void onBackPressed() {

    }

    private void sendMessageToService(int intvaluetosend) {
        if (mIsBound) {
            /*if (mService != null) {
                try {
                    //Message msg = Message.obtain(null, CompassSoundService.MSG_SET_INT_VALUE, intvaluetosend, 0);
                    //msg.replyTo = mMessenger;
                    //mService.send(msg);
                }
                catch (RemoteException e) {
                }
            }*/
        }
    }



    private static final int REQUEST_ENABLE_BT = 3;
    private static String mConnectedDeviceName = null;
    private StringBuffer mOutStringBuffer;



    public boolean startBluetooth() {
        // Get local Bluetooth adapter
        if (CompassSoundService.mBluetoothAdapter == null) {
            CompassSoundService.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }

        // If the adapter is null, then Bluetooth is not supported
        if (CompassSoundService.mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            return false;
        }
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!CompassSoundService.mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            return false;
            // Otherwise, setup the chat session
        } else if (CompassSoundService.mDataService == null) {
            setupConnection();

        }

        if (CompassSoundService.mDataService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (CompassSoundService.mDataService.getState() == BluetoothDataService.STATE_NONE) {
                // Start the Bluetooth chat services
                CompassSoundService.mDataService.start();
            }
        }
        return true;
    }


    @Override
    public void onResume() {
        super.onResume();
        CompassSoundService.AssertServiceRunning(this);
        SoundGenerator.getInstance().pauseEngine(false);

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (CompassSoundService.mDataService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (CompassSoundService.mDataService.getState() == BluetoothDataService.STATE_NONE) {
                // Start the Bluetooth chat services
                CompassSoundService.mDataService.start();
            }
        }
    }



    /**
     * Set up the UI and background operations for chat.
     */
    private void setupConnection() {

        // Initialize the BluetoothChatService to perform bluetooth connections
        CompassSoundService.mDataService = new BluetoothDataService(this, mHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
    }

    /**
     * Makes this device discoverable.
     */
    private void ensureDiscoverable() {
        if (CompassSoundService.mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    /**
     * Sends a message.
     *
     * @param message A string of text to send.
     */
    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (CompassSoundService.mDataService.getState() != BluetoothDataService.STATE_CONNECTED) {
            Toast.makeText(this, "Sending Data but not connected!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            CompassSoundService.mDataService.write(send);

            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);

        }
    }

    /**
     * The Handler that gets information back from the BluetoothDataService
     */
    private static final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothDataService.STATE_CONNECTED:
                            Toast.makeText(ts, "Connected to "
                                    + mConnectedDeviceName, Toast.LENGTH_SHORT).show();

                            break;
                        case BluetoothDataService.STATE_CONNECTING:
                            Toast.makeText(ts, "Connecting...", Toast.LENGTH_SHORT).show();

                            break;
                        case BluetoothDataService.STATE_LISTEN:
                        case BluetoothDataService.STATE_NONE:
                            Toast.makeText(ts, "Not connected", Toast.LENGTH_SHORT).show();
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    Toast.makeText(ts,writeMessage, Toast.LENGTH_LONG).show();;
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    parseMessage(readMessage,ts);

                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    Toast.makeText(ts, "Connected to "
                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();

                    break;
                case Constants.MESSAGE_TOAST:
                    Toast.makeText(ts, msg.getData().getString(Constants.TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }


        private void parseMessage(String msg, Context context) {
            String[] params = msg.split(",");
            if (params.length < 14) {
                Toast.makeText(context,"Parameter length format error", Toast.LENGTH_LONG).show();
                return; }
            float[] floats = new float[params.length];
            try {
                for (int i = 0; i < params.length; i++) {
                    floats[i] = Float.parseFloat(params[i]);
                }
                SoundGenerator.getInstance().setParameters(floats[0], floats[2], floats[1], floats[3], floats[4],
                        floats[5], floats[6], floats[7], floats[8], floats[9]);
                //SoundGenerator.setDirectionParameters(floats[10], floats[11],floats[12], floats[13]);


                SharedPreferences prefs = context.getSharedPreferences(ParticipantLog.PARTICIPANT_PREFS, 0);
                SharedPreferences.Editor edit = prefs.edit();
                edit.putFloat("slopeA", floats[0]);//slopeA,
                edit.putFloat("posA", floats[2]);//posA,
                edit.putFloat("slopeB", floats[1]);//slopeB,
                edit.putFloat("posB", floats[3]);//posB,
                edit.putFloat("freqLeft", floats[4]);//freqLeft,
                edit.putFloat("freqRight", floats[5]);//freqRight
                edit.putFloat("volLeft", floats[6]);//volLeft,
                edit.putFloat("volRight", floats[7]);//volRight
                edit.putFloat("rampLeft", floats[8]);//rampLeft,
                edit.putFloat("rampRight", floats[9]);//rampRight
                edit.putFloat("azimuthConstant", floats[10]);//azimuthConstant,
                edit.putFloat("azimuthMultiplier", floats[11]);//azimuthMultiplier
                edit.commit();


                Toast.makeText(context,"Parameters changed", Toast.LENGTH_LONG).show();
                AdjustsCodingActivity.Update();
            } catch(Exception e) {
                Toast.makeText(context,"Parameter number format error", Toast.LENGTH_LONG).show();
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupConnection();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d("ExperimenterActivity", "BT not enabled");
                    Toast.makeText(this, "BT not enabled!!!!",
                            Toast.LENGTH_SHORT).show();
                }
        }
    }
}
