package com.example.suraj.torch;

import android.app.IntentService;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.util.Log;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class MyIntentService extends IntentService {


    Boolean api23;
    CameraManager cameraManager;
    String cameraId;
    Camera camera;
    static int seekBarValue;
    static boolean finish = false;
    static Thread thread;
    public static final String TAG = "Service";

    public MyIntentService() {
        super("MyIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {


        Log.d(TAG, "run: ");

        api23 = intent.getBooleanExtra("API23",false);

        if(api23) {
            Log.d(TAG, "run: inside api23");

            cameraManager = getApplicationContext().getSystemService(CameraManager.class);
            cameraId = intent.getStringExtra("CAMERA_ID");

            Log.d(TAG, "run: befor setTorchMode");

            seekBarValue = intent.getIntExtra("VALUE",1);
            boolean value = true;
            while(!finish){

                thread = Thread.currentThread();

                try {
                    cameraManager.setTorchMode(cameraId,value);
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }

                value = !value;
                try {
                    Log.d(TAG, "onHandleIntent: "+value+" "+seekBarValue);
                    Thread.sleep(seekBarValue*100);
                } catch (InterruptedException e) {
                    //e.printStackTrace();
                }

            }
            finish=false;

        }else{

            camera = Camera.open();
            Camera.Parameters parameters = camera.getParameters();
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            camera.setParameters(parameters);
            camera.startPreview();

        }

    }

    @Override
    public void onDestroy() {

        Log.d(TAG, "onDestroy: ");
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

}
