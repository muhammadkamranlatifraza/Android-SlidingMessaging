package com.klinker.android.messaging_sliding.slide_over;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.*;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.view.*;
import android.widget.Toast;

/**
 * Created by luke on 8/2/13.
 */
public class ArcView extends ViewGroup {
    public Context mContext;

    public Bitmap halo;

    public Paint newMessagePaint;
    public Paint conversationsPaint;
    public float radius;
    public float breakAngle;

    public SharedPreferences sharedPrefs;

    public int height;
    public int width;

    public int sliverAdjustment = 0;
    public double sliverPercent;

    public ArcView(Context context, Bitmap halo, float radius, float breakAngle, double sliverPercent) {
        super(context);

        mContext = context;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);

        Display d = ((WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        height = d.getHeight();
        width = d.getWidth();

        newMessagePaint = new Paint();
        newMessagePaint.setAntiAlias(true);
        newMessagePaint.setColor(Color.WHITE);
        newMessagePaint.setAlpha(60);
        newMessagePaint.setStyle(Paint.Style.STROKE);
        newMessagePaint.setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, context.getResources().getDisplayMetrics()));

        conversationsPaint = new Paint(newMessagePaint);
        float dashLength = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, context.getResources().getDisplayMetrics());
        conversationsPaint.setPathEffect(new DashPathEffect(new float[] {dashLength, dashLength*2}, 0));

        this.halo = halo;
        this.radius = radius;
        this.breakAngle = breakAngle;
        this.sliverPercent = sliverPercent;
    }


    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        /*if (sharedPrefs.getString("slideover_side", "left").equals("left")) {

            if (sliverPercent > .5) // move the arc right
            {
                sliverPercent -= .5;
                sliverAdjustment = (int)((halo.getWidth()/2) * sliverPercent);
            } else // move the arc left
            {
                sliverAdjustment = (int)(-1 * ((halo.getWidth()/2) * sliverPercent));
            }

        } else
        {

        }


        CharSequence text = "" + sliverAdjustment;
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(mContext, text, duration);
        toast.show();*/

        int[] point = getPosition();

        if (sharedPrefs.getString("slideover_side", "left").equals("left")) {
            // todo: adjust for sliver size on the left and right of the oval here
            RectF oval = new RectF(-1 * radius, point[1] + (halo.getHeight() / 2) -  radius, radius, point[1] + (halo.getHeight() / 2) + radius);

            Path newMessagePath = new Path();
            newMessagePath.addArc(oval, breakAngle, -180);

            Path conversationsPath = new Path();
            conversationsPath.addArc(oval, breakAngle, 180);

            canvas.drawPath(newMessagePath, newMessagePaint);
            canvas.drawPath(conversationsPath, conversationsPaint);
        } else
        {
            RectF oval = new RectF(width - radius, point[1] + (halo.getHeight() / 2) -  radius, width + radius, point[1] + (halo.getHeight() / 2) + radius);

            Path newMessagePath = new Path();
            newMessagePath.addArc(oval, breakAngle - 45, -180);

            Path conversationsPath = new Path();
            conversationsPath.addArc(oval, breakAngle - 45, 180);

            canvas.drawPath(newMessagePath, newMessagePaint);
            canvas.drawPath(conversationsPath, conversationsPaint);
        }

        /*

        Original Circle Drawing

        if (sharedPrefs.getString("slideover_side", "left").equals("left")) {
            canvas.drawCircle(point[0] + (halo.getHeight()/2), point[1] + (halo.getHeight() / 2), radius, newMessagePaint);
        } else {
            canvas.drawCircle(point[0] + (halo.getHeight()/2), point[1] + (halo.getHeight() / 2), radius, newMessagePaint);
        }

        */
    }


    @Override
    protected void onLayout(boolean arg0, int arg1, int arg2, int arg3, int arg4) {
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }

    public int[] getPosition()
    {
        int[] returnArray = {0, 0};

        if (sharedPrefs.getString("slideover_side", "left").equals("left")) {
            returnArray[0] = (int)(-1 * halo.getWidth() * (1 - SlideOverService.HALO_SLIVER_RATIO));
        } else {
            returnArray[0] = (int)(width - (halo.getWidth() * SlideOverService.HALO_SLIVER_RATIO));
        }

        returnArray[1] = (int)(height * SlideOverService.PERCENT_DOWN_SCREEN);

        return returnArray;
    }
}