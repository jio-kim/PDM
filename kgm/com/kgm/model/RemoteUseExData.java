package com.kgm.model;

import java.io.Serializable;

@SuppressWarnings({"serial"})
public class RemoteUseExData implements Serializable {
	private String item_id;

	public String getItem_id() {
		return item_id;
	}

	public void setItem_id(String itemId) {
		item_id = itemId;
	}
}
