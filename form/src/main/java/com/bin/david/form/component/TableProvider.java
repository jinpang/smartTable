package com.bin.david.form.component;


import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;

import com.bin.david.form.core.TableConfig;
import com.bin.david.form.data.Cell;
import com.bin.david.form.data.CellInfo;
import com.bin.david.form.data.CellRightTopCorner;
import com.bin.david.form.data.TableInfo;
import com.bin.david.form.data.column.Column;
import com.bin.david.form.data.column.ColumnInfo;
import com.bin.david.form.data.TableInfo;
import com.bin.david.form.data.format.bg.ICellBackgroundFormat;
import com.bin.david.form.data.format.selected.IDrawOver;
import com.bin.david.form.data.format.selected.ISelectFormat;
import com.bin.david.form.data.format.tip.ITip;
import com.bin.david.form.data.table.TableData;
import com.bin.david.form.listener.OnColumnClickListener;
import com.bin.david.form.listener.TableClickObserver;
import com.bin.david.form.utils.DrawUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huang on 2017/11/1.
 * 表格内容绘制
 */

public class TableProvider<T> implements TableClickObserver {


    private Rect scaleRect;
    private Rect showRect;
    private TableConfig config;
    private PointF clickPoint;
    private ColumnInfo clickColumnInfo;
    private boolean isClickPoint;
    private OnColumnClickListener onColumnClickListener;
    /**
     * 选中格子格式化
     */
    private SelectionOperation operation;
    private TableData<T> tableData;
    private ITip<Column, ?> tip;
    private Rect clipRect;
    private Rect tempRect; //用于存储数据
    private Column tipColumn;
    private int tipPosition;
    private GridDrawer<T> gridDrawer;
    private PointF tipPoint = new PointF();
    private IDrawOver drawOver;
    private CellInfo cellInfo = new CellInfo();
    private List<CellInfo> trendCellInfoList = new ArrayList<>();
    private List<Rect> trendRectList = new ArrayList<>();

    public TableProvider() {

        clickPoint = new PointF(-1, -1);
        clipRect = new Rect();
        tempRect  = new Rect();
        operation = new SelectionOperation();
        gridDrawer  = new GridDrawer<>();
    }

    /**
     * 绘制
     * @param canvas 画布
     * @param scaleRect 缩放Rect
     * @param showRect 显示Rect
     * @param tableData 表格数据
     * @param config 配置
     */
    public void onDraw(Canvas canvas, Rect scaleRect, Rect showRect,
                       TableData<T> tableData, TableConfig config) {
        setData(scaleRect, showRect, tableData, config);
        canvas.save();
        canvas.clipRect(this.showRect);
        drawColumnTitle(canvas, config);
        drawCount(canvas);
        drawContent(canvas);
        operation.draw(canvas,showRect,config);
        if(drawOver !=null)
            drawOver.draw(canvas,scaleRect,showRect,config);
        canvas.restore();
        if (isClickPoint && clickColumnInfo != null) {
            onColumnClickListener.onClick(clickColumnInfo);
        }
        if (tipColumn != null) {
            drawTip(canvas, tipPoint.x, tipPoint.y, tipColumn, tipPosition);
        }
    }

    /**
     * 设置基本信息和清除数据
     * @param scaleRect 缩放Rect
     * @param showRect 显示Rect
     * @param tableData 表格数据
     * @param config 配置
     */
    private void setData(Rect scaleRect, Rect showRect, TableData<T> tableData, TableConfig config) {
        isClickPoint = false;
        clickColumnInfo = null;
        tipColumn = null;
        operation.reset();
        this.scaleRect = scaleRect;
        this.showRect = showRect;
        this.config = config;
        this.tableData = tableData;
        gridDrawer.setTableData(tableData);
    }


    private void drawColumnTitle(Canvas canvas, TableConfig config) {
        if(config.isShowColumnTitle()) {
            if (config.isFixedTitle()) {
                drawTitle(canvas);
                canvas.restore();
                canvas.save();
                canvas.clipRect(this.showRect);
            } else {
                drawTitle(canvas);
            }
        }
    }

