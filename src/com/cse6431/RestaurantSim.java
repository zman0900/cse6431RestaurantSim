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
import java.util.concurrent.CountDownLatch;

public class RestaurantSim {

	// Time of each clock tick in msec
	private static final int clockResolution = 500;

	private Integer clock;
	private final Object clockLock = new Object();
	private BlockingQueue<CookRunnable> cooks;
	private Queue<Diner> diners;
	private CountDownLatch dinersCounter;
	private int numCooks;
	private int numTables;
	private BlockingQueue<Integer> tables;
	private boolean timeToStop = false;

	private static final Random rnd = new Random();

	private class CookRunnable implements Runnable {

		private Diner diner;
		private final Object food = new Object(); // dummy object
		private int id;

		public CookRunnable(int id) {
			this.diner = null;
			this.id = id;
		}

		@Override
		public void run() {
			System.out.println("Cook " + id + " started at " + clock);
			while (!timeToStop) {
				System.out.println("Cook " + id + " has clock " + clock);
				if (diner != null) {
					// For testing, sleep randomly then finish cooking
					int sleepTime = rnd.nextInt(5000);
					System.out.println("Cook " + id + " sleeping for "
							+ sleepTime + " msecs");
					try {
						Thread.sleep(sleepTime);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					diner = null;
					synchronized (food) {
						food.notify();
					}
				}
				// Wait for next clock tick
				try {
					synchronized (clockLock) {
						clockLock.wait();
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}
		}

		public void prepareOrderFor(Diner diner) {
			System.out.println("Cook " + id + " received order for diner "
					+ diner.getId());
			this.diner = diner;
		}

		public void waitForFood() {
			try {
				synchronized (food) {
					food.wait();
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	private class DinerRunnable implements Runnable {

		private Diner diner;
		private Integer table;
		private int foodStartTime;

		public DinerRunnable(Diner diner) {
			this.diner = diner;
		}

		@Override
		public void run() {
			System.out.println("Diner " + diner.getId() + " arrived at time "
					+ clock);
			// Acquire table
			acquireTable();
			// Acquire food
			acquireFood();
			// Eat for 30 min
			foodStartTime = clock;
			System.out.println("Diner " + diner.getId() + " served at time "
					+ foodStartTime);
			while (foodStartTime + 30 > clock) {
				try {
					synchronized (clockLock) {
						clockLock.wait();
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			// Leave table
			try {
				tables.put(table);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// Notify of leaving
			dinersCounter.countDown();
			System.out.println("Diner " + diner.getId() + " left at time "
					+ clock);
		}

		private void acquireTable() {
			try {
				table = tables.take(); // Blocking call if none available
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("Diner " + diner.getId() + " acquired table "
					+ table + " at time " + clock);
		}

		private void acquireFood() {
			// TODO Get cook, block until food is ready
			try {
				CookRunnable cook = cooks.take();
				cook.prepareOrderFor(diner); // Give cook order
				cook.waitForFood(); // Blocks until food ready
				cooks.put(cook); // Done with cook
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
		// Use this to stop when all diners served
		dinersCounter = new CountDownLatch(diners.size());
	}

	public void startSim() {
		// Build pool of 'cook' threads
		cooks = new ArrayBlockingQueue<CookRunnable>(numCooks, true);
		for (int i = 0; i < numCooks; ++i) {
			CookRunnable c = new CookRunnable(i);
			cooks.offer(c);
			// Start thread
			new Thread(c).start();
		}
		// Clock thread
		Thread clockThread = new Thread(new Runnable() {
			@Override
			public void run() {
				while (!timeToStop) {
					try {
						Thread.sleep(clockResolution);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					clock++;
					System.out.println("=== CLOCK INCREMENTED === " + clock);
					synchronized (clockLock) {
						clockLock.notifyAll();
					}
					// Start new diners
					startCurrentDiners();
				}
			}
		});
		clockThread.start();
		// Start any diners arriving at time 0
		startCurrentDiners();
		System.out.println("Threads started");
		// Wait for all diners to leave
		try {
			dinersCounter.await();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("All diners left");
		// Stop clock and cooks
		timeToStop = true;
	}

	private void startCurrentDiners() {
		Diner next = diners.peek();
		if (next != null) {
			if (next.getArrivalTime() == clock) {
				// TODO Next diner arrived now
				diners.remove();
				new Thread(new DinerRunnable(next)).start();
			}
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
			rs.startSim();

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
