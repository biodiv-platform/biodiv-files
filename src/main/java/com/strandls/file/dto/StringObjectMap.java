package com.strandls.file.dto;

import java.util.Map;

public class StringObjectMap<T> {
	Map<String, T> data;

	public StringObjectMap() {
	}

	public StringObjectMap(Map<String, T> filePaths) {
		this.data = filePaths;
	}

	public Map<String, T> getData() {
		return data;
	}

	public void setData(Map<String, T> data) {
		this.data = data;
	}
}
