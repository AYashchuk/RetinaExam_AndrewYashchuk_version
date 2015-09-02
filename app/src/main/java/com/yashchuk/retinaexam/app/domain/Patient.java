package com.yashchuk.retinaexam.app.domain;

/**
 * Created by admin on 25.03.2015.
 */
public class Patient {
    private String name;
    private static Patient instance = new Patient();


    private Patient() {
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {

        return name;
    }

    public static Patient getInstance() {
        return instance;
    }
}
