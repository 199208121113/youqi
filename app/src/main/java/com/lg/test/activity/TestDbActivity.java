package com.lg.test.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

import com.lg.base.core.ActionBarMenu;
import com.lg.base.utils.StringUtil;
import com.lg.base.utils.ToastUtil;
import com.lg.test.R;
import com.lg.test.core.SuperActivity;
import com.lg.test.task.NoteAddTask;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Created by liguo on 2015/10/14.
 */
public class TestDbActivity extends SuperActivity {

    @Bind(R.id.act_note_add_text)
    EditText actNoteAddText;

    public static Intent createIntent(Context ctx) {
        Intent it = new Intent(ctx, TestDbActivity.class);
        return it;
    }

    @Override
    protected ActionBarMenu onActionBarCreate() {
        return new ActionBarMenu("数据库测试");
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_note_add;
    }

    @OnClick(R.id.act_note_add_btn_ok)
    public void onClick() {
        String text = actNoteAddText.getText().toString().trim();
        if (StringUtil.isEmpty(text)) {
            return;
        }

        new NoteAddTask(text) {
            @Override
            protected void onSuccess(Boolean result) {
                super.onSuccess(result);
                ToastUtil.show("笔记添加成功!");
            }
        }.execute();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
