/*====================================================================*\

DifferencePanel.java

Difference panel class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.datecalculator;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Calendar;
import java.util.TimeZone;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import uk.blankaspect.common.exception.AppException;

import uk.blankaspect.common.gui.FButton;
import uk.blankaspect.common.gui.FLabel;
import uk.blankaspect.common.gui.GuiUtils;

import uk.blankaspect.common.misc.Time;

//----------------------------------------------------------------------


// DIFFERENCE PANEL CLASS


class DifferencePanel
	extends JPanel
	implements ActionListener, CompoundDateField.Observer
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	int	MILLISECONDS_PER_DAY	= 24 * 60 * 60 * 1000;

	private static final	int	DAYS_RESULT_FIELD_NUM_COLUMNS	= 16;

	private static final	Insets	COPY_BUTTON_MARGINS	= new Insets(2, 4, 2, 4);

	private static final	String	START_DATE_STR	= "Start date";
	private static final	String	END_DATE_STR	= "End date";
	private static final	String	RESULT_STR		= "Result";
	private static final	String	COPY_STR		= "Copy";
	private static final	String	SUBTRACT_STR	= "Subtract";

	private static final	String	SELECT_START_DATE_TOOLTIP_STR	= "Select start date";
	private static final	String	TODAY_START_TOOLTIP_STR			= "Set start date to current date";
	private static final	String	SELECT_END_DATE_TOOLTIP_STR		= "Select end date";
	private static final	String	TODAY_END_TOOLTIP_STR			= "Set end date to current date";
	private static final	String	COPY_TOOLTIP_STR				= "Copy result to clipboard";
	private static final	String	SUBTRACT_TOOLTIP_STR			= "Calculate number of days from " +
																		"start date to end date";

	private static final	String	KEY	= DifferencePanel.class.getCanonicalName();

	// Commands
	private interface Command
	{
		String	COPY_RESULT	= "copyResult";
		String	SUBTRACT	= "subtract";
	}

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

		INVALID_DATE1
		("The %1 of date 1 is invalid."),

		INVALID_DATE2
		("The %1 of date 2 is invalid."),

		DATE1_OUT_OF_BOUNDS
		("The %1 of date 1 must be between %2 and %3."),

		DATE2_OUT_OF_BOUNDS
		("The %1 of date 2 must be between %2 and %3.");

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
//  Constructors
////////////////////////////////////////////////////////////////////////

	public DifferencePanel()
	{

		//----  Control panel

		GridBagLayout gridBag = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();

		JPanel controlPanel = new JPanel(gridBag);
		GuiUtils.setPaddedLineBorder(controlPanel);

		int keyIndex = 0;
		int gridY = 0;

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
		startDatePanel = new DatePanel(KEY + keyIndex++, SELECT_START_DATE_TOOLTIP_STR,
									   TODAY_START_TOOLTIP_STR);
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

		// Label: end date
		JLabel date2Label = new FLabel(END_DATE_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(date2Label, gbc);
		controlPanel.add(date2Label);

		// Panel: end date
		endDatePanel = new DatePanel(KEY + keyIndex++, SELECT_END_DATE_TOOLTIP_STR,
									 TODAY_END_TOOLTIP_STR);
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

		// Label: result
		JLabel resultLabel = new FLabel(RESULT_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(resultLabel, gbc);
		controlPanel.add(resultLabel);

		// Panel: result
		JPanel resultPanel = new JPanel(gridBag);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(resultPanel, gbc);
		controlPanel.add(resultPanel);

		// Field: result
		resultField = new ResultField(DAYS_RESULT_FIELD_NUM_COLUMNS);

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(resultField, gbc);
		resultPanel.add(resultField);

		// Button: copy result
		JButton copyResultButton = new FButton(COPY_STR);
		copyResultButton.setMargin(COPY_BUTTON_MARGINS);
		copyResultButton.setToolTipText(COPY_TOOLTIP_STR);
		copyResultButton.setActionCommand(Command.COPY_RESULT);
		copyResultButton.addActionListener(this);

		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 6, 0, 0);
		gridBag.setConstraints(copyResultButton, gbc);
		resultPanel.add(copyResultButton);


		//----  Button panel

		JPanel buttonPanel = new JPanel(new GridLayout(1, 0, 0, 0));
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));

		// Button: subtract
		subtractButton = new FButton(SUBTRACT_STR);
		subtractButton.setToolTipText(SUBTRACT_TOOLTIP_STR);
		subtractButton.setActionCommand(Command.SUBTRACT);
		subtractButton.addActionListener(this);
		buttonPanel.add(subtractButton);

		updateButtons();


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

	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : ActionListener interface
////////////////////////////////////////////////////////////////////////

	public void actionPerformed(ActionEvent event)
	{
		try
		{
			String command = event.getActionCommand();

			if (command.equals(Command.COPY_RESULT))
				onCopyResult();

			else if (command.equals(Command.SUBTRACT))
				onSubtract();
		}
		catch (AppException e)
		{
			App.INSTANCE.showErrorMessage(App.SHORT_NAME, e);
		}
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : CompoundDateField.Observer interface
////////////////////////////////////////////////////////////////////////

	public void notifyChanged(CompoundDateField source)
	{
		updateButtons();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	private void updateButtons()
	{
		boolean enabled = startDatePanel.hasDate() && endDatePanel.hasDate();
		subtractButton.setEnabled(enabled);
	}

	//------------------------------------------------------------------

	private void onCopyResult()
		throws AppException
	{
		Utils.putClipboardText(resultField.getText());
	}

	//------------------------------------------------------------------

	private void onSubtract()
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
			throw new AppException(ErrorId.INVALID_DATE1, e.getString());
		}
		catch (CompoundDateField.DateOutOfBoundsException e)
		{
			GuiUtils.setFocus(startDatePanel.getDateField().getField(e.getKey()));
			throw new AppException(ErrorId.DATE1_OUT_OF_BOUNDS, e.getStrings());
		}

		// Validate end date
		try
		{
			endDatePanel.getDateField().validateDate();
		}
		catch (CompoundDateField.InvalidDateException e)
		{
			GuiUtils.setFocus(endDatePanel.getDateField().getField(e.getKey()));
			throw new AppException(ErrorId.INVALID_DATE2, e.getString());
		}
		catch (CompoundDateField.DateOutOfBoundsException e)
		{
			GuiUtils.setFocus(endDatePanel.getDateField().getField(e.getKey()));
			throw new AppException(ErrorId.DATE2_OUT_OF_BOUNDS, e.getStrings());
		}

		// Get date 1
		Calendar date1 = startDatePanel.getDate();
		date1.setTimeZone(TimeZone.getTimeZone(Time.UTC_TIME_ZONE_STR));
		long time1 = date1.getTimeInMillis();

		// Get date 2
		Calendar date2 = endDatePanel.getDate();
		date2.setTimeZone(TimeZone.getTimeZone(Time.UTC_TIME_ZONE_STR));
		long time2 = date2.getTimeInMillis();

		// Calculate difference and display it in result field
		resultField.setText(Long.toString(Math.round((double)(time2 - time1) /
																		(double)MILLISECONDS_PER_DAY)));
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance fields
////////////////////////////////////////////////////////////////////////

	private	DatePanel	startDatePanel;
	private	DatePanel	endDatePanel;
	private	ResultField	resultField;
	private	JButton		subtractButton;

}

//----------------------------------------------------------------------
