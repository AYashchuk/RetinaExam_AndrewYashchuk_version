package com.yashchuk.retinaexam.app.camera.v2;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Size;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import com.pantasenko.retinaexam.app.R;
import com.yashchuk.retinaexam.app.connection.Connection;
import com.yashchuk.retinaexam.app.connection.TransmitFile;
import com.yashchuk.retinaexam.app.controller.MainActivity;
import com.yashchuk.retinaexam.app.controller.SettingsActivity;
import com.yashchuk.retinaexam.app.domain.Patient;

public class CameraActivity_v2 extends Activity {

    SurfaceView sv;
    SurfaceHolder holder;
    HolderCallback holderCallback;
    Camera camera;
    private File pictureFile = null;
    final int CAMERA_ID = 0;
    final boolean FULL_SCREEN = true;
    private String patientName = Patient.getInstance().getName();
    private ImageButton photo;
    private String currentPath;
    private LisenConnection listener;
    private static SimpleDateFormat format = new SimpleDateFormat("dd.MM.yy.mmss");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_camera_v2);

        sv = (SurfaceView) findViewById(R.id.surfaceView);
        photo = (ImageButton) findViewById(R.id.photo);

        holder = sv.getHolder();
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        holderCallback = new HolderCallback();
        holder.addCallback(holderCallback);

        photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                photo.setEnabled(false);
                Thread threadTakePhoto = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        TakePicture();
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
                    threadSendPhoto.start();
                }else{
                }
                photo.setEnabled(true);
            }
        });
        if(listener == null && Connection.getInstance().getIsConnect()){
            listener = new LisenConnection();
            listener.execute();
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
                photo.performClick();
            }
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

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    @Override
    protected void onResume() {
        super.onResume();
        camera = Camera.open(CAMERA_ID);
        setPreviewSize(FULL_SCREEN);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (camera != null)
            camera.release();
        camera = null;
    }

    public synchronized void TakePicture() {
        pictureFile =  getOutputMediaFile();
        currentPath = "/storage/sdcard0/RetinaExam/Unnamed/" + pictureFile.getName();
        try{
            camera.takePicture(null, null,null, new Camera.PictureCallback() {


                @Override
                public void onPictureTaken(byte[] data, Camera camera) {
                    try {
                        FileOutputStream fos = new FileOutputStream(pictureFile);
                        fos.write(data);
                        fos.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    camera.startPreview();
                }
            });
        }catch (RuntimeException e){

        }

    }



    class HolderCallback implements SurfaceHolder.Callback {

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                Camera.Parameters parameters = camera.getParameters();
                parameters.setRotation(90);                                               // or 90
                parameters.set("orientation", "portrait");
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                camera.setParameters(parameters);
                camera.setPreviewDisplay(holder);
                camera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                   int height) {
            camera.stopPreview();
            setCameraDisplayOrientation(CAMERA_ID);
            try {
                camera.setPreviewDisplay(holder);
                camera.startPreview();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }

    }

    void setPreviewSize(boolean fullScreen) {

        // получаем размеры экрана
        Display display = getWindowManager().getDefaultDisplay();
        boolean widthIsMax = display.getWidth() > display.getHeight();

        // определяем размеры превью камеры
        Size size = camera.getParameters().getPreviewSize();

        RectF rectDisplay = new RectF();
        RectF rectPreview = new RectF();

        // RectF экрана, соотвествует размерам экрана
        rectDisplay.set(0, 0, display.getWidth(), display.getHeight());

        // RectF первью
        if (widthIsMax) {
            // превью в горизонтальной ориентации
            rectPreview.set(0, 0, size.width, size.height);
        } else {
            // превью в вертикальной ориентации
            rectPreview.set(0, 0, size.height, size.width);
        }

        Matrix matrix = new Matrix();
        // подготовка матрицы преобразования
        if (!fullScreen) {
            // если превью будет "втиснут" в экран (второй вариант из урока)
            matrix.setRectToRect(rectPreview, rectDisplay,
                    Matrix.ScaleToFit.START);
        } else {
            // если экран будет "втиснут" в превью (третий вариант из урока)
            matrix.setRectToRect(rectDisplay, rectPreview,
                    Matrix.ScaleToFit.START);
            matrix.invert(matrix);
        }
        // преобразование
        matrix.mapRect(rectPreview);

        // установка размеров surface из получившегося преобразования
        sv.getLayoutParams().height = (int) (rectPreview.bottom);
        sv.getLayoutParams().width = (int) (rectPreview.right);
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    void setCameraDisplayOrientation(int cameraId) {
        // определяем насколько повернут экран от нормального положения
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result = 0;

        // получаем инфо по камере cameraId
        CameraInfo info = new CameraInfo();
        Camera.getCameraInfo(cameraId, info);

        // задняя камера
        if (info.facing == CameraInfo.CAMERA_FACING_BACK) {
            result = ((360 - degrees) + info.orientation);
        } else
            // передняя камера
            if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
                result = ((360 - degrees) - info.orientation);
                result += 360;
            }
        result = result % 360;
        camera.setDisplayOrientation(result);
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


    @Override
    public void onBackPressed(){
        if(listener!=null){
            listener.interupted();
        }

        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
    }

}
