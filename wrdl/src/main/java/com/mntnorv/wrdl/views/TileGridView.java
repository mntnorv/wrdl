package com.mntnorv.wrdl.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;

public class TileGridView extends View {

    private Paint mTextPaint;
    private Paint mCirclePaint;

    private float[] mTextXOffsets;
    private float[] mTextYOffsets;
    private String[] mTileStrings;
    private boolean[] mTilesSelected;

    private float[] mCircleXOffsets;
    private float[] mCircleYOffsets;
    private float mCircleRadius;

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
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
        mTextPaint.setColor(0xFF000000);
        mTextPaint.setTextSize(12);

        mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCirclePaint.setColor(0xFFFF0000);

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

        // Draw selection circles
        for (int i = 0; i < mTileStrings.length; i++) {
            if (mTilesSelected[i]) {
                canvas.drawCircle(mCircleXOffsets[i], mCircleYOffsets[i], mCircleRadius, mCirclePaint);
            }
        }

        // Draw text
        for (int i = 0; i < mTileStrings.length; i++) {
            canvas.drawText(mTileStrings[i], mTextXOffsets[i], mTextYOffsets[i], mTextPaint);
        }
    }

    private void updateDrawableProperties() {
        touchListener.initialize(getWidth(), getHeight(), sizeInTiles, sizeInTiles);

        mTextPaint.setTextSize(tileSize * 0.3f);

        mTextXOffsets = new float[mTileStrings.length];
        mTextYOffsets = new float[mTileStrings.length];
        mTilesSelected = new boolean[mTileStrings.length];

        mCircleRadius = tileSize * 0.8f / 2;
        mCircleXOffsets = new float[mTileStrings.length];
        mCircleYOffsets = new float[mTileStrings.length];

        Rect textBounds = new Rect();
        float baseXOffset;
        float baseYOffset;

        for (int i = 0; i < mTileStrings.length; i++) {
            baseXOffset = tileSize * (i % sizeInTiles);
            baseYOffset = tileSize * (i / sizeInTiles);

            // Update text properties
            mTilesSelected[i] = false;
            mTextPaint.getTextBounds(mTileStrings[i], 0, mTileStrings[i].length(), textBounds);
            mTextXOffsets[i] = baseXOffset + (tileSize - textBounds.right) / 2;
            mTextYOffsets[i] = baseYOffset + (tileSize + textBounds.bottom - textBounds.top) / 2;

            // Update circle properties
            mCircleXOffsets[i] = baseXOffset + tileSize / 2;
            mCircleYOffsets[i] = baseYOffset + tileSize / 2;
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
