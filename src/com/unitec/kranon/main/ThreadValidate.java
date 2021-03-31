package com.unitec.kranon.main;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.kranon.conexionSQL.ConexionMySQL;
import com.kranon.conexionSQL.ConexionSQL;
import com.unitec.kranon.util.Log;

public class ThreadValidate extends Thread {
	private String vsUUI;
	private StringBuffer vsStringBuffer;
	private String[] vaConfi;
	private Map<String, Map<String,String>> voMapNoDuplicados;
	private Map<String, List<String>> voMapProduccion;
	private Map<String, List<String>> voMapEliminados;
	private Map<String, List<String>> voListContenido;
	private List<String> vlListBadFormat;
	private Buffer voBuffer = null;
	private Integer viContadorProduccion = 0;
	private Integer viContadorEliminar = 0;
	private ConexionMySQL voConexionMySQL = null;
	private ConexionSQL voConexionSQL = null;
	
	Integer viCont = 1;
	
	public ThreadValidate(Buffer voBuffer, String[] vaConfi, String vsUUI, StringBuffer vsStringBuffer, 
			Map<String, List<String>> voMapProduccion, Map<String, List<String>> voMapEliminados, ConexionMySQL voConexionMySQL) {
		this.voConexionMySQL = voConexionMySQL;
		this.voMapProduccion = voMapProduccion;
		this.voMapEliminados = voMapEliminados;
		this.voBuffer = voBuffer;
		this.vaConfi = vaConfi;
		this.vsUUI = vsUUI;
		this.vsStringBuffer = vsStringBuffer;
	}
	
	public ThreadValidate(Buffer voBuffer, String[] vaConfi, String vsUUI, StringBuffer vsStringBuffer, 
			Map<String, List<String>> voMapProduccion, Map<String, List<String>> voMapEliminados, ConexionSQL voConexionSQL) {
		this.voConexionSQL = voConexionSQL;
		this.voMapProduccion = voMapProduccion;
		this.voMapEliminados = voMapEliminados;
		this.voBuffer = voBuffer;
		this.vaConfi = vaConfi;
		this.vsUUI = vsUUI;
		this.vsStringBuffer = vsStringBuffer;
	}
	
	@Override
	public void run(){
		
		if(voConexionSQL == null && voConexionMySQL == null) {
			voBuffer.SetFin(true);
			Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][ThreadValidate][ERROR] ---> NO SE ENCONTRO UNA CONEXIÓN A UNA BASE DE DATOS.");
			return;
		}
		
