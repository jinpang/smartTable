package com.bin.david.form.data.format.draw;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import com.bin.david.form.core.TableConfig;
import com.bin.david.form.data.CellInfo;
import com.bin.david.form.data.column.Column;
import com.bin.david.form.core.TableConfig;
import com.bin.david.form.data.format.bg.ICellBackgroundFormat;
import com.bin.david.form.utils.DrawUtils;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by huang on 2017/10/30.
 */

public class TextDrawFormat<T> implements IDrawFormat<T> {


    private Map<String,SoftReference<String[]>> valueMap; //避免产生大量对象

    public TextDrawFormat() {
        valueMap = new HashMap<>();
    }

    @Override
    public int measureWidth(Column<T>column, int position, TableConfig config) {

        Paint paint = config.getPaint();
        config.getContentStyle().fillPaint(paint);
        return DrawUtils.getMultiTextWidth(paint,getSplitString(column.format(position)));
    }


    @Override
    public int measureHeight(Column<T> column,int position, TableConfig config) {
        Paint paint = config.getPaint();
        config.getContentStyle().fillPaint(paint);
        return DrawUtils.getMultiTextHeight(paint,getSplitString(column.format(position)));
    }

    @Override
    public void draw(Canvas c,Rect rect, CellInfo<T> cellInfo, TableConfig config) {
        Paint paint = config.getPaint();
        setTextPaint(config,cellInfo, paint);
        if(cellInfo.column.getTextAlign() !=null) {
            paint.setTextAlign(cellInfo.column.getTextAlign());
        }
        drawText(c, cellInfo.value, rect, paint);
    }

    protected void drawText(Canvas c, String value, Rect rect, Paint paint) {
        DrawUtils.drawMultiText(c,paint,rect,getSplitString(value));
    }

    protected void drawCornerText(Canvas c, String value, Rect rect, Paint paint) {
        int oldColor = paint.getColor();
        int width = rect.width();
        int height = rect.height();
        int colorCorner = Color.parseColor("#0000FF");
        float radius = width > height ? height * 0.45f : width * 0.45f;
        if (colorCorner != TableConfig.INVALID_COLOR) {
            paint.setColor(colorCorner);
            paint.setStyle(Paint.Style.FILL);
            float cornerRadius = width > height ? height * 0.25f : width * 0.25f;
            float delta = (float) (radius * Math.sin(45*Math.PI/180));
            float cx = rect.centerX() + delta;
            float cy = rect.centerY() - delta;
            c.drawCircle(rect.centerX() + delta, rect.centerY() - delta, cornerRadius, paint);
            paint.setColor(oldColor);
            Rect cornerRect = new Rect((int) (cx - cornerRadius), (int) (cy - cornerRadius), (int) (cx + cornerRadius), (int) (cy + cornerRadius));
            DrawUtils.drawSingleText(c, paint, cornerRect, value);
        }
    }


    public void setTextPaint(TableConfig config,CellInfo<T> cellInfo, Paint paint) {
        config.getContentStyle().fillPaint(paint);
        ICellBackgroundFormat<CellInfo> backgroundFormat = config.getContentCellBackgroundFormat();
        if(backgroundFormat!=null && backgroundFormat.getTextColor(cellInfo) != TableConfig.INVALID_COLOR){
            paint.setColor(backgroundFormat.getTextColor(cellInfo));
        }
        if (cellInfo.textColorPoint != null && config.getPointListTextColor() != TableConfig.INVALID_COLOR) {
            paint.setColor(config.getPointListTextColor());
        }
        if (cellInfo.textColorPoint2 != null && config.getPointListTextColor2() != TableConfig.INVALID_COLOR) {
            paint.setColor(config.getPointListTextColor2());
        }
        if (cellInfo.trendPoint != null && config.getTrendPointTextColor() != TableConfig.INVALID_COLOR) {
            paint.setColor(config.getTrendPointTextColor());
        }
        paint.setTextSize(paint.getTextSize()*config.getZoom());

    }

    protected String[] getSplitString(String val){
        String[] values = null;
        if(valueMap.get(val)!=null){
            values= valueMap.get(val).get();
        }
        if(values == null){
            values = val.split("\n");

            valueMap.put(val, new SoftReference<>(values));
        }
        return values;
    }
}
