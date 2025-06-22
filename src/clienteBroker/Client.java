package clienteBroker;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import Resources.ControlCodes;

public class Client extends JFrame implements Runnable{
		
/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final int portListen = 4999;
	private static final int portCtrl = 5999;
	private static final int[] serverPorts = {4446, 4447, 4448};
	private static final String grupoMulticast = "224.48.75.1";
	private MulticastSocket socketListen; // socket para escuchar broadcasts
	private DatagramSocket socketCtrl; // socket para enviar msg de control y recibirlos
	private InetSocketAddress BCADDR;
	private byte[] buf = new byte[256];
	private JFrame consola;
	private JPanel contentPane;
	private JScrollPane salidaMensajes;
	private JTextArea textAreaSalida;
	private JPanel panel;
	private JLabel lista_lbl;
	private JComboBox<String> comboBox;
	private JButton btnStartStop;
	private JButton btnXML;
	private JButton btnJSON;
	private JButton btnLimpiarTerminal;
	private JButton btnSalir;
	private JLabel lblNewLabel;
	private JPanel panel_1;
	private JTextField txtFieldMs;
	private JButton btnCambiarFrecuencia;
	private JPanel panel_2;
	private JButton btnStop;
	private Component rigidArea;
	private JLabel lblErrorMs;
	private ExecutorService exec = Executors.newFixedThreadPool(6);
	private File logs;
	private BufferedWriter logWriter;
	private final DataBroker brokerDatos;
 
    public Client(DataBroker broker) {
    	this.brokerDatos = broker;
    	// ################# CODIGO DE SOCKETS ###############
    	int puerto2 = this.portCtrl;
    	// Mediante este bucle se pueden lanzar varios clientes
    	while(true) {
    		try {
    			this.BCADDR = new InetSocketAddress(InetAddress.getByName(grupoMulticast), portListen);
    			this.socketListen = new MulticastSocket(portListen);
    			this.socketCtrl = new DatagramSocket(puerto2);
    			break;
    		} catch (IOException e) {
    			puerto2++;
    		}
    	}
    	
    	// ################ CODIGO DE LOGS ###############
		try {
			LocalDateTime ahora = LocalDateTime.now();
	        DateTimeFormatter formato = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
	        String nombreLog = ahora.format(formato);

			new File("./logs").mkdirs();
			logs = new File("logs/log" + nombreLog + ".txt");
			logWriter = new BufferedWriter(new FileWriter(logs));
		}
		catch(NullPointerException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
    	
    	// ################# CODIGO DE GUI ###############
    	consola = new JFrame("Consola Cliente"); // Creamos la ventana
        consola.setSize(400, 300);
    	consola.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	consola.setBounds(300, 300, 750, 500);
    	contentPane = new JPanel();
    	contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
    	
    	consola.setContentPane(contentPane);
    	contentPane.setLayout(new BorderLayout(0, 0));
    	
    	JSplitPane splitPane = new JSplitPane();
    	splitPane.setResizeWeight(0.8);
    	splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
    	contentPane.add(splitPane, BorderLayout.CENTER);
    	
    	textAreaSalida = new JTextArea();
    	textAreaSalida.setEditable(false);
    	textAreaSalida.setLineWrap(true); // esto hace que se envuelvan las lineas muy largas
    	textAreaSalida.setWrapStyleWord(true); // esto corta las l�neas por palabras
    	salidaMensajes = new JScrollPane(textAreaSalida);
    	salidaMensajes.setAutoscrolls(true);
    	splitPane.setLeftComponent(salidaMensajes);
    	
    	panel = new JPanel();
    	panel.setBorder(new TitledBorder(null, "Panel de Control", TitledBorder.LEADING, TitledBorder.TOP, null, null));
    	splitPane.setRightComponent(panel);
    	GridBagLayout gbl_panel = new GridBagLayout();
    	gbl_panel.columnWidths = new int[]{0, 0, 75, 94, 83, 0, 0, 0};
    	gbl_panel.rowHeights = new int[]{0, 64, 49, 0};
    	gbl_panel.columnWeights = new double[]{0.0, 0.0, 1.0, 1.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
    	gbl_panel.rowWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
    	panel.setLayout(gbl_panel);
    	
    	btnLimpiarTerminal = new JButton("Limpiar Mensajes");
    	GridBagConstraints gbc_btnLimpiarTerminal = new GridBagConstraints();
    	gbc_btnLimpiarTerminal.insets = new Insets(0, 0, 5, 5);
    	gbc_btnLimpiarTerminal.gridx = 4;
    	gbc_btnLimpiarTerminal.gridy = 0;
    	btnLimpiarTerminal.addActionListener(new ActionListener() {
    		@Override
    		public void actionPerformed(ActionEvent e) {
    			textAreaSalida.setText("");
    		}
    	});
    	panel.add(btnLimpiarTerminal, gbc_btnLimpiarTerminal);
    	
    	btnSalir = new JButton("Salir");
    	GridBagConstraints gbc_btnSalir = new GridBagConstraints();
    	gbc_btnSalir.insets = new Insets(0, 0, 5, 0);
    	gbc_btnSalir.gridx = 6;
    	gbc_btnSalir.gridy = 0;
    	btnSalir.addActionListener(new ActionListener() {
    		@Override
    		public void actionPerformed(ActionEvent e) {
    			try {
    				exec.shutdown(); 
					logWriter.close();
				} catch (IOException e1) {}
    			
    			try {
    				Thread.sleep(500);
    			} catch (InterruptedException b) {
    				b.printStackTrace();
    			}
    			System.exit(0);
    		}
    	});
    	panel.add(btnSalir, gbc_btnSalir);
    	
    	lista_lbl = new JLabel("Selecciona un servidor");
    	GridBagConstraints gbc_lista_lbl = new GridBagConstraints();
    	gbc_lista_lbl.insets = new Insets(0, 0, 5, 5);
    	gbc_lista_lbl.gridx = 1;
    	gbc_lista_lbl.gridy = 1;
    	panel.add(lista_lbl, gbc_lista_lbl);
    	
    	comboBox = new JComboBox<>();
    	comboBox.setModel(new DefaultComboBoxModel(new String[] {"Servidor 1", "Servidor 2", "Servidor 3"}));
    	comboBox.setSelectedIndex(0);
    	comboBox.setEditable(false);
    	GridBagConstraints gbc_comboBox = new GridBagConstraints();
    	gbc_comboBox.ipadx = 20;
    	gbc_comboBox.insets = new Insets(0, 0, 0, 5);
    	gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
    	gbc_comboBox.gridx = 1;
    	gbc_comboBox.gridy = 2;
    	panel.add(comboBox, gbc_comboBox);
    	
    	panel_2 = new JPanel();
    	GridBagConstraints gbc_panel_2 = new GridBagConstraints();
    	gbc_panel_2.insets = new Insets(0, 10, 0, 10);
    	gbc_panel_2.fill = GridBagConstraints.BOTH;
    	gbc_panel_2.gridx = 2;
    	gbc_panel_2.gridy = 2;
    	panel.add(panel_2, gbc_panel_2);
    	
    	btnStop = new JButton("Stop");
    	btnStop.setAlignmentY(Component.BOTTOM_ALIGNMENT);
    	btnStop.setAlignmentX(Component.CENTER_ALIGNMENT);
    	btnStop.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent e) {
    			exec.submit(() -> enviaControl(ControlCodes.STOP));
    		}
    	});
    	panel_2.setLayout(new BoxLayout(panel_2, BoxLayout.Y_AXIS));
    	panel_2.add(btnStop);
    	
