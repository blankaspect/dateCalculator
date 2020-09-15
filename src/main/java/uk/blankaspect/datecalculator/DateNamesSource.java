/*====================================================================*\

DateNamesSource.java

Date-names source enumeration.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.datecalculator;

//----------------------------------------------------------------------


// IMPORTS


import uk.blankaspect.common.misc.IStringKeyed;

//----------------------------------------------------------------------


// DATE-NAMES SOURCE ENUMERATION


enum DateNamesSource
	implements IStringKeyed
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	LOCALE
	(
		"locale",
		"Locale"
	),

	USER_DEFINED
	(
		"userDefined",
		"User-defined"
	);

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	private DateNamesSource(String key,
							String text)
	{
		this.key = key;
		this.text = text;
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
	public String toString()
	{
		return text;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	String	key;
	private	String	text;

}

//----------------------------------------------------------------------
