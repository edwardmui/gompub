package com.orderfoodnow.pos.frontend;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.orderfoodnow.pos.shared.ConfigKeyDefs;
import com.orderfoodnow.pos.shared.Configuration;
import com.orderfoodnow.pos.shared.staff.Employee;

public class HelpText {
	private static final Logger logger = Logger.getLogger(HelpText.class.getName());

	public static String showHelp(Employee employee) {
		logger.finest("Entered");
		StringBuilder sb = new StringBuilder();
		sb.append("<html>")
				.append("?  Questionmark to show this help. Input text can be entered in upper or lower case.<br/>")
				.append("`  Back tick to enter/update Phone, Name, or Dine-in table number; Enter address with comma separated field after name. Template: `name, stree, city, state, zip.<br/>")
				.append("=  Equal sign to void or specify/update order type: =V void, =P Phone-in, =W Walk-in, =I Dine-in, =D Delivery. =D# where # is zone number.<br/>")
				.append("$  Dollar sign to enter payment amount for payment type: $c cash, $r credit card, $k check. Order can have more than one payment.<br/>")
				.append("/  Slash to search order. 1-4 digits: Order number; 4-10: last digits of phone; Characters: customer name.<br/>")
				.append("#  Pound Sign to enter credit card number. Credit card number will be erased upon order settlement.<br/>")
				.append("@  At Sign to enter house number and street name. Usefule for paste street name from an online map.<br/>")
				.append("[  Square Bracket to enter dish customizer(s) that is not on the menu. Numerical value at the end is the price. example: [x dried shrimp 500]<br/>")
				.append("-  Minus Sign to enter/update the discount amount in cent (i.e no decimal) to subtract from the order total.<br/>")
				.append("+  Plus Sign to enter/update the miscellaneous charges in cent to add to the order total.<br/>")
				.append("\" Double Quote to enter/update the customer note. It's used for this order and future orders from this customer.<br/>")
				.append("'  Single Quote to enter an order note for this order for single use.<br/>")
				.append(":  Colon to enter/update requested time. e.g. 7:45, optional A/P for AM/PM. ommitting the hour adds minutes to current time. e.g. :45. :0 to clear requested time.<br/>")
				.append(">  Greater than sign to specify/update employee with two-letter initials. >Dxx for driver or >Sxx for server where xx is the employee's two-letter initials<br/>")
				.append("<br>")
				.append("Enter dish name or dish code in the input box to search best matching dishes. Spacebar or Enter key adds the hightlighted large dish size to the order.<br/>")
				.append("Double click on the name, code, or prices of the dish adds it to the order.<br/>")
				.append("Single or double digits infront of a dish name or dish code specifies the quantity for the highlighted dish to be added. Default to large without a dot.<br/>")
				.append("A period/dot '.' anywhere in the input box during dish searching specifies the small size if available.<br/>")
				.append("Customizers can be added to the order table highlighted dish by selecting them from the menu (or use this template: [name price] for nonmenu customizers.<br/>")
				.append("Exchangers can be specified to the order table highlighted dish for similar items or upgrade for a fee. e.g. upgrade steam rice to fried rice.<br/>")
				.append("Double click or delete key on selected row in the order table to remove/void/unvoid the item depending on that status of the item.<br/>")
				.append("Delete key on selected row in the arrival table removes it from displaying. It cannot be undone as it's not modifiable due to it's paid. Double click shows the order.<br/>")
				.append("Clicking or selecting a row in the business table at the bottom displays the order in the order table. Use configured FKey to display unsettled, unpaid, or stashed orders.<br/>")
				.append("Arrival table is shown when it's not empty and total panel is not used on displaying an order. Arrow keys can be used to navigate within a table.<br/>")
				.append("Alt+underscored letter to select a button. e.g. Alt+D to select Delivery. Alt+Y to select 'Yes' on popup response.<br><br/>")
				;

		try {
			Map<String, List<Map<String, String>>> configuredEventToFunctionKey = Configuration
					.getConfiguredEventToActionAttributes();

			for (int i = 1; i <= 12; ++i) {
				String functionKey = "f" + i;
				appendToHelp(configuredEventToFunctionKey, functionKey, sb, employee);
			}
			for (int i = 1; i <= 12; ++i) {
				String functionKey = "shiftF" + i;
				appendToHelp(configuredEventToFunctionKey, functionKey, sb, employee);
			}
			for (int i = 1; i <= 12; ++i) {
				String functionKey = "altF" + i;
				appendToHelp(configuredEventToFunctionKey, functionKey, sb, employee);
			}
		} catch (Exception e) {
			logger.warning("Unable to configuredEventToFunctionKey. " + e.getMessage());
			e.printStackTrace();
		}

		sb.append("</html>");
		return sb.toString();
	}

	private static void appendToHelp(Map<String, List<Map<String, String>>> configuredEventToFunctionKey,
			String functionKey, StringBuilder sb, Employee employee) {
		List<Map<String, String>> actions = configuredEventToFunctionKey.get(functionKey);

		boolean hasContent = false;
		String helpLine = functionKey + ": ";
		int actionCount = actions.size();
		for (Map<String, String> action : actions) {
			String actionName = action.get(ConfigKeyDefs.configuredEventToActionAttributes_action);
			actionCount--;
			if (actionName.equals(ConfigKeyDefs.noAction)) {
				continue;
			}

			PermissionRequiredType permissionRequiredType = PermissionRequiredType.getEnum(actionName);
			if (permissionRequiredType == null || PermissionUtil.hasPermission(permissionRequiredType, employee)) {
				hasContent = true;
				String locationName = action.get(ConfigKeyDefs.configuredEventToActionAttributes_location);
				if (locationName == null) {
					helpLine = helpLine + actionName;
				} else {
					helpLine = helpLine + actionName + " at " + locationName;
				}

				if (helpLine.length() > 120) {
					helpLine += (actionCount == 0) ? "." : ",";
					helpLine += "<br/>";
					sb.append(helpLine);
					helpLine = " --> ";
					hasContent = false;
				} else {
					helpLine = helpLine + ", ";
				}
			}
		}

		if (hasContent) {
			helpLine = helpLine.trim();
			int lastCharIndex = helpLine.length() - 1;
			if (lastCharIndex > 0 && helpLine.charAt(lastCharIndex) == ',') {
				helpLine = helpLine.substring(0, lastCharIndex);
				helpLine = helpLine + ".";
			}

			helpLine = helpLine + "<br/>";
			sb.append(helpLine);
		}
	}
}
