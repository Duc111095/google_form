package com.ducnh.form_dev.entity;

public class LopHoc {
	private String sttRec;
	private String idForm;
	
	public LopHoc() {}
	public LopHoc(String sttRec, String idForm) {
		this.sttRec = sttRec;
		this.idForm = idForm;
	}
	
	public String getSttRec() {
		return this.sttRec;
	}
	
	public void setSttRec(String sttRec) {
		this.sttRec = sttRec;
	}
	
	public String getIdForm() {
		return this.idForm;
	}
	
	public void setIdForm(String idForm) {
		this.idForm = idForm;
	}
}
