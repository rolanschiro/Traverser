package ui;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.FormData;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Label;
import org.eclipse.wb.swt.SWTResourceManager;

public class Results {

	protected Shell shlResults;
	private Composite composite;
	private Table table;
	private TableViewer tableViewer;
	private TableColumn colPO;
	private TableViewerColumn tableViewerColumn;
	private TableColumn colEFJ;
	private TableViewerColumn tableViewerColumn_1;
	private TableColumn colOffice;
	private TableViewerColumn tableViewerColumn_2;
	private TableColumn colRevisedLoad;
	private TableViewerColumn tableViewerColumn_3;
	private TableColumn colShipID;
	private TableViewerColumn tableViewerColumn_4;
	private ArrayList<String> results;

	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		ArrayList<String> arr = new ArrayList<String>();
		arr.add("12030627380,3647173,FL,3637987,1");
		arr.add("12030627380,3647173,FL,3637987,2");
		arr.add("12030627380,3647173,FL,3637987,3");

		
		try {
			Results window = new Results(arr);
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
	 */
	
	public Results(ArrayList<String> r){
		results = r;
	}
	
	public void open() {
		Display display = Display.getDefault();
		createContents();
		shlResults.open();
		shlResults.layout();
		while (!shlResults.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shlResults = new Shell();
		shlResults.setSize(492, 298);
		shlResults.setText("Results");
		shlResults.setLayout(new FormLayout());
		
		composite = new Composite(shlResults, SWT.NONE);
		composite.setBounds(139, 108, 64, 64);
		FormData fd_composite = new FormData();
		fd_composite.bottom = new FormAttachment(0, 252);
		fd_composite.right = new FormAttachment(0, 466);
		fd_composite.top = new FormAttachment(0, 44);
		fd_composite.left = new FormAttachment(0, 10);
		composite.setLayoutData(fd_composite);
		TableColumnLayout tcl_composite = new TableColumnLayout();
		composite.setLayout(tcl_composite);
		
		tableViewer = new TableViewer(composite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		table = tableViewer.getTable();
		table.pack();
		table.addKeyListener(new KeyAdapter() {
			@SuppressWarnings("static-access")
			@Override
			public void keyPressed(KeyEvent e) {
		        if (e.stateMask == SWT.CTRL && e.keyCode == 'c') {
		        	StringBuilder str = new StringBuilder();
		            for(TableItem item : table.getSelection()){
		            	for(int i = 0; i < 5; i++){
								str.append(item.getText(i) + "	");
						}
						str.append("\n");
		            }
		            
		            StringSelection selection = new StringSelection(str.toString());
		            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		            clipboard.setContents(selection, null);
		        }
			}
		});
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		
		tableViewerColumn = new TableViewerColumn(tableViewer, SWT.CENTER);
		colPO = tableViewerColumn.getColumn();
		colPO.setResizable(false);
		colPO.setText("PO");
		tcl_composite.setColumnData(colPO, new ColumnPixelData(96, true, true));
		
		tableViewerColumn_1 = new TableViewerColumn(tableViewer, SWT.CENTER);
		colEFJ = tableViewerColumn_1.getColumn();
		colEFJ.setResizable(false);
		tcl_composite.setColumnData(colEFJ, new ColumnPixelData(70, true, true));
		colEFJ.setText("EFJ");
		
		tableViewerColumn_2 = new TableViewerColumn(tableViewer, SWT.CENTER);
		colOffice = tableViewerColumn_2.getColumn();
		colOffice.setResizable(false);
		tcl_composite.setColumnData(colOffice, new ColumnPixelData(82, true, true));
		colOffice.setText("OFFICE");
		
		tableViewerColumn_3 = new TableViewerColumn(tableViewer, SWT.CENTER);
		colRevisedLoad = tableViewerColumn_3.getColumn();
		colRevisedLoad.setResizable(false);
		tcl_composite.setColumnData(colRevisedLoad, new ColumnPixelData(100, true, true));
		colRevisedLoad.setText("REVISED LOAD");
		
		tableViewerColumn_4 = new TableViewerColumn(tableViewer, SWT.CENTER);
		colShipID = tableViewerColumn_4.getColumn();
		colShipID.setResizable(false);
		tcl_composite.setColumnData(colShipID, new ColumnPixelData(84, true, true));
		colShipID.setText("SHIP ID");
		
		Label lblNewLabel = new Label(shlResults, SWT.NONE);
		lblNewLabel.setFont(SWTResourceManager.getFont("Segoe UI Symbol", 11, SWT.BOLD));
		FormData fd_lblNewLabel = new FormData();
		fd_lblNewLabel.top = new FormAttachment(0, 10);
		fd_lblNewLabel.left = new FormAttachment(0, 177);
		lblNewLabel.setLayoutData(fd_lblNewLabel);
		lblNewLabel.setText("Completed EDIs");
		
		for (String str : results){
			TableItem t = new TableItem(table, SWT.CENTER);
			t.setText(str.split(","));
		}
		
	}
}
