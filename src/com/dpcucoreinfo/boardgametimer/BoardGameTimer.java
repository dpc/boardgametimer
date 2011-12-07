package com.dpcucoreinfo.boardgametimer;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.io.IOException;

public class BoardGameTimer extends Activity
{
	private RelativeLayout mainLayout;
	private TextView centralText;
	private CountDownTimer timer;
	private boolean timerRunning;
	private MediaPlayer player;
	private AudioManager audioManager;
	private int countdownValues[] = { 5, 15, 30, 60, 120, 300};
	private int countdownSelected = 2;
	private final int hurryUpTime = 10;
	private int backgroundWarn = 0xFFCC0000;
	private int backgroundNormal = 0xFF0000;
	private int backgroundRunning = 0xFF003399;
	private int currentTimer = 0;
	private int currentBackground = 0;


	private void restartPlayer() {
		if (player.isPlaying()) {
			player.stop();
			try {
				player.prepare();
				player.seekTo(0);
			} catch (IOException ex) {
				// ignore
			}
		}
	}

	private void onStartCounting() {
		timerRunning = true;
		if (countdown() > hurryUpTime) {
			updateBackgroundColor(backgroundRunning);
		}

		restartPlayer();
		timer.cancel();
		timer.start();
	}

	private void onStopCounting() {
		timerRunning = false;
		updateBackgroundColor(backgroundNormal);
		updateTimerText(countdown());

	}

	private void initTimer() {
		timer = new CountDownTimer(countdown() * 1000, 250) {
			public void onTick(long millisUntilFinished) {
				int newTime = (int)((millisUntilFinished + 999) / 1000);
				updateTimerText(newTime);
				if (millisUntilFinished < 5 * 1000) {
					if (((millisUntilFinished / 250) % 2) == 0) {
						updateBackgroundColor(backgroundWarn);
					} else {
						updateBackgroundColor(backgroundNormal);
					}
				} else if (millisUntilFinished < hurryUpTime * 1000) {
					updateBackgroundColor(backgroundWarn);
				}
			}

			public void onFinish() {
				onStopCounting();
				if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
					player.start();
				}
			}
		};
	}
	private OnTouchListener touchListener = new OnTouchListener() {
		public boolean onTouch(View v, android.view.MotionEvent mev) {
			onStartCounting();
			return true;
		}

	};

	private void updateTimerText(int sec) {
		if (sec == currentTimer) {
			return;
		}
		currentTimer = sec;
		centralText.setText("" + sec);
	}

	private void updateBackgroundColor(int col) {
		if (currentBackground == col) {
			return;
		}
		currentBackground = col;
		mainLayout.setBackgroundColor(col);
	}

	private int countdown() {
		return countdownValues[countdownSelected];
	}

	private void cycleCountdownValue() {
		countdownSelected = (countdownSelected + 1) % countdownValues.length;
		if (!timerRunning) {
			updateTimerText(countdown());
		}
	}

	private Uri alarmUri() {

		Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
		if (alert == null) {
			// alert is null, using backup
			alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			if (alert == null) {  // I can't see this ever being null (as always have a default notification) but just incase
				// alert backup is null, using 2nd backup
				alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);               
			}
		}
		return alert;
	}

	@Override
	public boolean onKeyDown(int keycode, KeyEvent event ) {
		if (keycode == KeyEvent.KEYCODE_MENU && !timerRunning) {
			cycleCountdownValue();
			initTimer();
		} else if (keycode == KeyEvent.KEYCODE_SEARCH) {
			timer.cancel();
			onStopCounting();
		} else if (keycode == KeyEvent.KEYCODE_BACK && timerRunning) {
			// just don't pass to system
		} else {
			return super.onKeyDown(keycode, event);
		}
		return true;
	}


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		centralText = (TextView) findViewById(R.id.CentralText);
		mainLayout = (RelativeLayout) findViewById(R.id.MainLayout);
		mainLayout.setOnTouchListener(touchListener);

		updateTimerText(countdown());
		initTimer();

		audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
		player = new MediaPlayer();
		try {
			player.setDataSource(getApplicationContext(), alarmUri());
			player.setAudioStreamType(AudioManager.STREAM_ALARM);
			player.setLooping(false);
			player.prepare();
		} catch (IOException ex) {
			// ignore
		}

	}
	@Override
	protected void onStop() {
		timer.cancel();
		restartPlayer();
		onStopCounting();
		super.onStop();
	}

}
