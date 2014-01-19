package com.mntnorv.wrdl.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.mntnorv.wrdl.R;

import java.util.ArrayList;

public class TileGridView extends View {

    private static final int DEFAULT_TEXT_COLOR = 0xFF000000;
    private static final int DEFAULT_TILE_COLOR = 0xFFFF0000;
    private static final int DEFAULT_SHADOW_COLOR = 0xFFCCCCCC;
    private static final boolean DEFAULT_HANDLE_TOUCH_VALUE = false;

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

    private boolean mHandleTouch;
    private int mTextColor;
    private int mTileColor;
    private int mShadowColor;

    public TileGridView(Context context) {
        super(context);
        setDefaults();
        init();
    }

    public TileGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDefaults();
        parseAttributes(context, attrs);
        init();
    }

    private void setDefaults() {
        mTileColor = DEFAULT_TEXT_COLOR;
        mTileColor = DEFAULT_TILE_COLOR;
        mShadowColor = DEFAULT_SHADOW_COLOR;
        mHandleTouch = DEFAULT_HANDLE_TOUCH_VALUE;

        mTileStrings = new String[] {"Empty"};
        sizeInTiles = 1;
    }

    private void parseAttributes(Context context, AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.TileGridView,
                0, 0);

        try {
            mHandleTouch = a.getBoolean(R.styleable.TileGridView_handleTouchEvents, false);
            mTextColor = a.getColor(R.styleable.TileGridView_android_textColor, 0xFF000000);
            mTileColor = a.getColor(R.styleable.TileGridView_tileColor, 0xFFFF0000);
            mShadowColor = a.getColor(R.styleable.TileGridView_shadowColor, 0xFFCCCCCC);
        } finally {
            a.recycle();
        }
    }

    private void init() {
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
        mTextPaint.setColor(mTextColor);
        mTextPaint.setTextSize(12);

        mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCirclePaint.setColor(mTileColor);

        if (mHandleTouch) {
            setOnTouchListener(touchListener);
        }
    }

    public void setTiles(String[] tiles) {
        mTileStrings = tiles.clone();
        sizeInTiles = (int) Math.sqrt(mTileStrings.length);

        updateDrawableProperties();
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
