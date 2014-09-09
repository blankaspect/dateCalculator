/*====================================================================*\

DateFormatComboBox.java

Date-format combo box class.

\*====================================================================*/


// IMPORTS


import java.util.List;

import javax.swing.JComboBox;

import uk.org.blankaspect.gui.ComboBoxRenderer;

import uk.org.blankaspect.util.DateFormat;

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
        implements ComboBoxRenderer.TooltipSource
    {

    ////////////////////////////////////////////////////////////////////
    //  Constructors
    ////////////////////////////////////////////////////////////////////

        private ListItem( DateFormat dateFormat )
        {
            this.dateFormat = dateFormat;
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance methods : ComboBoxRenderer.TooltipSource interface
    ////////////////////////////////////////////////////////////////////

        public String getTooltip( )
        {
            return dateFormat.getPattern( );
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance methods : overriding methods
    ////////////////////////////////////////////////////////////////////

        @Override
        public String toString( )
        {
            return dateFormat.getName( );
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance variables
    ////////////////////////////////////////////////////////////////////

        private DateFormat  dateFormat;

    }

    //==================================================================

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

    public DateFormatComboBox( )
    {
        AppFont.COMBO_BOX.apply( this );
        setRenderer( new ComboBoxRenderer<>( this ) );
        addItems( );
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

    public DateFormat getSelectedFormat( )
    {
        int index = getSelectedIndex( );
        return ( (index < 0) ? new DateFormat( ) : getItemAt( index ).dateFormat );
    }

    //------------------------------------------------------------------

    public void reset( )
    {
        removeAllItems( );
        addItems( );
    }

    //------------------------------------------------------------------

    private void addItems( )
    {
        List<DateFormat> dateFormats = AppConfig.getInstance( ).getDateFormats( );
        if ( dateFormats.isEmpty( ) )
            dateFormats.add( DateFormat.DEFAULT_FORMAT );

        for ( DateFormat dateFormat : dateFormats )
            addItem( new ListItem( dateFormat ) );
    }

    //------------------------------------------------------------------

}

//----------------------------------------------------------------------
