package com.yashchuk.retinaexam.app.controller;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

import com.pantasenko.retinaexam.app.R;

public class InstructionsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // если хотим, чтобы приложение было без заголовка
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_instructions);
    }

}
