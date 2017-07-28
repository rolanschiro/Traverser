package ui;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Composite;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class Settings {

	protected Shell shlTraverserSettings;
	private Text pathName;
	private Text username;
	private Text pass;
	private Properties prop;

	File config = new File("config.properties");
	
	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			Settings window = new Settings();
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
		shlTraverserSettings.open();
		shlTraverserSettings.layout();
		while (!shlTraverserSettings.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shlTraverserSettings = new Shell();
		shlTraverserSettings.setSize(375, 335);
		shlTraverserSettings.setText("Traverser - Settings");
		
		InputStream input = null;
		prop = new Properties();
		try {
			input = new FileInputStream("config.properties");
			prop.load(input);
		} catch (IOException e1) {
		}

		Composite composite = new Composite(shlTraverserSettings, SWT.BORDER);
		composite.setBounds(20, 68, 329, 38);
		
		Label update = new Label(shlTraverserSettings, SWT.NONE);
		update.setFont(SWTResourceManager.getFont("Segoe UI Symbol", 9, SWT.BOLD));
		update.setBounds(159, 241, 40, 15);

		Label lblMatrix = new Label(composite, SWT.NONE);
		lblMatrix.setBounds(25, 10, 55, 15);
		lblMatrix.setText("Directory:");
		
		pathName = new Text(composite, SWT.BORDER);
		pathName.setBounds(104, 7, 204, 21);
		try {
			pathName.setText(prop.getProperty("path"));
		} catch (Exception e2) {
			pathName.setText("");
		}
		
		Label lblPathVariables = new Label(shlTraverserSettings, SWT.NONE);
		lblPathVariables.setFont(SWTResourceManager.getFont("Segoe UI Symbol", 12, SWT.BOLD));
		lblPathVariables.setBounds(141, 10, 77, 21);
		lblPathVariables.setText("SETTINGS");
		
		Composite composite_1 = new Composite(shlTraverserSettings, SWT.NONE);
		composite_1.setBounds(101, 262, 156, 25);
		
		Button saveButton = new Button(composite_1, SWT.NONE);
		saveButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					OutputStream output = null;
					prop = new Properties();
					try {
						output = new FileOutputStream("config.properties");
						config.createNewFile();
					} catch (IOException e1) {
					}

					prop.setProperty("path", pathName.getText());
					prop.setProperty("username", username.getText());
					prop.setProperty("password", pass.getText());
					
					prop.store(output, null);
					
					update.setText("Saved!");
					
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		saveButton.setBounds(0, 0, 75, 25);
		saveButton.setText("Save");
		
		Button exitButton = new Button(composite_1, SWT.NONE);
		exitButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				shlTraverserSettings.dispose();
			}
		});
		exitButton.setBounds(81, 0, 75, 25);
		exitButton.setText("Cancel");
		
		Label lblPathVariable = new Label(shlTraverserSettings, SWT.NONE);
		lblPathVariable.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.BOLD | SWT.ITALIC));
		lblPathVariable.setBounds(24, 47, 86, 15);
		lblPathVariable.setText("Path Variables:");
		
		Composite composite_2 = new Composite(shlTraverserSettings, SWT.BORDER);
		composite_2.setBounds(20, 145, 329, 81);

		Label lblLogInCredentials = new Label(shlTraverserSettings, SWT.NONE);
		lblLogInCredentials.setText("Log-In Credentials:");
		lblLogInCredentials.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.BOLD | SWT.ITALIC));
		lblLogInCredentials.setBounds(24, 124, 106, 15);
		
		Label lblUsername = new Label(composite_2, SWT.NONE);
		lblUsername.setText("Username:");
		lblUsername.setBounds(25, 10, 55, 15);
		
		username = new Text(composite_2, SWT.BORDER);
		username.setBounds(104, 7, 204, 21);
		try {
			username.setText(prop.getProperty("username"));
		} catch (Exception e2) {
			username.setText("");
		}
			Label lblPassword = new Label(composite_2, SWT.NONE);
		lblPassword.setText("Password:");
		lblPassword.setBounds(25, 49, 55, 15);
		
		pass = new Text(composite_2, SWT.BORDER | SWT.PASSWORD);
		pass.setBounds(104, 46, 204, 21);
		try {
			pass.setText(prop.getProperty("password"));
		} catch (Exception e2) {
			pass.setText("");
		}
	
		

	}
}