    /**
     * 绘制统计行
     * @param canvas 画布
     */
    private void drawCount(Canvas canvas) {
        if (tableData.isShowCount()) {
            float left = scaleRect.left;
            float bottom = config.isFixedCountRow() ? Math.min(scaleRect.bottom,showRect.bottom) : scaleRect.bottom;
            int countHeight = tableData.getTableInfo().getCountHeight();
            float top = bottom - countHeight;
            if(config.getCountBackground() != null){
                tempRect.set((int)left, (int)top, showRect.right,(int)bottom);
                config.getCountBackground().drawBackground(canvas,tempRect,config.getPaint());
            }
            List<ColumnInfo> childColumnInfos = tableData.getChildColumnInfos();
            if (DrawUtils.isVerticalMixRect(showRect, (int)top, (int)bottom)) {
                List<Column> columns = tableData.getChildColumns();
                int columnSize = columns.size();
                boolean isPerColumnFixed = false;
                clipRect.set(showRect);
                int clipCount = 0;
                for (int i = 0; i < columnSize; i++) {
                    Column column = columns.get(i);
                    float tempLeft = left;
                    float width = column.getComputeWidth()*config.getZoom();
                    if(childColumnInfos.get(i).getTopParent().column.isFixed()){
                        if(left < clipRect.left) {
                            left = clipRect.left;
                            clipRect.left += width;
                            isPerColumnFixed = true;
                        }
                    }else if(isPerColumnFixed){
                        canvas.save();
                        clipCount++;
                        canvas.clipRect(clipRect.left, showRect.bottom - countHeight,
                                showRect.right, showRect.bottom);
                    }
                    tempRect.set((int)left, (int)top, (int)(left+width), (int)bottom);
                    drawCountText(canvas, column,i,tempRect, column.getTotalNumString(), config);
                    left = tempLeft;
                    left +=width;
                }
                for(int i = 0;i < clipCount;i++){
                    canvas.restore();
                }
            }
        }
    }

    /**
     * 绘制列标题
     * @param canvas 画布
     */
    private void drawTitle(Canvas canvas) {
        int dis = showRect.top - scaleRect.top;
        TableInfo tableInfo = tableData.getTableInfo();
        int titleHeight = tableInfo.getTitleHeight() * tableInfo.getMaxLevel();
        int clipHeight = config.isFixedTitle() ? titleHeight : Math.max(0, titleHeight - dis);
        if(config.getColumnTitleBackground() !=null){
            tempRect.set(showRect.left, showRect.top, showRect.right,
                    showRect.top + clipHeight);
            config.getColumnTitleBackground().drawBackground(canvas,tempRect,config.getPaint());
        }
        clipRect.set(showRect);
        List<ColumnInfo> columnInfoList = tableData.getColumnInfos();
        float zoom = config.getZoom();
        boolean isPerColumnFixed = false;
        int clipCount = 0;
        ColumnInfo parentColumnInfo = null;
        for (ColumnInfo info : columnInfoList) {
            int left = (int) (info.left*zoom + scaleRect.left);
            //根据top ==0是根部，根据最根部的Title判断是否需要固定
            if (info.top == 0 && info.column.isFixed()) {
                if (left < clipRect.left) {
                    parentColumnInfo = info;
                    left = clipRect.left;
                    fillColumnTitle(canvas, info, left);
                    clipRect.left += info.width * zoom;
                    isPerColumnFixed = true;
                    continue;
                }
                //根部需要固定，同时固定所有子类
            }else if(isPerColumnFixed && info.top != 0){
                    left = (int) (clipRect.left - info.width * zoom);
                    left += (info.left -parentColumnInfo.left);
            }else if(isPerColumnFixed){
                canvas.save();
                canvas.clipRect(clipRect.left, showRect.top, showRect.right,
                        showRect.top + clipHeight);
                isPerColumnFixed = false;
                clipCount++;
            }
            fillColumnTitle(canvas, info, left);
        }
        for(int i = 0;i < clipCount;i++){
            canvas.restore();
        }
        if (config.isFixedTitle()) {
            scaleRect.top += titleHeight;
            showRect.top += titleHeight;
        } else {
            showRect.top += clipHeight;
            scaleRect.top += titleHeight;
        }

    }

