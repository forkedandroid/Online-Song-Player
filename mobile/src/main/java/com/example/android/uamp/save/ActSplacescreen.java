package com.example.android.uamp.save;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;


public class ActSplacescreen extends Activity
{
	private ImageView imageView;
	private TextView tvChatsIn;
	private static int TIME = 300;

	SharedPreferences sharedpreferences;
	public static final String MyPREFERENCES = "GeetPrefs" ;
	public static final String Name = "folderPath";

	String myFolderPath = null;

	@SuppressLint("InlinedApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		try {
			try {
				requestWindowFeature(Window.FEATURE_NO_TITLE);
		        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		            WindowManager.LayoutParams.FLAG_FULLSCREEN);
			} catch (Exception e) {
				// TODO: handle exceptione.
				e.printStackTrace();
			}


/*
			setContentView(R.layout.act_splash_screen);
			System.out.println("=======come to ActSplacescreen========");
			imageView = (ImageView)findViewById(R.id.imgBack);
			tvChatsIn = (TextView)findViewById(R.id.textViewChatsIn);
            */

			try {

				//myFolderPath = createdFolderPath(getPackageName());
				System.out.println("=======finally created folder name is ====="+myFolderPath);

				sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
				SharedPreferences.Editor editor = sharedpreferences.edit();
				if(myFolderPath != null)
				{
					System.out.println("===myFolderPath=="+myFolderPath);
					editor.putString(Name, myFolderPath);
					editor.commit();
				}
				else
				{
					editor.putString(Name, "No");
					editor.commit();
				}
				
//				08-04 15:43:41.161: I/System.out(26147): =======finally created folder name is =====/storage/sdcard1/Android/data/com.geet.download

				 
				
				System.out.println("=====retrive data===");
				
				/*SharedPreferences prfs = getSharedPreferences("AUTHENTICATION_FILE_NAME", Context.MODE_PRIVATE);
				String Astatus = prfs.getString("Authentication_Status", "");*/
				
				//SharedPreferences prefs = getPreferences(MODE_PRIVATE);
				SharedPreferences prefs = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
				String retFolderName = prefs.getString(Name, null);
				
				if(retFolderName != null)
				{
					System.out.println("=====retrive folder name is======"+retFolderName);
				}
				else
				{
					System.out.println("=====retrive folder name is======"+"=======Null folder retrive====");
				}

			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}

			/*String mDirPath = "/mnt/sdcard1/Android/data/";
			 LinearLayout root = new LinearLayout(this);
			    //File file = new File(Environment.getExternalStorageDirectory().getPath());
			 File file = new File(mDirPath);
			*/ 
			 
			 /*
			 for the internal storage
			 String extStore = System.getenv("EXTERNAL_STORAGE");
			 File f_exts = new File(extStore);
			 
			 */
			 
			 String secStore = System.getenv("SECONDARY_STORAGE");
			 File file2 = new File(secStore+File.separator+ "GeetApp"+File.separator+"FullSongs"); // run creat folder main
			 File file3 = new File(secStore+File.separator+ "GeetApp"+File.separator+"Ringones"); // run creat folder main
			 System.out.println("==file2 path=="+file2.getAbsolutePath());
			 if(!file2.exists())
			 {
				 System.out.println("===create dir FullSongs====");
				 file2.mkdirs();
				 
			 }
			 else
			 {
				 System.out.println("=You can not write=already created FullSongs==");
			 }