		if(voConexionMySQL != null) {
			
		}
		Log.GuardaLog("productivos","************************************** REGISTROS PRODUCTIVOS **************************************");
		Log.GuardaLog("aEliminar", "************************************** REGISTROS A ELIMINAR **************************************");
		Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][ThreadValidate][INFO] ---> VALIDANDO LIST DATA");
		
		int viContadorLine = 0;
			
		voMapNoDuplicados = new HashMap<String, Map<String,String>>();
		voListContenido = new HashMap<String, List<String>>();
		vlListBadFormat = new ArrayList<String>();
		
		String[] vaLinea = vsStringBuffer.toString().split("\n");
		Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][ThreadValidate][INFO] ---> GENERANDO MAPA DE DATOS");
		
		for(String vsLinea : vaLinea) {
			if(vsLinea.trim().length() <= 0) continue;
			viContadorLine++;
			String[] vaSplits = vsLinea.split(",");
			if(vaSplits[0].equals("") || vaSplits[1].equals("") || vaSplits[2].equals("") || vaSplits.length < 3 || vaSplits.length > 3) {
				vlListBadFormat.add(vsLinea);
				continue;
			}
			if (voListContenido.get(vaSplits[0]) == null) {			
				List<String> vlContacts = new ArrayList<String>();
				vlContacts.add(vsLinea);
				voListContenido.put(vaSplits[0], vlContacts);
			} else {
				List<String> vlContacts = voListContenido.get(vaSplits[0]);
				vlContacts.add(vsLinea);
				voListContenido.replace(vaSplits[0], vlContacts);
			}
		}
		Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][ThreadValidate][INFO] ---> NUMERO DE LINEAS LEIDAS[" + viContadorLine + "], "
				+ "NUMERO DE KEY[" + voListContenido.size() + "], NUMERO DE LINEAS CON ERRORES[" + vlListBadFormat.size() + "]");
		
		
		for (Entry<String, List<String>> voEntryPersona : voListContenido.entrySet()) {
			String vsNumPersona = voEntryPersona.getKey();
			List<String> voDatosPersona = voEntryPersona.getValue();	
			for (String voEntryContact : voDatosPersona) {
				String[] vaSplitData = voEntryContact.split(",");
				String vsContactId = vaSplitData[2];
				String vsContactListId = vaSplitData[1];
				
				ResultSet voResultado = null;
				
				if(voConexionSQL != null) {
					String vsQuery = "SELECT * FROM " + vaConfi[3] + " "
							+ "WHERE [NPersona] = '" + vsNumPersona + "' "
								+ "and [ContactId] = '" + vsContactId + "' "
								+ "and [ContactListId] = '" + vsContactListId + "'";
					voResultado = voConexionSQL.ExecuteSelect(vsQuery);
				}
				else if(voConexionMySQL != null) {
					String vsQuery = "SELECT * FROM " + vaConfi[4] + " "
							+ "WHERE NPersona = '" + vsNumPersona + "' "
								+ "and contactId = '" + vsContactId + "' "
								+ "and contactListId = '" + vsContactListId + "';";
					voResultado = voConexionMySQL.ExecuteSelect(vsQuery);
				}
				else Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][ThreadValidate][ERROR] ---> NO EXISTE UNA CONEXION A UNA BASE DE DATOS.");

				Boolean vbExisteEnSQL = false;
				if (voResultado == null) {
					voBuffer.SetFin(true);
					return;
				} else {
					
					try {
						while (voResultado.next()) vbExisteEnSQL = true;
					} catch (SQLException e) {
						Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][ThreadValidate][ERROR] ---> ERROR SQL/MYSQL: " + e.getMessage());
					}
				}
				
				if (vbExisteEnSQL) {
					viContadorProduccion++;
					if (voMapProduccion.get(vsNumPersona) == null) {
						List<String> vlContacts = new ArrayList<String>();
						vlContacts.add(voEntryContact);
						voMapProduccion.put(vsNumPersona, vlContacts);
					} else {
						List<String> vlContacts = voMapProduccion.get(vsNumPersona);
						vlContacts.add(voEntryContact);
						voMapProduccion.replace(vsNumPersona, vlContacts);
					}
					Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][ThreadValidate][INFO] ---> [PRODUCTIVO] REGISTRO[" + vsNumPersona + "," + vsContactListId + "," + vsContactId + "]");
					Log.GuardaLog("productivos", "[" + new Date() + "][" + vsUUI + "][ThreadValidate][INFO] ---> REGISTRO [" + vsNumPersona + ", " + vsContactListId + ", " + vsContactId + "]");
					voBuffer.SetBuffer("[" + (viCont++) + "][PRODUCTIVO] " + voEntryContact + "\n");
					if (voMapNoDuplicados.get(vsNumPersona) == null) {
						Map<String,String> vlContacts = new HashMap<String,String>();
						vlContacts.put(vsContactId, vsContactListId);
						voMapNoDuplicados.put(vsNumPersona, vlContacts);
					} else {
						Map<String,String> vlContacts = voMapNoDuplicados.get(vsNumPersona);
						vlContacts.put(vsContactId, vsContactListId);
						voMapNoDuplicados.replace(vsNumPersona, vlContacts);
					}
				} else {
					viContadorEliminar++;
					if (voMapEliminados.get(vsNumPersona) == null) {
						List<String> vlContacts = new ArrayList<String>();
						vlContacts.add(voEntryContact);
						voMapEliminados.put(vsNumPersona, vlContacts);
					} else {
						List<String> vlContacts = voMapEliminados.get(vsNumPersona);
						vlContacts.add(voEntryContact);
						voMapEliminados.replace(vsNumPersona, vlContacts);
					}
					Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][ThreadValidate][INFO] ---> [PARA ELIMINAR] REGISTRO[" + vsNumPersona + "," + vsContactListId + "," + vsContactId + "]");
					Log.GuardaLog("aEliminar", "[" + new Date() + "][" + vsUUI + "][ThreadValidate][INFO] ---> REGISTRO [" + vsNumPersona + ", " + vsContactListId + ", " + vsContactId + "]");
					voBuffer.SetBuffer("[" + (viCont++) + "][NO PRODUCTIVO] " + voEntryContact + "\n");
				}
			}
		}
		
		Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][ThreadValidate][INFO] ---> NUMERO DE REGISTROS TOTALES[" + (viContadorProduccion + viContadorEliminar) + "]");
		if(voConexionSQL != null) voConexionSQL.closeConnection();
		else if(voConexionMySQL != null) voConexionMySQL.closeConnection();

		Integer viContadorSinDuplicar = 0;
		for (Entry<String, Map<String,String>> voEntryPersona : voMapNoDuplicados.entrySet()) 
			for(@SuppressWarnings("unused") Entry<String, String> voEntry : voEntryPersona.getValue().entrySet()) 
				viContadorSinDuplicar++;
		
		voBuffer.SetBuffer("\nREGISTROS ENCONTRADOS EN PRODUCCION [" + viContadorProduccion + "]");
		voBuffer.SetBuffer("\nREGISTROS UNICOS ENCONTRADOS EN PRODUCCION[" + viContadorSinDuplicar + "]");
		voBuffer.SetBuffer("\nREGISTROS NO ENCONTRADOS, PARA ELIMINAR [" + viContadorEliminar + "]");
		voBuffer.SetBuffer("\nREGISTROS TOTALES [" + (viContadorProduccion + viContadorEliminar) + "]");
		
		Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][ThreadValidate][INFO] ---> PROCESO EXITOSO");
		voBuffer.SetFin(true);
	}
}
