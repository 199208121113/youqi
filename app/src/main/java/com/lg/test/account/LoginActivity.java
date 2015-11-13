package com.lg.test.account;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.lg.base.account.AccountLoginActivity;
import com.lg.base.core.InjectManager;
import com.lg.base.core.InjectView;
import com.lg.base.utils.StringUtil;
import com.lg.test.R;


/**
 * 用户登录
 * Created by liguo on 2015/10/14.
 */
public class LoginActivity extends AccountLoginActivity implements View.OnClickListener{

    @InjectView(R.id.act_login_et_user_name)
    EditText etUserName;

    @InjectView(R.id.act_login_et_pwd)
    EditText etPassword;

    @InjectView(R.id.act_login_btn_ok)
    Button btnOK;

    @InjectView(value = R.id.act_login_action_bar_icon_left,click = "onBack")
    View backView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        InjectManager.init(this);
        btnOK.setOnClickListener(this);
        backView.setBackgroundResource(R.drawable.sl_back_bg);
    }

    @Override
    public void onClick(View v) {
        String username = etUserName.getText().toString().trim();
        String pwd = etPassword.getText().toString().trim();
        if(StringUtil.isEmpty(username) || StringUtil.isEmpty(pwd)){
            return;
        }
        new UserLoginTask(this,username,pwd){
            @Override
            protected void onSuccess(Boolean aBoolean) {
                super.onSuccess(aBoolean);
                onLoginSuccess(this.getUsername(), this.getPwd());
                finish();
            }
        }.execute();
    }

    public void onBack(View v){
        finish();
    }
}
