/*====================================================================*\

Utils.java

Utility methods class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.datecalculator;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.Toolkit;

import java.awt.datatransfer.StringSelection;

import java.io.File;

import uk.blankaspect.common.exception.AppException;

import uk.blankaspect.common.misc.PropertiesPathname;
import uk.blankaspect.common.misc.SystemUtils;

//----------------------------------------------------------------------


// UTILITY METHODS CLASS


class Utils
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	String	USER_HOME_PREFIX			= "~";
	private static final	String	FAILED_TO_GET_PATHNAME_STR	= "Failed to get the canonical pathname " +
																	"for the file or directory.";

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

		CLIPBOARD_IS_UNAVAILABLE
		("The clipboard is currently unavailable.");

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
	//  Instance fields
	////////////////////////////////////////////////////////////////////

		private	String	message;

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	private Utils()
	{
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static int indexOf(Object   target,
							  Object[] values)
	{
		for (int i = 0; i < values.length; i++)
		{
			if (values[i].equals(target))
				return i;
		}
		return -1;
	}

	//------------------------------------------------------------------

	public static char getFileSeparatorChar()
	{
		return (AppConfig.INSTANCE.isShowUnixPathnames() ? '/' : File.separatorChar);
	}

	//------------------------------------------------------------------

	public static String getPathname(File file)
	{
		return getPathname(file, AppConfig.INSTANCE.isShowUnixPathnames());
	}

	//------------------------------------------------------------------

	public static String getPathname(File    file,
									 boolean unixStyle)
	{
		String pathname = null;
		if (file != null)
		{
			try
			{
				try
				{
					pathname = file.getCanonicalPath();
				}
				catch (Exception e)
				{
					System.err.println(file.getPath());
					System.err.println(FAILED_TO_GET_PATHNAME_STR);
					System.err.println("(" + e + ")");
					pathname = file.getAbsolutePath();
				}
			}
			catch (SecurityException e)
			{
				System.err.println(e);
				pathname = file.getPath();
			}

			if (unixStyle)
			{
				try
				{
					String userHome = SystemUtils.getUserHomePathname();
					if ((userHome != null) && pathname.startsWith(userHome))
						pathname = USER_HOME_PREFIX + pathname.substring(userHome.length());
				}
				catch (SecurityException e)
				{
					// ignore
				}
				pathname = pathname.replace(File.separatorChar, '/');
			}
		}
		return pathname;
	}

	//------------------------------------------------------------------

	public static String getPropertiesPathname()
	{
		String pathname = PropertiesPathname.getPathname();
		if (pathname != null)
			pathname += App.NAME_KEY;
		return pathname;
	}

	//------------------------------------------------------------------

	public static File appendSuffix(File   file,
									String suffix)
	{
		String filename = file.getName();
		if (!filename.isEmpty() && (filename.indexOf('.') < 0))
			file = new File(file.getParentFile(), filename + suffix);
		return file;
	}

	//------------------------------------------------------------------

	public static String[] getOptionStrings(String... optionStrs)
	{
		String[] strs = new String[optionStrs.length + 1];
		System.arraycopy(optionStrs, 0, strs, 0, optionStrs.length);
		strs[optionStrs.length] = AppConstants.CANCEL_STR;
		return strs;
	}

	//------------------------------------------------------------------

	public static void putClipboardText(String text)
		throws AppException
	{
		try
		{
			StringSelection selection = new StringSelection(text);
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
		}
		catch (IllegalStateException e)
		{
			throw new AppException(ErrorId.CLIPBOARD_IS_UNAVAILABLE, e);
		}
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
