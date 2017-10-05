package com.example.android.uamp.save;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.uamp.R;
import com.example.android.uamp.ui.BaseActivity;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

public class ActFileDownloader extends BaseActivity
{
	LinearLayout llNewDownload,llDownloadList;
	ImageView ivDownload;
	EditText etDownloadLink;
	String strDownloadUrl;
	ListView lvDownloadedFiles;
	String[] values = null; 
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		/*try {*//*
			requestWindowFeature(Window.FEATURE_NO_TITLE);
	        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
	            WindowManager.LayoutParams.FLAG_FULLSCREEN);*//*
			requestWindowFeature(Window.FEATURE_NO_TITLE);
		} catch (Exception e) {
			// TODO: handle exceptione.
			e.printStackTrace();
		}*/
		
		setContentView(R.layout.act_dowlnload_list);
		initializeToolbar();
		llNewDownload = (LinearLayout)findViewById(R.id.llNewDownload);
		llDownloadList = (LinearLayout)findViewById(R.id.llDownloadList);

		etDownloadLink = (EditText)findViewById(R.id.etDownloadLink);
		ivDownload = (ImageView)findViewById(R.id.ivDownload);

		lvDownloadedFiles = (ListView)findViewById(R.id.lvDownloadedFiles);
		ivDownload.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				strDownloadUrl = etDownloadLink.getText().toString();

				if(isValidURL(strDownloadUrl) == true)
				{
					//http://www.smeconnect.in/issues/VOL4_ISSUE18.pdf
					String fileName = strDownloadUrl.substring( strDownloadUrl.lastIndexOf('/')+1, strDownloadUrl.length() );
					//	String fileNameWithoutExtn = fileName.substring(0, fileName.lastIndexOf('.'));
					if(fileName !=null && fileName.length()>0)
					{					
						MyDownloader downloader = new MyDownloader();
						downloader.Download(ActFileDownloader.this, strDownloadUrl, fileName);
					}
					else
					{
						Toast.makeText(ActFileDownloader.this, "Some thing went wrong please try with another download link...!", Toast.LENGTH_SHORT).show();
					}
				}
				else
				{
					Toast.makeText(ActFileDownloader.this, "Please enter currect download url for eg.http://www.smyc.in/isys/VOL4.pdf", Toast.LENGTH_SHORT).show();
				}
			}
		});



		try {

			String FolderName = "MyGeet";

			String dirPath = "/storage/sdcard1/Android/data/";
			File dir = new File(dirPath);
			final File appDir;
			if(dir.exists())
			{
				String secStore = System.getenv("SECONDARY_STORAGE");
				appDir = new File(secStore+File.separator+ FolderName);

			}
			else
			{
				appDir = new File(Environment.getExternalStorageDirectory()+File.separator+FolderName);

			}


			if(appDir !=null)
			{
				File f = new File(appDir.getAbsolutePath());    			
				File file[] = f.listFiles();
				
				Log.d("---Files---", "Size: "+ file.length);
				values = new String[file.length];
				
				for (int i=0; i < file.length; i++)
				{
					Log.d("Files-->>"+i, "  ===>>>FileName:==>>" + file[i].getName());
					
					values[i] = file[i].getName();
				}


				if(values !=null)
				{
					ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.textview, android.R.id.text1, values);
					lvDownloadedFiles.setAdapter(adapter);
				}
			}
			
			

			
			lvDownloadedFiles.setOnItemClickListener(new OnItemClickListener() 
	        {
	            public void onItemClick(AdapterView<?> parent, View view, int position, long i) 
	            {
	            	Toast.makeText(ActFileDownloader.this,"ItemClickListener",Toast.LENGTH_SHORT).show();
	            	
	            	System.out.println("==file path is =="+appDir.getAbsolutePath()+values[position]);
	            	
	            	
	            	String strFilePath = appDir.getAbsolutePath()+values[position];
	            	File filestrFilePath = new File(appDir.getAbsolutePath()+File.separator+values[position]); 
	            	
	            	MimeTypeMap myMime = MimeTypeMap.getSingleton();
	            	Intent newIntent = new Intent(Intent.ACTION_VIEW);
	            	String mimeType = myMime.getMimeTypeFromExtension(fileExt(strFilePath).substring(1));
	            	
	            	System.out.println("==filestrFilePath=="+filestrFilePath);
	            	
	            	newIntent.setDataAndType(Uri.parse("file://"+ filestrFilePath.getAbsolutePath()),mimeType);

	            	try {
	            	    startActivity(newIntent);
	            	} catch (ActivityNotFoundException e) {
	            	    Toast.makeText(ActFileDownloader.this, "No handler for this type of file.", Toast.LENGTH_LONG).show();
	            	}
	            	catch (Exception e) {
						// TODO: handle exception
	            		System.out.println("=============Myexp=====");
	            		e.printStackTrace();
					}
	            	

	            	
	            	
					
	            }
	        });


			

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}


	}
	private String fileExt(String url) {
	    if (url.indexOf("?") > -1) {
	        url = url.substring(0, url.indexOf("?"));
	    }
	    if (url.lastIndexOf(".") == -1) {
	        return null;
	    } else {
	        String ext = url.substring(url.lastIndexOf("."));
	        if (ext.indexOf("%") > -1) {
	            ext = ext.substring(0, ext.indexOf("%"));
	        }
	        if (ext.indexOf("/") > -1) {
	            ext = ext.substring(0, ext.indexOf("/"));
	        }
	        return ext.toLowerCase();

	    }
	}
	public boolean isValidURL(String url) {  

		URL u = null;

		try {  
			u = new URL(url);  
		} catch (MalformedURLException e) {  
			return false;  
		}

		try {  
			u.toURI();  
		} catch (URISyntaxException e) {  
			return false;  
		}  

		return true;  
	} 

}
