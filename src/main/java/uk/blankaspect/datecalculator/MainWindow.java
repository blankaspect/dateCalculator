/*====================================================================*\

MainWindow.java

Main window class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.datecalculator;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import uk.blankaspect.common.exception.ExceptionUtils;

import uk.blankaspect.common.swing.button.FButton;

import uk.blankaspect.common.swing.misc.GuiUtils;

import uk.blankaspect.common.swing.tabbedpane.FTabbedPane;

//----------------------------------------------------------------------


// MAIN WINDOW CLASS


class MainWindow
	extends JFrame
	implements ActionListener
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	String	INCREMENT_STR	= "Increment";
	private static final	String	DIFFERENCE_STR	= "Difference";
	private static final	String	INTERVAL_STR	= "Interval";
	private static final	String	PREFERENCES_STR	= "Preferences";
	private static final	String	EXIT_STR		= "Exit";

	// Commands
	private interface Command
	{
		String	EDIT_PREFERENCES	= "editPreferences";
		String	EXIT				= "exit";
	}

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public MainWindow(String titleStr)
	{

		// Call superclass constructor
		super(titleStr);

		// Set icons
		setIconImages(Images.APP_ICON_IMAGES);


		//----  Tabbed pane

		JTabbedPane tabbedPane = new FTabbedPane();
		tabbedPane.addTab(INCREMENT_STR, new IncrementPanel());
		tabbedPane.addTab(DIFFERENCE_STR, new DifferencePanel());
		tabbedPane.addTab(INTERVAL_STR, new IntervalPanel());


		//----  Button panel

		// Left button panel
		JPanel leftButtonPanel = new JPanel(new GridLayout(1, 0, 8, 0));

		// Button: preferences
		JButton preferencesButton = new FButton(PREFERENCES_STR + AppConstants.ELLIPSIS_STR);
		preferencesButton.setActionCommand(Command.EDIT_PREFERENCES);
		preferencesButton.addActionListener(this);
		leftButtonPanel.add(preferencesButton);

		// Right button panel
		JPanel rightButtonPanel = new JPanel(new GridLayout(1, 0, 8, 0));

		// Button: exit
		JButton exitButton = new FButton(EXIT_STR);
		exitButton.setMnemonic(KeyEvent.VK_X);
		exitButton.setActionCommand(Command.EXIT);
		exitButton.addActionListener(this);
		rightButtonPanel.add(exitButton);

		// Button panel
		GridBagLayout gridBag = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();

		JPanel buttonPanel = new JPanel(gridBag);
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(3, 24, 3, 24));

		int gridX = 0;

		gbc.gridx = gridX++;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.5;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 12);
		gridBag.setConstraints(leftButtonPanel, gbc);
		buttonPanel.add(leftButtonPanel);

		gbc.gridx = gridX++;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.5;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 12, 0, 0);
		gridBag.setConstraints(rightButtonPanel, gbc);
		buttonPanel.add(rightButtonPanel);


		//----  Main panel

		JPanel mainPanel = new JPanel(gridBag);
		mainPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

		int gridY = 0;

		gbc.gridx = 0;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(tabbedPane, gbc);
		mainPanel.add(tabbedPane);

		gbc.gridx = 0;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(1, 0, 0, 0);
		gridBag.setConstraints(buttonPanel, gbc);
		mainPanel.add(buttonPanel);


		//----  Window

		// Set content pane
		setContentPane(mainPanel);

		// Dispose of window explicitly
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

		// Handle window closing
		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent event)
			{
				onExit();
			}
		});

		// Prevent window from being resized
		setResizable(false);

		// Resize window to its preferred size
		pack();

		// Set location of window
		AppConfig config = AppConfig.INSTANCE;
		if (config.isMainWindowLocation())
			setLocation(GuiUtils.getLocationWithinScreen(this, config.getMainWindowLocation()));

		// Set default button
		getRootPane().setDefaultButton(exitButton);

		// Make window visible
		setVisible(true);

	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : ActionListener interface
////////////////////////////////////////////////////////////////////////

	public void actionPerformed(ActionEvent event)
	{
		String command = event.getActionCommand();

		if (command.equals(Command.EDIT_PREFERENCES))
			onEditPreferences();

		else if (command.equals(Command.EXIT))
			onExit();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	private void updateConfiguration()
	{
		// Save location of main window
		AppConfig config = AppConfig.INSTANCE;
		if (config.isMainWindowLocation())
		{
			Point location = GuiUtils.getFrameLocation(this);
			if (location != null)
				config.setMainWindowLocation(location);
		}

		// Write configuration file
		config.write();
	}

	//------------------------------------------------------------------

	private void onEditPreferences()
	{
		if (PreferencesDialog.showDialog(this))
		{
			AppConfig config = AppConfig.INSTANCE;
			ExceptionUtils.setUnixStyle(config.isShowUnixPathnames());
			config.updateDateNames();
		}
	}

	//------------------------------------------------------------------

	private void onExit()
	{
		// Update configuration
		updateConfiguration();

		// Destroy window
		setVisible(false);
		dispose();

		// Exit application
		System.exit(0);
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
