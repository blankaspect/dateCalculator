/*====================================================================*\

PreferencesDialog.java

Preferences dialog box class.

\*====================================================================*/


// IMPORTS


import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Window;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.io.File;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import uk.org.blankaspect.exception.AppException;

import uk.org.blankaspect.gui.BooleanComboBox;
import uk.org.blankaspect.gui.FButton;
import uk.org.blankaspect.gui.FComboBox;
import uk.org.blankaspect.gui.FixedWidthLabel;
import uk.org.blankaspect.gui.FixedWidthPanel;
import uk.org.blankaspect.gui.FLabel;
import uk.org.blankaspect.gui.FontEx;
import uk.org.blankaspect.gui.FontStyle;
import uk.org.blankaspect.gui.FTabbedPane;
import uk.org.blankaspect.gui.GuiUtilities;
import uk.org.blankaspect.gui.IntegerSpinner;
import uk.org.blankaspect.gui.SingleSelectionList;
import uk.org.blankaspect.gui.TextRendering;
import uk.org.blankaspect.gui.TitledBorder;

import uk.org.blankaspect.textfield.IntegerValueField;

import uk.org.blankaspect.util.DateFormat;
import uk.org.blankaspect.util.DateUtilities;
import uk.org.blankaspect.util.KeyAction;
import uk.org.blankaspect.util.MaxValueMap;

//----------------------------------------------------------------------


// PREFERENCES DIALOG BOX CLASS


class PreferencesDialog
    extends JDialog
    implements ActionListener, ChangeListener, ListSelectionListener
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

    // Main panel
    private static final    int     MODIFIERS_MASK  = ActionEvent.ALT_MASK | ActionEvent.META_MASK |
                                                            ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK;

    private static final    String  TITLE_STR               = "Preferences";
    private static final    String  SAVE_CONFIGURATION_STR  = "Save configuration";
    private static final    String  SAVE_CONFIG_FILE_STR    = "Save configuration file";
    private static final    String  WRITE_CONFIG_FILE_STR   = "Write configuration file";

    // General panel
    private static final    String  SHOW_UNIX_PATHNAMES_STR         = "Display UNIX-style pathnames:";
    private static final    String  SELECT_TEXT_ON_FOCUS_GAINED_STR = "Select text when focus is gained:";
    private static final    String  SAVE_MAIN_WINDOW_LOCATION_STR   = "Save location of main window:";

    // Appearance panel
    private static final    String  LOOK_AND_FEEL_STR       = "Look-and-feel:";
    private static final    String  TEXT_ANTIALIASING_STR   = "Text antialiasing:";
    private static final    String  NO_LOOK_AND_FEELS_STR   = "<no look-and-feels>";

    // Date panel
    private static final    String  DATE_FORMATS_STR                = "Date formats";
    private static final    String  ADD_STR                         = "Add";
    private static final    String  EDIT_STR                        = "Edit";
    private static final    String  DELETE_STR                      = "Delete";
    private static final    String  ADD_DATE_FORMAT_STR             = "Add date format";
    private static final    String  EDIT_DATE_FORMAT_STR            = "Edit date format";
    private static final    String  DELETE_DATE_FORMAT_STR          = "Delete date format";
    private static final    String  DELETE_DATE_FORMAT_MESSAGE_STR  = "Do you want to delete the " +
                                                                        "selected format?";
    private static final    String  DATE_NAMES_STR                  = "Names of months & days of the week";
    private static final    String  SOURCE_STR                      = "Source:";
    private static final    String  LOCALE_STR                      = "Locale:";
    private static final    String  USER_DEFINED_STR                = "User-defined:";
    private static final    String  EDIT_MONTHS_STR                 = "Edit months";
    private static final    String  EDIT_DAYS_STR                   = "Edit days";
    private static final    String  EDIT_MONTH_NAMES_STR            = "Edit names of months";
    private static final    String  EDIT_DAY_NAMES_STR              = "Edit names of days of the week";
    private static final    String  FIRST_DAY_OF_WEEK_STR           = "First day of week:";
    private static final    String  DEFAULT_STR                     = "<default>";

    private static final    Insets  EDIT_NAMES_BUTTON_MARGINS   = new Insets( 2, 6, 2, 6 );

    // Fonts panel
    private static final    String  PT_STR  = "pt";

    // Commands
    private interface Command
    {
        String  ADD_DATE_FORMAT             = "addDateFormat";
        String  EDIT_DATE_FORMAT            = "editDateFormat";
        String  DELETE_DATE_FORMAT          = "deleteDateFormat";
        String  CONFIRM_DELETE_DATE_FORMAT  = "confirmDeleteDateFormat";
        String  MOVE_DATE_FORMAT_UP         = "moveDateFormatUp";
        String  MOVE_DATE_FORMAT_DOWN       = "moveDateFormatDown";
        String  MOVE_DATE_FORMAT            = "moveDateFormat";
        String  SELECT_DATE_NAMES_SOURCE    = "selectDateNamesSource";
        String  EDIT_MONTH_NAMES            = "editMonthNames";
        String  EDIT_DAY_NAMES              = "editDayNames";
        String  SAVE_CONFIGURATION          = "saveConfiguration";
        String  ACCEPT                      = "accept";
        String  CLOSE                       = "close";
    }

    private static final    Map<String, String> COMMAND_MAP;

