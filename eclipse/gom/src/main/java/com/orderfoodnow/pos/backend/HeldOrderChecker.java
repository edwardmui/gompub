package com.orderfoodnow.pos.backend;

import java.util.Timer;

public class HeldOrderChecker {
	private static long INITIAL_DELAY_MILLI = 1000 * 30; // 30 seconds
	private static long FREQUENCY_MILLI = 1000 * 60 * 2; // 2 minutes
	private Timer timer;

	public HeldOrderChecker(Server server) {
		boolean daemonThread = true;

		// start as daemon thread.JVM can exit without waiting for thread.
		timer = new Timer(daemonThread);
		timer.schedule(new CheckHeldOrderTask(server), INITIAL_DELAY_MILLI, FREQUENCY_MILLI);
	}
}
