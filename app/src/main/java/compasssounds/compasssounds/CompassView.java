package compasssounds.compasssounds;

import android.content.Context;
import android.graphics.Canvas;
import android.view.View;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.AttributeSet;
/**
 * Created by james on 20/02/15.
 */
public class CompassView extends View {
    private Bitmap compassImage;
    private float compassImageOffset;
    private float azimuth = 0;

    public CompassView(Context context) {
        super(context);
        compassImage = BitmapFactory.decodeResource(getResources(), R.drawable.compass_md);
        compassImageOffset = 0 - compassImage.getHeight() / 2;
    }

    public CompassView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        compassImage = BitmapFactory.decodeResource(getResources(), R.drawable.compass_md);
        compassImageOffset = 0 - compassImage.getHeight() / 2;
    }
    public CompassView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        compassImage = BitmapFactory.decodeResource(getResources(), R.drawable.compass_md);
        compassImageOffset = 0 - compassImage.getHeight() / 2;
    }

    @Override protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.WHITE);

        int w = canvas.getWidth();
        int h = canvas.getHeight();
        int cx = w / 2;
        int cy = h / 2;

        canvas.translate(cx, cy);
        canvas.rotate(azimuth);

        canvas.drawBitmap(compassImage, compassImageOffset, compassImageOffset, null);

    }

    public void setAzimuth(float new_azimuth) {
        azimuth = 360 - new_azimuth;
    }

}