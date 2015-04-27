package com.sap.inspection.listener;

public interface MainMenuListener {
	public static int CORRECTIVE = 0;
	public static int PREVENTIVE = 1;
	public static int NEW_LOCATION = 2;
	public static int COLOCATION = 3;
	public void changeFrameRight(int fragmentId);
}
