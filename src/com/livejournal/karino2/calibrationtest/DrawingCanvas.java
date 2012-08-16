package com.livejournal.karino2.calibrationtest;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.View;

public class DrawingCanvas extends View {
	
	public interface MessageListener {
		void notify(String message);
	}
	
	MessageListener mMessageListener = new MessageListener(){
		public void notify(String message) {
		}};
		
	public void setMessageListener(MessageListener listener)
	{
		mMessageListener = listener;
	}

    private Bitmap  mBitmap;
    private Canvas  mCanvas;
    private Path    mPath;
    private Paint   mBitmapPaint;
    private Paint       mPaint;
    private Paint mCursorPaint;
    
    private static final int CALIBRATION_NO = 0;
    private static final int CALIBRATION_POS1 = 1;
    /*
    private static final int CALIBRATION_POS2 = 2;
    private static final int CALIBRATION_POS3 = 3;
    private static final int CALIBRATION_POS4 = 4;
    private static final int CALIBRATION_POS5 = 5;
    */
    
    private static final int CALIBRATION_ANGLE_FROM_UP = 6;
    private static final int CALIBRATION_ANGLE_FROM_DOWN = 7;
    private static final int CALIBRATION_ANGLE_FROM_LEFT = 8;
    private static final int CALIBRATION_ANGLE_FROM_RIGHT = 9;
    
    
    private int mCalibrationState = CALIBRATION_NO;
    
    public void startCalibration()
    {
    	mCalibrationState = CALIBRATION_POS1;
    	invalidate();
    }


	public DrawingCanvas(Context context, AttributeSet attrs) {
		super(context, attrs);
        mPath = new Path();
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);
        
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(0xFF00FF00);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(12);
        
        mCursorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCursorPaint.setStyle(Paint.Style.STROKE);
        mCursorPaint.setPathEffect(new DashPathEffect(new float[]{5, 2}, 0));

	}
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mCenterX = ((float)w)/2F;
        mCenterY = ((float)h)/2F;
        resetCanvas(w, h);
    }
    
    public void resetCanvas() {
    	resetCanvas(mBitmap.getWidth(), mBitmap.getHeight());
    	invalidate();
    }
	public void resetCanvas(int w, int h) {
		mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
	}
	
	
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(0xFFFFFFFF);
        
        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);

        canvas.drawPath(mPath, mPaint);
        canvas.drawOval(mBrushCursorRegion, mCursorPaint);
        
        switch(mCalibrationState)
        {
        case CALIBRATION_NO:
        	break;
        case CALIBRATION_POS1:
        	drawCross(canvas, mCenterX, mCenterY);
        	break;
        }
    }
    
    private final int CROSS_SIZE = 20;
    private void drawCross(Canvas canvas, float x, float y) {
    	canvas.drawLine(x-CROSS_SIZE, y, x+CROSS_SIZE, y, mCursorPaint);
    	canvas.drawLine(x, y-CROSS_SIZE, x, y+CROSS_SIZE, mCursorPaint);
	}

	private float mCenterX, mCenterY;

    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;
    
    private static final float CURSOR_SIZE=10;
    RectF mBrushCursorRegion = new RectF(0f, 0f, 0f, 0f);
    
    private void setBrushCursorPos(float x, float y)
    {
    	mBrushCursorRegion = new RectF(x-CURSOR_SIZE/2, y-CURSOR_SIZE/2,
				x+CURSOR_SIZE/2, y+CURSOR_SIZE/2);

    }
    
    Matrix mTranslate = new Matrix();
    float[] mPointBuf = new float[2];
    
    int mDeb = 0;
    
    public boolean onTouchEvent(MotionEvent event) {
    	if(MotionEvent.TOOL_TYPE_STYLUS != event.getToolType(0))
    		return true;
        float x = event.getX();
        float y = event.getY();
        
        if(mCalibrationState == CALIBRATION_POS1)
        {
            if (MotionEvent.ACTION_UP == event.getAction()) {
            	mCalibrationState = CALIBRATION_NO;
            	mTranslate.reset();
            	mTranslate.postTranslate(mCenterX-x, mCenterY-y);
            }
        	return true;
        }
        
        applyCalibration(x, y);
        x = mPointBuf[0];
        y = mPointBuf[1];
        
        setBrushCursorPos(x, y);
        
        float dist = event.getAxisValue(MotionEvent.AXIS_DISTANCE);
        float orien = event.getAxisValue(MotionEvent.AXIS_ORIENTATION);
        float tilt = event.getAxisValue(MotionEvent.AXIS_TILT);
        mMessageListener.notify("dist:" + String.valueOf(dist) + ", orient: " + String.valueOf(orien) + ", tilt: " + String.valueOf(tilt) + "," + String.valueOf(mDeb++));
        InputDevice dev = event.getDevice();
        List<InputDevice.MotionRange> range = dev.getMotionRanges();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mPath.reset();
                mPath.moveTo(x, y);
                mX = x;
                mY = y;
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                float dx = Math.abs(x - mX);
                float dy = Math.abs(y - mY);
                if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                    mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
                    mX = x;
                    mY = y;
                }
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                mPath.lineTo(mX, mY);
                mCanvas.drawPath(mPath, mPaint);
                mPath.reset();
                invalidate();
                break;
        }
        return true;
    }

    
    boolean mApplyTranslateCalibration = false;
    public void setPositionCalibrationEnabled(boolean val)
    {
    	mApplyTranslateCalibration = val;
    }

	public void applyCalibration(float x, float y) {
		mPointBuf[0] = x;
        mPointBuf[1] = y;
		if(mApplyTranslateCalibration)
			mTranslate.mapPoints(mPointBuf);
	}
}
