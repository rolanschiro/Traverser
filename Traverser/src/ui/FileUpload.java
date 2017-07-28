package ui;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.custom.StyledText;

public class FileUpload {

	protected Shell shlUpload;

	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			FileUpload window = new FileUpload();
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
		createContents();
		shlUpload.open();
		shlUpload.layout();
		while (!shlUpload.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shlUpload = new Shell();
		shlUpload.setSize(450, 300);
		shlUpload.setText("Traverser - Upload");
		
		Label lblFileUpload = new Label(shlUpload, SWT.NONE);
		lblFileUpload.setFont(SWTResourceManager.getFont("Segoe UI Symbol", 12, SWT.BOLD));
		lblFileUpload.setBounds(172, 10, 89, 21);
		lblFileUpload.setText("File Upload");
		
		Composite composite = new Composite(shlUpload, SWT.BORDER);
		composite.setBounds(10, 57, 414, 64);
		
		DropTarget dropTarget = new DropTarget(composite, DND.DROP_COPY);
		
		
		Composite composite_1 = new Composite(shlUpload, SWT.BORDER);
		composite_1.setBounds(10, 143, 414, 64);
		
		DropTarget dropTarget_1 = new DropTarget(composite_1, DND.DROP_COPY);

	}
}
