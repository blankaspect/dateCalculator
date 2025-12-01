/*====================================================================*\

CompoundDateField.java

Compound date field class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.datecalculator;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import java.util.Calendar;
import java.util.EnumMap;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import uk.blankaspect.common.misc.ModernCalendar;

import uk.blankaspect.common.number.NumberUtils;

import uk.blankaspect.common.string.StringUtils;

import uk.blankaspect.ui.swing.container.DateSelectionPanel;

import uk.blankaspect.ui.swing.font.FontUtils;

import uk.blankaspect.ui.swing.label.FLabel;

import uk.blankaspect.ui.swing.misc.GuiUtils;

import uk.blankaspect.ui.swing.textfield.IntegerField;

//----------------------------------------------------------------------


// COMPOUND DATE FIELD CLASS


class CompoundDateField
	extends JPanel
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	int	MIN_YEAR	= DateSelectionPanel.MIN_YEAR;
	private static final	int	MAX_YEAR	= DateSelectionPanel.MAX_YEAR;

	private static final	int	SEPARATOR_MARGIN	= 1;

	private static final	char[]	SEPARATOR_CHARS	=   { '\u2013', '\u2212' };

////////////////////////////////////////////////////////////////////////
//  Enumerated types
////////////////////////////////////////////////////////////////////////


	// DATE-COMPONENT FIELD KEY


	enum FieldKey
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		YEAR    ("year",  4),
		MONTH   ("month", 2),
		DAY     ("day",   2);

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private FieldKey(String key,
						 int    length)
		{
			this.key = key;
			this.length = length;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public String toString()
		{
			return key;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		public int getLength()
		{
			return length;
		}

		//--------------------------------------------------------------

		public String getTitle()
		{
			return StringUtils.firstCharToUpperCase(key);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	String	key;
		private	int		length;

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Member interfaces
////////////////////////////////////////////////////////////////////////


	// OBSERVER INTERFACE


	interface Observer
	{

	////////////////////////////////////////////////////////////////////
	//  Methods
	////////////////////////////////////////////////////////////////////

		void notifyChanged(CompoundDateField source);

		//--------------------------------------------------------------

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


	// INVALID DATE EXCEPTION CLASS


	public static class InvalidDateException
		extends Exception
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private InvalidDateException(FieldKey key)
		{
			this.key = key;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		public FieldKey getKey()
		{
			return key;
		}

		//--------------------------------------------------------------

		public String getString()
		{
			return key.toString();
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	FieldKey	key;

	}

	//==================================================================


	// DATE OUT OF BOUNDS EXCEPTION CLASS


	public static class DateOutOfBoundsException
		extends Exception
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private DateOutOfBoundsException(FieldKey key,
										 int      minValue,
										 int      maxValue)
		{
			this.key = key;
			this.minValue = minValue;
			this.maxValue = maxValue;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		public FieldKey getKey()
		{
			return key;
		}

		//--------------------------------------------------------------

		public String[] getStrings()
		{
			return new String[] { key.toString(), Integer.toString(minValue), Integer.toString(maxValue) };
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	FieldKey	key;
		private	int			minValue;
		private	int			maxValue;

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Member classes : inner classes
////////////////////////////////////////////////////////////////////////


	// DATE-COMPONENT FIELD CLASS


	private class DateComponentField
		extends IntegerField.Unsigned
		implements DocumentListener
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private DateComponentField(FieldKey key)
		{
			super(key.getLength());
			this.key = key;
			AppFont.TEXT_FIELD.apply(this);
			GuiUtils.setTextComponentMargins(this);
			getDocument().addDocumentListener(this);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : DocumentListener interface
	////////////////////////////////////////////////////////////////////

		public void changedUpdate(DocumentEvent event)
		{
			// do nothing
		}

		//--------------------------------------------------------------

		public void insertUpdate(DocumentEvent event)
		{
			if ((event.getOffset() + event.getLength() >= key.getLength()) && isFocusOwner())
			{
				switch (key)
				{
					case YEAR:
						getField(FieldKey.MONTH).requestFocusInWindow();
						break;

					case MONTH:
						getField(FieldKey.DAY).requestFocusInWindow();
						break;

					case DAY:
						// do nothing
						break;
				}
			}

			if (observer != null)
				observer.notifyChanged(CompoundDateField.this);
		}

		//--------------------------------------------------------------

		public void removeUpdate(DocumentEvent event)
		{
			if (observer != null)
				observer.notifyChanged(CompoundDateField.this);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public void setValue(int value)
		{
			setText((value <= 0) ? null : NumberUtils.uIntToDecString(value, getColumns(), '0'));
		}

		//--------------------------------------------------------------

		@Override
		protected int getColumnWidth()
		{
			return FontUtils.getCharWidth('0', getFontMetrics(getFont())) + 1;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	FieldKey	key;

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public CompoundDateField()
	{
		this(0, 0, 0);
	}

	//------------------------------------------------------------------

	public CompoundDateField(int year,
							 int month,
							 int day)
	{
		// Set layout
		GridBagLayout gridBag = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();
		setLayout(gridBag);

		// Date-component fields
		String separatorString = Character.toString(getSeparatorChar(AppFont.MAIN.getFont()));
		fields = new EnumMap<>(FieldKey.class);
		int gridX = 0;
		for (FieldKey key : FieldKey.values())
		{
			// Separator
			if (key != FieldKey.YEAR)
			{
				JLabel separatorLabel = new FLabel(separatorString);

				gbc.gridx = gridX++;
				gbc.gridy = 0;
				gbc.gridwidth = 1;
				gbc.gridheight = 1;
				gbc.weightx = 0.0;
				gbc.weighty = 0.0;
				gbc.anchor = GridBagConstraints.LINE_START;
				gbc.fill = GridBagConstraints.NONE;
				gbc.insets = new Insets(0, SEPARATOR_MARGIN, 0, SEPARATOR_MARGIN);
				gridBag.setConstraints(separatorLabel, gbc);
				add(separatorLabel);
			}

			// Field
			DateComponentField field = new DateComponentField(key);
			field.setToolTipText(key.getTitle());
			fields.put(key, field);

			gbc.gridx = gridX++;
			gbc.gridy = 0;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.weightx = 0.0;
			gbc.weighty = 0.0;
			gbc.anchor = GridBagConstraints.LINE_START;
			gbc.fill = GridBagConstraints.NONE;
			gbc.insets = new Insets(0, 0, 0, 0);
			gridBag.setConstraints(field, gbc);
			add(field);
		}

		// Set date
		setDate(year, month, day);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static char getSeparatorChar(Font font)
	{
		for (char ch : SEPARATOR_CHARS)
		{
			if (font.canDisplay(ch))
				return ch;
		}
		return '-';
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	@Override
	public boolean requestFocusInWindow()
	{
		return GuiUtils.setFocus(getField(FieldKey.YEAR));
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public IntegerField.Unsigned getField(FieldKey key)
	{
		return fields.get(key);
	}

	//------------------------------------------------------------------

	/**
	 * @throws NumberFormatException
	 */

	public int getYear()
	{
		return getField(FieldKey.YEAR).getValue();
	}

	//------------------------------------------------------------------

	/**
	 * @throws NumberFormatException
	 */

	public int getMonth()
	{
		return getField(FieldKey.MONTH).getValue();
	}

	//------------------------------------------------------------------

	/**
	 * @throws NumberFormatException
	 */

	public int getDay()
	{
		return getField(FieldKey.DAY).getValue();
	}

	//------------------------------------------------------------------

	public void setDate(int year,
						int month,
						int day)
	{
		getField(FieldKey.YEAR).setValue(year);
		getField(FieldKey.MONTH).setValue(month);
		getField(FieldKey.DAY).setValue(day);
	}

	//------------------------------------------------------------------

	public boolean hasDate()
	{
		for (FieldKey key : fields.keySet())
		{
			if (getField(key).isEmpty())
				return false;
		}
		return true;
	}

	//------------------------------------------------------------------

	public void setObserver(CompoundDateField.Observer observer)
	{
		this.observer = observer;
	}

	//------------------------------------------------------------------

	public void validateDate()
		throws DateOutOfBoundsException, InvalidDateException
	{
		FieldKey key = null;
		int year = 0;
		int month = 0;
		int day = 0;
		try
		{
			key = FieldKey.YEAR;
			year = getField(key).getValue();
			if ((year < MIN_YEAR) || (year > MAX_YEAR))
				throw new DateOutOfBoundsException(key, MIN_YEAR, MAX_YEAR);

			key = FieldKey.MONTH;
			month = getField(key).getValue() - 1;

			key = FieldKey.DAY;
			day = getField(key).getValue();
		}
		catch (NumberFormatException e)
		{
			throw new InvalidDateException(key);
		}

		Calendar calendar = new ModernCalendar(year, 0, 1);
		int minValue = calendar.getActualMinimum(Calendar.MONTH);
		int maxValue = calendar.getActualMaximum(Calendar.MONTH);
		if ((month < minValue) || (month > maxValue))
			throw new DateOutOfBoundsException(FieldKey.MONTH, minValue + 1, maxValue + 1);

		calendar = new ModernCalendar(year, month, 1);
		minValue = calendar.getActualMinimum(Calendar.DAY_OF_MONTH);
		maxValue = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
		if ((day < minValue) || (day > maxValue))
			throw new DateOutOfBoundsException(FieldKey.DAY, minValue, maxValue);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	Map<FieldKey, DateComponentField>	fields;
	private	Observer							observer;

}

//----------------------------------------------------------------------
