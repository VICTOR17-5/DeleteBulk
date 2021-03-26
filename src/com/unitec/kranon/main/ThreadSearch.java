package com.unitec.kranon.main;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;

import com.unitec.kranon.util.Log;

public class ThreadSearch extends Thread {
	private String vsUUI;
	private String vsRutaCSV;
	
	private Buffer voBuffer;
	private StringBuffer voStringBuffer;
	
	private Integer viContadorLine = 0;

	public ThreadSearch(String vsUUI, Buffer voBuffer, String vsRutaCSV) {
		this.vsUUI = vsUUI;
		this.voBuffer = voBuffer;
		this.vsRutaCSV = vsRutaCSV;
	}
	
	public StringBuffer getData() {
		return voStringBuffer;
	}
	
	@SuppressWarnings("resource")
	@Override
	public void run(){
		String vsLinea = "";
		voBuffer.SetBuffer("LEYENDO ARCHIVO CSV...\n");
		BufferedReader voBufferedReader = null;
		Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][ThreadSearch][INFO] ---> LEYENDO CSV [" + vsRutaCSV + "]");
		try {
			voBuffer.SetBuffer("ESPERE...\n");
			voStringBuffer = new StringBuffer();
			voBufferedReader = new BufferedReader(new FileReader(vsRutaCSV));
			voBufferedReader.readLine();
			while ((vsLinea = voBufferedReader.readLine()) != null) {
				if(vsLinea.trim().length() <= 0) continue;
				String[] vaSplits = vsLinea.split(",");
				viContadorLine++;
				if(vaSplits[0].equals("") || vaSplits[1].equals("") || vaSplits[2].equals("") 
						|| vaSplits.length < 3 || vaSplits.length > 3) continue;
				
				voStringBuffer.append(vsLinea + "\n");
			}
			voBuffer.SetBuffer("LECTURA CORRECTAMENTE...\n");
			voBuffer.SetBuffer("SE OBTUVIERON [" + viContadorLine + "] LINEAS.");
			
		} catch (FileNotFoundException e) {
			Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][ThreadSearch][ERROR] ---> ERROR LEYENDO CSV [" + e.getMessage() + "]");
			return;
		} catch (IOException e) {
			Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][ThreadSearch][ERROR] ---> ERROR LEYENDO CSV [" + e.getMessage() + "]");
			return;
		}
		Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][ThreadSearch][INFO] ---> LECTURA CORRECTA.");
		voBuffer.SetFin(true);
	}
}
