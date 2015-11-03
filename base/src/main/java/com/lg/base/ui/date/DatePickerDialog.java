package com.lg.base.ui.date;

import android.app.AlertDialog;
import android.content.Context;

public class DatePickerDialog extends AlertDialog {

	public DatePickerDialog(Context context, boolean cancelable, OnCancelListener cancelListener,OnCellItemClickListener cellItemClickListener) {
		super(context, cancelable, cancelListener);
		this.cellItemClickListener = cellItemClickListener;
		init();
	}

	public DatePickerDialog(Context context, int theme,OnCellItemClickListener cellItemClickListener) {
		super(context, theme);
		this.cellItemClickListener = cellItemClickListener;
		init();
	}

	public DatePickerDialog(Context context,OnCellItemClickListener cellItemClickListener) {
		super(context);
		this.cellItemClickListener = cellItemClickListener;
		init();
	}
	
	private DatePicker datePicker = null;
	private OnCellItemClickListener cellItemClickListener = null;
	private void init(){
		datePicker = new DatePicker(getContext());
		datePicker.setOnCellItemClickListener(this.cellItemClickListener);
		this.setView(datePicker);
	}

	public void show(String title){
		this.setTitle(title);
		this.show();
	}
}
