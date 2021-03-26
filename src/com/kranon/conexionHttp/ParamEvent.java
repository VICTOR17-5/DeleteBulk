package com.kranon.conexionHttp;

public class ParamEvent {

	   private String clave;
	   private String valor;

	   public ParamEvent(String clave, String valor) {
	       this.clave = clave;
	       this.valor = valor;
	   }

	   public String getClave() {
	       return clave;
	   }

	   public void setClave(String clave) {
	       this.clave = clave;
	   }

	   public String getValor() {
	       return valor;
	   }

	   public void setValor(String valor) {
	       this.valor = valor;
	   }

	   public String toString() {
	       return clave + "=" + valor;
	   }
}