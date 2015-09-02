package com.yashchuk.retinaexam.app.camera.v3;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import java.io.File;
import android.graphics.Bitmap;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import com.pantasenko.retinaexam.app.R;
import com.yashchuk.retinaexam.app.connection.Connection;
import com.yashchuk.retinaexam.app.controller.SettingsActivity;
import com.yashchuk.retinaexam.app.domain.Patient;

import java.io.File;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

/**
 * Created by admin on 18.03.2015.
 */
public class CameraActivity_v3 extends Activity {

    private final int TYPE_PHOTO = 1;
    private String patientName = Patient.getInstance().getName();
    private final int REQUEST_CODE_PHOTO = 1;
    private List<File> photos;
    private final String TAG = "myLogs";
    private static int currentPosition = 0;
    private ImageView ivPhoto;
    private final String currentPath = "/storage/sdcard0/RetinaExam/";
    private static SimpleDateFormat format = new SimpleDateFormat("dd.MM.yy.mmss");
    private TextView PhotoName;
    private TextView PhotoDate;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_camera_v3);
        ivPhoto = (ImageView) findViewById(R.id.imageView_v3);


        PhotoName = (TextView) findViewById(R.id.textPatientName);
        PhotoDate = (TextView) findViewById(R.id.textDate);

        PhotoName.setTextSize(15);
        PhotoDate.setTextSize(15);
        setPhotoFile();

    }

    public void setPhotoFile(){
        try{
            photos = searchPhotoFile(new File(currentPath));
        }catch (NullPointerException e){
            e.printStackTrace();
        }
        if(photos != null){
            currentPosition = photos.size()-1;
            ivPhoto.setImageURI(Uri.fromFile(photos.get(currentPosition)));
            setPatientProperties(photos.get(currentPosition));
        }

    }


    private List<File> searchPhotoFile(File currentDirectory){
        List<File> photos = new ArrayList<File>();
        File [] usersDir = currentDirectory.listFiles();
        for(int i = usersDir.length-1 ; i>=0 ;i--){
            File [] usersPhotoInCurrentDir = usersDir[i].listFiles();
            for(int j = 0 ;j < usersPhotoInCurrentDir.length; j++){
                photos.add(usersPhotoInCurrentDir[j]);
            }
        }
        return photos;
    }

    public void onClickNavi(View view){
        int id = view.getId();
        if(photos != null){
            if(id == R.id.btnLeft){
                currentPosition--;
                if(currentPosition == -1){
                    currentPosition = photos.size()-1;
                }
            }if(id == R.id.btnRight){
                currentPosition++;
                if(currentPosition == photos.size()){
                    currentPosition = 0;
                }
            }
            new ImageViewTask().execute();
        }else{

        }
    }

    private void setPatientProperties(File file) {
        String patientName = file.getPath();
        String dirName [] = patientName.split("/");
        patientName =   dirName[dirName.length-2];


        String photoName = file.getName();
        String date = photoName.substring(4,12);

        PhotoName.setText("Patient name: " + patientName);
        PhotoDate.setText("Date: " + date);

    }

    public void onClickPhoto(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, generateFileUri());
        startActivityForResult(intent, REQUEST_CODE_PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,Intent intent) {
        if (requestCode == REQUEST_CODE_PHOTO) {
            if (resultCode == RESULT_OK) {
                setPhotoFile();
            } else if (resultCode == RESULT_CANCELED) {
                Log.d(TAG, "Canceled");
            }
        }
    }

    private Uri generateFileUri() {
        File file = getOutputMediaFile();
        return Uri.fromFile(file);
    }

    /** Create a File for saving an image or video */
    private File getOutputMediaFile(){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
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


    class ImageViewTask extends AsyncTask<String, Uri, Void> {

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Void doInBackground(String... params) {
            Uri uri = Uri.fromFile(photos.get(currentPosition));
            publishProgress(uri);
            return null;
        }

        @Override
        protected void onProgressUpdate(Uri... values) {
            super.onProgressUpdate(values);
            try{
                 ivPhoto.setImageURI(values[0]);
                 setPatientProperties(photos.get(currentPosition));
            }catch (NullPointerException e){
                e.printStackTrace();
            }
        }
    }
}