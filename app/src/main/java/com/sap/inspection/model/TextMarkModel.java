package com.sap.inspection.model;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Parcel;

import com.sap.inspection.util.ImageUtil;

/**
 * Created by domikado on 3/16/18.
 */

public class TextMarkModel extends BaseModel {
    private static final String TAG = TextMarkModel.class.getSimpleName();
    private Context context;
    private String textContent;
    private Rect textRect;
    private Paint textPaint;
    private Typeface textStyleApplied;
    private TextMarkDisplayOptionsModel defaultTexkMarkOptions;
    private static volatile TextMarkModel instance;

    public static TextMarkModel getInstance() {
        if(instance == null) {
            Class var0 = TextMarkModel.class;
            synchronized(TextMarkModel.class) {
                if(instance == null) {
                    instance = new TextMarkModel();
                }
            }
        }
        return instance;
    }

    protected TextMarkModel(){

    }

    public synchronized void init(TextMarkDisplayOptionsModel textOptions) {
        if (textOptions == null) {
            throw new IllegalArgumentException("text option can't be null");
        } else {
            this.defaultTexkMarkOptions = textOptions;
        }
        this.context = this.defaultTexkMarkOptions.getContext();
    }

    public synchronized void setTextMark(String sourceText) {
        if (sourceText == null) {
            throw new IllegalArgumentException("source text can't be null");
        } else {

            this.textContent = sourceText;
        }
    }

    public String getTextMark() {
        return this.textContent;
    }

    public Paint generateTextPaint() {
        this.textStyleApplied =
                Typeface.create(defaultTexkMarkOptions.getTextFamilyName(), defaultTexkMarkOptions.getTextStyle());
        this.textPaint = new Paint();
        textPaint.setTypeface(this.textStyleApplied);
        textPaint.setStyle(defaultTexkMarkOptions.gettextColorStyle());
        textPaint.setColor(defaultTexkMarkOptions.getTextColor());
        textPaint.setTextAlign(defaultTexkMarkOptions.getTextAlign());
        textPaint.setTextSize(ImageUtil.convertToPixels(this.context, defaultTexkMarkOptions.getTextSize()));
        textRect = new Rect();
        textPaint.getTextBounds(this.textContent, 0, this.textContent.length(), textRect);
        return textPaint;
    }

    public Rect getTextRect() {
        return this.textRect;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }
}
