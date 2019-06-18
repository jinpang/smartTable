package com.bin.david.form.data.format.bg;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import com.bin.david.form.core.TableConfig;

/**
 * Created by huang on 2017/11/14.
 * 通用绘制Rect格子背景绘制
 */

public abstract class BaseCellTrendCirclePointBackgroundFormat<T> implements ICellBackgroundFormat<T> {

    @Override
    public void drawBackground(Canvas canvas, Rect rect, T t, Paint paint) {
        int color = getBackGroundColor(t);
        int width = rect.width();
        int height = rect.height();
        float radius = width > height ? height * 0.45f : width * 0.45f;
        if (color != TableConfig.INVALID_COLOR) {
            paint.setColor(color);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(rect.centerX(), rect.centerY(), radius, paint);
        }
       /* int colorCorner = getCornerBackGroundColor(t);
        if (colorCorner != TableConfig.INVALID_COLOR) {
            paint.setColor(colorCorner);
            paint.setStyle(Paint.Style.FILL);
            float cornerRadius = width > height ? height * 0.25f : width * 0.25f;
            float delta = (float) (radius * Math.sin(45));
            canvas.drawCircle(rect.centerX() + delta, rect.centerY() - delta, cornerRadius, paint);
        }*/
    }

    /**
     * 获取背景颜色
     */
    public abstract int getBackGroundColor(T t);

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

    /**
     * 获取右上角背景颜色
     */
    public abstract int getCornerBackGroundColor(T t);

}
