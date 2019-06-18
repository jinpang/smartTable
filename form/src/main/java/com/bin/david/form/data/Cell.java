package com.bin.david.form.data;

import com.bin.david.form.core.TableConfig;

/**
 * Created by huang on 2018/1/24.
 */

public class Cell {
    public static final int INVALID = -1;

    public int col;
    public int row;
    public Cell realCell;
    public int width;
    public int height;
    public Cell prevCell;
    public Cell nextCell;
    public int textColor = TableConfig.INVALID_COLOR;
    public CellRightTopCorner rightTopCorner;

    public Cell(int col, int row) {
        this.col = col;
        this.row = row;
        realCell = this;
    }

    public Cell(Cell realCell) {
        this.col = INVALID;
        this.row = INVALID;
        this.realCell = realCell;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Cell cell = (Cell) o;

        if (col != cell.col) return false;
        return row == cell.row;
    }

    @Override
    public int hashCode() {
        int result = col;
        result = 31 * result + row;
        return result;
    }

    public boolean isInCell(int row, int col){
        return this.row == row && this.col == col;
    }
}
