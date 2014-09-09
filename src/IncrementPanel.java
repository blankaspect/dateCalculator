/*====================================================================*\

IncrementPanel.java

Increment panel class.

\*====================================================================*/


// IMPORTS


import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Calendar;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import uk.org.blankaspect.exception.AppException;

import uk.org.blankaspect.gui.FButton;
import uk.org.blankaspect.gui.FIntegerSpinner;
import uk.org.blankaspect.gui.FLabel;
import uk.org.blankaspect.gui.GuiUtilities;

import uk.org.blankaspect.util.Property;

//----------------------------------------------------------------------


// INCREMENT PANEL CLASS


class IncrementPanel
    extends JPanel
    implements ActionListener, CompoundDateField.Observer, Property.Observer
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

    private static final    int MIN_NUM_DAYS    = 0;
    private static final    int MAX_NUM_DAYS    = 999999;

    private static final    int NUM_DAYS_FIELD_LENGTH       = 6;
    private static final    int RESULT_FIELD_NUM_COLUMNS    = 24;

    private static final    Insets  RESULT_BUTTON_MARGINS   = new Insets( 2, 4, 2, 4 );

    private static final    String  DATE_STR        = "Date:";
    private static final    String  DAYS_STR        = "Days:";
    private static final    String  DATE_FORMAT_STR = "Date format:";
    private static final    String  RESULT_STR      = "Result:";
    private static final    String  COPY_STR        = "Copy";
    private static final    String  ADD_STR         = "Add";
    private static final    String  SUBTRACT_STR    = "Subtract";

    private static final    String  SELECT_DATE_TOOLTIP_STR = "Select date";
    private static final    String  TODAY_TOOLTIP_STR       = "Set date to current date";
    private static final    String  ADD_TOOLTIP_STR         = "Add days to date";
    private static final    String  SUBTRACT_TOOLTIP_STR    = "Subtract days from date";
    private static final    String  COPY_TOOLTIP_STR        = "Copy result to clipboard";
    private static final    String  SET_DATE_TOOLTIP_STR    = "Set date to result";

    private static final    String  KEY = IncrementPanel.class.getCanonicalName( );

    // Commands
    private interface Command
    {
        String  SELECT_DATE_FORMAT  = "selectDateFormat";
        String  COPY_RESULT         = "copyResult";
        String  SET_DATE_TO_RESULT  = "setDateToResult";
        String  ADD                 = "add";
        String  SUBTRACT            = "subtract";
    }

