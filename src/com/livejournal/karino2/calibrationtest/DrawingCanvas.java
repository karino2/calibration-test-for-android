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


    class FivePointCalibration {

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


        ArrayList<Float> mResults = new ArrayList<Float>();

        private int mCalibrationState = CALIBRATION_NO;

        void onDraw(Canvas canvas) {

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

        boolean onTouchEvent(MotionEvent event) {
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
            return false;

        }

        private void resultToTransform() {
            Matrix transform = new Matrix();

    	/*
    	 * Y
    	 * mCenterX, mCenterY
    	 * mX1, mY1,
    	 * mX2, mY1,
    	 * mX1, mY2,
    	 * mX2, mY2
    	 */
    	/* Y'
    	 * mCenterX, mX1, mX2, mX1, mX2
    	 * mCenterY, mY1, mY1, mY2, mY2
    	 */
    	/* X'
    	 * res(0), res(2), res(4), res(6), res(8)
    	 * res(1), res(3), res(5), res(7), res(9)
    	 */
    	/*X
    	 * res(0), res(1)
    	 * res(2), res(3)
    	 * res(4), res(5)
    	 * res(6), res(7)
    	 * res(8), res(9)
    	 */
    	/*
    	 * calculate
    	 * Y' * X * (X' X)^-1
    	 */


            // calculate  X' * X
            float[] XX = new float[9];
            XX[8] = 5;
            for(int i = 0; i < 5; i++) {
                XX[0] += Math.pow(mResults.get(i*2), 2);
                XX[1] += mResults.get(i*2)*mResults.get(i*2+1);
                XX[2] +=mResults.get(i*2);
                XX[3] += mResults.get(i*2)*mResults.get(i*2+1);
                XX[4] += Math.pow(mResults.get(i*2+1), 2);
                XX[5] += mResults.get(i*2+1);
                XX[6] += mResults.get(i*2);
                XX[7] += mResults.get(i*2+1);
            }

            float[] Ys = new float[] {
                    mCenterX, mCenterY,
                    mX1, mY1,
                    mX2, mY1,
                    mX1, mY2,
                    mX2, mY2,
            };

    	/*
    	  X
    	 * res(0), res(1)
    	 * res(2), res(3)
    	 * res(4), res(5)
    	 * res(6), res(7)
    	 * res(8), res(9)
    	 */

            // calculate Y*X
            float[] YX = new float[9];
            YX[8] = 5;
            for(int i = 0; i < 5; i++) {
                YX[0] += Ys[i*2]*mResults.get(i*2);
                YX[1] += Ys[i*2]*mResults.get(i*2+1);
                YX[2] += Ys[i*2];
                YX[3] += Ys[i*2+1]*mResults.get(i*2);
                YX[4] += Ys[i*2+1]*mResults.get(i*2+1);
                YX[5] += Ys[i*2+1];
                YX[6] += mResults.get(i*2);
                YX[7] += mResults.get(i*2+1);
            }

            // calculate Y*X * (X' X)-1
            Matrix YXMat = new Matrix();
            YXMat.setValues(YX);

            Matrix XXMat = new Matrix();
            Matrix XXInv = new Matrix();
            XXMat.setValues(XX);
            XXMat.invert(XXInv);

            // YXMat * XXInv
            transform.setConcat(YXMat, XXInv);
            setNewTransform(transform);
        }

        void start() {
            mCalibrationState = CALIBRATION_POS1;
        }
    }

    FivePointCalibration calibration = new FivePointCalibration();

    private Bitmap  mBitmap;
    private Canvas  mCanvas;
    private Path    mPath;
    private Paint   mBitmapPaint;
    private Paint       mPaint;
    private Paint mCursorPaint;

    public void startCalibration()
    {
        calibration.start();
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

        calibration.onDraw(canvas);
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

    public void setNewTransform(Matrix transform) {
        mTransform = transform;
    }
    
    int mDeb = 0;
    
    
    boolean mDownHandled = false;
    
    public boolean onTouchEvent(MotionEvent event) {
    	/*
    	if(MotionEvent.TOOL_TYPE_STYLUS != event.getToolType(0))
    		return true;
    		*/
        if(calibration.onTouchEvent(event))
            return true;

        float x = event.getX();
        float y = event.getY();


        applyCalibration(x, y);
        x = mPointBuf[0];
        y = mPointBuf[1];
        
        setBrushCursorPos(x, y);

        float dist = event.getAxisValue(MotionEvent.AXIS_DISTANCE);
        float orien = event.getAxisValue(MotionEvent.AXIS_ORIENTATION);
        float tilt = event.getAxisValue(MotionEvent.AXIS_TILT);
        mMessageListener.notify("dist:" + String.valueOf(dist) + ", orient: " + String.valueOf(orien) + ", tilt: " + String.valueOf(tilt) + ", xy: " + String.valueOf(x)+","+ String.valueOf(event.getRawX())+ "," + String.valueOf(event.getOrientation()));
        // mMessageListener.notify("dist:" + String.valueOf(dist) + ", orient: " + String.valueOf(orien) + ", tilt: " + String.valueOf(tilt) + "," + String.valueOf(x)+","+ String.valueOf(event.getRawX())+ "," + String.valueOf(event.getOrientation()));
        /*
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
