package org.krre.aprilbrush.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import org.krre.aprilbrush.logic.BrushEngine;

public final class PaintView extends View {
    private String TAG = "AB";
    private Bitmap mainBitmap;
    private Paint mainPaint = new Paint();
    private Paint bufferPaint = new Paint();
    private BrushEngine brushEngine = BrushEngine.getInstance();

    public PaintView(Context context, AttributeSet attrs) {
        super(context, attrs);
        bufferPaint.setAlpha(255);
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        brushEngine.setPaintView(this);
        if (mainBitmap == null) {
            mainBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            mainBitmap.eraseColor(Color.TRANSPARENT);
            brushEngine.setBufferSize(w, h);
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.drawBitmap(mainBitmap, 0, 0, mainPaint);
        canvas.drawBitmap(brushEngine.getBufferBitmap(), 0, 0, bufferPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        brushEngine.paintDab(event);
        return true;
    }

    public void clear() {
        mainBitmap.eraseColor(Color.TRANSPARENT);
        invalidate();
    }

    public void setOpacity(int opacity) {
        bufferPaint.setAlpha(opacity * 255 / 100);
    }

    public void applyBuffer() {
        Canvas canvas = new Canvas(mainBitmap);
        canvas.drawBitmap(brushEngine.getBufferBitmap(), 0, 0, bufferPaint);
        invalidate();
    }
}
