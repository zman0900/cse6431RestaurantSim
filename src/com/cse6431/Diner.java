package com.cse6431;

public class Diner {
	private final int arrivalTime, id, numBurgers, numFries;
	private final boolean coke;
	private static int totalDiners = 0;

	public Diner(int arrivalTime, int numBurgers, int numFries, boolean coke) {
		this.arrivalTime = arrivalTime;
		this.numBurgers = numBurgers;
		this.numFries = numFries;
		this.coke = coke;

		this.id = totalDiners;
		totalDiners++;
	}

	public final int getArrivalTime() {
		return arrivalTime;
	}

	public final int getId() {
		return id;
	}

	public final int getNumBurgers() {
		return numBurgers;
	}

	public final int getNumFries() {
		return numFries;
	}

	public final boolean wantsCoke() {
		return coke;
	}

}
