/*====================================================================*\

AppConfig.java

Application configuration class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.datecalculator;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.Component;
import java.awt.Point;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.lang.reflect.Field;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import javax.swing.JFileChooser;
import javax.swing.UIManager;

import uk.blankaspect.common.date.DateFormat;
import uk.blankaspect.common.date.DateUtils;

import uk.blankaspect.common.exception.AppException;
import uk.blankaspect.common.exception.FileException;

import uk.blankaspect.common.filesystem.PathnameUtils;

import uk.blankaspect.common.misc.FilenameSuffixFilter;

import uk.blankaspect.common.property.Property;
import uk.blankaspect.common.property.PropertySet;

import uk.blankaspect.common.swing.font.FontEx;

import uk.blankaspect.common.swing.text.TextRendering;

import uk.blankaspect.common.ui.progress.IProgressView;

//----------------------------------------------------------------------


// APPLICATION CONFIGURATION CLASS


class AppConfig
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	public static final		AppConfig	INSTANCE;

	public static final		int	MAX_NUM_DATE_FORMATS	= 64;

	private static final	int	VERSION					= 0;
	private static final	int	MIN_SUPPORTED_VERSION	= 0;
	private static final	int	MAX_SUPPORTED_VERSION	= 0;

	private static final	String	CONFIG_ERROR_STR	= "Configuration error";
	private static final	String	CONFIG_DIR_KEY		= Property.APP_PREFIX + "configDir";
	private static final	String	PROPERTIES_FILENAME	= App.NAME_KEY + "-properties" +
																			AppConstants.XML_FILE_SUFFIX;
	private static final	String	FILENAME_STEM		= App.NAME_KEY + "-config";
	private static final	String	CONFIG_FILENAME		= FILENAME_STEM + AppConstants.XML_FILE_SUFFIX;
	private static final	String	CONFIG_OLD_FILENAME	= FILENAME_STEM + "-old" +
																			AppConstants.XML_FILE_SUFFIX;

	private static final	String	SAVE_CONFIGURATION_FILE_STR	= "Save configuration file";
	private static final	String	WRITING_STR					= "Writing";

	private interface Key
	{
		String	APPEARANCE					= "appearance";
		String	CONFIGURATION				= App.NAME_KEY + "Configuration";
		String	DATE						= "date";
		String	DAY_NAME					= "dayName";
		String	FIRST_DAY_OF_WEEK			= "firstDayOfWeek";
		String	FONT						= "font";
		String	FORMAT						= "format";
		String	GENERAL						= "general";
		String	LOOK_AND_FEEL				= "lookAndFeel";
		String	MAIN_WINDOW_LOCATION		= "mainWindowLocation";
		String	MONTH_NAME					= "monthName";
		String	NAMES_LOCALE				= "namesLocale";
		String	NAMES_SOURCE				= "namesSource";
		String	SELECT_TEXT_ON_FOCUS_GAINED	= "selectTextOnFocusGained";
		String	SHOW_ADJACENT_MONTHS		= "showAdjacentMonths";
		String	SHOW_UNIX_PATHNAMES			= "showUnixPathnames";
		String	TEXT_ANTIALIASING			= "textAntialiasing";
	}

////////////////////////////////////////////////////////////////////////
//  Enumerated types
////////////////////////////////////////////////////////////////////////


	// ERROR IDENTIFIERS


	private enum ErrorId
		implements AppException.IId
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		ERROR_READING_PROPERTIES_FILE
		("An error occurred when reading the properties file."),

		NO_CONFIGURATION_FILE
		("No configuration file was found at the specified location."),

		NO_VERSION_NUMBER
		("The configuration file does not have a version number."),

		INVALID_VERSION_NUMBER
		("The version number of the configuration file is invalid."),

		UNSUPPORTED_CONFIGURATION_FILE
		("The version of the configuration file (%1) is not supported by this version of " +
			App.SHORT_NAME + "."),

		FAILED_TO_CREATE_DIRECTORY
		("Failed to create the directory for the configuration file."),

		DUPLICATE_DATE_FORMAT_NAME
		("There is more than one date format with the name \"%1\"."),

		UNSUPPORTED_LOCALE
		("The locale '%1' is not supported by this implementation of Java.\n" +
			"The locale '%2' will be used instead for the names of months and days.");

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private ErrorId(String message)
		{
			this.message = message;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : AppException.IId interface
	////////////////////////////////////////////////////////////////////

		public String getMessage()
		{
			return message;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	String	message;

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


	// CONFIGURATION FILE CLASS


	private static class ConfigFile
		extends PropertySet
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	String	CONFIG_FILE1_STR	= "configuration file";
		private static final	String	CONFIG_FILE2_STR	= "Configuration file";

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private ConfigFile()
		{
		}

		//--------------------------------------------------------------

		private ConfigFile(String versionStr)
			throws AppException
		{
			super(Key.CONFIGURATION, null, versionStr);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public String getSourceName()
		{
			return CONFIG_FILE2_STR;
		}

		//--------------------------------------------------------------

		@Override
		protected String getFileKindString()
		{
			return CONFIG_FILE1_STR;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		public void read(File file)
			throws AppException
		{
			// Read file
			read(file, Key.CONFIGURATION);

			// Test version number
			String versionStr = getVersionString();
			if (versionStr == null)
				throw new FileException(ErrorId.NO_VERSION_NUMBER, file);
			try
			{
				int version = Integer.parseInt(versionStr);
				if ((version < MIN_SUPPORTED_VERSION) || (version > MAX_SUPPORTED_VERSION))
					throw new FileException(ErrorId.UNSUPPORTED_CONFIGURATION_FILE, file, versionStr);
			}
			catch (NumberFormatException e)
			{
				throw new FileException(ErrorId.INVALID_VERSION_NUMBER, file);
			}
		}

		//--------------------------------------------------------------

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Member classes : inner classes
////////////////////////////////////////////////////////////////////////


	// PROPERTY CLASS: SHOW UNIX PATHNAMES


	private class CPShowUnixPathnames
		extends Property.BooleanProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPShowUnixPathnames()
		{
			super(concatenateKeys(Key.GENERAL, Key.SHOW_UNIX_PATHNAMES));
			value = false;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public boolean isShowUnixPathnames()
	{
		return cpShowUnixPathnames.getValue();
	}

	//------------------------------------------------------------------

	public void setShowUnixPathnames(boolean value)
	{
		cpShowUnixPathnames.setValue(value);
	}

	//------------------------------------------------------------------

	public void addShowUnixPathnamesObserver(Property.IObserver observer)
	{
		cpShowUnixPathnames.addObserver(observer);
	}

	//------------------------------------------------------------------

	public void removeShowUnixPathnamesObserver(Property.IObserver observer)
	{
		cpShowUnixPathnames.removeObserver(observer);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPShowUnixPathnames	cpShowUnixPathnames	= new CPShowUnixPathnames();

	//==================================================================


	// PROPERTY CLASS: SELECT TEXT ON FOCUS GAINED


	private class CPSelectTextOnFocusGained
		extends Property.BooleanProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPSelectTextOnFocusGained()
		{
			super(concatenateKeys(Key.GENERAL, Key.SELECT_TEXT_ON_FOCUS_GAINED));
			value = true;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public boolean isSelectTextOnFocusGained()
	{
		return cpSelectTextOnFocusGained.getValue();
	}

	//------------------------------------------------------------------

	public void setSelectTextOnFocusGained(boolean value)
	{
		cpSelectTextOnFocusGained.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPSelectTextOnFocusGained	cpSelectTextOnFocusGained	= new CPSelectTextOnFocusGained();

	//==================================================================


	// PROPERTY CLASS: MAIN WINDOW LOCATION


	private class CPMainWindowLocation
		extends Property.SimpleProperty<Point>
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPMainWindowLocation()
		{
			super(concatenateKeys(Key.GENERAL, Key.MAIN_WINDOW_LOCATION));
			value = new Point();
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public void parse(Input input)
			throws AppException
		{
			if (input.getValue().isEmpty())
				value = null;
			else
			{
				int[] outValues = input.parseIntegers(2, null);
				value = new Point(outValues[0], outValues[1]);
			}
		}

		//--------------------------------------------------------------

		@Override
		public String toString()
		{
			return ((value == null) ? "" : value.x + ", " + value.y);
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public boolean isMainWindowLocation()
	{
		return (getMainWindowLocation() != null);
	}

	//------------------------------------------------------------------

	public Point getMainWindowLocation()
	{
		return cpMainWindowLocation.getValue();
	}

	//------------------------------------------------------------------

	public void setMainWindowLocation(Point value)
	{
		cpMainWindowLocation.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPMainWindowLocation	cpMainWindowLocation	= new CPMainWindowLocation();

	//==================================================================


	// PROPERTY CLASS: LOOK-AND-FEEL


	private class CPLookAndFeel
		extends Property.StringProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPLookAndFeel()
		{
			super(concatenateKeys(Key.APPEARANCE, Key.LOOK_AND_FEEL));
			value = "";
			for (UIManager.LookAndFeelInfo lookAndFeelInfo : UIManager.getInstalledLookAndFeels())
			{
				if (lookAndFeelInfo.getClassName().
											equals(UIManager.getCrossPlatformLookAndFeelClassName()))
				{
					value = lookAndFeelInfo.getName();
					break;
				}
			}
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public String getLookAndFeel()
	{
		return cpLookAndFeel.getValue();
	}

	//------------------------------------------------------------------

	public void setLookAndFeel(String value)
	{
		cpLookAndFeel.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPLookAndFeel	cpLookAndFeel	= new CPLookAndFeel();

	//==================================================================


	// PROPERTY CLASS: TEXT ANTIALIASING


	private class CPTextAntialiasing
		extends Property.EnumProperty<TextRendering.Antialiasing>
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPTextAntialiasing()
		{
			super(concatenateKeys(Key.APPEARANCE, Key.TEXT_ANTIALIASING),
				  TextRendering.Antialiasing.class);
			value = TextRendering.Antialiasing.DEFAULT;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public TextRendering.Antialiasing getTextAntialiasing()
	{
		return cpTextAntialiasing.getValue();
	}

	//------------------------------------------------------------------

	public void setTextAntialiasing(TextRendering.Antialiasing value)
	{
		cpTextAntialiasing.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPTextAntialiasing	cpTextAntialiasing	= new CPTextAntialiasing();

	//==================================================================


	// PROPERTY CLASS: SHOW ADJACENT MONTHS


	private class CPShowAdjacentMonths
		extends Property.BooleanProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPShowAdjacentMonths()
		{
			super(concatenateKeys(Key.APPEARANCE, Key.SHOW_ADJACENT_MONTHS));
			value = true;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public boolean isShowAdjacentMonths()
	{
		return cpShowAdjacentMonths.getValue();
	}

	//------------------------------------------------------------------

	public void setShowAdjacentMonths(boolean value)
	{
		cpShowAdjacentMonths.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPShowAdjacentMonths	cpShowAdjacentMonths	= new CPShowAdjacentMonths();

	//==================================================================


	// PROPERTY CLASS: DATE FORMATS


	private class CPDateFormats
		extends Property.PropertyList<DateFormat>
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPDateFormats()
		{
			super(concatenateKeys(Key.DATE, Key.FORMAT), MAX_NUM_DATE_FORMATS);
			fill(null);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		protected void parse(Input input,
							 int   index)
		{
			try
			{
				DateFormat dateFormat = new DateFormat(input.getValue());
				String name = dateFormat.getName();
				boolean duplicate = false;
				for (DateFormat value : values)
				{
					if ((value != null) && value.getName().equals(name))
					{
						duplicate = true;
						break;
					}
				}
				values.set(index, dateFormat);
				if (duplicate)
				{
					AppException e = new AppException(AppConfig.ErrorId.DUPLICATE_DATE_FORMAT_NAME, name);
					showWarningMessage(new InputException(e, input));
				}
			}
			catch (DateFormat.ParseException e)
			{
				showWarningMessage(new InputException(e.getException(), input));
			}
		}

		//--------------------------------------------------------------

		@Override
		protected String toString(int index)
		{
			DateFormat value = values.get(index);
			return ((value == null) ? null : value.toString());
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public List<DateFormat> getDateFormats()
	{
		return cpDateFormats.getNonNullValues();
	}

	//------------------------------------------------------------------

	public void setDateFormats(List<DateFormat> values)
	{
		cpDateFormats.setValues(values);
	}

	//------------------------------------------------------------------

	public String addDateFormatObserver(Property.IObserver observer)
	{
		return cpDateFormats.addObserver(observer);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPDateFormats	cpDateFormats	= new CPDateFormats();

	//==================================================================


	// PROPERTY CLASS: DATE NAMES SOURCE


	private class CPDateNamesSource
		extends Property.EnumProperty<DateNamesSource>
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPDateNamesSource()
		{
			super(concatenateKeys(Key.DATE, Key.NAMES_SOURCE), DateNamesSource.class);
			value = DateNamesSource.LOCALE;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public DateNamesSource getDateNamesSource()
	{
		return cpDateNamesSource.getValue();
	}

	//------------------------------------------------------------------

	public void setDateNamesSource(DateNamesSource value)
	{
		cpDateNamesSource.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPDateNamesSource	cpDateNamesSource	= new CPDateNamesSource();

	//==================================================================


	// PROPERTY CLASS: DATE NAMES LOCALE


	private class CPDateNamesLocale
		extends Property.StringProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPDateNamesLocale()
		{
			super(concatenateKeys(Key.DATE, Key.NAMES_LOCALE));
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	String	substituteValue;

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public String getDateNamesLocale()
	{
		String str = cpDateNamesLocale.substituteValue;
		if (str == null)
			str = cpDateNamesLocale.getValue();
		if (str == null)
			str = "";
		return str;
	}

	//------------------------------------------------------------------

	public void setDateNamesLocale(String value)
	{
		cpDateNamesLocale.substituteValue = null;
		cpDateNamesLocale.setValue(value.isEmpty() ? null : value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPDateNamesLocale	cpDateNamesLocale	= new CPDateNamesLocale();

	//==================================================================


	// PROPERTY CLASS: MONTH NAMES


	private class CPMonthNames
		extends Property.PropertyList<String>
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPMonthNames()
		{
			super(concatenateKeys(Key.DATE, Key.MONTH_NAME), DateUtils.NUM_MONTH_NAMES);
			fill("");
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		protected void parse(Input input,
							 int   index)
		{
			values.set(index, input.getValue());
		}

		//--------------------------------------------------------------

		@Override
		protected String toString(int index)
		{
			String value = values.get(index);
			return (value.isEmpty() ? null : value);
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public List<String> getMonthNames()
	{
		return cpMonthNames.getValues();
	}

	//------------------------------------------------------------------

	public void setMonthNames(List<String> values)
	{
		cpMonthNames.setValues(values);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPMonthNames	cpMonthNames	= new CPMonthNames();

	//==================================================================


	// PROPERTY CLASS: DAY NAMES


	private class CPDayNames
		extends Property.PropertyList<String>
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPDayNames()
		{
			super(concatenateKeys(Key.DATE, Key.DAY_NAME), DateUtils.NUM_DAY_NAMES);
			fill("");
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		protected void parse(Input input,
							 int   index)
		{
			values.set(index, input.getValue());
		}

		//--------------------------------------------------------------

		@Override
		protected String toString(int index)
		{
			String value = values.get(index);
			return (value.isEmpty() ? null : value);
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public List<String> getDayNames()
	{
		return cpDayNames.getValues();
	}

	//------------------------------------------------------------------

	public void setDayNames(List<String> values)
	{
		cpDayNames.setValues(values);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPDayNames	cpDayNames	= new CPDayNames();

	//==================================================================


	// PROPERTY CLASS: FIRST DAY OF WEEK


	private class CPFirstDayOfWeek
		extends Property.IntegerProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPFirstDayOfWeek()
		{
			super(concatenateKeys(Key.DATE, Key.FIRST_DAY_OF_WEEK), 0, Calendar.SATURDAY);
			value = 0;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public int getFirstDayOfWeek()
	{
		return cpFirstDayOfWeek.getValue();
	}

	//------------------------------------------------------------------

	public void setFirstDayOfWeek(int value)
	{
		cpFirstDayOfWeek.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPFirstDayOfWeek	cpFirstDayOfWeek	= new CPFirstDayOfWeek();

	//==================================================================


	// PROPERTY CLASS: FONTS


	private class CPFonts
		extends Property.PropertyMap<AppFont, FontEx>
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPFonts()
		{
			super(Key.FONT, AppFont.class);
			for (AppFont font : AppFont.values())
				values.put(font, new FontEx());
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public void parse(Input   input,
						  AppFont appFont)
		{
			try
			{
				FontEx font = new FontEx(input.getValue());
				appFont.setFontEx(font);
				values.put(appFont, font);
			}
			catch (IllegalArgumentException e)
			{
				showWarningMessage(new IllegalValueException(input));
			}
			catch (uk.blankaspect.common.exception.ValueOutOfBoundsException e)
			{
				showWarningMessage(new ValueOutOfBoundsException(input));
			}
		}

		//--------------------------------------------------------------

		@Override
		public String toString(AppFont appFont)
		{
			return getValue(appFont).toString();
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public FontEx getFont(int index)
	{
		return cpFonts.getValue(AppFont.values()[index]);
	}

	//------------------------------------------------------------------

	public void setFont(int    index,
						FontEx font)
	{
		cpFonts.setValue(AppFont.values()[index], font);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPFonts	cpFonts	= new CPFonts();

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	private AppConfig()
	{
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static void showWarningMessage(AppException exception)
	{
		App.INSTANCE.showWarningMessage(App.SHORT_NAME + " : " + CONFIG_ERROR_STR, exception);
	}

	//------------------------------------------------------------------

	public static void showErrorMessage(AppException exception)
	{
		App.INSTANCE.showErrorMessage(App.SHORT_NAME + " : " + CONFIG_ERROR_STR, exception);
	}

	//------------------------------------------------------------------

	private static File getFile()
		throws AppException
	{
		File file = null;

		// Get directory of JAR file
		File jarDirectory = null;
		try
		{
			jarDirectory = new File(AppConfig.class.getProtectionDomain().getCodeSource().getLocation().
																				toURI()).getParentFile();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		// Get pathname of configuration directory from properties file
		String pathname = null;
		File propertiesFile = new File(jarDirectory, PROPERTIES_FILENAME);
		if (propertiesFile.isFile())
		{
			try
			{
				Properties properties = new Properties();
				properties.loadFromXML(new FileInputStream(propertiesFile));
				pathname = properties.getProperty(CONFIG_DIR_KEY);
			}
			catch (IOException e)
			{
				throw new FileException(ErrorId.ERROR_READING_PROPERTIES_FILE, propertiesFile);
			}
		}

		// Get pathname of configuration directory from system property or set system property to pathname
		try
		{
			if (pathname == null)
				pathname = System.getProperty(CONFIG_DIR_KEY);
			else
				System.setProperty(CONFIG_DIR_KEY, pathname);
		}
		catch (SecurityException e)
		{
			// ignore
		}

		// Look for configuration file in default locations
		if (pathname == null)
		{
			// Look for configuration file in local directory
			file = new File(CONFIG_FILENAME);

			// Look for configuration file in default configuration directory
			if (!file.isFile())
			{
				file = null;
				pathname = Utils.getPropertiesPathname();
				if (pathname != null)
				{
					file = new File(pathname, CONFIG_FILENAME);
					if (!file.isFile())
						file = null;
				}
			}
		}

		// Set configuration file from pathname of configuration directory
		else if (!pathname.isEmpty())
		{
			file = new File(PathnameUtils.parsePathname(pathname), CONFIG_FILENAME);
			if (!file.isFile())
				throw new FileException(ErrorId.NO_CONFIGURATION_FILE, file);
		}

		return file;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public File chooseFile(Component parent)
	{
		if (fileChooser == null)
		{
			fileChooser = new JFileChooser();
			fileChooser.setDialogTitle(SAVE_CONFIGURATION_FILE_STR);
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fileChooser.setFileFilter(new FilenameSuffixFilter(AppConstants.XML_FILES_STR,
															   AppConstants.XML_FILE_SUFFIX));
			selectedFile = file;
		}

		fileChooser.setSelectedFile((selectedFile == null) ? new File(CONFIG_FILENAME).getAbsoluteFile()
														   : selectedFile.getAbsoluteFile());
		fileChooser.rescanCurrentDirectory();
		if (fileChooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION)
		{
			selectedFile = Utils.appendSuffix(fileChooser.getSelectedFile(),
											  AppConstants.XML_FILE_SUFFIX);
			return selectedFile;
		}
		return null;
	}

	//------------------------------------------------------------------

	public void read()
	{
		// Read configuration file
		fileRead = false;
		ConfigFile configFile = null;
		try
		{
			file = getFile();
			if (file != null)
			{
				configFile = new ConfigFile();
				configFile.read(file);
				fileRead = true;
			}
		}
		catch (AppException e)
		{
			showErrorMessage(e);
		}

		// Get properties
		if (fileRead)
			getProperties(configFile, Property.getSystemSource());
		else
			getProperties(Property.getSystemSource());

		// Reset changed status of properties
		resetChanged();
	}

	//------------------------------------------------------------------

	public void write()
	{
		if (isChanged())
		{
			try
			{
				if (file == null)
				{
					if (System.getProperty(CONFIG_DIR_KEY) == null)
					{
						String pathname = Utils.getPropertiesPathname();
						if (pathname != null)
						{
							File directory = new File(pathname);
							if (!directory.exists() && !directory.mkdirs())
								throw new FileException(ErrorId.FAILED_TO_CREATE_DIRECTORY, directory);
							file = new File(directory, CONFIG_FILENAME);
						}
					}
				}
				else
				{
					if (!fileRead)
						file.renameTo(new File(file.getParentFile(), CONFIG_OLD_FILENAME));
				}
				if (file != null)
				{
					write(file);
					resetChanged();
				}
			}
			catch (AppException e)
			{
				showErrorMessage(e);
			}
		}
	}

	//------------------------------------------------------------------

	public void write(File file)
		throws AppException
	{
		// Initialise progress view
		IProgressView progressView = Task.getProgressView();
		if (progressView != null)
		{
			progressView.setInfo(WRITING_STR, file);
			progressView.setProgress(0, -1.0);
		}

		// Create new DOM document
		ConfigFile configFile = new ConfigFile(Integer.toString(VERSION));

		// Set configuration properties in document
		putProperties(configFile);

		// Write file
		configFile.write(file);
	}

	//------------------------------------------------------------------

	public void updateDateNames()
	{
		switch (getDateNamesSource())
		{
			case LOCALE:
			{
				Locale locale = null;
				String key = getDateNamesLocale();
				LocaleEx localeEx = LocaleEx.findLocale(key);
				if (localeEx == null)
				{
					String langKey = LocaleEx.getLanguageKey(key);
					if (!langKey.equals(key))
						localeEx = LocaleEx.findLocale(langKey);
					locale = (localeEx == null) ? Locale.getDefault() : localeEx.getLocale();
					String substituteKey = new LocaleEx(locale).getKey();
					cpDateNamesLocale.substituteValue = substituteKey;
					showWarningMessage(new AppException(ErrorId.UNSUPPORTED_LOCALE, key,
														substituteKey));
				}
				else
					locale = localeEx.getLocale();
				DateUtils.setMonthNames(DateUtils.getMonthNames(locale));
				DateUtils.setDayNames(DateUtils.getDayNames(locale));
				break;
			}

			case USER_DEFINED:
				DateUtils.setMonthNames(getMonthNames());
				DateUtils.setDayNames(getDayNames());
				break;
		}
	}

	//------------------------------------------------------------------

	private void getProperties(Property.ISource... propertySources)
	{
		for (Property property : getProperties())
		{
			try
			{
				property.get(propertySources);
			}
			catch (AppException e)
			{
				showWarningMessage(e);
			}
		}
	}

	//------------------------------------------------------------------

	private void putProperties(Property.ITarget propertyTarget)
	{
		for (Property property : getProperties())
			property.put(propertyTarget);
	}

	//------------------------------------------------------------------

	private boolean isChanged()
	{
		for (Property property : getProperties())
		{
			if (property.isChanged())
				return true;
		}
		return false;
	}

	//------------------------------------------------------------------

	private void resetChanged()
	{
		for (Property property : getProperties())
			property.setChanged(false);
	}

	//------------------------------------------------------------------

	private List<Property> getProperties()
	{
		if (properties == null)
		{
			properties = new ArrayList<>();
			for (Field field : getClass().getDeclaredFields())
			{
				try
				{
					if (field.getName().startsWith(Property.FIELD_PREFIX))
						properties.add((Property)field.get(this));
				}
				catch (IllegalAccessException e)
				{
					e.printStackTrace();
				}
			}
		}
		return properties;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Static initialiser
////////////////////////////////////////////////////////////////////////

	static
	{
		INSTANCE = new AppConfig();
	}

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	File			file;
	private	boolean			fileRead;
	private	File			selectedFile;
	private	JFileChooser	fileChooser;
	private	List<Property>	properties;

}

//----------------------------------------------------------------------
