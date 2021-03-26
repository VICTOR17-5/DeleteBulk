package com.kranon.conexionSQL;

import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.Properties;

import com.unitec.kranon.util.Log;

public class ConexionSQL {
	public String vsUUI = "N/A";
	Connection voConnection = null;
	private String vsConfi = "C:/Appl/UNITEC/DeleteRegistro/Configuraciones/conf.properties";
	private final String url = "jdbc:sqlserver://";
	private String serverName = "VPAREDES";
	private String portNumber = "5678";
	private String database = "insertcontactunitec";
	private String userName = "sa";
	private String password = "Kranon01#";
 
	public ConexionSQL(String vsUUI) {
		this.vsUUI = vsUUI;
		Properties properties = new Properties();
		 try {
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			properties.load(new FileReader(vsConfi));
			serverName = properties.getProperty("SQL_ServerName");
			portNumber = properties.getProperty("SQL_Port");
			database = properties.getProperty("SQL_DataBase");
			userName = properties.getProperty("SQL_UserName");
			password = properties.getProperty("SQL_Password");
			String urlSQL = url + serverName + ":" + portNumber + ";" + "databaseName=" + database + ";";
			voConnection = java.sql.DriverManager.getConnection(urlSQL,userName,password);
			Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][ConexionSQL][INFO] ---> CONEXION[SUCCESS].");
		} catch (IOException e) {
			Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][ConexionSQL][ERROR] ---> " + e.getMessage());
		} catch (ClassNotFoundException e) {
			Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][ConexionSQL][ERROR] ---> " + e.getMessage());
		} catch (SQLException e) {
			Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][ConexionSQL][ERROR] ---> " + e.getMessage());
		}
	}
	
	public Boolean getConnection() {
		if(voConnection == null) return false;
		try {
			voConnection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return true;
	}

	 public Integer ExecuteQuery(String vsQuery) {
		 try {
			 if (voConnection != null) {
				  Statement select = voConnection.createStatement(); 
				  Integer viResultado = select.executeUpdate(vsQuery);
				  select.close();
				  return viResultado;
			 } else Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][ExecuteQuery][WARNING] ---> NO EXISTE UNA CONEXION A LA BASE DE DATOS.");
	 	} catch (Exception e) {
	 		Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][ExecuteQuery][ERROR] ---> " + e.getMessage());
	 	}
		 return -10;
	 }
	 
	 public ResultSet ExecuteSelect(String vsQuery) {
		 try {
			 if (voConnection != null) {
				  Statement select = voConnection.createStatement(); 
				  ResultSet viResultado = select.executeQuery(vsQuery);
				  return viResultado;
			 } else Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][ExecuteSelect][WARNING] ---> NO EXISTE UNA CONEXION A LA BASE DE DATOS.");
	 	} catch (Exception e) {
	 		Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][ExecuteSelect][ERROR] ---> " + e.getMessage());
	 	}
		 return null;
	 }
	 
	 public void closeConnection() {
		 try {
			 if (voConnection != null) {
				 voConnection.close();
			 }
		 } catch (Exception e) {
			 Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][closeConnection][ERROR] ---> " + e.getMessage());
		 }
	 }

}
