package com.example.capturepicture;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
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
import android.util.Log;
import android.util.Rational;
import android.util.Size;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {



    private final int REQUEST_CODE = 100;

    private String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA};

    Camera camera;

    private int state=0;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        permission();

    }
    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderListenableFuture;
        cameraProviderListenableFuture=ProcessCameraProvider.getInstance(this);
        cameraProviderListenableFuture.addListener(()->{
            try{
                ProcessCameraProvider cameraProvider= cameraProviderListenableFuture.get();
                bindPreview(cameraProvider);
            }catch (ExecutionException|InterruptedException e){

            }
        },ContextCompat.getMainExecutor(this));

    }

   private void bindPreview(@NonNull ProcessCameraProvider cameraProvider){
       PreviewView previewView = findViewById(R.id.preview);
       Preview preview= new Preview.Builder().build();

       CameraSelector cameraSelector = new CameraSelector.Builder().
               requireLensFacing(CameraSelector.LENS_FACING_BACK)
               .build();

        preview.setSurfaceProvider( previewView.createSurfaceProvider());



       ImageCapture imageCapture = new ImageCapture.Builder().
               setTargetRotation(this.getWindowManager().getDefaultDisplay().getRotation())
           .build();
       camera= cameraProvider.bindToLifecycle( (LifecycleOwner)this, cameraSelector,
               imageCapture , preview);




    }






    //Handles Permission
    private void permission(){
        if(allpermissionsGranted()){
            startCamera();
            Log.d("Error", "Working");
        }
        else{
            ActivityCompat.requestPermissions(MainActivity.this,
                    REQUIRED_PERMISSIONS,REQUEST_CODE);
        }

    }

    //Checks for Permission
    private boolean allpermissionsGranted(){
        for(String permission:REQUIRED_PERMISSIONS){
            if(ContextCompat.checkSelfPermission(getBaseContext(),permission)!=
                    PackageManager.PERMISSION_GRANTED){
                return false;
            }
            else{
                return true;
            }
        }

        return false;
    }



    public void turnFlash(View view) {
        if(state==0){
            camera.getCameraControl().enableTorch(true);
            state=1;
        }
        else {
            camera.getCameraControl().enableTorch(false);
            state=0;
        }
    }
}


