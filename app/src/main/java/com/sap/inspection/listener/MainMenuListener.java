package com.sap.inspection.listener;

public interface MainMenuListener {
	int CORRECTIVE = 0;
	int PREVENTIVE = 1;
	int NEW_LOCATION = 2;
	int COLOCATION = 3;
	int FIBEROPTIK = 4;
	void changeFrameRight(int fragmentId);
}
