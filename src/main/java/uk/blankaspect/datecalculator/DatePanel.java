/*====================================================================*\

DatePanel.java

Date panel class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.datecalculator;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Calendar;

import javax.swing.JButton;
import javax.swing.JPanel;

import uk.blankaspect.common.gui.DateSelectionDialog;
import uk.blankaspect.common.gui.FButton;

import uk.blankaspect.common.misc.Date;
import uk.blankaspect.common.misc.ModernCalendar;

//----------------------------------------------------------------------


// DATE PANEL CLASS


class DatePanel
	extends JPanel
	implements ActionListener
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	Insets	DATE_BUTTON_MARGINS		= new Insets(2, 4, 2, 4);
	private static final	Insets	TODAY_BUTTON_MARGINS	= DATE_BUTTON_MARGINS;

	private static final	String	TODAY_STR	= "Today";

	// Commands
	private interface Command
	{
		String	SELECT_DATE			= "selectDate";
		String	SET_DATE_TO_TODAY	= "setDateToToday";
	}

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public DatePanel(String key,
					 String selectTooltipString,
					 String todayTooltipString)
	{
		// Initialise instance fields
		this.key = key;
		selectedDate = new Date(new ModernCalendar());

		// Set layout
		GridBagLayout gridBag = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();
		setLayout(gridBag);

		// Initialise local variables
		int gridX = 0;

		// Field: date
		dateField = new CompoundDateField();

		gbc.gridx = gridX++;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(dateField, gbc);
		add(dateField);

		// Button: select date
		selectButton = new JButton(AppIcon.CALENDAR);
		selectButton.setToolTipText(selectTooltipString);
		selectButton.setMargin(DATE_BUTTON_MARGINS);
		selectButton.setActionCommand(Command.SELECT_DATE);
		selectButton.addActionListener(this);

		gbc.gridx = gridX++;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 6, 0, 0);
		gridBag.setConstraints(selectButton, gbc);
		add(selectButton);

		// Button: today
		JButton todayButton = new FButton(TODAY_STR);
		todayButton.setMargin(TODAY_BUTTON_MARGINS);
		todayButton.setToolTipText(todayTooltipString);
		todayButton.setActionCommand(Command.SET_DATE_TO_TODAY);
		todayButton.addActionListener(this);

		gbc.gridx = gridX++;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 6, 0, 0);
		gridBag.setConstraints(todayButton, gbc);
		add(todayButton);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : ActionListener interface
////////////////////////////////////////////////////////////////////////

	public void actionPerformed(ActionEvent event)
	{
		String command = event.getActionCommand();

		if (command.equals(Command.SELECT_DATE))
			onSelectDate();

		else if (command.equals(Command.SET_DATE_TO_TODAY))
			onSetDateToToday();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	@Override
	public boolean requestFocusInWindow()
	{
		return dateField.requestFocusInWindow();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public CompoundDateField getDateField()
	{
		return dateField;
	}

	//------------------------------------------------------------------

	public Calendar getDate()
	{
		return new ModernCalendar(dateField.getYear(), dateField.getMonth() - 1, dateField.getDay());
	}

	//------------------------------------------------------------------

	public boolean hasDate()
	{
		return dateField.hasDate();
	}

	//------------------------------------------------------------------

	public void setObserver(CompoundDateField.Observer observer)
	{
		dateField.setObserver(observer);
	}

	//------------------------------------------------------------------

	private void onSelectDate()
	{
		Date date = null;
		try
		{
			dateField.validateDate();
			date = new Date(getDate());
		}
		catch (Exception e)
		{
			date = selectedDate;
		}

		AppConfig config = AppConfig.INSTANCE;
		date = DateSelectionDialog.showDialog(this, selectButton.getLocationOnScreen(), date,
											  config.getFirstDayOfWeek(), config.isShowAdjacentMonths(),
											  key);
		if (date != null)
		{
			selectedDate = date;
			dateField.setDate(date.year, date.month + 1, date.day + 1);
		}
	}

	//------------------------------------------------------------------

	private void onSetDateToToday()
	{
		Calendar date = new ModernCalendar();
		dateField.setDate(date.get(Calendar.YEAR), date.get(Calendar.MONTH) + 1,
						  date.get(Calendar.DAY_OF_MONTH));
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance fields
////////////////////////////////////////////////////////////////////////

	private	String				key;
	private	Date				selectedDate;
	private	CompoundDateField	dateField;
	private	JButton				selectButton;

}

//----------------------------------------------------------------------
