package com.lg.base.ui.imageview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.lg.base.R;

public class RoundedImageView extends ImageView {

	public RoundedImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs, defStyle);
	}

	public RoundedImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context,attrs,0);
	}

	public RoundedImageView(Context context) {
		super(context);
		init(context,null,0);
	}

	private final RectF roundRect = new RectF();
	private float rect_adius = 4;
	private final Paint maskPaint = new Paint();
	private final Paint zonePaint = new Paint();

	private void init(Context ctx,AttributeSet attrs,int defStyle) {
		maskPaint.setAntiAlias(true);
		maskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
		zonePaint.setAntiAlias(true);
		zonePaint.setColor(Color.WHITE);
		float density = getResources().getDisplayMetrics().density;
		if(attrs != null){
			try {
				TypedArray a = ctx.obtainStyledAttributes(attrs, R.styleable.RoundedImageView, defStyle, 0);
				rect_adius = a.getDimensionPixelSize(R.styleable.RoundedImageView_corner_radius, 2);
				a.recycle();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		rect_adius = rect_adius * density;
	}

	public void setRectAdius(float adius) {
		rect_adius = adius;
		invalidate();
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		int w = getWidth();
		int h = getHeight();
		roundRect.set(0, 0, w, h);
	}

	@Override
	public void draw(Canvas canvas) {
		canvas.saveLayer(roundRect, zonePaint, Canvas.ALL_SAVE_FLAG);
		canvas.drawRoundRect(roundRect, rect_adius, rect_adius, zonePaint);
		canvas.saveLayer(roundRect, maskPaint, Canvas.ALL_SAVE_FLAG);
		super.draw(canvas);
		canvas.restore();
	}

}
