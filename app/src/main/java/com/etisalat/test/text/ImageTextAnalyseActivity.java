/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.etisalat.test.text;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.etisalat.test.R;
import com.huawei.hms.mlsdk.common.LensEngine;
import com.huawei.hms.mlsdk.common.MLAnalyzer;
import com.huawei.hms.mlsdk.text.MLText;
import com.huawei.hms.mlsdk.text.MLTextAnalyzer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ImageTextAnalyseActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = ImageTextAnalyseActivity.class.getSimpleName();

    private TextView mTextView;

    private MLTextAnalyzer analyzer;
    private static final int CAMERA_PERMISSION_CODE = 1;

    //private SurfaceView cameraView;
    private LensEngine lensEngine;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_image_text_analyse);
        this.mTextView = this.findViewById(R.id.text_result);
        this.findViewById(R.id.text_detect).setOnClickListener(this);
        //cameraView = (SurfaceView) findViewById(R.id.surface_view);

        // Check whether the app has the camera permission.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED) {
            // The app has the camera permission.
        } else {
            // Apply for the camera permission.
            requestCameraPermission();
        }
    }

    private void requestCameraPermission() {
        final String[] permissions = new String[]{Manifest.permission.CAMERA};
        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, CAMERA_PERMISSION_CODE);
            return;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != CAMERA_PERMISSION_CODE) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }
        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // The camera permission is granted.
        }
    }

    @Override
    public void onClick(View v) {
        this.scanAnalyzer();
    }

    private void scanAnalyzer() {
        analyzer = new MLTextAnalyzer.Factory(getApplicationContext())
                .setLanguage("en")
                .create();

        analyzer.setTransactor(new OcrDetectorProcessor());

        lensEngine = new LensEngine.Creator(getApplicationContext(), analyzer)
                .setLensType(LensEngine.BACK_LENS)
                .applyDisplayDimension(1440, 1080)
                .applyFps(30.0f)
                .enableAutomaticFocus(true)
                .create();

        SurfaceView mSurfaceView = findViewById(R.id.surface_view);
        try {
            lensEngine.run(mSurfaceView.getHolder());
        } catch (IOException e) {
            // Exception handling logic.
            Log.e(ImageTextAnalyseActivity.TAG, "IOException: " + e.getMessage());
            Toast.makeText(this, "IOException:" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (this.analyzer == null) {
            return;
        }
        try {
            this.analyzer.stop();
        } catch (IOException e) {
            Log.e(ImageTextAnalyseActivity.TAG, "Stop failed: " + e.getMessage());
            Toast.makeText(this, "OnDestroy: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        if (lensEngine != null) {
            lensEngine.release();
        }
    }

    public class OcrDetectorProcessor implements MLAnalyzer.MLTransactor<MLText.Block> {
        @Override
        public void transactResult(MLAnalyzer.Result<MLText.Block> results) {
            SparseArray<MLText.Block> items = results.getAnalyseList();
            // Determine detection result processing as required.
            // Note that only the detection results are processed.
            // Other detection-related APIs provided by ML Kit cannot be called.

            if (items.size() != 0) {
                String result = "";
                //List<MLText.Block> blocks = convertSparseArrayToArrayList(items);
                for (MLText.Block block : convertSparseArrayToArrayList(items)) {
                    for (MLText.TextLine line : block.getContents()) {
                        result += line.getStringValue() + "\n";
                    }
                }
                mTextView.setText(result);
            }
        }

        @Override
        public void destroy() {
            // Callback method used to release resources when the detection ends.
            //lensEngine.close();
            onDestroy();
        }

        private List<MLText.Block> convertSparseArrayToArrayList(SparseArray<MLText.Block> items){
            if (items == null) {
                return null;
            }else {
                List<MLText.Block> arrayList = new ArrayList<MLText.Block>(items.size());
                for (int i = 0; i < items.size(); i++){
                    arrayList.add(items.valueAt(i));
                }
                return arrayList;
            }
        }
    }
}
