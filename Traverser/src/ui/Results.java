package ui;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.SWT;
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
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Label;
import org.eclipse.wb.swt.SWTResourceManager;

public class Results {

	protected Shell shlResults;
	private Composite composite;
	private Table table;
	private TableViewer tableViewer;
	
	private ArrayList<TableColumn> cols = new ArrayList<TableColumn>();
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
		shlResults.setText("Results");
		shlResults.setLayout(new FormLayout());

		
		composite = new Composite(shlResults, SWT.NONE);
		composite.setBounds(139, 108, 64, 64);

		TableColumnLayout tcl_composite = new TableColumnLayout();
		composite.setLayout(tcl_composite);
		
		tableViewer = new TableViewer(composite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		for(String str : results.get(0).split(",")){		
			TableColumn column = createColumn(str, tcl_composite);
			cols.add(column);
		}
		results.remove(0);
		
		shlResults.pack();

		int width = (cols.get(0).getWidth() * cols.size());

		FormData fd_composite = new FormData();
		fd_composite.bottom = new FormAttachment(0, 300);
		fd_composite.right = new FormAttachment(0, width + 35);
		fd_composite.top = new FormAttachment(0, 44);
		fd_composite.left = new FormAttachment(0, 10);
		composite.setLayoutData(fd_composite);
		
		table = tableViewer.getTable();
		
		shlResults.update();

		shlResults.setSize(width + 50, 350);

    	System.out.println(width);
	   	System.out.println(table.getSize());
    	System.out.println(composite.getSize());
    	System.out.println(shlResults.getSize());

		table.addKeyListener(new KeyAdapter() {
			@SuppressWarnings("static-access")
			@Override
			public void keyPressed(KeyEvent e) {
		        if (e.stateMask == SWT.CTRL && e.keyCode == 'c') {
		        	StringBuilder str = new StringBuilder();
		            for(TableItem item : table.getSelection()){
		            	System.out.println(item.getBounds().width);
		            	for(int i = 0; i < cols.size() - 1; i++){
								str.append(item.getText(i) + "	");
						}
		            	str.append(item.getText(cols.size()- 1));
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
		
		Label lblNewLabel = new Label(shlResults, SWT.NONE);
		lblNewLabel.setFont(SWTResourceManager.getFont("Segoe UI Symbol", 11, SWT.BOLD));
		FormData fd_lblNewLabel = new FormData();
		fd_lblNewLabel.top = new FormAttachment(0, 10);
		fd_lblNewLabel.left = new FormAttachment(0, (width - 25)/2);
		lblNewLabel.setLayoutData(fd_lblNewLabel);
		lblNewLabel.setText("Results");
		
		for (String str : results){
			TableItem t = new TableItem(table, SWT.CENTER);
			t.setText(str.split(","));
		}
	}
	
	public TableColumn createColumn(String header, TableColumnLayout tcl){
		TableViewerColumn tableViewerCol = new TableViewerColumn(tableViewer, SWT.CENTER);
		TableColumn column = tableViewerCol.getColumn();
		tcl.setColumnData(column, new ColumnPixelData(100, true, true));
		
		column.setText(header);
		
		return column;
	}
}


