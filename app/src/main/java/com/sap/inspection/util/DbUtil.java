package com.sap.inspection.util;

import java.util.ArrayList;

public class DbUtil {

    public static int getColIndex(ArrayList<String> cols, String colKeyword) {
        return cols.indexOf(colKeyword);
    }
}
