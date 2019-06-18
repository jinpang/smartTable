package com.bin.david.form.data;

import com.bin.david.form.core.TableConfig;

/**
 * Created by Administrator on 2019/2/12.
 */

public class CellRightTopCorner {
    public int rightTopCornerTextColor = TableConfig.INVALID_COLOR;
    public int rightTopCornerBackgroundColor = TableConfig.INVALID_COLOR;
    public String rightTopValue;

    public CellRightTopCorner() {
    }

    public CellRightTopCorner(int rightTopCornerColor, int rightTopCornerBackgroundColor, String rightTopValue) {
        this.rightTopCornerTextColor = rightTopCornerColor;
        this.rightTopCornerBackgroundColor = rightTopCornerBackgroundColor;
        this.rightTopValue = rightTopValue;
    }
}
