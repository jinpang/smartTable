package com.bin.david.form.data.format.bg;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import com.bin.david.form.core.TableConfig;

/**
 * Created by huang on 2017/11/14.
 * 通用绘制Rect格子柱状背景绘制
 */

public abstract class BaseCellHistogramBackgroundFormat<T> implements ICellBackgroundFormat<T> {

    @Override
    public void drawBackground(Canvas canvas, Rect rect, T t, Paint paint) {
        int color = getBackGroundColor(t);
        float histogram = getHistogramValue(t);
        if (histogram <= 0){
            return;
        }
        if (histogram > 1) {
            histogram = 1;
        } else if (histogram < 0) {
            histogram = 0;
        }
        int width = rect.width();
        int height = rect.height();
        //Log.e("drawBackground", "width:" + width + ", height:" + height);
        int min = rect.width() > rect.height() ? rect.height() : rect.width();
        int padding = 0;
        if (min > 20){
            padding = 20/2;
            width = rect.width() - padding * 2;
            height = rect.height() - padding * 2;
        }else if (min > 2){
            padding = 2/2;
            width = rect.width() - padding * 2;
            height = rect.height() - padding * 2;
        }
        int w = (int) (width * histogram);
        int h = height;
        //Log.e("drawBackground", "w:" + w + ", h:" + h + ", histogram:" + histogram + ", padding:" + padding);
        if (color != TableConfig.INVALID_COLOR && w > 0) {
            Rect r = new Rect();
            r.set(rect.left + padding, rect.top + padding, rect.left + padding + w, rect.top + padding + h);
            paint.setColor(color);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawRect(r, paint);
        }
    }

    /**
     * 获取背景颜色
     */
    public abstract int getBackGroundColor(T t);

    /**
     * 获取柱状图的数值，取值范围：【0-1】
     */
    public abstract float getHistogramValue(T t);

    /**
     * 默认字体颜色不跟随背景变化，
     * 当有需要多种字体颜色，请重写该方法
     *
     * @param t
     * @return
     */
    @Override
    public int getTextColor(T t) {
        return TableConfig.INVALID_COLOR;
    }
}
