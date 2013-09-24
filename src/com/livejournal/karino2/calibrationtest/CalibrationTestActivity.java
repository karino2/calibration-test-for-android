package com.livejournal.karino2.calibrationtest;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

public class CalibrationTestActivity extends Activity {
    /** Called when the activity is first created. */
	DrawingCanvas mDCan;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mDCan = (DrawingCanvas)findViewById(R.id.canvas);
        mDCan.setMessageListener(new DrawingCanvas.MessageListener() {
			
			public void notify(String message) {
				showExplanation(message);
			}
		});
        ((Button)findViewById(R.id.ClearButton)).setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                mDCan.resetCanvas();
            }
        });
        ((Button)findViewById(R.id.CalibrationButton)).setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				mDCan.startCalibration5();
			}
		});
        ((Button)findViewById(R.id.start9Button)).setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                mDCan.startCalibration9();
            }
        });
        ((Button)findViewById(R.id.start25Button)).setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                mDCan.startCalibration25();
            }
        });
        ((CheckBox)findViewById(R.id.PositionCalibrationCheck)).setOnCheckedChangeListener(new OnCheckedChangeListener(){

			public void onCheckedChanged(CompoundButton arg0, boolean val) {
				mDCan.setPositionCalibrationEnabled(val);
			}});
    }
    
    
    void showExplanation(String msg)
    {
    	((TextView)findViewById(R.id.ExplationTV)).setText(msg);
    }
}