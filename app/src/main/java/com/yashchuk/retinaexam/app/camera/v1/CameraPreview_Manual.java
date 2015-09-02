package com.yashchuk.retinaexam.app.camera.v1;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import java.io.IOException;

/**
 * Created by admin on 18.03.2015.
 */
public class CameraPreview_Manual extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private Context mContext;

    public CameraPreview_Manual(Context context, Camera camera) {
        super(context);
        this.mContext = context;
        this.mCamera = camera;
        this.mCamera.setDisplayOrientation(90);
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        this.mHolder = getHolder();
        this.mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        this.mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
/*        setCameraDisplayOrientation();*/
    }

    public void surfaceCreated(SurfaceHolder holder) {
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setRotation(90);                                               // or 90
        parameters.set("orientation", "portrait");
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        mCamera.setParameters(parameters);
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
    }


    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (mHolder.getSurface() == null){
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e){
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();

        } catch (Exception e){
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
}
