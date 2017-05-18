package compasssounds.compasssounds;

import android.content.Context;

/**
 * Created by james on 30/04/15.
 */
public abstract class CompassSensor {

    public abstract void setup(Context context);
    public abstract void stop();
    public abstract void tare();
}
