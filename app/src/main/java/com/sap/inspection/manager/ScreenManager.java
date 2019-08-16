package com.sap.inspection.manager;

//import android.util.Log;

public class ScreenManager {

	private static ScreenManager instance;
	private int scrHeight = 0;
	private int scrWidth = 0;
	private int timeLineHeight = 0;

	private ScreenManager() {
	}

	public static ScreenManager getInstance() {
		if (instance == null) {
			instance = new ScreenManager();
		}
		return instance;
	}

	public void setHeight(int height) {
		if (this.scrHeight == 0)
			this.scrHeight = height;
	}

	public int getScreenHeight() {
		return this.scrHeight;
	}

	public void setWidth(int width) {
		if (this.scrWidth == 0)
			this.scrWidth = width;
	}

	public int getScreenWidth() {
		return this.scrWidth;
	}

	public Boolean isPotrait() {
        return scrHeight > scrWidth;
    }

	public int getMin() {
		return Math.min(scrHeight, scrWidth);
	}

	public int getMax() {
		return Math.max(scrHeight, scrWidth);
	}

	public int getPadding() {
		return getMin() / 50;
	}

	public int getPaddingFrame() {
		return getMin() / 100;
	}

	public int getButtonWidth() {
		// return getMin()/6;
		return getMainMenuButtonHeight();
	}

	public int getTimelineButtonWidth() {
		return getMin() / 9;
	}

	public int getUserTimeLineImageWidth() {
		return getMin() / 6;
	}

	public int getRestSize() {
		return getScreenWidth() - getButtonWidth();
	}

	public int getPaddingPage() {
		return getMin() * 18 / 640;
	}

	public int getMainMenuSizeWidth() {
		return getScreenWidth() * 520 / 640;
	}

	public int getMainMenuRest() {
		return getScreenWidth() - getMainMenuSizeWidth();
	}

	public int getMainMenuOver() {
		return getScreenWidth() * 16 / 640;
	}

	public int getMainMenuButtonHeight() {
		return getMax() * 88 / 960;
	}

	public int getMainMenuButtonIconHeight() {
		return getMax() * 58 / 960;
	}

	public int getMainMenuButtonIconWidth() {
		return getMin() * 82 / 640;
	}

	public int getMainMenuIconPaddingLeftRight() {
		return getMin() * 25 / 640;
	}

	public int getMainMenuSatuTempatLogoAndSearchHeight() {
		return getScreenHeight() * 60 / 960;
	}

	public int getMainMenuSearchWidth() {
		return getScreenWidth() * 490 / 640;
	}

	public int getMainMenuSearchIconWidth() {
		return getScreenWidth() * 81 / 640;
	}

	// ============================= Timeline =============================
	public int getTimeLineHeight(int timeLineWidth) {
		return timeLineWidth * 3 / 4;
	}

	public int getTimeLineUser() {
		return getConversionMin(64);
	}

	public int getTimeLinePad18() {
		return getConversionMin(18);
	}

	public int getTimeLineContentIconSize(){
		return getConversionMin(16) *3;
	}
	
	public int getTimeLineProductWidth() {
		return getMin() - getTimeLineUser() - 2 * getTimeLinePad18() - getConversionMin(11); 
	}

	
	// others
	
	public int getButtonHeight(){
		return getMainMenuButtonIconHeight();
	}
	
	public int getConversionMin(int pixel) {
		return getMin() * pixel / 640;
	}

	public int getConversionMax(int pixel) {
		return getMax() * pixel / 960;
	}

}