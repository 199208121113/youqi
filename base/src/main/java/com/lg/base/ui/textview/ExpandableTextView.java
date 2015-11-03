package com.lg.base.ui.textview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.lg.base.R;
import com.lg.base.core.LogUtil;
import com.lg.base.utils.ScreenUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Xudafeng on 2015/6/10.
 */
public class ExpandableTextView extends TextView {
    private static final String ELLIPSIS = "  ...  ";
    private static final String TAG = ExpandableTextView.class.getSimpleName();

    private CharSequence originalText;
    private CharSequence trimmedText;
    private boolean trim = true;
    private int defaultLineCount = 3;
    private int drawableWidth = 0;
    private BufferType bufferType;
    public ExpandableTextView(Context context) {
        this(context, null);
    }

    public ExpandableTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ExpandableTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.ExpandableTextView, defStyleAttr, 0);
        defaultLineCount = a.getInteger(R.styleable.ExpandableTextView_defaultLineCount, 3);
        LogUtil.d(TAG, "defaultLineCount=" + defaultLineCount);
        a.recycle();
        setOnClickListener(deaultOnclickListener);
    }

    private OnClickListener deaultOnclickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            trim = !trim;
            setDisplasyText();
            requestFocusFromTouch();
            if(myOnClickListener != null){
                myOnClickListener.onClick(v);
            }
        }
    };

    OnClickListener myOnClickListener = null;

    public void setMyOnClickListener(OnClickListener myOnClickListener) {
        this.myOnClickListener = myOnClickListener;
    }

    private void setDisplasyText() {
        super.setText(getDisplayableText(),this.bufferType);
    }

    private CharSequence getDisplayableText() {
        return trim ? trimmedText : originalText;
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        originalText = text;
        trimmedText = getTrimTextByDefaultLineCount();
        this.bufferType = type;
        setDisplasyText();
    }


    private CharSequence getTrimTextByDefaultLineCount() {
        final int canvasWidth = getCanvasWidth();
        if(canvasWidth == 0){
            return originalText;
        }
        if(TextUtils.isEmpty(originalText))
            return "";
        if (getLineCountByOriginalText() <= defaultLineCount) {
            return originalText;
        }
        Drawable drawable = getContext().getResources().getDrawable(R.drawable.arrow_bottom_blue);
        drawableWidth = drawable.getIntrinsicWidth();
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());// 这里设置图片的大小
        ImageSpan span = new ImageSpan(drawable, ImageSpan.ALIGN_BASELINE);
        SpannableString spanStr = new SpannableString("p");
        spanStr.setSpan(span, 0, 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        return new SpannableStringBuilder(getTrimTextNoDrawable()).append(ELLIPSIS).append(spanStr);

    }

    private String getTrimTextNoDrawable() {
        final int canvasWidth = getCanvasWidth();
        Paint textPaint = getPaint();
        float offsetWidth = drawableWidth+10;
        float lastDrawableWidth = textPaint.measureText(ELLIPSIS) + drawableWidth+offsetWidth;
        LogUtil.d(TAG,"offsetWidth=["+offsetWidth+"],lastDrawableWidth=["+lastDrawableWidth+"]");
        StringBuilder sb = new StringBuilder();
        if (!TextUtils.isEmpty(originalText)) {
            List<String> paragraphList = createShowableList(originalText.toString());
            int i = 0;
            for (String str : paragraphList){
                int startIndex = 0;
                while (i < defaultLineCount) {
                    int drawWidth = canvasWidth;
                    if (i == defaultLineCount-1) {
                        drawWidth -= lastDrawableWidth;
                    }
                    int receivedLength = textPaint.breakText(str,startIndex,str.length(),true,drawWidth,null);
                    if(receivedLength == 0) {
                        break;
                    }
                    String ss = str.substring(startIndex,startIndex+receivedLength);
                    startIndex+=receivedLength;
                    sb.append(ss);
                    if(i < defaultLineCount-1){
                        sb.append("\n");
                    }
                    i++;
                    LogUtil.d(TAG, "i=[" + i + "],line=[" + ss + "],drawWidth=["+drawWidth+"]");
                    if(startIndex >= str.length() -1){
                        break ;
                    }
                }
                if(i>=defaultLineCount){
                    break;
                }
            }

        }
        return sb.toString();
    }


    // 获得原文的行数
    private int getLineCountByOriginalText() {
        if (TextUtils.isEmpty(originalText)) {
            return 0;
        }
        final int canvasWidth = getCanvasWidth();
        if(canvasWidth <= 0){
            return 0;
        }
        Paint textPaint = getPaint();
        /*float originalUseWidth = textPaint.measureText(originalText.toString());
        int originalLine = (int) (originalUseWidth / canvasWidth);
        if (originalUseWidth % canvasWidth != 0) {
            originalLine = originalLine + 1;
        }
        return originalLine;*/

        int lineCount = 0;
        List<String> paragraphList = createShowableList(originalText.toString());
        for (String str : paragraphList){
            int startIndex = 0;
            while (true) {
                int receivedLength = textPaint.breakText(str,startIndex,str.length(),true,canvasWidth,null);
                if(receivedLength == 0) {
                    break ;
                }
                startIndex+=receivedLength;
                lineCount++;
                if(startIndex == str.length() -1){
                    break ;
                }
            }
        }
        return lineCount;
    }
    private volatile int currentCanvasWidth = 0;
    @Override
    protected void onDraw(Canvas canvas) {
        if(currentCanvasWidth == 0 && canvas != null){
            try {
                currentCanvasWidth = canvas.getWidth()-this.getPaddingLeft()-this.getPaddingRight();
                if(currentCanvasWidth == 0){
                    currentCanvasWidth = ScreenUtil.getDisplay(getContext()).getWidth();
                }
                LogUtil.d(TAG,"currentCanvasWidth="+currentCanvasWidth);
                super.setText(getText());
            } catch (Exception e) {
                //ignore
            }
            return;
        }
        super.onDraw(canvas);
    }

    private int getCanvasWidth(){
        return currentCanvasWidth;
    }


    private ArrayList<String> createShowableList(String mChapterContent){
        if (mChapterContent == null || mChapterContent.trim().length() == 0) {
            mChapterContent = " ";
        }
        final String p_token = "##p#";
        ArrayList<String> mShowableList = new ArrayList<>();
        mChapterContent = mChapterContent.replaceAll("[(\n)?\r]", p_token);
        mChapterContent = mChapterContent.replaceAll("<[a-zA-Z]+[1-9]?[^><]*>", "");
        mChapterContent = mChapterContent.replaceAll("</([^>]*)>", "");// 结束标签
        String[] txtArray = mChapterContent.split(p_token);
        for (String txtP : txtArray) {
            mShowableList.add(txtP);
        }
        return mShowableList;
    }
}