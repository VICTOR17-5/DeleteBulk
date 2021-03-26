package com.unitec.kranon.util;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Properties;

public class Utilidades {
    
    public boolean getProperty(String[] confi, String vsRutaArchivo) {
    	try {
            Properties p = new Properties();
            p.load(new FileReader(vsRutaArchivo));
            for (int i = 0; i < confi.length; i++) {
                String cadena = confi[i];
                confi[i] = p.getProperty(cadena);
                if (confi[i] == null) {
                    confi[i] = "";
                } else {
                    confi[i] = confi[i].trim();
                }
            }
            return true;
        } catch (Exception e) {
        	Log.GuardaLog("[" + new Date() + "][ERROR] NO SE PUDO LEER " + vsRutaArchivo);
            return false;
        }
    }
    
    public void createArchivo(String vsRuta, String vsTexto) {
		File voFile = new File(vsRuta);
		if(voFile.exists()) voFile.delete();
    	PrintWriter pw = null;
        try {
            pw = new PrintWriter(new FileWriter(vsRuta, true));
            pw.print(vsTexto);
            pw.close();
        } catch (Exception e) {
        	Log.GuardaLog("[" + new Date() + "][createArchivo][ERROR] ---> " + e.getMessage());
        }
    }
 
}
