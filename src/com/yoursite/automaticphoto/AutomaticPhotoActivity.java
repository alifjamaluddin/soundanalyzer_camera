package com.yoursite.automaticphoto;
 
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;




import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
 
public class AutomaticPhotoActivity extends Activity{
	private Camera camera; // camera object
	private TextView textTimeLeft; // time left field
	SoundMeter mSensor;
	private PowerManager.WakeLock mWakeLock;
	double amp;
	private static final int POLL_INTERVAL = 300;
	private Handler mHandler = new Handler();
 
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		textTimeLeft=(TextView)findViewById(R.id.textTimeLeft); // make time left object
		camera = Camera.open();
mSensor = new SoundMeter();
		
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "NoiseAlert");
		SurfaceView view = new SurfaceView(this);
		
		try {
			camera.setPreviewDisplay(view.getHolder()); // feed dummy surface to surface
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		camera.startPreview();   
	}
 
	Camera.PictureCallback jpegCallBack=new Camera.PictureCallback() {		
		public void onPictureTaken(byte[] data, Camera camera) {
			// set file destination and file name
			String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
			String picName = "IMG_" + timeStamp + ".jpg";
			File destination=new File(Environment.getExternalStorageDirectory(),picName);
			try {
				Bitmap userImage = BitmapFactory.decodeByteArray(data, 0, data.length);
				// set file out stream
				FileOutputStream out = new FileOutputStream(destination);
				// set compress format quality and stream
				userImage.compress(Bitmap.CompressFormat.JPEG, 90, out);		
				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
 
		}
	};
 
	public void startTimer(){
		
		// 5000ms=5s at intervals of 1000ms=1s so that means it lasts 3 seconds
		new CountDownTimer(3000,1000){
 
			@SuppressWarnings("deprecation")
			@Override
			public void onFinish() {
				// count finished
				textTimeLeft.setText("Picture Taken");
				camera.startPreview();
				camera.takePicture( null, null, jpegCallBack);
			}
 
			@Override
			public void onTick(long millisUntilFinished) {
				// every time 1 second passes
				textTimeLeft.setText("Seconds Left: "+millisUntilFinished/1000);
			}
 
		}.start();
	}

	private Runnable mPollTask = new Runnable() {
        public void run() {
                amp = mSensor.getAmplitude();
                //Log.i("Noise Alert", "Value : " + String.valueOf(amp));
                
                ((TextView) findViewById(R.id.amplitude_value_text)).setText("Value : " + String.valueOf(amp));
                mHandler.postDelayed(mPollTask, POLL_INTERVAL);
                
                if(amp > 10){
                	startTimer();
//                	Toast.makeText(getApplicationContext(), "Photo Captured", Toast.LENGTH_LONG).show();		
                }
//                try {
//					capture();
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}               
                
               
                	
        }
};
 

@Override
protected void onResume() {
	super.onResume();
	//test
//	try {
//		mSensor.start();
//	} catch (Exception e) {
//		// TODO Auto-generated catch block
//		e.printStackTrace();
//	}
	//end-test
	if (!mWakeLock.isHeld()) 
        mWakeLock.acquire();
	try {
		mSensor.start();
	} catch (Exception e) {
		e.printStackTrace();
	}
	mHandler.postDelayed(mPollTask, POLL_INTERVAL);
}

@Override
protected void onStop() {
	super.onStop();
	if (mWakeLock.isHeld()) 
        mWakeLock.release();
	mHandler.removeCallbacks(mPollTask);
	mSensor.stop();
}
}