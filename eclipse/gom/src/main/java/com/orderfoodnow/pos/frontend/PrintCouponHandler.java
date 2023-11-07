package com.orderfoodnow.pos.frontend;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.print.PrintService;

import com.orderfoodnow.pos.shared.ConfigKeyDefs;
import com.orderfoodnow.pos.shared.Configuration;
import com.orderfoodnow.pos.shared.CouponVariantType;

public class PrintCouponHandler extends PrinterBase {
	private static Map<String, byte[]> controlToBytes;
	static {
		controlToBytes = new HashMap<String, byte[]>();
		controlToBytes.put("font12x24_W1H1", font12x24_W1H1);
		controlToBytes.put("font12x24_W1H2", font12x24_W1H2);
		controlToBytes.put("font12x24_W2H1", font12x24_W2H1);
		controlToBytes.put("font12x24_W2H2", font12x24_W2H2);
		controlToBytes.put("font09x17_W1H1", font09x17_W1H1);
		controlToBytes.put("font09x17_W2H2", font09x17_W2H2);
		controlToBytes.put("whiteBlackReverseOff", whiteBlackReverseOff);
		controlToBytes.put("whiteBlackReverseOn", whiteBlackReverseOn);
		controlToBytes.put("alignLeft", alignLeft);
		controlToBytes.put("alignCenter", alignCenter);
		controlToBytes.put("alignRight", alignRight);
	}

	public PrintCouponHandler(String location, PrintService printService) {
		super(location, printService);
	}

	public void print(CouponVariantType couponVariantType, Client clientGui) {
		logger.finest("Entered");
		try {
			print(couponVariantType, out);

			sendOutputStreamToPrinter();

			clientGui.setInfoFeedback("Coupon printed to " + location);
		} catch (Exception e) {
			logger.warning("Print Coupon failed. error: " + e.getMessage());
			clientGui.setWarningFeedback("Coupon was not printed. Check printer and try again...");
		}
	}

	public static void print(CouponVariantType couponVariantType, ByteArrayOutputStream out) throws Exception {
		logger.finest("Entered");
		List<String> content = Configuration.getCouponContent(couponVariantType);
		if (content == null) {
			logger.fine("Coupon specified has no content to print. Add content: [] coupon1 in conf.yaml file.");
			return;
		}

		out.write(init);
		out.write(alignCenter);

		List<String> header = Configuration.getCouponHeader();
		for (String line : header) {
			if (line.startsWith(ConfigKeyDefs.coupon___ctrl__)) {
				if (line.contains(ConfigKeyDefs.coupon_content_separator)) {
					String[] controlParts = line.split(ConfigKeyDefs.coupon_content_separator);
					if (controlParts.length > 1) {
						String controlKeyword = controlParts[1].trim();
						byte[] controlBytes = controlToBytes.get(controlKeyword);
						out.write(controlBytes);
					}
				}
			} else if (line.startsWith(ConfigKeyDefs.coupon___expiration__)) {
				if (line.contains(ConfigKeyDefs.coupon_content_separator)) {
					String[] expirationParts = line.split(ConfigKeyDefs.coupon_content_separator);
					if (expirationParts.length > 1) {
						String expirationLable = line.split(ConfigKeyDefs.coupon_content_separator)[1];
						int expiredInDays = Configuration.getCouponExpiredInDays(couponVariantType);
						long expiredInSeconds = expiredInDays * 24 * 60 * 60; // # days in second
						Date expDate = new Date(System.currentTimeMillis() + expiredInSeconds * 1000);
						String expDateInStr = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.US).format(expDate);
						out.write((expirationLable + " " + expDateInStr + '\n').getBytes());
					}
				}
			} else {
				out.write((line + '\n').getBytes());
			}
		}

		for (String line : content) {
			if (line.startsWith(ConfigKeyDefs.coupon___ctrl__)) {
				if (line.contains(ConfigKeyDefs.coupon_content_separator)) {
					String[] controlParts = line.split(ConfigKeyDefs.coupon_content_separator);
					if (controlParts.length > 1) {
						String controlKeyword = controlParts[1].trim();
						byte[] controlBytes = controlToBytes.get(controlKeyword);
						out.write(controlBytes);
					}
				}

			} else {
				out.write((line + '\n').getBytes());
			}
		}

		out.write(init);
		out.write("\n\n\n\n\n\n\n\n".getBytes());
		out.write(cut);
		out.write("\n".getBytes());
	}
}