			 if(!file3.exists())
			 {
				 System.out.println("===create dir Ringones====");
				 file3.mkdirs();
			 }
			 else
			 {
				 System.out.println("=You can not write=already created Ringones==");
			 }

/*			    if(file.isDirectory() == false)
			    {
			        Toast.makeText(this, "ERROR", Toast.LENGTH_SHORT).show();
			        return;
			    }
			    File[] files = file.listFiles();
			    int i = 1;
			    for(File f : files)
			    {
			        if(f.isFile() || f.isDirectory())
			        {
			            try
			            {
			                LinearLayout layout = new LinearLayout(this);
			                layout.setId(i);
			                    Button text = new Button(this);
			                    text.setText(f.getName());
			                    text.setMinWidth(400);
			                layout.addView(text);
			                root.addView(layout);
			                i++;
			            }
			            catch(Exception e){}
			        }
			    }
			    LinearLayout layout = new LinearLayout(this);
			    HorizontalScrollView scroll = new HorizontalScrollView(this);
			    scroll.addView(root);
			    layout.addView(scroll);
			    setContentView(layout);*/
			    
			    
			displaySplash();

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

	}


	private String createdFolderPath(String FolderName)
	{
		String dirPathMain = null;
		try {
			System.out.println("==create folder name is-->>==="+FolderName);
			
			
			if (Environment.getExternalStorageState().equals(
		            Environment.MEDIA_MOUNTED)) {
				System.out.println("=================createdFolderPath=====================");

		     //Check for the file
		     File appFolder = new File(Environment.getExternalStorageDirectory() + File.separator+ "GeetApp");
		
		     boolean exist = appFolder.exists();
		     
		     System.out.println("======appFolder==exist=="+exist);
		     
		     if(!appFolder.exists())
		     {
		    	 appFolder.mkdir();
		     }
		     System.out.println("====appFolder path =="+appFolder.getAbsolutePath());
		     System.out.println("======appFolder==exist=="+exist);
		}



			String dirPath = "/storage/sdcard1/Android/data/";

			System.out.println("===dirPath==="+dirPath);
			//File dir = new File(Environment.getExternalStorageDirectory().getPath()+"/"+getPackageName()+"/");
			File dir = new File(dirPath);
			if(dir.exists())
			{
				System.out.println("==exists andorid/data=Folder path is =="+dirPath);
				//File dir2 = new File(dirPath+FolderName+"/");
				File dir2 = new File(dirPath+FolderName);	
				if(!dir2.exists())
				{
					System.out.println("====make new dir==");
					dir2.mkdirs();
					dirPathMain = dir2.getAbsolutePath(); 
					System.out.println("====dirPathMain=dir create on sdcard1 ====="+dirPathMain);
					
					
					File f = new File(dirPath);
					File[] files = f.listFiles();
					for (File inFile : files) {
					    if (inFile.isDirectory()) {
					        // is directory
					    	
					    	System.out.println("=========is directory====");
					    	
					    	
					    }
					}
				}

				else
					System.out.println("===dir already created in sdcard1====");
			}
			else
			{
				System.out.println("== create folder in the internal storage sdcard==");
				String dirPath2 = "/storage/emulated/0/Android/data/";
				System.out.println("===dirPath2==="+dirPath2);
				//File dir = new File(Environment.getExternalStorageDirectory().getPath()+"/"+FolderName+"/");
				File dir2 = new File(dirPath2);
				if(dir2.exists())
				{
					System.out.println("===Folder path is =="+dirPath);
					//File dir22 = new File(dirPath2+FolderName+"/");
					File dir22 = new File(dirPath2+FolderName);		
					if(!dir22.exists()) {
						dir22.mkdirs();
						dirPathMain = dir22.getAbsolutePath();
					}
					else
						System.out.println("===dir already created in sdcard 1====");
				}
				else
				{
					System.out.println("=====create android data folder in the emulated -0 -android- data-===");
					dir2.mkdirs();
					if(dir2.exists())
					{
						System.out.println("===Folder path is =="+dirPath);
						//File dir22 = new File(dirPath2+FolderName+"/");
						File dir22 = new File(dirPath2+FolderName);		
						if(!dir22.exists())
						{
							dir22.mkdirs();
							dirPathMain = dir22.getAbsolutePath();
						}

						else
							System.out.println("===dir already created in sdcard 1====");
					}

				}
			}




		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

		return dirPathMain;
	}



	// splash screen set with timing
	private void displaySplash() {
		// TODO Auto-generated method stub b

		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				/*Intent iv = new Intent(ActSplacescreen.this, ActSoundCloudSearch.class);
				iv.putExtra("from", "splash");
				startActivity(iv);*/
				finish();
			}
		}, TIME);
	}
	
	
	// for maxico the radio chanhesl===http://streema.com/radios/play/38364
	// us- http://streema.com/radios/play/28046
}
