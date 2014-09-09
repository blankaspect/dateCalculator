/*====================================================================*\

App.java

Application class.

\*====================================================================*/


// IMPORTS


import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import uk.org.blankaspect.exception.ExceptionUtilities;

import uk.org.blankaspect.gui.TextRendering;

import uk.org.blankaspect.textfield.TextFieldUtilities;

import uk.org.blankaspect.util.CalendarTime;
import uk.org.blankaspect.util.NoYes;
import uk.org.blankaspect.util.ResourceProperties;

//----------------------------------------------------------------------


// APPLICATION CLASS


class App
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

    public static final     String  SHORT_NAME  = "DateCalculator";
    public static final     String  LONG_NAME   = "Date calculator";
    public static final     String  NAME_KEY    = "dateCalculator";

    private static final    String  DEBUG_PROPERTY_KEY      = "app.debug";
    private static final    String  VERSION_PROPERTY_KEY    = "version";
    private static final    String  BUILD_PROPERTY_KEY      = "build";
    private static final    String  RELEASE_PROPERTY_KEY    = "release";

    private static final    String  BUILD_PROPERTIES_PATHNAME   = "resources/build.properties";

    private static final    String  DEBUG_STR           = " Debug";
    private static final    String  CONFIG_ERROR_STR    = "Configuration error";
    private static final    String  LAF_ERROR1_STR      = "Look-and-feel: ";
    private static final    String  LAF_ERROR2_STR      = "\nThe look-and-feel is not installed.";

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

    private App( )
    {
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

    public static void main( String[] args )
    {
        getInstance( ).init( );
    }

    //------------------------------------------------------------------

    public static App getInstance( )
    {
        if ( instance == null )
            instance = new App( );
        return instance;
    }

    //------------------------------------------------------------------

    public static boolean isDebug( )
    {
        return debug;
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

    public MainWindow getMainWindow( )
    {
        return mainWindow;
    }

    //------------------------------------------------------------------

    public String getVersionString( )
    {
        StringBuilder buffer = new StringBuilder( 32 );
        String str = buildProperties.get( VERSION_PROPERTY_KEY );
        if ( str != null )
            buffer.append( str );

        str = buildProperties.get( RELEASE_PROPERTY_KEY );
        if ( str == null )
        {
            long time = System.currentTimeMillis( );
            if ( buffer.length( ) > 0 )
                buffer.append( ' ' );
            buffer.append( 'b' );
            buffer.append( CalendarTime.dateToString( time ) );
            buffer.append( '-' );
            buffer.append( CalendarTime.hoursMinsToString( time ) );
        }
        else
        {
            NoYes release = NoYes.forKey( str );
            if ( (release == null) || !release.toBoolean( ) )
            {
                str = buildProperties.get( BUILD_PROPERTY_KEY );
                if ( str != null )
                {
                    if ( buffer.length( ) > 0 )
                        buffer.append( ' ' );
                    buffer.append( str );
                }
            }
        }

        if ( debug )
            buffer.append( DEBUG_STR );

        return buffer.toString( );
    }

    //------------------------------------------------------------------

    public void showWarningMessage( String titleStr,
                                    Object message )
    {
        showMessageDialog( titleStr, message, JOptionPane.WARNING_MESSAGE );
    }

    //------------------------------------------------------------------

    public void showErrorMessage( String titleStr,
                                  Object message )
    {
        showMessageDialog( titleStr, message, JOptionPane.ERROR_MESSAGE );
    }

    //------------------------------------------------------------------

    public void showMessageDialog( String titleStr,
                                   Object message,
                                   int    messageKind )
    {
        JOptionPane.showMessageDialog( mainWindow, message, titleStr, messageKind );
    }

    //------------------------------------------------------------------

    private void init( )
    {
        // Set runtime debug flag
        debug = (System.getProperty( DEBUG_PROPERTY_KEY ) != null);

        // Read build properties
        buildProperties = new ResourceProperties( BUILD_PROPERTIES_PATHNAME, getClass( ) );

        // Read configuration
        AppConfig config = AppConfig.getInstance( );
        config.read( );

        // Set UNIX style for pathnames in file exceptions
        ExceptionUtilities.setUnixStyle( config.isShowUnixPathnames( ) );

        // Set text antialiasing
        TextRendering.setAntialiasing( config.getTextAntialiasing( ) );

        // Set look-and-feel
        String lookAndFeelName = config.getLookAndFeel( );
        for ( UIManager.LookAndFeelInfo lookAndFeelInfo : UIManager.getInstalledLookAndFeels( ) )
        {
            if ( lookAndFeelInfo.getName( ).equals( lookAndFeelName ) )
            {
                try
                {
                    UIManager.setLookAndFeel( lookAndFeelInfo.getClassName( ) );
                }
                catch ( Exception e )
                {
                    // ignore
                }
                lookAndFeelName = null;
                break;
            }
        }
        if ( lookAndFeelName != null )
            showWarningMessage( SHORT_NAME + " | " + CONFIG_ERROR_STR,
                                LAF_ERROR1_STR + lookAndFeelName + LAF_ERROR2_STR );

        // Select all text when a text field gains focus
        if ( config.isSelectTextOnFocusGained( ) )
            TextFieldUtilities.selectAllOnFocusGained( );

        // Update the names of months and days
        config.updateDateNames( );

        // Create main window
        SwingUtilities.invokeLater( new Runnable( )
        {
            public void run( )
            {
                mainWindow = new MainWindow( LONG_NAME + " " + getVersionString( ) );
            }
        } );
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class variables
////////////////////////////////////////////////////////////////////////

    private static  App     instance;
    private static  boolean debug;

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

    private ResourceProperties  buildProperties;
    private MainWindow          mainWindow;

}

//----------------------------------------------------------------------
