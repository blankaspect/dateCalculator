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

import uk.blankaspect.common.gui.GuiUtils;

//----------------------------------------------------------------------


// RESULT TEXT FIELD CLASS


class ResultField
	extends JTextField
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	int	VERTICAL_MARGIN		= 2;
	private static final	int	HORIZONTAL_MARGIN	= 4;

	private static final	Color	BACKGROUND_COLOUR	= new Color(252, 244, 200);
	private static final	Color	BORDER_COLOUR		= new Color(240, 216, 160);

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public ResultField(int numColumns)
	{
		// Call superclass constructor
		super(numColumns);

		// Set attributes
		AppFont.TEXT_FIELD.apply(this);
		GuiUtils.setPaddedLineBorder(this, VERTICAL_MARGIN, HORIZONTAL_MARGIN, BORDER_COLOUR);
		setBackground(BACKGROUND_COLOUR);
		setEditable(false);
		setFocusable(false);
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
