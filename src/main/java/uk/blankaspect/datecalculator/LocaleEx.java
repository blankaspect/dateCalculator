/*====================================================================*\

LocaleEx.java

Extended locale class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.datecalculator;

//----------------------------------------------------------------------


// IMPORTS


import java.util.Collections;
import java.util.List;
import java.util.Locale;

import uk.blankaspect.common.gui.ComboBoxRenderer;

import uk.blankaspect.common.misc.ArraySet;
import uk.blankaspect.common.misc.IStringKeyed;
import uk.blankaspect.common.misc.StringUtils;

//----------------------------------------------------------------------


// EXTENDED LOCALE CLASS


class LocaleEx
	implements ComboBoxRenderer.ITooltipSource, Comparable<LocaleEx>, IStringKeyed
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	public static final		List<LocaleEx>	LOCALES;

	private static final	char	KEY_SEPARATOR_CHAR	= '-';

	private static final	String	DEFAULT_LOCALE_STR	= "<default locale>";

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public LocaleEx()
	{
		key = "";
	}

	//------------------------------------------------------------------

	public LocaleEx(String key)
	{
		this.key = key;
	}

	//------------------------------------------------------------------

	public LocaleEx(Locale locale)
	{
		this.locale = locale;

		StringBuilder buffer = new StringBuilder(64);
		String[] strs = { locale.getLanguage(), locale.getCountry(), locale.getVariant() };
		for (String str : strs)
		{
			if (!str.isEmpty())
			{
				if (buffer.length() > 0)
					buffer.append(KEY_SEPARATOR_CHAR);
				buffer.append(str);
			}
		}
		key = buffer.toString();

		text = locale.getDisplayName();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static LocaleEx findLocale(String key)
	{
		int index = LOCALES.indexOf(new LocaleEx(key));
		return ((index < 0) ? null : LOCALES.get(index));
	}

	//------------------------------------------------------------------

	public static String getLanguageKey(String key)
	{
		return StringUtils.removeFromFirst(key, KEY_SEPARATOR_CHAR);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : ComboBoxRenderer.ITooltipSource interface
////////////////////////////////////////////////////////////////////////

	@Override
	public String getTooltip()
	{
		return text;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : Comparable interface
////////////////////////////////////////////////////////////////////////

	@Override
	public int compareTo(LocaleEx other)
	{
		return key.compareTo(other.key);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : IStringKeyed interface
////////////////////////////////////////////////////////////////////////

	public String getKey()
	{
		return key;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	@Override
	public boolean equals(Object obj)
	{
		return ((obj instanceof LocaleEx) && key.equals(((LocaleEx)obj).key));
	}

	//------------------------------------------------------------------

	@Override
	public int hashCode()
	{
		return key.hashCode();
	}

	//------------------------------------------------------------------

	@Override
	public String toString()
	{
		return ((locale == null) ? DEFAULT_LOCALE_STR : key);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public Locale getLocale()
	{
		return ((locale == null) ? Locale.getDefault() : locale);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Static initialiser
////////////////////////////////////////////////////////////////////////

	static
	{
		List<LocaleEx> locales = new ArraySet<>();
		locales.add(new LocaleEx());
		for (Locale locale : Locale.getAvailableLocales())
		{
			if (!locale.getLanguage().isEmpty())
				locales.add(new LocaleEx(locale));
		}
		Collections.sort(locales);
		LOCALES = Collections.unmodifiableList(locales);
	}

////////////////////////////////////////////////////////////////////////
//  Instance fields
////////////////////////////////////////////////////////////////////////

	private	Locale	locale;
	private	String	key;
	private	String	text;

}

//----------------------------------------------------------------------
