/*====================================================================*\

AppConstants.java

Application constants interface.

\*====================================================================*/


// IMPORTS


import java.awt.Insets;

//----------------------------------------------------------------------


// APPLICATION CONSTANTS INTERFACE


interface AppConstants
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

    // Component constants
    Insets  COMPONENT_INSETS    = new Insets( 2, 3, 2, 3 );

    // Strings
    String  ELLIPSIS_STR        = "...";
    String  OK_STR              = "OK";
    String  CANCEL_STR          = "Cancel";
    String  CLOSE_STR           = "Close";
    String  REPLACE_STR         = "Replace";
    String  CLEAR_STR           = "Clear";
    String  ALREADY_EXISTS_STR  = "\nThe file already exists.\nDo you want to replace it?";

    // Filename suffixes
    String  XML_FILE_SUFFIX = ".xml";

    // File-filter descriptions
    String  XML_FILES_STR   = "XML files";

}

//----------------------------------------------------------------------
