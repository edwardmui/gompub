package com.orderfoodnow.pos.backend;

import java.util.TimerTask;

public class CheckHeldOrderTask extends TimerTask {

	private Server server;

	public CheckHeldOrderTask(Server server) {
		this.server = server;
	}

	@Override
	public void run() {
		server.checkHeldOrder();
	}
}
