package com.cmsc434.adi.doodle;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;
import android.util.AttributeSet;
import android.view.View;
import java.util.ArrayList;

/**
 * Created by Adi on 11/2/16.
 */

public class ScribbleView extends View {

    private Resources res = getResources();
    private Path drawPath = new Path();
    MainActivity containerActivity;
    Paint drawPaint = new Paint();
    int paintColor = Color.argb(res.getInteger(R.integer.initial_opacity), res.getInteger(R.integer.initial_red), res.getInteger(R.integer.initial_green), res.getInteger(R.integer.initial_blue));
    private Canvas drawCanvas;
    private ArrayList<APath> paths = new ArrayList<>();
    private int amountOfPathsUndone = 0;

    public ScribbleView(Context context, AttributeSet attrs){
        super(context, attrs);

        setupPaint();
    }

    // MARK: - View Overrides

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        drawCanvas = new Canvas(Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for (int i = 0; i < paths.size() - amountOfPathsUndone; i++) {
            APath path = paths.get(i);
            canvas.drawPath(path.path, path.paint);
        }
    }

    // MARK: - Setup

    private void setupPaint() {
        // Setup paint
        drawPaint.setColor(paintColor);
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(res.getInteger(R.integer.initial_size));
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    // MARK: - Touch Event Handling (Scribbling)

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        float touchX = event.getX();
        float touchY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                paths.subList(paths.size()-amountOfPathsUndone, paths.size()).clear();
                amountOfPathsUndone = 0;

                paths.add(new APath(drawPath, new Paint(drawPaint)));
                drawPath.moveTo(touchX, touchY);
                break;
            case MotionEvent.ACTION_MOVE:
                drawPath.lineTo(touchX, touchY);
                drawCanvas.drawPath(drawPath, drawPaint);
                break;
            case MotionEvent.ACTION_UP:
                drawCanvas.drawPath(drawPath, drawPaint);
                drawPath = new Path();
                canUndoRedo();
                break;
            default:
                return false;
        }

        invalidate();
        return true;

    }

    // MARK: - Undo/Redo

    public void undo() {
        if (paths.size() != amountOfPathsUndone) {
            amountOfPathsUndone += 1;
            invalidate();
        }

        canUndoRedo();
    }

    public void redo() {
        if (amountOfPathsUndone != 0) {
            amountOfPathsUndone -= 1;
            invalidate();
        }

        canUndoRedo();
    }

    public void changePaintColor(int paintColor) {
        this.paintColor = paintColor;
        drawPaint.setColor(paintColor);
    }

    public void changeBrushSize(int brushSize) {
        drawPaint.setStrokeWidth(brushSize);
    }

    public int getBrushSize() {
        return (int) drawPaint.getStrokeWidth();
    }

    public void clear() {
        amountOfPathsUndone = 0;
        paths.clear();
        invalidate();
    }

    public void canUndoRedo() {
        int enabled = res.getInteger(R.integer.enabled_alpha);
        int disabled = res.getInteger(R.integer.disabled_alpha);

        boolean canUndo = paths.size() > amountOfPathsUndone;
        boolean canRedo = amountOfPathsUndone > 0;
        containerActivity.undo.setImageAlpha(canUndo ? enabled : disabled);
        containerActivity.redo.setImageAlpha(canRedo ? enabled : disabled);
    }

}
