package my.com.pack;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class CS133ProjectActivity extends Activity {
	private SensorManager mSensorManager;
	private TextView mTextViewLight;
	private float xValue = 0.0f;
	private float yValue = 0.0f;
	private float zValue = 0.0f;
	private Lock lock = new ReentrantLock();
	private SensorEventListener mEventListenerOri;
	private float []mLastMagFields;
	private float []mLastAccels;
	private float []mRotationMatrix;
	private float []mOrientation;
	boolean lastAccels;
	boolean lastMags;
	private TextView mTextViewLight1;
	private TextView mTextViewLight2;
	static MediaPlayer mp;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mTextViewLight = (TextView) findViewById(R.id.editText);
		mTextViewLight1 = (TextView) findViewById(R.id.editText1);
		mTextViewLight2 = (TextView) findViewById(R.id.editText2);
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE); 
		mLastAccels = new float[3];
		mLastMagFields = new float[3];
		mRotationMatrix = new float[16];
		mOrientation = new float[4];
		lastAccels = false;
		lastMags = false;
		mp = new MediaPlayer();

		mEventListenerOri = new SensorEventListener(){
			public void onAccuracyChanged(Sensor arg0, int arg1) {
				// TODO Auto-generated method stub
			}
			public void onSensorChanged(SensorEvent event) {

				if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
					System.arraycopy(event.values, 0, mLastAccels, 0, 3);
					if (!lastAccels)
						lastAccels = true;
					if (lastMags)
						computeOrientation();
				}
				if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
					System.arraycopy(event.values, 0, mLastMagFields, 0, 3);
					if (!lastMags)
						lastMags = true;
					if (lastAccels)
						computeOrientation();
				}
			}
		};
	}

	public void updateUI() {
		if (lock.tryLock()){
			mTextViewLight.setText("X: " + xValue);	
			mTextViewLight1.setText("Y: " + yValue);
			mTextViewLight2.setText("Z: " + zValue);
			lock.unlock();
		}

	}

	public void playSound(Uri path){
		if (mp.isPlaying()) {
			return;
		}
		mp.reset();
		try {
			mp.setDataSource(getApplicationContext(), path);
			mp.prepare();
		}
		catch (Exception ex) {
			Log.d("main thread ex", ex.getStackTrace()[0].toString() + "path : " + path);
		}
		mp.start();
		
	}

	public void computeOrientation(){
		if (SensorManager.getRotationMatrix(mRotationMatrix, null, mLastAccels, mLastMagFields)) {
			SensorManager.getOrientation(mRotationMatrix, mOrientation);

			xValue = mOrientation[1] * 57.2957795f;
			yValue = mOrientation[2] * 57.2957795f;
			zValue = mOrientation[0] * 57.2957795f;
			updateUI();
			if (zValue > 150 || zValue < -150)
				playSound(Uri.parse("android.resource://my.com.pack/" + R.raw.stupid));
			else if (yValue < -50)
				playSound(Uri.parse("android.resource://my.com.pack/" + R.raw.beepboop));
			else if (yValue > 50)
				playSound(Uri.parse("android.resource://my.com.pack/" + R.raw.bonk));
			else if (xValue > 45)
				playSound(Uri.parse("android.resource://my.com.pack/" + R.raw.kaboom));
			else if (xValue < -45)
				playSound(Uri.parse("android.resource://my.com.pack/" + R.raw.danke));
		}
	}

	public void onResume(){
		super.onResume();

		mSensorManager.registerListener(mEventListenerOri, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_GAME);
		mSensorManager.registerListener(mEventListenerOri, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
	}

	public void onStop(){
		mSensorManager.unregisterListener(mEventListenerOri);
		super.onStop();
	}
}