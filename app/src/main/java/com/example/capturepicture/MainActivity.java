package com.example.capturepicture;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraControl;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.core.impl.PreviewConfig;
import androidx.camera.core.internal.ThreadConfig;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.Rational;
import android.util.Size;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {


    private final int REQUEST_CODE = 100;

    private String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private Camera camera;

    private int state = 0;
    private int cam_state = 0;

    int lens = CameraSelector.LENS_FACING_BACK;
    private ListenableFuture<ProcessCameraProvider> cameraProviderListenableFuture;
    private Executor cameraExecutor = Executors.newSingleThreadExecutor();
    private ImageButton imageButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        permission();
        imageButton = findViewById(R.id.captureButton);

    }

    private void startCamera() {

        cameraProviderListenableFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderListenableFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderListenableFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {

            }
        }, ContextCompat.getMainExecutor(this));

    }

    private void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {

        cameraProvider.unbindAll();
        PreviewView previewView = findViewById(R.id.preview);
        Preview preview = new Preview.Builder().build();

        CameraSelector cameraSelector = new CameraSelector.Builder().
                requireLensFacing(lens)
                .build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        ImageCapture imageCapture = new ImageCapture.Builder().
                setTargetRotation(this.getWindowManager().getDefaultDisplay().getRotation())
                .build();

        camera = cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector,
                imageCapture, preview);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraCapture(v,imageCapture);
            }
        });






    }


    //Handles Permission
    private void permission() {
        if (allpermissionsGranted()) {
            startCamera();
            Log.d("Error", "Working");
        } else {
            ActivityCompat.requestPermissions(MainActivity.this,
                    REQUIRED_PERMISSIONS, REQUEST_CODE);
        }

    }

    //Checks for Permission
    private boolean allpermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(getBaseContext(), permission) !=
                    PackageManager.PERMISSION_GRANTED) {
                return false;
            } else {
                return true;
            }
        }

        return false;
    }


    public void turnFlash(View view) {
        if (state == 0) {
            camera.getCameraControl().enableTorch(true);
            state = 1;
        } else {
            camera.getCameraControl().enableTorch(false);
            state = 0;
        }
    }

    public void turnCamera(View view) {

        if (cam_state == 0) {
            lens = CameraSelector.LENS_FACING_FRONT;
            cam_state = 1;
            try {
                bindPreview(cameraProviderListenableFuture.get());
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            lens = CameraSelector.LENS_FACING_BACK;
            cam_state = 0;
            try {
                bindPreview(cameraProviderListenableFuture.get());
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void cameraCapture(View view, ImageCapture imageCapture){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);


        Date date = new Date();
        File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),dateFormat.format(date)+".jpg");

        ImageCapture.OutputFileOptions outputFileOptions =
                new ImageCapture.OutputFileOptions.Builder(file).build();

        imageCapture.takePicture(outputFileOptions, cameraExecutor,
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                            //
                        // Toast.makeText(MainActivity.this,"Saved Successfully", Toast.LENGTH_SHORT).show();


                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {

                    }
                });
    }


    public String getDirectory(){
        String file_path = "";
        file_path=Environment.DIRECTORY_PICTURES;


        return file_path;
    }

}


