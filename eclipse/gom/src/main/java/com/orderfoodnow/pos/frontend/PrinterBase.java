package com.orderfoodnow.pos.frontend;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintService;
import javax.print.SimpleDoc;
import javax.print.attribute.DocAttributeSet;
import javax.print.attribute.HashDocAttributeSet;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;

import com.orderfoodnow.pos.shared.Calculator;
import com.orderfoodnow.pos.shared.Cart;
import com.orderfoodnow.pos.shared.Configuration;
import com.orderfoodnow.pos.shared.OrderStatus;
import com.orderfoodnow.pos.shared.OrderType;
import com.orderfoodnow.pos.shared.PaymentType;
import com.orderfoodnow.pos.shared.Util;
import com.orderfoodnow.pos.shared.order.DeliveryInfo;
import com.orderfoodnow.pos.shared.order.DineInInfo;
import com.orderfoodnow.pos.shared.staff.Employee;

public class PrinterBase {
	protected static final Logger logger = Logger.getLogger(PrinterBase.class.getName());

	protected static final String CANCELLED = "Cancelled";
	protected static final String CANCELLED_IN_CHINESE = "取消";
	protected static final String CANCELLED_ENGLISH_AND_CHINESE = CANCELLED + " " + CANCELLED_IN_CHINESE;

	public static final String CHINESE_ENCODING = "GB2312"; // "GB18030" super seeds GB2312, but not support by mysql
															// 5.0
	// printer control characters Initialize printer P 57
	protected static final byte init[] = { 27, 64 };
	// protected static final byte standardMode[] = {12}; //p15 t88info.pdf

	// Line Spacing Commands p16 max spacing is 255/180 inches = 35.983 mm {27, 51,
	// n} 0 <= n <= 255 default is 60 for the paper feed amount is 1/6 inch
	// equivalent to 30 dots 1/4 inch = 45 dots. From 0 to 30 are no line spacing
	protected static final byte defaultLineSpacing[] = { 27, 50 }; // default line spacing
	protected static final byte lineSpacing00[] = { 27, 51, 00 }; // 0 from 0 to 35 is no line spacing (by test)
	protected static final byte lineSpacing60[] = { 27, 51, 60 }; // 1 n = 60 is default line spacing
	protected static final byte lineSpacing80[] = { 27, 51, 80 }; // 2
	protected static final byte lineSpacing100[] = { 27, 51, 100 };// 3
	protected static final byte lineSpacing125[] = { 27, 51, 125 };// 4

	// Character Spacing Commands p17 {27, 32, n} 0 <= n <= 255 default is 0 max
	// spacing is 255/180 inches = 35.983 mm sets tte right-side character spacing
	// to n X (horizontal or vertical motion unit)
	protected static final byte defaultEngCharSpacingIs00[] = { 27, 32, 0 }; // default no char spacing
	protected static final byte engCharSpacing01[] = { 27, 32, 2 };
	protected static final byte engCharSpacing02[] = { 27, 32, 4 };
	protected static final byte engCharSpacing03[] = { 27, 32, 6 };
	protected static final byte engCharSpacing04[] = { 27, 32, 8 };

	// Chinese Character Spacing Command on p 114 t88info_chi.fpd 0 < = n1, or n2 <=
	// 255. max spacing is 255/180 inches = 35.983 mm {28, 83, n1, n2} n1 is the
	// Left spacing n2 is the Right spacing
	protected static final byte defaultChiCharSpacingIs00[] = { 28, 83, 0, 0 }; // default no char spacing
	protected static final byte chiCharSpacing01[] = { 28, 83, 0, 2 };
	protected static final byte chiCharSpacing02[] = { 28, 83, 0, 4 };
	protected static final byte chiCharSpacing03[] = { 28, 83, 0, 6 };
	protected static final byte chiCharSpacing04[] = { 28, 83, 0, 8 };

