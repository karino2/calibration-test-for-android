package com.livejournal.karino2.calibrationtest;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
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
    // center
    private static final int CALIBRATION_POS1 = 1;
    // 0,0
    private static final int CALIBRATION_POS2 = 2;
    // w, 0
    private static final int CALIBRATION_POS3 = 3;
    // 0, h
    private static final int CALIBRATION_POS4 = 4;
    // w, h
    private static final int CALIBRATION_POS5 = 5;
    
    /*
    private static final int CALIBRATION_ANGLE_FROM_UP = 6;
    private static final int CALIBRATION_ANGLE_FROM_DOWN = 7;
    private static final int CALIBRATION_ANGLE_FROM_LEFT = 8;
    private static final int CALIBRATION_ANGLE_FROM_RIGHT = 9;
    */
    
    
    private int mCalibrationState = CALIBRATION_NO;
    
    public void startCalibration()
    {
    	mCalibrationState = CALIBRATION_POS1;
    	invalidate();
    }
    
    ArrayList<Float> mResults = new ArrayList<Float>();


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
	int mWidth;
	int mHeight;
	int mX1, mX2, mY1, mY2;
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mCenterX = ((float)w)/2F;
        mCenterY = ((float)h)/2F;
        mWidth = w;
        mHeight = h;
        mX1 = CROSS_SIZE*2;
        mY1 = CROSS_SIZE*2;
        mX2 = mWidth-(CROSS_SIZE*2);
        mY2 = mHeight-(CROSS_SIZE*2);
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
        case CALIBRATION_POS2:
        	drawCross(canvas, mX1, mY1);
        	break;
        case CALIBRATION_POS3:
        	drawCross(canvas, mX2, mY1);
        	break;
        case CALIBRATION_POS4:
        	drawCross(canvas, mX1, mY2);
        	break;
        case CALIBRATION_POS5:
        	drawCross(canvas, mX2, mY2);
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
    
    Matrix mTransform = new Matrix();
    float[] mPointBuf = new float[2];
    
    int mDeb = 0;
    
    
    boolean mDownHandled = false;
    
    public boolean onTouchEvent(MotionEvent event) {
    	/*
    	if(MotionEvent.TOOL_TYPE_STYLUS != event.getToolType(0))
    		return true;
    		*/
        float x = event.getX();
        float y = event.getY();

        switch(mCalibrationState)
        {
        case CALIBRATION_POS1:
            if (MotionEvent.ACTION_DOWN == event.getAction()) {
            	mCalibrationState = CALIBRATION_POS2;
            	mResults.clear();
            	mResults.add(x);
            	mResults.add(y);
            	invalidate();
            }
        	return true;
        case CALIBRATION_POS2:
            if (MotionEvent.ACTION_DOWN == event.getAction()) {
            	mCalibrationState = CALIBRATION_POS3;
            	mResults.add(x);
            	mResults.add(y);
            	invalidate();
            }
        	return true;
        case CALIBRATION_POS3:
            if (MotionEvent.ACTION_DOWN == event.getAction()) {
            	mCalibrationState = CALIBRATION_POS4;
            	mResults.add(x);
            	mResults.add(y);
            	invalidate();
            }
        	return true;
        case CALIBRATION_POS4:
            if (MotionEvent.ACTION_DOWN == event.getAction()) {
            	mCalibrationState = CALIBRATION_POS5;
            	mResults.add(x);
            	mResults.add(y);
            	invalidate();
            }
        	return true;
        case CALIBRATION_POS5:
            if (MotionEvent.ACTION_DOWN == event.getAction()) {
            	mCalibrationState = CALIBRATION_NO;
            	mResults.add(x);
            	mResults.add(y);
            	resultToTransform();
            	invalidate();
            }
        	return true;
        	
        }
        
        applyCalibration(x, y);
        x = mPointBuf[0];
        y = mPointBuf[1];
        
        setBrushCursorPos(x, y);

        /*
        float dist = event.getAxisValue(MotionEvent.AXIS_DISTANCE);
        float orien = event.getAxisValue(MotionEvent.AXIS_ORIENTATION);
        float tilt = event.getAxisValue(MotionEvent.AXIS_TILT);
        mMessageListener.notify("dist:" + String.valueOf(dist) + ", orient: " + String.valueOf(orien) + ", tilt: " + String.valueOf(tilt) + "," + String.valueOf(x)+","+ String.valueOf(event.getRawX())+ "," + String.valueOf(event.getOrientation()));
        MotionEvent.PointerCoords coords = new MotionEvent.PointerCoords();
        event.getPointerCoords(0, coords);
        InputDevice dev = event.getDevice();
        List<InputDevice.MotionRange> range = dev.getMotionRanges();
        */

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            	mDownHandled = true;
                mPath.reset();
                mPath.moveTo(x, y);
                mX = x;
                mY = y;
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
            	if(!mDownHandled)
            		break;
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
            	if(!mDownHandled)
            		break;
            	mDownHandled = false;
                mPath.lineTo(mX, mY);
                mCanvas.drawPath(mPath, mPaint);
                mPath.reset();
                invalidate();
                break;
        }
        return true;
    }

	/*
	float[] targetBuf = new float[]{mCenterX, mCenterY, (float)CROSS_SIZE,(float)CROSS_SIZE,
			(float)(mWidth-CROSS_SIZE), (float)CROSS_SIZE,
			(float)CROSS_SIZE, (float)(mHeight-CROSS_SIZE),
			(float)(mWidth-CROSS_SIZE), (float)(mHeight-CROSS_SIZE)
	};
	*/
    
    private void resultToTransform() {
    	float[] actualBuf = new float[]{
    			mResults.get(0), mResults.get(2), mResults.get(4),
    			mResults.get(1), mResults.get(3), mResults.get(5),
    			1, 1, 1
    	};
    	float[] targetBuf = new float[]{mCenterX, mX1, mX2,
    			mCenterY, mY1, mY1,
    			1, 1, 1
    	};
    	
    	Matrix trans1 = createTransformMatrix(actualBuf, targetBuf);
    	actualBuf = new float[] {
    			mResults.get(0), mResults.get(6), mResults.get(8),
    			mResults.get(1), mResults.get(7), mResults.get(9),
    			1, 1, 1
    	};
    	targetBuf = new float[] {
    			mCenterX, mX1, mX2,
    			mCenterY, mY2, mY2,
    			1, 1, 1
    	};
    	Matrix trans2 = createTransformMatrix(actualBuf, targetBuf);
    	float[] vals = new float[9];
    	float[] vals2 = new float[9];
    	
    	trans1.getValues(vals);
    	trans2.getValues(vals2);
    	for(int i = 0; i <vals.length; i++)
    	{
    		vals[i] = (vals[i] + vals2[i])/2f;
    	}
    	mTransform.setValues(vals);
    	// mTransform.set(trans1);
    	
    	// target1.postConcat(tmp);
    	/*
    	Matrix tmp2 = new Matrix();
    	tmp2.setConcat(tmp, result1);
    	float[] tmpBuf = new float[9];
    	float[] tmpPts2 = new float[] {
    			mResults.get(0), mResults.get(1),
    			mResults.get(2), mResults.get(3),
    	};
    	tmp.mapPoints(tmpPts2);
    	*/
    	// target1.getValues(tmpBuf);
    	/*
    	tmp.getValues(tmpBuf);
    	copyBuf(tmpBuf, ptsBuf);
    	float scaleX = FloatMath.sqrt(tmpBuf[0]*tmpBuf[0]+tmpBuf[1]*tmpBuf[1]);
    	float scaleY = FloatMath.sqrt(tmpBuf[3]*tmpBuf[3]+tmpBuf[4]*tmpBuf[4]);
    	mTransform.setRotate(ptsBuf[1]);
    	mTransform.postScale(scaleX, scaleY);
    	mTransform.postTranslate(ptsBuf[2], ptsBuf[5]);
    	mTransform.getValues(ptsBuf);
    	
    	Matrix test = new Matrix();
    	test.setRotate(45);
    	test.getValues(ptsBuf);
    	test.postScale(2, 2);
    	test.getValues(ptsBuf);
    	*/
//    	mTransform.set(test);
    	
    	// mTransform.set(target1);
    	/*
    	float[] ptsBuf = new float[8];
    	for(int i =0; i < 4; i++)
    	{
    		ptsBuf[i*2] = mResults.get(i*2);
    		ptsBuf[i*2+1] = mResults.get(i*2+1);
    	}
    	float[] targetBuf = new float[]{mCenterX, mCenterY, (float)CROSS_SIZE,(float)CROSS_SIZE,
    			(float)(mWidth-CROSS_SIZE), (float)CROSS_SIZE,
    			(float)CROSS_SIZE, (float)(mHeight-CROSS_SIZE)
    	};
    	
    	first.setPolyToPoly(ptsBuf, 0, targetBuf, 0, 4);
    	
    	Matrix second = new Matrix();
		ptsBuf[0] = mResults.get(0);
		ptsBuf[1] = mResults.get(0);
    	for(int i =0; i < 3; i++)
    	{
    		ptsBuf[2+i*2] = mResults.get(4+i*2);
    		ptsBuf[2+i*2+1] = mResults.get(4+i*2+1);
    	}
		
    	targetBuf = new float[]{mCenterX, mCenterY, 
    			(float)(mWidth-CROSS_SIZE), (float)CROSS_SIZE,
    			(float)CROSS_SIZE, (float)(mHeight-CROSS_SIZE),
    			(float)(mWidth-CROSS_SIZE), (float)(mHeight-CROSS_SIZE)
    	};
    	second.setPolyToPoly(ptsBuf, 0, targetBuf, 0, 4);
    	float[] vals1 = new float[9];
    	float[] vals2 = new float[9];
    	first.getValues(vals1);
    	second.getValues(vals2);
    	for(int i = 0; i < vals2.length; i++)
    	{
    		vals2[i] = (vals2[i]+vals1[i])/2f;
    	}
    	// mTransform.setValues(vals2);
    	mTransform.set(first);
    	*/
	}

	public Matrix createTransformMatrix(float[] actualBuf, float[] targetBuf) {
		Matrix result1 = new Matrix();
    	result1.setValues(actualBuf);
    	Matrix target1 = new Matrix();
    	target1.setValues(targetBuf);
    	Matrix trans1 = new Matrix();
    	result1.invert(trans1);
    	trans1.postConcat(target1);
		return trans1;
	}

	public void copyBuf(float[] dstBuf, float[] srcBuf) {
		for(int i = 0; i < dstBuf.length;i++)
    	{
    		dstBuf[i] = srcBuf[i];
    	}
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
			mTransform.mapPoints(mPointBuf);
	}
}
