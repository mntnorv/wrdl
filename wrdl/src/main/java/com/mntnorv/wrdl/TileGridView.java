package com.mntnorv.wrdl;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class TileGridView extends View {

    private Paint mTextPaint;
    private float[] mTextXOffsets;
    private float[] mTextYOffsets;
    private String[] mTileStrings;

    private int sizeInTiles;

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
        mTileStrings = new String[] {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P"};

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(0xFF000000);
        mTextPaint.setTextSize(12);

        sizeInTiles = (int) Math.sqrt(mTileStrings.length);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        int newSize = w > h ? h : w;
        super.onSizeChanged(newSize, newSize, oldw, oldh);

        float tileSize = newSize / (sizeInTiles * 1.0f);
        mTextPaint.setTextSize(tileSize * 0.3f);

        mTextXOffsets = new float[mTileStrings.length];
        mTextYOffsets = new float[mTileStrings.length];

        Rect textBounds = new Rect();
        for (int i = 0; i < mTileStrings.length; i++) {
            mTextPaint.getTextBounds(mTileStrings[i], 0, mTileStrings[i].length(), textBounds);
            mTextXOffsets[i] = tileSize * (i % sizeInTiles) + (tileSize - textBounds.right) / 2;
            mTextYOffsets[i] = tileSize * (i / sizeInTiles) + (tileSize / 2);
        }
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
            canvas.drawText(mTileStrings[i], mTextXOffsets[i], mTextYOffsets[i], mTextPaint);
        }
    }
}