    /**
     * 填充列标题
     * @param canvas 画布
     * @param info 列信息
     * @param left 左边
     */
    private void fillColumnTitle(Canvas canvas, ColumnInfo info, int left) {

        int top = (int)(info.top*config.getZoom())
                + (config.isFixedTitle() ? showRect.top : scaleRect.top);
        int right = (int) (left + info.width *config.getZoom());
        int bottom = (int) (top + info.height*config.getZoom());
        if (DrawUtils.isMixRect(showRect, left, top, right, bottom)) {
            if (!isClickPoint && onColumnClickListener != null) {
                if (DrawUtils.isClick(left, top, right, bottom, clickPoint)) {
                    isClickPoint = true;
                    clickColumnInfo = info;
                    clickPoint.set(-1, -1);
                }
            }
            Paint paint = config.getPaint();
            tempRect.set(left,top,right,bottom);
            if(config.getTableGridFormat() !=null) {
                config.getColumnTitleGridStyle().fillPaint(paint);
                int position = tableData.getChildColumns().indexOf(info.column);
                config.getTableGridFormat().drawColumnTitleGrid(canvas,tempRect,info.column,position,paint);
            }
            tableData.getTitleDrawFormat().draw(canvas, info.column, tempRect, config);

        }
    }

    /**
     * 绘制内容
     * @param canvas 画布
     */
    private void drawContent(Canvas canvas) {
        trendCellInfoList.clear();
        trendRectList.clear();
        float top;
        float left = scaleRect.left;
        List<Column> columns = tableData.getChildColumns();
        clipRect.set(showRect);
        TableInfo info = tableData.getTableInfo();
        int columnSize = columns.size();
        int dis = config.isFixedCountRow() ? info.getCountHeight()
                : showRect.bottom + info.getCountHeight() - scaleRect.bottom;
        int fillBgBottom = showRect.bottom - Math.max(dis, 0);
        if(config.getContentBackground() !=null){
            tempRect.set(showRect.left, showRect.top, showRect.right, fillBgBottom);
            config.getContentBackground().drawBackground(canvas,tempRect,config.getPaint());
        }
        if (config.isFixedCountRow()) {
            canvas.save();
            canvas.clipRect(showRect.left, showRect.top, showRect.right, showRect.bottom - info.getCountHeight());
        }
        List<ColumnInfo> childColumnInfo = tableData.getChildColumnInfos();
        boolean isPerFixed = false;
        int clipCount = 0;
        Rect correctCellRect;
        Rect tempCellRect;
        TableInfo tableInfo = tableData.getTableInfo();
        for (int i = 0; i < columnSize; i++) {
            top = scaleRect.top;
            Column column = columns.get(i);
            float width = column.getComputeWidth()*config.getZoom();
            float tempLeft = left;
            //根据根部标题是否固定
            Column topColumn = childColumnInfo.get(i).getTopParent().column;
            if (topColumn.isFixed()) {
                isPerFixed = false;
                if(tempLeft < clipRect.left){
                    left = clipRect.left;
                    clipRect.left +=width;
                    isPerFixed = true;
                }
            }else if(isPerFixed){
                canvas.save();
                canvas.clipRect(clipRect);
                isPerFixed = false;
               clipCount++;
            }
            float right = left + width;

            if (left < showRect.right) {
                int size = column.getDatas().size();
                int realPosition = 0;
                for (int j = 0; j < size; j++) {
                    String value = column.format(j);
                    int skip =tableInfo.getSeizeCellSize(column,j);
                    int totalLineHeight =0;
                    for(int k = realPosition;k<realPosition+skip;k++){
                        totalLineHeight += info.getLineHeightArray()[k];
                    }
                    realPosition+=skip;
                    float bottom = top + totalLineHeight*config.getZoom();
                    tempRect.set((int) left, (int) top, (int) right, (int) bottom);
                    correctCellRect = tempCellRect = gridDrawer.correctCellRect(j, i, tempRect, config.getZoom()); //矫正格子的大小
                    if (correctCellRect != null) {
                    if (correctCellRect.top < showRect.bottom) {
                        if (correctCellRect.right > showRect.left && correctCellRect.bottom > showRect.top) {
                            Object data = column.getDatas().get(j);
                                if (DrawUtils.isClick(correctCellRect, clickPoint)) {
                                    operation.setSelectionRect(i, j, correctCellRect);
                                    tipPoint.x = (left + right) / 2;
                                    tipPoint.y = (top + bottom) / 2;
                                    tipColumn = column;
                                    tipPosition = j;
                                    clickColumn(column, j, value, data);
                                    isClickPoint = true;
                                    clickPoint.set(-Integer.MAX_VALUE, -Integer.MAX_VALUE);
                                }
                                operation.checkSelectedPoint(i, j, correctCellRect);
                                cellInfo.set(column, data, value, i, j, tableData.getTrendPoint(j, i), tableData.getTextColorPoint(j, i), tableData.getRightTopCornerPoint(j, i), tableData.getTextColorPoint2(j, i), tableData.getHistogramCell(j, i));
                                /*if (trendPoint != null){
                                    CellInfo trendCellInfo = new CellInfo();
                                    trendCellInfo.set(column,data,value,i,j, trendPoint);
                                    trendCellInfoList.add(trendCellInfo);
                                    trendRectList.add(tempCellRect);
                                }*/
                                drawContentCell(canvas, cellInfo, correctCellRect, config);
//                                drawContentCellWithoutTrend(canvas,cellInfo,correctCellRect,config);
                            }
                        } else {
                            break;
                        }
                    }
                    top = bottom;
                }
                left = tempLeft + width;
            } else {
                break;
            }
        }
        drawTrendLine(canvas);
        /*int size = trendCellInfoList.size();
        for (int i=0; i<size; i++){
            CellInfo cellInfo = trendCellInfoList.get(i);
            drawContentCellOnlyTrend(canvas, cellInfo, trendRectList.get(i), config);
        }*/
        for (int i = 0; i < clipCount; i++) {
            canvas.restore();
        }
        if (config.isFixedCountRow()) {
            canvas.restore();
        }
    }

