package com.cse6431;

public class Diner {
	private final int arrivalTime, numBurgers, numFries;
	private final boolean coke;
	
	public Diner(int arrivalTime, int numBurgers, int numFries, boolean coke) {
		this.arrivalTime = arrivalTime;
		this.numBurgers = numBurgers;
		this.numFries = numFries;
		this.coke = coke;
	}

	public final int getArrivalTime() {
		return arrivalTime;
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
