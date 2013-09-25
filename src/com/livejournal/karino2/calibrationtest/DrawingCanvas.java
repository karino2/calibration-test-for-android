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

    public void startCalibration9() {
        calibration = new NPointCalibration(9, new Float[] {
                (float)mX1, (float)mY1,
                mCenterX, (float)mY1,
                (float)mX2, (float)mY1,
                (float)mX1, mCenterY,
                mCenterX, mCenterY,
                (float)mX2, mCenterY,
                (float)mX1, (float)mY2,
                mCenterX, (float)mY2,
                (float)mX2, (float)mY2,
        });
        startCalibration();
    }

    public void startCalibration25() {
        float[] ys = new float[] { mY1, (mY1+mCenterY)/2F, mCenterY, (mCenterY+mY2)/2F, mY2};

        ArrayList<Float> yList = new ArrayList<Float>();
        for(int i = 0; i < 5; i++) {
            yList.add((float)mX1); yList.add(ys[i]);
            yList.add((mX1+mCenterX)/2F); yList.add(ys[i]);
            yList.add(mCenterX); yList.add(ys[i]);
            yList.add((mX2+mCenterX)/2F); yList.add(ys[i]);
            yList.add((float)mX2); yList.add(ys[i]);
        }


        calibration = new NPointCalibration(25, yList.toArray(new Float[]{0F}));
        startCalibration();
    }

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

    interface Calibration {
        void onDraw(Canvas canvas);
        boolean onTouchEvent(MotionEvent event);
        void start();
        Matrix getTransform() ;
    }

    class NPointCollector {

        NPointCollector(int pointNum, Float[] Ys) {
            nPointNum = pointNum;
            this.Ys = Ys;
        }
        private static final int CALIBRATION_NO = 0;
        private static final int CALIBRATION_POS1 = 1;
        int nPointNum; /* = 9; */
        Float[] Ys;
        /* = new float[] {
                mX1, mY1,
                mCenterX, mY1,
                mX2, mY1,
                mX1, mCenterY,
                mCenterX, mCenterY,
                mX2, mCenterY,
                mX1, mY2,
                mCenterX, mY2,
                mX2, mY2,
        };
        */

        ArrayList<Float> mResults = new ArrayList<Float>();

        private int mCalibrationState = CALIBRATION_NO;

        public void start() {
            mCalibrationState = CALIBRATION_POS1;
        }

        public void onDraw(Canvas canvas) {
            if(mCalibrationState == CALIBRATION_NO)
                return;

            drawCross(canvas, Ys[(mCalibrationState-1)*2], Ys[(mCalibrationState-1)*2+1]);
        }

        public ArrayList<Float> getResults() {
            return mResults;
        }

        public boolean isDormant() {
            return mCalibrationState == CALIBRATION_NO;
        }

        public boolean onTouchEvent(MotionEvent event) {
            if(mCalibrationState == CALIBRATION_NO)
                throw new RuntimeException("never called for this situation");


            float x = event.getX();
            float y = event.getY();

            if(mCalibrationState == 1)
                mResults.clear();

            if(mCalibrationState < nPointNum) {
                if (MotionEvent.ACTION_DOWN == event.getAction()) {
                    mCalibrationState++;
                    mResults.add(x);
                    mResults.add(y);
                    invalidate();
                }
                return false;
            }

            if(mCalibrationState == nPointNum) {
                if (MotionEvent.ACTION_DOWN == event.getAction()) {
                    mCalibrationState = CALIBRATION_NO;
                    mResults.add(x);
                    mResults.add(y);
                    invalidate();
                    return true;
                }
            }
            return false;
        }
    }

    class ResultToTransformer {
        Matrix resultToTransform(ArrayList<Float> results, ArrayList<Float> Ys, int pointNum) {
            Matrix transform = new Matrix();


            // calculate  X' * X
            float[] XX = new float[9];
            XX[8] = 5;
            for(int i = 0; i < pointNum; i++) {
                XX[0] += Math.pow(results.get(i*2), 2);
                XX[1] += results.get(i*2)*results.get(i*2+1);
                XX[2] +=results.get(i*2);
                XX[3] += results.get(i*2)*results.get(i*2+1);
                XX[4] += Math.pow(results.get(i*2+1), 2);
                XX[5] += results.get(i*2+1);
                XX[6] += results.get(i*2);
                XX[7] += results.get(i*2+1);
            }

            // calculate Y*X
            float[] YX = new float[9];
            YX[8] = 5;
            for(int i = 0; i < pointNum; i++) {
                YX[0] += Ys.get(i*2)*results.get(i*2);
                YX[1] += Ys.get(i*2)*results.get(i*2+1);
                YX[2] += Ys.get(i*2);
                YX[3] += Ys.get(i*2+1)*results.get(i*2);
                YX[4] += Ys.get(i*2+1)*results.get(i*2+1);
                YX[5] += Ys.get(i*2+1);
                YX[6] += results.get(i*2);
                YX[7] += results.get(i*2+1);
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
            return transform;

        }
    }

    class NPointCalibration implements Calibration{

        NPointCollector collector;
        ResultToTransformer resultToTransformer;

        NPointCalibration(int pointNum, Float[] Ys) {
            collector = new NPointCollector(pointNum, Ys);
            resultToTransformer = new ResultToTransformer();
            nPointNum = pointNum;
            this.Ys = Ys;
        }


        int nPointNum; /* = 9; */
        Float[] Ys;
        /* = new float[] {
                mX1, mY1,
                mCenterX, mY1,
                mX2, mY1,
                mX1, mCenterY,
                mCenterX, mCenterY,
                mX2, mCenterY,
                mX1, mY2,
                mCenterX, mY2,
                mX2, mY2,
        };
        */


        public void onDraw(Canvas canvas) {
            if(collector.isDormant())
                return;
            collector.onDraw(canvas);
        }

        public boolean onTouchEvent(MotionEvent event) {
            if(collector.isDormant())
                return false;

            boolean isLast = collector.onTouchEvent(event);
            if(isLast) {
                resultToTransform();

            }
            return true;
        }

        Matrix transform = new Matrix();

        public Matrix getTransform() {
            return transform;
        }

        private void resultToTransform() {
            ArrayList<Float> ysAList = new ArrayList<Float>();
            for(float y : Ys) {
                ysAList.add(y);
            }

            transform = resultToTransformer.resultToTransform(collector.getResults(), ysAList, nPointNum);
        }

        public void start() {
            collector.start();
        }
    }



    Calibration calibration = null;

    private Bitmap  mBitmap;
    private Canvas  mCanvas;
    private Path    mPath;
    private Paint   mBitmapPaint;
    private Paint       mPaint;
    private Paint mCursorPaint;

    Calibration getCalibration() {
        if(calibration == null) {
            setFivePointCalibration();
        }
        return calibration;
    }

    public void startCalibration5()
    {
        setFivePointCalibration();
        startCalibration();
    }

    private void setFivePointCalibration() {
        calibration = new NPointCalibration(5, new Float[] {
            mCenterX, mCenterY,
                (float)mX1, (float)mY1,
                (float)mX2, (float)mY1,
                (float)mX1, (float)mY2,
                (float)mX2, (float)mY2,
        });
    }

    private void startCalibration() {
        getCalibration().start();
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

        getCalibration().onDraw(canvas);
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
    
    float[] mPointBuf = new float[2];

    int mDeb = 0;
    
    
    boolean mDownHandled = false;
    
    public boolean onTouchEvent(MotionEvent event) {
    	/*
    	if(MotionEvent.TOOL_TYPE_STYLUS != event.getToolType(0))
    		return true;
    		*/
        if(getCalibration().onTouchEvent(event))
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
    

	boolean mApplyTranslateCalibration = false;
    public void setPositionCalibrationEnabled(boolean val)
    {
    	mApplyTranslateCalibration = val;
    }

	public void applyCalibration(float x, float y) {
		mPointBuf[0] = x;
        mPointBuf[1] = y;
		if(mApplyTranslateCalibration) {
            getCalibration().getTransform().mapPoints(mPointBuf);
        }
	}
}