	// p22 t88info.pdf the default font is size A (n=0) {27, 33, n} 0 <= n <= 255
	// default is 0 W1H1 = 00, W1H2 == 16, W2H1 = 32, W2H2 = 16+32 = 48
	protected static final byte font12x24_W1H1[] = { 27, 33, 00 }; // Single width, Single-Height
	protected static final byte font12x24_W1H2[] = { 27, 33, 16 }; // Single-Width, Double-Height
	protected static final byte font12x24_W2H1[] = { 27, 33, 32 }; // Double-Width, Single-Height
	protected static final byte font12x24_W2H2[] = { 27, 33, 48 }; // Double-Width, Double-Height

	// p22 t88info.pdf set the n = 1 for font size B {27, 33, n} 0 <= n <= 255
	// default is 0 W1H1 = 01, W1H2 == 16+1, W2H1 = 32+1, W2H2 = W1H2 + W2H1 = 48+1
	// font size: A = 12x24, B = 9x17 see page 62 t88info_chi.pdf
	protected static final byte font09x17_W1H1[] = { 27, 33, 01 };
	// protected static final byte font09x17_W1H2[] = {27, 33, 17};
	// protected static final byte font09x17_W2H1[] = {27, 33, 33};
	protected static final byte font09x17_W2H2[] = { 27, 33, 49 };

	// p26 t88info.pdf This command is effective for English and Chinese Font {29,
	// 33, n} 0 <= n <= 255 default is 0 Width: W1=00, W2=16, W3=32, W4=48, W5=64,
	// W6=80, W7=96, W8=112 Hight: H1=00, H2=01, H3=02, H4=03, H5=04, H6=05, H7=06,
	// H8=07 W2H2 = W2(16) + H2(01) = 17 W3H3 = W3(32) + H3(02) = 34

	// protected static final byte fontW1H1[] = {29, 33, 00};
	// protected static final byte fontW1H2[] = {29, 33, 01};
	// protected static final byte fontW2H1[] = {29, 33, 16};
	// protected static final byte fontW2H2[] = {29, 33, 17};
	// protected static final byte fontW3H3[] = {29, 33, 34};

	// p108 t88info_chi.pdf Chinese Font Control command {28, 33, 0x04} 0 <= n <=
	// 255 default is 0 to allowChineseFont double width and double height: W1H1 =
	// 00, W2H1 = 4, W1H2 = 8, W2H2 = W1H2 + W2H1 = 4+8 = 12 to disAllowChineseFont
	// double width and double height: {28, 33, 00} for underline n = 128

	// protected static final byte allowChineseFontW2H1[] = {28, 33, 4}; //p108
	// Chinese version
	protected static final byte allowChineseFontW1H2[] = { 28, 33, 8 };
	protected static final byte allowChineseFontW2H2[] = { 28, 33, 12 };
	protected static final byte disAllowChineseFontWxHx[] = { 28, 33, 00 };

	// p61 i88info.pdf
	// protected static final byte realTtimeRequest[] = {16, 5, 2};
	// p31 t88info.pdf the default is left justification
	protected static final byte alignLeft[] = { 27, 97, 0 };
	protected static final byte alignCenter[] = { 27, 97, 1 };
	protected static final byte alignRight[] = { 27, 97, 2 };

	// p 26 t88info.pdf Characters are printed in white on a black background p 26
	// t88info.pdf
	protected static final byte whiteBlackReverseOff[] = { 29, 66, 0 };
	protected static final byte whiteBlackReverseOn[] = { 29, 66, 1 };

	// p25 t88info.pdf p24 t88info.pdf prints 180 deg rotated characters from right
	// to left. p23 t88info.pdf emphasized = double-strike
	// protected static final byte clockwiseRotation90[] = {27, 86, 1};
	// protected static final byte upsideDownOn[] = {27, 123, 1};
	// protected static final byte emphasized[] = {27, 69, 1};

