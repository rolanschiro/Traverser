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

import ediProgram.*;

import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;

public class Main {

	protected Shell shlTraverser;
	private static StyledText systemOutput;
	private ProgressBar progressBar;
	private Text startDate;
	private Text endDate;
	private Combo rowsCombo;
	
	static ArrayList <EDI> ediRepository = new ArrayList <EDI>();
	static ArrayList<EDI> incompleteEDIs = new ArrayList<EDI>();


	static String PATH;
	static File MATRIX;
	static File LOAD_MANAGERS;

	ArrayList<String> cancelledLoads = new ArrayList<String>();
	ArrayList<String> revisedLoads = new ArrayList<String>();
	ArrayList<String> originalLoads = new ArrayList<String>();
	
	File config = new File("config.properties");
	File auto_edi, bin, logs; 
	File costco, alx_input, costco_input;

	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		
		Main window = new Main();
		window.open();
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
			
			System.out.println(exceptionAsString);
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
		
		//path variables
		PATH = prop.getProperty("path");
		
		auto_edi = new File(PATH + "/autoEdi");
		auto_edi.mkdir();
		
		bin = new File(PATH + "/autoEdi/bin");
		bin.mkdir();

		logs = new File(PATH + "/autoEdi/logs");
		logs.mkdir();

		costco = new File(PATH + "/costco");
		costco.mkdir();

		costco_input = new File(PATH + "/costco/costcoInput");
		costco_input.mkdir();
		
		alx_input = new File(PATH + "/costco/alxInput");
		alx_input.mkdir();

		System.setProperty("webdriver.chrome.driver", PATH + "/chromedriver.exe/");
		ChromeDriver driver = new ChromeDriver();
//		driver.manage().window().setPosition(new Point(0, -2000));

