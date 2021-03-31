package com.unitec.kranon.main;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import com.kranon.conexionSQL.ConexionMySQL;
import com.kranon.conexionSQL.ConexionSQL;
import com.unitec.kranon.util.Log;
import com.unitec.kranon.util.Utilidades;

public class Vista extends JFrame {
	private static final long serialVersionUID = 1L;
	private String vsConfi = "C:/Appl/UNITEC/DeleteRegistro/Configuraciones/conf.properties";
	private String[] vaConfi = { "ClientId", "ClientSecret", "Log", "SQL_Table", "MySQL_Table", "Sleep" };
	private String vsUUI;
	private String vsRutaCSV;
	
	private ThreadValidate voValidateThread = null;
	private ThreadDelete voDeleteThread = null;
	private ThreadSearch voSearchThread = null;
	
	private Utilidades voUti;
	private Buffer voBuffer;
	private Thread voWrite;
	
    private JTextField voFieldURL;
    private JPanel voPanelPrincipal;
    private JScrollPane voScroll;
    private JTextArea voTextAreaResultados;
    private JButton voButtonEliminar , voButtonValidar, voButtonBuscar;
    private JSeparator voSeparatorTittle;
    private JButton voButtonToggleTestSQL;
    private JLabel voLabelTittle;
    private JRadioButton voRadioButtonSQL ,voRadioButtonMySQL;
    private ButtonGroup voButtonGroup;
    
    
    private Map<String, List<String>> voMapProduccion;
	private Map<String, List<String>> voMapEliminados;
	private Map<String, List<String>> voMapEliminadosOK;
	private Map<String, List<String>> voMapEliminadosKO;
    
	
	public Vista() {
		initComponents();
		vsUUI = java.util.UUID.randomUUID().toString();
		voUti = new Utilidades();
		Log.GuardaLog("[" + new Date() + "][" + vsUUI + "] ************************************** INICIANDO PROCESO **************************************");
		if (voUti.getProperty(vaConfi, vsConfi)) Log.vsRutaLog = vaConfi[2];
	}
	
	private void ActionButtonBuscar(ActionEvent evt) {   
		JFileChooser voFileChooser = new JFileChooser();
		FileNameExtensionFilter filtro = new FileNameExtensionFilter("CSV (delimitado por comas)", "csv");
		voFileChooser.setFileFilter(filtro);
		if(voFileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			voBuffer = new Buffer();
			vsRutaCSV = voFileChooser.getSelectedFile().getAbsolutePath();
			voFieldURL.setText(vsRutaCSV);
			voWrite = new WriteTextArea(voBuffer, voTextAreaResultados);
			voWrite.start();
			voSearchThread = new ThreadSearch(vsUUI,voBuffer, vsRutaCSV);
			voSearchThread.start();
			voButtonValidar.setEnabled(true);

		}
    }
	
