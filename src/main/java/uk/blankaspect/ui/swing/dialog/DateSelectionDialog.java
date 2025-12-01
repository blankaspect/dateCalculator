/*====================================================================*\

DateSelectionDialog.java

Date selection dialog class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.ui.swing.dialog;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Window;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import uk.blankaspect.common.date.Date;

import uk.blankaspect.ui.swing.action.KeyAction;

import uk.blankaspect.ui.swing.button.FButton;

import uk.blankaspect.ui.swing.container.DateSelectionPanel;

import uk.blankaspect.ui.swing.misc.GuiConstants;
import uk.blankaspect.ui.swing.misc.GuiUtils;

import uk.blankaspect.ui.swing.workaround.LinuxWorkarounds;

//----------------------------------------------------------------------


// DATE SELECTION DIALOG CLASS


public class DateSelectionDialog
	extends JDialog
	implements ActionListener
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	Insets	BUTTON_MARGINS	= new Insets(1, 2, 1, 2);

	private static final	Color	BUTTON_PANEL_BORDER_COLOUR	= new Color(200, 200, 200);

	private static final	Color	BORDER_COLOUR	= new Color(216, 176, 72);

	// Commands
	private interface Command
	{
		String	ACCEPT	= "accept";
		String	CLOSE	= "close";
	}

////////////////////////////////////////////////////////////////////////
//  Class variables
////////////////////////////////////////////////////////////////////////

	private static	Map<String, Point>	locations	= new HashMap<>();

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	String				key;
	private	Point				initialLocation;
	private	boolean				accepted;
	private	DateSelectionPanel	dateSelectionPanel;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public DateSelectionDialog(Window  owner,
							   Point   location,
							   Date    date,
							   boolean showAdjacentMonths,
							   String  key)
	{
		// Call alternative constructor
		this(owner, location, date, 0, showAdjacentMonths, key);
	}

	//------------------------------------------------------------------

	public DateSelectionDialog(Window  owner,
							   Point   location,
							   Date    date,
							   int     firstDayOfWeek,
							   boolean showAdjacentMonths,
							   String  key)
	{
		// Call superclass constructor
		super(owner, ModalityType.APPLICATION_MODAL);

		// Initialise instance variables
		this.key = key;


		//----  Date selection panel

		dateSelectionPanel = new DateSelectionPanel(date, firstDayOfWeek, showAdjacentMonths);
		dateSelectionPanel.setAcceptAction(new AcceptAction());


		//----  Button panel

		JPanel buttonPanel = new JPanel(new GridLayout(1, 0, 8, 0));
		buttonPanel.setBorder(BorderFactory
				.createCompoundBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BUTTON_PANEL_BORDER_COLOUR),
									  BorderFactory.createEmptyBorder(2, 4, 2, 4)));

		// Button: OK
		JButton okButton = new FButton(GuiConstants.OK_STR);
		okButton.setMargin(BUTTON_MARGINS);
		okButton.setActionCommand(Command.ACCEPT);
		okButton.addActionListener(this);
		buttonPanel.add(okButton);

		// Button: cancel
		JButton cancelButton = new FButton(GuiConstants.CANCEL_STR);
		cancelButton.setMargin(BUTTON_MARGINS);
		cancelButton.setActionCommand(Command.CLOSE);
		cancelButton.addActionListener(this);
		buttonPanel.add(cancelButton);


		//----  Main panel

		GridBagLayout gridBag = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();

		JPanel mainPanel = new JPanel(gridBag);
		mainPanel.setBorder(BorderFactory.createLineBorder(BORDER_COLOUR));

		int gridY = 0;

		gbc.gridx = 0;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(dateSelectionPanel, gbc);
		mainPanel.add(dateSelectionPanel);

		gbc.gridx = 0;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(buttonPanel, gbc);
		mainPanel.add(buttonPanel);

		// Add commands to action map
		KeyAction.create(mainPanel, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,
						 KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), Command.CLOSE, this);


		//----  Window

		// Set content pane
		setContentPane(mainPanel);

		// Omit frame from dialog
		setUndecorated(true);

		// Dispose of window explicitly
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

		// Handle window events
		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowOpened(
				WindowEvent	event)
			{
				// WORKAROUND for a bug that has been observed on Linux/GNOME whereby a window is displaced downwards
				// when its location is set.  The error in the y coordinate is the height of the title bar of the
				// window.  The workaround is to set the location of the window again with an adjustment for the error.
				LinuxWorkarounds.fixWindowYCoord(event.getWindow(), initialLocation);
			}

			@Override
			public void windowClosing(
				WindowEvent	event)
			{
				onClose();
			}
		});

		// Prevent dialog from being resized
		setResizable(false);

		// Resize dialog to its preferred size
		pack();

		// Set location of dialog
		if (location == null)
			location = locations.get(key);
		if (location == null)
			location = GuiUtils.getComponentLocation(this, owner);
		initialLocation = GuiUtils.getComponentLocation(this, location);
		setLocation(initialLocation);

		// Set default button
		getRootPane().setDefaultButton(okButton);

		// Set focus
		dateSelectionPanel.requestFocusInWindow();

		// Show dialog
		setVisible(true);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static Date showDialog(Component parent,
								  Point     location,
								  Date      date,
								  boolean   showAdjacentMonths,
								  String    key)
	{
		return showDialog(parent, location, date, 0, showAdjacentMonths, key);
	}

	//------------------------------------------------------------------

	public static Date showDialog(Component parent,
								  Point     location,
								  Date      date,
								  int       firstDayOfWeek,
								  boolean   showAdjacentMonths,
								  String    key)
	{
		return new DateSelectionDialog(GuiUtils.getWindow(parent), location, date, firstDayOfWeek, showAdjacentMonths,
									   key).getDate();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : ActionListener interface
////////////////////////////////////////////////////////////////////////

	@Override
	public void actionPerformed(ActionEvent event)
	{
		switch (event.getActionCommand())
		{
			case Command.ACCEPT -> onAccept();
			case Command.CLOSE  -> onClose();
		}
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public Date getDate()
	{
		return accepted ? dateSelectionPanel.getDate() : null;
	}

	//------------------------------------------------------------------

	private void onAccept()
	{
		accepted = true;
		onClose();
	}

	//------------------------------------------------------------------

	private void onClose()
	{
		locations.put(key, getLocation());
		setVisible(false);
		dispose();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Member classes : inner classes
////////////////////////////////////////////////////////////////////////


	// ACCEPT ACTION CLASS


	private class AcceptAction
		extends AbstractAction
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private AcceptAction()
		{
			putValue(Action.ACTION_COMMAND_KEY, Command.ACCEPT);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : ActionListener interface
	////////////////////////////////////////////////////////////////////

		@Override
		public void actionPerformed(ActionEvent event)
		{
			DateSelectionDialog.this.actionPerformed(event);
		}

		//--------------------------------------------------------------

	}

	//==================================================================

}

//----------------------------------------------------------------------
