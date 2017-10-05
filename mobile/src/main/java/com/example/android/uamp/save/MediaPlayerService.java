package com.example.android.uamp.save;

import java.io.IOException;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

public class MediaPlayerService extends Service implements 
MediaPlayer.OnPreparedListener,
MediaPlayer.OnBufferingUpdateListener {

private MediaPlayer mMediaPlayer;
private WifiLock mWifiLock;

public int onStartCommand(Intent intent, int flags, int startId) {
    mMediaPlayer = new MediaPlayer();

    mMediaPlayer.setOnBufferingUpdateListener(this);
    mMediaPlayer.setOnPreparedListener(this);

    mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    try {
    	// radio url - http://streema.com/radios/play/226
		mMediaPlayer.setDataSource("http://streema.com/radios/play/98453");
	} catch (IllegalArgumentException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (SecurityException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (IllegalStateException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

    // Acquire CPU lock and wi-fi lock

    mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
    mWifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE))
         .createWifiLock(WifiManager.WIFI_MODE_FULL, "Media Player Wi-Fi Lock");
    mWifiLock.acquire();

    mMediaPlayer.prepareAsync();

    return START_STICKY;
}

@Override
public void onPrepared(MediaPlayer mediaPlayer) {
    mMediaPlayer.start();
}

@Override
public void onBufferingUpdate(MediaPlayer mp, int percent) {
    Log.d("Data","Buffered " + percent);
}

@Override
public IBinder onBind(Intent intent) {
	// TODO Auto-generated method stub
	return null;
}
}