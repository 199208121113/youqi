package com.lg.test.db;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.lg.base.core.BaseActivity;
import com.lg.base.core.InjectView;
import com.lg.base.utils.StringUtil;
import com.lg.test.R;

/**
 * Created by liguo on 2015/10/14.
 */
public class UserOpActivity extends BaseActivity implements View.OnClickListener{

    @InjectView(R.id.act_user_op_user_name)
    EditText etUserName;

    @InjectView(R.id.act_user_op_et_pwd)
    EditText etPassword;

    @InjectView(R.id.act_user_op_add_btn_ok)
    Button btnAdd;

    public static Intent createIntent(Context ctx){
        Intent it = new Intent(ctx,UserOpActivity.class);
        return it;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        btnAdd.setOnClickListener(this);
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_user_op;
    }

    @Override
    public void onClick(View v) {

        if(v == btnAdd){
            String username = etUserName.getText().toString().trim();
            String pwd = etPassword.getText().toString().trim();
            if(StringUtil.isEmpty(username) || StringUtil.isEmpty(pwd)){
                return;
            }
            new UserOpTask(this,username,pwd){}.execute();
        }
    }
}