    /**
     * 绘制趋势图
     * 首先要准备好的趋势坐标点（即趋势点的横纵index值），根据这个些坐标点再画出趋势图
     *
     * @param canvas 画布
     */
    private void drawTrendLine(Canvas canvas) {
        /*if (tableData != null) {
            List<Cell> trendPoints = tableData.getTrendPoints();
            int cellHeight = 0;
            if (trendPoints != null) {
                Paint paint = new Paint();             // 创建画笔
                if (tableData.getTrendColor() != 0) {
                    paint.setColor(tableData.getTrendColor());           // 画笔颜色 - 黑色
                }
                int length = trendPoints.size();
                Path path = new Path();
                //List<int[]> centerPoints = new ArrayList<>();
                for (int i = 0; i < length; i++) {
                    Cell point = trendPoints.get(i);
                    if (point != null) {
                        int[] location = getPointLocation(point.row, point.col);
                        int[] size = getPointSize(point.row, point.col);
                        if (cellHeight == 0) {
                            cellHeight = (int) (size[1] / 3.0f);
                        }
                        int[] center = new int[]{location[0] - size[0] / 2, location[1] - size[1] / 2};
                        if (i == 0) {
                            path.moveTo(center[0], center[1]);
                        } else {
                            path.lineTo(center[0], center[1]);
                        }
                    }
                }
                paint.setStrokeWidth(2);              // 边框宽度 - 10
                paint.setStyle(Paint.Style.STROKE);    // 填充模式 - 描边
                canvas.save();
                canvas.drawPath(path, paint);
                canvas.restore();
            }
        }*/
        drawTrendLine1(canvas, tableData);
        drawTrendLine2(canvas, tableData);
        drawTrendLine3(canvas, tableData);
        drawTrendLine4(canvas, tableData);
        drawTrendLine5(canvas, tableData);
        drawTrendLine6(canvas, tableData);
        drawTrendLine7(canvas, tableData);
    }

    private void drawTrendLine1(Canvas canvas, TableData<T> tableData) {
        if (canvas != null && tableData != null){
            drawTrendLine(canvas, tableData.getTrendPoints(), config.getTrendLineColor());
        }
    }

    private void drawTrendLine2(Canvas canvas, TableData<T> tableData) {
        if (canvas != null && tableData != null){
            drawTrendLine(canvas, tableData.getTrendPoints2(), config.getTrendLineColor2());
        }
    }

    private void drawTrendLine3(Canvas canvas, TableData<T> tableData) {
        if (canvas != null && tableData != null){
            drawTrendLine(canvas, tableData.getTrendPoints3(), config.getTrendLineColor3());
        }
    }

