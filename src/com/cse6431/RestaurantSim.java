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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class RestaurantSim {

	final CyclicBarrier barrier;
	int clock;
	Queue<Diner> diners;
	int numCooks;
	int numTables;
	BlockingQueue<Integer> tables;

	private static final Random rnd = new Random();

	private class CookRunnable implements Runnable {

		private int id;

		public CookRunnable(int id) {
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

	private class DinerRunnable implements Runnable {

		private Diner diner;
		private Integer table;
		private int tableStartTime;

		public DinerRunnable(Diner diner) {
			this.diner = diner;
		}

		@Override
		public void run() {
			System.out.println("Diner " + diner.getId() + " arrived at time "
					+ clock);
			// Acquire table
			acquireTable();
			// for testing, loop on table for 5 ticks
			boolean done = false;
			while (!done) {
				if (clock >= tableStartTime + 5) {
					done = true;
					try {
						System.out.println("Diner " + diner.getId()
								+ " left at time " + clock);
						tables.put(table);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					Thread.yield();
				}
			}
		}

		private void acquireTable() {
			try {
				table = tables.take(); // Blocking call if none available
				tableStartTime = clock;
				System.out.println("Diner " + diner.getId()
						+ " acquired table " + table + " at time " + clock);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	public RestaurantSim(Queue<Diner> diners, int numTables, int numCooks) {
		clock = 0;
		this.diners = diners;
		this.numCooks = numCooks;
		this.numTables = numTables;

		// Build pool of 'table' resources
		tables = new ArrayBlockingQueue<Integer>(numTables, true);
		for (int i = 0; i < numTables; ++i) {
			tables.offer(i);
		}

		barrier = new CyclicBarrier(numCooks, new Runnable() {
			@Override
			public void run() {
				clock++;
				System.out.println("Clock is now at " + clock);
				// Start any new diner threads
				startCurrentDiners();
			}
		});
	}

	public void run() {
		for (int i = 0; i < numCooks; ++i) {
			new Thread(new CookRunnable(i)).start();
		}
	}

	private void startCurrentDiners() {
		Diner next = diners.peek();
		if (next != null) {
			if (next.getArrivalTime() == clock) {
				// TODO Next diner arrived now
				diners.remove();
				new Thread(new DinerRunnable(next)).start();
			}
		} else {
			// TODO All diners arrived

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
