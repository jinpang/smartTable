package com.bin.david.form.data;

import com.bin.david.form.data.column.Column;

/**
 * Created by huang on 2017/11/15.
 * 单元格数据
 */

public class CellInfo<T> {
    /**
     * 数据
     */
    public T data;
    /**
     * 所在行位置
     */
    public int row;
    /**
     * 所在列位置
     */
    public int col;

    /**
     * 所在列
     */
    public Column<T> column;
    /**
     * 显示的值
     */
    public String value;

    public Cell trendPoint;
    public Cell textColorPoint;
    public Cell rightTopPoint;
    public Cell textColorPoint2;
    public Cell histogramCell;

    public void set(Column<T> column, T t, String value, int col, int row) {
        set(column, t, value, col, row, trendPoint, textColorPoint);
    }

    public void set(Column<T> column, T t, String value, int col, int row, Cell trendPoint, Cell textColorPoint) {
        set(column, t, value, col, row, trendPoint, textColorPoint, rightTopPoint);
    }

    public void set(Column<T> column, T t, String value, int col, int row, Cell trendPoint, Cell textColorPoint, Cell rightTopPoint) {
        set(column, t, value, col, row, trendPoint, textColorPoint, rightTopPoint, textColorPoint2);
    }

    public void set(Column<T> column, T t, String value, int col, int row, Cell trendPoint, Cell textColorPoint, Cell rightTopPoint, Cell textColorPoint2) {
        set(column, t, value, col, row, trendPoint, textColorPoint, rightTopPoint, textColorPoint2, histogramCell);
    }

    public void set(Column<T> column, T t, String value, int col, int row, Cell trendPoint, Cell textColorPoint, Cell rightTopPoint, Cell textColorPoint2, Cell histogramCell) {
        this.column = column;
        this.value= value;
        this.data = t;
        this.row = row;
        this.col = col;
        this.trendPoint = trendPoint;
        this.textColorPoint = textColorPoint;
        this.rightTopPoint = rightTopPoint;
        this.textColorPoint2 = textColorPoint2;
        this.histogramCell = histogramCell;
    }

    public void setTrendPoint(Cell trendPoint) {
        this.trendPoint = trendPoint;
    }

    public void setTextColorPoint(Cell textColorPoint) {
        this.textColorPoint = textColorPoint;
    }
}
