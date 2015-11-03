package com.lg.base.ui.date;

public class CellStatus {
	
	/** 是否可选择 */
	private boolean enable = true;
	
	/** 是否是当前日期 */
	private boolean isCurrentDate = false;
	
	/** 是否选中 */
	private boolean checked = false;

	public boolean isEnable() {
		return enable;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}

	public boolean isCurrentDate() {
		return isCurrentDate;
	}

	public void setCurrentDate(boolean isCurrentDate) {
		this.isCurrentDate = isCurrentDate;
	}

	public boolean isChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
	}

	public CellStatus(boolean enable) {
		super();
		this.enable = enable;
	}
}
