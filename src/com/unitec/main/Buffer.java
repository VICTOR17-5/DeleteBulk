package com.unitec.main;

public class Buffer {
	private StringBuffer voBuffer;
	private Boolean vbTerminar;
	
	public Buffer() {
		voBuffer = new StringBuffer();
		vbTerminar = false;
	}
	
	public void SetBuffer(String voBuffer) {
		this.voBuffer.append(voBuffer);
	}
	
	public String GetBuffer() {
		return voBuffer.toString();
	}
	
	public Boolean GetFin() {
		return vbTerminar;
	}
	
	public void SetFin(Boolean vbTerminar) {
		this.vbTerminar = vbTerminar;
	}
	
}
