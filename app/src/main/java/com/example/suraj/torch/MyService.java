package com.example.suraj.torch;

import android.app.Service;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyService extends Service {

    Boolean api23;
    CameraManager cameraManager;
    String cameraId;
    Camera camera;
    Thread thread;
    Handler handler;
    Runnable runnable;
    public static final String TAG = "Service";
    public MyService() {
        Log.d(TAG, "MyService: ");
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        final Intent intent1 = intent;

        Log.d(TAG, "onStartCommand: ");


         runnable = new Runnable() {

            @Override
            public void run() {

                    Log.d(TAG, "run: ");

                    api23 = intent1.getBooleanExtra("API23",false);

                    if(api23) {
                        Log.d(TAG, "run: inside api23");

                        cameraManager = getApplicationContext().getSystemService(CameraManager.class);
                        cameraId = intent1.getStringExtra("CAMERA_ID");

                        Log.d(TAG, "run: befor setTorchMode");

                        int seekBarValue = intent1.getIntExtra("VALUE",1);
                        boolean value = true;
                        while(true){

                            if(thread.isInterrupted())
                            {
                                break;
                            }
                            try {
                                cameraManager.setTorchMode(cameraId,value);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }

                            value = !value;
                            try {
                                Log.d(TAG, "run: before sleep "+value+" "+seekBarValue);
                                Thread.sleep(seekBarValue*1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                        }

//                    int i =0;
//                    boolean value = true;
//                    while(i<20){
//                        try {
//                            cameraManager.setTorchMode(cameraId,value);
//                        } catch (CameraAccessException e) {
//                            e.printStackTrace();
//                        }
//                        value = !value;
//                        try {
//                            Thread.sleep(500);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                    }

                    }else{

                        camera = Camera.open();
                        Camera.Parameters parameters = camera.getParameters();
                        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                        camera.setParameters(parameters);
                        camera.startPreview();

                    }
                }

         };

        if(thread!= null){
            thread.interrupt();
        }
        thread = new Thread(runnable,"work");
        thread.start();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {

    //    thread.interrupt();
        if(api23){

            try {
                cameraManager.setTorchMode(cameraId,false);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }

        }else{
            camera.stopPreview();
            camera.release();
        }

        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