	// p22 t88info.pdf When underline mode is on, 90 degree clockwise rotated
	// characters and //p22 t88info.pdf white/black reverse characters cannot be
	// underlined. The default setting is n=0.
	// protected static final byte underline1dotWidth[] = {27, 45, 1};
	protected static final byte underline2dotWidth[] = { 27, 45, 2 };
	// protected static final byte smoothing[] = {29, 98, 1}; //p27 t88info.pdf

	// partial cut paper P 55
	protected static final byte cut[] = { 29, 86, 1 };

	// p59 Drawer kick-out pulse 00=pin 2, 01=pin 5 pin4=24 volt
	protected static final byte openDrawer[] = { 27, 112, 0, 100, (byte) 200 }; // was {0x1b, 0x70, 0x00, 0x64, 0x128}
	protected static final byte printerAlarm[] = { 27, 112, 01, 100, (byte) 200 }; // was {0x1b, 0x70, 0x00, 0x64,
																					// 0x128}

	// Panel Button Commands p28
	// Paper Sensor Commands p28
	// Print Position Commands p30
	// Bit-Map Images Commands p37
	// Status Commands p41
	// Bar Code Commands p48
	// Marco Function Commands p53
	// Mechanism Control Commands p55
	// Miscellaneous Function Commands p56

	protected static final long couponValidDuration1 = 45 * 24 * 60 * 60; // 45 days in seconds
	// private PrinterSetting printerSetting_;

	// set the default value
	protected boolean printCoupon = true;
	protected boolean printOrderNumberOnOrder;
	protected boolean printCheckNumberOnOrder = true;
	// protected boolean printInvoiceNumberOnOrder = false;
	protected boolean printOrderNumberOnReceipt = false;
	protected int englishCharSpacing = 0;
	protected int chineseCharSpacing = 0;
	protected int lineSpacingOnOrd = 1;
	protected int lineSpacingOnReceipt = 0;

	protected byte lineSpacingPrtOrdInCSV[];
	protected byte lineSpacingPrtReceiptInCSV[];
	protected byte engCharSpacingInCSV[];
	protected byte chiCharSpacingInCSV[];

	protected byte dishNameFont[]; // = font12x24_W1H2;
	protected byte engCharSpacing[]; // = defaultEngCharSpacingIs00;
	protected byte customizerFont[]; // = font12x24_W1H2;
	protected byte chiDishNameFont[]; // = allowChineseFontW1H2; // W1H1, or W1H2, or W2H2
	protected byte chiCharSpacing[]; // = defaultChiCharSpacingIs00;
	protected byte chiCustomizerFont[]; // = allowChineseFontW1H2;
	protected byte orderItemTotalFont[]; // = font12x24_W1H1;
	protected byte lineSpacing[]; // = defaultLineSpacing; // set to the default line spacing

	protected String location;
	protected PrintService printService;

	protected ByteArrayOutputStream out;

	// public PrinterBase(String location, PrintService printService, Client gui)

