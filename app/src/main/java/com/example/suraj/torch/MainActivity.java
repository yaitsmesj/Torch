package com.example.suraj.torch;

import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;

import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ImageButton imageButton;
    FloatingActionButton fab;
    SeekBar seekBar;
    AlertDialog alertDialog;

    SharedPreferences preferences;

    Boolean api23;
    Boolean torch = false;

    int seekBarValue = 1;
    Intent intent = null;
    public static final String TAG = "Service";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageButton = (ImageButton) findViewById(R.id.imageButton);
        seekBar = (SeekBar)findViewById(R.id.seekBar);
        fab = (FloatingActionButton)findViewById(R.id.fab_exit);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                Log.d(TAG, "onProgressChanged: "+i);

                seekBarValue = i;
               if(torch){
                    MyIntentService.seekBarValue = i;
                 }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                MyIntentService.finish = true;
                if(MyIntentService.thread!=null) {
                    MyIntentService.thread.interrupt();
                }
                torch = false;
                finish();

            }
        });

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!torch) {
                    decideVersion();
                    Snackbar snackbar = Snackbar.make(view,"Flash On",Snackbar.LENGTH_SHORT);
                    snackbar.show();

                }else{
                    if(MyIntentService.thread!=null) {
                        MyIntentService.thread.interrupt();
                        MyIntentService.finish = true;

                        Snackbar snackbar = Snackbar.make(view,"Flash Off",Snackbar.LENGTH_SHORT);
                        snackbar.show();
                    }
                }
                torch=!torch;

                }
        });



    }


    @Override
    protected void onStop() {

//        Log.d(TAG, "onStop: ");
//        Bundle bundle = new Bundle();
//        bundle.putInt("SEEK_BAR",seekBarValue);
//        bundle.putBoolean("TORCH",torch);
//        getIntent().putExtras(bundle);

        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("TORCH",torch);
        editor.putInt("SEEK_BAR",seekBarValue);
        editor.apply();

        Log.d(TAG, "onStop: ");
        super.onStop();

    }

    @Override
    protected void onResume() {
        super.onResume();

        preferences = getPreferences(MODE_PRIVATE);
        torch = preferences.getBoolean("TORCH",false);
        seekBarValue = preferences.getInt("SEEK_BAR",1);

        seekBar.setProgress(seekBarValue);
      //  seekBar.setMin(1);
//        Log.d(TAG, "onResume: ");
//        Bundle bundle = getIntent().getExtras();
//        if(bundle!=null) {
//            torch = bundle.getBoolean("TORCH");
//            seekBarValue = bundle.getInt("SEEK_BAR");
//        }
        Log.d(TAG, "onResume: ");
    }

    public void newApiFlash(){

        CameraManager cameraManager = getApplicationContext().getSystemService(CameraManager.class);
        CameraCharacteristics cameraCharacteristics = null;

        Log.d(TAG, "newApiFlash: ");

            String[] cameraList = new String[0];
            String backCameraId = null;

            Log.d(TAG, "newApiFlash: inside false");
            try {
                cameraList = cameraManager.getCameraIdList();
                for(String id : cameraList){
                    cameraCharacteristics = cameraManager.getCameraCharacteristics(id);
                    if(cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK){
                        backCameraId = id;
                        Log.d(TAG, "newApiFlash: got Id");
                        break;
                    }

                }
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }

            if(cameraCharacteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)){

                Log.d(TAG, "newApiFlash: info checked");
                intent= new Intent(MainActivity.this, MyIntentService.class);
                intent.putExtra("CAMERA_ID",backCameraId);
                intent.putExtra("API23",true);
                intent.putExtra("VALUE",seekBarValue);
                startService(intent);

            }else{
                showErrorDiaolg();
            }

        }




    public void oldApiFlash(){


            torch = !torch;
            if(getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)){

                intent = new Intent(MainActivity.this,MyIntentService.class);
                intent.putExtra("API23",false);
                intent.putExtra("VALUE",seekBarValue);
                startService(intent);

            }else{
                showErrorDiaolg();
            }


        }



    public void showErrorDiaolg(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Error");
        builder.setMessage("Device does not have Flash");
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                alertDialog.dismiss();
                finish();
            }
        });
        alertDialog = builder.create();
        finish();
    }
//
//    private void isTorchOn(){
//        Log.d(TAG, "isTorchOn: ");
//        ActivityManager activityManager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
//
//        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = activityManager.getRunningAppProcesses();
//        Iterator<ActivityManager.RunningAppProcessInfo> iterator = runningAppProcesses.iterator();
//
//        while (iterator.hasNext()){
//            Log.d(TAG, "isTorchOn: ");
//            ActivityManager.RunningAppProcessInfo info = iterator.next();
//            Log.d(TAG, "isTorchOn: "+info.processName);
//            String serviceName = getPackageName();
//            if(info.processName.equals(serviceName)){
//                torch = true;
//                Log.d(TAG, "isTorchOn: " +torch);
//                Log.d(TAG, "isTorchOn: "+info.processName);
//                break;
//            }
//        }
//    }

    public void decideVersion(){

      //  torch = isTorchOn();


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            api23 = true;
        } else
            api23 = false;


        if (api23) {
            Log.d(TAG, "onClick: ");
            newApiFlash();
        } else {
            oldApiFlash();
        }
    }

}
