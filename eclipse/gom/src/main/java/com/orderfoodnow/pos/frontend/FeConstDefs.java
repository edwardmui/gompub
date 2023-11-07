package com.orderfoodnow.pos.frontend;

public class FeConstDefs {
	// prevent direct instantiation of this class
	private FeConstDefs() {
	}

	// dish view column names
	public static final String DISHVIEW_CODE = "Code";
	public static final String DISHVIEW_NAME = "Name";
	public static final String DISHVIEW_LARGE = "Col3"; // Use generic display name to accommodate for other usages
														// besides dish view
	public static final String DISHVIEW_SMALL = "Col4"; // Ditto
	public static final String DISHVIEW_CHINESE = "名                      称";

	// @formatter:off
	public static final String[] DISHVIEW_COLUMN_NAMES = {
			DISHVIEW_CODE,
			DISHVIEW_NAME,
			DISHVIEW_LARGE,
			DISHVIEW_SMALL,
			DISHVIEW_CHINESE,
		};
	// @formatter:on

	// order view column names
	public static final String ORDERVIEW_INFO = "Info";
	public static final String ORDERVIEW_LARGE = "Lge";
	public static final String ORDERVIEW_SMALL = "Sml";
	public static final String ORDERVIEW_NAME = "Name";
	public static final String ORDERVIEW_TOTAL = "Total";
	public static final String ORDERVIEW_CHINESE = "名                     称";

	// @formatter:off
	public static final String[] ORDERVIEW_COLUMN_NAMES = {
			ORDERVIEW_INFO,
			ORDERVIEW_LARGE,
			ORDERVIEW_SMALL,
			ORDERVIEW_NAME,
			ORDERVIEW_TOTAL,
			ORDERVIEW_CHINESE,
		};
	// @formatter:on

	// Business view column names
	public static final String BUSINESSVIEW_ORDERNUMBER = "Order#";
	public static final String BUSINESSVIEW_NAME = "Name";
	public static final String BUSINESSVIEW_PHONE = "Phone";
	public static final String BUSINESSVIEW_STATUS = "Status";
	public static final String BUSINESSVIEW_TOTAL = "Total";
	public static final String BUSINESSVIEW_TYPE = "Type";
	public static final String BUSINESSVIEW_SERVER = "Server";
	public static final String BUSINESSVIEW_DRIVER = "Driver";
	public static final String BUSINESSVIEW_IN = "In";
	public static final String BUSINESSVIEW_OUT = "Out";
	public static final String BUSINESSVIEW_CASHIER = "Cashier";
	public static final String BUSINESSVIEW_COUPON = "Coupon";
	public static final String BUSINESSVIEW_PERCENTOFF = "%  Off";
	public static final String BUSINESSVIEW_VOIDEDITEM = "Voided Item";
	public static final String BUSINESVIEW_NOTE = "Note";
	public static final String BUSINESSVIEW_ADDRESS = "Address";

	// @formatter:off
	public static final String[] BUSVIEW_COLUMN_NAMES = {
			BUSINESSVIEW_ORDERNUMBER,
			BUSINESSVIEW_NAME,
			BUSINESSVIEW_PHONE,
			BUSINESSVIEW_STATUS,
			BUSINESSVIEW_TOTAL,
			BUSINESSVIEW_TYPE,
			BUSINESSVIEW_SERVER,
			BUSINESSVIEW_DRIVER,
			BUSINESSVIEW_IN,
			BUSINESSVIEW_OUT,
			BUSINESSVIEW_CASHIER,
			BUSINESSVIEW_COUPON,
			BUSINESSVIEW_PERCENTOFF,
			BUSINESSVIEW_VOIDEDITEM,
			BUSINESVIEW_NOTE,
			BUSINESSVIEW_ADDRESS,
		};
	// @formatter:on

	public static final String ARRIVALVIEW_ORDERNUMBER = "Order#";
	public static final String ARRIVALVIEW_NAME = "Name";
	public static final String ARRIVALVIEW_PHONE = "Phone";
	public static final String ARRIVALVIEW_STATUS = "Status";
	public static final String ARRIVALVIEW_ARRIVED = "Arrived";
	// @formatter:off
	public static final String[] ARIVALVIEW_COLUMN_NAMES = {
			ARRIVALVIEW_ORDERNUMBER,
			ARRIVALVIEW_NAME,
			ARRIVALVIEW_PHONE,
			ARRIVALVIEW_STATUS,
			ARRIVALVIEW_ARRIVED,
		};
	// @formatter:on

	// public static final String SERVER_INCHINESE = "服务员";
	// public static final String NAME_INCHINESE = "名称";
	// public static final String ADDR_INCHINESE = "地址";
	// public static final String CITY_INCHINESE = "城市";
	// public static final String STATE_INCHINESE = "州";
	// public static final String ZIP_INCHINESE = "邮政编码";
	// public static final String CARD_INCHINESE = "信用卡";
	public static final String DRIVER_INCHINESE = "司机";
	public static final String TABLE_INCHINESE = "台号";
	public static final String GUEST_INCHINESE = "人数";
	public static final String SEE_ENGLISH_IN_CHINESE = "请看英文";
}