package com.example.myapplication.ui.login;

import android.app.Activity;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

import com.example.myapplication.FilmActivity;
import com.example.myapplication.R;
import com.example.myapplication.RegisterActivity;
import com.example.myapplication.data.OkClient;
import com.example.myapplication.ui.login.LoginViewModel;
import com.example.myapplication.ui.login.LoginViewModelFactory;

public class LoginActivity extends AppCompatActivity {

    private LoginViewModel loginViewModel;
    private Button lButton,loginButton;
    private TextView register;
    public  int tag_P=1,tag_E=0;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        //跳过登录部分
        SharedPreferences sharedPreferences = getSharedPreferences("login",MODE_PRIVATE);
        String cookie = sharedPreferences.getString("cookie","");
        final Intent intent=new Intent(LoginActivity.this, FilmActivity.class);
        if(cookie!=""){
            startActivity(intent);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginViewModel = ViewModelProviders.of(this, new LoginViewModelFactory())
                .get(LoginViewModel.class);

         register=findViewById(R.id.register);
       // register.setMovementMethod(LinkMovementMethod.getInstance());
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //jump to films
                Intent intent=new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        lButton=findViewById(R.id.btnFilm);
        lButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //jump to films
                Intent intent=new Intent(LoginActivity.this, FilmActivity.class);
                startActivity(intent);
            }
        });
        changeLoginEditText();
    }

    private void changeLoginEditText(){
        if(tag_P==0&&tag_E==1){
            setLoginEditText(findViewById(R.id.phone_number),findViewById(R.id.phone_id_code));
            loginViewModel.setType(tag_E);
        }else if(tag_E==0&&tag_P==1){
            setLoginEditText(findViewById(R.id.username),findViewById(R.id.password));
            loginViewModel.setType(tag_P);
        }
    }
    private void setLoginEditText(EditText account,EditText idcode){
        SharedPreferences sharedPreferences = getSharedPreferences("login",MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        final Intent intent=new Intent(LoginActivity.this, FilmActivity.class);
        loginButton = findViewById(R.id.login);
        final ProgressBar loadingProgressBar = findViewById(R.id.loading);

        loginViewModel.getLoginFormState().observe(this, new Observer<LoginFormState>() {
            @Override
            public void onChanged(@Nullable LoginFormState loginFormState) {
                if (loginFormState == null) {
                    return;
                }
                loginButton.setEnabled(loginFormState.isDataValid());
                if (loginFormState.getUsernameError() != null) {
                    account.setError(getString(loginFormState.getUsernameError()));
                }
                if (loginFormState.getPasswordError() != null) {
                    idcode.setError(getString(loginFormState.getPasswordError()));
                }
            }
        });

        loginViewModel.getLoginResult().observe(this, new Observer<LoginResult>() {
            @Override
            public void onChanged(@Nullable LoginResult loginResult) {
                if (loginResult == null) {
                    return;
                }
                loadingProgressBar.setVisibility(View.GONE);
                if (loginResult.getError() != null) {
                    showLoginFailed(loginResult.getError());
                }
                if (loginResult.getSuccess() != null) {
                    //确认登录成功后完成跳转工作
                    Log.e("cookie",loginResult.getSuccess().getCookie());
                    editor.putString("username",loginResult.getSuccess().getDisplayName());
                    editor.putString("cookie", loginResult.getSuccess().getCookie());
                    editor.commit();
                    startActivity(intent);
                    updateUiWithUser(loginResult.getSuccess());
                    finish();
                }
                setResult(Activity.RESULT_OK);
            }
        });

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                loginViewModel.loginDataChanged(account.getText().toString(),
                        idcode.getText().toString());
            }
        };
        account.addTextChangedListener(afterTextChangedListener);
        idcode.addTextChangedListener(afterTextChangedListener);
        idcode.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    loginViewModel.login(account.getText().toString(),
                            idcode.getText().toString());
                }
                return false;
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingProgressBar.setVisibility(View.VISIBLE);
                loginViewModel.login(account.getText().toString(),
                        idcode.getText().toString());
            }
        });

    }

    public void sendMessage(View view){
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final EditText phoneNumberEditText = findViewById(R.id.phone_number);
                    new OkClient().sendMessage(phoneNumberEditText.getText().toString());
                }catch (Exception e){
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String tips = "id code has been sent to your phone";
                        Toast toast = Toast.makeText(LoginActivity.this, tips, Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                    }
                });

            }
        });
        t.start();
    }
 public void login_P(View view)
    {
        change_P();
    }
    public void login_E(View view)
    {
        change_E();
    }

    public void change_P() {
        if (tag_P == 0) {
            LinearLayout off = findViewById(R.id.input_phone);
            off.setVisibility(View.INVISIBLE);
            LinearLayout on = findViewById(R.id.input_email);
            on.setVisibility(View.VISIBLE);
            tag_P = 1;tag_E=0;
        }
        changeLoginEditText();
    }

    public void change_E() {
        if (tag_E == 0) {
            LinearLayout off = findViewById(R.id.input_email);
            off.setVisibility(View.INVISIBLE);
            LinearLayout on = findViewById(R.id.input_phone);
            on.setVisibility(View.VISIBLE);
            tag_E = 1;tag_P=0;
        }
        changeLoginEditText();
    }
    private void updateUiWithUser(LoggedInUserView model) {
        String welcome = getString(R.string.welcome) + model.getDisplayName();
        // TODO : initiate successful logged in experience
        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
    }

    private void showLoginFailed(@StringRes Integer errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }
}
