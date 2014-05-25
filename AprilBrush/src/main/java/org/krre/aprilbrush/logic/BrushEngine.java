package org.krre.aprilbrush.logic;

import android.view.MotionEvent;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Canvas;
import android.graphics.Color;
import org.krre.aprilbrush.view.PaintView;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.content.res.Configuration;
import android.util.Log;
import android.graphics.Matrix;

public class BrushEngine {
    private String TAG = "AB";
    private Bitmap dabBitmap;
    private Bitmap bufferBitmap;
    private PaintView paintView;
    private Paint bufferPaint = new Paint();
    private Paint dabPaint = new Paint();
    private Canvas bufferCanvas = new Canvas();
    private Canvas dabCanvas = new Canvas();
    private Path path = new Path();
    private PathMeasure pathMeasure = new PathMeasure();
    private float pathLength;
    private int toolType;
    private float prevX;
    private float prevY;
    private float prevPressure;

    private int diameter = 20;
    private int spacing = 100;

    public BrushEngine(PaintView paintView) {
        this.paintView = paintView;
        dabPaint.setAntiAlias(true);
        dabBitmap = Bitmap.createBitmap(diameter, diameter, Bitmap.Config.ARGB_8888);
        dabBitmap.eraseColor(Color.TRANSPARENT);
        dabCanvas.setBitmap(dabBitmap);
        dabCanvas.drawCircle(diameter / 2f, diameter / 2f, diameter / 2f, dabPaint);
    }

    public Bitmap getBufferBitmap() {
        return bufferBitmap;
    }

    public void setBitmap(Bitmap value, int orientation) {
        int width  = value.getWidth();
        int height = value.getHeight();
        if ((orientation == Configuration.ORIENTATION_LANDSCAPE && width < height) ||
            (orientation == Configuration.ORIENTATION_PORTRAIT && width > height)) {
            Matrix matrix = new Matrix();
            matrix.postRotate(90f);
            bufferBitmap = Bitmap.createBitmap(value, 0, 0, width, height, matrix, true);
        } else {
            bufferBitmap = Bitmap.createBitmap(value, 0, 0, width, height);
        }
        bufferCanvas.setBitmap(bufferBitmap);
        paintView.invalidate();
    }

    public void setBufferSize(int width, int height) {
        bufferBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bufferCanvas.setBitmap(bufferBitmap);
    }

    public void paintDab(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        float pressure  = event.getPressure();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                toolType = event.getToolType(0);
                paintOneDab(x, y, pressure);

                path.reset();
                path.moveTo(x, y);
                pathLength = 0f;

                prevX = x;
                prevY = y;
                prevPressure = pressure;
                break;

            case MotionEvent.ACTION_MOVE:
                if (event.getHistorySize() > 0) {
                    for (int i = 0; i < event.getHistorySize(); i++) {
                        interpolateDab(event.getHistoricalX(i), event.getHistoricalY(i), event.getHistoricalPressure(i));
                    }
                } else {
                    interpolateDab(x, y, pressure);
                }
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
    }

    private void interpolateDab(float x, float y, float p) {
        double pointSpace = Math.sqrt(Math.pow(prevX - x, 2) + Math.pow(prevY - y, 2));

        float deltaDab = diameter * spacing / 100f;
        if (pointSpace >= deltaDab) {
            path.quadTo(prevX, prevY, (x + prevX) / 2, (y + prevY) / 2);
        } else {
            path.lineTo(x, y);
        }
        pathMeasure.setPath(path, false);
        float[] pathMeasurePos = new float[2];
        float[] pathMeasureTan = new float[2];
        while (pathMeasure.getLength() >= pathLength) {
            pathMeasure.getPosTan(pathLength, pathMeasurePos, pathMeasureTan);
            if (pathLength > 0) {
                paintOneDab(pathMeasurePos[0], pathMeasurePos[1], p); // TODO: interpolate pressure
            }
            pathLength += deltaDab;
        }
        prevX = x;
        prevY = y;
        prevPressure = p;
    }

    private void paintOneDab(float x, float y, float p) {
        float pressure = toolType == MotionEvent.TOOL_TYPE_STYLUS ? p : 1.0f;
        bufferCanvas.save();
        int alpha = Math.round((pressure * 255f));
        bufferPaint.setAlpha(alpha);
        float paintX = x - diameter / 2f;
        float paintY = y - diameter / 2f;
        bufferCanvas.drawBitmap(dabBitmap, paintX, paintY, bufferPaint);
        bufferCanvas.restore();
        paintView.invalidate((int)paintX - 1, (int)paintY - 1, (int)paintX + diameter + 1, (int)paintY + diameter + 1);
    }
}