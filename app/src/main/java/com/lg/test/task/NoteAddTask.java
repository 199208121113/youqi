package com.lg.test.task;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;

import com.lg.base.core.BaseRoboAsyncTask;
import com.lg.base.core.LogUtil;
import com.lg.test.greendao.DaoMaster;
import com.lg.test.greendao.DaoSession;
import com.lg.test.greendao.Note;
import com.lg.test.greendao.NoteDao;

import java.util.Calendar;
import java.util.List;

/**
 * Created by liguo on 2015/10/15.
 */
public class NoteAddTask extends BaseRoboAsyncTask<Boolean> {
    private String text;
    private String TAG = "NoteAddTask";
    public NoteAddTask(Activity context, String text) {
        super(context);
        this.text = text;
    }

    @Override
    protected Boolean run() throws Exception {
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(getWeakActivity(), "notes-db", null);
        SQLiteDatabase db = helper.getWritableDatabase();
        // 注意：该数据库连接属于 DaoMaster，所以多个 Session 指的是相同的数据库连接。
        DaoMaster daoMaster = new DaoMaster(db);
        DaoSession daoSession = daoMaster.newSession();
        NoteDao noteDao = daoSession.getNoteDao();

        Note note = new Note();
        note.setText(text);
        note.setDate(Calendar.getInstance().getTime());
        long newId = noteDao.insert(note);
        LogUtil.d(TAG, "newId=" + newId);

        List<Note> noteList = noteDao.loadAll();
        for(Note nt : noteList){
            LogUtil.e(TAG,"nt="+nt.getText());
        }
        return true;
    }
}
