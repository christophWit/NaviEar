package compasssounds.compasssounds;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Toast;

/**
 * Created by james on 03/03/15.
 */
public class CompassFeedbackView extends View {
    private float desired_azimuth = 90;
    private float given_azimuth = 80;
    private float radius = 40;
    public CompassFeedbackView(Context context) {
        super(context);
    }

    public CompassFeedbackView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }
    public CompassFeedbackView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.WHITE);

        int w = canvas.getWidth();
        int h = canvas.getHeight();
        radius = Math.min(w,h) / 2;
        int cx = w / 2;
        int cy = h / 2;

        int da_x = cx + (int)(Math.sin(desired_azimuth * Math.PI / 180f) * radius);
        int da_y = cy - (int)(Math.cos(desired_azimuth * Math.PI / 180f) * radius);

        int ga_x = cx + (int)(Math.sin(given_azimuth * Math.PI / 180f) * radius);
        int ga_y = cy - (int)(Math.cos(given_azimuth * Math.PI / 180f) * radius);

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(3f);
        canvas.drawPaint(paint);


        canvas.drawCircle(cx,cy,radius,paint);

        paint.setColor(Color.RED);
        canvas.drawLine(cx, cy, ga_x, ga_y, paint);
        paint.setStrokeWidth(5f);
        paint.setColor(Color.DKGRAY);
        canvas.drawLine(cx,cy,da_x,da_y,paint);

    }

    public void setArrows(float desired, float given)
    {
        desired_azimuth = desired;
        given_azimuth = given;
        //if (BuildConfig.DEBUG) {
            //Toast.makeText(this.getContext(), "Probe: " + desired + " Response: " + given, Toast.LENGTH_LONG).show();
        //}
    }

}