    private void drawTrendLine4(Canvas canvas, TableData<T> tableData) {
        if (canvas != null && tableData != null){
            drawTrendLine(canvas, tableData.getTrendPoints4(), config.getTrendLineColor4());
        }
    }

    private void drawTrendLine5(Canvas canvas, TableData<T> tableData) {
        if (canvas != null && tableData != null){
            drawTrendLine(canvas, tableData.getTrendPoints5(), config.getTrendLineColor5());
        }
    }

    private void drawTrendLine6(Canvas canvas, TableData<T> tableData) {
        if (canvas != null && tableData != null){
            drawTrendLine(canvas, tableData.getTrendPoints6(), config.getTrendLineColor6());
        }
    }

    private void drawTrendLine7(Canvas canvas, TableData<T> tableData) {
        if (canvas != null && tableData != null){
            drawTrendLine(canvas, tableData.getTrendPoints7(), config.getTrendLineColor7());
        }
    }

    private void drawTrendLine(Canvas canvas, List<Cell> trendPoints, int trendColor) {
        if (trendPoints != null) {
            Paint paint = new Paint();             // 创建画笔
            if (trendColor != 0) {
                paint.setColor(trendColor);           // 画笔颜色 - 黑色
            }
            int length = trendPoints.size();
            Path path = new Path();
            //List<int[]> centerPoints = new ArrayList<>();
            for (int i = 0; i < length; i++) {
                Cell point = trendPoints.get(i);
                if (point != null) {
                    int[] location = getPointLocation(point.row, point.col);
                    int[] size = getPointSize(point.row, point.col);
                    int[] center = new int[]{location[0] - size[0] / 2, location[1] - size[1] / 2};
                    if (i == 0) {
                        path.moveTo(center[0], center[1]);
                    } else {
                        path.lineTo(center[0], center[1]);
                    }
                }
            }
            paint.setStrokeWidth(2);              // 边框宽度 - 10
            paint.setStyle(Paint.Style.STROKE);    // 填充模式 - 描边
            canvas.save();
            canvas.drawPath(path, paint);
            canvas.restore();
        }
    }

    /*private void drawTrendLine(Cell point, Canvas canvas) {
        if (point != null) {
            Cell prevCell = point.prevCell;
            if (prevCell != null) {
                int[] locationPrev = getPointLocation(prevCell.row, prevCell.col);
                int[] sizePrev = getPointSize(prevCell.row, prevCell.col);
                int[] centerPrev = new int[]{locationPrev[0] - sizePrev[0] / 2, locationPrev[1] - sizePrev[1] / 2};

                int[] location = getPointLocation(point.row, point.col);
                int[] size = getPointSize(point.row, point.col);
                int[] center = new int[]{location[0] - size[0] / 2, location[1] - size[1] / 2};

                Path path = new Path();
                path.moveTo(centerPrev[0], centerPrev[1]);
                path.lineTo(center[0], center[1]);
                Cell nextCell = point.nextCell;
                if (nextCell != null) {
                    int[] locationNext = getPointLocation(nextCell.row, nextCell.col);
                    int[] sizeNext = getPointSize(nextCell.row, nextCell.col);
                    int[] centerNext = new int[]{locationNext[0] - sizeNext[0] / 2, locationNext[1] - sizeNext[1] / 2};
                    path.lineTo(centerNext[0], centerNext[1]);
                }
                Paint paint = new Paint();             // 创建画笔
                if (tableData.getTrendColor() != 0) {
                    paint.setColor(tableData.getTrendColor());           // 画笔颜色 - 黑色
                }
                paint.setStrokeWidth(2);              // 边框宽度 - 10
                paint.setStyle(Paint.Style.STROKE);    // 填充模式 - 描边
                canvas.save();
                canvas.drawPath(path, paint);
                canvas.restore();
            }
        }
    }
*/
    /**
     * 绘制内容格子
     *
     * @param c        画布
     * @param cellInfo 格子信息
     * @param rect 方位
     * @param config 表格配置
     */
    protected void drawContentCell(Canvas c, CellInfo<T> cellInfo, Rect rect,TableConfig config) {

        if(config.getContentCellBackgroundFormat()!= null){
            config.getContentCellBackgroundFormat().drawBackground(c,rect,cellInfo,config.getPaint());
        }
        //drawTrendLine(cellInfo.trendPoint, c);
        if (config.getContentCellTendBackgroundFormat() != null) {
            config.getContentCellTendBackgroundFormat().drawBackground(c, rect, cellInfo, config.getPaint());
        }
        if (config.getHistogramCellBackgroundFormat() != null && cellInfo.histogramCell != null){
            config.getHistogramCellBackgroundFormat().drawBackground(c, rect, cellInfo, config.getPaint());
        }
        if (config.getTableGridFormat() != null) {
            config.getContentGridStyle().fillPaint(config.getPaint());
            config.getTableGridFormat().drawContentGrid(c,cellInfo.col,cellInfo.row,rect,cellInfo,config.getPaint());
        }
        Rect rect2 = new Rect(rect);
        if (!(config.getHistogramCellBackgroundFormat() != null && cellInfo.histogramCell != null)) {
            rect.left += config.getTextLeftOffset();
            cellInfo.column.getDrawFormat().draw(c, rect, cellInfo, config);
        }
        if (cellInfo.rightTopPoint != null && cellInfo.rightTopPoint.rightTopCorner != null) {
            drawCornerText(c, cellInfo.rightTopPoint.rightTopCorner, rect2, config.getPaint());
        }
    }

