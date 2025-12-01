/*====================================================================*\

IntervalPanel.java

Class: interval panel.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.datecalculator;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Window;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Calendar;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import uk.blankaspect.common.date.DateFormat;

import uk.blankaspect.common.exception.AppException;

import uk.blankaspect.common.misc.MaxValueMap;

import uk.blankaspect.common.property.Property;

import uk.blankaspect.common.string.StringUtils;

import uk.blankaspect.ui.swing.button.FButton;
import uk.blankaspect.ui.swing.button.FixedWidthRadioButton;

import uk.blankaspect.ui.swing.dialog.NonEditableTextAreaDialog;

import uk.blankaspect.ui.swing.label.FLabel;

import uk.blankaspect.ui.swing.misc.GuiUtils;

import uk.blankaspect.ui.swing.spinner.FIntegerSpinner;

import uk.blankaspect.ui.swing.window.WindowUtils;

//----------------------------------------------------------------------


// CLASS: INTERVAL PANEL


class IntervalPanel
	extends JPanel
	implements ActionListener, CompoundDateField.Observer, Property.IObserver
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	int		MIN_INTERVAL	= 1;
	private static final	int		MAX_INTERVAL	= 9999;

	private static final	int		MIN_NUM_INTERVALS	= 1;
	private static final	int		MAX_NUM_INTERVALS	= 9999;

	private static final	int		MIN_NUM_DAYS	= 1;
	private static final	int		MAX_NUM_DAYS	= 9999;

	private static final	int		INTERVAL_FIELD_LENGTH		= 4;
	private static final	int		NUM_INTERVALS_FIELD_LENGTH	= 4;
	private static final	int		NUM_DAYS_FIELD_LENGTH		= 4;

	private static final	String	START_DATE_STR		= "Start date";
	private static final	String	INTERVAL_STR		= "Interval";
	private static final	String	DAYS_STR			= "days";
	private static final	String	NUM_INTERVALS_STR	= "Number of intervals";
	private static final	String	NUM_DAYS_STR		= "Number of days";
	private static final	String	END_DATE_STR		= "End date";
	private static final	String	DATE_FORMAT_STR		= "Date format";
	private static final	String	GENERATE_STR		= "Generate";
	private static final	String	INTERVALS_STR		= "Dates at intervals";

	private static final	String	SELECT_START_DATE_TOOLTIP_STR	= "Select start date";
	private static final	String	TODAY_START_TOOLTIP_STR			= "Set start date to current date";
	private static final	String	SELECT_END_DATE_TOOLTIP_STR		= "Select end date";
	private static final	String	TODAY_END_TOOLTIP_STR			= "Set end date to current date";
	private static final	String	GENERATE_TOOLTIP_STR			= "Generate list of dates on clipboard";

	private static final	String	KEY	= IntervalPanel.class.getCanonicalName();

	private enum Mode
	{
		NUM_INTERVALS,
		NUM_DAYS,
		END_DATE
	}

	// Commands
	private interface Command
	{
		String	SET_MODE	= "setMode.";
		String	GENERATE	= "generate";
	}

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	Mode				mode;
	private	String				dateFormatKey;
	private	DatePanel			startDatePanel;
	private	FIntegerSpinner		intervalSpinner;
	private	FIntegerSpinner		numDaysSpinner;
	private	FIntegerSpinner		numIntervalsSpinner;
	private	DatePanel			endDatePanel;
	private	DateFormatComboBox	dateFormatComboBox;
	private	JButton				generateButton;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public IntervalPanel()
	{
		// Initialise instance variables
		mode = Mode.NUM_INTERVALS;


		//----  Control panel

		GridBagLayout gridBag = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();

		JPanel controlPanel = new JPanel(gridBag);
		GuiUtils.setPaddedLineBorder(controlPanel);

		int keyIndex = 0;
		int gridY = 0;

		// Reset fixed-width radio buttons
		RadioButton.reset();

		// Label: start date
		JLabel startDateLabel = new FLabel(START_DATE_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(startDateLabel, gbc);
		controlPanel.add(startDateLabel);

		// Panel: start date
		startDatePanel = new DatePanel(KEY + keyIndex++, SELECT_START_DATE_TOOLTIP_STR, TODAY_START_TOOLTIP_STR);
		startDatePanel.setObserver(this);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(startDatePanel, gbc);
		controlPanel.add(startDatePanel);

		// Label: interval
		JLabel intervalLabel = new FLabel(INTERVAL_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(intervalLabel, gbc);
		controlPanel.add(intervalLabel);

		// Panel: interval
		JPanel intervalPanel = new JPanel(gridBag);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(intervalPanel, gbc);
		controlPanel.add(intervalPanel);

		// Spinner: interval
		intervalSpinner = new FIntegerSpinner(MIN_INTERVAL, MIN_INTERVAL, MAX_INTERVAL, INTERVAL_FIELD_LENGTH);

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(intervalSpinner, gbc);
		intervalPanel.add(intervalSpinner);

		// Label: days
		JLabel daysLabel = new FLabel(DAYS_STR);

		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 6, 0, 0);
		gridBag.setConstraints(daysLabel, gbc);
		intervalPanel.add(daysLabel);

		// Radio button: number of intervals
		ButtonGroup buttonGroup = new ButtonGroup();

		JRadioButton numIntervalsRadioButton = new RadioButton(NUM_INTERVALS_STR);
		buttonGroup.add(numIntervalsRadioButton);
		numIntervalsRadioButton.setSelected(true);
		numIntervalsRadioButton.setActionCommand(Command.SET_MODE + Mode.NUM_INTERVALS);
		numIntervalsRadioButton.addActionListener(this);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(numIntervalsRadioButton, gbc);
		controlPanel.add(numIntervalsRadioButton);

		// Spinner: number of intervals
		numIntervalsSpinner = new FIntegerSpinner(MIN_NUM_INTERVALS, MIN_NUM_INTERVALS, MAX_NUM_INTERVALS,
												  NUM_INTERVALS_FIELD_LENGTH);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(numIntervalsSpinner, gbc);
		controlPanel.add(numIntervalsSpinner);

		// Radio button: number of days
		JRadioButton numDaysRadioButton = new RadioButton(NUM_DAYS_STR);
		buttonGroup.add(numDaysRadioButton);
		numDaysRadioButton.setActionCommand(Command.SET_MODE + Mode.NUM_DAYS);
		numDaysRadioButton.addActionListener(this);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(numDaysRadioButton, gbc);
		controlPanel.add(numDaysRadioButton);

		// Spinner: number of days
		numDaysSpinner = new FIntegerSpinner(MIN_NUM_DAYS, MIN_NUM_DAYS, MAX_NUM_DAYS, NUM_DAYS_FIELD_LENGTH);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(numDaysSpinner, gbc);
		controlPanel.add(numDaysSpinner);

		// Radio button: end date
		JRadioButton endDateRadioButton = new RadioButton(END_DATE_STR);
		buttonGroup.add(endDateRadioButton);
		endDateRadioButton.setActionCommand(Command.SET_MODE + Mode.END_DATE);
		endDateRadioButton.addActionListener(this);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(endDateRadioButton, gbc);
		controlPanel.add(endDateRadioButton);

		// Panel: end date
		endDatePanel = new DatePanel(KEY + keyIndex++, SELECT_END_DATE_TOOLTIP_STR, TODAY_END_TOOLTIP_STR);
		endDatePanel.setObserver(this);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(endDatePanel, gbc);
		controlPanel.add(endDatePanel);

		// Label: date format
		JLabel dateFormatLabel = new FLabel(DATE_FORMAT_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(dateFormatLabel, gbc);
		controlPanel.add(dateFormatLabel);

		// Combo box: date format
		dateFormatComboBox = new DateFormatComboBox();

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(dateFormatComboBox, gbc);
		controlPanel.add(dateFormatComboBox);


		//----  Button panel

		JPanel buttonPanel = new JPanel(new GridLayout(1, 0, 8, 0));
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));

		// Button: generate
		generateButton = new FButton(GENERATE_STR);
		generateButton.setToolTipText(GENERATE_TOOLTIP_STR);
		generateButton.setActionCommand(Command.GENERATE);
		generateButton.addActionListener(this);
		buttonPanel.add(generateButton);

		// Update components
		updateMode();


		//----  Outer panel

		setLayout(gridBag);
		setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

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
		add(controlPanel);

		gbc.gridx = 0;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(3, 0, 0, 0);
		gridBag.setConstraints(buttonPanel, gbc);
		add(buttonPanel);

		// Update date-format key when date format is updated
		dateFormatKey = AppConfig.INSTANCE.addDateFormatObserver(this);

		// Update widths of radio buttons when this panel is added to a window
		WindowUtils.addRunOnAddedToWindow(this, RadioButton::update);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : ActionListener interface
////////////////////////////////////////////////////////////////////////

	@Override
	public void actionPerformed(
		ActionEvent	event)
	{
		try
		{
			String command = event.getActionCommand();

			if (command.startsWith(Command.SET_MODE))
				onSetMode(StringUtils.removePrefix(command, Command.SET_MODE));

			else if (command.equals(Command.GENERATE))
				onGenerate();
		}
		catch (AppException e)
		{
			DateCalculatorApp.INSTANCE.showErrorMessage(DateCalculatorApp.SHORT_NAME, e);
		}
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : CompoundDateField.Observer interface
////////////////////////////////////////////////////////////////////////

	@Override
	public void notifyChanged(
		CompoundDateField	source)
	{
		updateButtons();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : Property.IObserver interface
////////////////////////////////////////////////////////////////////////

	@Override
	public void propertyChanged(
		Property	property)
	{
		if (property.getKey().equals(dateFormatKey))
		{
			dateFormatComboBox.removeActionListener(this);
			dateFormatComboBox.reset();
			dateFormatComboBox.addActionListener(this);
		}
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	private void updateMode()
	{
		Window window = GuiUtils.getWindow(this);
		Component oldFocusOwner = (window == null) ? null : window.getFocusOwner();
		JComponent component = null;
		switch (mode)
		{
			case NUM_INTERVALS:
				component = numIntervalsSpinner;
				numIntervalsSpinner.setEnabled(true);
				numDaysSpinner.setEnabled(false);
				GuiUtils.setAllEnabled(endDatePanel, false);
				break;

			case NUM_DAYS:
				component = numDaysSpinner;
				numDaysSpinner.setEnabled(true);
				numIntervalsSpinner.setEnabled(false);
				GuiUtils.setAllEnabled(endDatePanel, false);
				break;

			case END_DATE:
				component = endDatePanel;
				GuiUtils.setAllEnabled(endDatePanel, true);
				numIntervalsSpinner.setEnabled(false);
				numDaysSpinner.setEnabled(false);
				break;
		}

		updateButtons();

		if ((window != null) && (window.getFocusOwner() != oldFocusOwner))
			GuiUtils.setFocus(component);
	}

	//------------------------------------------------------------------

	private void updateButtons()
	{
		boolean enabled = startDatePanel.hasDate() && ((mode != Mode.END_DATE) || endDatePanel.hasDate());
		generateButton.setEnabled(enabled);
	}

	//------------------------------------------------------------------

	private void onSetMode(
		String	key)
	{
		for (Mode m : Mode.values())
		{
			if (m.toString().equals(key))
			{
				mode = m;
				updateMode();
				break;
			}
		}
	}

	//------------------------------------------------------------------

	private void onGenerate()
		throws AppException
	{
		// Validate start date
		try
		{
			startDatePanel.getDateField().validateDate();
		}
		catch (CompoundDateField.InvalidDateException e)
		{
			GuiUtils.setFocus(startDatePanel.getDateField().getField(e.getKey()));
			throw new AppException(ErrorId.INVALID_START_DATE, e.getString());
		}
		catch (CompoundDateField.DateOutOfBoundsException e)
		{
			GuiUtils.setFocus(startDatePanel.getDateField().getField(e.getKey()));
			throw new AppException(ErrorId.START_DATE_OUT_OF_BOUNDS, e.getStrings());
		}

		// Get start date and interval
		Calendar date = startDatePanel.getDate();
		int interval = intervalSpinner.getIntValue();

		// Generate list of dates
		DateFormat dateFormat = dateFormatComboBox.getSelectedFormat();
		StringBuilder buffer = new StringBuilder(1024);
		switch (mode)
		{
			case NUM_INTERVALS:
			{
				int numIntervals = numIntervalsSpinner.getIntValue();
				for (int i = 0; i <= numIntervals; i++)
				{
					buffer.append(dateFormat.format(date));
					buffer.append('\n');
					date.add(Calendar.DATE, interval);
				}
				break;
			}

			case NUM_DAYS:
			{
				int numDays = numDaysSpinner.getIntValue();
				for (int i = 0; i <= numDays; i += interval)
				{
					buffer.append(dateFormat.format(date));
					buffer.append('\n');
					date.add(Calendar.DATE, interval);
				}
				break;
			}

			case END_DATE:
			{
				// Validate end date
				try
				{
					endDatePanel.getDateField().validateDate();
				}
				catch (CompoundDateField.InvalidDateException e)
				{
					GuiUtils.setFocus(endDatePanel.getDateField().getField(e.getKey()));
					throw new AppException(ErrorId.INVALID_END_DATE, e.getString());
				}
				catch (CompoundDateField.DateOutOfBoundsException e)
				{
					GuiUtils.setFocus(endDatePanel.getDateField().getField(e.getKey()));
					throw new AppException(ErrorId.END_DATE_OUT_OF_BOUNDS, e.getStrings());
				}

				// Test for start and end dates out of order
				Calendar endDate = endDatePanel.getDate();
				if (endDate.before(date))
					throw new AppException(ErrorId.DATES_OUT_OF_ORDER);

				// Generate list of dates
				while (!date.after(endDate))
				{
					buffer.append(dateFormat.format(date));
					buffer.append('\n');
					date.add(Calendar.DATE, interval);
				}
				break;
			}
		}

		// Display list of dates
		TextAreaDialog.showDialog(this, INTERVALS_STR, buffer.toString());
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Enumerated types
////////////////////////////////////////////////////////////////////////


	// ENUMERATION: ERROR IDENTIFIERS


	private enum ErrorId
		implements AppException.IId
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		INVALID_START_DATE
		("The %1 of the start date is invalid."),

		START_DATE_OUT_OF_BOUNDS
		("The %1 of the start date must be between %2 and %3."),

		INVALID_END_DATE
		("The %1 of the end date is invalid."),

		END_DATE_OUT_OF_BOUNDS
		("The %1 of the end date must be between %2 and %3."),

		DATES_OUT_OF_ORDER
		("The end date is before the start date.");

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	String	message;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private ErrorId(
			String	message)
		{
			this.message = message;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : AppException.IId interface
	////////////////////////////////////////////////////////////////////

		@Override
		public String getMessage()
		{
			return message;
		}

		//--------------------------------------------------------------

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


	// CLASS: RADIO BUTTON


	private static class RadioButton
		extends FixedWidthRadioButton
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	String	KEY	= RadioButton.class.getCanonicalName();

		private static final	Color	BACKGROUND_COLOUR	= new Color(252, 224, 128);

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private RadioButton(
			String	text)
		{
			super(text);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Class methods
	////////////////////////////////////////////////////////////////////

		private static void reset()
		{
			MaxValueMap.removeAll(KEY);
		}

		//--------------------------------------------------------------

		private static void update()
		{
			MaxValueMap.update(KEY);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public Color getBackground()
		{
			return isSelected() ? BACKGROUND_COLOUR : super.getBackground();
		}

		//--------------------------------------------------------------

		@Override
		protected String getKey()
		{
			return KEY;
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// CLASS: TEXT AREA DIALOG


	private static class TextAreaDialog
		extends NonEditableTextAreaDialog
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	int		NUM_COLUMNS	= 64;
		private static final	int		NUM_ROWS	= 20;

		private static final	String	KEY	= TextAreaDialog.class.getCanonicalName();

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private TextAreaDialog(
			Window	owner,
			String	title,
			String	text)
		{
			super(owner, title, KEY, NUM_COLUMNS, NUM_ROWS, text);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Class methods
	////////////////////////////////////////////////////////////////////

		private static TextAreaDialog showDialog(
			Component	parent,
			String		title,
			String		text)
		{
			return new TextAreaDialog(GuiUtils.getWindow(parent), title, text);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		protected void setTextAreaAttributes()
		{
			setCaretToStart();
		}

		//--------------------------------------------------------------

	}

	//==================================================================

}

//----------------------------------------------------------------------
