package com.orderfoodnow.pos.shared;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import com.orderfoodnow.pos.shared.menu.Dish;

public class Util {
	private static final Logger logger = Logger.getLogger(Util.class.getName());
	public static String formatTime(long epochMillis) {
		logger.finest("Entered");
		DateFormat df = SimpleDateFormat.getTimeInstance(DateFormat.LONG, Locale.US);
		String formattedTime = df.format(new Date(epochMillis));// HH:MM:SS AM CST
		if (formattedTime.length() - 4 < 0) {
			return "";
		}

		String formattedDate = formattedTime.substring(0, formattedTime.length() - 4); // drop CST
		return (formattedDate);
	}

	public static String formatTimeNoSeconds(long epochMillis) {
		logger.finest("Entered");
		DateFormat df = SimpleDateFormat.getTimeInstance(DateFormat.SHORT, Locale.US);
		String formattedTime = df.format(new Date(epochMillis)); // HH:MM AM
		return (formattedTime);
	}

	public static String formatDateAndTimeNoSeconds(long epochMillis) {
		logger.finest("Entered");
		DateFormat df = SimpleDateFormat.getTimeInstance(DateFormat.SHORT, Locale.US);
		String formattedTime = df.format(new Date(epochMillis)); // HH:MM AM
		return (String.format("%1$TY-%1$Tm-%1Td", epochMillis) + " " + formattedTime);
	}

	public static String formatEpochToLocal(long epochMillis) {
		logger.finest("Entered");
		String timeString = String.valueOf(epochMillis);
		if (epochMillis > 0) {
			timeString += "(" + LocalDateTime.ofEpochSecond(epochMillis / 1000, (int) (epochMillis % 1000 * 1000000),
					OffsetDateTime.now().getOffset()).toString() + ")";
		}
		return timeString;
	}

	public static String getDayOfTheWeek(long epochMillis) {
		logger.finest("Entered");
		// Sunday is defined to have value of 1 in Calendar
		final String[] DAY_OF_THE_WEEK = { "", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday",
				"Saturday" };

		return DAY_OF_THE_WEEK[getDayOfWeek(epochMillis)];
	}

	public static int getDayOfWeek(long epochMillis) {
		logger.finest("Entered");
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(epochMillis);
		return calendar.get(Calendar.DAY_OF_WEEK);
	}

	public static String formatDouble(double d, int maxFractionDigits, int minFractionDigits) {
		logger.finest("Entered");
		DecimalFormat formatter = new DecimalFormat();
		formatter.setMaximumFractionDigits(maxFractionDigits);
		formatter.setMinimumFractionDigits(minFractionDigits);
		formatter.setGroupingUsed(false);
		return formatter.format(d);
	}

	public static int parseInt(String s) {
		logger.finest("Entered");
		try {
			String trimmedS = s.trim();
			return Integer.parseInt(trimmedS);
		} catch (Exception e) {
			return -1;
		}
	}

	public static long parseTime(String s) {
		logger.finest("Entered");
		int colonIndex = s.indexOf(':');
		Calendar calendar = Calendar.getInstance(); // returns the current time
		if (colonIndex == 0) {
			try {
				calendar.add(Calendar.MINUTE, (Integer.valueOf(s.substring(1))).intValue());
				return calendar.getTime().getTime();
			} catch (NumberFormatException e) {
				return -1;
			}
		} else if (colonIndex + 3 > s.length()) {
			return -1;
		} else {
			try {
				calendar.set(Calendar.MINUTE,
						(Integer.valueOf(s.substring(colonIndex + 1, colonIndex + 3))).intValue());
			} catch (NumberFormatException e) {
				return -1;
			}

			int hour = 0;
			try {
				hour = (Integer.valueOf(s.substring(0, colonIndex))).intValue();
				if (hour < 1 || hour > 12) {
					return -1;
				}
			} catch (NumberFormatException e) {
				return -1;
			}

			if (s.endsWith("A")) {// AM
				if (hour == 12) { // 12:00am has HOUR_OF_DAY = 0 for next day
					hour = 24;
				}
			} else if (s.endsWith("P")) {// PM
				hour += 12;
			} else { // did not specify, so guessing, 11:xx is am, 12 - 10 is pm
				if (hour < 11) {
					hour += 12;
				}
			}

			calendar.set(Calendar.HOUR_OF_DAY, hour);
		}
		return calendar.getTime().getTime();
	}

	public static String parsePhoneNumber(String formattedPhoneNumber) {
		logger.finest("Entered");
		if (formattedPhoneNumber == null) {
			return null;
		}

		return formattedPhoneNumber.replaceAll("\\(", "").replaceAll("\\)", "").replaceAll("-", "").replaceAll(" ", "")
				.trim();
	}

	public static long roundToNearestFiveMinutes(long time) {
		logger.finest("Entered");
		final long fiveMinutes = 5 * 60 * 1000;
		return Math.round((double) ((double) time / (double) fiveMinutes)) * fiveMinutes;
	}

	public static void printStackTrace() {
		logger.finest("Entered");
		for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
			if (ste.toString().contains("com.orderfoodnow")) {
				logger.fine(ste.toString());
				System.out.println(ste);
			}
		}
	}

	public static List<String> formatDishAttributes(Dish dish) {
		//logger.finest("Entered");
		List<String> dishAttributes = new ArrayList<>();
		dishAttributes.add("  " + dish.getCode());
		dishAttributes.add("  " + dish.getShortName());
		int largePrice = dish.getLargePrice();
		if (largePrice == 0) {
			dishAttributes.add("    -    ");
		} else {
			dishAttributes.add(String.format("%5.2f ", largePrice/100.0));
		}

		int smallPrice = dish.getSmallPrice();
		if (smallPrice == 0) {
			dishAttributes.add("    -    ");
		} else {
			dishAttributes.add(String.format("%5.2f ", smallPrice/100.0));
		}

		String chineseName = dish.getChineseName();
		if (chineseName == null) {
			dishAttributes.add("  -   ");
		} else {
			dishAttributes.add("     " + chineseName);
		}

		return dishAttributes;
	}

	public static String buildCommaSepartedString(String[] strings, int max) {
		logger.finest("Entered");
		if (max > strings.length) {
			return "";
		}

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < max; i++) {
			sb.append(strings[i].trim()).append(',');
		}

		return sb.toString();
	}
}