    protected void drawCornerText(Canvas c, CellRightTopCorner rightTopCorner, Rect rect, Paint paint) {
        if (rightTopCorner.rightTopValue != null) {
            int oldColor = paint.getColor();
            float oldTextSize = paint.getTextSize();
            float newTextSize = oldTextSize - 8 > 0 ? oldTextSize - 8 : oldTextSize;
            int width = rect.width();
            int height = rect.height();
            int colorCornerBg = Color.parseColor("#0000FF");//背景默认颜色为蓝色
            int colorCornerText = Color.parseColor("#FFFFFF");//字体默认颜色为白色
            if (rightTopCorner.rightTopCornerBackgroundColor != TableConfig.INVALID_COLOR) {
                colorCornerBg = rightTopCorner.rightTopCornerBackgroundColor;
            }
            if (rightTopCorner.rightTopCornerTextColor != TableConfig.INVALID_COLOR) {
                colorCornerText = rightTopCorner.rightTopCornerTextColor;
            }
            float radius = width > height ? height * 0.45f : width * 0.45f;
            if (colorCornerBg != TableConfig.INVALID_COLOR) {
                paint.setColor(colorCornerBg);
                paint.setStyle(Paint.Style.FILL);
                paint.setTextSize(newTextSize);
                float cornerRadius = width > height ? height * 0.2f : width * 0.2f;
                float newRadius = radius + cornerRadius - 8 > 0 ? radius + cornerRadius - 8 : radius + cornerRadius;
                float delta = (float) (newRadius * Math.sin(45 * Math.PI / 180));
                float cx = rect.centerX() + delta;
                float cy = rect.centerY() - delta;
                c.drawCircle(rect.centerX() + delta, rect.centerY() - delta, cornerRadius, paint);
                paint.setColor(colorCornerText);
                Rect cornerRect = new Rect((int) (cx - cornerRadius), (int) (cy - cornerRadius), (int) (cx + cornerRadius), (int) (cy + cornerRadius));
                DrawUtils.drawSingleText(c, paint, cornerRect, rightTopCorner.rightTopValue);
                paint.setColor(oldColor);
                paint.setTextSize(oldTextSize);
            }
        }
    }

    protected void drawContentCellWithoutTrend(Canvas c, CellInfo<T> cellInfo, Rect rect, TableConfig config) {
        if (cellInfo.trendPoint == null) {
            drawContentCell(c, cellInfo, rect, config);
        }
    }

    protected void drawContentCellOnlyTrend(Canvas c, CellInfo<T> cellInfo, Rect rect, TableConfig config) {
        if (cellInfo.trendPoint != null) {
            drawContentCell(c, cellInfo, rect, config);
        }
    }

    /**
     * 点击格子
     * @param column 列
     * @param position 位置
     * @param value 值
     * @param data 数据
     */
    private void clickColumn(Column column, int position, String value, Object data) {
        if (!isClickPoint && column.getOnColumnItemClickListener() != null) {
            column.getOnColumnItemClickListener().onClick(column, value, data, position);
        }
    }