    	rigidArea = Box.createRigidArea(new Dimension(20, 20));
    	panel_2.add(rigidArea);
    	
    	btnStartStop = new JButton("Continua");
    	btnStartStop.setAlignmentX(Component.CENTER_ALIGNMENT);
    	panel_2.add(btnStartStop);
    	btnStartStop.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				exec.submit(() -> enviaControl(ControlCodes.CONTINUE));
			}
		});
    	
    	panel_1 = new JPanel();
    	panel_1.setToolTipText("Introduce el intervalo de tiempo en ms que quieres que espere el servidor entre mensajes ");
    	GridBagConstraints gbc_panel_1 = new GridBagConstraints();
    	gbc_panel_1.insets = new Insets(0, 10, 0, 10);
    	gbc_panel_1.fill = GridBagConstraints.BOTH;
    	gbc_panel_1.gridx = 3;
    	gbc_panel_1.gridy = 2;
    	panel.add(panel_1, gbc_panel_1);
    	panel_1.setLayout(new BoxLayout(panel_1, BoxLayout.Y_AXIS));
    	
    	lblNewLabel = new JLabel("Introduce los ms");
    	lblNewLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    	panel_1.add(lblNewLabel);
    	
    	txtFieldMs = new JTextField();
    	txtFieldMs.setHorizontalAlignment(SwingConstants.LEFT);
    	txtFieldMs.setToolTipText("Introduce el intervalo de tiempo en ms que quieres que espere el servidor entre mensajes ");
    	txtFieldMs.setText("3000");
    	panel_1.add(txtFieldMs);
    	txtFieldMs.setColumns(10);
    	
    	btnCambiarFrecuencia = new JButton("Cambiar Freq.");
    	btnCambiarFrecuencia.setAlignmentX(Component.CENTER_ALIGNMENT);
    	panel_1.add(btnCambiarFrecuencia);
    	btnCambiarFrecuencia.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				exec.submit(() -> enviaControl(ControlCodes.MOD_FREQ));
			}
		});
    	
    	lblErrorMs = new JLabel("");
    	lblErrorMs.setAlignmentX(Component.CENTER_ALIGNMENT);
    	lblErrorMs.setVisible(false);
    	panel_1.add(lblErrorMs);
    	
    	btnXML = new JButton("Env\u00EDa XML");
    	GridBagConstraints gbc_btnXML = new GridBagConstraints();
    	gbc_btnXML.insets = new Insets(0, 0, 0, 5);
    	gbc_btnXML.gridx = 4;
    	gbc_btnXML.gridy = 2;
    	panel.add(btnXML, gbc_btnXML);
    	btnXML.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				exec.submit(() -> enviaControl(ControlCodes.SEND_XML));
			}
		});
    	
    	btnJSON = new JButton("Env\u00EDa JSON");
    	GridBagConstraints gbc_btnJSON = new GridBagConstraints();
    	gbc_btnJSON.gridx = 6;
    	gbc_btnJSON.gridy = 2;
    	panel.add(btnJSON, gbc_btnJSON);
    	btnJSON.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				exec.submit(() -> enviaControl(ControlCodes.SEND_JSON));
			}
		});
    	
    	
    	try {
    		this.consola.setVisible(true);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	
    }
    
    public void recibePaquete() {
    	while (true) {
	    	try {
	    			DatagramPacket pak = new DatagramPacket(buf, buf.length);
	    			socketListen.receive(pak);
	    			String msg = new String(pak.getData(), 0, pak.getLength());
	    			String datos = "";
	    			if(msg.startsWith("<")) 
	    				datos = ClientParser.parsearPaqueteXML(msg);
	    			else datos = ClientParser.parsearPaqueteJSON(msg);

	    			printea(datos);
	//			} 
	    	}
	    	catch (IOException e) {
	    		e.printStackTrace();
	    	} catch (Exception e) {
				e.printStackTrace();
			}
    	}
    }
    
    public void enviaControl(ControlCodes codigo) {
    	int serverSelection = comboBox.getSelectedIndex();
    	
    	byte [] bufResp;
    	if(codigo == ControlCodes.MOD_FREQ) {
    		int dato = 0;
    		try {
				dato = Integer.parseInt(txtFieldMs.getText());
			} catch (NumberFormatException e) {
				lblErrorMs.setText("No se reconocen los ms");
				lblErrorMs.setVisible(true);
				return;
			}
    		bufResp = ClientParser.creaControl(codigo, dato).getBytes();
    	}
    	else bufResp = ClientParser.creaControl(codigo, 0).getBytes();
    	
		DatagramPacket resp = new DatagramPacket(bufResp, serverSelection);
		try {
			socketCtrl.setSoTimeout(5000);
			resp = new DatagramPacket(bufResp, bufResp.length, InetAddress.getLocalHost(), serverPorts[serverSelection]);
		} catch (SocketException e1) {
			e1.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		printea("CONTROL: enviando " + codigo + " al " + (String) comboBox.getSelectedItem() + "\n");
		for(int i = 0; i < 3; i++) {
			if (i > 0) SwingUtilities.invokeLater(() ->printea("CONTROL: Reintentando enviar mensaje de control...\n\n"));
			try {
				socketCtrl.send(resp); 
				byte[] buf = new byte[256];
				DatagramPacket ack = new DatagramPacket(buf, buf.length);
				socketCtrl.receive(ack);
				String ackStr = new String(ack.getData(), 0, ack.getLength());
				
				SwingUtilities.invokeLater(() ->printea("CONTROL: " + ackStr + "\n\n"));
				return;
			} catch(SocketTimeoutException sockEx) {
				SwingUtilities.invokeLater(() ->printea("CONTROL: No se ha recibido confirmaci�n del servidor. \n\n"));
				continue;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
    
    
    public static void main(String[] args) {
		SwingUtilities.invokeLater(new Client(new DataBroker()));
		
	}
    
    private void printea(String texto) {
    	textAreaSalida.append(texto);
		try {
			logWriter.write(texto);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		textAreaSalida.setCaretPosition(textAreaSalida.getDocument().getLength());
    }
    
    public void run() {    	
    	try {
			socketListen.joinGroup(BCADDR, NetworkInterface.getByName(grupoMulticast));
		} catch (IOException e) {
			e.printStackTrace();
		}
    	exec.submit(() -> recibePaquete());
    }
}
    
