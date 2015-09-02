package com.yashchuk.retinaexam.app.controller;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.internal.view.menu.ActionMenuItemView;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.widget.RadioButton;

import com.pantasenko.retinaexam.app.R;
import com.yashchuk.retinaexam.app.camera.v1.CameraActivity;
import com.yashchuk.retinaexam.app.camera.v2.CameraActivity_v2;
import com.yashchuk.retinaexam.app.camera.v3.CameraActivity_v3;
import com.yashchuk.retinaexam.app.connection.Connection;

public class MainActivity extends Activity {
    private RadioButton rb1;
    private RadioButton rb2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // если хотим, чтобы приложение было без заголовка
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // заполняем Activity видом activity_main
        setContentView(R.layout.activity_main);

        rb1 = (RadioButton) findViewById(R.id.radioButton1);
        rb2 = (RadioButton) findViewById(R.id.radioButton2);

        View.OnClickListener onClickListener = new MyOnClickListener();

        rb1.setOnClickListener(onClickListener);
        rb2.setOnClickListener(onClickListener);

        rb1.setChecked(true);
    }

    // создаем метод который будет обрабатывать нажатие кнопки с id "btnTakePhoto"
    public void onClickBtnTakePhoto (View v) {
        // создаем intent (намерение) котому прописываем какое Activity мы хотим вызвать
        Intent intent = null;
        if(rb1.isChecked()){
            intent = new Intent(this, CameraActivity.class);
        }if(rb2.isChecked()){
            intent = new Intent(this, CameraActivity_v2.class);
        }
        startActivity(intent);
    }

    // создаем метод который будет обрабатывать нажатие кнопки с id "btnSettings"
    public void onClickBtnSettings (View v) {
        // создаем intent (намерение) котому прописываем какое Activity мы хотим вызвать
        Intent intent = new Intent(this, SettingsActivity.class);
        // следующий метод находит соответствующее Activity и показывает его
        startActivity(intent);
    }

    // создаем метод который будет обрабатывать нажатие кнопки с id "btnInstructions"
    public void onClickBtnInstructions (View v) {
        // создаем intent (намерение) котому прописываем какое Activity мы хотим вызвать
        Intent intent = new Intent(this, InstructionsActivity.class);
        // следующий метод находит соответствующее Activity и показывает его
        startActivity(intent);
    }


    // создаем метод который будет обрабатывать нажатие кнопки с id "btnInstructions"
    public void onClickBtnView (View v) {
        // создаем intent (намерение) котому прописываем какое Activity мы хотим вызвать
        Intent intent = new Intent(this, CameraActivity_v3.class);
        // следующий метод находит соответствующее Activity и показывает его
        startActivity(intent);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        System.out.println("onDestroy close");
        Connection.getInstance().close();
    }

    class MyOnClickListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            int id = v.getId();
            if(id == R.id.radioButton1){
                rb2.setChecked(false);
            }if(id == R.id.radioButton2){
                rb1.setChecked(false);
            }
        }
    }


    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Exit with program?");
        // builder.setMessage("Покормите кота!");
        builder.setIcon(android.R.drawable.ic_dialog_info);
        builder.setCancelable(false);
        // системно выходим сприложения
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                onDestroy();
                startActivity(intent);


            }
        });
        builder.setNegativeButton("No",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel(); // отмена возвращаемся к MainActivity
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

}
