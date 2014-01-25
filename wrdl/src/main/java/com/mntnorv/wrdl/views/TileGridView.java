package com.mntnorv.wrdl.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.mntnorv.wrdl.R;

import java.util.ArrayList;
import java.util.List;

public class TileGridView extends View {

	// Static
	private static final int DEFAULT_TEXT_COLOR = 0xFF000000;
	private static final int DEFAULT_SELECTED_TEXT_COLOR = 0xFFFFFFFF;
	private static final int DEFAULT_INDICATOR_COLOR = 0xFFFF0000;
	private static final boolean DEFAULT_HANDLE_TOUCH_VALUE = false;

	// Paints
	private Paint mTextPaint;
	private Paint mSelectedTextPaint;
	private Paint mIndicatorPaint;

	// Text specific
	private float[] mTextXOffsets;
	private float[] mTextYOffsets;
	private String[] mTileStrings;

	// Tile circle specific
	private boolean[] mTilesSelected;
	private float[] mCircleXOffsets;
	private float[] mCircleYOffsets;
	private float mCircleRadius;

	// Indicator specific
	private int[] mRotationLookupMatrix;
	private List<IndicatorDrawable> mIndicatorDrawableList
			= new ArrayList<IndicatorDrawable>();
	private float mIndicatorHeight;
	private float mIndicatorRotatedLength;

	// Main view dimensions
	private int mSizeInTiles;
	private float mTileSize;

	// Attributes parsed from XML
	private boolean mHandleTouch;
	private int mTextColor;
	private int mSelectedTextColor;
	private int mIndicatorColor;

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
		mTextColor = DEFAULT_TEXT_COLOR;
		mSelectedTextColor = DEFAULT_SELECTED_TEXT_COLOR;
		mIndicatorColor = DEFAULT_INDICATOR_COLOR;
		mHandleTouch = DEFAULT_HANDLE_TOUCH_VALUE;

		mTileStrings = new String[]{"A", "A", "A", "A", "A", "Qu", "A", "A", "A", "A", "A", "A", "A", "A", "A", "A"};
		mSizeInTiles = 4;

