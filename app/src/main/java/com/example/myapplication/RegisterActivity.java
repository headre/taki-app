package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.myapplication.data.OkClient;
import com.example.myapplication.ui.login.LoginActivity;

import org.json.JSONObject;

public class RegisterActivity extends AppCompatActivity {
    private Button button;
    private EditText email, username, password,id_code;
    private String emailS, usernameS, passwordS,id_codeS;
    private Button send_email, register_confirm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        button=findViewById(R.id.register_return);
        email = findViewById(R.id.email);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        id_code = findViewById(R.id.id_code);

        send_email =findViewById(R.id.send_email);
        register_confirm = findViewById(R.id.register_confirm);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
                Intent intent=new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        send_email.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            emailS = email.getText().toString();
            Thread t = new Thread(new Runnable() {
              @Override
              public void run() {
                try{
                  new OkClient().sendIdCode(emailS);
                }catch (Exception e){
                  e.printStackTrace();
                }
              }
            });
            t.start();
          }
        });

        register_confirm.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            usernameS = username.getText().toString();
            passwordS = password.getText().toString();
            id_codeS = id_code.getText().toString();
            Thread t = new Thread(new Runnable() {
              @Override
              public void run() {
                register();
              }
            });
            t.start();
            Intent intent = new Intent(RegisterActivity.this,LoginActivity.class);
            startActivity(intent);
            finish();
          }
        });
    }

    private void register(){
      try {
        new OkClient().register(usernameS,emailS,passwordS,id_codeS);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
}
