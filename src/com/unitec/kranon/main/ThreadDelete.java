package com.unitec.kranon.main;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import com.kranon.purecloud.PureCloud;
import com.unitec.kranon.util.Log;

public class ThreadDelete extends Thread {
	private String vsUUI;
	private String vsFecha;
	private PureCloud voPureCloud;
	private Buffer voBuffer;
	private Map<String, List<String>> voMapLists;
	private Map<String, List<String>> voMapProduccion;
	private Map<String, List<String>> voMapEliminados;
	private Map<String, List<String>> voMapEliminadosOK;
	private Map<String, List<String>> voMapEliminadosKO;
	
	private Integer viDeletedOK = 0, viDeletedKO = 0;
	private Integer TIME_SLEEP = 100;
	private Integer viNumRegistro = 0;
	
	public ThreadDelete(String[] vaConfi, String vsUUI, Buffer voBuffer,
			Map<String, List<String>> voMapProduccion, Map<String, List<String>> voMapEliminados, 
			Map<String, List<String>> voMapEliminadosOK, Map<String, List<String>> voMapEliminadosKO) {
		this.voMapProduccion = voMapProduccion;
		this.voMapEliminados = voMapEliminados;
		this.voMapEliminadosKO = voMapEliminadosKO;
		this.voMapEliminadosOK = voMapEliminadosOK;
		this.voBuffer = voBuffer;
		this.vsUUI = vsUUI;
		TIME_SLEEP = Integer.getInteger(vaConfi[5]);
		Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][ThreadDelete][INFO] ---> INICIANDO PROCESO...");
		vsFecha = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
		voPureCloud = new PureCloud(vsUUI);
		voPureCloud.getToken(vaConfi[0], vaConfi[1]);
		voMapLists = new HashMap<String, List<String>>();
	}
	
	@Override
	public void run() {
		Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][ThreadDelete][INFO] ---> INICIANDO LIMPIEZA DENTRO DE PURECLOUD");
		Log.GuardaLog("deleteSuccess","****************************** ELIMINADOS EXITOSAMENTE **************************************");
		Log.GuardaLog("deleteFailure","******************************* FALLA AL ELIMINARLOS ****************************************");

		voBuffer.SetBuffer("ELIMINANDO REGISTROS...\n");

		int viRegistros = 0;
		for (Entry<String, List<String>> voEntryPersona : voMapEliminados.entrySet()) {
			for (String voEntryInfo : voEntryPersona.getValue()) {
				String[] vaSplit = voEntryInfo.split(",");
				String vsContactListId = vaSplit[1];
				if(voMapLists.get(vsContactListId) == null) {
					List<String> vlContcts = new ArrayList<String>();
					vlContcts.add(voEntryInfo);
					voMapLists.put(vsContactListId, vlContcts);
				} else {
					List<String> vlContcts = voMapLists.get(vsContactListId);
					vlContcts.add(voEntryInfo);
					voMapLists.replace(vsContactListId, vlContcts);
				}
				viRegistros++;
			}
		}
		Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][ThreadDelete][INFO] ---> TOTAL DE REGISTROS A ELIMINAR[" + viRegistros + "]");
		
		List<String> vlContactId = null;
		for (Entry<String, List<String>> voEntryList : voMapLists.entrySet()) {
			vlContactId = new ArrayList<String>();
			String vsContactList = voEntryList.getKey();
			
			for (String vsInfo : voEntryList.getValue()) {
				String[] vaSplitInfo = vsInfo.split(",");
				vlContactId.add(vaSplitInfo[2]);
			}

			Map<String, String> voMapResult = voPureCloud.DeleteContactsBulk(vsContactList, vlContactId);
			//SE ELIMINARON LOS REGISTROS ENVIADOS EN LA LISTA
			if (voMapResult.get("status").equals("200")) {
				
				//SE HIZO EL DELETE COMPLETO DE LA LISTA ENVIADA
				if (voMapResult.get("errors").equals("")) {
					
					Map<String, String> voMapResultSearch = voPureCloud.SearchContactsBulk(vsContactList, vlContactId);
					if(voMapResultSearch.get("status").equals("200")) {
						String vsIdsEncontrados = voMapResultSearch.get("contactFound");
						
						if(vsIdsEncontrados.equals("")) {
							
							for (String vsInfo : voEntryList.getValue()) {
								String[] vaSplit = vsInfo.split(",");
								voBuffer.SetBuffer("[" + (++viNumRegistro) + "][SUCCESS] REGISTRO[" + vsInfo + "], STATUS[Deleted successful]\n");
								Log.GuardaLog("deleteSuccess", "[" + new Date() + "][" + vsUUI + "][ThreadDelete][INFO] ---> CODE[200], STATUS[Contact delete successful], REGISTRO[" + vsInfo + "]");
								if (voMapEliminadosOK.get(vaSplit[0]) == null) {
									List<String> vlContcts = new ArrayList<String>();
									vlContcts.add(vsInfo);
									voMapEliminadosOK.put(vaSplit[0], vlContcts);
								} else {
									List<String> vlContcts = voMapEliminadosOK.get(vaSplit[0]);
									vlContcts.add(vsInfo);
									voMapEliminadosOK.replace(vaSplit[0], vlContcts);
								}
								viDeletedOK++;
							}
							
						} else {

							for (String vsInfo : voEntryList.getValue()) {
								String[] vaSplitInfo = vsInfo.split(",");
								if(vsIdsEncontrados.contains(vaSplitInfo[2])) {
									voBuffer.SetBuffer("[" + (++viNumRegistro) + "][FAILURE] REGISTRO[" + vsInfo + "], STATUS[It wasn't removed successfully the contact]\n");
									Log.GuardaLog("deleteFailure", "[" + new Date() + "][" + vsUUI + "][ThreadDelete][INFO] ---> CODE[500], STATUS[It wasn't removed successfully the contact], REGISTRO[" + vsInfo + "]");
									if (voMapEliminadosKO.get(vaSplitInfo[0]) == null) {
										List<String> vlContcts = new ArrayList<String>();
										vlContcts.add(vsInfo);
										voMapEliminadosKO.put(vaSplitInfo[0], vlContcts);
									} else {
										List<String> vlContcts = voMapEliminadosKO.get(vaSplitInfo[0]);
										vlContcts.add(vsInfo);
										voMapEliminadosKO.replace(vaSplitInfo[0], vlContcts);
									}
									viDeletedKO++;
								} else {
									voBuffer.SetBuffer("[" + (++viNumRegistro) + "][SUCCESS] REGISTRO[" + vsInfo + "], STATUS[The contactId delete successful]\n");
									Log.GuardaLog("deleteSuccess", "[" + new Date() + "][" + vsUUI + "][ThreadDelete][INFO] ---> CODE[200], STATUS[Contact delete successful], REGISTRO[" + vsInfo + "]");
									if (voMapEliminadosOK.get(vaSplitInfo[0]) == null) {
										List<String> vlContcts = new ArrayList<String>();
										vlContcts.add(vsInfo);
										voMapEliminadosOK.put(vaSplitInfo[0], vlContcts);
									} else {
										List<String> vlContcts = voMapEliminadosOK.get(vaSplitInfo[0]);
										vlContcts.add(vsInfo);
										voMapEliminadosOK.replace(vaSplitInfo[0], vlContcts);
									}
									viDeletedOK++;
								}
							}
						}
					}	
					
				} else {

					//SE REALIZO EL DELETE PERO ALGUNOS REGISTROS NO SE ELIMINARON
					String vsData = voMapResult.get("errors");
					for (String vsInfo : voEntryList.getValue()) {
						String[] vaSplitInfo = vsInfo.split(",");
						
						//SE ENCUENTRA EL REGISTRO EN LA LISTA DE LOS "NO" ELIMINADOS
						if (vsData.contains(vaSplitInfo[2])) {
							voBuffer.SetBuffer("[" + (++viNumRegistro) + "][FAILURE] REGISTRO[" + vsInfo + "], STATUS[It wasn't removed successfully the contact]\n");
							Log.GuardaLog("deleteFailure", "[" + new Date() + "][" + vsUUI + "][ThreadDelete][INFO] ---> CODE[500], STATUS[It wasn't removed successfully the contact], REGISTRO[" + vsInfo + "]");
							if (voMapEliminadosKO.get(vaSplitInfo[0]) == null) {
								List<String> vlContcts = new ArrayList<String>();
								vlContcts.add(vsInfo);
								voMapEliminadosKO.put(vaSplitInfo[0], vlContcts);
							} else {
								List<String> vlContcts = voMapEliminadosKO.get(vaSplitInfo[0]);
								vlContcts.add(vsInfo);
								voMapEliminadosKO.replace(vaSplitInfo[0], vlContcts);
							}
							viDeletedKO++;
							
							//SE ENCUENTRA EL REGISTRO EN LA LISTA DE LOS "SI" ELIMINADOS
						} else {
							
							Map<String, String> voMapResultSearch = voPureCloud.SearchContactId(vsContactList, vaSplitInfo[2]);
							
							if(voMapResultSearch.get("status").equals("404")) {
								voBuffer.SetBuffer("[" + (++viNumRegistro) + "][SUCCESS] REGISTRO[" + vsInfo + "], STATUS[Delete successful]\n");
								Log.GuardaLog("deleteSuccess", "[" + new Date() + "][" + vsUUI + "][ThreadDelete][INFO] ---> CODE[200], STATUS[Contact delete successful], REGISTRO[" + vsInfo + "]");
								if (voMapEliminadosOK.get(vaSplitInfo[0]) == null) {
									List<String> vlContcts = new ArrayList<String>();
									vlContcts.add(vsInfo);
									voMapEliminadosOK.put(vaSplitInfo[0], vlContcts);
								} else {
									List<String> vlContcts = voMapEliminadosOK.get(vaSplitInfo[0]);
									vlContcts.add(vsInfo);
									voMapEliminadosOK.replace(vaSplitInfo[0], vlContcts);
								}
								viDeletedOK++;
							} else {
								voBuffer.SetBuffer("[" + (++viNumRegistro) + "][FAILURE] REGISTRO[" + vsInfo + "], STATUS[It wasn't removed successfully the contact]\n");
								Log.GuardaLog("deleteFailure", "[" + new Date() + "][" + vsUUI + "][ThreadDelete][INFO] ---> CODE[500], STATUS[It wasn't removed successfully the contact], REGISTRO[" + vsInfo + "]");
								if (voMapEliminadosKO.get(vaSplitInfo[0]) == null) {
									List<String> vlContcts = new ArrayList<String>();
									vlContcts.add(vsInfo);
									voMapEliminadosKO.put(vaSplitInfo[0], vlContcts);
								} else {
									List<String> vlContcts = voMapEliminadosKO.get(vaSplitInfo[0]);
									vlContcts.add(vsInfo);
									voMapEliminadosKO.replace(vaSplitInfo[0], vlContcts);
								}
								viDeletedKO++;
							}
						}
					}
				}
				
			//NO SE ELIMINO NINGUN REGISTRO ENVIADO EN LA LISTA
			} else {
				for (String vsInfo : voEntryList.getValue()) {
					String[] vaSplitInfo = vsInfo.split(",");
					voBuffer.SetBuffer("[" + (++viNumRegistro) + "][FAILURE][404] REGISTRO[" + vsInfo + "], STATUS[ContactList not found]\n");
					Log.GuardaLog("deleteFailure", "[" + new Date() + "][" + vsUUI + "][ThreadDelete][INFO] ---> CODE[404], STATUS[ContactList not found], REGISTRO[" + vsInfo + "]");
					if (voMapEliminadosKO.get(vaSplitInfo[0]) == null) {
						List<String> vlContcts = new ArrayList<String>();
						vlContcts.add(vsInfo);
						voMapEliminadosKO.put(vaSplitInfo[0], vlContcts);
					} else {
						List<String> vlContcts = voMapEliminadosKO.get(vaSplitInfo[0]);
						vlContcts.add(vsInfo);
						voMapEliminadosKO.replace(vaSplitInfo[0], vlContcts);
					}
					viDeletedKO++;
				}
			}
			
			if(TIME_SLEEP == null || TIME_SLEEP <= 0) TIME_SLEEP = 1000;
			try {
				Thread.sleep(TIME_SLEEP);
			} catch (InterruptedException e) {
				Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][ThreadDelete][ERROR] ---> " + e.getMessage());
			}
		}
		

		voBuffer.SetBuffer("\nTOTAL ---> OK[" + viDeletedOK + "], KO[" + viDeletedKO + "]\n");

		Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][ThreadDelete][INFO] ---> GENERANDO CSV's FINALES");
		Map<String, List<String>> voMapHeaders = new HashMap<String, List<String>>();
		List<String> vlHeaders = new ArrayList<String>();
		vlHeaders.add("NPERSONA,CONTACT_LIST_ID,CONTACT_ID");
		voMapHeaders.put("NPERSONA", vlHeaders);
		
		File voFile = new File("C:\\Appl\\UNITEC\\DeleteRegistro\\CSVs\\" + vsFecha + "_EstatusFinal.csv");
		voFile.delete();
		
		EscribirCSV("C:\\Appl\\UNITEC\\DeleteRegistro\\CSVs\\" + vsFecha + "_EstatusFinal.csv", voMapHeaders, "ESTATUS_FINAL");
		EscribirCSV("C:\\Appl\\UNITEC\\DeleteRegistro\\CSVs\\" + vsFecha + "_EstatusFinal.csv", voMapProduccion, "PRODUCCCION");
		EscribirCSV("C:\\Appl\\UNITEC\\DeleteRegistro\\CSVs\\" + vsFecha + "_EstatusFinal.csv", voMapEliminadosOK, "ELIMINADOS_OK");
		EscribirCSV("C:\\Appl\\UNITEC\\DeleteRegistro\\CSVs\\" + vsFecha + "_EstatusFinal.csv", voMapEliminadosKO, "ELIMINADOS_KO");

		Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][EliminarPureCloud][INFO] ---> SEGUNDO PROCESO EXITOSO");
		Log.GuardaLog("****************************** INICIANDO ******************************");
		voBuffer.SetFin(true);
	}
	
	private synchronized Boolean EscribirCSV(String vsRutaArchivo, Map<String, List<String>> voMap,String vsStatus) {
		Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][ThreadDelete][INFO] ---> INICIANDO ESCRITURA DE CSV, REGISTROS CON ESTATUS[" + vsStatus + "]");
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(new FileWriter(vsRutaArchivo, true));
			for (Entry<String, List<String>> voEntry : voMap.entrySet()) {
				List<String> voData = voEntry.getValue();
				for (String voEntryData : voData) {
					pw.println(voEntryData + "," + vsStatus);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} finally {
			if (pw != null) {
				pw.close();
			}
		}
		return true;
	}

}
