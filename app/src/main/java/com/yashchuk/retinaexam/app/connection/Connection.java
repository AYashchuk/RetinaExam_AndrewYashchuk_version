package com.yashchuk.retinaexam.app.connection;

import android.util.Log;

import com.yashchuk.retinaexam.app.controller.SettingsActivity;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * Created by admin on 01.03.2015.
 *
 *  поскольку передавать обекты между активностями процес затруднительный
 *  то создаем клас контейнер который по шаблону одиночка, хранит потоки, для
 *  возможной передачи другой активности
 */
public class Connection {
    private static final String TAG = "MyLogs";
    private Socket socket = null;
    private static Connection instance = new Connection();
    private PrintWriter outStream = null;
    private Scanner inStream = null;
    private Boolean isConnect=false;
    private DataOutputStream outData = null;

    public Socket openConnection(String HOST,int PORT) throws IOException {
        socket = new Socket(HOST, PORT);
        outStream = new PrintWriter(socket.getOutputStream());
        inStream = new Scanner(new InputStreamReader(socket.getInputStream()));
        outData = new DataOutputStream(socket.getOutputStream());

        isConnect = true;
        return socket;
    }


    public void setSocket(Socket socket) {
        this.socket = socket;
        try {
            outStream = new PrintWriter(socket.getOutputStream());
            inStream = new Scanner(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    public void clearStream(){
        this.inStream = null;
        this.outStream = null;
        this.socket = null;
    }


    public void close(){
        if(socket != null){
            try {
                System.out.println("send disconnect ");
                sendMassage("%disconnect=0");
                inStream.close();
                outStream.close();
                socket.close();
                clearStream();
            } catch (IOException e) {

                e.printStackTrace();
            }
        }
        isConnect = false;
    }



    synchronized public void sendMassage(String massage) {
        if(outStream != null){
            outStream.write(massage + "\n\r");
            outStream.flush();
        }
    }


    // getters___________________________________________

    public Socket getSocket() {

        return socket;
    }

    public Boolean getIsConnect() {
        return isConnect;
    }

    public PrintWriter getOutStream() {
        return outStream;

    }

    public DataOutputStream getOutData() {
        return outData;
    }

    public Scanner getInStream() {
        return inStream;
    }

    public static Connection getInstance() {
        return instance;
    }
}