	// public PrinterBase(String location, PrintService printService, Map<Integer,
	// Menu> menus, Map<Integer, Employee> idToEmployee) {
	public PrinterBase(String location, PrintService printService) {
		logger.finest("Entered");
		this.location = location;
		this.printService = printService;

		printOrderNumberOnOrder = Configuration.getPrintOrderNumberOnOrder();
		printCheckNumberOnOrder = Configuration.getPrintCheckNumberOnOrder();
		// printInvoiceNumberOnOrder = Configuration.getPrintInvoiceNumberOnOrder();
		printOrderNumberOnReceipt = Configuration.getPrintOrderNumberOnReceipt();
		englishCharSpacing = Configuration.getEnglishCharacterSpacing();
		chineseCharSpacing = Configuration.getChineseCharacterSpacing();
		lineSpacingOnOrd = Configuration.getOrderLineSpacing();
		lineSpacingOnReceipt = Configuration.getReceiptLineSpacing();

		// print order line spacing is the spacing between dishes, no effect on the
		// customizer
		if (lineSpacingOnOrd == 4) {
			lineSpacingPrtOrdInCSV = lineSpacing125;
		} else if (lineSpacingOnOrd == 3) {
			lineSpacingPrtOrdInCSV = lineSpacing100;
		} else if (lineSpacingOnOrd == 2) {
			lineSpacingPrtOrdInCSV = lineSpacing80;
		} else if (lineSpacingOnOrd == 1) {
			lineSpacingPrtOrdInCSV = lineSpacing60;
		} else if (lineSpacingOnOrd == 0) {
			lineSpacingPrtOrdInCSV = lineSpacing00;
		} else {
			// if no match is found set the line spacing to default setting.
			lineSpacingPrtOrdInCSV = defaultLineSpacing; // default line spacing is lineSpacing60
		}

		// print receipt line spacing is the spacing between dishes,
		// no effect on the customizer (customer copy)
		if (lineSpacingOnReceipt == 4) {
			lineSpacingPrtReceiptInCSV = lineSpacing125;
		} else if (lineSpacingOnReceipt == 3) {
			lineSpacingPrtReceiptInCSV = lineSpacing100;
		} else if (lineSpacingOnReceipt == 2) {
			lineSpacingPrtReceiptInCSV = lineSpacing80;
		} else if (lineSpacingOnReceipt == 1) {
			lineSpacingPrtReceiptInCSV = lineSpacing60;
		} else if (lineSpacingOnReceipt == 0) {
			lineSpacingPrtReceiptInCSV = lineSpacing00;
			// if no match is found, set the line spacing to lineSpacing00
		} else {
			lineSpacingPrtReceiptInCSV = lineSpacing00;
		}

		// engCharSpacing are effect on print Order in English and print Order in
		// English and Chinese in two line. Spacing only effect on dish Name in English,
		// not the customizer.
		if (englishCharSpacing == 4) {
			engCharSpacingInCSV = engCharSpacing04;
		} else if (englishCharSpacing == 3) {
			engCharSpacingInCSV = engCharSpacing03;
		} else if (englishCharSpacing == 2) {
			engCharSpacingInCSV = engCharSpacing02;
		} else if (englishCharSpacing == 1) {
			engCharSpacingInCSV = engCharSpacing01;
		} else {
			engCharSpacingInCSV = defaultEngCharSpacingIs00;
		}

		// chiCharSpacing are effect on print Order in Chinese and print Order in
		// English and Chinese in two line. Spacing only effect on dish Name in Chinese,
		// not the customizer.
		if (chineseCharSpacing == 4) {
			chiCharSpacingInCSV = chiCharSpacing04;
		} else if (englishCharSpacing == 3) {
			chiCharSpacingInCSV = chiCharSpacing03;
		} else if (englishCharSpacing == 2) {
			chiCharSpacingInCSV = chiCharSpacing02;
		} else if (englishCharSpacing == 1) {
			chiCharSpacingInCSV = chiCharSpacing01;
		} else {
			chiCharSpacingInCSV = defaultChiCharSpacingIs00;
		}

		out = new ByteArrayOutputStream();
	}

	protected String getLocation() {
		return location;
	}

	protected void openDrawer() throws IOException, Exception {
		logger.finest("Entered");
		out.write(init);
		out.write(openDrawer);
		out.write(init); // to clear the openDrawer function

		sendOutputStreamToPrinter();
	}

	protected void printAlarm() throws IOException, Exception {
		logger.finest("Entered");
		out.write(init);
		out.write(printerAlarm);
		out.write(init); // to clear the openDrawer function

		sendOutputStreamToPrinter();
	}

