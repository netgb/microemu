/*
 *  MicroEmulator
 *  Copyright (C) 2001-2003 Bartek Teodorczyk <barteo@barteo.net>
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
 
package org.microemu.app;

import java.io.File;
import java.net.MalformedURLException;

import javax.microedition.midlet.MIDlet;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.microemu.DisplayComponent;
import org.microemu.EmulatorContext;
import org.microemu.MIDletBridge;
import org.microemu.app.launcher.Launcher;
import org.microemu.app.ui.ResponseInterfaceListener;
import org.microemu.app.ui.StatusBarListener;
import org.microemu.app.ui.swt.SwtDeviceComponent;
import org.microemu.app.ui.swt.SwtDialog;
import org.microemu.app.ui.swt.SwtInputDialog;
import org.microemu.app.ui.swt.SwtMessageDialog;
import org.microemu.app.ui.swt.SwtSelectDeviceDialog;
import org.microemu.app.util.DeviceEntry;
import org.microemu.app.util.ProgressJarClassLoader;
import org.microemu.device.Device;
import org.microemu.device.DeviceDisplay;
import org.microemu.device.DeviceFactory;
import org.microemu.device.FontManager;
import org.microemu.device.InputMethod;
import org.microemu.device.swt.SwtDeviceDisplay;
import org.microemu.device.swt.SwtFontManager;
import org.microemu.device.swt.SwtInputMethod;



public class Swt extends Common
{
	public static Shell shell;

	protected static SwtDeviceComponent devicePanel;

	protected boolean initialized = false;
  
	protected MenuItem menuOpenJADFile;
	protected MenuItem menuOpenJADURL;

	private SwtSelectDeviceDialog selectDeviceDialog;
	private FileDialog fileDialog = null;
	private MenuItem menuSelectDevice;
	    
	private DeviceEntry deviceEntry;

	private Label statusBar;
  
	private KeyListener keyListener = new KeyListener()
	{    
		public void keyTyped(KeyEvent e)
		{
		}
    
		public void keyPressed(KeyEvent e)
		{
//			devicePanel.keyPressed(e);
		}
    
		public void keyReleased(KeyEvent e)
		{
//			devicePanel.keyReleased(e);
		}    
	};
   
	protected Listener menuOpenJADFileListener = new Listener()
	{
		public void handleEvent(Event ev)
		{
			if (fileDialog == null) {
				fileDialog = new FileDialog(shell, SWT.OPEN);
				fileDialog.setText("Open JAD File...");
				fileDialog.setFilterNames(new String[] {"JAD files"});
				fileDialog.setFilterExtensions(new String[] {"*.jad"});
			}
      
			fileDialog.open();

			if (fileDialog.getFileName().length() > 0) {
				try {
					openJadUrl(new File(fileDialog.getFilterPath(), fileDialog.getFileName()).toURL().toString());
				} catch (MalformedURLException ex) {
					System.err.println("Bad URL format " + fileDialog.getFileName());
				}
			}
		} 
	};
  
	protected Listener menuOpenJADURLListener = new Listener()
	{
		public void handleEvent(Event ev)
		{
			SwtInputDialog inputDialog = new SwtInputDialog(shell, "Open...", "Enter JAD URL:");
			if (inputDialog.open() == SwtDialog.OK) {
				try {
					openJadUrl(inputDialog.getValue());
				} catch (MalformedURLException ex) {
					System.err.println("Bad URL format " + inputDialog.getValue());
				}
			}
		}    
	};
  
	protected Listener menuExitListener = new Listener()
	{    
		public void handleEvent(Event e)
		{
			System.exit(0);
		}    
	};
  
  
	private Listener menuSelectDeviceListener = new Listener()
	{    
		public void handleEvent(Event e)
		{
			if (selectDeviceDialog.open() == SwtDialog.OK) {
				if (selectDeviceDialog.getSelectedDeviceEntry().equals(getDevice())) {
					return;
				}
				if (MIDletBridge.getCurrentMIDlet() != getLauncher()) {
					if (!SwtMessageDialog.openQuestion(shell,
							"Question?", "Changing device needs MIDlet to be restarted. All MIDlet data will be lost. Are you sure?")) { 
						return;
					}
				}
				setDevice(selectDeviceDialog.getSelectedDeviceEntry());

				if (MIDletBridge.getCurrentMIDlet() != getLauncher()) {
					try {
						MIDlet result = (MIDlet) MIDletBridge.getCurrentMIDlet().getClass().newInstance();
						startMidlet(result);
					} catch (Exception ex) {
						System.err.println(ex);
					}
				} else {
					startMidlet(getLauncher());
				}
			}
		}    
	};
	
	private StatusBarListener statusBarListener = new StatusBarListener()
	{			
		public void statusBarChanged(final String text) 
		{			
			shell.getDisplay().asyncExec(new Runnable()
			{
				public void run() 
				{
					statusBar.setText(text);
				}
			});
		}  
	};
  
	private ResponseInterfaceListener responseInterfaceListener = new ResponseInterfaceListener()
	{
		public void stateChanged(final boolean state) 
		{
			shell.getDisplay().asyncExec(new Runnable()
			{
				public void run() 
				{
					menuOpenJADFile.setEnabled(state);
					menuOpenJADURL.setEnabled(state);
					menuSelectDevice.setEnabled(state);
				}
			});
		}  
	};
  
/*	WindowAdapter windowListener = new WindowAdapter()
	{
		public void windowClosing(WindowEvent ev) 
		{
			menuExitListener.actionPerformed(null);
		}
		

		public void windowIconified(WindowEvent ev) 
		{
			MIDletBridge.getMIDletAccess(common.getLauncher().getCurrentMIDlet()).pauseApp();
		}
		
		public void windowDeiconified(WindowEvent ev) 
		{
			try {
				MIDletBridge.getMIDletAccess(common.getLauncher().getCurrentMIDlet()).startApp();
			} catch (MIDletStateChangeException ex) {
				System.err.println(ex);
			}
		}
	};*/
	
  
	protected Swt(Shell shell)
	{
		super(new EmulatorContext()
		{
			private ProgressJarClassLoader loader = new ProgressJarClassLoader(this.getClass().getClassLoader());
    
			private InputMethod inputMethod = new SwtInputMethod();
			
			private DeviceDisplay deviceDisplay = new SwtDeviceDisplay(this);

			private FontManager fontManager = new SwtFontManager();
			
			public ClassLoader getClassLoader()
			{
				return loader;
			}
    
			public DisplayComponent getDisplayComponent()
			{
				return devicePanel.getDisplayComponent();
			}

			public Launcher getLauncher() 
			{
				return getLauncher();
			}

            public InputMethod getDeviceInputMethod()
            {
                return inputMethod;
            }    

            public DeviceDisplay getDeviceDisplay()
            {
                return deviceDisplay;
            }

			public FontManager getDeviceFontManager() 
			{
				return fontManager;
			}    
		});

		initInterface(shell);

//		addWindowListener(windowListener);
		    
		Config.loadConfig("config.xml");
		shell.addKeyListener(keyListener);

		selectDeviceDialog = new SwtSelectDeviceDialog(shell);
		setDevice(selectDeviceDialog.getSelectedDeviceEntry());
    
		setStatusBarListener(statusBarListener);
		setResponseInterfaceListener(responseInterfaceListener);
    
		initialized = true;
	}
	
	
	protected void initInterface(Shell shell)
	{
		GridLayout layout = new GridLayout(1, false);
		shell.setLayout(layout);
		shell.setLayoutData(new GridData(GridData.FILL_BOTH));

		Menu bar = new Menu(shell, SWT.BAR);
		shell.setMenuBar(bar);
    
		MenuItem menuFile = new MenuItem(bar, SWT.CASCADE);
		menuFile.setText("File");
    
		Menu fileSubmenu = new Menu(shell, SWT.DROP_DOWN);
		menuFile.setMenu(fileSubmenu);

		menuOpenJADFile = new MenuItem(fileSubmenu, SWT.PUSH);
		menuOpenJADFile.setText("Open JAD File...");
		menuOpenJADFile.addListener(SWT.Selection, menuOpenJADFileListener);

		menuOpenJADURL = new MenuItem(fileSubmenu, 0);
		menuOpenJADURL.setText("Open JAD URL...");
		menuOpenJADURL.addListener(SWT.Selection, menuOpenJADURLListener);

		new MenuItem(fileSubmenu, SWT.SEPARATOR);
    
		MenuItem menuExit = new MenuItem(fileSubmenu, SWT.PUSH);
		menuExit.setText("Exit");
		menuExit.addListener(SWT.Selection, menuExitListener);
    
		MenuItem menuOptions = new MenuItem(bar, SWT.CASCADE);
		menuOptions.setText("Options");
    
		Menu optionsSubmenu = new Menu(shell, SWT.DROP_DOWN);
		menuOptions.setMenu(optionsSubmenu);

		menuSelectDevice = new MenuItem(optionsSubmenu, SWT.PUSH);
		menuSelectDevice.setText("Select device...");
		menuSelectDevice.addListener(SWT.Selection, menuSelectDeviceListener);

		shell.setText("MicroEmulator");

		devicePanel = new SwtDeviceComponent(shell);
		devicePanel.setLayoutData(new GridData(GridData.FILL_BOTH));

		statusBar = new Label(shell, SWT.HORIZONTAL);
		statusBar.setText("Status");
		statusBar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	}
      
  
	public DeviceEntry getDevice()
	{
		return deviceEntry;
	}
  
  
	public void setDevice(DeviceEntry entry)
	{
		if (DeviceFactory.getDevice() != null) {
//			((SwtDevice) DeviceFactory.getDevice()).dispose();
		}
		
		ProgressJarClassLoader loader = (ProgressJarClassLoader) emulatorContext.getClassLoader();
		try {
			Class deviceClass = null;
			if (entry.getFileName() != null) {
				loader.addRepository(
						new File(Config.getConfigPath(), entry.getFileName()).toURL());
				deviceClass = loader.findClass(entry.getClassName());
			} else {
				deviceClass = Class.forName(entry.getClassName());
			}
			Device device = (Device) deviceClass.newInstance();
			this.deviceEntry = entry;
			setDevice(device);
		} catch (MalformedURLException ex) {
			System.err.println(ex);          
		} catch (ClassNotFoundException ex) {
			System.err.println(ex);          
		} catch (InstantiationException ex) {
			System.err.println(ex);          
		} catch (IllegalAccessException ex) {
			System.err.println(ex);          
		}
	}
	
	
	protected void setDevice(Device device)
	{
		super.setDevice(device);
		
		device.init(emulatorContext);
		shell.setSize(shell.computeSize(SWT.DEFAULT, SWT.DEFAULT, true));
	}
  
  
	public static void main(String args[])
	{
		Display display = new Display();
		shell = new Shell(display, SWT.CLOSE | SWT.TITLE | SWT.MIN);
		    
		Swt app = new Swt(shell);
		MIDlet m = null;

		if (args.length > 0) {
			for (int i = 0; i < args.length; i++) {
				if (args[i].equals("--deviceClass")) {
					i++;
					try {
						Class deviceClass = Class.forName(args[i]);
						app.setDevice((Device) deviceClass.newInstance());
					} catch (ClassNotFoundException ex) {
						System.err.println(ex);          
					} catch (InstantiationException ex) {
						System.err.println(ex);          
					} catch (IllegalAccessException ex) {
						System.err.println(ex);          
					}
					
				} else if (args[i].endsWith(".jad")) {
					try {
						File file = new File(args[i]);
						String url = file.exists() ? file.toURL().toString() : args[i];
						app.openJadUrl(url);
					} catch(MalformedURLException exception) {
						System.out.println("Cannot parse " + args[0] + " URL");
					}
				} else {
					Class midletClass;
					try {
						midletClass = Class.forName(args[i]);
						m = app.loadMidlet("MIDlet", midletClass);
					} catch (ClassNotFoundException ex) {
						System.out.println("Cannot find " + args[i] + " MIDlet class");
					}
				}
			}
		} else {
			m = app.getLauncher();
		}
    
		if (app.initialized) {
			if (m != null) {
				app.startMidlet(m);
			}
			
			shell.pack ();
			shell.open ();
			while (!shell.isDisposed ()) {
				if (!display.readAndDispatch ())
					display.sleep ();
			}
			display.dispose ();			
		}

		System.exit(0);
	}

}