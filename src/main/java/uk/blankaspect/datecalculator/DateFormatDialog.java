/*====================================================================*\

DateFormatDialog.java

Date format dialog box class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.datecalculator;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import uk.blankaspect.common.exception.AppException;

import uk.blankaspect.common.gui.Colours;
import uk.blankaspect.common.gui.FButton;
import uk.blankaspect.common.gui.FLabel;
import uk.blankaspect.common.gui.FontStyle;
import uk.blankaspect.common.gui.FTextField;
import uk.blankaspect.common.gui.GuiUtils;
import uk.blankaspect.common.gui.TaggedText;

import uk.blankaspect.common.misc.DateFormat;
import uk.blankaspect.common.misc.KeyAction;

//----------------------------------------------------------------------


// DATE FORMAT DIALOG BOX CLASS


class DateFormatDialog
	extends JDialog
	implements ActionListener
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	int	NAME_FIELD_NUM_COLUMNS		= 16;
	private static final	int	PATTERN_FIELD_NUM_COLUMNS	= 32;

	private static final	String	NAME_STR	= "Name";
	private static final	String	PATTERN_STR	= "Pattern";
	private static final	String	HELP_STR	= "Help";

	// Commands
	private interface Command
	{
		String	HELP	= "help";
		String	ACCEPT	= "accept";
		String	CLOSE	= "close";
	}

	private static final	Color	HELP_PANEL_BACKGROUND_COLOUR	= Color.WHITE;
	private static final	Color	HELP_PANEL_BORDER_COLOUR		= Colours.LINE_BORDER;

	private static final	String[]	HELP_TEXT_STRS	=
	{
		"{s0}Field identifiers",
		"{v0}",
		"{h0}{f0}{s2}y{f}{h1}year",
		"{h0}{f0}{s2}m{f}{h1}month (number)",
		"{h0}{f0}{s2}M{f}{h1}month (name)",
		"{h0}{f0}{s2}d{f}{h1}day (number)",
		"{h0}{f0}{s2}D{f}{h1}day (name)",
		"{v1}",
		"{s0}Field prefixes",
		"{v0}",
		"{s1}In the following prefixes, {s3}n{s1} is a decimal number between",
		"{s1}1 and 999 denoting the width of the field in characters.",
		"{h0}{f1}{s2}%{f}{h1}implicit width",
		"{h0}{f1}{s2}%{s3}n{f}{h1}width {s3}n{s}, left-aligned",
		"{h0}{f1}{s2}%0{s3}n{f}{h1}width {s3}n{s}, padded with leading zeros",
		"{h0}{f1}{s2}%[{s3}n{f}{h1}width {s3}n{s}, left-aligned",
		"{h0}{f1}{s2}%]{s3}n{f}{h1}width {s3}n{s}, right-aligned",
		"{v1}",
		"{s0}Other",
		"{v0}",
		"{h0}{s2}%%{s}{h1}literal '%' character"
	};

	private static final	List<TaggedText.FieldDef>		FIELD_DEFS		= Arrays.asList
	(
		new TaggedText.FieldDef(0),
		new TaggedText.FieldDef(1)
	);
	private static final	List<TaggedText.VPaddingDef>	V_PADDING_DEFS	= Arrays.asList
	(
		new TaggedText.VPaddingDef(0, 0.2f, new Color(160, 192, 160)),
		new TaggedText.VPaddingDef(1, 0.6f)
	);
	private static final	List<TaggedText.StyleDef>		STYLE_DEFS		= Arrays.asList
	(
		new TaggedText.StyleDef(0, FontStyle.BOLD, new Color(0, 64, 64)),
		new TaggedText.StyleDef(1, new Color(96, 96, 96)),
		new TaggedText.StyleDef(2, FontStyle.BOLD, new Color(192, 64, 0)),
		new TaggedText.StyleDef(3, FontStyle.BOLD_ITALIC, new Color(0, 96, 128))
	);
	private static final	List<TaggedText.HPaddingDef>	H_PADDING_DEFS	= Arrays.asList
	(
		new TaggedText.HPaddingDef(0, 1.0f),
		new TaggedText.HPaddingDef(1, 1.2f)
	);

////////////////////////////////////////////////////////////////////////
//  Enumerated types
////////////////////////////////////////////////////////////////////////


	// ERROR IDENTIFIERS


	private enum ErrorId
		implements AppException.IId
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		NO_NAME
		("No name was specified."),

		DUPLICATE_NAME
		("Name: %1\nA date format with this name is already defined."),

		NO_PATTERN
		("No pattern was specified."),

		SEPARATOR_NOT_ALLOWED
		("The name/pattern separator, \"" + DateFormat.SEPARATOR + "\", is not allowed in a name or " +
			"pattern.");

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private ErrorId(String message)
		{
			this.message = message;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : AppException.IId interface
	////////////////////////////////////////////////////////////////////

		public String getMessage()
		{
			return message;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance fields
	////////////////////////////////////////////////////////////////////

		private	String	message;

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Member classes : inner classes
////////////////////////////////////////////////////////////////////////


	// HELP DIALOG BOX CLASS


	private class HelpDialog
		extends JDialog
		implements ActionListener
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	String	DATE_FORMAT_STR	= "Date format";

	////////////////////////////////////////////////////////////////////
	//  Member classes : inner classes
	////////////////////////////////////////////////////////////////////


		// TEXT PANEL CLASS


		private class TextPanel
			extends JComponent
		{

		////////////////////////////////////////////////////////////////
		//  Constants
		////////////////////////////////////////////////////////////////

			private static final	int	VERTICAL_MARGIN		= 3;
			private static final	int	HORIZONTAL_MARGIN	= 6;

		////////////////////////////////////////////////////////////////
		//  Constructors
		////////////////////////////////////////////////////////////////

			private TextPanel()
			{
				AppFont.MAIN.apply(this);
				text = new TaggedText('{', '}', FIELD_DEFS, STYLE_DEFS, V_PADDING_DEFS, H_PADDING_DEFS,
									  HELP_TEXT_STRS);
				setOpaque(true);
				setFocusable(false);
			}

			//----------------------------------------------------------

		////////////////////////////////////////////////////////////////
		//  Instance methods : overriding methods
		////////////////////////////////////////////////////////////////

			@Override
			public Dimension getPreferredSize()
			{
				return new Dimension(2 * HORIZONTAL_MARGIN + text.getWidth(),
									 2 * VERTICAL_MARGIN + text.getHeight());
			}

			//----------------------------------------------------------

			@Override
			protected void paintComponent(Graphics gr)
			{
				// Fill background
				Rectangle rect = gr.getClipBounds();
				gr.setColor(HELP_PANEL_BACKGROUND_COLOUR);
				gr.fillRect(rect.x, rect.y, rect.width, rect.height);

				// Draw border
				gr.setColor(HELP_PANEL_BORDER_COLOUR);
				gr.drawRect(0, 0, getWidth() - 1, getHeight() - 1);

				// Draw text
				text.draw(gr, VERTICAL_MARGIN, HORIZONTAL_MARGIN);
			}

			//----------------------------------------------------------

		////////////////////////////////////////////////////////////////
		//  Instance methods
		////////////////////////////////////////////////////////////////

			public void updateText()
			{
				text.update(getGraphics());
			}

			//----------------------------------------------------------

		////////////////////////////////////////////////////////////////
		//  Instance fields
		////////////////////////////////////////////////////////////////

			private	TaggedText	text;

		}

		//==============================================================

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private HelpDialog()
		{
			// Call superclass constructor
			super(DateFormatDialog.this, DATE_FORMAT_STR);

			// Set icons
			setIconImages(DateFormatDialog.this.getIconImages());


			//----  Text panel

			TextPanel textPanel = new TextPanel();


			//----  Button panel

			JPanel buttonPanel = new JPanel(new GridLayout(1, 0, 0, 0));
			buttonPanel.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));

			// Button: close
			JButton closeButton = new FButton(AppConstants.CLOSE_STR);
			closeButton.setActionCommand(Command.CLOSE);
			closeButton.addActionListener(this);
			buttonPanel.add(closeButton);


			//----  Main panel

			GridBagLayout gridBag = new GridBagLayout();
			GridBagConstraints gbc = new GridBagConstraints();

			JPanel mainPanel = new JPanel(gridBag);

			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.weightx = 0.0;
			gbc.weighty = 0.0;
			gbc.anchor = GridBagConstraints.NORTH;
			gbc.fill = GridBagConstraints.NONE;
			gbc.insets = new Insets(0, 0, 0, 0);
			gridBag.setConstraints(textPanel, gbc);
			mainPanel.add(textPanel);

			gbc.gridx = 0;
			gbc.gridy = 1;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.weightx = 0.0;
			gbc.weighty = 0.0;
			gbc.anchor = GridBagConstraints.NORTH;
			gbc.fill = GridBagConstraints.NONE;
			gbc.insets = new Insets(3, 0, 3, 0);
			gridBag.setConstraints(buttonPanel, gbc);
			mainPanel.add(buttonPanel);

			// Add commands to action map
			KeyAction.create(mainPanel, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,
							 KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), Command.CLOSE, this);


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
					onClose();
				}
			});

			// Prevent dialog from being resized
			setResizable(false);

			// Resize dialog to its preferred size
			pack();

			// Resize dialog again after updating dimensions of tagged text
			textPanel.updateText();
			pack();

			// Set location of dialog box
			if (helpDialogLocation == null)
				helpDialogLocation = GuiUtils.getComponentLocation(this, DateFormatDialog.this);
			setLocation(helpDialogLocation);

			// Set default button
			getRootPane().setDefaultButton(closeButton);

			// Set focus
			closeButton.requestFocusInWindow();

			// Show dialog
			setVisible(true);

		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : ActionListener interface
	////////////////////////////////////////////////////////////////////

		public void actionPerformed(ActionEvent event)
		{
			if (event.getActionCommand().equals(Command.CLOSE))
				onClose();
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		private void close()
		{
			helpDialogLocation = getLocation();
			setVisible(false);
			dispose();
		}

		//--------------------------------------------------------------

		private void onClose()
		{
			DateFormatDialog.this.closeHelpDialog();
		}

		//--------------------------------------------------------------

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	private DateFormatDialog(Window       owner,
							 String       titleStr,
							 DateFormat   dateFormat,
							 List<String> names)
	{

		// Call superclass constructor
		super(owner, titleStr, Dialog.ModalityType.APPLICATION_MODAL);

		// Set icons
		setIconImages(owner.getIconImages());

		// Initialise instance fields
		this.names = names;


		//----  Control panel

		GridBagLayout gridBag = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();

		JPanel controlPanel = new JPanel(gridBag);
		GuiUtils.setPaddedLineBorder(controlPanel);

		int gridY = 0;

		// Label: name
		JLabel nameLabel = new FLabel(NAME_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(nameLabel, gbc);
		controlPanel.add(nameLabel);

		// Field: name
		nameField = new FTextField(NAME_FIELD_NUM_COLUMNS);
		if (dateFormat != null)
			nameField.setText(dateFormat.getName());

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(nameField, gbc);
		controlPanel.add(nameField);

		// Label: pattern
		JLabel patternLabel = new FLabel(PATTERN_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(patternLabel, gbc);
		controlPanel.add(patternLabel);

		// Field: pattern
		patternField = new FTextField(PATTERN_FIELD_NUM_COLUMNS);
		if (dateFormat != null)
			patternField.setText(dateFormat.getPattern());

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(patternField, gbc);
		controlPanel.add(patternField);


		//----  Button panel

		// Left button panel
		JPanel leftButtonPanel = new JPanel(new GridLayout(1, 0, 8, 0));

		// Button: help
		helpButton = new FButton(HELP_STR + AppConstants.ELLIPSIS_STR);
		helpButton.setActionCommand(Command.HELP);
		helpButton.addActionListener(this);
		leftButtonPanel.add(helpButton);

		// Right button panel
		JPanel rightButtonPanel = new JPanel(new GridLayout(1, 0, 8, 0));

		// Button: OK
		JButton okButton = new FButton(AppConstants.OK_STR);
		okButton.setActionCommand(Command.ACCEPT);
		okButton.addActionListener(this);
		rightButtonPanel.add(okButton);

		// Button: cancel
		JButton cancelButton = new FButton(AppConstants.CANCEL_STR);
		cancelButton.setActionCommand(Command.CLOSE);
		cancelButton.addActionListener(this);
		rightButtonPanel.add(cancelButton);

		// Outer button panel
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

		gridY = 0;

		gbc.gridx = 0;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(controlPanel, gbc);
		mainPanel.add(controlPanel);

		gbc.gridx = 0;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(3, 0, 0, 0);
		gridBag.setConstraints(buttonPanel, gbc);
		mainPanel.add(buttonPanel);

		// Add commands to action map
		KeyAction.create(mainPanel, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,
						 KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), Command.CLOSE, this);


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
				onClose();
			}
		});

		// Prevent dialog from being resized
		setResizable(false);

		// Resize dialog to its preferred size
		pack();

		// Set location of dialog box
		if (location == null)
			location = GuiUtils.getComponentLocation(this, owner);
		setLocation(location);

		// Set default button
		getRootPane().setDefaultButton(okButton);

		// Show dialog
		setVisible(true);

	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static DateFormat showDialog(Component    parent,
										String       titleStr,
										DateFormat   dateFormat,
										List<String> names)
	{
		return new DateFormatDialog(GuiUtils.getWindow(parent), titleStr, dateFormat, names).
																						getDateFormat();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : ActionListener interface
////////////////////////////////////////////////////////////////////////

	public void actionPerformed(ActionEvent event)
	{
		String command = event.getActionCommand();

		if (command.equals(Command.HELP))
			onHelp();

		else if (command.equals(Command.ACCEPT))
			onAccept();

		else if (command.equals(Command.CLOSE))
			onClose();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	private DateFormat getDateFormat()
	{
		return (accepted ? new DateFormat(nameField.getText(), patternField.getText())
						 : null);
	}

	//------------------------------------------------------------------

	private void validateUserInput()
		throws AppException
	{
		// Name
		try
		{
			String str = nameField.getText();
			if (str.trim().isEmpty())
				throw new AppException(ErrorId.NO_NAME);
			if (names.contains(str))
				throw new AppException(ErrorId.DUPLICATE_NAME, str);
			if (str.contains(DateFormat.SEPARATOR))
				throw new AppException(ErrorId.SEPARATOR_NOT_ALLOWED);
		}
		catch (AppException e)
		{
			GuiUtils.setFocus(nameField);
			throw e;
		}

		// Pattern
		try
		{
			String str = patternField.getText();
			if (str.trim().isEmpty())
				throw new AppException(ErrorId.NO_PATTERN);
			if (str.contains(DateFormat.SEPARATOR))
				throw new AppException(ErrorId.SEPARATOR_NOT_ALLOWED);
		}
		catch (AppException e)
		{
			GuiUtils.setFocus(patternField);
			throw e;
		}

		// Date format
		try
		{
			new DateFormat(nameField.getText(), patternField.getText());
		}
		catch (DateFormat.ParseException e)
		{
			GuiUtils.setFocus(patternField);
			patternField.setCaretPosition(e.getIndex());
			throw new AppException(e.getId());
		}
	}

	//------------------------------------------------------------------

	private void closeHelpDialog()
	{
		helpDialog.close();
		helpDialog = null;
		helpButton.setEnabled(true);
	}

	//------------------------------------------------------------------

	private void onHelp()
	{
		if (helpDialog == null)
		{
			helpButton.setEnabled(false);
			helpDialog = new HelpDialog();
		}
	}

	//------------------------------------------------------------------

	private void onAccept()
	{
		try
		{
			validateUserInput();
			accepted = true;
			onClose();
		}
		catch (AppException e)
		{
			JOptionPane.showMessageDialog(this, e, App.SHORT_NAME, JOptionPane.ERROR_MESSAGE);
		}
	}

	//------------------------------------------------------------------

	private void onClose()
	{
		location = getLocation();
		if (helpDialog != null)
			helpDialogLocation = helpDialog.getLocation();
		setVisible(false);
		dispose();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class fields
////////////////////////////////////////////////////////////////////////

	private static	Point	location;
	private static	Point	helpDialogLocation;

////////////////////////////////////////////////////////////////////////
//  Instance fields
////////////////////////////////////////////////////////////////////////

	private	List<String>	names;
	private	boolean			accepted;
	private	JButton			helpButton;
	private	JTextField		nameField;
	private	JTextField		patternField;
	private	HelpDialog		helpDialog;

}

//----------------------------------------------------------------------
