package com.lg.test.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.lg.base.core.ActionBarMenu;
import com.lg.base.core.InjectView;
import com.lg.base.utils.StringUtil;
import com.lg.test.R;
import com.lg.test.core.SuperActivity;
import com.lg.test.task.NoteAddTask;

/**
 * Created by liguo on 2015/10/14.
 */
public class TestDbActivity extends SuperActivity{

    @InjectView(R.id.act_note_add_text)
    EditText etNoteText;

    @InjectView(value = R.id.act_note_add_btn_ok,click = "onClick")
    Button btnAdd;

    public static Intent createIntent(Context ctx){
        Intent it = new Intent(ctx,TestDbActivity.class);
        return it;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected ActionBarMenu onActionBarCreate() {
        return new ActionBarMenu("数据库测试");
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_note_add;
    }

    public void onClick(View v) {
        if(v == btnAdd){
            String text = etNoteText.getText().toString().trim();
            if(StringUtil.isEmpty(text)){
                return;
            }
            new NoteAddTask(this,text){}.execute();
        }
    }
}