    /**
     * 绘制提示
     */
    private void drawTip(Canvas canvas, float x, float y, Column c, int position) {
        if (tip != null) {
            tip.drawTip(canvas, x, y, showRect, c, position);
        }
    }

    private void drawCountText(Canvas canvas,Column column,int position, Rect rect, String text, TableConfig config) {
        Paint paint = config.getPaint();
        //绘制背景
        ICellBackgroundFormat<Column> backgroundFormat = config.getCountBgCellFormat();
        if(backgroundFormat != null){
            backgroundFormat.drawBackground(canvas,rect,column,config.getPaint());
        }
        //绘制网格
        if(config.getTableGridFormat() !=null){
            config.getContentGridStyle().fillPaint(paint);
            config.getTableGridFormat().drawCountGrid(canvas,position,rect,column,paint);
        }
        config.getCountStyle().fillPaint(paint);
        //字体颜色跟随背景变化
        if(backgroundFormat != null&& backgroundFormat.getTextColor(column) != TableConfig.INVALID_COLOR){
            paint.setColor(backgroundFormat.getTextColor(column));
        }
        //绘制字体
        paint.setTextSize(paint.getTextSize()*config.getZoom());
        if(column.getTextAlign() !=null) {
            paint.setTextAlign(column.getTextAlign());
        }
        canvas.drawText(text, DrawUtils.getTextCenterX(rect.left,rect.right,paint), DrawUtils.getTextCenterY(rect.centerY(), paint), paint);
    }


    @Override
    public void onClick(float x, float y) {
        clickPoint.x = x;
        clickPoint.y = y;
    }

    public OnColumnClickListener getOnColumnClickListener() {
        return onColumnClickListener;
    }

    public void setOnColumnClickListener(OnColumnClickListener onColumnClickListener) {
        this.onColumnClickListener = onColumnClickListener;
    }

    public ITip<Column, ?> getTip() {
        return tip;
    }

    public void setTip(ITip<Column, ?> tip) {
        this.tip = tip;
    }


    public void setSelectFormat(ISelectFormat selectFormat) {
        this.operation.setSelectFormat(selectFormat);
    }

    public GridDrawer<T> getGridDrawer() {
        return gridDrawer;
    }

    public void setGridDrawer(GridDrawer<T> gridDrawer) {
        this.gridDrawer = gridDrawer;
    }


    /**
     * 计算任何point在View的位置
     * @param row 列
     * @param col 行
     * @return
     */
    public int[] getPointLocation(double row,double col){
        List<Column> childColumns = tableData.getChildColumns();
        int[] lineHeights =  tableData.getTableInfo().getLineHeightArray();
        int x=0,y =0;
        int columnSize = childColumns.size();
        for(int i = 0; i <= (columnSize > col+1 ? col+1 : columnSize-1);i++){
            int w = childColumns.get(i).getComputeWidth();
            if(i == (int)col+1){
                x +=w *(col-(int)col);
            }else {
                x += w;
            }
        }
        for(int i = 0; i <= (lineHeights.length > row+1 ? row+1 : lineHeights.length-1);i++){
            int h = lineHeights[i];
            if(i == (int)row+1){
                y +=h *(row-(int)row);
            }else {
                y += h;
            }
        }
        x *= config.getZoom();
        y *= config.getZoom();
        x += scaleRect.left;
        y +=scaleRect.top;
        return new int[]{x,y};

    }
    /**
     * 计算任何point在View的大小
     * @param row 列
     * @param col 行
     * @return
     */
    public int[] getPointSize(int row,int col){
        List<Column> childColumns = tableData.getChildColumns();
        int[] lineHeights =  tableData.getTableInfo().getLineHeightArray();
        col= col < childColumns.size() ? col:childColumns.size()-1;//列
        row = row< lineHeights.length ? row:lineHeights.length;//行
        col = col< 0 ? 0 : col;
        row = row< 0 ? 0 : row;
        return new int[]{(int) (childColumns.get(col).getComputeWidth()*config.getZoom()),
                (int) (lineHeights[row]*config.getZoom())};

    }

    /**
     * 设置表面绘制
     */
    public void setDrawOver(IDrawOver drawOver) {
        this.drawOver = drawOver;
    }

    public SelectionOperation getOperation() {
        return operation;
    }

}
