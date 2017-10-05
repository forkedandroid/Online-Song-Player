package com.example.android.uamp.save;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat.Builder;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Random;




public abstract class MeDownl extends Observable implements Runnable{
	
	// Member variables
	/** The URL to download the file */
	protected URL mURL;
	
	/** Output folder for downloaded file */
	protected String mOutputFolder;
	
	/** Number of connections (threads) to download the file */
	protected int mNumConnections;
	
	/** The file name, extracted from URL */
	protected String mFileName;
	
	/** Size of the downloaded file (in bytes) */
	protected int mFileSize;
	
	/** The state of the download */
	protected int mState;
	
	/** downloaded size of the file (in bytes) */
	protected int mDownloaded;
	
	/** List of download threads */
	protected ArrayList<DownloadThread> mListDownloadThread;
	
	// Contants for block and buffer size
	protected static final int BLOCK_SIZE = 4096;
	protected static final int BUFFER_SIZE = 4096;
	protected static final int MIN_DOWNLOAD_SIZE = BLOCK_SIZE * 100;
	
	// These are the status names.
    public static final String STATUSES[] = {"Downloading",
    				"Paused", "Complete", "Cancelled", "Error"};
	
	// Contants for download's state
	public static final int DOWNLOADING = 0;
	public static final int PAUSED = 1;
	public static final int COMPLETED = 2;
	public static final int CANCELLED = 3;
	public static final int ERROR = 4;
	
	private NotificationManager mNotifyManager;
	private Builder mBuilder;
	int id = 1;
	float fltProcess = 0;
	String strDownlStart = "Download in progress.....";
	String strDownlComplete = "Download complete";
	
	Context mContext;
	
	/**
	 * Constructor
	 * @param outputFolder
	 * @param numConnections
	 */
	protected MeDownl(URL url, String outputFolder, int numConnections,String filename,Context context) {
		mContext = context;
		mURL = url;
		mOutputFolder = outputFolder;
		mNumConnections = numConnections;
		
		// Get the file name from url path
		String fileURL = url.getFile();
		mFileName =filename;
				//fileURL.substring(fileURL.lastIndexOf('/') + 1);
		System.out.println("File name: " + mFileName);
		mFileSize = -1;
		mState = DOWNLOADING;
		mDownloaded = 0;
		System.out.println("======DownloadThread==staring...111====");
		File file = new File(mOutputFolder);
		if(file.exists())
		{
			System.out.println("==folder exist=");
		}
		else
		{
			System.out.println("==folder not exist=");
		}
		mListDownloadThread = new ArrayList<DownloadThread>();
		
		
	}
	
	/**
	 * Pause the downloader
	 */
	public void pause() {
		setState(PAUSED);
	}
	
	/**
	 * Resume the downloader
	 */
	public void resume() {
		setState(DOWNLOADING);
		download();
	}
	
	/**
	 * Cancel the downloader
	 */
	public void cancel() {
		setState(CANCELLED);
	}
	
	/**
	 * Get the URL (in String)
	 */
	public String getURL() {
		return mURL.toString();
	}
	
	/**
	 * Get the downloaded file's size
	 */
	public int getFileSize() {
		return mFileSize;
	}
	
	/**
	 * Get the current progress of the download
	 */
	public float getProgress() {
		System.out.println("===getProgress==="+((float)mDownloaded / mFileSize )*100);
		return ((float)mDownloaded / mFileSize) * 100;
	}
	
	/**
	 * Get current state of the downloader
	 */
	public int getState() {
		return mState;
	}
	
	/**
	 * Set the state of the downloader
	 */
	protected void setState(int value) {
		mState = value;
		stateChanged();
	}
	
	/**
	 * Start or resume download
	 */
	protected void download() {
		System.out.println("====download start111=========");
		
		mNotifyManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		mBuilder = new Builder(mContext);
		mBuilder.setContentTitle(mFileName)
				.setContentText(strDownlStart)
				.setSmallIcon(android.R.drawable.ic_popup_sync);
		
		// Displays the progress bar for the first time.
					mBuilder.setProgress(100, 1, false);
					 id = (int) (System.currentTimeMillis() / 1000L);
					mNotifyManager.notify(id, mBuilder.build());

					
		Thread t = new Thread(this);
		t.start();
	}
	
