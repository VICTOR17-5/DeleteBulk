package com.kranon.conexionHttp;

import java.util.HashMap;
import java.util.List;

public class ConexionResponse {
	private int codigoRespuesta;

	private String mensajeEnvio;
	private String mensajeEnvioCodificado;
	private HashMap<String, String> headerEnvio;

	private String mensajeRespuesta;
	private String mensajeRespuestaCodificado;
	private HashMap<String, List<String>> headerRespuesta;

	private String resultado;
	private String mensajeError;

	public int getCodigoRespuesta() {
		return codigoRespuesta;
	}

	public void setCodigoRespuesta(int codigoRespuesta) {
		this.codigoRespuesta = codigoRespuesta;
	}

	public String getMensajeEnvio() {
		return mensajeEnvio;
	}

	public void setMensajeEnvio(String mensajeEnvio) {
		this.mensajeEnvio = mensajeEnvio;
	}

	public HashMap<String, String> getHeaderEnvio() {
		return headerEnvio;
	}

	public void setHeaderEnvio(HashMap<String, String> headerEnvio) {
		this.headerEnvio = headerEnvio;
	}

	public String getMensajeRespuesta() {
		return mensajeRespuesta;
	}

	public void setMensajeRespuesta(String mensajeRespuesta) {
		this.mensajeRespuesta = mensajeRespuesta;
	}

	public HashMap<String, List<String>> getHeaderRespuesta() {
		return headerRespuesta;
	}

	public void setHeaderRespuesta(HashMap<String, List<String>> headerRespuesta) {
		this.headerRespuesta = headerRespuesta;
	}

	public String getResultado() {
		return resultado;
	}

	public void setResultado(String resultado) {
		this.resultado = resultado;
	}

	public String getMensajeError() {
		return mensajeError;
	}

	public void setMensajeError(String mensajeError) {
		this.mensajeError = mensajeError;
	}

	public String getMensajeEnvioCodificado() {
		return mensajeEnvioCodificado;
	}

	public void setMensajeEnvioCodificado(String mensajeEnvioCodificado) {
		this.mensajeEnvioCodificado = mensajeEnvioCodificado;
	}

	public String getMensajeRespuestaCodificado() {
		return mensajeRespuestaCodificado;
	}

	public void setMensajeRespuestaCodificado(String mensajeRespuestaCodificado) {
		this.mensajeRespuestaCodificado = mensajeRespuestaCodificado;
	}

}

