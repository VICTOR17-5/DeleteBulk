package com.kranon.purecloud;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.kranon.conexionHttp.*;
import com.unitec.kranon.util.Log;

public class PureCloud {

	private final Integer MAX_DELETE_BULK = 100;
	private final Integer MAX_SEARCH_BULK = 50;
	private final Integer TIME_SLEEP_TO_REQUEST = 100;
	
	private String vsUUI = "";
	private String vsAccessToken = "ERROR";
	private ConexionHttp voConexionHttp = null;
	private ConexionResponse voConexionResponse = null;
	private Integer viTO = 15000;
	
	
	public PureCloud(String vsUUI) {
		this.vsUUI = vsUUI;
	}
	
	public String getToken(String vsClID, String vsClSec) {
        String encodeData;
        String URLServicio = "https://login.mypurecloud.com/oauth/token?grant_type=client_credentials";
        String inputJson = "";
        int timeOut = 15000;
        HashMap<String, String> header = new HashMap<>();
        voConexionHttp = new ConexionHttp();
		try {
			encodeData = new String(Base64.encodeBase64((vsClID + ":" + vsClSec).getBytes("ISO-8859-1")));
	        header.put("Authorization", " Basic " + encodeData);
		} catch (UnsupportedEncodingException e1) {
			Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][getToken][ERROR] ---> " + e1.getMessage());
		}
        try {
            voConexionResponse = voConexionHttp.executePost(URLServicio, timeOut, inputJson, header);
            if (voConexionResponse.getCodigoRespuesta() == 200) {
                JSONObject json = new JSONObject(voConexionResponse.getMensajeRespuesta());
                if (json.has("access_token")) {
                    vsAccessToken = json.getString("access_token");
                    Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][getToken][INFO] ---> TOKEN[SUCCESS].");
                 } else {
                	 Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][getToken][ERROR] ---> TOKEN[ERROR].");
                 }
            } else {
            	Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][getToken][ERROR] ---> TOKEN[" + vsAccessToken + "], "
            			+ "CODIGO RESPUESTA[" + voConexionResponse.getCodigoRespuesta() + "], MENSAJE RESPUESTA[" + voConexionResponse.getMensajeRespuesta() + "]");
            }
        } catch (Exception e) {
        	Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][getToken][ERROR] --->" + e.getMessage());
        }
        return vsAccessToken;
    }
	
	public Map<String, String> DeleteContact(String vsContactListId, String vsContactId) {
		String vsURLAPI = "https://api.mypurecloud.com/api/v2/outbound/contactlists/{contactListId}/contacts/{contactId}";
		HashMap<String, String> voHeader = new  HashMap<String, String>();
		voHeader.put("Authorization", "bearer " + vsAccessToken);
		vsURLAPI = vsURLAPI.replace("{contactListId}", vsContactListId).replace("{contactId}", vsContactId);
		Map<String, String> voMapStatus = new  HashMap<String, String>();
		try {
			Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][SearchContactId][INFO] ---> ENDPOINT[" + vsURLAPI + "]");
			voConexionResponse = voConexionHttp.executeDelete(vsURLAPI, viTO, voHeader);
			if(voConexionResponse.getCodigoRespuesta() == 200) {
				voMapStatus.put("status", "200");
				voMapStatus.put("message", "contact deleted successfully");
				Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][DeleteContact][INFO] ---> CONTACT[" + vsContactId + "] "
						+ "IN LIST[" + vsContactListId + "] DELETE[SUCCESS]");	
			} else {
				JSONObject voJSONError = new JSONObject(voConexionResponse.getMensajeError());
				String vsCode = voJSONError.getString("code");
				voMapStatus.put("status", "" + voConexionResponse.getCodigoRespuesta());
				voMapStatus.put("message", vsCode.replace(".", " "));
				Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][DeleteContact][WARNING] ---> CONTACT [" + vsContactId + "] IN LIST[ " + vsContactListId + "] "
						+ "DELETE[FAILURE], STATUS[" + voConexionResponse.getCodigoRespuesta() + "], MESSAGE[" + vsCode.replace(".", " ") + "]");
			}
		} catch (Exception e) {
			Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][DeleteContact][ERROR] --->" + e.getMessage());	
			Map<String, String> voMapStatusError = new  HashMap<String, String>();
			voMapStatusError.put("status", "500");
			voMapStatusError.put("message", "error server");
			return voMapStatusError;
		} finally {
			try {
				Thread.sleep(TIME_SLEEP_TO_REQUEST);
			} catch (InterruptedException e) {
				Map<String, String> voMapStatusError = new  HashMap<String, String>();
				voMapStatusError.put("status", "500");
				voMapStatusError.put("message", "error server");
				return voMapStatusError;
			}
		}
		return voMapStatus;
	}
	
	public Map<String, String> DeleteContactsBulk(String vsContactListId, List<String> vlContactIds) {
		StringBuffer vsContactsErrores = new StringBuffer("");
		Map<String, String> voMapStatus = new  HashMap<String, String>();
		String vsURLAPI = "https://api.mypurecloud.com/api/v2/outbound/contactlists/{contactListId}/contacts?contactIds=";
		HashMap<String, String> voHeader = new  HashMap<String, String>();
		voHeader.put("Authorization", "bearer " + vsAccessToken);
		vsURLAPI = vsURLAPI.replace("{contactListId}", vsContactListId);
		int viContador = 0;
		int viTotal = 0;
		String vsTotalContacts = "";
		for(String vsContactPersona : vlContactIds) {
			if(viContador == (MAX_DELETE_BULK-1) || viTotal >= (vlContactIds.size()-1)) {
				try {
					vsTotalContacts += vsContactPersona;
					Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][DeleteContactsBulk][INFO] ---> ENDPOINT[" + vsURLAPI + vsTotalContacts + "]");
					voConexionResponse = voConexionHttp.executeDelete((vsURLAPI + vsTotalContacts), viTO, voHeader);
					if(voConexionResponse.getCodigoRespuesta() == 200) {
						Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][DeleteContactsBulk][INFO] ---> CONTACTS DELETE[SUCCESS], STATUS[" 
								+ voConexionResponse.getCodigoRespuesta() + "], MESSAGE[contacts deleted successfully]");
					} else {
						
						JSONObject voJSONError = new JSONObject(voConexionResponse.getMensajeError());
						String vsCode = voJSONError.getString("code");
						Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][DeleteContactsBulk][WARNING] ---> CONTACTS DELETE[FAILURE], "
								+ "STATUS[" + voConexionResponse.getCodigoRespuesta() + "], MESSAGE[" + vsCode.replace(".", " ") + "], "
										+ "CONTACTS NOT DELETED[" + vsTotalContacts.replace("%2C", ",") + "]");
						if(vsCode.equals("contact.list.not.found")) {
							Map<String, String> voMapStatusError = new  HashMap<String, String>();
							voMapStatusError.put("status", "404");
							voMapStatusError.put("message", "contact list not found");
							return voMapStatusError;
						}
						vsContactsErrores.append(vsTotalContacts.replace("%2C", ",") + ",");
						
						
					}
				} catch (JSONException e) {
					Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][DeleteContactsBulk][ERROR] --->" + e.getMessage());	
					Map<String, String> voMapStatusError = new  HashMap<String, String>();
					voMapStatusError.put("status", "500");
					voMapStatusError.put("message", "error");
					return voMapStatusError;
				} finally {
					try {
						Thread.sleep(TIME_SLEEP_TO_REQUEST);
					} catch (InterruptedException e) {
						Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][DeleteContactsBulk][ERROR] --->" + e.getMessage());	
						Map<String, String> voMapStatusError = new  HashMap<String, String>();
						voMapStatusError.put("status", "500");
						voMapStatusError.put("message", "error");
						return voMapStatusError;
					}
					viContador = 0;
					vsTotalContacts = "";
					viTotal++;
				}
					
			} else {
				viTotal++;
				viContador++;
				vsTotalContacts += vsContactPersona + "%2C";
			}
		}
		voMapStatus.put("status", "200");
		voMapStatus.put("errors", (!vsContactsErrores.toString().equals(""))?vsContactsErrores.toString().substring(0,(vsContactsErrores.length()-2)):"");
		voMapStatus.put("Message", "contacts deleted successfully");
		return voMapStatus;
	}
	
	public Map<String, String> SearchContactId(String vsContactListId, String vsContactId){
		Map<String, String> voMapStatus = new  HashMap<String, String>();
		String vsURLGetContacts = "https://api.mypurecloud.com/api/v2/outbound/contactlists/{contactListId}/contacts/{contactId}";
		vsURLGetContacts = vsURLGetContacts.replace("{contactListId}", vsContactListId).replace("{contactId}", vsContactId);
		HashMap<String, String> voHeader = new  HashMap<String, String>();
		voHeader.put("Authorization", "bearer " + vsAccessToken);
		HashMap<String, String> voParamsURL = null;
		try {	
			Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][SearchContactId][INFO] ---> ENDPOINT[" + vsURLGetContacts + "]");
			voConexionResponse = voConexionHttp.executeGet(vsURLGetContacts, viTO, voHeader, voParamsURL);
			if(voConexionResponse.getCodigoRespuesta() == 200) {
				JSONObject voJSONSuccess = new JSONObject(voConexionResponse.getMensajeRespuesta());
				if(voJSONSuccess.has("data")) {
					String vsData = voJSONSuccess.getJSONObject("data").toString();
					voMapStatus.put("status", "200");
					voMapStatus.put("message", "contact found");
					voMapStatus.put("data", vsData);
					Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][SearchContactId][INFO] ---> CONTACT FOUND, STATUS[200], "
							+ "RESPONSE[" + voConexionResponse.getMensajeRespuesta().replace("\r\n", "") + "]");
				}
			} else {
				JSONObject voJSONError = new JSONObject(voConexionResponse.getMensajeError());
				String vsCode = voJSONError.getString("code");
				voMapStatus.put("status", "" + voConexionResponse.getCodigoRespuesta());
				voMapStatus.put("data", "");
				voMapStatus.put("message", vsCode.replace(".", " "));
				Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][SearchContactId][WARNING] ---> CONTACT NOT FOUND, "
						+ "STATUS[" + voConexionResponse.getCodigoRespuesta() + "], MESSAGE[" + vsCode.replace(".", " ") + "]");
			}
		} catch (Exception e) {
			Map<String, String> voMapStatusError = new  HashMap<String, String>();
			Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][SearchContactId][ERROR] --->" + e.getMessage());
			voMapStatusError.put("status", "500");
			voMapStatusError.put("data", "");
			voMapStatusError.put("message", "error");
			return voMapStatusError;
		} finally {
			try {
				Thread.sleep(TIME_SLEEP_TO_REQUEST);
			} catch (InterruptedException e) {
				Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][SearchContactId][ERROR] --->" + e.getMessage());
				Map<String, String> voMapStatusError = new  HashMap<String, String>();
				voMapStatusError.put("status", "500");
				voMapStatusError.put("data", "");
				voMapStatusError.put("message", "error");
				return voMapStatusError;
			}
		}
		return voMapStatus;
	}
	
	public Map<String, String> SearchContactsBulk(String vsContactListId, List<String> vlContactsId){
		String vsTotalContacts = "[\"";
		Map<String, String> voMapStatus = new  HashMap<String, String>();
		String vsURLGetContacts = "https://api.mypurecloud.com/api/v2/outbound/contactlists/{contactListId}/contacts/bulk";
		vsURLGetContacts = vsURLGetContacts.replace("{contactListId}", vsContactListId);
		HashMap<String, String> voHeader = new  HashMap<String, String>();
		voHeader.put("Authorization", "bearer " + vsAccessToken);
		int viTotal = 0;
		int viContador = 0;
		List<String> vlEncontrados = new ArrayList<String>();

		for(String vsContactPersona : vlContactsId) {
			if(viContador == (MAX_SEARCH_BULK-1) || viTotal >= (vlContactsId.size()-1)) {
				Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][SearchContactsBulk][INFO] ---> ENDPOINT[" + vsURLGetContacts + "]");	
				try {
					vsTotalContacts += vsContactPersona + "\"]";
					Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][SearchContactsBulk][INFO] ---> BODY" + vsTotalContacts.replace("\"", ""));	
					voConexionResponse = voConexionHttp.executePost(vsURLGetContacts, viTO, vsTotalContacts, voHeader);
					if(voConexionResponse.getCodigoRespuesta() == 200) {
						if(voConexionResponse.getMensajeRespuesta().equals("[]")) {
							Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][SearchContactsBulk][INFO] ---> CONTACTS FOUND[], "
									+ "STATUS[" + voConexionResponse.getCodigoRespuesta() + "], MESSAGE[contacts found]");
						} else {
							JSONArray voJSONEncontrados = new JSONArray(voConexionResponse.getMensajeRespuesta());
							for(int i=0;i<voJSONEncontrados.length();i++) 
								if(voJSONEncontrados.getJSONObject(i).has("id")) 
									vlEncontrados.add(voJSONEncontrados.getJSONObject(i).getString("id"));
						}
					} else {
						String vsError = new JSONObject(voConexionResponse.getMensajeError()).getString("message");
						Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][SearchContactsBulk][WARNING] ---> SEARCH CONTACTS [FAILURE], "
								+ "STATUS[" + voConexionResponse.getCodigoRespuesta() + "], MESSAGE[" + vsError + "]");
						Map<String, String> voMapStatusError = new  HashMap<String, String>();
						voMapStatusError.put("status", "" + voConexionResponse.getCodigoRespuesta());
						voMapStatusError.put("message", vsError);
						return voMapStatusError;
					}
				} catch (Exception e) {
					Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][SearchContactsBulk][ERROR] --->" + e.getMessage());	
					Map<String, String> voMapStatusError = new  HashMap<String, String>();
					voMapStatusError.put("status", "500");
					voMapStatusError.put("message", "error");
					return voMapStatusError;
				} finally {
					try {
						Thread.sleep(TIME_SLEEP_TO_REQUEST);
					} catch (InterruptedException e) {
						Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][SearchContactsBulk][ERROR] --->" + e.getMessage());	
						Map<String, String> voMapStatusError = new  HashMap<String, String>();
						voMapStatusError.put("status", "500");
						voMapStatusError.put("message", "error");
						return voMapStatusError;
					}
				}
				viContador = 0;
				vsTotalContacts = "[\"";
				viTotal++;
			} else {
				viTotal++;
				viContador++;
				vsTotalContacts += vsContactPersona + "\",\"";	
			}
		}
		voMapStatus.put("status", "200");
		voMapStatus.put("message", "contacts found");
		StringBuffer voEncontrado = new StringBuffer("");
		int viCont = 0;
		if(vlEncontrados.size()>0) {
			for(String vsContactEncontrado : vlEncontrados) {
				voEncontrado.append(vsContactEncontrado);
				if(++viCont < vlEncontrados.size()) 
					voEncontrado.append(",");
			}
			voMapStatus.put("contactFound", voEncontrado.toString());
		} else {
			voMapStatus.put("contactFound", "");
		}
		Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][SearchContactsBulk][INFO] ---> TOTAL CONTACT FOUND[" + vlEncontrados.size() + "]");
		return voMapStatus;
	}
	
}
