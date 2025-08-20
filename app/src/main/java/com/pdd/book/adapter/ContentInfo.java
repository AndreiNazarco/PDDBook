package com.pdd.book.adapter;

public class ContentInfo {
		private String sContentName, sContentSubName, sContentCode;

	public ContentInfo() {
	}

	public ContentInfo(String psContentName, String psContentSubName, String psContentCode) {

		this.sContentName = psContentName;
		this.sContentSubName = psContentSubName;
		this.sContentCode = psContentCode;
	}

	// sContentName
	public String getContentName() {return sContentName;	}

	public void setContentName(String psContentName) {
		this.sContentName = psContentName;
	}

	// sContentSubName
	public String getContentSubName() {
		return sContentSubName;
	}

	public void setContentSubName(String psContentSubName) {
		this.sContentSubName = psContentSubName;
	}

	// sContentCode
	public String getContentCode() {return sContentCode;	}

	public void setContentCode(String psContentCode) {	this.sContentCode = psContentCode;	}

}
