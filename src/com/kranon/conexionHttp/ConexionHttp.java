package com.kranon.conexionHttp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ConexionHttp {
	
	public ConexionResponse executeGet ( String url, int timeout, HashMap<String, String> header, HashMap<String, String> paramsUrl ) {
		ArrayList<ParamEvent> parametrosEntrada = new ArrayList<ParamEvent>();
		parametrosEntrada.add(new ParamEvent("url", url));
		parametrosEntrada.add(new ParamEvent("header", header == null ? "null" : header.toString()));
		parametrosEntrada.add(new ParamEvent("paramsUrl", (paramsUrl == null ? "null" : paramsUrl.toString())));
		HttpURLConnection conn = null;
		BufferedReader br = null;
		BufferedReader brError = null;
		OutputStreamWriter os = null;
		ConexionResponse conexionResponse = new ConexionResponse();
		try {
			String urlEnvio = url;
			if (paramsUrl != null) {
				urlEnvio = urlEnvio + "?";
				Iterator<Entry<String, String>> it = paramsUrl.entrySet().iterator();
				while (it.hasNext()) {
					Entry<String, String> e = (Map.Entry<String, String>) it.next();
					urlEnvio = urlEnvio + e.getKey() + "=" + e.getValue();
					if (it.hasNext()) {
						urlEnvio = urlEnvio + "&";
					}
				}
			}
			URL urlServicio = new URL(urlEnvio);
			conn = (HttpURLConnection) urlServicio.openConnection();
			conn.setReadTimeout(timeout);
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");		
			if (header != null) {
				Iterator<Entry<String, String>> it = header.entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry<String, String> e = (Map.Entry<String, String>) it.next();
					conn.addRequestProperty(e.getKey(), e.getValue());
				}
			}
			conexionResponse.setHeaderEnvio(header);
			conexionResponse.setMensajeEnvio("");
			int responseCode = conn.getResponseCode();
			conexionResponse.setCodigoRespuesta(responseCode);
			
			if (responseCode != 200 && responseCode != 201) {
				String mensajeError = "";
				if (conn.getErrorStream()!= null) {
					brError = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
					mensajeError = brError.readLine();
					conexionResponse.setResultado("KO");
					conexionResponse.setMensajeError(mensajeError);
				} else {
					conexionResponse.setResultado("KO");
					conexionResponse.setMensajeError(null);
				}
			} else {
				conexionResponse.setResultado("OK");
				conexionResponse.setMensajeError("");
				conexionResponse.setMensajeRespuesta("");
				br = new BufferedReader(new InputStreamReader((conn.getInputStream()),"UTF-8"));
				String output;
				while ((output = br.readLine()) != null) {
					conexionResponse.setMensajeRespuesta(conexionResponse.getMensajeRespuesta() + output);
				}
				Map<String, List<String>> map = conn.getHeaderFields();
				for (Map.Entry<String, List<String>> entry : map.entrySet()) {
					if (conexionResponse.getHeaderRespuesta() == null) {
						conexionResponse.setHeaderRespuesta(new HashMap<String, List<String>>());
					}
					conexionResponse.getHeaderRespuesta().put(entry.getKey(), entry.getValue());
				}
			}
		} catch (Exception e) {
			ArrayList<ParamEvent> parametrosAdicionales = new ArrayList<ParamEvent>();
			parametrosAdicionales.add(new ParamEvent("error", e.toString()));
			conexionResponse.setResultado("ERROR");
			conexionResponse.setMensajeError(e.getMessage());
		} finally {
			try {
				if (conn != null) {
					conn.disconnect();
				}
				if (os != null) {
					os.close();
				}
				if (br != null) {
					br.close();
				}
				if (brError != null) {
					brError.close();
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
			ArrayList<ParamEvent> parametrosSalida = new ArrayList<ParamEvent>();
			parametrosSalida.add(new ParamEvent("conexionResponse", conexionResponse.toString()));
		}
		return conexionResponse;
	}

	public ConexionResponse executePost( String url, int timeout, String body, HashMap<String, String> header ) {
		ArrayList<ParamEvent> parametrosEntrada = new ArrayList<ParamEvent>();
		parametrosEntrada.add(new ParamEvent("url", url));
		parametrosEntrada.add(new ParamEvent("body", body));
		parametrosEntrada.add(new ParamEvent("header", header == null ? "null" : header.toString()));
		HttpURLConnection conn = null;
		BufferedReader br = null;
		BufferedReader brError = null;
		OutputStreamWriter os = null;
		ConexionResponse conexionResponse = new ConexionResponse();
		try {
			URL urlServicio = new URL(url);
			conn = (HttpURLConnection) urlServicio.openConnection();
			conn.setReadTimeout(timeout);
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setConnectTimeout(timeout);
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("Content-enconding", "chunked");
			conn.setRequestProperty("Accept", "application/json");			
			if (header != null) {
				Iterator<Entry<String, String>> it = header.entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry<String, String> e = (Map.Entry<String, String>) it.next();
					conn.addRequestProperty(e.getKey(), e.getValue());
				}
			}
			conexionResponse.setHeaderEnvio(header);
			conexionResponse.setMensajeEnvio(body);
			os = new OutputStreamWriter(conn.getOutputStream());
			os.write(body);
			os.flush();
			int responseCode = conn.getResponseCode();
			conexionResponse.setCodigoRespuesta(responseCode);

			if (responseCode != 200 && responseCode != 201) {
				String mensajeError = "";
				if(conn.getErrorStream() != null) {	
					brError = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
					mensajeError = brError.readLine();
				}
				conexionResponse.setResultado("KO");
				conexionResponse.setMensajeError(mensajeError);
				conexionResponse.setMensajeRespuesta("");
				Map<String, List<String>> map = conn.getHeaderFields();
				if (map != null) {	
					for (Map.Entry<String, List<String>> entry : map.entrySet()) {
						if (conexionResponse.getHeaderRespuesta() == null) {
							conexionResponse.setHeaderRespuesta(new HashMap<String, List<String>>());
						}
						conexionResponse.getHeaderRespuesta().put(entry.getKey(), entry.getValue());
					}
				}
			} else {
				conexionResponse.setResultado("OK");
				conexionResponse.setMensajeError("");
				conexionResponse.setMensajeRespuesta("");
				br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
				String output;
				while ((output = br.readLine()) != null) {
					conexionResponse.setMensajeRespuesta(conexionResponse.getMensajeRespuesta() + output);
				}
				Map<String, List<String>> map = conn.getHeaderFields();
				for (Map.Entry<String, List<String>> entry : map.entrySet()) {
					if (conexionResponse.getHeaderRespuesta() == null) {
						conexionResponse.setHeaderRespuesta(new HashMap<String, List<String>>());
					}
					conexionResponse.getHeaderRespuesta().put(entry.getKey(), entry.getValue());
				}
			}
		} catch (Exception e) {
			ArrayList<ParamEvent> parametrosAdicionales = new ArrayList<ParamEvent>();
			parametrosAdicionales.add(new ParamEvent("error", e.toString()));
			conexionResponse.setResultado("ERROR");
			conexionResponse.setMensajeError(e.getMessage());
			conexionResponse.setMensajeRespuesta("");
		} finally {
			try {
				if (conn != null) {
					conn.disconnect();
				}
				if (os != null) {
					os.close();
				}
				if (br != null) {
					br.close();
				}
				if (brError != null) {
					brError.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			ArrayList<ParamEvent> parametrosSalida = new ArrayList<ParamEvent>();
			parametrosSalida.add(new ParamEvent("conexionResponse", conexionResponse.toString()));
		}
		return conexionResponse;
	}

	public ConexionResponse executePut( String url, int timeout, String body, HashMap<String, String> header ) {
		ArrayList<ParamEvent> parametrosEntrada = new ArrayList<ParamEvent>();
		parametrosEntrada.add(new ParamEvent("url", url));
		parametrosEntrada.add(new ParamEvent("body", body));
		parametrosEntrada.add(new ParamEvent("header", header == null ? "null" : header.toString()));
		HttpURLConnection conn = null;
		BufferedReader br = null;
		BufferedReader brError = null;
		OutputStreamWriter os = null;
		ConexionResponse conexionResponse = new ConexionResponse();
		try {
			URL urlServicio = new URL(url);
			conn = (HttpURLConnection) urlServicio.openConnection();
			conn.setReadTimeout(timeout);
			conn.setDoOutput(true);
			conn.setRequestMethod("PUT");
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("Content-enconding", "chunked");
			conn.setRequestProperty("Accept", "application/json");
			if (header != null) {
				Iterator<Entry<String, String>> it = header.entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry<String, String> e = (Map.Entry<String, String>) it.next();
					conn.addRequestProperty(e.getKey(), e.getValue());
				}
			}
			conexionResponse.setHeaderEnvio(header);
			conexionResponse.setMensajeEnvio(body);
			os = new OutputStreamWriter(conn.getOutputStream());
			os.write(body);
			os.flush();
			int responseCode = conn.getResponseCode();
			conexionResponse.setCodigoRespuesta(responseCode);
			if (responseCode != 200) {
				brError = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
				String mensajeError = brError.readLine();
				conexionResponse.setResultado("KO");
				conexionResponse.setMensajeError(mensajeError);
			} else {
				conexionResponse.setResultado("OK");
				conexionResponse.setMensajeError("");
				br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
				String output;
				while ((output = br.readLine()) != null) {
					conexionResponse.setMensajeRespuesta(conexionResponse.getMensajeRespuesta() + output);
				}
				Map<String, List<String>> map = conn.getHeaderFields();
				for (Map.Entry<String, List<String>> entry : map.entrySet()) {
					if (conexionResponse.getHeaderRespuesta() == null) {
						conexionResponse.setHeaderRespuesta(new HashMap<String, List<String>>());
					}
					conexionResponse.getHeaderRespuesta().put(entry.getKey(), entry.getValue());
				}
			}
		} catch (Exception e) {
			ArrayList<ParamEvent> parametrosAdicionales = new ArrayList<ParamEvent>();
			parametrosAdicionales.add(new ParamEvent("error", e.toString()));
			conexionResponse.setResultado("KO");
			conexionResponse.setMensajeError(e.getMessage());
		} finally {
			try {
				if (conn != null) {
					conn.disconnect();
				}
				if (os != null) {
					os.close();
				}
				if (br != null) {
					br.close();
				}
				if (brError != null) {
					brError.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			ArrayList<ParamEvent> parametrosSalida = new ArrayList<ParamEvent>();
			parametrosSalida.add(new ParamEvent("conexionResponse", conexionResponse.toString()));
		}
		return conexionResponse;
	}
	
	public ConexionResponse executeDelete(String url, int timeout, HashMap<String,String> header) {
        ArrayList<ParamEvent> parametrosEntrada = new ArrayList<ParamEvent>();
        parametrosEntrada.add(new ParamEvent("url", url));
        parametrosEntrada.add(new ParamEvent("header", header == null ? "null" : header.toString()));
        HttpURLConnection conn = null;
        BufferedReader br = null;
        BufferedReader brError = null;
        OutputStreamWriter os = null;
        ConexionResponse conexionResponse = new ConexionResponse();
        try {
        String urlEnvio = url;
        URL urlServicio = new URL(urlEnvio);
        conn = (HttpURLConnection) urlServicio.openConnection();
        conn.setReadTimeout(timeout);
        conn.setRequestMethod("DELETE");
        if (header != null) {
            Iterator<Entry<String, String>> it = header.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, String> e = (Map.Entry<String, String>) it.next();
                conn.addRequestProperty(e.getKey(), e.getValue());
            }
        }
        conexionResponse.setHeaderEnvio(header);
        conexionResponse.setMensajeEnvio("");
        int responseCode = conn.getResponseCode();
        conexionResponse.setCodigoRespuesta(responseCode);
        if (responseCode != 200 && responseCode != 201) {
            String mensajeError = "";
            if (conn.getErrorStream()!= null) {
                brError = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                mensajeError = brError.readLine();
                conexionResponse.setResultado("KO");
                conexionResponse.setMensajeError(mensajeError);
            } else {
                conexionResponse.setResultado("KO");
                conexionResponse.setMensajeError(null);
            }
        } else {
            conexionResponse.setResultado("OK");
            conexionResponse.setMensajeError("");
            conexionResponse.setMensajeRespuesta("");
            br = new BufferedReader(new InputStreamReader((conn.getInputStream()),"UTF-8"));
            String output;
            while ((output = br.readLine()) != null) {
                conexionResponse.setMensajeRespuesta(conexionResponse.getMensajeRespuesta() + output);
            }
            Map<String, List<String>> map = conn.getHeaderFields();
            for (Map.Entry<String, List<String>> entry : map.entrySet()) {
                if (conexionResponse.getHeaderRespuesta() == null) {
                    conexionResponse.setHeaderRespuesta(new HashMap<String, List<String>>());
                }
                conexionResponse.getHeaderRespuesta().put(entry.getKey(), entry.getValue());
            }
        }
        } catch (Exception e) {
            ArrayList<ParamEvent> parametrosAdicionales = new ArrayList<ParamEvent>();
            parametrosAdicionales.add(new ParamEvent("error", e.toString()));
            conexionResponse.setResultado("ERROR");
            conexionResponse.setMensajeError(e.getMessage());
        } finally {
            try {
                if (conn != null) {
                    conn.disconnect();
                }
                if (os != null) {
                    os.close();
                }
                if (br != null) {
                    br.close();
                }
                if (brError != null) {
                    brError.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            ArrayList<ParamEvent> parametrosSalida = new ArrayList<ParamEvent>();
            parametrosSalida.add(new ParamEvent("conexionResponse", conexionResponse.toString()));
        }
        return conexionResponse;
    }
}

