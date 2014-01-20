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
	private static final int DEFAULT_TILE_COLOR = 0xFFFF0000;
	private static final int DEFAULT_SHADOW_COLOR = 0xFFCCCCCC;
	private static final boolean DEFAULT_HANDLE_TOUCH_VALUE = false;

	// Paints
	private Paint mTextPaint;
	private Paint mCirclePaint;
	private Paint mShadowPaint;

	// Text specific
	private float[] mTextXOffsets;
	private float[] mTextYOffsets;
	private String[] mTileStrings;

	// Tile circle specific
	private boolean[] mTilesSelected;
	private float[] mCircleXOffsets;
	private float[] mCircleYOffsets;
	private float mCircleRadius;

	private float mShadowOffset;

	// Indicator specific
	private float[] mIndicatorRotationMatrix;
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

		mTileStrings = new String[]{"A", "A", "A", "A", "A", "A", "A", "A", "A", "A", "A", "A", "A", "A", "A", "A"};
		mSizeInTiles = 4;

		setupRotationMatrix();
	}

	private void setupRotationMatrix() {
		mIndicatorRotationMatrix = new float[9];
		mIndicatorRotationMatrix[4] = 0; // center

		mIndicatorRotationMatrix[1] = 90; // top
		mIndicatorRotationMatrix[7] = -90; // bottom
		mIndicatorRotationMatrix[3] = 0; // left
		mIndicatorRotationMatrix[5] = 180; // right

		mIndicatorRotationMatrix[0] = 45;       // top left
		mIndicatorRotationMatrix[2] = -45 + 180; // top right
		mIndicatorRotationMatrix[6] = -45;       // bottom left
		mIndicatorRotationMatrix[8] = 45 - 180; // bottom right
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

		mShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mShadowPaint.setColor(mShadowColor);

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

		// Draw selection indicator shadows
		drawSelectionIndicators(canvas, mShadowPaint, mShadowOffset);

		// Draw selection indicators
		drawSelectionIndicators(canvas, mCirclePaint, 0);

		// Draw text
		for (int i = 0; i < mTileStrings.length; i++) {
			canvas.drawText(mTileStrings[i], mTextXOffsets[i], mTextYOffsets[i], mTextPaint);
		}
	}

	private void drawSelectionIndicators(Canvas canvas, Paint indicatorPaint, float offset) {
		if (offset != 0) {
			canvas.save();
			canvas.translate(offset, offset);
		}

		// Draw selection sequence indicators
		IndicatorDrawable currentDrawable;

		for (IndicatorDrawable indicatorDrawable : mIndicatorDrawableList) {
			currentDrawable = indicatorDrawable;

			canvas.save();
			canvas.rotate(
					indicatorDrawable.rotation,
					currentDrawable.rectangle.left,
					currentDrawable.rectangle.top + mIndicatorHeight / 2
			);

			canvas.drawRect(currentDrawable.rectangle, indicatorPaint);
			canvas.restore();
		}

		// Draw selection circles
		for (int i = 0; i < mTileStrings.length; i++) {
			if (mTilesSelected[i]) {
				canvas.drawCircle(mCircleXOffsets[i], mCircleYOffsets[i], mCircleRadius, indicatorPaint);
			}
		}

		if (offset != 0) {
			canvas.restore();
		}
	}

	private void updateDrawableProperties() {
		touchListener.initialize(getWidth(), getHeight(), mSizeInTiles, mSizeInTiles);

		mTextPaint.setTextSize(mTileSize * 0.3f);

		mTextXOffsets = new float[mTileStrings.length];
		mTextYOffsets = new float[mTileStrings.length];
		mTilesSelected = new boolean[mTileStrings.length];

		mCircleRadius = mTileSize * 0.8f / 2;
		mCircleXOffsets = new float[mTileStrings.length];
		mCircleYOffsets = new float[mTileStrings.length];

		mShadowOffset = mTileSize * 0.04f;

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
		h = mIndicatorHeight;

		if (dX != 0 && dY != 0) {
			w = mIndicatorRotatedLength;
		} else {
			w = mTileSize;
		}

		x = fromCol * mTileSize + mTileSize / 2;
		y = fromRow * mTileSize + mTileSize / 2 - h / 2;

		mIndicatorDrawableList.add(0, new IndicatorDrawable(
				mIndicatorRotationMatrix[(dY + 1) * 3 + dX + 1],
				new RectF(x, y, x + w, y + h)
		));
	}

	private class IndicatorDrawable {
		public float rotation;
		public RectF rectangle;

		private IndicatorDrawable(float rotation, RectF rectangle) {
			this.rotation = rotation;
			this.rectangle = rectangle;
		}
	}
}
