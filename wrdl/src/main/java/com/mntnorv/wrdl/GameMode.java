package com.mntnorv.wrdl;

public enum GameMode {
	INFINITE (1);

	private int id;

	private GameMode(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}
}
