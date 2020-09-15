/*====================================================================*\

App.java

Application class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.datecalculator;

//----------------------------------------------------------------------


// IMPORTS


import java.io.IOException;

import java.time.LocalDateTime;

import java.time.format.DateTimeFormatter;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import uk.blankaspect.common.cls.ClassUtils;

import uk.blankaspect.common.exception.ExceptionUtils;

import uk.blankaspect.common.exception2.LocationException;

import uk.blankaspect.common.logging.ErrorLogger;

import uk.blankaspect.common.resource.ResourceProperties;

import uk.blankaspect.common.swing.text.TextRendering;

import uk.blankaspect.common.swing.textfield.TextFieldUtils;

//----------------------------------------------------------------------


// APPLICATION CLASS


public class App
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	public static final		App		INSTANCE	= new App();

	public static final		String	SHORT_NAME	= "DateCalculator";
	public static final		String	LONG_NAME	= "Date calculator";
	public static final		String	NAME_KEY	= "dateCalculator";

	private static final	String	VERSION_PROPERTY_KEY	= "version";
	private static final	String	BUILD_PROPERTY_KEY		= "build";
	private static final	String	RELEASE_PROPERTY_KEY	= "release";

	private static final	String	VERSION_DATE_TIME_PATTERN	= "uuuuMMdd-HHmmss";

	private static final	String	BUILD_PROPERTIES_FILENAME	= "build.properties";

	private static final	String	CONFIG_ERROR_STR	= "Configuration error";
	private static final	String	LAF_ERROR1_STR		= "Look-and-feel: ";
	private static final	String	LAF_ERROR2_STR		= "\nThe look-and-feel is not installed.";

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	private App()
	{
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static void main(String[] args)
	{
		INSTANCE.init();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public MainWindow getMainWindow()
	{
		return mainWindow;
	}

	//------------------------------------------------------------------

	/**
	 * Returns a string representation of the version of this application.  If this class was loaded from a JAR, the
	 * string is created from the values of properties that are defined in a resource named 'build.properties';
	 * otherwise, the string is created from the date and time when this method is first called.
	 *
	 * @return a string representation of the version of this application.
	 */

	public String getVersionString()
	{
		if (versionStr == null)
		{
			StringBuilder buffer = new StringBuilder(32);
			if (ClassUtils.isFromJar(getClass()))
			{
				// Append version number
				String str = buildProperties.get(VERSION_PROPERTY_KEY);
				if (str != null)
					buffer.append(str);

				// If this is not a release, append build
				boolean release = Boolean.parseBoolean(buildProperties.get(RELEASE_PROPERTY_KEY));
				if (!release)
				{
					str = buildProperties.get(BUILD_PROPERTY_KEY);
					if (str != null)
					{
						if (buffer.length() > 0)
							buffer.append(' ');
						buffer.append(str);
					}
				}
			}
			else
			{
				buffer.append('b');
				buffer.append(DateTimeFormatter.ofPattern(VERSION_DATE_TIME_PATTERN).format(LocalDateTime.now()));
			}
			versionStr = buffer.toString();
		}
		return versionStr;
	}

	//------------------------------------------------------------------

	public void showWarningMessage(String titleStr,
								   Object message)
	{
		showMessageDialog(titleStr, message, JOptionPane.WARNING_MESSAGE);
	}

	//------------------------------------------------------------------

	public void showErrorMessage(String titleStr,
								 Object message)
	{
		showMessageDialog(titleStr, message, JOptionPane.ERROR_MESSAGE);
	}

	//------------------------------------------------------------------

	public void showMessageDialog(String titleStr,
								  Object message,
								  int    messageKind)
	{
		JOptionPane.showMessageDialog(mainWindow, message, titleStr, messageKind);
	}

	//------------------------------------------------------------------

	private void init()
	{
		// Log stack trace of uncaught exception
		if (ClassUtils.isFromJar(getClass()))
		{
			Thread.setDefaultUncaughtExceptionHandler((thread, exception) ->
			{
				try
				{
					ErrorLogger.INSTANCE.write(exception);
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			});
		}

		// Read build properties
		try
		{
			buildProperties = new ResourceProperties(BUILD_PROPERTIES_FILENAME);
		}
		catch (LocationException e)
		{
			e.printStackTrace();
		}

		// Read configuration
		AppConfig config = AppConfig.INSTANCE;
		config.read();

		// Set UNIX style for pathnames in file exceptions
		ExceptionUtils.setUnixStyle(config.isShowUnixPathnames());

		// Set text antialiasing
		TextRendering.setAntialiasing(config.getTextAntialiasing());

		// Set look-and-feel
		String lookAndFeelName = config.getLookAndFeel();
		for (UIManager.LookAndFeelInfo lookAndFeelInfo : UIManager.getInstalledLookAndFeels())
		{
			if (lookAndFeelInfo.getName().equals(lookAndFeelName))
			{
				try
				{
					UIManager.setLookAndFeel(lookAndFeelInfo.getClassName());
				}
				catch (Exception e)
				{
					// ignore
				}
				lookAndFeelName = null;
				break;
			}
		}
		if (lookAndFeelName != null)
			showWarningMessage(SHORT_NAME + " : " + CONFIG_ERROR_STR,
							   LAF_ERROR1_STR + lookAndFeelName + LAF_ERROR2_STR);

		// Select all text when a text field gains focus
		if (config.isSelectTextOnFocusGained())
			TextFieldUtils.selectAllOnFocusGained();

		// Update the names of months and days
		config.updateDateNames();

		// Create main window
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				mainWindow = new MainWindow(LONG_NAME + " " + getVersionString());
			}
		});
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	ResourceProperties	buildProperties;
	private	String				versionStr;
	private	MainWindow			mainWindow;

}

//----------------------------------------------------------------------
