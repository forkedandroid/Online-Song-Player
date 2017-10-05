package com.example.android.uamp.save;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class MyDownloader 
{
	URL url;
	String FolderName = "MyGeet";

	public void Download(final Context context,String urlPath,final String fileName)
	{
		System.out.println("===Download_barProgressDialog2===");


		String urlStr = urlPath; // obj.stream_url+"?client_id=b368cb306911b579a39e8d5050f62457";
		//"http://yumvideo.com/English%20Songs/Good-For-You-(English-Song)/Good-For-You.mp4";

		try {
			url = new URL(urlStr);
			URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
			url = uri.toURL();

		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			System.out.println("==MalformedURLException==");
			e1.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			System.out.println("==URISyntaxException==");
			e.printStackTrace();
		}

		try {
			System.out.println("===Download song111===");
		
			 


			String dirPath = "/storage/sdcard1/Android/data/";
			File dir = new File(dirPath);
			final File appDir;
			if(dir.exists())
			{
				String secStore = System.getenv("SECONDARY_STORAGE");
				appDir = new File(secStore+File.separator+ FolderName);
				if(!appDir.exists()) 
				{
					// create empty directory
					if (appDir.mkdirs())
					{   
						System.out.println("=sdcard1 folder created="+FolderName);
					}
					else
					{
						System.out.println("=sdcard1 unable to create folder="+FolderName);
					}
				}
				else
				{
					System.out.println("=sdcard1 folder already exist="+FolderName);
				}
				
			}
			else
			{

				appDir = new File(Environment.getExternalStorageDirectory()+File.separator+FolderName);
				if(!appDir.exists() ) 
				{
					// create empty directory
					if (appDir.mkdirs())
					{   
						System.out.println("=sdcard folder created="+FolderName);
					}
					else
					{
						System.out.println("=sdcard unable to create folder="+FolderName);
					}
				}
				else
				{
					System.out.println("=sdcard folder already exist="+FolderName);
				}
			}

			((Activity) context).runOnUiThread(new Runnable() {
				public void run() {
					System.out.println("==running=");
					System.out.println("==folder path is ### ="+appDir.getAbsolutePath());

					MeDownlManage downlManage = new MeDownlManage();
					MeDownlManage.createDownload(url, appDir.getAbsolutePath()+"/", 5 ,fileName,context);
				}
			});

			System.out.println("===run 2==");		
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("====Downloader error==");
			e.printStackTrace();
		}
	}


}
