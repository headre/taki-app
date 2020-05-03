package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.data.OkClient;
import com.example.myapplication.ui.login.LoginActivity;

import org.json.JSONObject;

/**this is the activity for new users to register
 * it ask users to send a idcode email to email address first
 * if idcode is certificated, other information will be able to build the account*/

public class RegisterActivity extends AppCompatActivity {
    private Button button;
    private EditText email, username, password,id_code;
    private String emailS, usernameS, passwordS,id_codeS,response;
    private Button send_email, register_confirm;
    private Integer emailOK=0,pwdOK=0,idcodeOK=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //initialize the widgets
        button=findViewById(R.id.register_return);
        email = findViewById(R.id.email);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        id_code = findViewById(R.id.id_code);

        send_email =findViewById(R.id.send_email);
        send_email.setEnabled(false);
        register_confirm = findViewById(R.id.register_confirm);
        register_confirm.setEnabled(false);

        //email address format check
        email.addTextChangedListener(new TextWatcher() {
          @Override
          public void beforeTextChanged(CharSequence s, int start, int count, int after) {

          }

          @Override
          public void onTextChanged(CharSequence s, int start, int before, int count) {

          }

          @Override
          public void afterTextChanged(Editable s) {
              if(!(s.toString().indexOf("@") >0)){
                email.setError("please enter 合适的格式");
                emailOK=0;
                send_email.setEnabled(false);
              }else{
                emailOK=1;
                send_email.setEnabled(true);
              }
              listenOnEditFormat();
          }
        });

        //id code format check
        password.addTextChangedListener(new TextWatcher() {
          @Override
          public void beforeTextChanged(CharSequence s, int start, int count, int after) {

          }

          @Override
          public void onTextChanged(CharSequence s, int start, int before, int count) {

          }

          @Override
          public void afterTextChanged(Editable s) {
            if(s.length()<6){
              password.setError("password must be >6 characters");
              pwdOK=0;
            }else {
              pwdOK=1;
            }
            listenOnEditFormat();
          }
        });

        id_code.addTextChangedListener(new TextWatcher() {
          @Override
          public void beforeTextChanged(CharSequence s, int start, int count, int after) {

          }

          @Override
          public void onTextChanged(CharSequence s, int start, int before, int count) {

          }

          @Override
          public void afterTextChanged(Editable s) {
              if(s.length()!=6){
                id_code.setError("Id code must be 6 numbers");
                idcodeOK=0;
              }else {
                idcodeOK=1;
              }
              listenOnEditFormat();
          }
        });


        //jump to login page
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
                Intent intent=new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        //call the method to send email
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
                runOnUiThread(new Runnable() {
                  @Override
                  public void run() {
                    Toast toast = Toast.makeText(RegisterActivity.this,R.string.idcode_sent,Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER,0,0);
                    toast.show();
                  }
                });
              }
            });
            t.start();
          }
        });

        //upload data to register
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
          }
        });
    }

    private void register(){
      try {
        response = new OkClient().register(usernameS,emailS,passwordS,id_codeS);
        checkIfRegisterSuccess(response);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    //if unenough data is input, rigister is not allowed
    private void listenOnEditFormat(){
      if(emailOK==1&&pwdOK==1&&idcodeOK==1){
        register_confirm.setEnabled(true);
      }else {
        register_confirm.setEnabled(false);
      }
    }

    //with different message, print the hints out
    private void checkIfRegisterSuccess(String response) throws Exception{
      JSONObject responseJSON = new JSONObject(response);
      int tips;
      Boolean success = false;
      if(responseJSON.has("message")&&responseJSON.getString("message").equals(R.string.response_overdue)){
        tips = R.string.toast_overdue;

      }else if(responseJSON.has("error")){
        tips = R.string.unknown_error;
      }else{
        tips = R.string.register_success;
        success=true;
      }
      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          Toast toast = Toast.makeText(RegisterActivity.this,tips,Toast.LENGTH_SHORT);
          toast.setGravity(Gravity.CENTER,0,0);
          toast.show();
        }
      });

      if(success){
        Intent intent = new Intent(RegisterActivity.this,LoginActivity.class);
        startActivity(intent);
        finish();
      }
    }
}
