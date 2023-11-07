package com.orderfoodnow.pos.shared;

//The purpose of this class is to have the keyword spelling defined once and use in all java classes.
//The value must match in the conf.yaml file.
//Key words are basically all words is followed by a ':' in the conf.yaml file.

//the name of the definition uses exactly as the value to facilitate easy copy and paste to reduce typos.
//This deviates from the all UPPER convention.
public class ConfigKeyDefs {
	// prevent direct instantiation of this class
	private ConfigKeyDefs() {
	}

	// Required configuration keys
	public static final String restaurantName = "restaurantName";
	public static final String restaurantChineseName = "restaurantChineseName";
	public static final String restaurantAddress = "restaurantAddress";
	public static final String restaurantCity = "restaurantCity";
	public static final String restaurantState = "restaurantState";
	public static final String restaurantZip = "restaurantZip";
	public static final String restaurantPhone = "restaurantPhone";
	public static final String restaurantAreaCode = "restaurantAreaCode";
	public static final String restaurantTaxRate = "restaurantTaxRate";

	public static final String datasourceUrl = "datasourceUrl";
	public static final String datasourceUser = "datasourceUser";
	public static final String datasourcePassword = "datasourcePasword";
	public static final String datasourceDatabase = "datasourceDatabase";

	// Optional configuration keys
	public static final String noAction = "noAction";
	public static final String showMap = "showMap";
	public static final String showHelp = "showHelp";
	public static final String showOrderHistory = "showOrderHistory";
	public static final String toggleDishSize = "toggleDishSize";
	public static final String clearOrder = "clearOrder";
	public static final String stashOrder = "stashOrder";
	public static final String commitOrder = "commitOrder";
	public static final String refreshUnpaidOrders = "refreshUnpaidOrders";
	public static final String refreshUnsettledOrders = "refreshUnsettledOrders";
	public static final String refreshStashedOrders = "refreshStashedOrders";
	public static final String prepareForNextOrder = "prepareForNextOrder";

	public static final String dishCategories = "dishCategories";
	public static final String couponDish = "couponDish";
	public static final String condiment = "condiment";
	public static final String softDrink = "softDrink";
	public static final String alcoholDrink = "alcoholDrink";
	public static final String allNoIngredient = "allNoIngredient";
	public static final String exchangeCondiment = "exchangeCondiment";
	public static final String customizer = "customizer";

	public static final String restaurantLatitude = "restaurantLatitude";
	public static final String restaurantLongitude = "restaurantLongitude";
	public static final String restaurantNextToLandmark = "restaurantNextToLandmark";
	public static final String restaurantUrl = "restaurantUrl";
	public static final String restaurantOperationHours = "restaurantOperationHours";
	public static final String restaurantReceiptEndingText = "restaurantReceiptEndingText";
	public static final String restaurantDiningEndingText = "restaurantDiningEndingText";

	public static final String roundToNickel = "roundToNickel";
	public static final String printOrderNumberOnOrder = "printOrderNumberOnOrder";
	public static final String printCheckNumberOnOrder = "printCheckNumberOnOrder";
	public static final String printInvoiceNumberOnOrder = "printInvoiceNumberOnOrder";
	public static final String printOrderNumberOnReceipt = "printOrderNumberOnReceipt";
	public static final String deliveryCosts = "deliveryCosts";
	public static final String deliveryCities = "deliveryCities";
	public static final String deliveryStates = "deliveryStates";
	public static final String deliveryZips = "deliveryZips";
	public static final String deliveryStreetsPerZip = "deliveryStreetsPerZip";
	public static final String tableNumbers = "tableNumbers";
	public static final String printGratuityPercentages = "printGratuityPercentages";

	public static final String minimumSubtotalForDeliveryInCent = "minimumSubtotalForDeliveryInCent";
	public static final String deliveryOrderMustHavePhoneNumber = "deliveryOrderMustHavePhoneNumber";
	public static final String phoneInOrderMustHavePhoneNumber = "phoneInOrderMustHavePhoneNumber";
	public static final String restaurantCapabilities = "restaurantCapabilities";
	public static final String kitchenQueueTimeFactors = "kitchenQueueTimeFactors";
	public static final String weeklyBaseKitchenQueueTimeMinutes = "weeklyBaseKitchenQueueTimeMinutes";
	public static final int KITCHEN_QUEUE_TIME_FACTOR_SIZE = 4;

	public static final String maxHistoryOrderDisplayCount = "maxHistoryOrderDisplayCount";

	public static final String lineSpacings = "lineSpacings";
	public static final String lineSpacings_options = "options";
	public static final String lineSpacings_forOrder = "forOrder";
	public static final String lineSpacings_forReceipt = "forReceipt";

	public static final String characterSpacings = "characterSpacings";
	public static final String characterSpacings_options = "options";
	public static final String characterSpacings_forChinese = "forChinese";
	public static final String characterSpacings_forEnglish = "forEnglish";

	public static final String clientWindowResolutions = "clientWindowResolutions";
	public static final String clientWindowResolutions_options = "options";
	public static final String clientWindowResolutions_forLocalClient = "forLocalClient";

	public static final String roleOptions = "roleOptions";
	public static final String permissionRequiredActionOptions = "permissionRequiredActionOptions";
	public static final String actionPermission = "actionPermission";
	public static final String nonPrintingActionOptions = "nonPrintingActionOptions";
	public static final String printWaitingNoticeActionOptions = "printWaitingNoticeActionOptions";
	public static final String printCouponActionOptions = "printCouponActionOptions";
	public static final String printToGoPerStationActionOptions = "printToGoPerStationActionOptions";
	public static final String printDineInPerStationActionOptions = "printDineInPerStationActionOptions";
	public static final String printOrderActionOptions = "printOrderActionOptions";
	public static final String printOrderNoPriceActionOptions = "printOrderNoPriceActionOptions";

	public static final String printReceiptActionOptions = "printReceiptActionOptions";
	public static final String fontSizeOptions = "fontSizeOptions";
	public static final String toGoPrinterLocationToPrinter = "toGoPrinterLocationToPrinter";
	public static final String dineInPrinterLocationToPrinter = "dineInPrinterLocationToPrinter";
	public static final String printerLocationToPrinter_name = "name";
	public static final String printerLocationToPrinter_dishWorkstationId = "dishWorkstationId";
	public static final String printerLocationToPrinter_dishWorkstationName = "dishWorkstationName";
	public static final String printerLocationToPrinter_dishWorkstationChineseName = "dishWorkstationChineseName";

	public static final String configuredEventToActionAttributes = "configuredEventToActionAttributes";
	public static final String configuredEventToActionAttributes_action = "action";
	public static final String configuredEventToActionAttributes_fontSize = "fontSize";
	public static final String configuredEventToActionAttributes_location = "location";

	public static final String paidPrintOrderToKitchen = "paidPrintOrderToKitchen";
	public static final String defaultStation = "defaultStation";
	public static final String packingStation = "packingStation";

	public static final String couponControlOptions = "couponControlOptions";
	public static final String couponHeader = "couponHeader";
	public static final String coupon1 = "coupon1";
	public static final String coupon2 = "coupon2";
	public static final String coupon3 = "coupon3";
	public static final String coupon_print = "print";
	public static final String coupon_expiredInDays = "expiredInDays";
	public static final String coupon_content = "content";
	public static final String coupon_content_separator = ":";
	public static final String coupon___ctrl__ = "__ctrl__";
	public static final String coupon___expiration__ = "__expiration__";

	public static final String printBagXofYtemplate = "printBagXofYtemplate";
}
