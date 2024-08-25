import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import javax.imageio.ImageIO;

import javax.swing.Timer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.IAxis;
import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.ITracePainter;
import info.monitorenter.gui.chart.traces.Trace2DLtd;
import info.monitorenter.gui.chart.traces.Trace2DSimple;

public class DisplayFrame 
{
	private JFrame mainFrame;
	private JTextField cmnd_Field, tempField;
	private JComboBox<String> portList ;
	private JButton Start, Connect, R, Disconnect, clearGraph, save;
	private JPanel graphPanel;
	private JTextPane toutTextPane;
	private Timer displayUpdateTimer;
	public int var = 1;
	public int i = 0;	
	static Chart2D chart;
	static final int NTRACES = 3;
    static ITrace2D mtraces[] = new ITrace2D[3];
	private ITrace2D trace;
    private int numTraces = 1000;
    private SimpleAttributeSet TextSet = new SimpleAttributeSet();
    static int Downl_Cnt;
    static boolean Pb_Ready;
    static final int NROWS = 1000, NCOLS = 1;
    static double Plot_Buffer[][] = new double[NROWS][NCOLS];
    static int Pb_NValues;
    private String file = "OutputFile";
    public static final boolean DEBUG = true;
    
	
	public static void main(String[] args) {
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					DisplayFrame window = new DisplayFrame();
					window.mainFrame.setVisible(true);
				}
			catch (Exception e) {
				e.printStackTrace();
				}	
			}
		});
	}

/** Default Constructor 
 * 	wherein the initialise function is called
 */
	public DisplayFrame() {
		initialize();
	}
	
