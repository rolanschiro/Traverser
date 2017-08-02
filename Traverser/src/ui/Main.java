package ui;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Label;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.wb.swt.SWTResourceManager;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Point;
import org.openqa.selenium.chrome.ChromeDriver;

import ediProgram.ALCWebManager;
import ediProgram.EDI;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.custom.StyledText;

public class Main {

	protected Shell shlTraverser;
	private static StyledText systemOutput;
	private ProgressBar progressBar;
	private Text startDate;
	private Text endDate;
	private Combo rowsCombo;
	
	final static ArrayList <EDI> ediRepository = new ArrayList <EDI>();

	static String PATH;
	static File MATRIX;
	static File LOAD_MANAGERS;

	ArrayList<String> cancelledLoads = new ArrayList<String>();
	ArrayList<String> revisedLoads = new ArrayList<String>();
	ArrayList<String> originalLoads = new ArrayList<String>();
	ArrayList<String> incompleteEDIs = new ArrayList<String>();
	
	File config = new File("config.properties");
	File bin;
	File logs;

	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		
		try {
			Main window = new Main();
			window.open();
		
		} catch (Exception e) {
			e.printStackTrace();
		}
 	}

	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		try {
			createContents();
		} catch (Throwable e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String exceptionAsString = sw.toString();
			
			updateSystemOutput(exceptionAsString);
			saveLog("(errorlog)");
		}
		shlTraverser.open();
		shlTraverser.layout();
		
		while (!shlTraverser.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		InputStream input = null;
		OutputStream output = null;
		
		Properties prop = new Properties();
		try {
			config.createNewFile();
			input = new FileInputStream("config.properties");
			prop.load(input);
		} catch (IOException e1) {
		}
		
		if(prop.getProperty("path") == null){
			try {
				PATH = new File(".").getCanonicalPath();
				output = new FileOutputStream("config.properties");
				config.createNewFile();
				prop.setProperty("path", PATH);
				prop.store(output, null);
			} catch (IOException e3) {
				e3.printStackTrace();
			}
		}
		PATH = prop.getProperty("path");
		
		bin = new File(PATH + "/bin");
		bin.mkdir();

		logs = new File(PATH + "/logs");
		logs.mkdir();
		
		System.setProperty("webdriver.chrome.driver", PATH + "/chromedriver.exe/");
		ChromeDriver driver = new ChromeDriver();
		driver.manage().window().setPosition(new Point(0, -2000));

		ALCWebManager ediScraper = new ALCWebManager(driver);

		
		shlTraverser = new Shell();
		shlTraverser.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent arg0) {
				driver.quit();
			}
		});
		shlTraverser.setSize(256, 453);
		shlTraverser.setText("Traverser");
		shlTraverser.setLayout(null);
		
		systemOutput = new StyledText(shlTraverser, SWT.BORDER | SWT.READ_ONLY | SWT.WRAP | SWT.V_SCROLL);
		systemOutput.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
		        systemOutput.setTopIndex(systemOutput.getLineCount() - 1);
			}
		});
		systemOutput.setBlockSelection(true);
		systemOutput.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		systemOutput.setEditable(false);
		systemOutput.setBounds(10, 228, 218, 161);
		
		
		Composite inputComposite = new Composite(shlTraverser, SWT.NONE);
		inputComposite.setBounds(26, 37, 202, 97);
		
		Label lblStartDate = new Label(inputComposite, SWT.NONE);
		lblStartDate.setBounds(0, 3, 55, 15);
		lblStartDate.setText("Start Date:");
		
		Label lblEndDate = new Label(inputComposite, SWT.NONE);
		lblEndDate.setBounds(0, 30, 55, 15);
		lblEndDate.setText("End Date:");
		
		Label lblShipper = new Label(inputComposite, SWT.NONE);
		lblShipper.setBounds(0, 82, 55, 15);
		lblShipper.setText("Shipper:");

		startDate = new Text(inputComposite, SWT.BORDER);
		startDate.setBounds(61, 0, 70, 21);

		endDate = new Text(inputComposite, SWT.BORDER);
		endDate.setBounds(61, 27, 70, 21);
		
		Button checkJDA = new Button(inputComposite, SWT.CHECK);
		checkJDA.setSelection(true);
		checkJDA.setBounds(61, 81, 41, 16);
		checkJDA.setText("JDA");
		
		Button checkJDM = new Button(inputComposite, SWT.CHECK);
		checkJDM.setSelection(true);
		checkJDM.setBounds(108, 81, 44, 16);
		checkJDM.setText("JDM");
		
		Button checkCOS = new Button(inputComposite, SWT.CHECK);
		checkCOS.setSelection(true);
		checkCOS.setBounds(158, 81, 44, 16);
		checkCOS.setText("COS");
		
		Label lblRows = new Label(inputComposite, SWT.NONE);
		lblRows.setBounds(0, 56, 41, 15);
		lblRows.setText("Rows:");
		
		Label lblMmddyy = new Label(inputComposite, SWT.NONE);
		lblMmddyy.setLocation(137, 3);
		lblMmddyy.setSize(60, 15);
		lblMmddyy.setText("MM/dd/YY");
		
		Label label = new Label(inputComposite, SWT.NONE);
		label.setLocation(137, 30);
		label.setSize(60, 15);
		label.setText("MM/dd/YY");
		
		rowsCombo = new Combo(inputComposite, SWT.READ_ONLY);
		rowsCombo.setItems(new String[] {"5", "10", "15", "20", "30", "50", "100"});
		rowsCombo.setBounds(61, 52, 70, 23);
		rowsCombo.select(6);
	
		Menu menu = new Menu(shlTraverser, SWT.BAR);
		shlTraverser.setMenuBar(menu);
		
		MenuItem mntmFile = new MenuItem(menu, SWT.CASCADE);
		mntmFile.setText("File");
		
		Menu menu_1 = new Menu(mntmFile);
		mntmFile.setMenu(menu_1);
		
		MenuItem mntmUpload = new MenuItem(menu_1, SWT.NONE);
		mntmUpload.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileUpload window = new FileUpload();
				window.open();
			}
		});
		mntmUpload.setText("Upload...");
		
		MenuItem mntmSettings = new MenuItem(menu, SWT.CASCADE);
		mntmSettings.setText("Edit");
		
		Menu menu_2 = new Menu(mntmSettings);
		mntmSettings.setMenu(menu_2);
		
		MenuItem mntmSetPathVariables = new MenuItem(menu_2, SWT.NONE);
		mntmSetPathVariables.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Settings window = new Settings();
				window.open();
			}
		});
		mntmSetPathVariables.setText("Settings...");
		
		Composite buttonComposite = new Composite(shlTraverser, SWT.NONE);
		buttonComposite.setBounds(10, 142, 218, 57);
		
		//full EFJ creation button, includes ALX output
		Button createEFJsButton = new Button(buttonComposite, SWT.NONE);
		createEFJsButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				systemOutput.setText("");
				updateSystemOutput("Creating EFJs...");

				InputStream input = null;
				try {
					input = new FileInputStream("config.properties");
					prop.load(input);
				} catch (IOException e2) {
					e2.printStackTrace();
				}

				PATH = prop.getProperty("path");
				MATRIX = new File(PATH + "/matrix.csv");
				LOAD_MANAGERS = new File(PATH + "/loadmanagers.csv");
				
				String end = endDate.getText();		
				String start = startDate.getText();
				String rows = rowsCombo.getText();
				
				StringBuilder c = new StringBuilder(); 
				if(checkJDA.getSelection())
					c.append(checkJDA.getText() + ";");
				if(checkJDM.getSelection())
					c.append(checkJDM.getText() + ";"); 
				if(checkCOS.getSelection())
					c.append(checkCOS.getText()); 
				String cust = c.toString();
				
				boolean check = true;
				if(prop.getProperty("username") == null){
					updateSystemOutput("[ERROR]: No username!");
					check = false;
				}
				if(prop.getProperty("password") == null){
					updateSystemOutput("[ERROR]: No password!");
					check = false;
				}
				if(prop.getProperty("path") == null){
					updateSystemOutput("[ERROR]: No path specified!");
					check = false;
				}
				
				Path results = Paths.get(PATH, "results.csv");
				
				if(check == false){
					updateSystemOutput("Stopping Traverser...");
					return;
				}
				
				Thread s = new Thread(){
					public void run(){
						updateProgressBar(0);
						ediScraper.EDIlogIn(prop.getProperty("username"), prop.getProperty("password"));
						updateProgressBar(5);
						ediScraper.setEDISearchSettings(cust, "N", start, end, rows);
						ediScraper.search();
						
						for(int i = 0; true; i++){
							try{
								ediScraper.scrapeTo(bin.getAbsolutePath(), i);
							} catch(NoSuchElementException e1){
								updateSystemOutput("File Creation Complete!");
								break;
							}
						}
						updateProgressBar(25);

						if(bin.listFiles().length == 0){
							updateSystemOutput("File bin is empty!");
						}
						
						int interval = 40/(bin.listFiles().length + 1);
						for(final File f : bin.listFiles()){
							EDI edi = new EDI();
							//file to object conversion, delete file
							try {
								edi = convertFileToEDI(f);
								updateSystemOutput("Successfully converted file "+ f.getName()+ " to EDI object.");
								f.delete();
							} catch (Exception e1) {
								updateSystemOutput("[ERROR]: Could not open file "+ f.getName()+ " - continuing to next file.");
								incompleteEDIs.add(f.getName());
								e1.printStackTrace();
								continue;
								}

							//parsing information from EDI's table variable
							try {
								edi.parseAsJDA();
							} catch (Exception e1) {
								updateSystemOutput("[ERROR]: Could not parse EDI "+ f.getName()+ "- continuing to next EDI.");
								incompleteEDIs.add(edi.getShipID());
								e1.printStackTrace();
								continue;
							}
							
							if(edi.getStatus().equals("CANCELLED")){
								cancelledLoads.add(edi.getShipID());
							}
							if(edi.getStatus().equals("REVISED")){
								revisedLoads.add(edi.getShipID());
							}
							if(edi.getStatus().equals("ORIGINAL")){
								originalLoads.add(edi.getShipID());
							}
							
							//checks EDI information is valid on ALX, if true, completes the EDI file clientside by retrieving matrix information
							updateSystemOutput("Checking shipment " + edi.getShipID() + " for problems...\n");
							
							if(ediScraper.checkEDIdata(edi)){
								updateSystemOutput(ediScraper.getLog());
								try {
									completeEDIfile(edi);
									updateSystemOutput(ediScraper.getLog());
									ediRepository.add(edi);
									updateSystemOutput("Gathered rate, office, and manager for shipment " + f.getName() + ".");
								} catch (Exception e1) {
									updateSystemOutput("[ERROR]: Cannot complete file " + f.getName() + ".");
									incompleteEDIs.add(edi.getShipID());
									e1.printStackTrace();
									continue;
								}
							}
							else{
								updateSystemOutput("[ERROR]: EDI " + f.getName() + " was checked and returned false." + ediScraper.getLog() + "\n");
								incompleteEDIs.add(edi.getShipID());
								continue;
							}
							addToProgressBar(interval);
							updateSystemOutput(edi.toString());
						}
						updateProgressBar(70);
						
						ediScraper.setEDISearchSettings("", "N", start, end, rows);
						ediScraper.search();
					
						ediScraper.outputToALX(ediRepository);
						updateSystemOutput(ediScraper.getLog());
						updateProgressBar(85);

						ediScraper.findLoadIDs(ediRepository, cust);
						updateSystemOutput(ediScraper.getLog());
						updateProgressBar(95);

						ArrayList<String> ediResults = new ArrayList<String>();
						ediResults.add("PO,EFJ,OFFICE,REVISED LOAD,SHIP ID");
						
						for(EDI edi : ediRepository){
							ediResults.add(edi.getPONumber() + "," + edi.getLoadNumber() + "," + edi.getOfficeName() + "," + edi.getStatus() + "," + edi.getShipID());
							updateSystemOutput(edi.toString() + "\n");
						}
						try {
							Files.write(results, ediResults, StandardCharsets.UTF_8);
						} catch (IOException e1) {
							updateSystemOutput("[ERROR]: Could not write to 'results' file.");
							e1.printStackTrace();
						}

						updateSystemOutput("ORIGINAL LOADS:");
						for(String str : originalLoads){
							updateSystemOutput("	" + (originalLoads.indexOf(str) + 1) + ". "+ str);
						}
						originalLoads.clear();

						updateSystemOutput("REVISED LOADS:");
						for(String str : revisedLoads){
							updateSystemOutput("	" + (revisedLoads.indexOf(str) + 1) + ". "+ str);
						}
						revisedLoads.clear();

						updateSystemOutput("CANCELLED LOADS:");
						for(String str : cancelledLoads){
							updateSystemOutput("	" + (cancelledLoads.indexOf(str) + 1) + ". "+ str);
						}
						cancelledLoads.clear();

						updateSystemOutput("COMPLETED EDIS:");
						for(EDI edi : ediRepository){
							updateSystemOutput("	" + (ediRepository.indexOf(edi) + 1) + ". "+ edi.getShipID());
						}
						ediRepository.clear();

						updateSystemOutput("INCOMPLETE EDIS:");
						for(String str : incompleteEDIs){
							updateSystemOutput("	" + (incompleteEDIs.indexOf(str) + 1) + ". "+ str);
						}
						incompleteEDIs.clear();

						updateProgressBar(100);
						ediResults.remove(0);
						saveLog();
						createEFJsButton.getDisplay().syncExec(new Runnable(){
						      public void run() {
						    	  Results window = new Results(ediResults);
						    	  window.open();
						      }
						});
						ediResults.clear();
					}
				};
				s.start();
			}
		});
		createEFJsButton.setBounds(0, -1, 94, 57);
		createEFJsButton.setText("Create EFJs");
		
		//does not output to ALX, only parses initial scripts
		Button safeCheckButton = new Button(buttonComposite, SWT.NONE);
		safeCheckButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				systemOutput.setText("");
				updateSystemOutput("Running SafeCheck...");
				InputStream input = null;
				try {
					input = new FileInputStream("config.properties");
					prop.load(input);
				} catch (IOException e2) {
					e2.printStackTrace();
				}

				PATH = prop.getProperty("path");
				MATRIX = new File(PATH + "/matrix.csv");
				LOAD_MANAGERS = new File(PATH + "/loadmanagers.csv");
				
				String end = endDate.getText();		
				String start = startDate.getText();
				String rows = rowsCombo.getText();

				StringBuilder c = new StringBuilder(); 
				if(checkJDA.getSelection())
					c.append(checkJDA.getText() + ";");
				if(checkJDM.getSelection())
					c.append(checkJDM.getText() + ";"); 
				if(checkCOS.getSelection())
					c.append(checkCOS.getText()); 
				String cust = c.toString();
				
				boolean check = true;
				if(prop.getProperty("username")== null){
					updateSystemOutput("[ERROR]: No username!");
					check = false;
				}
				if(prop.getProperty("password") == null){
					updateSystemOutput("[ERROR]: No password!");
					check = false;
				}
				if(prop.getProperty("path") == null){
					updateSystemOutput("[ERROR]: No path specified!");
					check = false;
				}

				Thread s = new Thread(){
					public void run(){
						
						updateProgressBar(0);
						ediScraper.EDIlogIn(prop.getProperty("username"), prop.getProperty("password"));
						updateProgressBar(5);
						ediScraper.setEDISearchSettings(cust , "N", start, end, rows);
						ediScraper.search();
						updateProgressBar(15);
						
						for(int i = 0; true; i++){
							try{
								ediScraper.scrapeTo(bin.getAbsolutePath(), i);
								updateSystemOutput(ediScraper.getLog());
							} catch(NoSuchElementException e1){
								updateSystemOutput(ediScraper.getLog());
								e1.printStackTrace();
								updateSystemOutput("File Creation Complete!");
								break;
							}
						}
						updateProgressBar(50);

						if(bin.listFiles().length == 0){
							updateSystemOutput("File bin is empty!");
						}
						int interval = 30/(bin.listFiles().length + 1);
						for(final File f : bin.listFiles()){
							EDI edi = new EDI();
							//file to object conversion, delete file
							try {
								edi = convertFileToEDI(f);
								updateSystemOutput("Successfully converted file "+ f.getName()+ " to EDI object.");
								f.delete();
							} catch (Exception e1) {
								updateSystemOutput("[ERROR]: Could not open file "+ f.getName()+ " - continuing to next file.");
								incompleteEDIs.add(f.getName());
								e1.printStackTrace();
								continue;
							}

							//parsing information from EDI's table variable
							try {
								edi.parseAsJDA();
							} catch (Exception e1) {
								updateSystemOutput("[ERROR]: Could not parse EDI "+ f.getName()+ "- continuing to next EDI.");
								incompleteEDIs.add(edi.getShipID());
								e1.printStackTrace();
								continue;
							}
							
							if(edi.getStatus().equals("CANCELLED")){
								cancelledLoads.add(edi.getShipID());
							}
							if(edi.getStatus().equals("REVISED")){
								revisedLoads.add(edi.getShipID());
							}
							if(edi.getStatus().equals("ORIGINAL")){
								originalLoads.add(edi.getShipID());
							}
							
							//checks EDI information is valid on ALX, if true, completes the EDI file clientside by retrieving matrix information
							updateSystemOutput("Checking shipment " + edi.getShipID() + " for problems...");
							
							if(ediScraper.safeCheckEDIdata(edi)){
								updateSystemOutput(ediScraper.getLog());
								try {
									completeEDIfile(edi);
									ediRepository.add(edi);
								} catch (Exception e1) {
									updateSystemOutput("[ERROR]: Cannot complete file " + f.getName() + "." + ediScraper.getLog() + edi.toString() + "\n");
									incompleteEDIs.add(edi.getShipID());
									e1.printStackTrace();
									continue;
								}
								updateSystemOutput(ediScraper.getLog() + "Gathered rate, office, and manager for shipment " + f.getName() + ".");
							}
							else{
								updateSystemOutput("[ERROR]: EDI " + f.getName() + " was checked and returned false. See below:" + ediScraper.getLog() + "\n");
								incompleteEDIs.add(edi.getShipID());
								continue;
							}
							addToProgressBar(interval);
							updateSystemOutput(ediScraper.getLog() + edi.toString() + "\n");
						}
						
						updateSystemOutput("COMPLETE!");
						updateProgressBar(100);
						saveLog();
					}
				};
				
				if(check == false){
					updateSystemOutput("Stopping Traverser...");
				}
				else
					s.start();
			}
		});
		safeCheckButton.setBounds(97, -1, 70, 57);
		safeCheckButton.setText("Safe Check");
		
		Button stopButton = new Button(buttonComposite, SWT.NONE);
		stopButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateSystemOutput(ediScraper.getLog());
				ArrayList <String> log = new ArrayList<String>();
				for(String str : systemOutput.getText().split("\n")){
					log.add(str);
				}
				LocalDateTime dateTime = LocalDateTime.now();
				DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
				String date = (dateTime.format(format));

				Path file = Paths.get(logs.getAbsolutePath(), date);
				try {
					Files.write(file, log, StandardCharsets.UTF_8);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				updateSystemOutput("Log Stored!");
				shlTraverser.dispose();
			}
		});
		stopButton.setBounds(173, -1, 45, 57);
		stopButton.setText("STOP");
		
		Label label_1 = new Label(buttonComposite, SWT.SEPARATOR | SWT.VERTICAL);
		label_1.setBounds(169, -4, 2, 64);
		
		progressBar = new ProgressBar(shlTraverser, SWT.NONE);
		progressBar.setBounds(10, 205, 218, 17);
		
		Label lblEdi = new Label(shlTraverser, SWT.NONE);
		lblEdi.setFont(SWTResourceManager.getFont("Segoe UI Symbol", 12, SWT.BOLD));
		lblEdi.setBounds(85, 10, 68, 21);
		lblEdi.setText("Auto EDI");
	}
	
	public static EDI convertFileToEDI(File edi) throws IOException{
		ArrayList<String> table = new ArrayList<String>();
		List <String> rows = Files.readAllLines(Paths.get(edi.getPath()));
		for(String line : rows){
			table.add(line);
		}
		EDI e = new EDI(table);
		return e;
	}

	public static void completeEDIfile(EDI e) throws IOException{
		
		boolean foo = false;
		//checks for 5 digit zipcodes first

		for(String line : Files.readAllLines(MATRIX.toPath())){
			if(line.contains(e.getOriginZip())){
				String[] l = line.split(",");
				if(l[2].equals(e.getOriginZip()))
					if(l[5].equals(e.getDepot()))
						if(l[7].equals(e.getEquipment())){
							e.setOffice(l[8]);
							e.setRate(l[9]);
							foo = true;
							break;
						}
			}
		}
		
		//if it cannot find exact 5 digit zip, trims to 3 digit zip
		if(foo == false){
			for(String line : Files.readAllLines(MATRIX.toPath())){
				if(line.contains(e.trimZip(e.getOriginZip()))){
					String[] l = line.split(",");
					if(l[2].equals(e.trimZip(e.getOriginZip())))
						if(l[5].equals(e.getDepot()))
							if(l[7].equals(e.getEquipment())){
								e.setOffice(l[8]);
								e.setRate(l[9]);
								foo = true;
								break;
							}
				}
			}
		}
		
		if(foo == false)
			updateSystemOutput("[ERROR]: Could not find correct lane in matrix.");
		
		
		//gather load manager and office name
		for(String line : Files.readAllLines(LOAD_MANAGERS.toPath())){
			if (line.contains(e.getOffice())){
				String[] l = line.split(",");
				if(l[0].equals(e.getOffice())){
					e.setOffice(l[1]);
					e.setOfficeName(l[2]);
					e.setLoadManager(l[3]);
					break;
				}
			}
		}
		
		if(foo == false){
			updateSystemOutput("[ERROR]: Could not find associated load manager/office in load managers.");
		}

	}
	
	private static void updateSystemOutput(String line){
		systemOutput.getDisplay().syncExec(new Runnable() {
		      @Override
		      public void run() {
		    	  if(!systemOutput.isDisposed())
		    		  systemOutput.append(line + "\n");
		      }
		});
	}
	
	private void updateProgressBar(int state){
		progressBar.getDisplay().asyncExec(new Runnable() {
		      @Override
		      public void run() {
		    	  if(!progressBar.isDisposed())
		    		  progressBar.setSelection(state);
		      }
		});
	}
	
	private void addToProgressBar(int interval){
		progressBar.getDisplay().asyncExec(new Runnable() {
		      @Override
		      public void run() {
		    	  if(!progressBar.isDisposed())
		    		  progressBar.setSelection(progressBar.getSelection() + interval);
		      }
		});
	}

	//saves a copy of the system output text into a file
	private void saveLog(){
		systemOutput.getDisplay().syncExec(new Runnable(){
			public void run(){
				ArrayList <String> log = new ArrayList<String>();
				for(String str : systemOutput.getText().split("\n")){
					log.add(str);
				}
				LocalDateTime dateTime = LocalDateTime.now();
				DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
				String date = (dateTime.format(format));

				Path file = Paths.get(logs.getAbsolutePath(), date);
				try {
					Files.write(file, log, StandardCharsets.UTF_8);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});

	}
	
	private void saveLog(String titleAppend){
		systemOutput.getDisplay().syncExec(new Runnable(){
			public void run(){
				ArrayList <String> log = new ArrayList<String>();
				for(String str : systemOutput.getText().split("\n")){
					log.add(str);
				}
				LocalDateTime dateTime = LocalDateTime.now();
				DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss" + titleAppend);
				String date = (dateTime.format(format));

				Path file = Paths.get(logs.getAbsolutePath(), date);
				try {
					Files.write(file, log, StandardCharsets.UTF_8);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});

	}

}
