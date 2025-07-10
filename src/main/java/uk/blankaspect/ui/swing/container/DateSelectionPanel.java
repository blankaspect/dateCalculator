/*====================================================================*\

DateSelectionPanel.java

Class: date-selection panel.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.ui.swing.container;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;

import java.util.Calendar;
import java.util.List;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import uk.blankaspect.common.date.Date;
import uk.blankaspect.common.date.DateUtils;

import uk.blankaspect.common.misc.ModernCalendar;

import uk.blankaspect.ui.swing.action.KeyAction;

import uk.blankaspect.ui.swing.button.FButton;

import uk.blankaspect.ui.swing.combobox.FComboBox;

import uk.blankaspect.ui.swing.font.FontKey;
import uk.blankaspect.ui.swing.font.FontUtils;

import uk.blankaspect.ui.swing.label.FLabel;

import uk.blankaspect.ui.swing.misc.GuiConstants;
import uk.blankaspect.ui.swing.misc.GuiUtils;

import uk.blankaspect.ui.swing.spinner.FIntegerSpinner;

import uk.blankaspect.ui.swing.text.TextRendering;

//----------------------------------------------------------------------


// CLASS: DATE-SELECTION PANEL


public class DateSelectionPanel
	extends JPanel
	implements ActionListener, MouseListener
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	public static final		int		MIN_YEAR	= 1600;
	public static final		int		MAX_YEAR	= 3999;

	private static final	int		MIN_MONTH	= 0;
	private static final	int		MAX_MONTH	= 11;

	private static final	int		MIN_DAY	= 0;

	private static final	int		NUM_DAYS_IN_WEEK	= 7;

	private static final	int		MIN_MONTH_NAME_LENGTH	= 3;
	private static final	int		MIN_DAY_NAME_LENGTH		= 2;

	private static final	int		MONTH_LABEL_HORIZONTAL_MARGIN	= 4;
	private static final	int		MONTH_LABEL_VERTICAL_MARGIN		= 1;

	private static final	Color	MONTH_LABEL_BACKGROUND_COLOUR	= new Color(248, 244, 224);

	private static final	Color	NAVIGATION_PANEL_BORDER_COLOUR	= new Color(200, 200, 200);

	private static final	Color	FOCUSED_BORDER_COLOUR1	= Color.WHITE;
	private static final	Color	FOCUSED_BORDER_COLOUR2	= Color.BLACK;

	private static final	String	PREVIOUS_MONTH_STR	= "Previous month (PageUp)";
	private static final	String	NEXT_MONTH_STR		= "Next month (PageDown)";
	private static final	String	PREVIOUS_YEAR_STR	= "Previous year (Ctrl+PageUp)";
	private static final	String	NEXT_YEAR_STR		= "Next year (Ctrl+PageDown)";

	private static final	String	PROTOTYPE_YEAR_STR	= "0000";

	// Icons
	private static final	ImageIcon	ANGLE_SINGLE_LEFT_ICON	= new ImageIcon(ImgData.ANGLE_SINGLE_LEFT);
	private static final	ImageIcon	ANGLE_SINGLE_RIGHT_ICON	= new ImageIcon(ImgData.ANGLE_SINGLE_RIGHT);
	private static final	ImageIcon	ANGLE_DOUBLE_LEFT_ICON	= new ImageIcon(ImgData.ANGLE_DOUBLE_LEFT);
	private static final	ImageIcon	ANGLE_DOUBLE_RIGHT_ICON	= new ImageIcon(ImgData.ANGLE_DOUBLE_RIGHT);

	// Commands
	private interface Command
	{
		String	PREVIOUS_MONTH		= "previousMonth";
		String	NEXT_MONTH			= "nextMonth";
		String	PREVIOUS_YEAR		= "previousYear";
		String	NEXT_YEAR			= "nextYear";
		String	EDIT_MONTH_YEAR		= "editMonthYear";

		String	SELECT_UP_UNIT		= "selectUpUnit";
		String	SELECT_DOWN_UNIT	= "selectDownUnit";
		String	SELECT_UP_MAX		= "selectUpMax";
		String	SELECT_DOWN_MAX		= "selectDownMax";
		String	SELECT_LEFT_UNIT	= "selectLeftUnit";
		String	SELECT_RIGHT_UNIT	= "selectRightUnit";
		String	SELECT_LEFT_MAX		= "selectLeftMax";
		String	SELECT_RIGHT_MAX	= "selectRightMax";
	}

	private static final	KeyAction.KeyCommandPair[]	KEY_COMMANDS	=
	{
		new KeyAction.KeyCommandPair
		(
			KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0),
			Command.PREVIOUS_MONTH
		),
		new KeyAction.KeyCommandPair
		(
			KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0),
			Command.NEXT_MONTH
		),
		new KeyAction.KeyCommandPair
		(
			KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, KeyEvent.CTRL_DOWN_MASK),
			Command.PREVIOUS_YEAR
		),
		new KeyAction.KeyCommandPair
		(
			KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, KeyEvent.CTRL_DOWN_MASK),
			Command.NEXT_YEAR
		),
		new KeyAction.KeyCommandPair
		(
			KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, KeyEvent.CTRL_DOWN_MASK),
			Command.EDIT_MONTH_YEAR
		)
	};

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


	// CLASS: NAVIGATION BUTTON


	private static class NavigationButton
		extends JButton
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	int	HORIZONTAL_MARGIN	= 4;
		private static final	int	VERTICAL_MARGIN		= 4;

		private static final	Color	BACKGROUND_COLOUR		= new Color(216, 232, 216);
		private static final	Color	ARMED_BACKGROUND_COLOUR	= new Color(248, 224, 104);
		private static final	Color	BORDER_COLOUR			= new Color(184, 200, 184);

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private NavigationButton(ImageIcon icon)
		{
			super(icon);
			setBorder(null);
			setBackground(null);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public Dimension getPreferredSize()
		{
			return new Dimension(2 * HORIZONTAL_MARGIN + getIcon().getIconWidth(),
								 2 * VERTICAL_MARGIN + getIcon().getIconHeight());
		}

		//--------------------------------------------------------------

		@Override
		protected void paintComponent(Graphics gr)
		{
			// Cast copy of graphics context
			Graphics2D gr2d = GuiUtils.copyGraphicsContext(gr);

			// Get dimensions
			int width = getWidth();
			int height = getHeight();

			// Fill interior
			gr2d.setColor(isEnabled()
								? (isSelected() != getModel().isArmed())
										? ARMED_BACKGROUND_COLOUR
										: BACKGROUND_COLOUR
								: getBackground());
			gr2d.fillRect(0, 0, width, height);

			// Set alpha composite
			if (!isEnabled())
				gr2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));

			// Draw icon
			getIcon().paintIcon(this, gr2d, HORIZONTAL_MARGIN, VERTICAL_MARGIN);

			// Draw border
			gr2d.setColor(BORDER_COLOUR);
			gr2d.drawRect(0, 0, width - 1, height - 1);
			if (isFocusOwner())
			{
				gr2d.setColor(FOCUSED_BORDER_COLOUR1);
				gr2d.drawRect(1, 1, width - 3, height - 3);

				gr2d.setStroke(GuiConstants.BASIC_DASH);
				gr2d.setColor(FOCUSED_BORDER_COLOUR2);
				gr2d.drawRect(1, 1, width - 3, height - 3);
			}
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// CLASS: DAY-SELECTION PANEL


	private static class DaySelectionPanel
		extends JComponent
		implements ActionListener, FocusListener, MouseListener, MouseMotionListener
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	int		MIN_NUM_DAYS	= 28;
		private static final	int		MAX_NUM_DAYS	= 31;

		private static final	int		NUM_COLUMNS	= NUM_DAYS_IN_WEEK;

		private static final	int		HORIZONTAL_MARGIN	= 3;
		private static final	int		VERTICAL_MARGIN		= 2;

		private static final	Color	HEADER_BACKGROUND_COLOUR			= new Color(236, 236, 236);
		private static final	Color	HEADER_BORDER_COLOUR				= new Color(212, 212, 212);
		private static final	Color	BACKGROUND1_COLOUR					= Color.WHITE;
		private static final	Color	BACKGROUND2_COLOUR					= new Color(240, 246, 240);
		private static final	Color	SELECTED_BACKGROUND_COLOUR			= new Color(224, 224, 224);
		private static final	Color	FOCUSED_SELECTED_BACKGROUND_COLOUR	= new Color(252, 232, 160);
		private static final	Color	SELECTED_BORDER_COLOUR				= new Color(160, 160, 160);
		private static final	Color	FOCUSED_SELECTED_BORDER_COLOUR		= new Color(224, 128, 72);
		private static final	Color	TEXT_COLOUR							= Color.BLACK;
		private static final	Color	DISABLED_TEXT_COLOUR				= new Color(192, 192, 192);

		private static final	String	PROTOTYPE_DAY_STR	= "00";

		private static final	KeyAction.KeyCommandPair[]	KEY_COMMANDS	=
		{
			new KeyAction.KeyCommandPair
			(
				KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0),
				Command.SELECT_UP_UNIT
			),
			new KeyAction.KeyCommandPair
			(
				KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0),
				Command.SELECT_DOWN_UNIT
			),
			new KeyAction.KeyCommandPair
			(
				KeyStroke.getKeyStroke(KeyEvent.VK_HOME, KeyEvent.CTRL_DOWN_MASK),
				Command.SELECT_UP_MAX
			),
			new KeyAction.KeyCommandPair
			(
				KeyStroke.getKeyStroke(KeyEvent.VK_END, KeyEvent.CTRL_DOWN_MASK),
				Command.SELECT_DOWN_MAX
			),
			new KeyAction.KeyCommandPair
			(
				KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0),
				Command.SELECT_LEFT_UNIT
			),
			new KeyAction.KeyCommandPair
			(
				KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0),
				Command.SELECT_RIGHT_UNIT
			),
			new KeyAction.KeyCommandPair
			(
				KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0),
				Command.SELECT_LEFT_MAX
			),
			new KeyAction.KeyCommandPair
			(
				KeyStroke.getKeyStroke(KeyEvent.VK_END, 0),
				Command.SELECT_RIGHT_MAX
			)
		};

	////////////////////////////////////////////////////////////////////
	//  Member classes : non-inner classes
	////////////////////////////////////////////////////////////////////


		// CLASS: POP-UP CONTENT


		private static class PopUpContent
			extends JComponent
		{

		////////////////////////////////////////////////////////////////
		//  Constants
		////////////////////////////////////////////////////////////////

			private static final	Color	TEXT_COLOUR			= Color.BLACK;
			private static final	Color	BACKGROUND_COLOUR	= new Color(255, 248, 192);
			private static final	Color	BORDER_COLOUR		= new Color(224, 176, 128);

		////////////////////////////////////////////////////////////////
		//  Instance variables
		////////////////////////////////////////////////////////////////

			private	String	text;
			private	int		width;
			private	int		height;

		////////////////////////////////////////////////////////////////
		//  Constructors
		////////////////////////////////////////////////////////////////

			private PopUpContent(String text,
								 int    height)
			{
				this.text = text;
				this.height = height;
				FontUtils.setAppFont(FontKey.MAIN, this);
				width = 2 * HORIZONTAL_MARGIN + getFontMetrics(getFont()).stringWidth(text);
				setOpaque(true);
				setFocusable(false);
			}

			//----------------------------------------------------------

		////////////////////////////////////////////////////////////////
		//  Instance methods : overriding methods
		////////////////////////////////////////////////////////////////

			@Override
			public Dimension getPreferredSize()
			{
				return new Dimension(width, height);
			}

			//----------------------------------------------------------

			@Override
			protected void paintComponent(Graphics gr)
			{
				// Create copy of graphics context
				Graphics2D gr2d = GuiUtils.copyGraphicsContext(gr);

				// Fill interior
				gr2d.setColor(BACKGROUND_COLOUR);
				gr2d.fillRect(0, 0, width, height);

				// Set rendering hints for text antialiasing and fractional metrics
				TextRendering.setHints(gr2d);

				// Draw text
				gr2d.setColor(TEXT_COLOUR);
				gr2d.drawString(text, HORIZONTAL_MARGIN, FontUtils.getBaselineOffset(height, gr2d.getFontMetrics()));

				// Draw border
				gr2d.setColor(BORDER_COLOUR);
				gr2d.drawRect(0, 0, width - 1, height - 1);
			}

			//----------------------------------------------------------

		}

		//==============================================================

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	int				dayOffset;
		private	int				numDays;
		private	int				selectedDay;
		private	int				firstDayOfWeek;
		private	Action			acceptAction;
		private	boolean			fullHeight;
		private	int				prevMonthNumDays;
		private	int				numRows;
		private	int				columnWidth;
		private	int				rowHeight;
		private	int				titleHeight;
		private	int				dayMousePressed;
		private	int				dayMouseReleased;
		private	List<String>	dayNames;
		private	int				dayNameLength;
		private	Popup			popUp;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		/**
		 * @throws IllegalArgumentException
		 */

		private DaySelectionPanel(int     dayOffset,
								  int     numDays,
								  int     selectedDay,
								  int     prevMonthNumDays,
								  int     firstDayOfWeek,
								  boolean fullHeight)
		{
			// Initialise instance variables
			init(dayOffset, numDays, selectedDay, prevMonthNumDays);
			this.firstDayOfWeek = firstDayOfWeek;
			this.fullHeight = fullHeight;

			// Create list of names of days of week
			dayNames = DateUtils.getDayNames(getDefaultLocale());
			int numDayNames = dayNames.size();

			// Get length of names of days of week
			dayNameLength = MIN_DAY_NAME_LENGTH - 1;
			boolean done = false;
			while (!done)
			{
				++dayNameLength;
				for (int i = 0; i < numDayNames - 1; i++)
				{
					String str = getDayString(i, dayNameLength);
					int j = i + 1;
					while (j < numDayNames)
					{
						if (str.equals(getDayString(j, dayNameLength)))
							break;
						++j;
					}
					if (j == numDayNames)
					{
						done = true;
						break;
					}
				}
			}

			// Get maximum width of day names and day numbers
			FontUtils.setAppFont(FontKey.MAIN, this);
			FontMetrics fontMetrics = getFontMetrics(getFont());
			int maxStrWidth = fontMetrics.stringWidth(PROTOTYPE_DAY_STR);
			for (int i = 0; i < numDayNames; i++)
			{
				int strWidth = fontMetrics.stringWidth(getDayString(i, dayNameLength));
				if (maxStrWidth < strWidth)
					maxStrWidth = strWidth;
			}

			// Initialise remaining instance variables
			columnWidth = 2 * HORIZONTAL_MARGIN + maxStrWidth;
			rowHeight = 2 * VERTICAL_MARGIN + fontMetrics.getAscent() + fontMetrics.getDescent();
			titleHeight = rowHeight;
			dayMousePressed = -1;
			dayMouseReleased = -1;

			// Set properties
			setOpaque(true);
			setFocusable(true);

			// Add commands to action map
			KeyAction.create(this, JComponent.WHEN_FOCUSED, this, KEY_COMMANDS);

			// Add listeners
			addFocusListener(this);
			addMouseListener(this);
			addMouseMotionListener(this);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : ActionListener interface
	////////////////////////////////////////////////////////////////////

		@Override
		public void actionPerformed(ActionEvent event)
		{
			String command = event.getActionCommand();

			if (command.equals(Command.SELECT_UP_UNIT))
				onSelectUpUnit();

			else if (command.equals(Command.SELECT_DOWN_UNIT))
				onSelectDownUnit();

			else if (command.equals(Command.SELECT_UP_MAX))
				onSelectUpMax();

			else if (command.equals(Command.SELECT_DOWN_MAX))
				onSelectDownMax();

			else if (command.equals(Command.SELECT_LEFT_UNIT))
				onSelectLeftUnit();

			else if (command.equals(Command.SELECT_RIGHT_UNIT))
				onSelectRightUnit();

			else if (command.equals(Command.SELECT_LEFT_MAX))
				onSelectLeftMax();

			else if (command.equals(Command.SELECT_RIGHT_MAX))
				onSelectRightMax();
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : FocusListener interface
	////////////////////////////////////////////////////////////////////

		@Override
		public void focusGained(FocusEvent event)
		{
			repaint();
		}

		//--------------------------------------------------------------

		@Override
		public void focusLost(FocusEvent event)
		{
			repaint();
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : MouseListener interface
	////////////////////////////////////////////////////////////////////

		@Override
		public void mouseClicked(MouseEvent event)
		{
			if (SwingUtilities.isLeftMouseButton(event) && (event.getClickCount() > 1)
					&& (dayMousePressed >= 0) && (dayMousePressed == dayMouseReleased) && (acceptAction != null))
				acceptAction.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED,
															 (String)acceptAction.getValue(Action.ACTION_COMMAND_KEY)));
		}

		//--------------------------------------------------------------

		@Override
		public void mouseEntered(MouseEvent event)
		{
			// do nothing
		}

		//--------------------------------------------------------------

		@Override
		public void mouseExited(MouseEvent event)
		{
			// do nothing
		}

		//--------------------------------------------------------------

		@Override
		public void mousePressed(MouseEvent event)
		{
			requestFocusInWindow();

			if (SwingUtilities.isLeftMouseButton(event))
			{
				showPopUp(event);

				dayMousePressed = getDay(event);
				setSelection(dayMousePressed);
			}
		}

		//--------------------------------------------------------------

		@Override
		public void mouseReleased(MouseEvent event)
		{
			if (SwingUtilities.isLeftMouseButton(event))
			{
				hidePopUp();

				dayMouseReleased = getDay(event);
				setSelection(dayMouseReleased);
			}
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : MouseMotionListener interface
	////////////////////////////////////////////////////////////////////

		@Override
		public void mouseDragged(MouseEvent event)
		{
			if (SwingUtilities.isLeftMouseButton(event))
				setSelection(getDay(event));
		}

		//--------------------------------------------------------------

		@Override
		public void mouseMoved(MouseEvent event)
		{
			// do nothing
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public Dimension getPreferredSize()
		{
			return new Dimension(NUM_COLUMNS * columnWidth, titleHeight + getNumPanelRows() * rowHeight);
		}

		//--------------------------------------------------------------

		@Override
		public void doLayout()
		{
			// Call superclass method
			super.doLayout();

			// Widen columns if preferred width of panel is less than its actual width
			int width = getWidth();
			while (getPreferredSize().width < width)
				++columnWidth;

			// Lay out ancestor window again if width of this panel has changed
			int preferredWidth = getPreferredSize().width;
			if (width != preferredWidth)
			{
				setSize(preferredWidth, getHeight());
				SwingUtilities.getWindowAncestor(this).pack();
			}
		}

		//--------------------------------------------------------------

		@Override
		protected void paintComponent(Graphics gr)
		{
			// Create copy of graphics context
			Graphics2D gr2d = GuiUtils.copyGraphicsContext(gr);

			// Get dimensions
			int width = getWidth();
			int height = getHeight();

			// Fill header
			int x = 0;
			int y = 0;
			gr2d.setColor(HEADER_BACKGROUND_COLOUR);
			gr2d.fillRect(x, y, width, titleHeight);

			// Draw header vertical borders
			x += columnWidth - 1;
			gr2d.setColor(HEADER_BORDER_COLOUR);
			for (int i = 0; i < NUM_COLUMNS; i++)
			{
				gr2d.drawLine(x, y, x, y + titleHeight - 1);
				x += columnWidth;
			}

			// Draw header bottom border
			x = 0;
			y += titleHeight - 1;
			gr2d.drawLine(x, y, x + width - 1, y);

			// Fill column backgrounds
			++y;
			for (int i = 0; i < NUM_COLUMNS; i++)
			{
				gr2d.setColor(((i % 2) == 0) ? BACKGROUND1_COLOUR : BACKGROUND2_COLOUR);
				gr2d.fillRect(x, y, columnWidth, height - y);
				x += columnWidth;
			}

			// Draw background and border of selected cell
			if (selectedDay >= 0)
			{
				x = getCellX(selectedDay);
				y = getCellY(selectedDay);
				gr2d.setColor(isFocusOwner() ? FOCUSED_SELECTED_BACKGROUND_COLOUR : SELECTED_BACKGROUND_COLOUR);
				gr2d.fillRect(x, y, columnWidth, rowHeight);
				gr2d.setColor(isFocusOwner() ? FOCUSED_SELECTED_BORDER_COLOUR : SELECTED_BORDER_COLOUR);
				gr2d.drawRect(x, y, columnWidth - 1, rowHeight - 1);
			}

			// Set rendering hints for text antialiasing and fractional metrics
			TextRendering.setHints(gr2d);

			// Draw header text
			FontMetrics fontMetrics = gr2d.getFontMetrics();
			int ascent = fontMetrics.getAscent();
			x = 0;
			y = VERTICAL_MARGIN + ascent;
			gr2d.setColor(TEXT_COLOUR);
			for (int i = 0; i < NUM_DAYS_IN_WEEK; i++)
			{
				String str = getDayString(i, dayNameLength);
				gr2d.drawString(str, x + (columnWidth - fontMetrics.stringWidth(str)) / 2, y);
				x += columnWidth;
			}

			// Draw text of all day numbers
			int strWidth = fontMetrics.stringWidth(PROTOTYPE_DAY_STR);
			int xOffset = (columnWidth - strWidth) / 2 + strWidth;
			int startDay = (prevMonthNumDays < 0) ? 0 : -dayOffset;
			int endDay = (prevMonthNumDays < 0) ? numDays : startDay + getNumPanelRows() * NUM_COLUMNS;
			for (int day = startDay; day < endDay; day++)
			{
				gr2d.setColor(((day >= 0) && (day < numDays)) ? TEXT_COLOUR : DISABLED_TEXT_COLOUR);
				int dayIndex = (day < 0)
									? day + prevMonthNumDays
									: (day < numDays)
											? day
											: day - numDays;
				String str = Integer.toString(dayIndex + 1);
				x = xOffset + getColumnForDay(day) * columnWidth - fontMetrics.stringWidth(str);
				y = titleHeight + getRowForDay(day) * rowHeight + VERTICAL_MARGIN + ascent;
				gr2d.drawString(str, x, y);
			}
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		public int getSelectedDay()
		{
			return selectedDay;
		}

		//--------------------------------------------------------------

		/**
		 * @throws IllegalArgumentException
		 */

		public void setMonth(int dayOffset,
							 int numDays,
							 int selectedDay,
							 int prevMonthNumDays)
		{
			// Set instance variables
			init(dayOffset, numDays, selectedDay, prevMonthNumDays);

			// Redraw panel
			revalidate();
			repaint();
		}

		//--------------------------------------------------------------

		/**
		 * @throws IllegalArgumentException
		 */

		private void init(int dayOffset,
						  int numDays,
						  int selectedDay,
						  int prevMonthNumDays)
		{
			// Validate arguments
			if ((dayOffset < 0) || (dayOffset >= NUM_COLUMNS))
				throw new IllegalArgumentException("Day offset out of bounds: " + dayOffset);
			if ((numDays < MIN_NUM_DAYS) || (numDays > MAX_NUM_DAYS))
				throw new IllegalArgumentException("Number of days out of bounds: " + numDays);
			if (selectedDay >= numDays)
				throw new IllegalArgumentException("Selected day out of bounds: " + selectedDay);

			// Set instance variables
			this.dayOffset = ((prevMonthNumDays < 0) || (dayOffset >= 0)) ? dayOffset : dayOffset + NUM_COLUMNS;
			this.numDays = numDays;
			this.selectedDay = (selectedDay < 0) ? -1 : selectedDay;
			this.prevMonthNumDays = prevMonthNumDays;
			numRows = (this.dayOffset + numDays + NUM_COLUMNS - 1) / NUM_COLUMNS;
		}

		//--------------------------------------------------------------

		private int getNumPanelRows()
		{
			return fullHeight ? MAX_NUM_DAYS / NUM_COLUMNS + Math.min(MAX_NUM_DAYS % NUM_COLUMNS, 2) : numRows;
		}

		//--------------------------------------------------------------

		private String getDayString(int index,
									int length)
		{
			int i = firstDayOfWeek + index - Calendar.SUNDAY;
			if (i >= NUM_DAYS_IN_WEEK)
				i -= NUM_DAYS_IN_WEEK;
			String str = dayNames.get(i);
			if ((length > 0) && (length < str.length()))
				str = str.substring(0, length);
			return str;
		}

		//--------------------------------------------------------------

		private void incrementSelectionColumn(int increment)
		{
			int day = (selectedDay < 0) ? 0 : selectedDay;
			int column = Math.min(Math.max(0, getColumnForDay(day) + increment), NUM_COLUMNS - 1);
			day = rowColumnToDay(getRowForDay(day), column);
			day = Math.min(Math.max(MIN_DAY, day), numDays - 1);
			setSelection(day);
		}

		//--------------------------------------------------------------

		private void incrementSelectionRow(int increment)
		{
			int day = (selectedDay < 0) ? 0 : selectedDay;
			int row = Math.min(Math.max(0, getRowForDay(day) + increment), numRows - 1);
			day = rowColumnToDay(row, getColumnForDay(day));
			while (day < MIN_DAY)
				day += NUM_COLUMNS;
			while (day >= numDays)
				day -= NUM_COLUMNS;
			setSelection(day);
		}

		//--------------------------------------------------------------

		private int getCellX(int day)
		{
			return (day + dayOffset) % NUM_COLUMNS * columnWidth;
		}

		//--------------------------------------------------------------

		private int getCellY(int day)
		{
			return  titleHeight + ((day + dayOffset) / NUM_COLUMNS) * rowHeight;
		}

		//--------------------------------------------------------------

		private int getRowForDay(int day)
		{
			return (day + dayOffset) / NUM_COLUMNS;
		}

		//--------------------------------------------------------------

		private int getColumnForDay(int day)
		{
			return (day + dayOffset) % NUM_COLUMNS;
		}

		//--------------------------------------------------------------

		private int rowColumnToDay(int row,
								   int column)
		{
			return row * NUM_COLUMNS + column - dayOffset;
		}

		//--------------------------------------------------------------

		private void showPopUp(MouseEvent event)
		{
			int x = event.getX();
			int x1 = 0;
			int x2 = x1 + NUM_COLUMNS * columnWidth;
			int y = event.getY();
			int y1 = 0;
			int y2 = y1 + titleHeight;
			if ((x >= x1) && (x < x2) && (y >= y1) && (y < y2))
			{
				int index = (x - x1) / columnWidth;
				int strWidth = getFontMetrics(getFont()).stringWidth(getDayString(index, dayNameLength));
				x = x1 + index * columnWidth + (columnWidth - strWidth) / 2 - HORIZONTAL_MARGIN;
				PopUpContent popUpComponent = new PopUpContent(getDayString(index, 0), titleHeight);
				int popUpWidth = popUpComponent.getPreferredSize().width;
				Point location = new Point(x, y1);
				SwingUtilities.convertPointToScreen(location, this);
				Rectangle screen = GuiUtils.getVirtualScreenBounds(this);
				x = Math.min(location.x, screen.x + screen.width - popUpWidth);
				popUp = PopupFactory.getSharedInstance().getPopup(this, popUpComponent, x, location.y);
				popUp.show();
			}
		}

		//--------------------------------------------------------------

		private void hidePopUp()
		{
			if (popUp != null)
			{
				popUp.hide();
				popUp = null;
			}
		}

		//--------------------------------------------------------------

		private int getDay(MouseEvent event)
		{
			int day = -1;
			int x = event.getX();
			int x1 = 0;
			int x2 = x1 + NUM_COLUMNS * columnWidth;
			int y = event.getY();
			int y1 = titleHeight;
			int y2 = y1 + getNumPanelRows() * rowHeight;
			if ((x >= x1) && (x < x2) && (y >= y1) && (y < y2))
			{
				int column = (x - x1) / columnWidth;
				int row = (y - y1) / rowHeight;
				day = rowColumnToDay(row, column);
				if ((day < 0) || (day >= numDays))
					day = -1;
			}
			return day;
		}

		//--------------------------------------------------------------

		private void setSelection(int day)
		{
			if ((day >= 0) && (day != selectedDay))
			{
				selectedDay = day;
				repaint();
			}
		}

		//--------------------------------------------------------------

		private void onSelectUpUnit()
		{
			incrementSelectionRow(-1);
		}

		//--------------------------------------------------------------

		private void onSelectDownUnit()
		{
			incrementSelectionRow(1);
		}

		//--------------------------------------------------------------

		private void onSelectUpMax()
		{
			incrementSelectionRow(-numRows);
		}

		//--------------------------------------------------------------

		private void onSelectDownMax()
		{
			incrementSelectionRow(numRows);
		}

		//--------------------------------------------------------------

		private void onSelectLeftUnit()
		{
			incrementSelectionColumn(-1);
		}

		//--------------------------------------------------------------

		private void onSelectRightUnit()
		{
			incrementSelectionColumn(1);
		}

		//--------------------------------------------------------------

		private void onSelectLeftMax()
		{
			incrementSelectionColumn(-NUM_COLUMNS);
		}

		//--------------------------------------------------------------

		private void onSelectRightMax()
		{
			incrementSelectionColumn(NUM_COLUMNS);
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// CLASS: MONTH-YEAR DIALOG


	private static class MonthYearDialog
		extends JDialog
		implements ActionListener
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	int	YEAR_FIELD_LENGTH	= 4;

		private static final	Insets	BUTTON_MARGINS	= new Insets(1, 4, 1, 4);

		private static final	Color	BACKGROUND_COLOUR	= new Color(248, 244, 224);
		private static final	Color	BORDER_COLOUR		= new Color(216, 176, 72);

		// Commands
		private interface Command
		{
			String	ACCEPT	= "accept";
			String	CLOSE	= "close";
		}

		private static final	KeyAction.KeyCommandPair[]	KEY_COMMANDS	=
		{
			new KeyAction.KeyCommandPair
			(
				KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.CTRL_DOWN_MASK),
				Command.ACCEPT
			),
			new KeyAction.KeyCommandPair
			(
				KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				Command.CLOSE
			)
		};

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	boolean				accepted;
		private	JComboBox<String>	monthComboBox;
		private	FIntegerSpinner		yearSpinner;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private MonthYearDialog(Window       owner,
								int          month,
								int          year,
								List<String> monthStrs)
		{
			// Call superclass constructor
			super(owner, Dialog.ModalityType.APPLICATION_MODAL);


			//----  Control panel

			GridBagLayout gridBag = new GridBagLayout();
			GridBagConstraints gbc = new GridBagConstraints();

			JPanel controlPanel = new JPanel(gridBag);
			controlPanel.setBackground(BACKGROUND_COLOUR);

			int gridX = 0;

			// Combo box: months
			monthComboBox = new FComboBox<>();
			for (String str : monthStrs)
				monthComboBox.addItem(str);
			monthComboBox.setSelectedIndex(month);

			gbc.gridx = gridX++;
			gbc.gridy = 0;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.weightx = 0.0;
			gbc.weighty = 0.0;
			gbc.anchor = GridBagConstraints.LINE_START;
			gbc.fill = GridBagConstraints.VERTICAL;
			gbc.insets = new Insets(0, 0, 0, 0);
			gridBag.setConstraints(monthComboBox, gbc);
			controlPanel.add(monthComboBox);

			// Spinner: year
			yearSpinner = new FIntegerSpinner(year, MIN_YEAR, MAX_YEAR, YEAR_FIELD_LENGTH);

			gbc.gridx = gridX++;
			gbc.gridy = 0;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.weightx = 0.0;
			gbc.weighty = 0.0;
			gbc.anchor = GridBagConstraints.LINE_START;
			gbc.fill = GridBagConstraints.VERTICAL;
			gbc.insets = new Insets(0, 1, 0, 0);
			gridBag.setConstraints(yearSpinner, gbc);
			controlPanel.add(yearSpinner);


			//----  Button panel

			JPanel buttonPanel = new JPanel(new GridLayout(1, 0, 4, 0));
			buttonPanel.setBackground(BACKGROUND_COLOUR);
			buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));

			// Button: OK
			JButton okButton = new FButton(GuiConstants.OK_STR);
			okButton.setMargin(BUTTON_MARGINS);
			okButton.setActionCommand(Command.ACCEPT);
			okButton.addActionListener(this);
			buttonPanel.add(okButton);

			// Button: cancel
			JButton cancelButton = new FButton(GuiConstants.CANCEL_STR);
			cancelButton.setMargin(BUTTON_MARGINS);
			cancelButton.setActionCommand(Command.CLOSE);
			cancelButton.addActionListener(this);
			buttonPanel.add(cancelButton);


			//----  Main panel

			JPanel mainPanel = new JPanel(gridBag);
			mainPanel.setBackground(BACKGROUND_COLOUR);
			GuiUtils.setPaddedLineBorder(mainPanel, 1, BORDER_COLOUR);

			int gridY = 0;

			gbc.gridx = 0;
			gbc.gridy = gridY++;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.weightx = 0.0;
			gbc.weighty = 0.0;
			gbc.anchor = GridBagConstraints.NORTH;
			gbc.fill = GridBagConstraints.NONE;
			gbc.insets = new Insets(0, 0, 0, 0);
			gridBag.setConstraints(controlPanel, gbc);
			mainPanel.add(controlPanel);

			gbc.gridx = 0;
			gbc.gridy = gridY++;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.weightx = 0.0;
			gbc.weighty = 0.0;
			gbc.anchor = GridBagConstraints.NORTH;
			gbc.fill = GridBagConstraints.NONE;
			gbc.insets = new Insets(1, 0, 0, 0);
			gridBag.setConstraints(buttonPanel, gbc);
			mainPanel.add(buttonPanel);

			// Add commands to action map
			KeyAction.create(mainPanel, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, this, KEY_COMMANDS);


			//----  Window

			// Set content pane
			setContentPane(mainPanel);

			// Omit frame from dialog
			setUndecorated(true);

			// Dispose of window when it is closed
			setDefaultCloseOperation(DISPOSE_ON_CLOSE);

			// Prevent dialog from being resized
			setResizable(false);

			// Resize dialog to its preferred size
			pack();
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : ActionListener interface
	////////////////////////////////////////////////////////////////////

		@Override
		public void actionPerformed(ActionEvent event)
		{
			String command = event.getActionCommand();

			if (command.equals(Command.ACCEPT))
				onAccept();

			else if (command.equals(Command.CLOSE))
				onClose();
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		private void onAccept()
		{
			accepted = true;
			onClose();
		}

		//--------------------------------------------------------------

		private void onClose()
		{
			dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
		}

		//--------------------------------------------------------------

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	Calendar			calendar;
	private	int					firstDayOfWeek;
	private	boolean				showAdjacentMonths;
	private	List<String>		monthNames;
	private	int[]				monthNameLengths;
	private	NavigationButton	previousMonthButton;
	private	NavigationButton	previousYearButton;
	private	NavigationButton	nextMonthButton;
	private	NavigationButton	nextYearButton;
	private	JLabel				monthLabel;
	private	DaySelectionPanel	daySelectionPanel;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	/**
	 * @throws IllegalArgumentException
	 */

	public DateSelectionPanel(Date    date,
							  boolean showAdjacentMonths)
	{
		this(date.getYear(), date.getMonth(), date.getDay(), 0, showAdjacentMonths);
	}

	//------------------------------------------------------------------

	/**
	 * @throws IllegalArgumentException
	 */

	public DateSelectionPanel(Date    date,
							  int     firstDayOfWeek,
							  boolean showAdjacentMonths)
	{
		this(date.getYear(), date.getMonth(), date.getDay(), firstDayOfWeek, showAdjacentMonths);
	}

	//------------------------------------------------------------------

	/**
	 * @throws IllegalArgumentException
	 */

	public DateSelectionPanel(int     year,
							  int     month,
							  int     day,
							  boolean showAdjacentMonths)
	{
		this(year, month, day, 0, showAdjacentMonths);
	}

	//------------------------------------------------------------------

	/**
	 * @throws IllegalArgumentException
	 */

	public DateSelectionPanel(int     year,
							  int     month,
							  int     day,
							  int     firstDayOfWeek,
							  boolean showAdjacentMonths)
	{
		// Validate arguments
		if ((year < MIN_YEAR) || (year > MAX_YEAR))
			throw new IllegalArgumentException("Year out of bounds: " + year);
		if ((month < MIN_MONTH) || (month > MAX_MONTH))
			throw new IllegalArgumentException("Month out of bounds: " + month);
		if ((firstDayOfWeek < 0) || (firstDayOfWeek > Calendar.SATURDAY))
			throw new IllegalArgumentException("First day of week out of bounds: " + firstDayOfWeek);

		// Initialise instance variables
		calendar = new ModernCalendar(year, month, 1);
		if ((day < MIN_DAY) || (day >= calendar.getActualMaximum(Calendar.DAY_OF_MONTH)))
			throw new IllegalArgumentException();
		this.firstDayOfWeek = (firstDayOfWeek == 0) ? calendar.getFirstDayOfWeek() : firstDayOfWeek;
		this.showAdjacentMonths = showAdjacentMonths;

		// Initialise list of names of months
		monthNames = DateUtils.getMonthNames(getDefaultLocale());
		int numMonths = monthNames.size();

		// Initialise array of lengths of names
		monthNameLengths = new int[numMonths];
		for (int i = 0; i < numMonths; i++)
		{
			int length = MIN_MONTH_NAME_LENGTH - 1;
			boolean done = false;
			while (!done)
			{
				++length;
				String str0 = monthNames.get(i);
				if (length < str0.length())
					str0 = str0.substring(0, length);
				int j = 0;
				while (j < numMonths)
				{
					if (i != j)
					{
						String str1 = monthNames.get(j);
						if (length < str1.length())
							str1 = str1.substring(0, length);
						if (str1.equals(str0))
							break;
					}
					++j;
				}
				if (j == numMonths)
					done = true;
			}
			monthNameLengths[i] = length;
		}


		//----  Control panel

		GridBagLayout gridBag = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();

		JPanel controlPanel = new JPanel(gridBag);
		controlPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, NAVIGATION_PANEL_BORDER_COLOUR));

		int gridX = 0;

		// Button: previous year
		previousYearButton = new NavigationButton(ANGLE_DOUBLE_LEFT_ICON);
		previousYearButton.setToolTipText(PREVIOUS_YEAR_STR);
		previousYearButton.setActionCommand(Command.PREVIOUS_YEAR);
		previousYearButton.addActionListener(this);

		gbc.gridx = gridX++;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(previousYearButton, gbc);
		controlPanel.add(previousYearButton);

		// Button: previous month
		previousMonthButton = new NavigationButton(ANGLE_SINGLE_LEFT_ICON);
		previousMonthButton.setToolTipText(PREVIOUS_MONTH_STR);
		previousMonthButton.setActionCommand(Command.PREVIOUS_MONTH);
		previousMonthButton.addActionListener(this);

		gbc.gridx = gridX++;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 1, 0, 0);
		gridBag.setConstraints(previousMonthButton, gbc);
		controlPanel.add(previousMonthButton);

		// Label: month and year
		monthLabel = new FLabel("");
		monthLabel.setFont(monthLabel.getFont().deriveFont(Font.BOLD));
		monthLabel.setHorizontalAlignment(SwingConstants.CENTER);
		FontMetrics fontMetrics = monthLabel.getFontMetrics(monthLabel.getFont());
		int width = 0;
		for (int i = 0; i < monthNames.size(); i++)
		{
			int strWidth = fontMetrics.stringWidth(getMonthString(i));
			if (width < strWidth)
				width = strWidth;
		}
		width += fontMetrics.stringWidth(" " + PROTOTYPE_YEAR_STR);
		monthLabel.setPreferredSize(new Dimension(2 * MONTH_LABEL_HORIZONTAL_MARGIN + width,
												  2 * MONTH_LABEL_VERTICAL_MARGIN + fontMetrics.getAscent()
																						+ fontMetrics.getDescent()));
		monthLabel.setOpaque(true);
		monthLabel.setBackground(MONTH_LABEL_BACKGROUND_COLOUR);
		monthLabel.setText(getMonthString(year, month));
		monthLabel.addMouseListener(this);

		gbc.gridx = gridX++;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(monthLabel, gbc);
		controlPanel.add(monthLabel);

		// Button: next month
		nextMonthButton = new NavigationButton(ANGLE_SINGLE_RIGHT_ICON);
		nextMonthButton.setToolTipText(NEXT_MONTH_STR);
		nextMonthButton.setActionCommand(Command.NEXT_MONTH);
		nextMonthButton.addActionListener(this);

		gbc.gridx = gridX++;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 1);
		gridBag.setConstraints(nextMonthButton, gbc);
		controlPanel.add(nextMonthButton);

		// Button: next year
		nextYearButton = new NavigationButton(ANGLE_DOUBLE_RIGHT_ICON);
		nextYearButton.setToolTipText(NEXT_YEAR_STR);
		nextYearButton.setActionCommand(Command.NEXT_YEAR);
		nextYearButton.addActionListener(this);

		gbc.gridx = gridX++;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(nextYearButton, gbc);
		controlPanel.add(nextYearButton);


		//----  Day selection panel

		daySelectionPanel = new DaySelectionPanel(getDayOffset(), getDaysInMonth(), day,
												  showAdjacentMonths ? getDaysInPrevMonth() : -1,
												  this.firstDayOfWeek, true);


		//----  Outer panel

		setLayout(gridBag);

		int gridY = 0;

		gbc.gridx = 0;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(controlPanel, gbc);
		add(controlPanel);

		gbc.gridx = 0;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(daySelectionPanel, gbc);
		add(daySelectionPanel);

		// Add commands to action map
		KeyAction.create(this, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, this, KEY_COMMANDS);

		// Update buttons
		updateButtons();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : ActionListener interface
////////////////////////////////////////////////////////////////////////

	@Override
	public void actionPerformed(ActionEvent event)
	{
		String command = event.getActionCommand();

		if (command.equals(Command.PREVIOUS_MONTH))
			onPreviousMonth();

		else if (command.equals(Command.PREVIOUS_YEAR))
			onPreviousYear();

		else if (command.equals(Command.NEXT_MONTH))
			onNextMonth();

		else if (command.equals(Command.NEXT_YEAR))
			onNextYear();

		else if (command.equals(Command.EDIT_MONTH_YEAR))
			onEditMonthYear();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : MouseListener interface
////////////////////////////////////////////////////////////////////////

	@Override
	public void mouseClicked(MouseEvent event)
	{
		if (SwingUtilities.isLeftMouseButton(event))
			onEditMonthYear();
	}

	//------------------------------------------------------------------

	@Override
	public void mouseEntered(MouseEvent event)
	{
		// do nothing
	}

	//------------------------------------------------------------------

	@Override
	public void mouseExited(MouseEvent event)
	{
		// do nothing
	}

	//------------------------------------------------------------------

	@Override
	public void mousePressed(MouseEvent event)
	{
		// do nothing
	}

	//------------------------------------------------------------------

	@Override
	public void mouseReleased(MouseEvent event)
	{
		// do nothing
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	@Override
	public boolean requestFocusInWindow()
	{
		return daySelectionPanel.requestFocusInWindow();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public Date getDate()
	{
		int day = daySelectionPanel.getSelectedDay();
		return ((day < 0) ? null : new Date(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), day));
	}

	//------------------------------------------------------------------

	public void setAcceptAction(Action action)
	{
		daySelectionPanel.acceptAction = action;
	}

	//------------------------------------------------------------------

	private void updateButtons()
	{
		previousMonthButton.setEnabled((calendar.get(Calendar.MONTH) > MIN_MONTH)
										|| (calendar.get(Calendar.YEAR) > MIN_YEAR));
		previousYearButton.setEnabled(calendar.get(Calendar.YEAR) > MIN_YEAR);
		nextMonthButton.setEnabled((calendar.get(Calendar.MONTH) < MAX_MONTH)
									|| (calendar.get(Calendar.YEAR) < MAX_YEAR));
		nextYearButton.setEnabled(calendar.get(Calendar.YEAR) < MAX_YEAR);
	}

	//------------------------------------------------------------------

	private int getDayOffset()
	{
		int offset = calendar.get(Calendar.DAY_OF_WEEK) - firstDayOfWeek;
		if (offset < 0)
			offset += NUM_DAYS_IN_WEEK;
		return offset;
	}

	//------------------------------------------------------------------

	private int getDaysInMonth()
	{
		return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
	}

	//------------------------------------------------------------------

	private int getDaysInPrevMonth()
	{
		ModernCalendar calendar = new ModernCalendar();
		calendar.setTimeInMillis(this.calendar.getTimeInMillis());
		calendar.roll(Calendar.MONTH, false);
		return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
	}

	//------------------------------------------------------------------

	private String getMonthString(int index)
	{
		String str = monthNames.get(index);
		if (monthNameLengths[index] < str.length())
			str = str.substring(0, monthNameLengths[index]);
		return str;
	}

	//------------------------------------------------------------------

	private String getMonthString(int year,
								  int month)
	{
		return (getMonthString(month) + " " + Integer.toString(year));
	}

	//------------------------------------------------------------------

	private void setMonth(int year,
						  int month)
	{
		calendar = new ModernCalendar(year, month, 1);
		int daysInMonth = getDaysInMonth();
		int selectedDay = Math.min(daySelectionPanel.getSelectedDay(), daysInMonth - 1);
		daySelectionPanel.setMonth(getDayOffset(), daysInMonth, selectedDay,
								   showAdjacentMonths ? getDaysInPrevMonth() : -1);
		monthLabel.setText(getMonthString(year, month));
		updateButtons();
	}

	//------------------------------------------------------------------

	private void onPreviousMonth()
	{
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		if (month == MIN_MONTH)
		{
			if (year > MIN_YEAR)
				setMonth(year - 1, MAX_MONTH);
		}
		else
			setMonth(year, month - 1);
	}

	//------------------------------------------------------------------

	private void onPreviousYear()
	{
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		if (year > MIN_YEAR)
			setMonth(year - 1, month);
	}

	//------------------------------------------------------------------

	private void onNextMonth()
	{
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		if (month == MAX_MONTH)
		{
			if (year < MAX_YEAR)
				setMonth(year + 1, MIN_MONTH);
		}
		else
			setMonth(year, month + 1);
	}

	//------------------------------------------------------------------

	private void onNextYear()
	{
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		if (year < MAX_YEAR)
			setMonth(year + 1, month);
	}

	//------------------------------------------------------------------

	private void onEditMonthYear()
	{
		MonthYearDialog dialog = new MonthYearDialog(SwingUtilities.getWindowAncestor(this),
													 calendar.get(Calendar.MONTH),
													 calendar.get(Calendar.YEAR), monthNames);
		Point location = getLocationOnScreen();
		location.x += (getWidth() - dialog.getWidth()) / 2;
		location.y += monthLabel.getHeight();
		dialog.setLocation(GuiUtils.getComponentLocation(dialog, location));
		dialog.setVisible(true);

		if (dialog.accepted)
			setMonth(dialog.yearSpinner.getIntValue(), dialog.monthComboBox.getSelectedIndex());
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Image data
////////////////////////////////////////////////////////////////////////

	/**
	 * PNG image data.
	 */

	private interface ImgData
	{
		// File: angleSingleL-13x10
		byte[]	ANGLE_SINGLE_LEFT	=
		{
			(byte)0x89, (byte)0x50, (byte)0x4E, (byte)0x47, (byte)0x0D, (byte)0x0A, (byte)0x1A, (byte)0x0A,
			(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x0D, (byte)0x49, (byte)0x48, (byte)0x44, (byte)0x52,
			(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x0D, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x0A,
			(byte)0x08, (byte)0x06, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x6F, (byte)0xEE, (byte)0xD4,
			(byte)0xC4, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x51, (byte)0x49, (byte)0x44, (byte)0x41,
			(byte)0x54, (byte)0x78, (byte)0x5E, (byte)0x63, (byte)0x60, (byte)0xC0, (byte)0x03, (byte)0x12,
			(byte)0x13, (byte)0x13, (byte)0x33, (byte)0x13, (byte)0x12, (byte)0x12, (byte)0x3E, (byte)0x01,
			(byte)0xB1, (byte)0x17, (byte)0xBA, (byte)0x1C, (byte)0x56, (byte)0x00, (byte)0x54, (byte)0x98,
			(byte)0x05, (byte)0xC4, (byte)0xFF, (byte)0x40, (byte)0x18, (byte)0xA8, (byte)0xD9, (byte)0x07,
			(byte)0x5D, (byte)0x1E, (byte)0x03, (byte)0x00, (byte)0x15, (byte)0xA6, (byte)0x22, (byte)0x69,
			(byte)0xC8, (byte)0x41, (byte)0x97, (byte)0xC7, (byte)0x00, (byte)0xE4, (byte)0x68, (byte)0x80,
			(byte)0x3B, (byte)0x09, (byte)0xC4, (byte)0x46, (byte)0x97, (byte)0xC7, (byte)0x0A, (byte)0xC8,
			(byte)0xD2, (byte)0x04, (byte)0x02, (byte)0xC8, (byte)0xCE, (byte)0x03, (byte)0xE2, (byte)0x5C,
			(byte)0x74, (byte)0x79, (byte)0x9C, (byte)0x80, (byte)0x12, (byte)0x8D, (byte)0xA4, (byte)0x05,
			(byte)0x39, (byte)0x0C, (byte)0xE0, (byte)0x8A, (byte)0x5C, (byte)0x00, (byte)0x66, (byte)0x3D,
			(byte)0x44, (byte)0x57, (byte)0xFB, (byte)0x67, (byte)0xB0, (byte)0x00, (byte)0x00, (byte)0x00,
			(byte)0x00, (byte)0x00, (byte)0x49, (byte)0x45, (byte)0x4E, (byte)0x44, (byte)0xAE, (byte)0x42,
			(byte)0x60, (byte)0x82
		};

		// File: angleSingleR-13x10
		byte[]	ANGLE_SINGLE_RIGHT	=
		{
			(byte)0x89, (byte)0x50, (byte)0x4E, (byte)0x47, (byte)0x0D, (byte)0x0A, (byte)0x1A, (byte)0x0A,
			(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x0D, (byte)0x49, (byte)0x48, (byte)0x44, (byte)0x52,
			(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x0D, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x0A,
			(byte)0x08, (byte)0x06, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x6F, (byte)0xEE, (byte)0xD4,
			(byte)0xC4, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x4B, (byte)0x49, (byte)0x44, (byte)0x41,
			(byte)0x54, (byte)0x78, (byte)0x5E, (byte)0x63, (byte)0x60, (byte)0x40, (byte)0x02, (byte)0x09,
			(byte)0x09, (byte)0x09, (byte)0x5E, (byte)0x40, (byte)0xFC, (byte)0x29, (byte)0x31, (byte)0x31,
			(byte)0x31, (byte)0x13, (byte)0x59, (byte)0x1C, (byte)0x2F, (byte)0x00, (byte)0x2A, (byte)0xF6,
			(byte)0x01, (byte)0x6A, (byte)0xFA, (byte)0x07, (byte)0xC5, (byte)0x59, (byte)0xE8, (byte)0xF2,
			(byte)0x38, (byte)0x01, (byte)0x50, (byte)0x71, (byte)0x2A, (byte)0x92, (byte)0xC6, (byte)0x5C,
			(byte)0x74, (byte)0x79, (byte)0x9C, (byte)0x80, (byte)0x12, (byte)0x8D, (byte)0x59, (byte)0x24,
			(byte)0x3B, (byte)0x95, (byte)0x64, (byte)0x4D, (byte)0xC8, (byte)0xCE, (byte)0x03, (byte)0x06,
			(byte)0x4E, (byte)0x0E, (byte)0xBA, (byte)0x3C, (byte)0x06, (byte)0x20, (byte)0x59, (byte)0x03,
			(byte)0x59, (byte)0x41, (byte)0x9E, (byte)0x40, (byte)0x64, (byte)0xE4, (byte)0x02, (byte)0x00,
			(byte)0xFC, (byte)0xBD, (byte)0x44, (byte)0x57, (byte)0xE7, (byte)0x8F, (byte)0x33, (byte)0x22,
			(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x49, (byte)0x45, (byte)0x4E, (byte)0x44,
			(byte)0xAE, (byte)0x42, (byte)0x60, (byte)0x82
		};

		// File: angleDoubleL-13x10
		byte[]	ANGLE_DOUBLE_LEFT	=
		{
			(byte)0x89, (byte)0x50, (byte)0x4E, (byte)0x47, (byte)0x0D, (byte)0x0A, (byte)0x1A, (byte)0x0A,
			(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x0D, (byte)0x49, (byte)0x48, (byte)0x44, (byte)0x52,
			(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x0D, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x0A,
			(byte)0x08, (byte)0x06, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x6F, (byte)0xEE, (byte)0xD4,
			(byte)0xC4, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x60, (byte)0x49, (byte)0x44, (byte)0x41,
			(byte)0x54, (byte)0x78, (byte)0x5E, (byte)0x63, (byte)0x60, (byte)0x40, (byte)0x03, (byte)0xB1,
			(byte)0xB1, (byte)0xB1, (byte)0x62, (byte)0x09, (byte)0x09, (byte)0x09, (byte)0x67, (byte)0xE2,
			(byte)0xE3, (byte)0xE3, (byte)0x0F, (byte)0xE3, (byte)0x13, (byte)0x83, (byte)0x03, (byte)0x90,
			(byte)0x24, (byte)0x50, (byte)0xE2, (byte)0x32, (byte)0x50, (byte)0xC1, (byte)0x7F, (byte)0x20,
			(byte)0x3E, (byte)0x85, (byte)0x4B, (byte)0x0C, (byte)0x0E, (byte)0x12, (byte)0x13, (byte)0x13,
			(byte)0x45, (byte)0x81, (byte)0x82, (byte)0x97, (byte)0xA0, (byte)0x92, (byte)0xD7, (byte)0x81,
			(byte)0x58, (byte)0x02, (byte)0x9B, (byte)0x18, (byte)0xF9, (byte)0x1A, (byte)0x90, (byte)0xAD,
			(byte)0x07, (byte)0xD1, (byte)0x20, (byte)0x3E, (byte)0x36, (byte)0x31, (byte)0xB8, (byte)0x06,
			(byte)0xB2, (byte)0x35, (byte)0x81, (byte)0x00, (byte)0xBA, (byte)0x53, (byte)0x62, (byte)0x62,
			(byte)0x62, (byte)0x24, (byte)0xB1, (byte)0x89, (byte)0xA1, (byte)0xEB, (byte)0x23, (byte)0x5F,
			(byte)0x23, (byte)0xB6, (byte)0xE0, (byte)0xC5, (byte)0x26, (byte)0x86, (byte)0x01, (byte)0xB0,
			(byte)0x45, (byte)0x24, (byte)0xBA, (byte)0x18, (byte)0x00, (byte)0x77, (byte)0x86, (byte)0x95,
			(byte)0x75, (byte)0x53, (byte)0xA0, (byte)0x2E, (byte)0x22, (byte)0x00, (byte)0x00, (byte)0x00,
			(byte)0x00, (byte)0x49, (byte)0x45, (byte)0x4E, (byte)0x44, (byte)0xAE, (byte)0x42, (byte)0x60,
			(byte)0x82
		};

		// File: angleDoubleR-13x10
		byte[]	ANGLE_DOUBLE_RIGHT	=
		{
			(byte)0x89, (byte)0x50, (byte)0x4E, (byte)0x47, (byte)0x0D, (byte)0x0A, (byte)0x1A, (byte)0x0A,
			(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x0D, (byte)0x49, (byte)0x48, (byte)0x44, (byte)0x52,
			(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x0D, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x0A,
			(byte)0x08, (byte)0x06, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x6F, (byte)0xEE, (byte)0xD4,
			(byte)0xC4, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x67, (byte)0x49, (byte)0x44, (byte)0x41,
			(byte)0x54, (byte)0x78, (byte)0x5E, (byte)0x63, (byte)0x60, (byte)0x00, (byte)0x82, (byte)0xF8,
			(byte)0xF8, (byte)0xF8, (byte)0xC3, (byte)0x09, (byte)0x09, (byte)0x09, (byte)0x67, (byte)0x62,
			(byte)0x63, (byte)0x63, (byte)0xC5, (byte)0x40, (byte)0x7C, (byte)0x5C, (byte)0x62, (byte)0x28,
			(byte)0x00, (byte)0x28, (byte)0x79, (byte)0x0A, (byte)0x88, (byte)0xFF, (byte)0x03, (byte)0x15,
			(byte)0x5E, (byte)0x86, (byte)0x29, (byte)0xC2, (byte)0x26, (byte)0x86, (byte)0x02, (byte)0x12,
			(byte)0x13, (byte)0x13, (byte)0x45, (byte)0x81, (byte)0x0A, (byte)0x2E, (byte)0x81, (byte)0x14,
			(byte)0x01, (byte)0xF1, (byte)0xF5, (byte)0x98, (byte)0x98, (byte)0x18, (byte)0x49, (byte)0x6C,
			(byte)0x62, (byte)0xE8, (byte)0xFA, (byte)0xC8, (byte)0xD7, (byte)0x08, (byte)0x72, (byte)0x06,
			(byte)0xC8, (byte)0x39, (byte)0xC8, (byte)0xCE, (byte)0xC2, (byte)0x26, (byte)0x46, (byte)0x99,
			(byte)0x26, (byte)0x74, (byte)0xA7, (byte)0x00, (byte)0xB1, (byte)0x04, (byte)0x36, (byte)0x31,
			(byte)0xF2, (byte)0x35, (byte)0x80, (byte)0x40, (byte)0x02, (byte)0x96, (byte)0xE0, (byte)0xC5,
			(byte)0x26, (byte)0x86, (byte)0x02, (byte)0xE2, (byte)0xB1, (byte)0x44, (byte)0x24, (byte)0x36,
			(byte)0x31, (byte)0x18, (byte)0x00, (byte)0x00, (byte)0xD2, (byte)0x96, (byte)0x95, (byte)0x75,
			(byte)0xE0, (byte)0xB8, (byte)0x56, (byte)0x5F, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
			(byte)0x49, (byte)0x45, (byte)0x4E, (byte)0x44, (byte)0xAE, (byte)0x42, (byte)0x60, (byte)0x82
		};
	}

	//==================================================================

}

//----------------------------------------------------------------------
