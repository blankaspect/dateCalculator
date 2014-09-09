/*====================================================================*\

ResultField.java

Result text field class.

\*====================================================================*/


// IMPORTS


import java.awt.Color;

import javax.swing.JTextField;

import uk.org.blankaspect.gui.GuiUtilities;

//----------------------------------------------------------------------


// RESULT TEXT FIELD CLASS


class ResultField
    extends JTextField
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

    private static final    int VERTICAL_MARGIN     = 2;
    private static final    int HORIZONTAL_MARGIN   = 4;

    private static final    Color   BACKGROUND_COLOUR   = new Color( 252, 240, 200 );
    private static final    Color   BORDER_COLOUR       = new Color( 240, 192, 128 );

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

    public ResultField( int numColumns )
    {
        super( numColumns );
        AppFont.TEXT_FIELD.apply( this );
        GuiUtilities.setPaddedLineBorder( this, VERTICAL_MARGIN, HORIZONTAL_MARGIN, BORDER_COLOUR );
        setBackground( BACKGROUND_COLOUR );
        setEditable( false );
        setFocusable( false );
    }

    //------------------------------------------------------------------

}

//----------------------------------------------------------------------
