package com.lg.test.task;

import com.lg.base.bus.LogUtil;
import com.lg.base.core.BaseAsyncTask;
import com.lg.test.core.DBHelper;
import com.lg.test.greendao.Note;
import com.lg.test.greendao.NoteDao;

import java.util.Calendar;
import java.util.List;

/**
 * Created by liguo on 2015/10/15.
 */
public class NoteAddTask extends BaseAsyncTask<Boolean> {
    private String text;
    private String TAG = "NoteAddTask";
    public NoteAddTask(String text) {
        super();
        this.text = text;
    }

    @Override
    protected Boolean run() throws Exception {
        NoteDao noteDao = DBHelper.get().getDaoSession().getNoteDao();

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
