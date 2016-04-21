package com.lg.test.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.lg.base.core.InjectView;
import com.lg.base.utils.AES256Cipher;
import com.lg.base.utils.StringUtil;
import com.lg.test.R;
import com.lg.test.core.SuperActivity;

/**
 * Created by liguo on 2015/11/24.
 */
public class TestEncodeActivity extends SuperActivity {

    public static final String AES_SEED_Key = ")O[NB]6,YF}+efcaj{+oESb9d8>Z'e9M";

    public static Intent createIntent(Context ctx){
        Intent it = new Intent(ctx,TestEncodeActivity.class);
        return it;
    }

    @Override
    protected ActionBarMenu onActionBarCreate() {
        return new ActionBarMenu("加密测试");
    }

    @InjectView(R.id.act_encode_et_content)
    EditText etSource;

    @InjectView(value = R.id.act_encode_btn_ok,click = "onClick")
    View btnEncode;

    @InjectView(R.id.act_encode_et_result)
    EditText tvResult;

    @Override
    protected int getContentView() {
        return R.layout.activity_encode;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void onClick(View view){
        if(view == btnEncode){
            String text = etSource.getText().toString();
            if(StringUtil.isEmpty(text)){
                return;
            }
            String result = AES256Cipher.AES_Encode(text,AES_SEED_Key);
            tvResult.setText(result);
        }
    }
}
