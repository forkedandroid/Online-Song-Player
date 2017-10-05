package com.example.android.uamp.save;

import android.content.Context;

import java.net.URL;
import java.util.ArrayList;

public class MeDownlManage {
	
	// The unique instance of this class
	private static MeDownlManage sInstance = null;
	
	// Constant variables
	private static final int DEFAULT_NUM_CONN_PER_DOWNLOAD = 8;
	public static final String DEFAULT_OUTPUT_FOLDER = "";

	// Member variables
	private static int mNumConnPerDownload;
	private static ArrayList<MeDownl> mDownloadList;
	
	/** Protected constructor */
	public MeDownlManage() {
		mNumConnPerDownload = DEFAULT_NUM_CONN_PER_DOWNLOAD;
		if(mDownloadList !=null)
		{
			System.out.println("=already created array list mDownloadList=");
			
		}
		else
		{
			System.out.println("==create new array list mDownloadList=");
			mDownloadList = new ArrayList<MeDownl>();
		}
			
	}
	
	/**
	 * Get the max. number of connections per download
	 */
	public int getNumConnPerDownload() {
		return mNumConnPerDownload;
	}
	
	/**
	 * Set the max number of connections per download
	 */
	public void SetNumConnPerDownload(int value) {
		mNumConnPerDownload = value;
	}
	
	/**
	 * Get the downloader object in the list
	 * @param index
	 * @return
	 */
	public MeDownl getDownload(int index) {
		return mDownloadList.get(index);
	}
	
	public void removeDownload(int index) {
		mDownloadList.remove(index);
	}
	
	/**
	 * Get the download list
	 * @return
	 */
	public ArrayList<MeDownl> getDownloadList() {
		return mDownloadList;
	}
	
	
	public static MeDownl createDownload(URL verifiedURL, String outputFolder,int connections,String filename,Context context) {
		mNumConnPerDownload = connections;
		MeDownlServer fd = new MeDownlServer(verifiedURL, outputFolder, mNumConnPerDownload,filename,context);
		mDownloadList.add(fd);
		
		return fd;
	}
	
	/**
	 * Get the unique instance of this class
	 * @return the instance of this class
	 */
	public static MeDownlManage getInstance() {
		if (sInstance == null)
			sInstance = new MeDownlManage();
		
		return sInstance;
	}
	
	/**
	 * Verify whether an URL is valid
	 * @param fileURL
	 * @return the verified URL, null if invalid
	 */
	public static URL verifyURL(String fileURL) {
		// Only allow HTTP URLs.
        if (!fileURL.toLowerCase().startsWith("http://") || !fileURL.toLowerCase().startsWith("https://"))
            return null;
        
        // Verify format of URL.
        URL verifiedUrl = null;
        try {
            verifiedUrl = new URL(fileURL);
        } catch (Exception e) {
            return null;
        }
        
        // Make sure URL specifies a file.
        if (verifiedUrl.getFile().length() < 2)
            return null;
        
        return verifiedUrl;
	}

}
