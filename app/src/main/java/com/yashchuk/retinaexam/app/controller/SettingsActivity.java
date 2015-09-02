package com.yashchuk.retinaexam.app.controller;

import android.app.Activity;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.pantasenko.retinaexam.app.R;
import com.yashchuk.retinaexam.app.connection.Connection;
import com.yashchuk.retinaexam.app.domain.Patient;

import java.io.IOException;
import java.net.Socket;

public class SettingsActivity extends Activity {

    private EditText editTextPatientName;
    private EditText editTextServerHost;
    private Button button1;
    private Button button2;
    private Button button3;
    private Button buttonQ10;
    private Button buttonQ08;
    private Button buttonQ06;
    private Button buttonQ04;
    private Button buttonConnect;
    static String serverHost = null;
    static int numberOfPhotos = 1;
    static int jpegQuality = 100;
    private TextView state;
    private ConnectionFromServer connectionFromServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // если хотим, чтобы приложение было без заголовка
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_settings);

        // находим элементы EditText
        editTextPatientName = (EditText) findViewById(R.id.editTextPatientName);
        editTextServerHost = (EditText) findViewById(R.id.editTextServerHost);

        // если переменная patientName не пустая заполняем поле editTextPatientName
        if(Patient.getInstance().getName() != null) {
            editTextPatientName.setText(Patient.getInstance().getName());
        }

        // если переменная serverHost не пустая заполняем поле editTextServerHost
        if(serverHost != null) {
            editTextServerHost.setText(serverHost);
        }

        // находим button элементы
        button1 = (Button) findViewById(R.id.btnFrame1);
        button2 = (Button) findViewById(R.id.btnFrame2);
        button3 = (Button) findViewById(R.id.btnFrame3);

        buttonQ10 = (Button) findViewById(R.id.btnQuality1);
        buttonQ08 = (Button) findViewById(R.id.btnQuality2);
        buttonQ06 = (Button) findViewById(R.id.btnQuality3);
        buttonQ04 = (Button) findViewById(R.id.btnQuality4);

        buttonConnect = (Button) findViewById(R.id.connect);
        state = (TextView) findViewById(R.id.textConnect);


        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Connection.getInstance().getIsConnect()){
                    Connection.getInstance().close();
                }
                connectionFromServer = new ConnectionFromServer();
                if(getServerHost()==null){
                    setServerHost(editTextServerHost.getText().toString());
                }
                connectionFromServer.execute(getServerHost());
            }
        });



        // создаем обработчик кнопок
        View.OnClickListener oclBtnFrame = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // делаем все кнопки активными
                button1.setEnabled(true);
                button2.setEnabled(true);
                button3.setEnabled(true);
                // по id находим кнопку, вызвавшую этот обработчик
                switch (view.getId()) {
                    case R.id.btnFrame1:
                        numberOfPhotos = 1;
                        button1.setEnabled(false);
                        break;
                    case R.id.btnFrame2:
                        numberOfPhotos = 2;
                        button2.setEnabled(false);
                        break;
                    case R.id.btnFrame3:
                        numberOfPhotos = 3;
                        button3.setEnabled(false);
                        break;
                }
            }
        };

        View.OnClickListener oclBtnQuality = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // делаем все кнопки активными
                buttonQ10.setEnabled(true);
                buttonQ08.setEnabled(true);
                buttonQ06.setEnabled(true);
                buttonQ04.setEnabled(true);
                // по id находим кнопку, вызвавшую этот обработчик
                switch (view.getId()) {
                    case R.id.btnQuality1:
                        jpegQuality = 100;
                        buttonQ10.setEnabled(false);
                        break;
                    case R.id.btnQuality2:
                        jpegQuality = 80;
                        buttonQ08.setEnabled(false);
                        break;
                    case R.id.btnQuality3:
                        jpegQuality = 60;
                        buttonQ06.setEnabled(false);
                        break;
                    case R.id.btnQuality4:
                        jpegQuality = 10;
                        buttonQ04.setEnabled(false);
                        break;
                }
            }
        };

        // присваиваем кнопкам обработчик
        button1.setOnClickListener(oclBtnFrame);
        button2.setOnClickListener(oclBtnFrame);
        button3.setOnClickListener(oclBtnFrame);
        //
        buttonQ10.setOnClickListener(oclBtnQuality);
        buttonQ08.setOnClickListener(oclBtnQuality);
        buttonQ06.setOnClickListener(oclBtnQuality);
        buttonQ04.setOnClickListener(oclBtnQuality);

        // оставляем выделенной нажату раньше кнопку
        if(numberOfPhotos == 1) button1.setEnabled(false);
        if(numberOfPhotos == 2) button2.setEnabled(false);
        if(numberOfPhotos == 3) button3.setEnabled(false);
        //
        if(jpegQuality == 100) buttonQ10.setEnabled(false);
        if(jpegQuality == 80) buttonQ08.setEnabled(false);
        if(jpegQuality == 60) buttonQ06.setEnabled(false);
        if(jpegQuality == 40) buttonQ04.setEnabled(false);


        if(Connection.getInstance().getIsConnect()){
            state.setText("State: connection Accept!");
            state.setTextColor(Color.GREEN);
        }

    }


    // создаем метод который будет обрабатывать нажатие кнопки с id "onClickBtnApply"
    public void onClickBtnApply (View v) {
        // проверяем поля на пустоту
        if (TextUtils.isEmpty(editTextServerHost.getText().toString())) {
            Toast.makeText(this, "Enter IP-adress", Toast.LENGTH_SHORT).show();
            serverHost = null;
        }if(TextUtils.isEmpty(editTextPatientName.getText().toString())){
            Patient.getInstance().setName("Unnamed");
        }
        // читаем EditText и заполняем переменные строками
        Patient.getInstance().setName(editTextPatientName.getText().toString());
        serverHost = editTextServerHost.getText().toString();
        System.out.println("Name : " + Patient.getInstance().getName() + "\nHost : " + serverHost);
    }


    class ConnectionFromServer extends AsyncTask <String, String, Void>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            state.setText("State: open connection...");
            state.setTextColor(Color.GREEN);
        }

        @Override
        protected Void doInBackground(String... host) {
            System.out.println("doInBackground  " +host[0]);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(host[0] != null){
                try {
                    publishProgress("State: Create socket...", "0");
                    Socket socket = Connection.getInstance().openConnection(host[0],8050);
                    publishProgress("State: connection Accept!", "0");

                } catch (IOException e) {
                    e.printStackTrace();
                    publishProgress("State: connection lost...", "1");
                }
            }else{
                publishProgress("State: enter IP!", "1");
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            String massage = values[0];
            System.out.println("onProgressUpdate:  " + massage);
            state.setText(massage);
            if(values[1].equals("0")){
                state.setTextColor(Color.GREEN);
            }else{
                state.setTextColor(Color.RED);
            }


        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

        }
    }

    public static void setServerHost(String serverHost) {
        SettingsActivity.serverHost = serverHost;
    }

    // создаем метод возвращающий строку с ip-адрессом сервера
    public static String getServerHost() {
        return serverHost;
    }

    // создаем метод возвращающий число с количеством фото
    public static int getNumberOfPhotos() {
        return numberOfPhotos;
    }

    public static int getJpegQuality() {
        return jpegQuality;
    }


}
