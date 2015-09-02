package com.yashchuk.retinaexam.app.connection;

import android.util.Log;

import com.yashchuk.retinaexam.app.controller.SettingsActivity;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by admin on 17.03.2015.
 */
public class TransmitFile{
    private static final String TAG = "MyLogs" ;
    private String filePath;
    private File file ;
    private String action;
    private String host;
    private int onPort;
    Socket socket;


    public TransmitFile(int onPort,File sendFile){
        this.onPort = onPort;
        this.file = sendFile;
    }




    synchronized public void run(){
        try {
            System.out.println( "Connect on srver (host = " + SettingsActivity.getServerHost()+"), port = " + onPort);
            socket = new Socket(SettingsActivity.getServerHost(),onPort);
            System.out.println( "send file");
            sendFile(file, socket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }






    private  void sendFile(File file, Socket socket) {
            Log.d(TAG, "send file...");
        try{
            System.out.println("path: " + file.getPath());
        }catch (Exception e ){
           e.printStackTrace();
        }

            String name = file.getName();
            System.out.println("Sending File: " + file.getName()+" ...");
            // передаём имя файла
        DataOutputStream outFileName = null;
        try {
            outFileName = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            outFileName.writeUTF(name);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            outFileName.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // создаем входной поток для передачи файла
            System.out.println("create inputFile...");
        FileInputStream inputFile = null;
        try {
            inputFile = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println("create inputFile");
            // записываем в переменную size размер (length) файла в байтах
            int size = (int)file.length();
            // создаем массив buffer типа byte в котором будем передавать байты информации
            byte[] buffer = new byte[size];
        System.out.println("size buffer: " + size);
            System.out.println("Create new Out Stream...");
            // создаем новый выходной поток для передачи файла серверу
        DataOutputStream outputFile = null;
        try {
            outputFile = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("New Out strea, is created...");

            // отправляем файл на сервер
            System.out.println("Send file...");
            int receivedBytes = 0;  // переменная счетчика
            while(true) {
                System.out.println(receivedBytes);
                try {
                    receivedBytes = inputFile.read(buffer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (receivedBytes > 0) {
                    try {
                        outputFile.write(buffer, 0, receivedBytes);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (receivedBytes == -1) {
                    System.out.println( "send is sent!");
                    break;
                }

            }


            System.out.println("File "+file.getName()+" is sent!");
        try {
            outFileName.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // закрываем входной поток
        try {
            inputFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // закрываем выходной поток
        try {
            outputFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("IO Streams wear closed");

            try {
                socket.close();
            } catch (IOException e)

    {
        e.printStackTrace();
    }



    }





    private  void receiveFile(File path,Socket socket) {
        System.out.println("recive file...");
        try{
            // поток для приема строки от сервера содержащей название файла
            DataInputStream disFileName = new DataInputStream(socket.getInputStream());
            System.out.println("Created new INstream...");
            // считуем имя файла
            String FileName = disFileName.readUTF();
            System.out.println("Recive File name: (" + FileName + ")");
            // создаем новый входной поток для записи принятого файла
            DataInputStream input = new DataInputStream(socket.getInputStream());
            // создаем новый файл в созданной нами директории
            File file = new File(path.getPath() + File.separator + FileName);
            filePath = file.getPath()+"\\"+file.getName();
            System.out.println("recive file: "+FileName);
            // создаем новый выходной поток чтобы сохранить созданный файл на диск
            System.out.println("Creted new stream..." + file.getPath());
            FileOutputStream output = new FileOutputStream(file);
            System.out.println("New Stream is created!");
            // создаем массив типа byte с количесством элементов (байт) равным 1024 (1 Кбайт)
            byte[] b = new byte[1024];
            // принимаем и сохраняем файл на диск
            System.out.println("Save File...");
            int count = 0;  // переенная счетчика
            while (true) {
                count = input.read(b);
                if (count > 0) {
                    output.write(b, 0, count);
                }
                if (count == -1) {
                    System.out.println("File is saved!");
                    output.close();

                    break;
                }
            }
            disFileName.close();
            input.close();
        }catch(Exception e){
            System.out.println("Error: File not recive...");
        }
        if(socket != null){
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // getters_____________


    public String getFilePath() {
        return filePath;
    }

    // setters__________________
    public void setOnPort(int onPort) {
        this.onPort = onPort;
    }
}
