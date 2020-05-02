package com.example.myapplication.ui.login;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import android.util.Log;
import android.util.Patterns;

import com.example.myapplication.data.LoginRepository;
import com.example.myapplication.data.Result;
import com.example.myapplication.data.model.LoggedInUser;
import com.example.myapplication.R;

public class LoginViewModel extends ViewModel {

    private MutableLiveData<LoginFormState> loginFormState = new MutableLiveData<>();
    private MutableLiveData<LoginResult> loginResult = new MutableLiveData<>();
    private LoginRepository loginRepository;
    private Integer type=0;//0=email,1=phone
    LoginViewModel(LoginRepository loginRepository) {
        this.loginRepository = loginRepository;
    }

    LiveData<LoginFormState> getLoginFormState() {
        return loginFormState;
    }

    LiveData<LoginResult> getLoginResult() {
        return loginResult;
    }

    public void login(String username, String password) {
        // can be launched in a separate asynchronous job
        Result<LoggedInUser> result = loginRepository.login(username, password);

        if (result instanceof Result.Success) {
            LoggedInUser data = ((Result.Success<LoggedInUser>) result).getData();
            //更改原model使登入用户模型包含获取到的cookie
            loginResult.setValue(new LoginResult(new LoggedInUserView(data.getUserId(),data.getDisplayName())));
        } else {
            loginResult.setValue(new LoginResult(R.string.login_failed));
        }
    }

    public void loginDataChanged(String username, String password) {
        if(type==0) {
            if (!isUserNameValid(username)) {
                loginFormState.setValue(new LoginFormState(R.string.invalid_username, null));
            } else if (!isPasswordValid(password)) {
                loginFormState.setValue(new LoginFormState(null, R.string.invalid_password));
            } else {
                loginFormState.setValue(new LoginFormState(true));
            }
        }else if(type==1){
            if(!Patterns.PHONE.matcher(username).matches()){
                loginFormState.setValue(new LoginFormState(R.string.invalid_phone,null));
            }
        }
    }

    // A placeholder username validation check
    private boolean isUserNameValid(String username) {
        if (username == null) {
            return false;
        }
        if (username.contains("@")) {
            return Patterns.EMAIL_ADDRESS.matcher(username).matches();
        } else {
            return !username.trim().isEmpty();
        }
    }

    // A placeholder password validation check
    //这里为了测试管理员的6位数pwd密码，先设定为length>5
    private boolean isPasswordValid(String password) {
        return password != null && password.trim().length() > 5;
    }

    public void setType(Integer type){
        this.type = type;
    }

}