	/**z
	 * Increase the downloaded size
	 */
	@SuppressLint("NewApi")
	protected synchronized void downloaded(int value) {
		mDownloaded += value;
		System.out.println("======downloaded======="+mDownloaded);
		stateChanged();
		
		fltProcess = (((float)mDownloaded / mFileSize )*100 );
		System.out.println("===getProgress==="+fltProcess);
		
		mBuilder.setProgress(100, (int) fltProcess, false);
		mNotifyManager.notify(id, mBuilder.build());
		
		if(fltProcess >= 99)
		{
			mBuilder.setContentText("Download complete");
			// Removes the progress bar
			mBuilder.setProgress(0, 0, false);
		//	mNotifyManager.notify(id, mBuilder.build());
			
			//Toast.makeText(mContext, "Download completed \n"+mOutputFolder, 200).show();
			
			
			
			Intent intent2 = new Intent(mContext,ActFileDownloader.class);
			intent2.putExtra("from", "MeDownl");
			intent2.putExtra("play_path", mOutputFolder+mFileName);
			System.out.println("==play_path is="+mOutputFolder+mFileName);
			//--intent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
			
			TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
			// Adds the back stack
			//--stackBuilder.addParentStack(ActSplacescreen.class);
			// Adds the Intent to the top of the stack
			//stackBuilder.addNextIntent(notificationIntent);
			stackBuilder.addNextIntent(intent2);
			
			Random randomGenerator = new Random();
			int randomInt = randomGenerator.nextInt(1000);		
			PendingIntent intent = stackBuilder.getPendingIntent(randomInt, PendingIntent.FLAG_UPDATE_CURRENT);
			Notification notification = new Builder(mContext).setContentTitle(strDownlComplete).setContentIntent(intent).setSmallIcon(android.R.drawable.ic_media_play).setContentTitle(mFileName).setContentText(strDownlComplete).setPriority(Notification.PRIORITY_MAX).build();
			//notification.setLatestEventInfo(mContext, mFileName, strDownlComplete, intent);
            mBuilder.setContentTitle(strDownlComplete);
            mBuilder.setContentIntent(intent);
            mBuilder.setSmallIcon(android.R.drawable.ic_media_play);


            notification.flags |= Notification.FLAG_AUTO_CANCEL;
			// Play default notification sound
			notification.defaults |= Notification.DEFAULT_SOUND;
			// Vibrate if vibrate is enabled
			notification.defaults |= Notification.DEFAULT_VIBRATE;
			//final NotificationManager notificationManager = (NotificationManager)mContext.getSystemService("notification");
			
			
			System.out.println("==noteId==="+id);
		//--	mNotifyManager.notify(id, notification);

            mNotifyManager.notify(id, mBuilder.build());
			
		}
		
	}
	
	/**
	 * Set the state has changed and notify the observers
	 */
	protected void stateChanged() {
		setChanged();
		notifyObservers();
	}
	
	/**
	 * Thread to download part of a file
	 */
	protected abstract class DownloadThread implements Runnable {
		protected int mThreadID;
		protected URL mURL;
		protected String mOutputFile;
		protected int mStartByte;
		protected int mEndByte;
		protected boolean mIsFinished;
		protected Thread mThread;
		
		public DownloadThread(int threadID, URL url, String outputFile, int startByte, int endByte) {
			mThreadID = threadID;
			mURL = url;
			mOutputFile = outputFile;
			mStartByte = startByte;
			mEndByte = endByte;
			mIsFinished = false;
			System.out.println("======DownloadThread====");
			
			download();
		}
		
		/**
		 * Get whether the thread is finished download the part of file
		 */
		public boolean isFinished() {
			return mIsFinished;
		}
		
		/**
		 * Start or resume the download
		 */
		public void download() {
			System.out.println("====download start222=========");
			mThread = new Thread(this);
			mThread.start();
		}
		
		/**
		 * Waiting for the thread to finish
		 * @throws InterruptedException
		 */
		public void waitFinish() throws InterruptedException {
			mThread.join();			
		}
		
	}
}
