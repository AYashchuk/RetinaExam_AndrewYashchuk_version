package com.yashchuk.retinaexam.app.camera.v1;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;
import com.pantasenko.retinaexam.app.R;
import com.yashchuk.retinaexam.app.connection.Connection;
import com.yashchuk.retinaexam.app.connection.TransmitFile;
import com.yashchuk.retinaexam.app.controller.MainActivity;
import com.yashchuk.retinaexam.app.controller.SettingsActivity;
import com.yashchuk.retinaexam.app.domain.Patient;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class CameraActivity extends Activity {

    private static final String TAG = "MyLogs";
    private Camera mCamera;
    private SurfaceView mPreview;  // (CameraPreview)
    private Context mContext;
    private FrameLayout preview;
    private ImageButton captureButton;
    private int camCounter = 0;
    private int numberOfPhotos = SettingsActivity.getNumberOfPhotos();
    private String patientName = Patient.getInstance().getName();
    private String[] filePath = new String[3];
    private String[] fileName = new String[3];
    private File pictureFile = null;
    private LisenConnection listener;
    private String currentPath = null;
    private static SimpleDateFormat format = new SimpleDateFormat("dd.MM.yy.mmss");





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);




        mContext = this;

        // Add a listener to the Capture button
        captureButton = (ImageButton) findViewById(R.id.capture);
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 Log.d(TAG, "Take photo");
                captureButton.setEnabled(false);
                 Thread threadTakePhoto = new Thread(new Runnable() {
                     @Override
                     public void run() {
                         takePhoto();
                         try {
                             Thread.sleep(2000);
                         } catch (InterruptedException e) {
                             e.printStackTrace();
                         }
                     }
                 });
                threadTakePhoto.start();


                if (Connection.getInstance().getIsConnect()) {
                    Thread threadSendPhoto = new Thread(new SendPhotoThread(threadTakePhoto));
                    Log.d(TAG, "Start threadSendPhoto");
                    threadSendPhoto.start();
                }else{
                    Log.d(TAG, "No conecction");
                }
                captureButton.setEnabled(true);
            }
        });
        if(listener == null && Connection.getInstance().getIsConnect()){
            listener = new LisenConnection();
            listener.execute();
        }

    }

     class SendPhotoThread implements Runnable{
         Thread threadSendPhoto;
         SendPhotoThread(Thread thread) {
             threadSendPhoto = thread;
         }

         @Override
                public void run() {
                 int port = 8085;
             try {
                 threadSendPhoto.join();
             } catch (InterruptedException e) {
                 e.printStackTrace();
             }
             System.out.println( "send Photo...");
                 Connection.getInstance().sendMassage("%sendFoto=" + port);
                 new TransmitFile(port, new File(currentPath)).run();
                }
            }



    class LisenConnection extends AsyncTask<String, String, Void> {
        private PrintWriter outStream = null;
        private Scanner inStream = null;
        private boolean interupted = false;

        public void interupted() {
            this.interupted = !interupted;
        }

        public boolean isInterrupted() {
            return this.interupted;
        }
        @Override
        protected void onPreExecute() {
            outStream = Connection.getInstance().getOutStream();
            inStream = Connection.getInstance().getInStream();
        }

        @Override
        protected Void doInBackground(String... params) {
            while (!isInterrupted()) {
                if (inStream.hasNext()) {
                    String massage = inStream.nextLine();
                    System.out.println( "Massage from server TakePhoto");
                    publishProgress(massage);
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            if(values[0].equals("%takePhoto")){
                captureButton.performClick();
            }
        }
    }


    private void takePhoto() {
        System.out.println("takePhoto(): getMediaFile");
        pictureFile = getOutputMediaFile();
        currentPath = "/storage/sdcard0/RetinaExam/Unnamed/" + pictureFile.getName();

        try {
            // dont use autoFocus
            System.out.println( "takePhoto(): makePhoto");
            mCamera.takePicture(null, null,null, mPictureCallback);
            System.out.println("takePhoto(): Photo is maked!");
            // if you want use autofocus:
            /*mCamera.autoFocus(new Camera.AutoFocusCallback() {
                        public void onAutoFocus(boolean success, Camera camera) {
                            mCamera.takePicture(null, null, mPictureCallback);
                            captureButton.setEnabled(false);
                        }
                    }
            );*/
        } catch (Exception e) {

        }

    }

    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
            Log.d(TAG, "Camera is not available (in use or does not exist)");
        }

        //

        return c; // returns null if camera is unavailable
    }

    private Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {

        @Override
        public synchronized void onPictureTaken(byte[] data, Camera camera) {
            mCamera.startPreview();

            //pictureFile = getOutputMediaFile();
            filePath[camCounter] = pictureFile.getPath();
            fileName[camCounter] = pictureFile.getName();
            if (pictureFile == null){
                System.out.println("Error creating media file, check storage permissions.");
                return;
            }
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.flush();
                fos.close();
                System.out.println( "Photo is saved on SD card.");
            } catch (FileNotFoundException e) {
                System.out.println( "File not found: " + e.getMessage());
            } catch (IOException e) {
                System.out.println("Error accessing file: " + e.getMessage());
            }
            //Toast.makeText(mContext, "Photo saved to : " + filePath[camCounter], Toast.LENGTH_SHORT).show();

            // применяем серийную сьемку:
            try {
                camCounter++;
                if (camCounter < numberOfPhotos) {
                    mCamera.takePicture(null,null, null, mPictureCallback);
                } else {
                    camCounter = 0;
                    captureButton.setEnabled(true);
                    try {
                        for (int i = 0; i < filePath.length; i++) {
                            filePath[i] = null;
                        }
                        for (int i = 0; i < fileName.length; i++) {
                            fileName[i] = null;
                        }
                    } catch (Exception e) {
                        Toast.makeText(mContext, "Server is not available.", Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (Exception ex) {
                Log.d(TAG, "Cannot enable burst mode.");
            }

        }
    };

    /** Create a file Uri for saving an image or video */
    //private static Uri getOutputMediaFileUri(){
    //    return Uri.fromFile(getOutputMediaFile());
    //}

    /** Create a File for saving an image or video */
    private File getOutputMediaFile(){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        patientName = Patient.getInstance().getName();
        if(patientName == null) {
            patientName = "Unnamed";
        }

        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(),
                "RetinaExam" + File.separator + patientName);
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                System.out.println("failed to create directory");
                return null;
            }
        }
        // Create a media file name
        String timeStamp = format.format(new Date());
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_"+ timeStamp + ".jpg");
        return mediaFile;
    }

    @Override
    protected void onPause() {
        super.onPause();
        // release the camera immediately on pause event
        if (mCamera != null){
            mCamera.stopPreview();
            mCamera.release();        // release the camera for other applications
            mCamera = null;
            preview.removeView(mPreview);
            mPreview = null;
            Log.d(TAG, "Camera is released.");
        } else {
            Log.d(TAG, "Camera is not release.");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mCamera == null) {
            // Create an instance of Camera
            mCamera = getCameraInstance();
            //Create our Preview view and set it as the content of our activity.
            mPreview = new CameraPreview_Manual(this, mCamera);
            preview = (FrameLayout) findViewById(R.id.preview_layout);
            preview.addView(mPreview, 0);
            mCamera.startPreview();
            Log.d(TAG, "Camera is resumed.");
        } else {
            Log.d(TAG, "Camera is in use.");
        }
    }

    @Override
    public void onBackPressed(){
        if(listener!=null){
            listener.interupted();
        }
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
    }

}
