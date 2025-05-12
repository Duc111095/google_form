package com.ducnh.form_dev.entity;

import lombok.Data;

@Data
public class Staff {
	private String sttRec;
	private String maNv;
	private String ten;
	private String tenBP;
	private String tenBac;
	
	@Override
	public String toString() {
		return this.sttRec + ": " + this.maNv;
	}
}
