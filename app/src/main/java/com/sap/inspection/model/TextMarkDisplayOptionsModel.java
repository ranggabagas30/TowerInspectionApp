package com.sap.inspection.model;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;

/**
 * Created by domikado on 3/15/18.
 */

public class TextMarkDisplayOptionsModel extends BaseModel {

    private Context context;
    private Paint.Style textColorStyle;
    private Paint.Align textAlign;
    private String textFamilyName;
    private int textColor;
    private int textStyle;
    private int textSize;

    private TextMarkDisplayOptionsModel(TextMarkDisplayOptionsModel.Builder builder) {
        this.context = builder.context;
        this.textColorStyle = builder.textColorStyle;
        this.textAlign = builder.textAlign;
        this.textFamilyName = builder.textFamilyName;
        this.textColor = builder.textColor;
        this.textStyle = builder.textStyle;
        this.textSize = builder.textSize;
    }

    protected Context getContext() {
        return this.context;
    }

    public Paint.Style getTextColorStyle() {
        return textColorStyle;
    }

    public int getTextColor() {
        return textColor;
    }

    public Paint.Align getTextAlign() {
        return textAlign;
    }

    public String getTextFamilyName() {
        return textFamilyName;
    }

    public int getTextStyle() {
        return textStyle;
    }

    public int getTextSize() {
        return textSize;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void setTextColorStyle(Paint.Style textColorStyle) {
        this.textColorStyle = textColorStyle;
    }

    public void setTextAlign(Paint.Align textAlign) {
        this.textAlign = textAlign;
    }

    public void setTextFamilyName(String textFamilyName) {
        this.textFamilyName = textFamilyName;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public void setTextStyle(int textStyle) {
        this.textStyle = textStyle;
    }

    public void setTextSize(int textSize) {
        this.textSize = textSize;
    }

    public static TextMarkDisplayOptionsModel createSimple(Context context){
        return (new TextMarkDisplayOptionsModel.Builder(context)).build();
    }

    public static class Builder{
        private Context context;
        private Paint.Style textColorStyle;
        private Paint.Align textAlign;
        private String textFamilyName;
        private int textColor;
        private int textStyle;
        private int textSize;

        public Builder(Context context){
            this.context = context.getApplicationContext();
            this.textColorStyle = Paint.Style.FILL;
            this.textAlign = Paint.Align.CENTER;
            this.textFamilyName = "Helvetica";
            this.textColor = Color.WHITE;
            this.textStyle = Typeface.BOLD;
            this.textSize = 20;
        }

        public TextMarkDisplayOptionsModel.Builder setTextColorStyle(Paint.Style textColorStyle) {
            this.textColorStyle = textColorStyle;
            return this;
        }

        public TextMarkDisplayOptionsModel.Builder setTextAlign(Paint.Align textAlign) {
            this.textAlign = textAlign;
            return this;
        }

        public TextMarkDisplayOptionsModel.Builder setTextFamilyName(String textFamilyName) {
            this.textFamilyName = textFamilyName;
            return this;
        }

        public TextMarkDisplayOptionsModel.Builder setTextColor(int textColor) {
            this.textColor = textColor;
            return this;
        }

        public TextMarkDisplayOptionsModel.Builder setTextStyle(int textStyle) {
            this.textStyle = textStyle;
            return this;
        }

        public TextMarkDisplayOptionsModel.Builder setTextSize(int textSize) {
            this.textSize = textSize;
            return this;
        }

        public TextMarkDisplayOptionsModel build() {
            return new TextMarkDisplayOptionsModel(this);
        }
    }

}
