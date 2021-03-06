package com.mntnorv.wrdl.views;

import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import java.util.ArrayList;

public abstract class GridSequenceTouchListener implements OnTouchListener {
	/* STATIC */
	public static final byte ELEMENT_ADDED = 0x01;
	public static final byte ELEMENT_REMOVED = 0x02;
	public static final byte SEQUENCE_CLEARED = 0x00;

	/* FIELDS */
	private boolean initialized;

	private float width;
	private float height;

	private float tileWidth;
	private float tileHeight;
	private float tileMinDim;
	private int rows;
	private int columns;

	private ArrayList<Integer> selected = new ArrayList<Integer>();
	
	/* CONSTRUCTOR */

	/**
	 * Makes a new GridSequenceTouchListener
	 */
	public GridSequenceTouchListener() {
		initialized = false;
	}

	/* SEQUENCE CHANGED */

	/**
	 * Gets called when the sequence of selected tiles
	 * in the grid is changed.
	 *
	 * @param sequence    - a list of Integers representing
	 *                    the sequence of selected tiles. The Integer is calculated
	 *                    like this: {@code row * numberOfColumns + column}. The last tile
	 *                    in the sequence is always the first in this list.
	 * @param changeType  - represents the change that was made to
	 *                    the sequence. Can be equal to {@link #ELEMENT_ADDED},
	 *                    {@link #ELEMENT_REMOVED} or {@link #SEQUENCE_CLEARED}.
	 * @param elemChanged - value of the element added or removed.
	 *                    {@code -1} if {@code changeType} == {@link #SEQUENCE_CLEARED}.
	 */
	protected abstract void sequenceChanged(ArrayList<Integer> sequence, byte changeType, int elemChanged);

	/* ON TOUCH */
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (!initialized) {
			return false;
		}

		if (event.getPointerCount() == 1) {
			boolean sequenceChanged = false;
			byte changeType = SEQUENCE_CLEARED;
			int elemChanged = -1;

			float x = event.getX();
			float y = event.getY();

			int xt = (int) (x / tileWidth);
			int yt = (int) (y / tileHeight);

			boolean inside = xt >= 0 && xt < columns && yt >= 0 && yt < rows;

			if (event.getAction() == MotionEvent.ACTION_DOWN && inside) {
				selected.add(yt * columns + xt);
				sequenceChanged = true;
				changeType = ELEMENT_ADDED;
				elemChanged = yt * columns + xt;
			} else if (event.getAction() == MotionEvent.ACTION_MOVE && inside && selected.size() > 0) {
				int lastXT = selected.get(0) % columns;
				int lastYT = selected.get(0) / rows;
				int prevXT = -1;
				int prevYT = -1;

				if (selected.size() > 1) {
					prevXT = selected.get(1) % columns;
					prevYT = selected.get(1) / rows;
				}

				int dx = Math.abs(lastXT - xt);
				int dy = Math.abs(lastYT - yt);

				if (!(dx == 0 && dy == 0) && (dx <= 1 && dy <= 1) &&
						(((!selected.contains(Integer.valueOf(yt * columns + xt))) && (selected.size() < rows * columns))
								|| (xt == prevXT && yt == prevYT))) {

					float xTile = xt * tileWidth + tileWidth / 2;
					float yTile = yt * tileHeight + tileHeight / 2;

					float distance = (x - xTile) * (x - xTile) + (y - yTile) * (y - yTile);
					float maxDistance = (tileMinDim * 0.9f) / 2;

					if (distance < maxDistance * maxDistance) {
						if (!selected.contains(Integer.valueOf(yt * columns + xt))) {
							selected.add(0, yt * columns + xt);
							changeType = ELEMENT_ADDED;
							elemChanged = yt * columns + xt;
						} else {
							selected.remove(0);
							changeType = ELEMENT_REMOVED;
							elemChanged = lastYT * columns + lastXT;
						}

						sequenceChanged = true;
					}
				}
			} else if (event.getAction() == MotionEvent.ACTION_UP) {
				selected.clear();
				sequenceChanged = true;
			}

			if (sequenceChanged) {
				sequenceChanged(selected, changeType, elemChanged);
			}
		}

		return true;
	}

	private void updateWidth() {
		tileWidth = width / columns;
		tileMinDim = Math.min(tileWidth, tileHeight);
	}

	private void updateHeight() {
		tileHeight = height / rows;
		tileMinDim = Math.min(tileWidth, tileHeight);
	}

	/* SETTERS */
	public void setWidth(float width) {
		this.width = width;
		updateWidth();
	}

	public void setHeight(float height) {
		this.height = height;
		updateHeight();
	}

	public void setSize(float width, float height) {
		setWidth(width);
		setHeight(height);
	}

	public void setColumns(int columns) {
		this.columns = columns;
		updateWidth();
	}

	public void setRows(int rows) {
		this.rows = rows;
		updateHeight();
	}

	public void setGridSize(int columns, int rows) {
		setColumns(columns);
		setRows(rows);
	}

	public void initialize(float width, float height, int columns, int rows) {
		setWidth(width);
		setHeight(height);
		setColumns(columns);
		setRows(rows);

		initialized = true;
	}
}