/** Making a function to initialise i.e construct a main display frame for GUI
 * @param mainFrame is the base frame that is divided into panels as per requirement
 * A timer - displayUpdateTimer is started in the initialise functions soon as the frame is made, which checks for incoming 
 * inputs in a forever loop.
 */
	
	private void initialize() {
		
		/** Create a main frame and setting required parameters
		* using spring layout throughout the frame to organise the different panels created within
		* using flow layout for arranging the components in Reading panel
		*/
		mainFrame = new JFrame();
		mainFrame.setTitle("PulseOximeter 0.0.1");
		mainFrame.setVisible(true);
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.setBounds(100, 100, 930, 650);
		SpringLayout springLayout =  new SpringLayout();
		mainFrame.getContentPane().setLayout(springLayout);
		
		// adding command panel and command text field
		JPanel Cmnd_panel = new JPanel();
		Cmnd_panel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		FlowLayout flowLayout = (FlowLayout) Cmnd_panel.getLayout();
		flowLayout.setHgap(10);
		flowLayout.setAlignment(FlowLayout.LEFT);
		springLayout.putConstraint(SpringLayout.NORTH, Cmnd_panel, -45, SpringLayout.SOUTH, mainFrame.getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, Cmnd_panel, -10, SpringLayout.EAST, mainFrame.getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, Cmnd_panel, 10, SpringLayout.WEST, mainFrame.getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, Cmnd_panel, -10, SpringLayout.SOUTH, mainFrame.getContentPane());
		mainFrame.getContentPane().add(Cmnd_panel);
		
		JLabel lblNewLabel = new JLabel("Command: ");
		lblNewLabel.setHorizontalAlignment(SwingConstants.LEFT);
		Cmnd_panel.add(lblNewLabel);
		
		cmnd_Field = new JTextField();
		cmnd_Field.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				CommandHandler(cmnd_Field.getText());
				cmnd_Field.setText("");
			}
		});
				
		Cmnd_panel.add(cmnd_Field);
		cmnd_Field.setColumns(60);
		
		// adding reading panel
		JPanel Readings_panel = new JPanel();
		Readings_panel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		FlowLayout flowLayout1 = (FlowLayout) Readings_panel.getLayout();
		flowLayout1.setHgap(10);
		flowLayout1.setVgap(2);
		flowLayout1.setAlignment(FlowLayout.LEFT);
		springLayout.putConstraint(SpringLayout.NORTH, Readings_panel, 10, SpringLayout.NORTH, mainFrame.getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, Readings_panel, 45, SpringLayout.NORTH, mainFrame.getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, Readings_panel, 10, SpringLayout.WEST, mainFrame.getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, Readings_panel, -10, SpringLayout.EAST, mainFrame.getContentPane());
		
		// adding drop down list for selecting port
		portList = new JComboBox<String>();
		addCommPorts();
		
		// adding button to refresh the available comm ports
		R = new JButton(" R ");
		R.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent arg0) {
			portList.removeAllItems();
			addCommPorts();

		}
			});
		R.setFocusable(false);
		R.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		R.setBackground(Color.LIGHT_GRAY);
		
		// adding connect button and its action listener
		Connect = new JButton("Connect ");
		Connect.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent arg0) {
			actionConnect();
		}
			});
		Connect.setFocusable(false);
		Connect.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		Readings_panel.add(portList);
		Readings_panel.add(R);
		Readings_panel.add(Connect);
		
		// adding disconnect button and its action listener
		Disconnect = new JButton();
		Disconnect.setText("Disconnect ");
		Disconnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				actionDisconnect();
			}
		});
		Disconnect.setFocusable(false);
		Disconnect.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		Disconnect.setEnabled(false);
		Readings_panel.add(Disconnect);

		
		// adding Start button and its action listener
		Start = new JButton();
		Start.setText("Start ");
		Start.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				actionStart();
			}
		});
		Start.setFocusable(false);
		Start.setEnabled(false);
		Start.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		Readings_panel.add(Start);		

		JLabel readingLbl = new JLabel("Temp:");
		Readings_panel.add(readingLbl);
		tempField = new JTextField();
		Readings_panel.add(tempField);
		tempField.setColumns(5);
		tempField.setEditable(false);
		tempField.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		tempField.setBackground(Color.WHITE);
		
		// adding Clear graph button and its action listener
		clearGraph = new JButton();
		clearGraph.setText("Clear Graph ");
		clearGraph.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				actionClearGraph();
								
			}
		});
		clearGraph.setFocusable(false);
		clearGraph.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		clearGraph.setEnabled(false);
		Readings_panel.add(clearGraph);

		// adding save button and its action listener
		save = new JButton();
		save.setText("Save ");
		save.setHorizontalAlignment(SwingConstants.RIGHT);
		save.setEnabled(false);
		save.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				saveChart();
				saveFile();
			}
		});
		save.setFocusable(false);
		save.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		Readings_panel.add(save);
				
		mainFrame.getContentPane().add(Readings_panel);
		
		//adding scroll pane
		JScrollPane scrollPane = new JScrollPane();
		springLayout.putConstraint(SpringLayout.SOUTH, scrollPane, -10, SpringLayout.NORTH, Cmnd_panel);
		springLayout.putConstraint(SpringLayout.EAST, scrollPane, 250, SpringLayout.WEST, mainFrame.getContentPane());
		springLayout.putConstraint(SpringLayout.NORTH, scrollPane, 55, SpringLayout.NORTH, mainFrame.getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, scrollPane, 10, SpringLayout.WEST, mainFrame.getContentPane());
		mainFrame.getContentPane().add(scrollPane);
		
		//adding graph panel
		graphPanel = new JPanel();
		graphPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		springLayout.putConstraint(SpringLayout.NORTH, graphPanel, 55, SpringLayout.NORTH, mainFrame.getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, graphPanel, 10, SpringLayout.EAST, scrollPane);
		springLayout.putConstraint(SpringLayout.SOUTH, graphPanel, -10, SpringLayout.NORTH, Cmnd_panel);
		springLayout.putConstraint(SpringLayout.EAST, graphPanel, -10, SpringLayout.EAST, mainFrame.getContentPane());
		
		
		//adding Tout text pane
		toutTextPane = new JTextPane();
		toutTextPane.setEditable(false);
		scrollPane.setViewportView(toutTextPane);
		mainFrame.getContentPane().add(graphPanel);
		
		// adding menu bar and items with action listener
		JMenuBar menuBar = new JMenuBar();
		mainFrame.setJMenuBar(menuBar);
		
		JMenu mnNewMenu = new JMenu("File");
		menuBar.add(mnNewMenu);
	
		JMenu mnNewMenu1 = new JMenu("Options");
		menuBar.add(mnNewMenu1);
		
		JMenuItem mnNewMenuItem1 = new JMenuItem("Save");
		mnNewMenu.add(mnNewMenuItem1);
		mnNewMenuItem1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(save.isEnabled()) {
					saveChart();
					saveFile();
				} else {
					printTextWin(" \n *** Cannot Save Data ***", 3, true);
				}
				
			}
		});
		
		JMenuItem mnNewMenu1Item1 = new JMenuItem("Exit");
		mnNewMenu1.add(mnNewMenu1Item1);
		mnNewMenu1Item1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SerialNetwork.disconnectPort();
				displayUpdateTimer.stop();
		        System.exit(0);
			}
		});
		
		// creating a blank chart with axis and labels to plot graph
		createChart();
		
		//Creating a timer for updating in real time
		displayUpdateTimer =  new Timer(1, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateDynamic();
				displayUpdateTimer.restart();
			}
		});
		displayUpdateTimer.start();
		Downl_Cnt = 0;
        Pb_NValues = 0;
        Pb_Ready = true;
	}

	// function to add available ports in drop down list
	public void addCommPorts() {
		String[] ports = SerialNetwork.getCommPorts() ;
		for (int p =0; p < ports.length; p++) {
			portList.addItem(ports[p]);
		}
	}
	
	//function for displaying readings in Readings panel
	private void displayReading(double temp) {
		tempField.setText(String.valueOf(temp));
	}
	
	//function to create chart, adding it to graph panel and setting the required parameters;  creating 3 different chart to plot 3 graphs
	private void createChart() {
		
		//creating third chart
		chart = new Chart2D();
		trace = new Trace2DLtd(numTraces);
		trace.setColor(Color.RED);
		IAxis axisX2 = chart.getAxisX();
	    axisX2.setPaintGrid(true);
	    axisX2.getAxisTitle().setTitle("Time (s)");
	    IAxis axisY2 = chart.getAxisY();
	    axisY2.setPaintGrid(true);
	    axisY2.getAxisTitle().setTitle("Temp Data");

		chart.addTrace(trace);
		trace.setName("Temp Data");
		
		graphPanel.setLayout(new BorderLayout(0, 0));
		graphPanel.add(chart);
		chart.setVisible(true);
		graphPanel.setVisible(true);
		graphPanel.repaint();		
	}
	
	//function to plot point at x,y on the chart; we have to pass value of x & y as parameter
	private void plotChart(int xvalue, double y2value) {	
		
		trace.addPoint(xvalue,y2value);
		if (Pb_NValues >= 999) {
//			xvalue = 0;
			Pb_NValues = 0;
			
		}
	}
	
	//function to clear Graph
	private void clearChart() {
		
		trace.removeAllPoints();
		trace.addPoint(0,0);
	
		save.setEnabled(false);
		//Pb_Ready = true;
		Start.setEnabled(true);
	}
	
	//function to save graph
	private void saveChart() {
		 Thread t = new Thread() {
	            public void run() {
	                // save the chart to a file
	                try {
	                    
	                    BufferedImage bi2 = chart.snapShot();
	                    ImageIO.write(bi2, "JPEG", new File("rValue.jpg"));
	                    
	                    JOptionPane.showMessageDialog(mainFrame, "Graph and Data Saved");
	                    printTextWin("\n Graph and Data Saved ", 1, true);
	                    System.out.println("\n Graph Saved  \n ");
	                    // other possible file formats are PNG and BMP
	                } catch (Exception ex) {
	                    System.err.println("Error saving Graph to File: "+ex.getMessage());
	                }
	            }
	        };
	        t.start();
	}
	
	//function to save readings in a file whose value is given in file string i.e. OutputFile
	private void saveFile() {
		Thread t = new Thread() {
            public void run() {
                // save the data to a file
                try {
                   Writer out = new OutputStreamWriter(new FileOutputStream(file));
					for (int k = 0; k < NROWS; k++) {
	                    out.write(String.format(Locale.ENGLISH, "%f %f %f \n",
	                    	Plot_Buffer[k][0], Plot_Buffer[k][1],Plot_Buffer[k][2]));
					}
					out.close();
				}
				catch (Exception ex) {
                    System.err.println("Error saving Data to File: "+ex.getMessage());
                }
            }
        };
        t.start();
	}
	


	//function for action on Connect command
	private void actionConnect() {
		boolean checkConnect = SerialNetwork.connectPort(portList.getSelectedItem().toString());
		if (checkConnect) {
			String c = "Connected to " + SerialNetwork.getConnectionName();
			clearChart();
			printTextWin(c,1,true);
			Disconnect.setEnabled(true);
			Connect.setEnabled(false);
			R.setEnabled(false);
			portList.setEnabled(false);
			Start.setEnabled(true);
			Pb_NValues = 0;
//			printTextWin("msg:- "+ SerialNetwork.ReadString(), 3, true);
		}
		else {
			String f = "Connection failed" ;
			printTextWin(f,3,true); 
			
		}
	}
	
	//function for action on disconnect command
	private void actionDisconnect() {
		String name = SerialNetwork.getConnectionName();
		SerialNetwork.disconnectPort();
		Pb_Ready = false;
		String d = "\n" + name + " Disconnected";
		printTextWin(d,1,true);
		Connect.setEnabled(true);
		Disconnect.setEnabled(false);
		clearGraph.setEnabled(true);
		Start.setEnabled(false);
		//stop.setEnabled(false);
		R.setEnabled(true);
		portList.setEnabled(true);             
	}
	
	//function for action on start command
	private void actionStart() {
		Start.setEnabled(false);
		printTextWin("Msg :- "+ SerialNetwork.ReadString(),3,true);
		clearChart();
		
		for(int i = 0 ; i < NROWS ; i++){
			for(int j = 0 ; j < NCOLS; j++){
			Plot_Buffer[i][j] =  0;
			}
		}

	}
		
		
	//function for action on Plot command
	private void actionPlotGraph() {
		for(int i=0; i<NROWS; i++) {
			plotChart(i,Plot_Buffer[i][0]);
		}
		save.setEnabled(true);
		//Pb_Ready = true;	
	}
	
	//function for action on clear graph command
	private void actionClearGraph() {
		clearChart();
		clearGraph.setEnabled(false);
	}

	//function to define the style of messages printed in the tout text pane
	private void printTextWin(String t, int tstyle, boolean newline) {
		try {
			Document doc = toutTextPane.getStyledDocument();
			StyleConstants.setItalic(TextSet, false);
            StyleConstants.setBold(TextSet, false);
            StyleConstants.setForeground(TextSet, Color.BLACK);
            switch (tstyle) {
            case 0:
                StyleConstants.setBold(TextSet, true);
                StyleConstants.setForeground(TextSet, Color.DARK_GRAY);
                break;
            case 1: StyleConstants.setForeground(TextSet, Color.BLUE);
                break;
            case 2: StyleConstants.setForeground(TextSet, Color.BLACK);
                break;
            case 3: StyleConstants.setForeground(TextSet, Color.RED);
            	break;
            case 4: StyleConstants.setForeground(TextSet, Color.GREEN);
            	break;
            default:
                doc.remove(0, doc.getLength());
            }
        if (tstyle >= 0) {
        	toutTextPane.setCharacterAttributes(TextSet, true);
        	if (newline) {
                doc.insertString(doc.getLength(), t+"\n", TextSet);            		
        	} else {
                doc.insertString(doc.getLength(), t, TextSet);
        		}
        	}	
		}
		catch(BadLocationException ex) {
            System.out.println(ex.toString());
		}
	}
	
	// function to define the action as per commands received in command field
	private void CommandHandler(String cmds) {
		String command = cmds, dev_name;
		if (command.equals("clc")) {
			toutTextPane.setText("");
		} else if (command.equals("clg")) {
			clearChart();
		} else if (command.equals("help")) {
			printTextWin("FPGA Control Help:", 1, true);
			printTextWin("    clc - clear text window", 1, true);	
			printTextWin("    connect - connect", 1, true);			
			printTextWin("    disconnect - disconnect", 1, true);
			printTextWin("    plot - plots the graph", 1, true);	
			printTextWin("    start - starts the sensor", 1, true);
			printTextWin("    saveGr - saves the graph", 1, true);
			printTextWin("    saveFile - saves the data to file", 1, true);
			printTextWin("    clearGr - clear chart window", 1, true);		
			printTextWin("    exit - exit", 1, true);	
		} else if (command.startsWith("connect")) {
			actionConnect();
		} else if (command.equals("disconnect")) {
			actionDisconnect();
		} else if (command.equals("exit")) {
			dev_name = SerialNetwork.getConnectionName();
			if (dev_name != null) {
				SerialNetwork.disconnectPort();
			}
			displayUpdateTimer.stop();
	        System.exit(0);
		} else if (command.equals("start")) {
			if(SerialNetwork.isConnected) {
				actionStart();
			}else {
				printTextWin("\n  *** Device not connected***", 3, true);
			}
		}else if (command.startsWith("plot")) {
				actionPlotGraph();
			}else if (command.startsWith("saveGr")) {
			if(save.isEnabled()) {
					saveChart();
			}else {
					printTextWin("\n  *** All Values not received***", 3, true);
				}
		}else if (command.startsWith("clearGr")) {
			if(clearGraph.isEnabled()) {
				actionClearGraph();
			}else {
				printTextWin("\n  *** Complete Graph not plotted***", 3, true);
			}
		}else if (command.startsWith("saveFile")) {
				if(save.isEnabled()) {
					saveFile();
				}else {
					printTextWin("\n  *** All Values not received***", 3, true);
				}
		}else if (command.startsWith("clc")) {
			toutTextPane.setText("");
		}else if (command.length() > 0) {
			printTextWin("\n   *** command???: \"" + command + "\"", 3, true);
		}
		else if ((SerialNetwork.ReadString() != null) && (SerialNetwork.ReadString() != " ")) {
			
				printTextWin("Msg :- "+SerialNetwork.ReadString() ,3,true);
			
		}
	}
	
	// function to define the action as per command received on serial communication
	public void updateDynamic() {
		
		byte[] rData ;
    	while ((rData = SerialNetwork.recvSerial()) != SerialNetwork.error) {
				System.out.println("["+ rData + "]");
			try {
				byte pType = rData[0];
				String S = SerialNetwork.ReadString();
			}
			catch  (NumberFormatException e) {
		        System.out.println(e.toString());
		    }
    	}
   	}

	private String S() {
		// TODO Auto-generated method stub
		return null;
	}

	//function to process the data when Data packet is received; we have to pass the received data as parameter
	private void processData(byte[] data) {
		if(data.length <= NCOLS) {
			if(DEBUG){
    		System.out.printf("processData: Data Insufficient");
			}
			return;
		}
				
		int a = 0x00FF & ((int)data[1]);
		int b = 0x00FF & ((int)data[2]);
		int temp1 = ((a<<8) | b);
		if (temp1 > 32767) {
			temp1 = temp1 - 65536;
		}

		double temp = (temp1*1.0)/100;
     	System.out.printf("temp Value: %f\n", temp);
		
		displayReading(temp);
		if(DEBUG){

		}
		if(Pb_Ready = true) {
			Plot_Buffer[Pb_NValues][0] = (double)temp;
			actionPlotGraph();
		}
		if(Pb_NValues >= NROWS-1) {
			printTextWin("\n Download finished.", 1, true);
    		Pb_Ready = false;    			
		}
		else {
			Pb_NValues++;
			}
	}
	
}
