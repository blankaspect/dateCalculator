/*====================================================================*\

ResultField.java

Result text field class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.datecalculator;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.Color;

import javax.swing.JTextField;

import uk.blankaspect.ui.swing.misc.GuiUtils;

//----------------------------------------------------------------------


// RESULT TEXT FIELD CLASS


class ResultField
	extends JTextField
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	int		VERTICAL_MARGIN		= 3;
	private static final	int		HORIZONTAL_MARGIN	= 4;

	private static final	Color	BACKGROUND_COLOUR	= new Color(252, 248, 232);
	private static final	Color	BORDER_COLOUR		= new Color(224, 208, 176);

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public ResultField(
		int	numColumns)
	{
		// Call superclass constructor
		super(numColumns);

		// Set properties
		AppFont.TEXT_FIELD.apply(this);
		GuiUtils.setPaddedLineBorder(this, VERTICAL_MARGIN, HORIZONTAL_MARGIN, BORDER_COLOUR);
		setBackground(BACKGROUND_COLOUR);
		setEditable(false);
		setFocusable(false);
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
