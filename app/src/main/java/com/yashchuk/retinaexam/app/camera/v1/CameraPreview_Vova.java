package com.yashchuk.retinaexam.app.camera.v1;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;

import com.yashchuk.retinaexam.app.controller.SettingsActivity;

import java.io.IOException;

public class CameraPreview_Vova extends SurfaceView implements SurfaceHolder.Callback {

    SurfaceHolder mHolder;
    Camera mCamera;
    Context mContext;
    int jpegQuality = SettingsActivity.getJpegQuality();

    public CameraPreview_Vova(Context context, Camera camera) {
        super(context);
        mContext = context;
        mCamera = camera;
        // Получаем SurfaceHolder этого SurfaceView, для управления отображением
        mHolder = getHolder();
        // Устанавливаем методы обратного вызова. Наш класс реализует интерфейс Callback, так что его и передаем
        mHolder.addCallback(this);
        // Эта строчка нужна только для версии android ниже 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {

            mCamera.setDisplayOrientation(90);    // --
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setPictureFormat(ImageFormat.JPEG);
            parameters.setJpegQuality(jpegQuality);
            //Toast.makeText(mContext, "Quality is :" + jpegQuality, Toast.LENGTH_LONG).show();
            parameters.setRotation(90);                                               // or 90
            parameters.set("orientation", "portrait");
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            mCamera.setParameters(parameters);   ///
            //Toast.makeText(mContext, "Rotation is 0", Toast.LENGTH_LONG).show();
            //

            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        }
        catch (IOException e) {
            Toast.makeText(mContext, "Camera preview failed", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // если нет изменений выходим из метода surfaceChanged
        if (mHolder.getSurface() == null) {
            return;
        }
        try {
            // останавливаем предпросмотр
            mCamera.stopPreview();
            // определяем ориентацию экрана
            //mCamera.setDisplayOrientation(90);    // --
            //mCamera.getParameters().setPictureFormat(ImageFormat.JPEG);
            //mCamera.getParameters().setJpegQuality(100);
            //mCamera.getParameters().setRotation(0);
            // -- PREVIEW (TEST)
            /*
            Camera.Parameters mParameters = mCamera.getParameters();
            Camera.Size bestSize = null;

            List<Camera.Size> sizeList = mCamera.getParameters().getSupportedPreviewSizes();
            bestSize = sizeList.get(0);

            for(int i = 1; i < sizeList.size(); i++){
                if((sizeList.get(i).width * sizeList.get(i).height) >
                        (bestSize.width * bestSize.height)){
                    bestSize = sizeList.get(i);
                }
            }

            mParameters.setPreviewSize(bestSize.width, bestSize.height);
            mCamera.setParameters(mParameters);
            //mCamera.startPreview();
            */
            // -- PREVIEW (TEST)
            //setCameraDisplayOrientation();
        } catch (Exception e) {
            //
        }

        // вносим изменения и возобновляем предпросмотр
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        }
        catch (IOException e) {
            Toast.makeText(mContext, "Camera preview failed", Toast.LENGTH_LONG).show();
        }
    }

    // определяем ориентацию экрана
    @SuppressLint("NewApi")
    public void setCameraDisplayOrientation() {
        if (mCamera == null) {
            return;
        }
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(0, info);

        WindowManager winManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);

        //  получаем ориентацию экрана. результат возвращается не в градусах, а в виде констант
        int rotation = winManager.getDefaultDisplay().getRotation();

        int degrees = 0;

        switch (rotation) {
            //  переводим константы в градусы
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

        // рассчитываем на сколько нам надо повернуть экран просмотра.
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;
        } else {
            result = (info.orientation - degrees + 360) % 360;
        }

        //  устанавливаем угол поворота экрана просмотра
        mCamera.setDisplayOrientation(result);

        // получаем параметры камеры. рассчитываем и устанавливаем поворот камеры
        Camera.Parameters parameters = mCamera.getParameters();
        int rotate = (degrees + 270) % 360;
        parameters.setRotation(rotate);
        mCamera.setParameters(parameters);

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        //
    }

}