////////////////////////////////////////////////////////////////////////
//  Enumerated types
////////////////////////////////////////////////////////////////////////


    // ERROR IDENTIFIERS


    private enum ErrorId
        implements AppException.Id
    {

    ////////////////////////////////////////////////////////////////////
    //  Constants
    ////////////////////////////////////////////////////////////////////

        INVALID_DATE
        ( "The %1 is invalid." ),

        DATE_OUT_OF_BOUNDS
        ( "The %1 must be between %2 and %3." );

    ////////////////////////////////////////////////////////////////////
    //  Constructors
    ////////////////////////////////////////////////////////////////////

        private ErrorId( String message )
        {
            this.message = message;
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance methods : AppException.Id interface
    ////////////////////////////////////////////////////////////////////

        public String getMessage( )
        {
            return message;
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance variables
    ////////////////////////////////////////////////////////////////////

        private String  message;

    }

    //==================================================================

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

    public IncrementPanel( )
    {

        //----  Control panel

        GridBagLayout gridBag = new GridBagLayout( );
        GridBagConstraints gbc = new GridBagConstraints( );

        JPanel controlPanel = new JPanel( gridBag );
        GuiUtilities.setPaddedLineBorder( controlPanel );

        int gridY = 0;

        // Label: date
        JLabel dateLabel = new FLabel( DATE_STR );

        gbc.gridx = 0;
        gbc.gridy = gridY;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = AppConstants.COMPONENT_INSETS;
        gridBag.setConstraints( dateLabel, gbc );
        controlPanel.add( dateLabel );

        // Panel: date
        datePanel = new DatePanel( KEY, SELECT_DATE_TOOLTIP_STR, TODAY_TOOLTIP_STR );
        datePanel.setObserver( this );

        gbc.gridx = 1;
        gbc.gridy = gridY++;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = AppConstants.COMPONENT_INSETS;
        gridBag.setConstraints( datePanel, gbc );
        controlPanel.add( datePanel );

        // Label: days
        JLabel daysLabel = new FLabel( DAYS_STR );

        gbc.gridx = 0;
        gbc.gridy = gridY;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = AppConstants.COMPONENT_INSETS;
        gridBag.setConstraints( daysLabel, gbc );
        controlPanel.add( daysLabel );

        // Spinner: days
        daysSpinner = new FIntegerSpinner( 0, MIN_NUM_DAYS, MAX_NUM_DAYS, NUM_DAYS_FIELD_LENGTH );

        gbc.gridx = 1;
        gbc.gridy = gridY++;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = AppConstants.COMPONENT_INSETS;
        gridBag.setConstraints( daysSpinner, gbc );
        controlPanel.add( daysSpinner );

        // Label: date format
        JLabel dateFormatLabel = new FLabel( DATE_FORMAT_STR );

        gbc.gridx = 0;
        gbc.gridy = gridY;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = AppConstants.COMPONENT_INSETS;
        gridBag.setConstraints( dateFormatLabel, gbc );
        controlPanel.add( dateFormatLabel );

        // Combo box: date format
        dateFormatComboBox = new DateFormatComboBox( );
        dateFormatComboBox.setActionCommand( Command.SELECT_DATE_FORMAT );
        dateFormatComboBox.addActionListener( this );

        gbc.gridx = 1;
        gbc.gridy = gridY++;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = AppConstants.COMPONENT_INSETS;
        gridBag.setConstraints( dateFormatComboBox, gbc );
        controlPanel.add( dateFormatComboBox );

        // Label: result
        JLabel resultLabel = new FLabel( RESULT_STR );

        gbc.gridx = 0;
        gbc.gridy = gridY;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = AppConstants.COMPONENT_INSETS;
        gridBag.setConstraints( resultLabel, gbc );
        controlPanel.add( resultLabel );

        // Panel: result
        JPanel resultPanel = new JPanel( gridBag );

        gbc.gridx = 1;
        gbc.gridy = gridY++;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = AppConstants.COMPONENT_INSETS;
        gridBag.setConstraints( resultPanel, gbc );
        controlPanel.add( resultPanel );

        // Field: result
        resultField = new ResultField( RESULT_FIELD_NUM_COLUMNS );

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets( 0, 0, 0, 0 );
        gridBag.setConstraints( resultField, gbc );
        resultPanel.add( resultField );

        // Button: copy result
        copyResultButton = new FButton( COPY_STR );
        copyResultButton.setMargin( RESULT_BUTTON_MARGINS );
        copyResultButton.setToolTipText( COPY_TOOLTIP_STR );
        copyResultButton.setActionCommand( Command.COPY_RESULT );
        copyResultButton.addActionListener( this );

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.insets = new Insets( 0, 6, 0, 0 );
        gridBag.setConstraints( copyResultButton, gbc );
        resultPanel.add( copyResultButton );

        // Button: set date to result
        setDateToResultButton = new JButton( AppIcon.ARROW_UP );
        setDateToResultButton.setMargin( RESULT_BUTTON_MARGINS );
        setDateToResultButton.setToolTipText( SET_DATE_TOOLTIP_STR );
        setDateToResultButton.setActionCommand( Command.SET_DATE_TO_RESULT );
        setDateToResultButton.addActionListener( this );

        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.insets = new Insets( 0, 6, 0, 0 );
        gridBag.setConstraints( setDateToResultButton, gbc );
        resultPanel.add( setDateToResultButton );


        //----  Button panel

        JPanel buttonPanel = new JPanel( new GridLayout( 1, 0, 8, 0 ) );
        buttonPanel.setBorder( BorderFactory.createEmptyBorder( 3, 8, 3, 8 ) );

        // Button: add
        addButton = new FButton( ADD_STR );
        addButton.setToolTipText( ADD_TOOLTIP_STR );
        addButton.setActionCommand( Command.ADD );
        addButton.addActionListener( this );
        buttonPanel.add( addButton );

        // Button: subtract
        subtractButton = new FButton( SUBTRACT_STR );
        subtractButton.setToolTipText( SUBTRACT_TOOLTIP_STR );
        subtractButton.setActionCommand( Command.SUBTRACT );
        subtractButton.addActionListener( this );
        buttonPanel.add( subtractButton );

        updateButtons( );


        //----  Outer panel

        setLayout( gridBag );
        setBorder( BorderFactory.createEmptyBorder( 2, 2, 2, 2 ) );

        gridY = 0;

        gbc.gridx = 0;
        gbc.gridy = gridY++;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets( 0, 0, 0, 0 );
        gridBag.setConstraints( controlPanel, gbc );
        add( controlPanel );

        gbc.gridx = 0;
        gbc.gridy = gridY++;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets( 3, 0, 0, 0 );
        gridBag.setConstraints( buttonPanel, gbc );
        add( buttonPanel );

        // Update result
        updateResult( );

        // Add listeners
        dateFormatKey = AppConfig.getInstance( ).addDateFormatObserver( this );

    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : ActionListener interface
////////////////////////////////////////////////////////////////////////

    public void actionPerformed( ActionEvent event )
    {
        try
        {
            String command = event.getActionCommand( );

            if ( command.equals( Command.SELECT_DATE_FORMAT ) )
                onSelectDateFormat( );

            else if ( command.equals( Command.COPY_RESULT ) )
                onCopyResult( );

            else if ( command.equals( Command.SET_DATE_TO_RESULT ) )
                onSetDateToResult( );

            else if ( command.equals( Command.ADD ) )
                onAdd( );

            else if ( command.equals( Command.SUBTRACT ) )
                onSubtract( );
        }
        catch ( AppException e )
        {
            App.getInstance( ).showErrorMessage( App.SHORT_NAME, e );
        }
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : CompoundDateField.Observer interface
////////////////////////////////////////////////////////////////////////

    public void notifyChanged( CompoundDateField source )
    {
        updateButtons( );
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : Property.Observer interface
////////////////////////////////////////////////////////////////////////

    public void propertyChanged( Property property )
    {
        if ( property.getKey( ).equals( dateFormatKey ) )
        {
            dateFormatComboBox.removeActionListener( this );
            dateFormatComboBox.reset( );
            dateFormatComboBox.addActionListener( this );
            updateResult( );
        }
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

    private void updateButtons( )
    {
        boolean enabled = datePanel.hasDate( );
        addButton.setEnabled( enabled );
        subtractButton.setEnabled( enabled );
    }

    //------------------------------------------------------------------

    private void updateResult( )
    {
        boolean isResult = (result != null);
        resultField.setText( isResult ? dateFormatComboBox.getSelectedFormat( ).format( result ) : null );
        copyResultButton.setEnabled( isResult );
        setDateToResultButton.setEnabled( isResult );
    }

    //------------------------------------------------------------------

    private void incrementDate( int numDays )
        throws AppException
    {
        // Validate date
        try
        {
            datePanel.getDateField( ).validateDate( );
        }
        catch ( CompoundDateField.InvalidDateException e )
        {
            GuiUtilities.setFocus( datePanel.getDateField( ).getField( e.getKey( ) ) );
            throw new AppException( ErrorId.INVALID_DATE, e.getString( ) );
        }
        catch ( CompoundDateField.DateOutOfBoundsException e )
        {
            GuiUtilities.setFocus( datePanel.getDateField( ).getField( e.getKey( ) ) );
            throw new AppException( ErrorId.DATE_OUT_OF_BOUNDS, e.getStrings( ) );
        }

        // Calculate result
        Calendar date = datePanel.getDate( );
        date.add( Calendar.DATE, numDays );
        result = date;
        updateResult( );
    }

    //------------------------------------------------------------------

    private void onSelectDateFormat( )
    {
        updateResult( );
    }

    //------------------------------------------------------------------

    private void onCopyResult( )
        throws AppException
    {
        Util.putClipboardText( resultField.getText( ) );
    }

    //------------------------------------------------------------------

    private void onSetDateToResult( )
    {
        if ( result != null )
            datePanel.getDateField( ).setDate( result.get( Calendar.YEAR ),
                                               result.get( Calendar.MONTH ) + 1,
                                               result.get( Calendar.DAY_OF_MONTH ) );
    }

    //------------------------------------------------------------------

    private void onAdd( )
        throws AppException
    {
        incrementDate( daysSpinner.getIntValue( ) );
    }

    //------------------------------------------------------------------

    private void onSubtract( )
        throws AppException
    {
        incrementDate( -daysSpinner.getIntValue( ) );
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

    private String              dateFormatKey;
    private Calendar            result;
    private DatePanel           datePanel;
    private FIntegerSpinner     daysSpinner;
    private DateFormatComboBox  dateFormatComboBox;
    private ResultField         resultField;
    private JButton             copyResultButton;
    private JButton             setDateToResultButton;
    private JButton             addButton;
    private JButton             subtractButton;

}

//----------------------------------------------------------------------
