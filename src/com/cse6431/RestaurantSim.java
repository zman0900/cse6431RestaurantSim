package com.cse6431;

import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class RestaurantSim {

	final CyclicBarrier barrier;
	int clock;
	private static final Random rnd = new Random();

	private class Cook implements Runnable {

		private int id;

		public Cook(int id) {
			this.id = id;
		}

		@Override
		public void run() {
			while (true) {
				int sleepTime = rnd.nextInt(5000);
				System.out.println("Cook " + id + " sleeping for " + sleepTime
						+ " msecs");
				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("Cook " + id + " has clock " + clock);
				try {
					barrier.await();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (BrokenBarrierException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}

	public RestaurantSim() {
		clock = 0;
		barrier = new CyclicBarrier(5, new Runnable() {
			@Override
			public void run() {
				clock++;
				System.out.println("Clock is now at " + clock);
			}
		});

	}

	public void run() {
		for (int i = 0; i < 5; ++i) {
			new Thread(new Cook(i)).start();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		RestaurantSim rs = new RestaurantSim();
		rs.run();
	}

}
