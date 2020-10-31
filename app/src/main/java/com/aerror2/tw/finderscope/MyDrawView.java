package com.aerror2.tw.finderscope;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;


/**
 * Created by wenzhe on 16-3-23.
 */
public class MyDrawView extends View {

    private final String TAG = this.getClass().getSimpleName();
    private int width;
    private int height;
    private Paint mPaint = null;

    public MyDrawView(Context context) {
        this(context, null);
    }

    public MyDrawView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyDrawView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPaint = new Paint();

        mPaint.setARGB(128,255,255,255);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(1);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        width   = getWidth();
        height  = getHeight();

        int radius = 25;
        for(int i=0; i < 9 ;i ++) {
            canvas.drawCircle(width / 2, height / 2,radius , mPaint);
            if(i<3)
                radius +=50;
            else
                radius +=100;
        }

        for(int i=0;i<12;i++)
        {
            double angle = Math.toRadians((double) (30 + (double)i*30));

            int dy =  (int)(Math.sin(angle) * (double) width);
            int dx =  (int)(Math.cos(angle) * (double) width);
            canvas.drawLine(width/2,height/2,width/2+dx,height/2+dy,mPaint);
        }
    }

}