		ALCWebManager webManager = new ALCWebManager(driver);

		
		shlTraverser = new Shell();
		shlTraverser.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent arg0) {
				driver.quit();
			}
		});
		shlTraverser.setSize(343, 489);
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
		systemOutput.setBounds(10, 260, 307, 161);
		LocalDateTime dateTime = LocalDateTime.now();
		DateTimeFormatter format = DateTimeFormatter.ofPattern("MM/dd/yy");
		String date = (dateTime.format(format));
	
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
		
		progressBar = new ProgressBar(shlTraverser, SWT.NONE);
		progressBar.setBounds(10, 237, 307, 17);
		
		CTabFolder tabFolder = new CTabFolder(shlTraverser, SWT.BORDER);
		tabFolder.setBounds(10, 0, 307, 231);
		tabFolder.setSelectionBackground(Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));
		
		CTabItem tabEDI = new CTabItem(tabFolder, SWT.NONE);
		tabEDI.setText("Auto EDI");
		
		
		Composite inputComposite = new Composite(tabFolder, SWT.NONE);
		tabEDI.setControl(inputComposite);
		
		Label lblStartDate = new Label(inputComposite, SWT.NONE);
		lblStartDate.setBounds(57, 40, 55, 15);
		lblStartDate.setText("Start Date:");
		
		Label lblEndDate = new Label(inputComposite, SWT.NONE);
		lblEndDate.setBounds(57, 67, 55, 15);
		lblEndDate.setText("End Date:");
		
		Label lblShipper = new Label(inputComposite, SWT.NONE);
		lblShipper.setBounds(57, 119, 55, 15);
		lblShipper.setText("Shipper:");
		
		startDate = new Text(inputComposite, SWT.BORDER);
		startDate.setBounds(118, 37, 70, 21);
		
		endDate = new Text(inputComposite, SWT.BORDER);
		endDate.setBounds(118, 64, 70, 21);
		endDate.setText(date);
				
		Button checkJDA = new Button(inputComposite, SWT.CHECK);
		checkJDA.setSelection(true);
		checkJDA.setBounds(118, 118, 41, 16);
		checkJDA.setText("JDA");
		
		Button checkJDM = new Button(inputComposite, SWT.CHECK);
		checkJDM.setSelection(true);
		checkJDM.setBounds(165, 118, 44, 16);
		checkJDM.setText("JDM");
		
		Button checkCOS = new Button(inputComposite, SWT.CHECK);
		checkCOS.setSelection(true);
		checkCOS.setBounds(215, 118, 44, 16);
		checkCOS.setText("COS");
		
		Label lblRows = new Label(inputComposite, SWT.NONE);
		lblRows.setBounds(57, 93, 41, 15);
		lblRows.setText("Rows:");
		
		Label lblMmddyy = new Label(inputComposite, SWT.NONE);
		lblMmddyy.setLocation(194, 40);
		lblMmddyy.setSize(60, 15);
		lblMmddyy.setText("MM/dd/YY");
		
		Label label = new Label(inputComposite, SWT.NONE);
		label.setLocation(194, 67);
		label.setSize(60, 15);
		label.setText("MM/dd/YY");
		
		rowsCombo = new Combo(inputComposite, SWT.READ_ONLY);
		rowsCombo.setItems(new String[] {"5", "10", "15", "20", "30", "50", "100"});
		rowsCombo.setBounds(118, 89, 70, 23);
		rowsCombo.select(6);
		
		Label lblEdi = new Label(inputComposite, SWT.NONE);
		lblEdi.setLocation(116, 10);
		lblEdi.setSize(68, 21);
		lblEdi.setFont(SWTResourceManager.getFont("Segoe UI Symbol", 12, SWT.BOLD));
		lblEdi.setText("Auto EDI");
		
		Composite buttonComposite = new Composite(inputComposite, SWT.NONE);
		buttonComposite.setLocation(42, 140);
		buttonComposite.setSize(218, 57);
		
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
				if(prop.getProperty("alxUsername") == null){
					updateSystemOutput("[ERROR]: No username!");
					check = false;
				}
				if(prop.getProperty("alxPassword") == null){
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
						webManager.EDIlogIn(prop.getProperty("alxUsername"), prop.getProperty("alxPassword"));
						updateProgressBar(5);
						webManager.setEDISearchSettings(cust, "N", start, end, rows);
						webManager.search();
						
						for(int i = 0; true; i++){
							try{
								webManager.scrapeTo(bin.getAbsolutePath(), i);
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
								edi.setShipID(f.getName());
								err("[ERROR]: Could not open file "+ f.getName()+ " - continuing to next file.", edi);
								incompleteEDIs.add(edi);
								e1.printStackTrace();
								continue;
								}

							//parsing information from EDI's table variable
							try {
								edi.parseAsJDA();
							} catch (Exception e1) {
								err("[ERROR]: Could not parse EDI "+ f.getName()+ "- continuing to next EDI.", edi);
								incompleteEDIs.add(edi);
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
							
							if(webManager.checkEDIdata(edi)){
								updateSystemOutput(webManager.getTempLog());
								//if it is a cancelled load, skip completing the file entirely, but still add to completed EDI repo.
								if(edi.getStatus() == "CANCELLED"){
									ediRepository.add(edi);
									updateSystemOutput(webManager.getTempLog());
								}
								else{	
									try {
										completeEDIfile(edi);
										ediRepository.add(edi);
										updateSystemOutput(webManager.getTempLog());
										updateSystemOutput("Gathered rate, office, and manager for shipment " + f.getName() + ".");
									} catch (Exception e1) {
										err("[ERROR]: Cannot complete file " + f.getName() + ".", edi);
										incompleteEDIs.add(edi);
										e1.printStackTrace();
										continue;
									}
								}
							}
							else{
								err("[ERROR]: EDI " + f.getName() + " was checked and returned false. See below: \n" + webManager.getTempLog() + "\n", edi);
								incompleteEDIs.add(edi);
								continue;
							}
							addToProgressBar(interval);
						}
						updateProgressBar(70);
						
						webManager.setEDISearchSettings("", "N", start, end, rows);
						webManager.search();
					
						webManager.outputToALX(ediRepository);
						updateSystemOutput(webManager.getTempLog());
						updateProgressBar(85);

						webManager.findLoadIDs(ediRepository, cust);
						updateSystemOutput(webManager.getTempLog());
						updateProgressBar(95);

						ArrayList<String> ediResults = new ArrayList<String>();
						ediResults.add("PO,EFJ,OFFICE,REVISED LOAD,SHIP ID");
						
						for(EDI edi : ediRepository){
							ediResults.add(edi.getPONumber() + "," + edi.getLoadNumber() + "," + edi.getOfficeName() + "," + edi.getStatus() + "," + edi.getShipID());
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
						for(EDI edi : incompleteEDIs){
							updateSystemOutput("	" + (incompleteEDIs.indexOf(edi) + 1) + ". "+ edi.getShipID());
							updateSystemOutput(edi.toString());
						}
						incompleteEDIs.clear();

						updateProgressBar(100);
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
				if(prop.getProperty("alxUsername")== null){
					updateSystemOutput("[ERROR]: No username!");
					check = false;
				}
				if(prop.getProperty("alxPassword") == null){
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
						webManager.EDIlogIn(prop.getProperty("alxUsername"), prop.getProperty("alxPassword"));
						updateProgressBar(5);
						webManager.setEDISearchSettings(cust , "N", start, end, rows);
						webManager.search();
						updateProgressBar(15);
						
						for(int i = 0; true; i++){
							try{
								webManager.scrapeTo(bin.getAbsolutePath(), i);
								updateSystemOutput(webManager.getTempLog());
							} catch(NoSuchElementException e1){
								updateSystemOutput(webManager.getTempLog());
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
								edi.setShipID(f.getName());
								err("[ERROR]: Could not open file "+ f.getName()+ " - continuing to next file.", edi);
								incompleteEDIs.add(edi);
								e1.printStackTrace();
								continue;
								}

							//parsing information from EDI's table variable
							try {
								edi.parseAsJDA();
							} catch (Exception e1) {
								err("[ERROR]: Could not parse EDI "+ f.getName()+ "- continuing to next EDI.", edi);
								incompleteEDIs.add(edi);
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
							
							if(webManager.safeCheckEDIdata(edi)){
								updateSystemOutput(webManager.getTempLog());
								//if it is a cancelled load, skip completing the file entirely, but still add to completed EDI repo.
								if(edi.getStatus() == "CANCELLED"){
									ediRepository.add(edi);
									updateSystemOutput(webManager.getTempLog());
								}
								else{	
									try {
										completeEDIfile(edi);
										ediRepository.add(edi);
										updateSystemOutput(webManager.getTempLog());
										updateSystemOutput("Gathered rate, office, and manager for shipment " + f.getName() + ".");
									} catch (Exception e1) {
										err("[ERROR]: Cannot complete file " + f.getName() + ".", edi);
										incompleteEDIs.add(edi);
										e1.printStackTrace();
										continue;
									}
								}
							}
							else{
								err("[ERROR]: EDI " + f.getName() + " was checked and returned false. See below: \n" + webManager.getTempLog() + "\n", edi);
								incompleteEDIs.add(edi);
								continue;
							}
							addToProgressBar(interval);
							updateSystemOutput(webManager.getTempLog() + edi.toString() + "\n");
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
				updateSystemOutput(webManager.getTempLog());
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
		
		CTabItem tabCostco = new CTabItem(tabFolder, SWT.NONE);
		tabCostco.setText("Costco");
		
		Composite composite = new Composite(tabFolder, SWT.NONE);
		tabCostco.setControl(composite);
		
		Label lblCostco = new Label(composite, SWT.NONE);
		lblCostco.setAlignment(SWT.CENTER);
		lblCostco.setText("Costco");
		lblCostco.setFont(SWTResourceManager.getFont("Segoe UI Symbol", 12, SWT.BOLD));
		lblCostco.setBounds(113, 10, 68, 21);
		
		Button btnRun = new Button(composite, SWT.NONE);
		btnRun.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				InputStream input = null;
				try {
					input = new FileInputStream("config.properties");
					prop.load(input);
				} catch (IOException e2) {
					e2.printStackTrace();
				}

				Thread test = new Thread(){
						public void run(){
//							webManager.shippersLogIn(prop.getProperty("alxUsername"), prop.getProperty("alxPassword"));
//							ArrayList<String> data = webManager.aggregateData(costco.getAbsolutePath());
							
							ArrayList<Payment> PAYMENTS = parsePayments();
							updateProgressBar(10);
							ArrayList<Invoice> INVOICES = parseInvoices();
							ArrayList<String> results = new ArrayList<String>();
							results.add("Office,File #,Load ID,Billed Amount,ADJ,Paid Amount,Balance Due,Days Old,Shipper #,Status");
							updateProgressBar(20);
								
							for(Invoice invoice:INVOICES){
								String invLoadNum = invoice.getLOAD_ID();
								double paymentSum = 0;
								for(Payment payment : PAYMENTS){

									if(invLoadNum.equals(payment.getLOAD_ID())){
										paymentSum += payment.getPAID_AMOUNT();
										
										invoice.setPAID_AMOUNT(Math.round(paymentSum));
										
										double balanceDue = Math.round((invoice.getBALANCE_DUE() - paymentSum));
										invoice.setBALANCE_DUE(balanceDue);
										
										invoice.setSTATUS("PENDING");
									}
								}
								if(invoice.getBALANCE_DUE() > 0){
									if(invoice.getPAID_AMOUNT() > 0)									
										invoice.setSTATUS("SHORT-PAID");
									results.add(invoice.toString());
								}
								
								if(INVOICES.indexOf(invoice) == INVOICES.size()/25){
									addToProgressBar(3);
								}
							}
							
							updateSystemOutput("COSTCO PAYMENTS = " + PAYMENTS.size());
							updateSystemOutput("ALX INVOICES = " + INVOICES.size());
							updateSystemOutput("\nTOTAL OCCURENCES = " + results.size());
							
							Path costco_results = Paths.get(costco.getAbsolutePath(), "results.csv");

							try {
								Files.write(costco_results, results, StandardCharsets.UTF_8);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
							updateSystemOutput("\nComplete!");

							updateProgressBar(100);
							
							createEFJsButton.getDisplay().syncExec(new Runnable(){
							      public void run() {
							    	  Results window = new Results(results);
							    	  window.open();
							      }
							});

						}
				};
				
				boolean check = true;
				if(prop.getProperty("alxUsername") == null){
					updateSystemOutput("[ERROR]: No username!");
					check = false;
				}
				if(prop.getProperty("alxPassword") == null){
					updateSystemOutput("[ERROR]: No password!");
					check = false;
				}
				if(prop.getProperty("path") == null){
					updateSystemOutput("[ERROR]: No path specified!");
					check = false;
				}
				if(check != false)
					test.start();
			}
		});
		btnRun.setBounds(64, 75, 75, 66);
		btnRun.setText("Run");
		
		Button btnCreateAlxReport = new Button(composite, SWT.NONE);
		btnCreateAlxReport.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				InputStream input = null;
				try {
					input = new FileInputStream("config.properties");
					prop.load(input);
				} catch (IOException e2) {
					e2.printStackTrace();
				}

				Thread t = new Thread(){
						public void run(){
							webManager.shippersLogIn(prop.getProperty("alxUsername"), prop.getProperty("alxPassword"));
							ArrayList<String> data = webManager.aggregateData(alx_input.getAbsolutePath());
						}
				};
				
				t.start();
			}
		});
		btnCreateAlxReport.setBounds(145, 75, 108, 25);
		btnCreateAlxReport.setText("Create ALX Report");
	}
	
//---------------
//EDI Methods
//---------------
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
//---------------
//Costco Methods
//---------------
	
	private ArrayList<Payment> parsePayments(){
		ArrayList <Payment> payments = new ArrayList<Payment>();
		for(File file: costco_input.listFiles()){
			List<String> rows = null;
			try {
				rows = Files.readAllLines(file.toPath());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for(String line : rows){
				Payment p = new Payment(line);
				payments.add(p);
			}
		}

		
		return payments;
	}

	private ArrayList<Invoice> parseInvoices(){
		ArrayList <Invoice> invoices = new ArrayList<Invoice>();
		for(File file: alx_input.listFiles()){
			List<String> rows = null;
			try {
				rows = Files.readAllLines(file.toPath());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for(String line : rows){
				Invoice i = new Invoice(line);
				invoices.add(i);
			}
		}

		return invoices;
	}

	
//---------------
//UI Methods
//---------------
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
	
	private void err(String errorMessage, EDI e){
		updateSystemOutput(errorMessage);
		e.addToErrorLog(errorMessage);
	}
}