	protected void printRestaurantLogo() throws IOException {
		logger.finest("Entered");
		out.write(init);
		out.write(alignCenter);
		out.write(font09x17_W2H2);
		out.write(Configuration.getRestaurantName().getBytes());
		out.write(lineSpacing80);
		out.write(defaultLineSpacing);
		out.write(alignCenter);
		out.write(font09x17_W1H1);

		if (Configuration.getRestaurantAddress().isEmpty() == false) {
			out.write(('\n' + Configuration.getRestaurantAddress() + '\n').getBytes());
		}

		if (Configuration.getRestaurantCityStateZip().isEmpty() == false) {
			out.write((Configuration.getRestaurantCityStateZip() + '\n').getBytes());
		}

		if (Configuration.getRestaurantPhone().isEmpty() == false) {
			out.write((Configuration.getRestaurantPhone() + '\n').getBytes());
		}

		if (Configuration.getRestaurantNextToLandmark().isEmpty() == false) {
			out.write((Configuration.getRestaurantNextToLandmark() + '\n').getBytes());
		}
	}

	protected void printOrderNumber(Cart cart) throws IOException {
		logger.finest("Entered");
		out.write(font12x24_W2H2);
		out.write((cart.getOrder().getOrderNumber() + "\n").getBytes());
		out.write("\n".getBytes());
	}

	protected void printPhoneNumber(Cart cart) throws IOException {
		logger.finest("Entered");
		out.write(font09x17_W1H1);
		out.write(alignCenter);
		out.write(font12x24_W2H1);
		out.write((cart.getToGoInfo().getFormattedPhoneNumber() + '\n').getBytes());
	}

	protected void printOrderTypeHeading(Cart cart) throws IOException {
		logger.finest("Entered");
		out.write(alignCenter);
		out.write(font12x24_W2H1);
		switch (cart.getOrder().getType()) {
		case DELIVERY:
			out.write("-- D E L I V E R Y --\n\n".getBytes());
			break;
		case PHONE_IN:
			out.write("-- P H O N E - I N --\n\n".getBytes());
			break;
		case WALK_IN:
			out.write(whiteBlackReverseOn);
			out.write(font12x24_W2H2);
			out.write("--- W A I T I N G ---\n\n".getBytes());
			out.write(whiteBlackReverseOff);
			break;
		case DINE_IN:
			out.write("--- D I N E - I N ---\n\n".getBytes());
			break;
		}
	}

	protected void printOrderTypeSpecifics(Cart cart) throws IOException {
		logger.finest("Entered");
		OrderType orderType = cart.getOrder().getType();
		switch (orderType) {
		case DINE_IN:
			DineInInfo dineInInfo = cart.getDineInInfo();
			Employee serverEmployee = Client.idToEmployee.get(dineInInfo.getServerId());
			String serverNickname = serverEmployee == null ? "" : serverEmployee.getNickname();
			out.write(font09x17_W1H1);
			out.write("Table: ".getBytes());
			out.write(font12x24_W2H2);
			out.write(dineInInfo.getTableNumber().getBytes());
			out.write(font09x17_W1H1);
			out.write("   Guests: ".getBytes());
			out.write(font12x24_W2H2);
			if (cart.getDineInInfo().getGuestCount() > 0) {
				out.write(String.valueOf(dineInInfo.getGuestCount()).getBytes());
			}
			out.write(font09x17_W1H1);
			out.write("   Server: ".getBytes());
			out.write(font12x24_W1H1);
			out.write((serverNickname + "\n").getBytes());
			break;
		default:
			out.write(font09x17_W1H1);
			// out.write("Name: ".getBytes());
			out.write(font12x24_W2H1);
			out.write((cart.getToGoInfo().getCustomerName() + "\n").getBytes());

			break;
		}
	}

