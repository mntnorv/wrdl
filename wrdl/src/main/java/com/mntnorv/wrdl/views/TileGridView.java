package com.mntnorv.wrdl.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;

public class TileGridView extends View {

    private Paint mTextNormalPaint;
    private Paint mTextSelectedPaint;

    private float[] mTextXOffsets;
    private float[] mTextYOffsets;
    private String[] mTileStrings;
    private boolean[] mTilesSelected;

    private int sizeInTiles;
    private float tileSize;

    public TileGridView(Context context) {
        super(context);
        init();
    }

    public TileGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TileGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mTextNormalPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
        mTextNormalPaint.setColor(0xFF000000);
        mTextNormalPaint.setTextSize(12);

        mTextSelectedPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
        mTextSelectedPaint.setColor(0xFFFF0000);
        mTextSelectedPaint.setTextSize(12);

        mTileStrings = new String[] {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P"};
        sizeInTiles = (int) Math.sqrt(mTileStrings.length);

        setOnTouchListener(touchListener);
    }

    private GridSequenceTouchListener touchListener = new GridSequenceTouchListener() {
        @Override
        protected void sequenceChanged(ArrayList<Integer> sequence, byte changeType, int elemChanged) {
            switch (changeType) {
                case GridSequenceTouchListener.ELEMENT_ADDED:
                    selectTile(elemChanged);
                    break;
                case GridSequenceTouchListener.ELEMENT_REMOVED:
                    deselectTile(elemChanged);
                    break;
                case GridSequenceTouchListener.SEQUENCE_CLEARED:
                    deselectAllTiles();
                    break;
            }
        }
    };

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        int newSize = w > h ? h : w;
        super.onSizeChanged(newSize, newSize, oldw, oldh);

        tileSize = newSize / (sizeInTiles * 1.0f);
        updateDrawableProperties();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int measureSpec = width > height ? heightMeasureSpec : widthMeasureSpec;
        super.onMeasure(measureSpec, measureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (int i = 0; i < mTileStrings.length; i++) {
            if (!mTilesSelected[i]) {
                canvas.drawText(mTileStrings[i], mTextXOffsets[i], mTextYOffsets[i], mTextNormalPaint);
            } else {
                canvas.drawText(mTileStrings[i], mTextXOffsets[i], mTextYOffsets[i], mTextSelectedPaint);
            }
        }
    }

    private void updateDrawableProperties() {
        touchListener.initialize(getWidth(), getHeight(), sizeInTiles, sizeInTiles);

        mTextNormalPaint.setTextSize(tileSize * 0.3f);
        mTextSelectedPaint.setTextSize(tileSize * 0.3f);

        mTextXOffsets = new float[mTileStrings.length];
        mTextYOffsets = new float[mTileStrings.length];
        mTilesSelected = new boolean[mTileStrings.length];

        Rect textBounds = new Rect();
        for (int i = 0; i < mTileStrings.length; i++) {
            mTilesSelected[i] = false;
            mTextNormalPaint.getTextBounds(mTileStrings[i], 0, mTileStrings[i].length(), textBounds);
            mTextXOffsets[i] = tileSize * (i % sizeInTiles) + (tileSize - textBounds.right) / 2;
            mTextYOffsets[i] = tileSize * (i / sizeInTiles) + (tileSize / 2);
        }
    }

    private void selectTile(int position) {
        mTilesSelected[position] = true;
        invalidate();
    }

    private void deselectTile(int position) {
        mTilesSelected[position] = false;
        invalidate();
    }

    private void deselectAllTiles() {
        for (int i = 0; i < mTilesSelected.length; i++) {
            mTilesSelected[i] = false;
        }
        invalidate();
    }
}
