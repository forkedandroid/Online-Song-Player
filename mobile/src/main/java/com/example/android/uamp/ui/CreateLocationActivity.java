/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.uamp.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.android.uamp.R;
import com.example.android.uamp.utils.LogHelper;

import java.io.File;

/**
 * Main activity for the music player.
 * This class hold the MediaBrowser and the MediaController instances. It will create a MediaBrowser
 * when it is created and connect/disconnect on start/stop. Thus, a MediaBrowser will be always
 * connected while this activity is running.
 */
public class CreateLocationActivity extends Activity
   {


    private Bundle mVoiceSearchParams;

    private Button btnCreate;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogHelper.d("1", "Activity onCreate");

        setContentView(R.layout.activity_create);
        btnCreate = (Button)findViewById(R.id.btnCreate);

        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createFolderFile();
            }
        });


    }
    private void createFolderFile()
    {
        try
        {

            File file = new File(
                    "/storage/extSdCard/Android/data/com.example.android.uamp/files",
                    "DemoPicture.jpg");
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                System.out.println("Created the File---------------"
                        + file.createNewFile());
            } else {
                System.out.println("Created the File---------------"
                        + file.createNewFile());
            }











            /*String dirName = "MyApp.png";
            File file = new File (getObbDir(), dirName);
            System.out.print("====getObbDir()====="+getObbDir().getAbsolutePath());

            if (!file.exists()) {
                boolean status = file.mkdirs();
                if (status) {
                    Toast.makeText(CreateLocationActivity.this, "Directory created successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(CreateLocationActivity.this, "Directory create failed", Toast.LENGTH_SHORT).show();
                }
            }*/
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }


    }


}