	protected void printDeliverySpecifics(Cart cart) throws IOException {
		logger.finest("Entered");
		DeliveryInfo deliveryInfo = cart.getDeliveryInfo();
		out.write(font09x17_W1H1);
		// out.write("Addr: ");
		out.write(font12x24_W2H1);
		out.write((deliveryInfo.getStreet() + '\n').getBytes());
		out.write(font09x17_W1H1);
		// out.write(" ");
		out.write(font12x24_W1H1);
		if (deliveryInfo.getCity().isEmpty() == false) {
			out.write((deliveryInfo.getCity() + ", " + deliveryInfo.getState() + " " + deliveryInfo.getZip() + '\n')
					.getBytes());
		}
	}

	protected void printDayOfTheWeek(Cart cart) throws IOException {
		logger.finest("Entered");
		String dayOfTheWeek = Util.getDayOfTheWeek(cart.getOrder().getOrderedTime());
		Employee serverEmployee = Client.idToEmployee.get(cart.getDineInInfo().getServerId());
		String serverNickname = serverEmployee == null ? "" : serverEmployee.getNickname();
		if (serverNickname.isEmpty() == false) {
			out.write((dayOfTheWeek + ", Server: " + serverNickname).getBytes());
		} else {
			out.write((dayOfTheWeek + '\n').getBytes());
		}
	}

	protected void printDateAndTime(Cart cart) throws IOException {
		logger.finest("Entered");
		out.write(font12x24_W2H1);
		out.write(("-" + Util.formatDateAndTimeNoSeconds(cart.getOrder().getOrderedTime()) + "-").getBytes());
		out.write(font12x24_W2H1);
		out.write("\n\n".getBytes());
	}

	protected void printRequestedTime(Cart cart) throws IOException {
		logger.finest("Entered");
		out.write(alignCenter);
		out.write(font12x24_W2H1);
		out.write("Requested Time\n".getBytes());
		out.write("\n".getBytes());
		out.write(font12x24_W2H2);
		out.write(whiteBlackReverseOn);
		out.write(("------ " + Util.formatTimeNoSeconds(cart.getToGoInfo().getRequestedTime()) + " -----\n\n")
				.getBytes());
		out.write(init); // clear the alignCenter
	}

	protected void printAllNoIngredient(String englishAndChineseNames) throws IOException {
		logger.finest("Entered");
		out.write(font09x17_W2H2);
		out.write(allowChineseFontW1H2);
		out.write(lineSpacing100);
		out.write(("   " + englishAndChineseNames + '\n').getBytes(CHINESE_ENCODING));
		out.write(disAllowChineseFontWxHx);
		out.write(lineSpacing60);
	}

	protected void printOrderNote(Cart cart) throws IOException {
		logger.finest("Entered");
		out.write(font09x17_W2H2);
		out.write(("   " + cart.getOrder().getNote() + '\n').getBytes());
	}

	protected void printCustomerNote(Cart cart) throws IOException {
		logger.finest("Entered");
		out.write(font09x17_W2H2);
		out.write(("   " + cart.getToGoInfo().getNote() + '\n').getBytes());
	}

	protected void printVoidedOrder(Cart cart, Calculator calculator) throws IOException {
		logger.finest("Entered");
		String voidTotal = String.format("%4.2f", calculator.getTotal()/100.0);
		out.write(font09x17_W2H2);
		out.write(alignRight);
		out.write(("VOIDED        - $" + voidTotal + " \n\n").getBytes());
		out.write("Balance Due:         0 \n".getBytes());
		out.write(init);
		out.write(font12x24_W1H1);
		out.write(
				("(Date:         " + Util.formatDateAndTimeNoSeconds(cart.getOrder().getPaidTime()) + '\n').getBytes());
		out.write("Autorized by: ".getBytes());
		out.write(font12x24_W2H1);
		out.write(("" + cart.getDineInInfo().getServerId() + '\n').getBytes());
		out.write(font12x24_W1H1);
		out.write("Signature:  ____________________________".getBytes());
		out.write("\n\n\n\n".getBytes());
	}

