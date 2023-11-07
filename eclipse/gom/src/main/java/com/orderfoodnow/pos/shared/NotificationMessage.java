package com.orderfoodnow.pos.shared;

import java.io.Serializable;

public class NotificationMessage<T> implements Serializable {
	private static final long serialVersionUID = 1L;
	private NotificationType type;
	private T payload;

	public NotificationMessage(NotificationType type, T payload) {
		this.setType(type);
		this.setPayload(payload);
	}

	public NotificationType getType() {
		return type;
	}

	public void setType(NotificationType type) {
		this.type = type;
	}

	public T getPayload() {
		return payload;
	}

	public void setPayload(T payload) {
		this.payload = payload;
	}

	@Override
	public String toString() {
		// @formatter:off
		return NotificationMessage.class.getSimpleName() +
				" [type=" + type + 
				", payload=" + payload +
				"]";
		// @formatter:on
	}
}