	private void ActionButtonValidate(ActionEvent evt) {
		if(!voRadioButtonSQL.isSelected() && !voRadioButtonMySQL.isSelected()) {
			Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][ActionButtonValidate][WARNING] ---> NO SE ENCONTRO UNA CONEXIÓN A UNA BASE DE DATOS.");
			JOptionPane.showMessageDialog(this, "SELECCIONE EL TIPO DE CONEXIÓN A UTILIZAR.","CONNECTION FAILED.",JOptionPane.WARNING_MESSAGE);
			return;
		}
		if(voSearchThread != null) {
			if(!voSearchThread.isAlive()) {
				StringBuffer voStringBuffer = voSearchThread.getData();
				voMapProduccion = new HashMap<String, List<String>>();
				voMapEliminados = new HashMap<String, List<String>>();
				voBuffer = new Buffer();
				
				if(voRadioButtonSQL.isSelected()) {
					ConexionSQL voConexionSQL = new ConexionSQL(vsUUI);
					voWrite = new WriteTextArea(voBuffer, voTextAreaResultados);
					voWrite.start();
					voValidateThread = new ThreadValidate(voBuffer, vaConfi, vsUUI, voStringBuffer, voMapProduccion, voMapEliminados, voConexionSQL);
					voValidateThread.start();
				} else if(voRadioButtonMySQL.isSelected()) {
					ConexionMySQL voConexionMySQL = new ConexionMySQL(vsUUI);
					voConexionMySQL.CreateProxy();
					voWrite = new WriteTextArea(voBuffer, voTextAreaResultados);
					voWrite.start();
					voValidateThread = new ThreadValidate(voBuffer, vaConfi, vsUUI, voStringBuffer, voMapProduccion, voMapEliminados, voConexionMySQL);
					voValidateThread.start();
				} 
				voButtonEliminar.setEnabled(true);

			}
		}
	}
	
	private void ActionButtonEliminar(ActionEvent evt) {
		voMapEliminadosOK = new HashMap<String,List<String>>();
		voMapEliminadosKO = new HashMap<String, List<String>>();
		voBuffer = new Buffer();
		
		voWrite = new WriteTextArea(voBuffer, voTextAreaResultados);
		voWrite.start();
		voDeleteThread = new ThreadDelete(vaConfi,vsUUI,voBuffer,voMapProduccion,voMapEliminados,voMapEliminadosOK,voMapEliminadosKO);
		voDeleteThread.start();
		
	}
	
	private void ActionButtonConectionSQL(ActionEvent evt) {
		Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][ActionButtonConectionSQL][INFO] ---> INICIANDO TEST DE CONEXION.");
		if(voRadioButtonSQL.isSelected()) {
			ConexionSQL voConexionSQL = new ConexionSQL(vsUUI);
			if(voConexionSQL.getConnection()) JOptionPane.showMessageDialog(this, "CONEXIÓN SQL EXITOSA.","CONNECTION SUCCESSFUL",JOptionPane.INFORMATION_MESSAGE);
			else JOptionPane.showMessageDialog(this, "ERROR SQL,NO SE LOGRO ESTABLECER CONEXIÓN.","CONNECTION  FAILED.",JOptionPane.WARNING_MESSAGE);
		}
		else if(voRadioButtonMySQL.isSelected()) {
			ConexionMySQL voConexionSQL = new ConexionMySQL(vsUUI);
			voConexionSQL.CreateProxy();
			if(voConexionSQL.getConnection()) JOptionPane.showMessageDialog(this, "CONEXIÓN MySQL EXITOSA.","CONNECTION SUCCESSFUL",JOptionPane.INFORMATION_MESSAGE);
			else JOptionPane.showMessageDialog(this, "ERROR MYSQL, NO SE LOGRO ESTABLECER CONEXIÓN.","CONNECTION FAILED.",JOptionPane.WARNING_MESSAGE);
		} else  JOptionPane.showMessageDialog(this, "SELECCIONE EL TIPO DE CONEXIÓN A UTILIZAR.","CONNECTION FAILED.",JOptionPane.WARNING_MESSAGE);
	}
	
	private void initComponents() {
		this.setResizable(false);
		this.setTitle("[UNITEC] CONTACTS DELETE");

		voButtonGroup = new ButtonGroup();
        voPanelPrincipal = new JPanel();
        voButtonToggleTestSQL = new JButton();
        voFieldURL = new JTextField();
        voButtonBuscar = new JButton();
        voScroll = new JScrollPane();
        voTextAreaResultados = new JTextArea();
        voButtonEliminar = new JButton();
        voSeparatorTittle = new JSeparator();
        voLabelTittle = new JLabel();
        voRadioButtonSQL = new JRadioButton();
        voRadioButtonMySQL = new JRadioButton();
        voButtonValidar = new JButton();

        voPanelPrincipal.setBackground(new Color(255, 255, 255));
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        voButtonToggleTestSQL.setText("TEST CONECTION BD");
        voButtonToggleTestSQL.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
            	ActionButtonConectionSQL(evt);
            }
        });

        voFieldURL.setEditable(false);
        voFieldURL.setFont(new Font("Arial", 0, 12));

        voButtonBuscar.setText("BUSCAR CSV");
        voButtonBuscar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
            	ActionButtonBuscar(evt);
            }
        });
        
        voButtonEliminar.setText("ELIMINAR REGISTROS DE PURECLOUD");
        voButtonEliminar.setEnabled(false);
        voButtonEliminar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
            	ActionButtonEliminar(evt);
            }
        });

        voTextAreaResultados.setEditable(false);
        voTextAreaResultados.setBackground(new Color(250, 250, 250));
        voTextAreaResultados.setColumns(20);
        voTextAreaResultados.setRows(5);
        voScroll.setViewportView(voTextAreaResultados);

        voLabelTittle.setFont(new java.awt.Font("Arial", 0, 24)); // NOI18N
        voLabelTittle.setText("ELIMINACION MASIVA");

        voRadioButtonSQL.setText("CONNECTION SQL");
        voRadioButtonSQL.setBackground(new Color(255, 255, 255));
        voRadioButtonMySQL.setText("CONNECTION MYSQL");
        voRadioButtonMySQL.setBackground(new Color(255, 255, 255));
        
        voButtonGroup.add(voRadioButtonSQL);
        voButtonGroup.add(voRadioButtonMySQL);

        voButtonValidar.setText("VALIDAR REGISTROS EN LA BD");
        voButtonValidar.setEnabled(false);
        voButtonValidar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
            	ActionButtonValidate(evt);
            }
        });

        GroupLayout voPanelPrincipalLayout = new GroupLayout(voPanelPrincipal);
        voPanelPrincipal.setLayout(voPanelPrincipalLayout);
        voPanelPrincipalLayout.setHorizontalGroup(
            voPanelPrincipalLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(GroupLayout.Alignment.TRAILING, voPanelPrincipalLayout.createSequentialGroup()
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(voSeparatorTittle, GroupLayout.PREFERRED_SIZE, 882, GroupLayout.PREFERRED_SIZE)
                .addGap(48, 48, 48))
            .addGroup(voPanelPrincipalLayout.createSequentialGroup()
                .addGroup(voPanelPrincipalLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(voPanelPrincipalLayout.createSequentialGroup()
                        .addGap(382, 382, 382)
                        .addComponent(voLabelTittle))
                    .addGroup(voPanelPrincipalLayout.createSequentialGroup()
                        .addGap(106, 106, 106)
                        .addComponent(voButtonValidar, GroupLayout.PREFERRED_SIZE, 298, GroupLayout.PREFERRED_SIZE)
                        .addGap(144, 144, 144)
                        .addComponent(voButtonEliminar, GroupLayout.PREFERRED_SIZE, 298, GroupLayout.PREFERRED_SIZE))
                    .addGroup(voPanelPrincipalLayout.createSequentialGroup()
                        .addGap(56, 56, 56)
                        .addComponent(voScroll, GroupLayout.PREFERRED_SIZE, 882, GroupLayout.PREFERRED_SIZE))
                    .addGroup(voPanelPrincipalLayout.createSequentialGroup()
                        .addGap(83, 83, 83)
                        .addComponent(voFieldURL, GroupLayout.PREFERRED_SIZE, 685, GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(voButtonBuscar, GroupLayout.PREFERRED_SIZE, 123, GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(62, Short.MAX_VALUE))
            .addGroup(GroupLayout.Alignment.TRAILING, voPanelPrincipalLayout.createSequentialGroup()
                .addGap(83, 83, 83)
                .addGroup(voPanelPrincipalLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(voRadioButtonMySQL)
                    .addComponent(voRadioButtonSQL))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(voButtonToggleTestSQL, GroupLayout.PREFERRED_SIZE, 170, GroupLayout.PREFERRED_SIZE)
                .addGap(82, 82, 82))
        );
        voPanelPrincipalLayout.setVerticalGroup(
            voPanelPrincipalLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(GroupLayout.Alignment.TRAILING, voPanelPrincipalLayout.createSequentialGroup()
                .addGroup(voPanelPrincipalLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                    .addGroup(voPanelPrincipalLayout.createSequentialGroup()
                        .addGap(26, 26, 26)
                        .addComponent(voLabelTittle)
                        .addGap(18, 18, 18)
                        .addComponent(voButtonToggleTestSQL)
                        .addGap(0, 12, Short.MAX_VALUE))
                    .addGroup(voPanelPrincipalLayout.createSequentialGroup()
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(voRadioButtonSQL)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(voRadioButtonMySQL)))
                .addGap(18, 18, 18)
                .addComponent(voSeparatorTittle, GroupLayout.PREFERRED_SIZE, 10, GroupLayout.PREFERRED_SIZE)
                .addGap(37, 37, 37)
                .addGroup(voPanelPrincipalLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(voFieldURL, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(voButtonBuscar, GroupLayout.PREFERRED_SIZE, 32, GroupLayout.PREFERRED_SIZE))
                .addGap(50, 50, 50)
                .addComponent(voScroll, GroupLayout.PREFERRED_SIZE, 378, GroupLayout.PREFERRED_SIZE)
                .addGap(28, 28, 28)
                .addGroup(voPanelPrincipalLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(voButtonValidar, GroupLayout.PREFERRED_SIZE, 35, GroupLayout.PREFERRED_SIZE)
                    .addComponent(voButtonEliminar, GroupLayout.PREFERRED_SIZE, 35, GroupLayout.PREFERRED_SIZE))
                .addGap(38, 38, 38))
        );

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup( layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(voPanelPrincipal, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup( layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(voPanelPrincipal, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }
	
	public static void main(String args[]) {
        try {
            javax.swing.UIManager.setLookAndFeel("com.birosoft.liquid.LiquidLookAndFeel");
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Vista.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Vista.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Vista.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Vista.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Vista().setVisible(true);
            }
        });
    }
}
