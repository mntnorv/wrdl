package com.mntnorv.wrdl;

public enum GameMode {
	INFINITE (1);

	private int id;

	private GameMode(int id) {
		this.id = id;
	}

	public static GameMode fromId(int id) {
		for (GameMode mode : GameMode.values()) {
			if (mode.getId() == id) {
				return mode;
			}
		}

		return null;
	}

	public int getId() {
		return id;
	}
}