		setupRotationMatrices();
	}

	private void setupRotationMatrices() {
		mRotationLookupMatrix = new int[] {
			7, 0, 1, 6, 0, 2, 5, 4, 3
		};
	}

	private void parseAttributes(Context context, AttributeSet attrs) {
		TypedArray a = context.getTheme().obtainStyledAttributes(
				attrs,
				R.styleable.TileGridView,
				0, 0);

		try {
			mHandleTouch = a.getBoolean(R.styleable.TileGridView_handleTouchEvents, DEFAULT_HANDLE_TOUCH_VALUE);
			mTextColor = a.getColor(R.styleable.TileGridView_android_textColor, DEFAULT_TEXT_COLOR);
			mSelectedTextColor = a.getColor(R.styleable.TileGridView_selectedTextColor, DEFAULT_SELECTED_TEXT_COLOR);
			mIndicatorColor = a.getColor(R.styleable.TileGridView_indicatorColor, DEFAULT_INDICATOR_COLOR);
		} finally {
			a.recycle();
		}
	}

	private void init() {
		mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
		mTextPaint.setColor(mTextColor);
		mTextPaint.setTextSize(12);

		mSelectedTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
		mSelectedTextPaint.setColor(mSelectedTextColor);
		mSelectedTextPaint.setTextSize(12);

		mIndicatorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mIndicatorPaint.setColor(mIndicatorColor);

		if (mHandleTouch) {
			setOnTouchListener(touchListener);
		}
	}

	public void setTiles(String[] tiles) {
		mTileStrings = tiles.clone();
		mSizeInTiles = (int) Math.sqrt(mTileStrings.length);

		updateDrawableProperties();
	}

	private GridSequenceTouchListener touchListener = new GridSequenceTouchListener() {
		@Override
		protected void sequenceChanged(ArrayList<Integer> sequence, byte changeType, int elemChanged) {
			switch (changeType) {
				case GridSequenceTouchListener.ELEMENT_ADDED:
					selectTile(elemChanged);

					if (sequence.size() > 1) {
						addIndicator(sequence.get(1), elemChanged);
					}

					break;
				case GridSequenceTouchListener.ELEMENT_REMOVED:
					deselectTile(elemChanged);
					mIndicatorDrawableList.remove(0);
					break;
				case GridSequenceTouchListener.SEQUENCE_CLEARED:
					deselectAllTiles();
					mIndicatorDrawableList.clear();
					break;
			}
		}
	};

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		int newSize = w > h ? h : w;
		super.onSizeChanged(newSize, newSize, oldw, oldh);

		mTileSize = newSize / (mSizeInTiles * 1.0f);
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

		// Draw selection indicators
		drawSelectionIndicators(canvas, mIndicatorPaint);

		// Draw text
		for (int i = 0; i < mTileStrings.length; i++) {
			if (!mTilesSelected[i]) {
				canvas.drawText(mTileStrings[i], mTextXOffsets[i], mTextYOffsets[i], mTextPaint);
			} else {
				canvas.drawText(mTileStrings[i], mTextXOffsets[i], mTextYOffsets[i], mSelectedTextPaint);
			}
		}
	}

	private void drawSelectionIndicators(Canvas canvas, Paint indicatorPaint) {
		for (IndicatorDrawable indicatorDrawable : mIndicatorDrawableList) {
			// Draw the line
			canvas.save();
			canvas.rotate(
					indicatorDrawable.lineRotation,
					indicatorDrawable.lineRectangle.left,
					indicatorDrawable.lineRectangle.top + mIndicatorHeight / 2
			);

			canvas.drawRect(indicatorDrawable.lineRectangle, indicatorPaint);
			canvas.restore();

			// Draw the pointy ends
			canvas.save();
			canvas.rotate(
					indicatorDrawable.pointyRectFromRotation,
					indicatorDrawable.pointyRectangleFrom.left,
					indicatorDrawable.pointyRectangleFrom.top
			);

			canvas.drawRect(indicatorDrawable.pointyRectangleFrom, indicatorPaint);
			canvas.restore();

			canvas.save();
			canvas.rotate(
					indicatorDrawable.pointyRectToRotation,
					indicatorDrawable.pointyRectangleTo.left,
					indicatorDrawable.pointyRectangleTo.top
			);

			canvas.drawRect(indicatorDrawable.pointyRectangleTo, indicatorPaint);
			canvas.restore();
		}

		// Draw selection circles
		for (int i = 0; i < mTileStrings.length; i++) {
			if (mTilesSelected[i]) {
				canvas.drawCircle(mCircleXOffsets[i], mCircleYOffsets[i], mCircleRadius, indicatorPaint);
			}
		}
	}

	private void updateDrawableProperties() {
		touchListener.initialize(getWidth(), getHeight(), mSizeInTiles, mSizeInTiles);

		mTextPaint.setTextSize(mTileSize * 0.3f);
		mSelectedTextPaint.setTextSize(mTileSize * 0.3f);

		mTextXOffsets = new float[mTileStrings.length];
		mTextYOffsets = new float[mTileStrings.length];
		mTilesSelected = new boolean[mTileStrings.length];

		mCircleRadius = mTileSize * 0.5f / 2;
		mCircleXOffsets = new float[mTileStrings.length];
		mCircleYOffsets = new float[mTileStrings.length];

		mIndicatorHeight = mTileSize * 0.1f;
		mIndicatorRotatedLength = (float) Math.sqrt(mTileSize * mTileSize * 2);

		Rect textBounds = new Rect();
		float baseXOffset;
		float baseYOffset;

		for (int i = 0; i < mTileStrings.length; i++) {
			baseXOffset = mTileSize * (i % mSizeInTiles);
			baseYOffset = mTileSize * (i / mSizeInTiles);

			// Update text properties
			mTilesSelected[i] = false;
			mTextPaint.getTextBounds(mTileStrings[i], 0, mTileStrings[i].length(), textBounds);
			mTextXOffsets[i] = baseXOffset + (mTileSize - textBounds.right - textBounds.left) / 2;
			mTextYOffsets[i] = baseYOffset + (mTileSize - textBounds.bottom - textBounds.top) / 2;

			// Update circle properties
			mCircleXOffsets[i] = baseXOffset + mTileSize / 2;
			mCircleYOffsets[i] = baseYOffset + mTileSize / 2;
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

	private int getRotation(int dX, int dY, int offset) {
		int matrixPosition = (dY + 1) * 3 + dX + 1;
		return ((mRotationLookupMatrix[matrixPosition] + offset) % 8) * 45;
	}

	/**
	 * Adds an indicator to the View.
	 * Both {@code toCol} and {@code toRow} can't be equal to {@code fromCol} and
	 * {@code fromRow}.
	 *
	 * @param fromPos - indicator start tile position
	 * @param toPos   - indicator end tile position
	 */
	private void addIndicator(int fromPos, int toPos) {
		int fromCol = fromPos % mSizeInTiles;
		int fromRow = fromPos / mSizeInTiles;
		int toCol = toPos % mSizeInTiles;
		int toRow = toPos / mSizeInTiles;

		int dX = fromCol - toCol;
		int dY = fromRow - toRow;

		float x, y, w, h;

		// Indicator line
		h = mIndicatorHeight;

		if (dX != 0 && dY != 0) {
			w = mIndicatorRotatedLength;
		} else {
			w = mTileSize;
		}

		x = fromCol * mTileSize + mTileSize / 2;
		y = fromRow * mTileSize + mTileSize / 2 - h / 2;

		RectF lineRect = new RectF(x, y, x + w, y + h);

		// Indicator pointy ends
		y = fromRow * mTileSize + mTileSize / 2;
		RectF pointyRectFrom = new RectF(x, y, x + mCircleRadius, y + mCircleRadius);

		x = toCol * mTileSize + mTileSize / 2;
		y = toRow * mTileSize + mTileSize / 2;
		RectF pointyRectTo = new RectF(x, y, x + mCircleRadius, y + mCircleRadius);

		mIndicatorDrawableList.add(0, new IndicatorDrawable(
				getRotation(dX, dY, 2),
				lineRect,
				getRotation(dX, dY, 1),
				pointyRectFrom,
				getRotation(dX, dY, 5),
				pointyRectTo
		));
	}

	private class IndicatorDrawable {
		public int lineRotation;
		public int pointyRectFromRotation;
		public int pointyRectToRotation;
		public RectF lineRectangle;
		public RectF pointyRectangleFrom;
		public RectF pointyRectangleTo;

		private IndicatorDrawable(int lineRotation, RectF lineRectangle,
								  int pointyRectRotation,RectF pointyRectangleFrom,
								  int pointyRectToRotation, RectF pointyRectangleTo) {
			this.lineRotation = lineRotation;
			this.lineRectangle = lineRectangle;
			this.pointyRectFromRotation = pointyRectRotation;
			this.pointyRectangleFrom = pointyRectangleFrom;
			this.pointyRectToRotation = pointyRectToRotation;
			this.pointyRectangleTo = pointyRectangleTo;
		}
	}
}