////////////////////////////////////////////////////////////////////////
//  Enumerated types
////////////////////////////////////////////////////////////////////////


    // TABS


    private enum Tab
    {

    ////////////////////////////////////////////////////////////////////
    //  Constants
    ////////////////////////////////////////////////////////////////////

        GENERAL
        (
            "General"
        )
        {
            @Override
            protected JPanel createPanel( PreferencesDialog dialog )
            {
                return dialog.createPanelGeneral( );
            }

            //----------------------------------------------------------

            @Override
            protected void validatePreferences( PreferencesDialog dialog )
                throws AppException
            {
                dialog.validatePreferencesGeneral( );
            }

            //----------------------------------------------------------

            @Override
            protected void setPreferences( PreferencesDialog dialog )
            {
                dialog.setPreferencesGeneral( );
            }

            //----------------------------------------------------------
        },

        APPEARANCE
        (
            "Appearance"
        )
        {
            @Override
            protected JPanel createPanel( PreferencesDialog dialog )
            {
                return dialog.createPanelAppearance( );
            }

            //----------------------------------------------------------

            @Override
            protected void validatePreferences( PreferencesDialog dialog )
                throws AppException
            {
                dialog.validatePreferencesAppearance( );
            }

            //----------------------------------------------------------

            @Override
            protected void setPreferences( PreferencesDialog dialog )
            {
                dialog.setPreferencesAppearance( );
            }

            //----------------------------------------------------------
        },

        DATE
        (
            "Date"
        )
        {
            @Override
            protected JPanel createPanel( PreferencesDialog dialog )
            {
                return dialog.createPanelDate( );
            }

            //----------------------------------------------------------

            @Override
            protected void validatePreferences( PreferencesDialog dialog )
                throws AppException
            {
                dialog.validatePreferencesDate( );
            }

            //----------------------------------------------------------

            @Override
            protected void setPreferences( PreferencesDialog dialog )
            {
                dialog.setPreferencesDate( );
            }

            //----------------------------------------------------------
        },

        FONTS
        (
            "Fonts"
        )
        {
            @Override
            protected JPanel createPanel( PreferencesDialog dialog )
            {
                return dialog.createPanelFonts( );
            }

            //----------------------------------------------------------

            @Override
            protected void validatePreferences( PreferencesDialog dialog )
                throws AppException
            {
                dialog.validatePreferencesFonts( );
            }

            //----------------------------------------------------------

            @Override
            protected void setPreferences( PreferencesDialog dialog )
            {
                dialog.setPreferencesFonts( );
            }

            //----------------------------------------------------------
        };

    ////////////////////////////////////////////////////////////////////
    //  Constructors
    ////////////////////////////////////////////////////////////////////

        private Tab( String text )
        {
            this.text = text;
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Abstract methods
    ////////////////////////////////////////////////////////////////////

        protected abstract JPanel createPanel( PreferencesDialog dialog );

        //--------------------------------------------------------------

        protected abstract void validatePreferences( PreferencesDialog dialog )
            throws AppException;

        //--------------------------------------------------------------

        protected abstract void setPreferences( PreferencesDialog dialog );

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance variables
    ////////////////////////////////////////////////////////////////////

        private String  text;

    }

    //==================================================================

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


    // DATE-FORMAT SELECTION LIST CLASS


    private static class DateFormatSelectionList
        extends SingleSelectionList<DateFormat>
    {

    ////////////////////////////////////////////////////////////////////
    //  Constants
    ////////////////////////////////////////////////////////////////////

        private static final    int NAME_FIELD_NUM_COLUMNS      = 16;
        private static final    int PATTERN_FIELD_NUM_COLUMNS   = 32;
        private static final    int NUM_ROWS                    = 8;

        private static final    int SEPARATOR_WIDTH = 1;

        private static final    Color   SEPARATOR_COLOUR    = new Color( 192, 200, 192 );

    ////////////////////////////////////////////////////////////////////
    //  Constructors
    ////////////////////////////////////////////////////////////////////

        private DateFormatSelectionList( List<DateFormat> formats )
        {
            super( NAME_FIELD_NUM_COLUMNS + PATTERN_FIELD_NUM_COLUMNS, NUM_ROWS, AppFont.MAIN.getFont( ),
                   formats );
            setExtraWidth( 2 * getHorizontalMargin( ) + SEPARATOR_WIDTH );
            setRowHeight( getRowHeight( ) + 1 );
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance methods : overriding methods
    ////////////////////////////////////////////////////////////////////

        @Override
        protected void drawElement( Graphics gr,
                                    int      index )
        {
            // Create copy of graphics context
            gr = gr.create( );

            // Set rendering hints for text antialiasing and fractional metrics
            TextRendering.setHints( (Graphics2D)gr );

            // Get date format
            DateFormat dateFormat = getElement( index );

            // Get name text and truncate it if it is too wide
            FontMetrics fontMetrics = gr.getFontMetrics( );
            int nameFieldWidth = NAME_FIELD_NUM_COLUMNS * getColumnWidth( );
            int patternFieldWidth = getMaxTextWidth( ) - nameFieldWidth;
            String text = truncateText( dateFormat.getName( ), fontMetrics, nameFieldWidth );

            // Draw name text
            int rowHeight = getRowHeight( );
            int x = getHorizontalMargin( );
            int y = index * rowHeight;
            int textY = y + DEFAULT_VERTICAL_MARGIN + fontMetrics.getAscent( );
            gr.setColor( getForegroundColour( index ) );
            gr.drawString( text, x, textY );

            // Get pattern text and truncate it if it is too wide
            text = truncateText( dateFormat.getPattern( ), fontMetrics, patternFieldWidth );

            // Draw pattern text
            x += nameFieldWidth + getExtraWidth( );
            gr.drawString( text, x, textY );

            // Draw separator
            x = getHorizontalMargin( ) + nameFieldWidth + getExtraWidth( ) / 2;
            gr.setColor( SEPARATOR_COLOUR );
            gr.drawLine( x, y, x, y + rowHeight - 1 );

            // Draw bottom border
            y += rowHeight - 1;
            gr.drawLine( 0, y, getWidth( ) - 1, y );
        }

        //--------------------------------------------------------------

        @Override
        protected String getPopUpText( int index )
        {
            DateFormat dateFormat = getElement( index );
            return ( dateFormat.getName( ) + " : " + dateFormat.getPattern( ) );
        }

        //--------------------------------------------------------------

        @Override
        protected boolean isShowPopUp( int index )
        {
            DateFormat dateFormat = getElement( index );
            FontMetrics fontMetrics = getFontMetrics( getFont( ) );
            int nameFieldWidth = NAME_FIELD_NUM_COLUMNS * getColumnWidth( );
            int patternFieldWidth = getMaxTextWidth( ) - nameFieldWidth;
            return ( (fontMetrics.stringWidth( dateFormat.getName( ) ) > nameFieldWidth) ||
                     (fontMetrics.stringWidth( dateFormat.getPattern( ) ) > patternFieldWidth) );
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance methods
    ////////////////////////////////////////////////////////////////////

        private List<String> getNames( int index )
        {
            List<String> names = new ArrayList<>( );
            for ( int i = 0; i < getNumElements( ); ++i )
            {
                if ( i != index )
                    names.add( getElement( i ).getName( ) );
            }
            return names;
        }

        //--------------------------------------------------------------

    }

    //==================================================================


    // DATE PANEL CLASS


    private static class DatePanel
        extends FixedWidthPanel
    {

    ////////////////////////////////////////////////////////////////////
    //  Constants
    ////////////////////////////////////////////////////////////////////

        private static final    String  KEY = DatePanel.class.getCanonicalName( );

    ////////////////////////////////////////////////////////////////////
    //  Constructors
    ////////////////////////////////////////////////////////////////////

        private DatePanel( LayoutManager layout )
        {
            super( layout );
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Class methods
    ////////////////////////////////////////////////////////////////////

        private static void reset( )
        {
            MaxValueMap.removeAll( KEY );
        }

        //--------------------------------------------------------------

        private static void update( )
        {
            MaxValueMap.update( KEY );
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance methods : overriding methods
    ////////////////////////////////////////////////////////////////////

        @Override
        protected String getKey( )
        {
            return KEY;
        }

        //--------------------------------------------------------------

    }

    //==================================================================


    // DATE PANEL LABEL CLASS


    private static class DatePanelLabel
        extends FixedWidthLabel
    {

    ////////////////////////////////////////////////////////////////////
    //  Constants
    ////////////////////////////////////////////////////////////////////

        private static final    String  KEY = DatePanelLabel.class.getCanonicalName( );

    ////////////////////////////////////////////////////////////////////
    //  Constructors
    ////////////////////////////////////////////////////////////////////

        private DatePanelLabel( String text )
        {
            super( text );
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Class methods
    ////////////////////////////////////////////////////////////////////

        private static void reset( )
        {
            MaxValueMap.removeAll( KEY );
        }

        //--------------------------------------------------------------

        private static void update( )
        {
            MaxValueMap.update( KEY );
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance methods : overriding methods
    ////////////////////////////////////////////////////////////////////

        @Override
        protected String getKey( )
        {
            return KEY;
        }

        //--------------------------------------------------------------

    }

    //==================================================================


    // FONT PANEL CLASS


    private static class FontPanel
    {

    ////////////////////////////////////////////////////////////////////
    //  Constants
    ////////////////////////////////////////////////////////////////////

        private static final    int MIN_SIZE    = 0;
        private static final    int MAX_SIZE    = 99;

        private static final    int SIZE_FIELD_LENGTH   = 2;

        private static final    String  DEFAULT_FONT_STR    = "<default font>";

    ////////////////////////////////////////////////////////////////////
    //  Member classes : non-inner classes
    ////////////////////////////////////////////////////////////////////


        // SIZE SPINNER CLASS


        private static class SizeSpinner
            extends IntegerSpinner
        {

        ////////////////////////////////////////////////////////////////
        //  Constructors
        ////////////////////////////////////////////////////////////////

            private SizeSpinner( int value )
            {
                super( value, MIN_SIZE, MAX_SIZE, SIZE_FIELD_LENGTH );
                AppFont.TEXT_FIELD.apply( this );
            }

            //----------------------------------------------------------

        ////////////////////////////////////////////////////////////////
        //  Instance methods : overriding methods
        ////////////////////////////////////////////////////////////////

            /**
             * @throws NumberFormatException
             */

            @Override
            protected int getEditorValue( )
            {
                IntegerValueField field = (IntegerValueField)getEditor( );
                return ( field.isEmpty( ) ? 0 : field.getValue( ) );
            }

            //----------------------------------------------------------

            @Override
            protected void setEditorValue( int value )
            {
                IntegerValueField field = (IntegerValueField)getEditor( );
                if ( value == 0 )
                    field.setText( null );
                else
                    field.setValue( value );
            }

            //----------------------------------------------------------

        }

        //==============================================================

    ////////////////////////////////////////////////////////////////////
    //  Constructors
    ////////////////////////////////////////////////////////////////////

        private FontPanel( FontEx   font,
                           String[] fontNames )
        {
            nameComboBox = new FComboBox<>( );
            nameComboBox.addItem( DEFAULT_FONT_STR );
            for ( String fontName : fontNames )
                nameComboBox.addItem( fontName );
            nameComboBox.setSelectedIndex( Util.indexOf( font.getName( ), fontNames ) + 1 );

            styleComboBox = new FComboBox<>( FontStyle.values( ) );
            styleComboBox.setSelectedValue( font.getStyle( ) );

            sizeSpinner = new SizeSpinner( font.getSize( ) );
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance methods
    ////////////////////////////////////////////////////////////////////

        public FontEx getFont( )
        {
            String name = (nameComboBox.getSelectedIndex( ) <= 0) ? null : nameComboBox.getSelectedValue( );
            return new FontEx( name, styleComboBox.getSelectedValue( ), sizeSpinner.getIntValue( ) );
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance variables
    ////////////////////////////////////////////////////////////////////

        private FComboBox<String>       nameComboBox;
        private FComboBox<FontStyle>    styleComboBox;
        private SizeSpinner             sizeSpinner;

    }

    //==================================================================

////////////////////////////////////////////////////////////////////////
//  Member classes : inner classes
////////////////////////////////////////////////////////////////////////


    // WINDOW EVENT HANDLER CLASS


    private class WindowEventHandler
        extends WindowAdapter
    {

    ////////////////////////////////////////////////////////////////////
    //  Constructors
    ////////////////////////////////////////////////////////////////////

        private WindowEventHandler( )
        {
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance methods : overriding methods
    ////////////////////////////////////////////////////////////////////

        @Override
        public void windowClosing( WindowEvent event )
        {
            onClose( );
        }

        //--------------------------------------------------------------

    }

    //==================================================================

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

    private PreferencesDialog( Window owner )
    {

        // Call superclass constructor
        super( owner, TITLE_STR, Dialog.ModalityType.APPLICATION_MODAL );

        // Set icons
        setIconImages( owner.getIconImages( ) );


        //----  Tabbed panel

        tabbedPanel = new FTabbedPane( );
        for ( Tab tab : Tab.values( ) )
            tabbedPanel.addTab( tab.text, tab.createPanel( this ) );
        tabbedPanel.setSelectedIndex( tabIndex );


        //----  Button panel: save configuration

        JPanel saveButtonPanel = new JPanel( new GridLayout( 1, 0, 8, 0 ) );

        // Button: save configuration
        JButton saveButton = new FButton( SAVE_CONFIGURATION_STR + AppConstants.ELLIPSIS_STR );
        saveButton.setActionCommand( Command.SAVE_CONFIGURATION );
        saveButton.addActionListener( this );
        saveButtonPanel.add( saveButton );


        //----  Button panel: OK, cancel

        JPanel okCancelButtonPanel = new JPanel( new GridLayout( 1, 0, 8, 0 ) );

        // Button: OK
        JButton okButton = new FButton( AppConstants.OK_STR );
        okButton.setActionCommand( Command.ACCEPT );
        okButton.addActionListener( this );
        okCancelButtonPanel.add( okButton );

        // Button: cancel
        JButton cancelButton = new FButton( AppConstants.CANCEL_STR );
        cancelButton.setActionCommand( Command.CLOSE );
        cancelButton.addActionListener( this );
        okCancelButtonPanel.add( cancelButton );


        //----  Button panel

        GridBagLayout gridBag = new GridBagLayout( );
        GridBagConstraints gbc = new GridBagConstraints( );

        JPanel buttonPanel = new JPanel( gridBag );
        buttonPanel.setBorder( BorderFactory.createEmptyBorder( 3, 24, 3, 24 ) );

        int gridX = 0;

        gbc.gridx = gridX++;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.5;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets( 0, 0, 0, 12 );
        gridBag.setConstraints( saveButtonPanel, gbc );
        buttonPanel.add( saveButtonPanel );

        gbc.gridx = gridX++;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.5;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets( 0, 12, 0, 0 );
        gridBag.setConstraints( okCancelButtonPanel, gbc );
        buttonPanel.add( okCancelButtonPanel );


        //----  Main panel

        JPanel mainPanel = new JPanel( gridBag );
        mainPanel.setBorder( BorderFactory.createEmptyBorder( 2, 2, 2, 2 ) );

        int gridY = 0;

        gbc.gridx = 0;
        gbc.gridy = gridY++;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets( 0, 0, 0, 0 );
        gridBag.setConstraints( tabbedPanel, gbc );
        mainPanel.add( tabbedPanel );

        gbc.gridx = 0;
        gbc.gridy = gridY++;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets( 3, 0, 0, 0 );
        gridBag.setConstraints( buttonPanel, gbc );
        mainPanel.add( buttonPanel );

        // Add commands to action map
        KeyAction.create( mainPanel, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,
                          KeyStroke.getKeyStroke( KeyEvent.VK_ESCAPE, 0 ), Command.CLOSE, this );


        //----  Window

        // Set content pane
        setContentPane( mainPanel );

        // Dispose of window explicitly
        setDefaultCloseOperation( DO_NOTHING_ON_CLOSE );

        // Handle window events
        addWindowListener( new WindowEventHandler( ) );

        // Prevent dialog from being resized
        setResizable( false );

        // Resize dialog to its preferred size
        pack( );

        // Set location of dialog box
        if ( location == null )
            location = GuiUtilities.getComponentLocation( this, owner );
        setLocation( location );

        // Set default button
        getRootPane( ).setDefaultButton( okButton );

        // Show dialog
        setVisible( true );

    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

    public static boolean showDialog( Component parent )
    {
        return new PreferencesDialog( GuiUtilities.getWindow( parent ) ).accepted;
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : ActionListener interface
////////////////////////////////////////////////////////////////////////

    public void actionPerformed( ActionEvent event )
    {
        String command = event.getActionCommand( );
        if ( command.equals( Command.CONFIRM_DELETE_DATE_FORMAT ) &&
             ((event.getModifiers( ) & MODIFIERS_MASK) == ActionEvent.SHIFT_MASK) )
            command = Command.DELETE_DATE_FORMAT;
        else if ( COMMAND_MAP.containsKey( command ) )
            command = COMMAND_MAP.get( command );

        if ( command.equals( Command.ADD_DATE_FORMAT ) )
            onAddDateFormat( );

        else if ( command.equals( Command.EDIT_DATE_FORMAT ) )
            onEditDateFormat( );

        else if ( command.equals( Command.DELETE_DATE_FORMAT ) )
            onDeleteDateFormat( );

        else if ( command.equals( Command.CONFIRM_DELETE_DATE_FORMAT ) )
            onConfirmDeleteDateFormat( );

        else if ( command.equals( Command.MOVE_DATE_FORMAT_UP ) )
            onMoveDateFormatUp( );

        else if ( command.equals( Command.MOVE_DATE_FORMAT_DOWN ) )
            onMoveDateFormatDown( );

        else if ( command.equals( Command.MOVE_DATE_FORMAT ) )
            onMoveDateFormat( );

        else if ( command.equals( Command.SELECT_DATE_NAMES_SOURCE ) )
            onSelectDateNamesSource( );

        else if ( command.equals( Command.EDIT_MONTH_NAMES ) )
            onEditMonthNames( );

        else if ( command.equals( Command.EDIT_DAY_NAMES ) )
            onEditDayNames( );

        else if ( command.equals( Command.SAVE_CONFIGURATION ) )
            onSaveConfiguration( );

        else if ( command.equals( Command.ACCEPT ) )
            onAccept( );

        else if ( command.equals( Command.CLOSE ) )
            onClose( );
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : ChangeListener interface
////////////////////////////////////////////////////////////////////////

    public void stateChanged( ChangeEvent event )
    {
        if ( !dateFormatListScrollPane.getVerticalScrollBar( ).getValueIsAdjusting( ) &&
             !dateFormatList.isDragging( ) )
            dateFormatList.snapViewPosition( );
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : ListSelectionListener interface
////////////////////////////////////////////////////////////////////////

    public void valueChanged( ListSelectionEvent event )
    {
        if ( !event.getValueIsAdjusting( ) )
        {
            Object eventSource = event.getSource( );

            if ( eventSource == dateFormatList )
                updateDateFormatButtons( );
        }
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

    private void validatePreferences( )
        throws AppException
    {
        for ( Tab tab : Tab.values( ) )
            tab.validatePreferences( this );
    }

    //------------------------------------------------------------------

    private void setPreferences( )
    {
        for ( Tab tab : Tab.values( ) )
            tab.setPreferences( this );
    }

    //------------------------------------------------------------------

    private void updateDateFormatButtons( )
    {
        dateFormatAddButton.setEnabled( dateFormatList.getNumElements( ) < AppConfig.MAX_NUM_DATE_FORMATS );
        dateFormatEditButton.setEnabled( dateFormatList.isSelection( ) );
        dateFormatDeleteButton.setEnabled( (dateFormatList.getNumElements( ) > 1) &&
                                           dateFormatList.isSelection( ) );
    }

    //------------------------------------------------------------------

    private DateNamesSource getDateNamesSource( )
    {
        return dateNamesSourceComboBox.getSelectedValue( );
    }

    //------------------------------------------------------------------

    private void updateDateNamesSource( )
    {
        for ( DateNamesSource dateNamesSource : dateNamesComponents.keySet( ) )
        {
            boolean enabled = (dateNamesSource == getDateNamesSource( ));
            for ( Component component : dateNamesComponents.get( dateNamesSource ) )
                GuiUtilities.setAllEnabled( component, enabled );
        }
    }

    //------------------------------------------------------------------

    private void onAddDateFormat( )
    {
        DateFormat dateFormat = DateFormatDialog.showDialog( this, ADD_DATE_FORMAT_STR, null,
                                                             dateFormatList.getNames( -1 ) );
        if ( dateFormat != null )
        {
            dateFormatList.addElement( dateFormat );
            updateDateFormatButtons( );
        }
    }

    //------------------------------------------------------------------

    private void onEditDateFormat( )
    {
        int index = dateFormatList.getSelectedIndex( );
        DateFormat dateFormat = DateFormatDialog.showDialog( this, EDIT_DATE_FORMAT_STR,
                                                             dateFormatList.getElement( index ),
                                                             dateFormatList.getNames( index ) );
        if ( dateFormat != null )
            dateFormatList.setElement( index, dateFormat );
    }

    //------------------------------------------------------------------

    private void onConfirmDeleteDateFormat( )
    {
        String[] optionStrs = Util.getOptionStrings( DELETE_STR );
        if ( (dateFormatList.getNumElements( ) > 1) &&
             (JOptionPane.showOptionDialog( this, DELETE_DATE_FORMAT_MESSAGE_STR, DELETE_DATE_FORMAT_STR,
                                           JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                                           optionStrs, optionStrs[1] ) == JOptionPane.OK_OPTION) )
            onDeleteDateFormat( );
    }

    //------------------------------------------------------------------

    private void onDeleteDateFormat( )
    {
        if ( dateFormatList.getNumElements( ) > 1 )
        {
            dateFormatList.removeElement( dateFormatList.getSelectedIndex( ) );
            updateDateFormatButtons( );
        }
    }

    //------------------------------------------------------------------

    private void onMoveDateFormatUp( )
    {
        int index = dateFormatList.getSelectedIndex( );
        dateFormatList.moveElement( index, index - 1 );
    }

    //------------------------------------------------------------------

    private void onMoveDateFormatDown( )
    {
        int index = dateFormatList.getSelectedIndex( );
        dateFormatList.moveElement( index, index + 1 );
    }

    //------------------------------------------------------------------

    private void onMoveDateFormat( )
    {
        int fromIndex = dateFormatList.getSelectedIndex( );
        int toIndex = dateFormatList.getDragEndIndex( );
        if ( toIndex > fromIndex )
            --toIndex;
        dateFormatList.moveElement( fromIndex, toIndex );
    }

    //------------------------------------------------------------------

    private void onSelectDateNamesSource( )
    {
        updateDateNamesSource( );
    }

    //------------------------------------------------------------------

    private void onEditMonthNames( )
    {
        if ( monthNames == null )
            monthNames = AppConfig.getInstance( ).getMonthNames( );
        List<String> names = DateNameDialog.showDialog( this, EDIT_MONTH_NAMES_STR, monthNames,
                                                        DateUtilities.
                                                                    getMonthNames( Locale.getDefault( ) ) );
        if ( names != null )
            monthNames = names;
    }

    //------------------------------------------------------------------

    private void onEditDayNames( )
    {
        if ( dayNames == null )
            dayNames = AppConfig.getInstance( ).getDayNames( );
        List<String> names = DateNameDialog.showDialog( this, EDIT_DAY_NAMES_STR, dayNames,
                                                        DateUtilities.getDayNames( Locale.getDefault( ) ) );
        if ( names != null )
            dayNames = names;
    }

    //------------------------------------------------------------------

    private void onSaveConfiguration( )
    {
        try
        {
            validatePreferences( );

            File file = AppConfig.getInstance( ).chooseFile( this );
            if ( file != null )
            {
                String[] optionStrs = Util.getOptionStrings( AppConstants.REPLACE_STR );
                if ( !file.exists( ) ||
                     (JOptionPane.showOptionDialog( this, Util.getPathname( file ) +
                                                                            AppConstants.ALREADY_EXISTS_STR,
                                                    SAVE_CONFIG_FILE_STR, JOptionPane.OK_CANCEL_OPTION,
                                                    JOptionPane.WARNING_MESSAGE, null, optionStrs,
                                                    optionStrs[1] ) == JOptionPane.OK_OPTION) )
                {
                    setPreferences( );
                    accepted = true;
                    TaskProgressDialog.showDialog( this, WRITE_CONFIG_FILE_STR,
                                                   new Task.WriteConfig( file ) );
                }
            }
        }
        catch ( AppException e )
        {
            JOptionPane.showMessageDialog( this, e, App.SHORT_NAME, JOptionPane.ERROR_MESSAGE );
        }
        if ( accepted )
            onClose( );
    }

    //------------------------------------------------------------------

    private void onAccept( )
    {
        try
        {
            validatePreferences( );
            setPreferences( );
            accepted = true;
            onClose( );
        }
        catch ( AppException e )
        {
            JOptionPane.showMessageDialog( this, e, App.SHORT_NAME, JOptionPane.ERROR_MESSAGE );
        }
    }

    //------------------------------------------------------------------

    private void onClose( )
    {
        location = getLocation( );
        tabIndex = tabbedPanel.getSelectedIndex( );
        setVisible( false );
        dispose( );
    }

    //------------------------------------------------------------------

    private JPanel createPanelGeneral( )
    {

        //----  Control panel

        GridBagLayout gridBag = new GridBagLayout( );
        GridBagConstraints gbc = new GridBagConstraints( );

        JPanel controlPanel = new JPanel( gridBag );
        GuiUtilities.setPaddedLineBorder( controlPanel );

        int gridY = 0;

        AppConfig config = AppConfig.getInstance( );

        // Label: show UNIX pathnames
        JLabel showUnixPathnamesLabel = new FLabel( SHOW_UNIX_PATHNAMES_STR );

        gbc.gridx = 0;
        gbc.gridy = gridY;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = AppConstants.COMPONENT_INSETS;
        gridBag.setConstraints( showUnixPathnamesLabel, gbc );
        controlPanel.add( showUnixPathnamesLabel );

        // Combo box: show UNIX pathnames
        showUnixPathnamesComboBox = new BooleanComboBox( config.isShowUnixPathnames( ) );

        gbc.gridx = 1;
        gbc.gridy = gridY++;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = AppConstants.COMPONENT_INSETS;
        gridBag.setConstraints( showUnixPathnamesComboBox, gbc );
        controlPanel.add( showUnixPathnamesComboBox );

        // Label: select text on focus gained
        JLabel selectTextOnFocusGainedLabel = new FLabel( SELECT_TEXT_ON_FOCUS_GAINED_STR );

        gbc.gridx = 0;
        gbc.gridy = gridY;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = AppConstants.COMPONENT_INSETS;
        gridBag.setConstraints( selectTextOnFocusGainedLabel, gbc );
        controlPanel.add( selectTextOnFocusGainedLabel );

        // Combo box: select text on focus gained
        selectTextOnFocusGainedComboBox = new BooleanComboBox( config.isSelectTextOnFocusGained( ) );

        gbc.gridx = 1;
        gbc.gridy = gridY++;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = AppConstants.COMPONENT_INSETS;
        gridBag.setConstraints( selectTextOnFocusGainedComboBox, gbc );
        controlPanel.add( selectTextOnFocusGainedComboBox );

        // Label: save main window location
        JLabel saveMainWindowLocationLabel = new FLabel( SAVE_MAIN_WINDOW_LOCATION_STR );

        gbc.gridx = 0;
        gbc.gridy = gridY;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = AppConstants.COMPONENT_INSETS;
        gridBag.setConstraints( saveMainWindowLocationLabel, gbc );
        controlPanel.add( saveMainWindowLocationLabel );

        // Combo box: save main window location
        saveMainWindowLocationComboBox = new BooleanComboBox( config.isMainWindowLocation( ) );

        gbc.gridx = 1;
        gbc.gridy = gridY++;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = AppConstants.COMPONENT_INSETS;
        gridBag.setConstraints( saveMainWindowLocationComboBox, gbc );
        controlPanel.add( saveMainWindowLocationComboBox );


        //----  Outer panel

        JPanel outerPanel = new JPanel( gridBag );
        outerPanel.setBorder( BorderFactory.createEmptyBorder( 2, 2, 2, 2 ) );

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets( 0, 0, 0, 0 );
        gridBag.setConstraints( controlPanel, gbc );
        outerPanel.add( controlPanel );

        return outerPanel;

    }

    //------------------------------------------------------------------

    private JPanel createPanelAppearance( )
    {

        //----  Control panel

        GridBagLayout gridBag = new GridBagLayout( );
        GridBagConstraints gbc = new GridBagConstraints( );

        JPanel controlPanel = new JPanel( gridBag );
        GuiUtilities.setPaddedLineBorder( controlPanel );

        int gridY = 0;

        AppConfig config = AppConfig.getInstance( );

        // Label: look-and-feel
        JLabel lookAndFeelLabel = new FLabel( LOOK_AND_FEEL_STR );

        gbc.gridx = 0;
        gbc.gridy = gridY;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = AppConstants.COMPONENT_INSETS;
        gridBag.setConstraints( lookAndFeelLabel, gbc );
        controlPanel.add( lookAndFeelLabel );

        // Combo box: look-and-feel
        lookAndFeelComboBox = new FComboBox<>( );

        UIManager.LookAndFeelInfo[] lookAndFeelInfos = UIManager.getInstalledLookAndFeels( );
        if ( lookAndFeelInfos.length == 0 )
        {
            lookAndFeelComboBox.addItem( NO_LOOK_AND_FEELS_STR );
            lookAndFeelComboBox.setSelectedIndex( 0 );
            lookAndFeelComboBox.setEnabled( false );
        }
        else
        {
            String[] lookAndFeelNames = new String[lookAndFeelInfos.length];
            for ( int i = 0; i < lookAndFeelInfos.length; ++i )
            {
                lookAndFeelNames[i] = lookAndFeelInfos[i].getName( );
                lookAndFeelComboBox.addItem( lookAndFeelNames[i] );
            }
            lookAndFeelComboBox.setSelectedValue( config.getLookAndFeel( ) );
        }

        gbc.gridx = 1;
        gbc.gridy = gridY++;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = AppConstants.COMPONENT_INSETS;
        gridBag.setConstraints( lookAndFeelComboBox, gbc );
        controlPanel.add( lookAndFeelComboBox );

        // Label: text antialiasing
        JLabel textAntialiasingLabel = new FLabel( TEXT_ANTIALIASING_STR );

        gbc.gridx = 0;
        gbc.gridy = gridY;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = AppConstants.COMPONENT_INSETS;
        gridBag.setConstraints( textAntialiasingLabel, gbc );
        controlPanel.add( textAntialiasingLabel );

        // Combo box: text antialiasing
        textAntialiasingComboBox = new FComboBox<>( TextRendering.Antialiasing.values( ) );
        textAntialiasingComboBox.setSelectedValue( config.getTextAntialiasing( ) );

        gbc.gridx = 1;
        gbc.gridy = gridY++;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = AppConstants.COMPONENT_INSETS;
        gridBag.setConstraints( textAntialiasingComboBox, gbc );
        controlPanel.add( textAntialiasingComboBox );


        //----  Outer panel

        JPanel outerPanel = new JPanel( gridBag );
        outerPanel.setBorder( BorderFactory.createEmptyBorder( 2, 2, 2, 2 ) );

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets( 0, 0, 0, 0 );
        gridBag.setConstraints( controlPanel, gbc );
        outerPanel.add( controlPanel );

        return outerPanel;

    }

    //------------------------------------------------------------------

    private JPanel createPanelDate( )
    {

        //----  Date format list

        AppConfig config = AppConfig.getInstance( );

        // Selection list
        dateFormatList = new DateFormatSelectionList( config.getDateFormats( ) );
        dateFormatList.addActionListener( this );
        dateFormatList.addListSelectionListener( this );

        // Scroll pane: selection list
        dateFormatListScrollPane = new JScrollPane( dateFormatList, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                                                    JScrollPane.HORIZONTAL_SCROLLBAR_NEVER );
        dateFormatListScrollPane.getVerticalScrollBar( ).setFocusable( false );
        dateFormatListScrollPane.getVerticalScrollBar( ).getModel( ).addChangeListener( this );

        dateFormatList.setViewport( dateFormatListScrollPane.getViewport( ) );


        //----  Date format button panel

        JPanel dateFormatButtonPanel = new JPanel( new GridLayout( 0, 1, 0, 8 ) );

        // Button: add
        dateFormatAddButton = new FButton( ADD_STR + AppConstants.ELLIPSIS_STR );
        dateFormatAddButton.setMnemonic( KeyEvent.VK_A );
        dateFormatAddButton.setActionCommand( Command.ADD_DATE_FORMAT );
        dateFormatAddButton.addActionListener( this );
        dateFormatButtonPanel.add( dateFormatAddButton );

        // Button: edit
        dateFormatEditButton = new FButton( EDIT_STR + AppConstants.ELLIPSIS_STR );
        dateFormatEditButton.setMnemonic( KeyEvent.VK_E );
        dateFormatEditButton.setActionCommand( Command.EDIT_DATE_FORMAT );
        dateFormatEditButton.addActionListener( this );
        dateFormatButtonPanel.add( dateFormatEditButton );

        // Button: delete
        dateFormatDeleteButton = new FButton( DELETE_STR + AppConstants.ELLIPSIS_STR );
        dateFormatDeleteButton.setMnemonic( KeyEvent.VK_D );
        dateFormatDeleteButton.setActionCommand( Command.CONFIRM_DELETE_DATE_FORMAT );
        dateFormatDeleteButton.addActionListener( this );
        dateFormatButtonPanel.add( dateFormatDeleteButton );

        // Update buttons
        updateDateFormatButtons( );


        //----  Date format panel

        GridBagLayout gridBag = new GridBagLayout( );
        GridBagConstraints gbc = new GridBagConstraints( );

        JPanel dateFormatPanel = new JPanel( gridBag );
        TitledBorder.setPaddedBorder( dateFormatPanel, DATE_FORMATS_STR );

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets( 0, 0, 0, 0 );
        gridBag.setConstraints( dateFormatListScrollPane, gbc );
        dateFormatPanel.add( dateFormatListScrollPane );

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets( 0, 8, 0, 0 );
        gridBag.setConstraints( dateFormatButtonPanel, gbc );
        dateFormatPanel.add( dateFormatButtonPanel );


        //----  Names panel

        // Reset fixed-width labels and panels
        DatePanelLabel.reset( );
        DatePanel.reset( );

        JPanel namesPanel = new JPanel( gridBag );
        TitledBorder.setPaddedBorder( namesPanel, DATE_NAMES_STR );

        JPanel namesInnerPanel = new DatePanel( gridBag );

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets( 0, 0, 0, 0 );
        gridBag.setConstraints( namesInnerPanel, gbc );
        namesPanel.add( namesInnerPanel );

        int gridY = 0;

        // Label: source
        JLabel sourceLabel = new DatePanelLabel( SOURCE_STR );

        gbc.gridx = 0;
        gbc.gridy = gridY;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = AppConstants.COMPONENT_INSETS;
        gridBag.setConstraints( sourceLabel, gbc );
        namesInnerPanel.add( sourceLabel );

        // Combo box: source
        dateNamesSourceComboBox = new FComboBox<>( DateNamesSource.values( ) );
        dateNamesSourceComboBox.setSelectedValue( config.getDateNamesSource( ) );
        dateNamesSourceComboBox.setActionCommand( Command.SELECT_DATE_NAMES_SOURCE );
        dateNamesSourceComboBox.addActionListener( this );

        gbc.gridx = 1;
        gbc.gridy = gridY++;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = AppConstants.COMPONENT_INSETS;
        gridBag.setConstraints( dateNamesSourceComboBox, gbc );
        namesInnerPanel.add( dateNamesSourceComboBox );

        // Label: locale
        dateNamesComponents = new EnumMap<>( DateNamesSource.class );
        for ( DateNamesSource dateNamesSource : DateNamesSource.values( ) )
            dateNamesComponents.put( dateNamesSource, new ArrayList<Component>( ) );

        JLabel localeLabel = new DatePanelLabel( LOCALE_STR );
        dateNamesComponents.get( DateNamesSource.LOCALE ).add( localeLabel );

        gbc.gridx = 0;
        gbc.gridy = gridY;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = AppConstants.COMPONENT_INSETS;
        gridBag.setConstraints( localeLabel, gbc );
        namesInnerPanel.add( localeLabel );

        // Combo box: locale
        dateNamesLocaleComboBox = new FComboBox<>( );
        dateNamesComponents.get( DateNamesSource.LOCALE ).add( dateNamesLocaleComboBox );
        for ( LocaleEx locale : LocaleEx.LOCALES )
            dateNamesLocaleComboBox.addItem( locale );
        dateNamesLocaleComboBox.setSelectedValue( new LocaleEx( config.getDateNamesLocale( ) ) );
        if ( dateNamesLocaleComboBox.getSelectedIndex( ) < 0 )
            dateNamesLocaleComboBox.setSelectedIndex( 0 );

        gbc.gridx = 1;
        gbc.gridy = gridY++;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = AppConstants.COMPONENT_INSETS;
        gridBag.setConstraints( dateNamesLocaleComboBox, gbc );
        namesInnerPanel.add( dateNamesLocaleComboBox );

        // Label: user-defined
        JLabel userDefinedLabel = new DatePanelLabel( USER_DEFINED_STR );
        dateNamesComponents.get( DateNamesSource.USER_DEFINED ).add( userDefinedLabel );

        gbc.gridx = 0;
        gbc.gridy = gridY;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = AppConstants.COMPONENT_INSETS;
        gridBag.setConstraints( userDefinedLabel, gbc );
        namesInnerPanel.add( userDefinedLabel );

        // Panel: user-defined
        JPanel userDefinedPanel = new JPanel( new GridLayout( 1, 0, 8, 0 ) );

        gbc.gridx = 1;
        gbc.gridy = gridY++;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = AppConstants.COMPONENT_INSETS;
        gridBag.setConstraints( userDefinedPanel, gbc );
        namesInnerPanel.add( userDefinedPanel );

        // Button: edit month names
        editMonthNamesButton = new FButton( EDIT_MONTHS_STR + AppConstants.ELLIPSIS_STR );
        dateNamesComponents.get( DateNamesSource.USER_DEFINED ).add( editMonthNamesButton );
        editMonthNamesButton.setMargin( EDIT_NAMES_BUTTON_MARGINS );
        editMonthNamesButton.setActionCommand( Command.EDIT_MONTH_NAMES );
        editMonthNamesButton.addActionListener( this );

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets( 0, 0, 0, 0 );
        gridBag.setConstraints( editMonthNamesButton, gbc );
        userDefinedPanel.add( editMonthNamesButton );

        // Button: edit month names
        editDayNamesButton = new FButton( EDIT_DAYS_STR + AppConstants.ELLIPSIS_STR );
        dateNamesComponents.get( DateNamesSource.USER_DEFINED ).add( editDayNamesButton );
        editDayNamesButton.setMargin( EDIT_NAMES_BUTTON_MARGINS );
        editDayNamesButton.setActionCommand( Command.EDIT_DAY_NAMES );
        editDayNamesButton.addActionListener( this );

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets( 0, 6, 0, 0 );
        gridBag.setConstraints( editDayNamesButton, gbc );
        userDefinedPanel.add( editDayNamesButton );

        // Enable/disable components according to source of names
        updateDateNamesSource( );


        //----  Control panel

        JPanel controlPanel = new JPanel( gridBag );
        GuiUtilities.setPaddedLineBorder( controlPanel );

        JPanel controlInnerPanel = new DatePanel( gridBag );

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets( 0, 0, 0, 0 );
        gridBag.setConstraints( controlInnerPanel, gbc );
        controlPanel.add( controlInnerPanel );

        gridY = 0;

        // Label: first day of week
        JLabel firstDayOfWeekLabel = new DatePanelLabel( FIRST_DAY_OF_WEEK_STR );

        gbc.gridx = 0;
        gbc.gridy = gridY;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = AppConstants.COMPONENT_INSETS;
        gridBag.setConstraints( firstDayOfWeekLabel, gbc );
        controlInnerPanel.add( firstDayOfWeekLabel );

        // Combo box: first day of week
        firstDayOfWeekComboBox = new FComboBox<>( );
        firstDayOfWeekComboBox.addItem( DEFAULT_STR );
        for ( String str : DateUtilities.getDayNames( Locale.getDefault( ) ) )
            firstDayOfWeekComboBox.addItem( str );
        firstDayOfWeekComboBox.setSelectedIndex( config.getFirstDayOfWeek( ) );

        gbc.gridx = 1;
        gbc.gridy = gridY++;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = AppConstants.COMPONENT_INSETS;
        gridBag.setConstraints( firstDayOfWeekComboBox, gbc );
        controlInnerPanel.add( firstDayOfWeekComboBox );

        // Update widths of labels and panels
        DatePanelLabel.update( );
        DatePanel.update( );


        //----  Outer panel

        JPanel outerPanel = new JPanel( gridBag );
        outerPanel.setBorder( BorderFactory.createEmptyBorder( 2, 2, 2, 2 ) );

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
        gridBag.setConstraints( dateFormatPanel, gbc );
        outerPanel.add( dateFormatPanel );

        gbc.gridx = 0;
        gbc.gridy = gridY++;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets( 3, 0, 0, 0 );
        gridBag.setConstraints( namesPanel, gbc );
        outerPanel.add( namesPanel );

        gbc.gridx = 0;
        gbc.gridy = gridY++;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets( 3, 0, 0, 0 );
        gridBag.setConstraints( controlPanel, gbc );
        outerPanel.add( controlPanel );

        return outerPanel;

    }

    //------------------------------------------------------------------

    private JPanel createPanelFonts( )
    {

        //----  Control panel

        GridBagLayout gridBag = new GridBagLayout( );
        GridBagConstraints gbc = new GridBagConstraints( );

        JPanel controlPanel = new JPanel( gridBag );
        GuiUtilities.setPaddedLineBorder( controlPanel );

        String[] fontNames =
                        GraphicsEnvironment.getLocalGraphicsEnvironment( ).getAvailableFontFamilyNames( );
        fontPanels = new FontPanel[AppFont.getNumFonts( )];
        for ( int i = 0; i < fontPanels.length; ++i )
        {
            FontEx fontEx = AppConfig.getInstance( ).getFont( i );
            fontPanels[i] = new FontPanel( fontEx, fontNames );

            int gridX = 0;

            // Label: font
            JLabel fontLabel = new FLabel( AppFont.values( )[i].toString( ) + ":" );

            gbc.gridx = gridX++;
            gbc.gridy = i;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.weightx = 0.0;
            gbc.weighty = 0.0;
            gbc.anchor = GridBagConstraints.LINE_END;
            gbc.fill = GridBagConstraints.NONE;
            gbc.insets = AppConstants.COMPONENT_INSETS;
            gridBag.setConstraints( fontLabel, gbc );
            controlPanel.add( fontLabel );

            // Combo box: font name
            gbc.gridx = gridX++;
            gbc.gridy = i;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.weightx = 0.0;
            gbc.weighty = 0.0;
            gbc.anchor = GridBagConstraints.LINE_START;
            gbc.fill = GridBagConstraints.NONE;
            gbc.insets = AppConstants.COMPONENT_INSETS;
            gridBag.setConstraints( fontPanels[i].nameComboBox, gbc );
            controlPanel.add( fontPanels[i].nameComboBox );

            // Combo box: font style
            gbc.gridx = gridX++;
            gbc.gridy = i;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.weightx = 0.0;
            gbc.weighty = 0.0;
            gbc.anchor = GridBagConstraints.LINE_START;
            gbc.fill = GridBagConstraints.NONE;
            gbc.insets = AppConstants.COMPONENT_INSETS;
            gridBag.setConstraints( fontPanels[i].styleComboBox, gbc );
            controlPanel.add( fontPanels[i].styleComboBox );

            // Panel: font size
            JPanel sizePanel = new JPanel( gridBag );

            gbc.gridx = gridX++;
            gbc.gridy = i;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.weightx = 0.0;
            gbc.weighty = 0.0;
            gbc.anchor = GridBagConstraints.LINE_START;
            gbc.fill = GridBagConstraints.NONE;
            gbc.insets = AppConstants.COMPONENT_INSETS;
            gridBag.setConstraints( sizePanel, gbc );
            controlPanel.add( sizePanel );

            // Spinner: font size
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.weightx = 0.0;
            gbc.weighty = 0.0;
            gbc.anchor = GridBagConstraints.LINE_START;
            gbc.fill = GridBagConstraints.NONE;
            gbc.insets = new Insets( 0, 0, 0, 0 );
            gridBag.setConstraints( fontPanels[i].sizeSpinner, gbc );
            sizePanel.add( fontPanels[i].sizeSpinner );

            // Label: "pt"
            JLabel ptLabel = new FLabel( PT_STR );

            gbc.gridx = 1;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.weightx = 0.0;
            gbc.weighty = 0.0;
            gbc.anchor = GridBagConstraints.LINE_START;
            gbc.fill = GridBagConstraints.NONE;
            gbc.insets = new Insets( 0, 4, 0, 0 );
            gridBag.setConstraints( ptLabel, gbc );
            sizePanel.add( ptLabel );
        }


        //----  Outer panel

        JPanel outerPanel = new JPanel( gridBag );
        outerPanel.setBorder( BorderFactory.createEmptyBorder( 2, 2, 2, 2 ) );

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets( 0, 0, 0, 0 );
        gridBag.setConstraints( controlPanel, gbc );
        outerPanel.add( controlPanel );

        return outerPanel;

    }

    //------------------------------------------------------------------

    private void validatePreferencesGeneral( )
    {
        // do nothing
    }

    //------------------------------------------------------------------

    private void validatePreferencesAppearance( )
    {
        // do nothing
    }

    //------------------------------------------------------------------

    private void validatePreferencesDate( )
    {
        // do nothing
    }

    //------------------------------------------------------------------

    private void validatePreferencesFonts( )
    {
        // do nothing
    }

    //------------------------------------------------------------------

    private void setPreferencesGeneral( )
    {
        AppConfig config = AppConfig.getInstance( );
        config.setShowUnixPathnames( showUnixPathnamesComboBox.getSelectedValue( ) );
        config.setSelectTextOnFocusGained( selectTextOnFocusGainedComboBox.getSelectedValue( ) );
        if ( saveMainWindowLocationComboBox.getSelectedValue( ) != config.isMainWindowLocation( ) )
            config.setMainWindowLocation( saveMainWindowLocationComboBox.getSelectedValue( ) ? new Point( )
                                                                                             : null );
    }

    //------------------------------------------------------------------

    private void setPreferencesAppearance( )
    {
        AppConfig config = AppConfig.getInstance( );
        if ( lookAndFeelComboBox.isEnabled( ) && (lookAndFeelComboBox.getSelectedIndex( ) >= 0) )
            config.setLookAndFeel( lookAndFeelComboBox.getSelectedValue( ) );
        config.setTextAntialiasing( textAntialiasingComboBox.getSelectedValue( ) );
    }

    //------------------------------------------------------------------

    private void setPreferencesDate( )
    {
        AppConfig config = AppConfig.getInstance( );
        config.setDateFormats( dateFormatList.getElements( ) );
        config.setDateNamesSource( getDateNamesSource( ) );
        switch ( getDateNamesSource( ) )
        {
            case LOCALE:
                config.setDateNamesLocale( dateNamesLocaleComboBox.getSelectedValue( ).getKey( ) );
                break;

            case USER_DEFINED:
                if ( monthNames != null )
                    config.setMonthNames( monthNames );
                if ( dayNames != null )
                    config.setDayNames( dayNames );
                break;
        }
        config.setFirstDayOfWeek( firstDayOfWeekComboBox.getSelectedIndex( ) );
    }

    //------------------------------------------------------------------

    private void setPreferencesFonts( )
    {
        for ( int i = 0; i < fontPanels.length; ++i )
        {
            if ( fontPanels[i].nameComboBox.getSelectedIndex( ) >= 0 )
                AppConfig.getInstance( ).setFont( i, fontPanels[i].getFont( ) );
        }
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class variables
////////////////////////////////////////////////////////////////////////

    private static  Point   location;
    private static  int     tabIndex;

////////////////////////////////////////////////////////////////////////
//  Static initialiser
////////////////////////////////////////////////////////////////////////

    static
    {
        COMMAND_MAP = new HashMap<>( );
        COMMAND_MAP.put( SingleSelectionList.Command.EDIT_ELEMENT,
                         Command.EDIT_DATE_FORMAT );
        COMMAND_MAP.put( SingleSelectionList.Command.DELETE_ELEMENT,
                         Command.CONFIRM_DELETE_DATE_FORMAT );
        COMMAND_MAP.put( SingleSelectionList.Command.DELETE_EX_ELEMENT,
                         Command.DELETE_DATE_FORMAT );
        COMMAND_MAP.put( SingleSelectionList.Command.MOVE_ELEMENT_UP,
                         Command.MOVE_DATE_FORMAT_UP );
        COMMAND_MAP.put( SingleSelectionList.Command.MOVE_ELEMENT_DOWN,
                         Command.MOVE_DATE_FORMAT_DOWN );
        COMMAND_MAP.put( SingleSelectionList.Command.DRAG_ELEMENT,
                         Command.MOVE_DATE_FORMAT );
    }

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

    // Main panel
    private boolean                                 accepted;
    private JTabbedPane                             tabbedPanel;

    // General panel
    private BooleanComboBox                         showUnixPathnamesComboBox;
    private BooleanComboBox                         selectTextOnFocusGainedComboBox;
    private BooleanComboBox                         saveMainWindowLocationComboBox;

    // Appearance panel
    private FComboBox<String>                       lookAndFeelComboBox;
    private FComboBox<TextRendering.Antialiasing>   textAntialiasingComboBox;

    // Date panel
    private DateFormatSelectionList                 dateFormatList;
    private JScrollPane                             dateFormatListScrollPane;
    private JButton                                 dateFormatAddButton;
    private JButton                                 dateFormatEditButton;
    private JButton                                 dateFormatDeleteButton;
    private FComboBox<DateNamesSource>              dateNamesSourceComboBox;
    private FComboBox<LocaleEx>                     dateNamesLocaleComboBox;
    private JButton                                 editMonthNamesButton;
    private JButton                                 editDayNamesButton;
    private Map<DateNamesSource, List<Component>>   dateNamesComponents;
    private List<String>                            monthNames;
    private List<String>                            dayNames;
    private FComboBox<String>                       firstDayOfWeekComboBox;

    // Fonts panel
    private FontPanel[]                             fontPanels;

}

//----------------------------------------------------------------------