	protected void printDrivingDirection(Cart cart) throws IOException {
		logger.finest("Entered");
		out.write(font09x17_W1H1);
		out.write("Driving Direction:\n".getBytes());
		out.write(font12x24_W1H1);
		out.write((cart.getDeliveryInfo().getDrivingDirection() + '\n').getBytes());
	}

	protected void printOrderTotal(Cart cart, Calculator calculator) throws IOException {
		logger.finest("Entered");
		out.write(font12x24_W2H1);
		out.write("\t-----------------\n".getBytes());
		out.write(dishNameFont);
		out.write(("\tSubtotal").getBytes());
		out.write(String.format("%25.2f\n", calculator.getSubtotal()/100.0).getBytes());

		if (cart.getOrder().getDiscountPercent() > 0) {
			out.write(font12x24_W1H1);
			out.write(("\tPercent Off: " + calculator.getPercentOffInString()).getBytes());
			out.write(dishNameFont);
			out.write(String.format("            -%4.2f\n", calculator.getPercentDiscountAmount()/100.0).getBytes());

			out.write(font12x24_W2H1);
			out.write("\t-----------------\n".getBytes());
			out.write(font12x24_W1H1);
			out.write("\tTotal Taxable:".getBytes());
			out.write(String.format("%18.2f\n",(calculator.getSubtotal() - calculator.getPercentDiscountAmount())/100.0).getBytes());
		}

		out.write(font12x24_W1H1);
		out.write(String.format("\tTax: %4.2f%% %22.2f\n", cart.getOrder().getTaxRate() * 100,
				calculator.getTaxAmount() / 100.0).getBytes());

		if (cart.getDeliveryInfo().getDeliveryCharge() > 0) {
			out.write(font12x24_W1H1);
			out.write("\tDelivery Charge:".getBytes());
			out.write(String.format("%17.2f\n", cart.getDeliveryInfo().getDeliveryCharge()/100.0).getBytes());
		}

		if (cart.getOrder().getAdditionalAmount() > 0) {
			out.write(font12x24_W1H1);
			out.write("\tMisc Charge:".getBytes());
			out.write(String.format("%20.2f\n", cart.getOrder().getAdditionalAmount()/100.0).getBytes());
		}

		if (cart.getOrder().getDiscountAmount() > 0) {
			out.write(dishNameFont);
			out.write("\tCoupon: ".getBytes());
			out.write(String.format("                    -%4.2f\n", cart.getOrder().getDiscountAmount()/100.0).getBytes());
		}

		out.write(font12x24_W2H1);
		out.write("=====================\n".getBytes());
		out.write(font09x17_W2H2);
		out.write("\tTotal:  ".getBytes());
		// If the '$' printed out as a Yuan character ¥, use the TM-T88V Utility ->
		// Font -> International character set: 'USA', [Not China], then hit the set
		// button
		out.write(String.format("$%13.2f\n\n", calculator.getTotal()/100.0).getBytes());
	}

