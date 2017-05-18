package compasssounds.compasssounds;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.professionalaudio.Sapa;
import com.samsung.android.sdk.professionalaudio.SapaPluginInfo;
import com.samsung.android.sdk.professionalaudio.SapaProcessor;
import com.samsung.android.sdk.professionalaudio.SapaService;

public class SamsungSoundGenerator extends SoundGenerator {

    private SapaService mService;
    private SapaProcessor mProcessor;
    private boolean mProcessorActivated = false;
    private static final String TAG = "SimpleClientNative";
    private static final String LEFTWAVFILE = "leftsoundfile.wav";
    private static final String RIGHTWAVFILE = "rightsoundfile.wav";
    private static final String[] ASSET_FILES = {LEFTWAVFILE, RIGHTWAVFILE};

    public void initializeEngine(Context context) {
        try {
            Sapa sapa = new Sapa();
            sapa.initialize(context);
            mService = new SapaService();
            mService.start(SapaService.START_PARAM_DEFAULT_LATENCY);

            List<SapaPluginInfo> pluginList = mService.getAllPlugin();
            Iterator<SapaPluginInfo> iter = pluginList.iterator();
            while (iter.hasNext()) {
                SapaPluginInfo info = iter.next();
                if (info.getName().contentEquals("AudioCompassSamsungJackService") == true) {
                    // load SapaSimplePlugin

                    mProcessor = new SapaProcessor(context, info, new SapaProcessor.StatusListener() {

                        @Override
                        public void onKilled() {
                            Log.v(TAG, "SapaSimplePluginClient will be closed. because of the SapaProcessor was closed.");
                            mService.stop(true);
                            return;
                        }
                    });
                    mService.register(mProcessor);
                    // success

                    if (mProcessor != null) {
                        Log.v(TAG, "Copying wav files...");
                        copyAssetFilesToLocal(context);
                        Log.v(TAG, "Activating...");
                        mProcessor.activate();
                        mProcessorActivated = true;

                    }


                    break;
                }
            }

        } catch (SsdkUnsupportedException e) {
            Toast.makeText(context, "Not support professional audio package", Toast.LENGTH_LONG).show();
            e.printStackTrace();
            return;
        } catch (InstantiationException e) {
            Toast.makeText(context, "fail to instantiation SapaService", Toast.LENGTH_LONG).show();
            e.printStackTrace();
            return;
        }
    }
    public void pauseEngine(boolean paused){
        String msg = paused ? "PAUSEON" : "PAUSEOFF";

        if (mProcessor != null && mProcessorActivated) {
            mProcessor.sendCommand(msg);
        }
    }
    public void setTransformedAzimuth(float transformedAzimuth) {
        if (mProcessor != null && mProcessorActivated) {
            mProcessor.sendCommand("AZIMUTH:" + String.valueOf(transformedAzimuth));
        }
    }
    public void testMode(boolean on) {

        testModeActive = on;
        String msg = on ? "TESTMODEON" : "TESTMODEOFF";

        if (mProcessor != null && mProcessorActivated) {
            mProcessor.sendCommand(msg);
        }
    }
    public void stopEngine(){
        mProcessorActivated = false;
        if(mService != null){
            if(mProcessor != null){
                mProcessor.deactivate();
                mService.unregister(mProcessor);
                mProcessor = null;
            }
            mService.stop(true);
            mService = null;
        }
    }
    public void setParameters(float slopeA,
                                       float posA,
                                       float slopeB,
                                       float posB,
                                       float freqLeft,
                                       float freqRight,
                                       float volLeft,
                                       float volRight,
                                       float rampLeft,
                                       float rampRight){
        String msg = "PARAMS:" + String.valueOf(slopeA) + ":" +
                String.valueOf(posA) + ":" +
                String.valueOf(slopeB) + ":" +
                String.valueOf(posB) + ":" +
                String.valueOf(freqLeft) + ":" +
                String.valueOf(freqRight) + ":" +
                String.valueOf(volLeft) + ":" +
                String.valueOf(volRight) + ":" +
                String.valueOf(rampLeft) + ":" +
                String.valueOf(rampRight) ;

        if (mProcessor != null && mProcessorActivated) {
            mProcessor.sendCommand(msg);
        }

    }

    public void copyAssetFilesToLocal(Context activity){
        AssetManager assetManager = activity.getAssets();
        String[] files = null;
        try {
            files = ASSET_FILES;
            for(int i=0 ; i < files.length; i++){
                String filename = files[i].toString();

                InputStream is = activity.getAssets().open(filename);

                // Destination
                File outFile = new File(activity.getFilesDir(), filename);


                Log.i("SoundGenerator", "retrieveFromAssets( .. ) copying "
                        + filename
                        + " to "
                        + outFile.getParent());

                FileOutputStream fos = new FileOutputStream(outFile);

                // transfer bytes from the inputfile to the outputfile
                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) > 0) {
                    fos.write(buffer, 0, length);
                }

                outFile.setReadable(true, false);

                // Close the streams
                fos.flush();
                fos.close();
                is.close();

                Log.v(TAG, "Copying wav file named: " + outFile.getAbsolutePath() + "was a success!");

            }
        } catch (IOException e) {
            Log.v(TAG, "Copying wavs files not successful!");
            e.printStackTrace();
        }
    }
}
