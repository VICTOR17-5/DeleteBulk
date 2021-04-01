package com.unitec.util;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Log {
	public static String vsRutaLog = "C:/Appl/UNITEC/DeleteRegistro/Logs/";
	private static String ArchLog = "";
	private static String vsFecha = "";
	
	public synchronized static void GuardaLog(String cadena) {
		Calendar c = Calendar.getInstance();
		vsFecha = new SimpleDateFormat("yyyy-MM-dd").format(c.getTime());
        ArchLog = vsFecha + ".log";
        if (ArchLog != null) {
            PrintWriter pw = null;
            try {
                pw = new PrintWriter(new FileWriter(vsRutaLog + ArchLog, true));
                pw.println(cadena);
                pw.close();
            } catch (Exception e) {
                ArchLog = "Errores.log";
                 System.out.println("Error en la creacion de logs, no se pudo guardar la cadena: " + cadena);
            }
        }
    }
	
	public synchronized static void GuardaLog(String vsTipo, String cadena) {
		Calendar c = Calendar.getInstance();
		vsFecha = new SimpleDateFormat("yyyy-MM-dd").format(c.getTime());
        ArchLog = vsFecha + "_" + vsTipo + ".log";
        if (ArchLog != null) {
            PrintWriter pw = null;
            try {
                pw = new PrintWriter(new FileWriter(vsRutaLog + ArchLog, true));
                pw.println(cadena);
                pw.close();
            } catch (Exception e) {
                ArchLog = "Errores.log";
                 System.out.println("Error en la creacion de logs, no se pudo guardar la cadena: " + cadena);
            }
        }
	}
}