	protected void printOrderPayment(Cart cart, Calculator calculator) throws IOException {
		logger.finest("Entered");
		if (cart.getPaymentTray().getTotalTendered() > 0) {
			out.write(font12x24_W1H1);
			if ((cart.getPaymentTray().getPaymentCount(PaymentType.CASH)) > 0) {
				out.write(("\t     Cash: " + String.format("%21.2f\n", cart.getPaymentTray().getTotalTendered(PaymentType.CASH)/100.0)).getBytes());
			}
			if (cart.getPaymentTray().getPaymentCount(PaymentType.CREDIT_CARD) > 0) {
				out.write(("\t     Card: " + String.format("%21.2f\n", cart.getPaymentTray().getTotalTendered(PaymentType.CREDIT_CARD)/100.0)).getBytes());
			}
			if (cart.getPaymentTray().getPaymentCount(PaymentType.CHECK) > 0) {
				out.write(("\t     Check: " + String.format("%20.2f\n", cart.getPaymentTray().getTotalTendered(PaymentType.CHECK)/100.0)).getBytes());
			}

			out.write(("\tTotal Tendered: "	+ String.format("%16.2f\n", cart.getPaymentTray().getTotalTendered()/100.0)).getBytes());

			int balanceDue = calculator.getTotal() - cart.getPaymentTray().getTotalTendered();
			if (balanceDue > 0) {
				out.write(font09x17_W2H2);
				out.write(("\tBALANCE DUE: " + String.format("%9.2f", balanceDue/100.0 + '\n')).getBytes());
				out.write(init);
			} else { // Change
				if (balanceDue == 0) {
					out.write(init);
					out.write(("\tCHANGE: " + String.format("%24s", " 0.00\n")).getBytes());
				} else {
					out.write(("\tCHANGE: " + String.format("%24.2f-\n", -balanceDue/100.0)).getBytes());
				}
				out.write(init);
			}
		}

		if (cart.getOrder().getStatus() == OrderStatus.PAID) {
			out.write(font12x24_W2H1);
			out.write("\t\tPaid\n".getBytes());
		}
	}

	protected void printGratuityGuideline(Cart cart, Calculator calculator) throws IOException {
		logger.finest("Entered");
		List<Integer> gratuityPercentages = Configuration.getPrintGratuityPercentages();
		if (gratuityPercentages == null) {
			return;
		}

		out.write(alignCenter);
		out.write(font12x24_W1H1);
		out.write("Gratuity Guideline\n".getBytes());
		out.write(alignLeft);

		for (int percentage : gratuityPercentages) {
			int gratuity = calculator.getSubtotal() * percentage / 100;
			int totalPlusGratuity = calculator.getTotal() + gratuity;
			//escape the percent sign is: %%
			out.write(String.format("%2d%%=$%5.2f   Total: $%-6.2f\n", percentage, gratuity/100.0, totalPlusGratuity/100.0).getBytes());
		}
	}

	protected void printedVoided(String voidedName, int voidedById, long voidedTime) throws IOException {
		logger.finest("Entered");
		out.write(font09x17_W1H1);
		out.write(disAllowChineseFontWxHx);
		out.write((voidedName + '\n').getBytes(CHINESE_ENCODING));
		Employee employee = Client.idToEmployee.get(voidedById);
		String employeeNickname = employee == null ? "" : employee.getNickname();
		if (employeeNickname.isEmpty() == false) {
			out.write((" by: " + employeeNickname).getBytes());
		}
		out.write(("        Time: " + Util.formatTimeNoSeconds(voidedTime) + "\n\n").getBytes());
	}

	protected void sendOutputStreamToPrinter() throws Exception {
		logger.finest("Entered");
		boolean debug = false;
		if (debug) {
			// See printout in standard out including unreadable control characters.
			// save some printing paper by printing to console during debugging
			int i = 0;
			byte[] bytes = out.toByteArray();
			while (i < bytes.length) {
				byte b = bytes[i];
				if (b >=27 && b <=31) {
					++i; //skip the next control character, this does not cover all cases. 
				} else if (b > 31 && b < 128 || b == 10) {
					System.out.print((char)b);
				}
				++i;
			}
		} else {
			DocPrintJob docPrintJob = printService.createPrintJob();

			PrintRequestAttributeSet pras = new HashPrintRequestAttributeSet();
			DocAttributeSet das = new HashDocAttributeSet();

			DocFlavor flavor = DocFlavor.BYTE_ARRAY.AUTOSENSE;

			// Doc simpleDoc = new SimpleDoc(outputStream.toByteArray(), flavor, null);
			// null in place of das works also
			Doc simpleDoc = new SimpleDoc(out.toByteArray(), flavor, das);

			// docPrintJob.print(simpleDoc, null); //null in place of pras works also
			docPrintJob.print(simpleDoc, pras);
		}
	}
}
