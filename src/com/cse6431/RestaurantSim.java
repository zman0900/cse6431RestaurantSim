package com.cse6431;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class RestaurantSim {

	final CyclicBarrier barrier;
	int clock;
	Queue<Diner> diners;
	int numCooks;
	int numTables;

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

	public RestaurantSim(Queue<Diner> diners, int numTables, int numCooks) {
		clock = 0;
		this.diners = diners;
		this.numCooks = numCooks;
		this.numTables = numTables;

		barrier = new CyclicBarrier(numCooks, new Runnable() {
			@Override
			public void run() {
				clock++;
				System.out.println("Clock is now at " + clock);
			}
		});

	}

	public void run() {
		for (int i = 0; i < numCooks; ++i) {
			new Thread(new Cook(i)).start();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("Specify an input file");
			return;
		}

		try {
			// Read input
			BufferedReader in = new BufferedReader(new FileReader(args[0]));
			int numDiners = Integer.parseInt(in.readLine());
			int numTables = Integer.parseInt(in.readLine());
			int numCooks = Integer.parseInt(in.readLine());

			Queue<Diner> q = new ArrayDeque<Diner>(numDiners);
			for (int i = 0; i < numDiners; ++i) {
				Scanner s = new Scanner(in.readLine());
				q.add(new Diner(s.nextInt(), s.nextInt(), s.nextInt(), Boolean
						.valueOf(s.nextInt() != 0)));
			}

			// Run simulation
			RestaurantSim rs = new RestaurantSim(q, numTables, numCooks);
			rs.run();

		} catch (FileNotFoundException e) {
			System.out.println("Cannot open file for reading");
			return;
		} catch (NumberFormatException e) {
			System.out.println("Bad input file format");
			return;
		} catch (IOException e) {
			System.out.println("Failed reading file");
			return;
		} catch (NoSuchElementException e) {
			System.out.println("Bad input file format");
			return;
		}
	}

}
