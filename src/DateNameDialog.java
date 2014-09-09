/*====================================================================*\

DateNameDialog.java

Date name dialog box class.

\*====================================================================*/


// IMPORTS


import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Window;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import uk.org.blankaspect.gui.FButton;
import uk.org.blankaspect.gui.FLabel;
import uk.org.blankaspect.gui.GuiUtilities;

import uk.org.blankaspect.util.KeyAction;

//----------------------------------------------------------------------


// DATE NAME DIALOG BOX CLASS


class DateNameDialog
    extends JDialog
    implements ActionListener
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

    private static final    int NAME_FIELD_NUM_COLUMNS  = 24;

    private static final    String  SET_FROM_LOCALE_STR = "Set from locale";
    private static final    String  CLEAR_ALL_STR       = "Clear all";
    private static final    String  CLEAR_MESSAGE_STR   = "Do you want to clear all the names?";

    private static final    Color   FIELD_BORDER_COLOUR = new Color( 160, 184, 184 );

    private static final    Insets  NAME_BUTTON_MARGINS = new Insets( 2, 4, 2, 4 );

    // Commands
    private interface Command
    {
        String  MOVE_UP         = "moveUp";
        String  MOVE_DOWN       = "moveDown";
        String  SET_FROM_LOCALE = "setFromLocale";
        String  CLEAR_ALL       = "clearAll";
        String  ACCEPT          = "accept";
        String  CLOSE           = "close";
    }

    private static final    KeyAction.KeyCommandPair[]  KEY_COMMANDS    =
    {
        new KeyAction.KeyCommandPair
        (
            KeyStroke.getKeyStroke( KeyEvent.VK_UP, 0 ),
            Command.MOVE_UP
        ),
        new KeyAction.KeyCommandPair
        (
            KeyStroke.getKeyStroke( KeyEvent.VK_DOWN, 0 ),
            Command.MOVE_DOWN
        ),
        new KeyAction.KeyCommandPair
        (
            KeyStroke.getKeyStroke( KeyEvent.VK_ESCAPE, 0 ),
            Command.CLOSE
        )
    };

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

    private DateNameDialog( Window       owner,
                            String       titleStr,
                            List<String> names,
                            List<String> localeNames )
    {

        // Call superclass constructor
        super( owner, titleStr, Dialog.ModalityType.APPLICATION_MODAL );

        // Set icons
        setIconImages( owner.getIconImages( ) );

        // Initialise instance variables
        this.localeNames = localeNames;


        //----  Name panel

        GridBagLayout gridBag = new GridBagLayout( );
        GridBagConstraints gbc = new GridBagConstraints( );

        JPanel namePanel = new JPanel( gridBag );
        nameFields = new JTextField[names.size( )];
        for ( int i = 0; i < nameFields.length; ++i )
        {
            // Label: index
            JLabel indexLabel = new FLabel( Integer.toString( i + 1 ) );

            gbc.gridx = 0;
            gbc.gridy = i;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.weightx = 0.0;
            gbc.weighty = 0.0;
            gbc.anchor = GridBagConstraints.LINE_END;
            gbc.fill = GridBagConstraints.NONE;
            gbc.insets = new Insets( (i == 0) ? 0 : -1, 0, 0, 0 );
            gridBag.setConstraints( indexLabel, gbc );
            namePanel.add( indexLabel );

            // Field: name
            JTextField nameField = new JTextField( names.get( i ), NAME_FIELD_NUM_COLUMNS );
            nameFields[i] = nameField;
            AppFont.TEXT_FIELD.apply( nameField );
            GuiUtilities.setPaddedLineBorder( nameField, 1, 4, FIELD_BORDER_COLOUR );
            nameField.setName( Integer.toString( i ) );

            gbc.gridx = 1;
            gbc.gridy = i;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.weightx = 0.0;
            gbc.weighty = 0.0;
            gbc.anchor = GridBagConstraints.LINE_START;
            gbc.fill = GridBagConstraints.NONE;
            gbc.insets = new Insets( (i == 0) ? 0 : -1, 6, 0, 0 );
            gridBag.setConstraints( nameField, gbc );
            namePanel.add( nameField );
        }


        //----  Name button panel

        JPanel nameButtonPanel = new JPanel( new GridLayout( 1, 0, 8, 0 ) );
        nameButtonPanel.setBorder( BorderFactory.createEmptyBorder( 3, 8, 3, 8 ) );

        // Button: set from locale
        JButton setFromLocaleButton = new FButton( SET_FROM_LOCALE_STR );
        setFromLocaleButton.setMargin( NAME_BUTTON_MARGINS );
        setFromLocaleButton.setActionCommand( Command.SET_FROM_LOCALE );
        setFromLocaleButton.addActionListener( this );
        nameButtonPanel.add( setFromLocaleButton );

        // Button: clear all
        JButton clearAllButton = new FButton( CLEAR_ALL_STR + AppConstants.ELLIPSIS_STR );
        clearAllButton.setMargin( NAME_BUTTON_MARGINS );
        clearAllButton.setActionCommand( Command.CLEAR_ALL );
        clearAllButton.addActionListener( this );
        nameButtonPanel.add( clearAllButton );


        //----  Control panel

        JPanel controlPanel = new JPanel( gridBag );
        GuiUtilities.setPaddedLineBorder( controlPanel );

        int gridY = 0;

        gbc.gridx = 0;
        gbc.gridy = gridY++;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets( 0, 0, 0, 0 );
        gridBag.setConstraints( namePanel, gbc );
        controlPanel.add( namePanel );

        gbc.gridx = 0;
        gbc.gridy = gridY++;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets( 3, 0, 0, 0 );
        gridBag.setConstraints( nameButtonPanel, gbc );
        controlPanel.add( nameButtonPanel );


        //----  Button panel

        JPanel buttonPanel = new JPanel( new GridLayout( 1, 0, 8, 0 ) );
        buttonPanel.setBorder( BorderFactory.createEmptyBorder( 3, 8, 3, 8 ) );

        // Button: OK
        JButton okButton = new FButton( AppConstants.OK_STR );
        okButton.setActionCommand( Command.ACCEPT );
        okButton.addActionListener( this );
        buttonPanel.add( okButton );

        // Button: cancel
        JButton cancelButton = new FButton( AppConstants.CANCEL_STR );
        cancelButton.setActionCommand( Command.CLOSE );
        cancelButton.addActionListener( this );
        buttonPanel.add( cancelButton );


        //----  Main panel

        JPanel mainPanel = new JPanel( gridBag );
        mainPanel.setBorder( BorderFactory.createEmptyBorder( 2, 2, 2, 2 ) );

        gridY = 0;

        gbc.gridx = 0;
        gbc.gridy = gridY++;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets( 0, 0, 0, 0 );
        gridBag.setConstraints( controlPanel, gbc );
        mainPanel.add( controlPanel );

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
        mainPanel.add( buttonPanel );

        // Add commands to action map
        KeyAction.create( mainPanel, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, this, KEY_COMMANDS );


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

    public static List<String> showDialog( Component    parent,
                                           String       titleStr,
                                           List<String> names,
                                           List<String> localeNames )
    {
        return new DateNameDialog( GuiUtilities.getWindow( parent ), titleStr, names, localeNames ).
                                                                                                getNames( );
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : ActionListener interface
////////////////////////////////////////////////////////////////////////

    public void actionPerformed( ActionEvent event )
    {
        String command = event.getActionCommand( );

        if ( command.equals( Command.MOVE_UP ) )
            onMoveUp( );

        else if ( command.equals( Command.MOVE_DOWN ) )
            onMoveDown( );

        else if ( command.equals( Command.SET_FROM_LOCALE ) )
            onSetFromLocale( );

        else if ( command.equals( Command.CLEAR_ALL ) )
            onClearAll( );

        else if ( command.equals( Command.ACCEPT ) )
            onAccept( );

        else if ( command.equals( Command.CLOSE ) )
            onClose( );
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

    public List<String> getNames( )
    {
        List<String> names = null;
        if ( accepted )
        {
            names = new ArrayList<>( );
            for ( JTextField nameField : nameFields )
                names.add( nameField.getText( ) );
        }
        return names;
    }

    //------------------------------------------------------------------

    private int getFocusIndex( )
    {
        int index = -1;
        Component component = getFocusOwner( );
        if ( component != null )
        {
            String str = component.getName( );
            if ( str != null )
                index = Integer.parseInt( str );
        }
        return index;
    }

    //------------------------------------------------------------------

    private void onMoveUp( )
    {
        int index = getFocusIndex( );
        if ( index > 0 )
            nameFields[index - 1].requestFocusInWindow( );
    }

    //------------------------------------------------------------------

    private void onMoveDown( )
    {
        int index = getFocusIndex( );
        if ( (index >= 0) && (index < nameFields.length - 1) )
            nameFields[index + 1].requestFocusInWindow( );
    }

    //------------------------------------------------------------------

    private void onSetFromLocale( )
    {
        for ( int i = 0; i < nameFields.length; ++i )
            nameFields[i].setText( localeNames.get( i ) );
    }

    //------------------------------------------------------------------

    private void onClearAll( )
    {
        String[] optionStrs = Util.getOptionStrings( AppConstants.CLEAR_STR );
        if ( JOptionPane.showOptionDialog( this, CLEAR_MESSAGE_STR, CLEAR_ALL_STR,
                                           JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                                           optionStrs, optionStrs[1] ) == JOptionPane.OK_OPTION )
        {
            for ( JTextField nameField : nameFields )
                nameField.setText( null );
        }
    }

    //------------------------------------------------------------------

    private void onAccept( )
    {
        accepted = true;
        onClose( );
    }

    //------------------------------------------------------------------

    private void onClose( )
    {
        location = getLocation( );
        setVisible( false );
        dispose( );
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class variables
////////////////////////////////////////////////////////////////////////

    private static  Point   location;

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

    private boolean         accepted;
    private List<String>    localeNames;
    private JTextField[]    nameFields;

}

//----------------------------------------------------------------------
