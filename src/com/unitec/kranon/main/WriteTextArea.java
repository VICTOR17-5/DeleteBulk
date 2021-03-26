package com.unitec.kranon.main;

import javax.swing.JTextArea;

public class WriteTextArea extends Thread {
	private JTextArea voTextAreaInfo;
	private Buffer voBuffer;
	private Boolean vbTerminar;
	
	public WriteTextArea(Buffer voBuffer, JTextArea voTextAreaInfo) {
		this.voTextAreaInfo = voTextAreaInfo;
		this.voBuffer = voBuffer;
		this.voTextAreaInfo.setText("");
	}
	
	@Override
	public void run() {
		vbTerminar = false;
		while(!vbTerminar) {
			vbTerminar = voBuffer.GetFin();
			String voStringBuffer = voBuffer.GetBuffer();
			voTextAreaInfo.setText(voStringBuffer);
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
}
