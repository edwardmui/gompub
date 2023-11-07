package com.orderfoodnow.pos.frontend;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;

//https://www.programcreek.com/java-api-examples/?api=javax.print.PrintService

public class AvailablePrintService {
	private static final Logger logger = Logger.getLogger(AvailablePrintService.class.getName());

	public static PrintService getDefaultPrintService() {
		return PrintServiceLookup.lookupDefaultPrintService();
	}

	public static PrintService getPrintService(String printerName) {
		return printerNameToPrintServiceMap.get(printerName);
	}

	public static Map<String, PrintService> getPrinterNameToPrintServiceMap() {
		return printerNameToPrintServiceMap;
	}

	// private constructor
	private AvailablePrintService() {
		PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
		for (PrintService printService : printServices) {
			logger.finer("Available printer name: " + printService.getName());
			printerNameToPrintServiceMap.put(printService.getName(), printService);
		}
	}

	private static Map<String, PrintService> printerNameToPrintServiceMap = new HashMap<>();

	// static block initialization for exception handling
	static {
		try {
			new AvailablePrintService();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Exception occured in creating AvailablePrintService singleton instance.");
		}
	}

	public static void main(String[] argc) throws Exception {
		logger.fine(printerNameToPrintServiceMap.toString());
		logger.fine("defaultPrintService=" + getDefaultPrintService());
		logger.fine("==========Last Line========");
	}
}
