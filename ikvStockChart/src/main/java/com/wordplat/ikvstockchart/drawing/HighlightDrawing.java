/*
 * Copyright (C) 2017 WordPlat Open Source Project
 *
 *      https://wordplat.com/InteractiveKLineView/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wordplat.ikvstockchart.drawing;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;

import com.wordplat.ikvstockchart.entry.SizeColor;
import com.wordplat.ikvstockchart.marker.IMarkerView;
import com.wordplat.ikvstockchart.render.AbstractRender;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>HighlightDrawing</p>
 * <p>Date: 2017/3/23</p>
 *
 * @author afon
 */

public class HighlightDrawing implements IDrawing {

    protected Paint highlightPaintY; // 高亮线条画笔
    protected Paint highlightPaintX; // 高亮线条画笔

    protected final RectF contentRect = new RectF(); // 绘图区域
    protected AbstractRender render;

    private List<IMarkerView> markerViewList = new ArrayList<>();

    public HighlightDrawing() {

    }

    public HighlightDrawing(IMarkerView... markerViews) {
        for (IMarkerView markerView : markerViews) {
            addMarkerView(markerView);
        }
    }

    public void addMarkerView(IMarkerView markerView) {
        markerViewList.add(markerView);
    }

    @Override
    public void onInit(RectF contentRect, AbstractRender render) {
        this.render = render;
        final SizeColor sizeColor = render.getSizeColor();

        if (highlightPaintX == null) {
            highlightPaintX = new Paint(Paint.ANTI_ALIAS_FLAG);
            highlightPaintX.setStyle(Paint.Style.STROKE);
        }
        highlightPaintX.setStrokeWidth(2f);
        highlightPaintX.setColor(sizeColor.getHighlightColor());

        if (highlightPaintY == null) {
            highlightPaintY = new Paint(Paint.ANTI_ALIAS_FLAG);
            highlightPaintY.setStyle(Paint.Style.STROKE);
        }
        highlightPaintY.setStrokeWidth(18f);
        highlightPaintY.setColor(Color.parseColor("#50000000"));
        this.contentRect.set(render.getViewRect());
        if (markerViewList.size() > 0) {
            for (IMarkerView markerView : markerViewList) {
                markerView.onInitMarkerView(this.contentRect, this.render);
            }
        }
    }

    @Override
    public void computePoint(int minIndex, int maxIndex, int currentIndex) {

    }

    @Override
    public void onComputeOver(Canvas canvas, int minIndex, int maxIndex, float minY, float maxY) {
        Log.e("com",minY+"==="+maxY);
    }

    @Override
    public void onDrawOver(Canvas canvas) {
        // 绘制高亮
        if (render.isHighlight()) {
            final float[] highlightPoint = render.getHighlightPoint();
            canvas.save();
            canvas.clipRect(contentRect);

            if (markerViewList.size() > 0) {
                for (IMarkerView markerView : markerViewList) {
                    markerView.onDrawMarkerView(canvas,
                            highlightPoint[0],
                            highlightPoint[1]);
                }
            }
            canvas.drawLine(highlightPoint[0], contentRect.top, highlightPoint[0], contentRect.bottom, highlightPaintY);
            canvas.drawLine(contentRect.left, highlightPoint[1], contentRect.right, highlightPoint[1], highlightPaintX);

            canvas.restore();
        }
    }
}
