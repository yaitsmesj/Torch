package com.example.suraj.torch;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;

public class MainActivity extends AppCompatActivity {

    private ImageButton imageButton;
    private FloatingActionButton fab;
    private SeekBar seekBar;
    private AlertDialog alertDialog;

    private SharedPreferences preferences;

    private Boolean api23;
    private Boolean torch = false;

    private int seekBarValue = 1;
    private Intent intent = null;
    private static final String TAG = "Service";

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
                    imageButton.setImageResource(R.drawable.ic_flash_on_black_48dp);

                }else{
                    if(MyIntentService.thread!=null) {
                        MyIntentService.thread.interrupt();
                        MyIntentService.finish = true;

                        Snackbar snackbar = Snackbar.make(view,"Flash Off",Snackbar.LENGTH_SHORT);
                        snackbar.show();
                        imageButton.setImageResource(R.drawable.ic_flash_off_black_48dp);
                    }
                }
                torch=!torch;
                if(!torch)
                    imageButton.setImageResource(R.drawable.ic_flash_off_black_48dp);

                }
        });

    }


    @Override
    protected void onStop() {

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

        if(torch)
            imageButton.setImageResource(R.drawable.ic_flash_on_black_48dp);
        else
            imageButton.setImageResource(R.drawable.ic_flash_off_black_48dp);

        Log.d(TAG, "onResume: ");
    }

    private void newApiFlash(){

        CameraManager cameraManager = getApplicationContext().getSystemService(CameraManager.class);
        CameraCharacteristics cameraCharacteristics = null;

        Log.d(TAG, "newApiFlash: ");

            String[] cameraList;
            String backCameraId = null;

            Log.d(TAG, "newApiFlash: inside false");
            try {
                cameraList = cameraManager.getCameraIdList();
                for(String id : cameraList){
                    cameraCharacteristics = cameraManager.getCameraCharacteristics(id);
                    //noinspection ConstantConditions
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




    private void oldApiFlash(){

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


    private void showErrorDiaolg(){
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


    private void decideVersion(){

        api23 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;


        if (api23) {
            Log.d(TAG, "onClick: ");
            newApiFlash();
        } else {
            oldApiFlash();
        }
    }

}
