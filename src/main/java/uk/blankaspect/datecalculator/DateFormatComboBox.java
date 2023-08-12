/*====================================================================*\

DateFormatComboBox.java

Date-format combo box class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.datecalculator;

//----------------------------------------------------------------------


// IMPORTS


import java.util.List;

import javax.swing.JComboBox;

import uk.blankaspect.common.date.DateFormat;

import uk.blankaspect.ui.swing.combobox.ComboBoxRenderer;

//----------------------------------------------------------------------


// DATE-FORMAT COMBO BOX CLASS


class DateFormatComboBox
	extends JComboBox<DateFormatComboBox.ListItem>
{

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


	// LIST ITEM CLASS


	protected static class ListItem
		implements ComboBoxRenderer.ITooltipSource
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private ListItem(DateFormat dateFormat)
		{
			this.dateFormat = dateFormat;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : ComboBoxRenderer.ITooltipSource interface
	////////////////////////////////////////////////////////////////////

		@Override
		public String getTooltip()
		{
			return dateFormat.getPattern();
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public String toString()
		{
			return dateFormat.getName();
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	DateFormat	dateFormat;

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public DateFormatComboBox()
	{
		AppFont.COMBO_BOX.apply(this);
		setRenderer(new ComboBoxRenderer<>(this));
		addItems();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public DateFormat getSelectedFormat()
	{
		int index = getSelectedIndex();
		return ((index < 0) ? new DateFormat() : getItemAt(index).dateFormat);
	}

	//------------------------------------------------------------------

	public void reset()
	{
		removeAllItems();
		addItems();
	}

	//------------------------------------------------------------------

	private void addItems()
	{
		List<DateFormat> dateFormats = AppConfig.INSTANCE.getDateFormats();
		if (dateFormats.isEmpty())
			dateFormats.add(DateFormat.DEFAULT_FORMAT);

		for (DateFormat dateFormat : dateFormats)
			addItem(new ListItem(dateFormat));
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
