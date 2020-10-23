package com.aerror2.tw.aim;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.SurfaceView;
import android.view.WindowManager;

public class GridSurfaceView extends SurfaceView {

    private int width;
    private int height;
    private Paint mPaint = null;
    public GridSurfaceView(Context context) {
        this(context, null);
    }

    public GridSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GridSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        mPaint = new Paint();

        mPaint.setARGB(128,255,255,255);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(1);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);

        float pxDimension = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40,getContext().getResources().getDisplayMetrics());

        width = wm.getDefaultDisplay().getWidth() ;
        height = width;
        setMeasuredDimension(width, height);

        Log.d("Scope", "width:" + width + " height "+ height + " pxDimension" + pxDimension);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

//        canvas.drawLine(0, height/2, width, height/2, mPaint);
//        canvas.drawLine(width/2, 0, width/2, height, mPaint);
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

