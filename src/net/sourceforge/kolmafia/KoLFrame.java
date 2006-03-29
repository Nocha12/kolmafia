/**
 * Copyright (c) 2005, KoLmafia development team
 * http://kolmafia.sourceforge.net/
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  [1] Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *  [2] Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in
 *      the documentation and/or other materials provided with the
 *      distribution.
 *  [3] Neither the name "KoLmafia development team" nor the names of
 *      its contributors may be used to endorse or promote products
 *      derived from this software without specific prior written
 *      permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package net.sourceforge.kolmafia;

// containers
import java.awt.Image;
import javax.swing.JDialog;
import javax.swing.JToolBar;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JFileChooser;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.Box;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JComboBox;

// layout
import java.awt.Point;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Dimension;
import java.awt.CardLayout;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import javax.swing.BoxLayout;
import javax.swing.table.TableColumnModel;

// event listeners
import javax.swing.ImageIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;

// basic utilities
import java.io.FileInputStream;
import java.lang.reflect.Method;

import java.util.List;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.Properties;
import java.util.StringTokenizer;

// other stuff
import javax.swing.SwingUtilities;
import java.lang.ref.WeakReference;

// spellcast imports
import net.java.dev.spellcast.utilities.ActionPanel;
import net.java.dev.spellcast.utilities.JComponentUtilities;
import net.java.dev.spellcast.utilities.LockableListModel;
import net.java.dev.spellcast.utilities.SortedListModel;

/**
 * An extended <code>JFrame</code> which provides all the frames in
 * KoLmafia the ability to update their displays, given some integer
 * value and the message to use for updating.
 */

public abstract class KoLFrame extends JDialog implements KoLConstants
{
	protected static final Color ERROR_COLOR = new Color( 255, 192, 192 );
	protected static final Color ENABLED_COLOR = new Color( 192, 255, 192 );
	protected static final Color DISABLED_COLOR = null;

	private String lastTitle;

	private String frameName;
	protected JPanel framePanel;
	protected JToolBar toolbarPanel;

	protected JPanel compactPane;
	protected JLabel levelLabel, roninLabel, mcdLabel;
	protected JLabel musLabel, mysLabel, moxLabel, drunkLabel;
	protected JLabel hpLabel, mpLabel, meatLabel, advLabel;
	protected JLabel familiarLabel;

	protected KoLCharacterAdapter refreshListener;

	/**
	 * Constructs a new <code>KoLFrame</code> with the given title,
	 * to be associated with the given StaticEntity.getClient().
	 */

	protected KoLFrame()
	{	this( "" );
	}

	/**
	 * Constructs a new <code>KoLFrame</code> with the given title,
	 * to be associated with the given StaticEntity.getClient().
	 */

	protected KoLFrame( String title )
	{
		super( KoLDesktop.getInstance(), title );
		setDefaultCloseOperation( DISPOSE_ON_CLOSE );

		this.lastTitle = title;
		this.framePanel = new JPanel( new BorderLayout( 0, 0 ) );
		getContentPane().add( this.framePanel, BorderLayout.CENTER );

		this.toolbarPanel = null;

		switch ( Integer.parseInt( GLOBAL_SETTINGS.getProperty( "toolbarPosition" ) ) )
		{
			case 1:
				this.toolbarPanel = new JToolBar( "KoLmafia Toolbar" );
				getContentPane().add( toolbarPanel, BorderLayout.NORTH );
				break;

			case 2:
				this.toolbarPanel = new JToolBar( "KoLmafia Toolbar" );
				getContentPane().add( toolbarPanel, BorderLayout.SOUTH );
				break;

			case 3:
				this.toolbarPanel = new JToolBar( "KoLmafia Toolbar", JToolBar.VERTICAL );
				getContentPane().add( toolbarPanel, BorderLayout.WEST );
				break;

			case 4:
				this.toolbarPanel = new JToolBar( "KoLmafia Toolbar", JToolBar.VERTICAL );
				getContentPane().add( toolbarPanel, BorderLayout.EAST );
				break;

			default:

				this.toolbarPanel = new JToolBar( "KoLmafia Toolbar" );
				if ( this instanceof LoginFrame || this instanceof ChatFrame )
				{
					getContentPane().add( toolbarPanel, BorderLayout.NORTH );
					break;
				}
		}

		this.frameName = getClass().getName();
		this.frameName = frameName.substring( frameName.lastIndexOf( "." ) + 1 );
		this.existingFrames.add( this );

		if ( useSidePane() )
			addCompactPane();
	}
	
	public void setTitle( String newTitle )
	{
		this.lastTitle = newTitle;
		if ( KoLCharacter.getUsername().length() > 0 )
			setTitle( KoLCharacter.getUsername() + ": " + this.lastTitle );
		else
			setTitle( this.lastTitle );
	}
	
	public void updateTitle()
	{	setTitle( lastTitle );
	}

	public boolean useSidePane()
	{	return false;
	}

	/**
	 * Overrides the default behavior of dispose so that the frames
	 * are removed from the internal list of existing frames.  Also
	 * allows for automatic exit.
	 */

	public void dispose()
	{
		super.dispose();

		// Determine which frame needs to be removed from
		// the maintained list of frames.

		existingFrames.remove( this );

		if ( refreshListener != null )
			KoLCharacter.removeCharacterListener( refreshListener );

		// If the list of frames is now empty, make sure
		// you end the session.  Ending the session for
		// a login frame involves exiting, and ending the
		// session for all other frames is restarting the
		// initial StaticEntity.getClient().

		if ( existingFrames.isEmpty() )
		{
			KoLMessenger.dispose();
			StaticEntity.closeSession();

			if ( this instanceof LoginFrame )
			{
				SystemTrayFrame.removeTrayIcon();
				System.exit(0);
			}
			else
				KoLmafiaGUI.main( new String[1] );
		}
	}

	public String getFrameName()
	{	return frameName;
	}

	/**
	 * Method which adds a compact pane to the west side of the component.
	 * Note that this method can only be used if the KoLFrame on which it
	 * is called has not yet added any components.  If there are any added
	 * components, this method will do nothing.
	 */

	public void addCompactPane()
	{
		if ( framePanel.getComponentCount() != 0 )
			return;

		boolean useTextOnly = GLOBAL_SETTINGS.getProperty( "useTextHeavySidepane" ).equals( "true" );
		StatusRefresher refresher;

		if ( useTextOnly )
			addTextOnlyCompactPane();
		else
			addGraphicalCompactPane();

		refresher = new StatusRefresher( useTextOnly );
		refresher.run();

		this.refreshListener = new KoLCharacterAdapter( refresher );
		KoLCharacter.addCharacterListener( refreshListener );
		compactPane.setBackground( ENABLED_COLOR );
	}

	public void addTextOnlyCompactPane()
	{
		JPanel [] panels = new JPanel[4];
		int panelCount = -1;

		panels[ ++panelCount ] = new JPanel( new GridLayout( 3, 1 ) );
		panels[ panelCount ].add( levelLabel = new JLabel( " ", JLabel.CENTER ) );
		panels[ panelCount ].add( roninLabel = new JLabel( " ", JLabel.CENTER ) );

		if ( KoLCharacter.inMysticalitySign() || true )
			panels[ panelCount ].add( mcdLabel = new JLabel( " ", JLabel.CENTER ) );

		panels[ ++panelCount ] = new JPanel( new GridLayout( 4, 2 ) );
		panels[ panelCount ].add( new JLabel( "Mus: ", JLabel.RIGHT ) );
		panels[ panelCount ].add( musLabel = new JLabel( " ", JLabel.LEFT ) );
		panels[ panelCount ].add( new JLabel( "Mys: ", JLabel.RIGHT ) );
		panels[ panelCount ].add( mysLabel = new JLabel( " ", JLabel.LEFT ) );
		panels[ panelCount ].add( new JLabel( "Mox: ", JLabel.RIGHT ) );
		panels[ panelCount ].add( moxLabel = new JLabel( " ", JLabel.LEFT ) );
		panels[ panelCount ].add( new JLabel( "Drunk: ", JLabel.RIGHT ) );
		panels[ panelCount ].add( drunkLabel = new JLabel( " ", JLabel.LEFT) );

		panels[ ++panelCount ] = new JPanel( new BorderLayout() );
		panels[ panelCount ].setOpaque( false );

			JPanel labelPanel = new JPanel( new GridLayout( 4, 1 ) );
			labelPanel.setOpaque( false );

			labelPanel.add( new JLabel( "    HP: ", JLabel.RIGHT ) );
			labelPanel.add( new JLabel( "    MP: ", JLabel.RIGHT ) );
			labelPanel.add( new JLabel( "    Meat: ", JLabel.RIGHT ) );
			labelPanel.add( new JLabel( "    Adv: ", JLabel.RIGHT ) );

			JPanel valuePanel = new JPanel( new GridLayout( 4, 1 ) );
			valuePanel.setOpaque( false );

			valuePanel.add( hpLabel = new JLabel( " ", JLabel.LEFT ) );
			valuePanel.add( mpLabel = new JLabel( " ", JLabel.LEFT ) );
			valuePanel.add( meatLabel = new JLabel( " ", JLabel.LEFT ) );
			valuePanel.add( advLabel = new JLabel( " ", JLabel.LEFT ) );

		panels[ panelCount ].add( labelPanel, BorderLayout.WEST );
		panels[ panelCount ].add( valuePanel, BorderLayout.CENTER );

		panels[ ++panelCount ] = new JPanel( new GridLayout( 1, 1 ) );
		panels[ panelCount ].add( familiarLabel = new UnanimatedLabel() );

		JPanel compactContainer = new JPanel();
		compactContainer.setOpaque( false );
		compactContainer.setLayout( new BoxLayout( compactContainer, BoxLayout.Y_AXIS ) );

		for ( int i = 0; i < panels.length; ++i )
		{
			panels[i].setOpaque( false );
			compactContainer.add( panels[i] );
			compactContainer.add( Box.createVerticalStrut( 20 ) );
		}

		JPanel compactCard = new JPanel( new CardLayout( 8, 8 ) );
		compactCard.setOpaque( false );
		compactCard.add( compactContainer, "" );

		JPanel refreshPanel = new JPanel();
		refreshPanel.setOpaque( false );
		refreshPanel.add( new RequestButton( "Refresh Status", "refresh.gif", new CharsheetRequest( StaticEntity.getClient() ) ) );

		this.compactPane = new JPanel( new BorderLayout() );
		this.compactPane.add( compactCard, BorderLayout.NORTH );
		this.compactPane.add( refreshPanel, BorderLayout.SOUTH );

		framePanel.setLayout( new BorderLayout() );
		framePanel.add( this.compactPane, BorderLayout.WEST );
	}

	private final void addGraphicalCompactPane()
	{
		JPanel compactPane = new JPanel( new GridLayout( 7, 1, 0, 20 ) );
		compactPane.setOpaque( false );

		compactPane.add( hpLabel = new JLabel( " ", JComponentUtilities.getSharedImage( "hp.gif" ), JLabel.CENTER ) );
		compactPane.add( mpLabel = new JLabel( " ", JComponentUtilities.getSharedImage( "mp.gif" ), JLabel.CENTER ) );

		compactPane.add( familiarLabel = new UnanimatedLabel() );

		compactPane.add( meatLabel = new JLabel( " ", JComponentUtilities.getSharedImage( "meat.gif" ), JLabel.CENTER ) );
		compactPane.add( advLabel = new JLabel( " ", JComponentUtilities.getSharedImage( "hourglass.gif" ), JLabel.CENTER ) );
		compactPane.add( drunkLabel = new JLabel( " ", JComponentUtilities.getSharedImage( "sixpack.gif" ), JLabel.CENTER) );

		compactPane.add( Box.createHorizontalStrut( 80 ) );

		this.compactPane = new JPanel();
		this.compactPane.setLayout( new BoxLayout( this.compactPane, BoxLayout.Y_AXIS ) );
		this.compactPane.add( Box.createVerticalStrut( 20 ) );
		this.compactPane.add( compactPane );

		framePanel.setLayout( new BorderLayout() );
		framePanel.add( this.compactPane, BorderLayout.WEST );
	}

	protected class StatusRefresher implements Runnable
	{
		private boolean useTextOnly;

		public StatusRefresher( boolean useTextOnly )
		{	this.useTextOnly = useTextOnly;
		}

		private String getStatText( int adjusted, int base )
		{
			return adjusted == base ? "<html>" + Integer.toString( base ) :
				adjusted >  base ? "<html><font color=blue>" + Integer.toString( adjusted ) + "</font> (" + Integer.toString( base ) + ")" :
				"<html><font color=red>" + Integer.toString( adjusted ) + "</font> (" + Integer.toString( base ) + ")";
		}

		public void run()
		{
			if ( useTextOnly )
				updateTextOnly();
			else
				updateGraphical();

			FamiliarData familiar = KoLCharacter.getFamiliar();
			int id = familiar == null ? -1 : familiar.getID();

			if ( id == -1 )
			{
				familiarLabel.setIcon( JComponentUtilities.getSharedImage( "debug.gif" ) );
				familiarLabel.setText( "0 lbs." );
				familiarLabel.setVerticalTextPosition( JLabel.BOTTOM );
				familiarLabel.setHorizontalTextPosition( JLabel.CENTER );
			}
			else
			{
				ImageIcon familiarIcon = FamiliarsDatabase.getFamiliarImage( id );
				familiarLabel.setIcon( familiarIcon );
				familiarLabel.setText( familiar.getModifiedWeight() + (familiar.getModifiedWeight() == 1 ? " lb." : " lbs.") );
				familiarLabel.setVerticalTextPosition( JLabel.BOTTOM );
				familiarLabel.setHorizontalTextPosition( JLabel.CENTER );

				familiarLabel.updateUI();
			}
		}

		private void updateTextOnly()
		{
			levelLabel.setText( "Level " + KoLCharacter.getLevel() );
			roninLabel.setText( KoLCharacter.isHardcore() ? "(Hardcore)" : KoLCharacter.canInteract() ? "(Ronin Clear)" :
				"(Ronin for " + (600 - KoLCharacter.getTotalTurnsUsed()) + ")" );

			mcdLabel.setText( "MCD @ " + KoLCharacter.getMindControlLevel() );

			musLabel.setText( getStatText( KoLCharacter.getAdjustedMuscle(), KoLCharacter.getBaseMuscle() ) );
			mysLabel.setText( getStatText( KoLCharacter.getAdjustedMysticality(), KoLCharacter.getBaseMysticality() ) );
			moxLabel.setText( getStatText( KoLCharacter.getAdjustedMoxie(), KoLCharacter.getBaseMoxie() ) );

			drunkLabel.setText( String.valueOf( KoLCharacter.getInebriety() ) );

			hpLabel.setText( df.format( KoLCharacter.getCurrentHP() ) + "/" + df.format( KoLCharacter.getMaximumHP() ) );
			mpLabel.setText( df.format( KoLCharacter.getCurrentMP() ) + "/" + df.format( KoLCharacter.getMaximumMP() ) );
			meatLabel.setText( df.format( KoLCharacter.getAvailableMeat() ) );
			advLabel.setText( String.valueOf( KoLCharacter.getAdventuresLeft() ) );
		}

		private void updateGraphical()
		{
			hpLabel.setText( KoLCharacter.getCurrentHP() + " / " + KoLCharacter.getMaximumHP() );
			hpLabel.setVerticalTextPosition( JLabel.BOTTOM );
			hpLabel.setHorizontalTextPosition( JLabel.CENTER );

			mpLabel.setText( KoLCharacter.getCurrentMP() + " / " + KoLCharacter.getMaximumMP() );
			mpLabel.setVerticalTextPosition( JLabel.BOTTOM );
			mpLabel.setHorizontalTextPosition( JLabel.CENTER );

			meatLabel.setText( df.format( KoLCharacter.getAvailableMeat() ) );
			meatLabel.setVerticalTextPosition( JLabel.BOTTOM );
			meatLabel.setHorizontalTextPosition( JLabel.CENTER );

			advLabel.setText( String.valueOf( KoLCharacter.getAdventuresLeft() ) );
			advLabel.setVerticalTextPosition( JLabel.BOTTOM );
			advLabel.setHorizontalTextPosition( JLabel.CENTER );

			drunkLabel.setText( String.valueOf( KoLCharacter.getInebriety() ) );
			drunkLabel.setVerticalTextPosition( JLabel.BOTTOM );
			drunkLabel.setHorizontalTextPosition( JLabel.CENTER );
		}
	}

	public void updateDisplayState( int displayState )
	{
		// Change the background of the frame based on
		// the current display state -- but only if the
		// compact pane has already been constructed.

		switch ( displayState )
		{
			case ERROR_STATE:

				if ( compactPane != null )
					compactPane.setBackground( ERROR_COLOR );

				setEnabled( false );
				break;

			case ENABLE_STATE:

				if ( compactPane != null )
					compactPane.setBackground( ENABLED_COLOR );

				setEnabled( true );
				break;

			case ABORT_STATE:
			case CONTINUE_STATE:

				if ( compactPane != null )
					compactPane.setBackground( DISABLED_COLOR );

				setEnabled( false );
				break;
		}
	}

	/**
	 * Overrides the default isEnabled() method, because the setEnabled()
	 * method does not call the superclass's version.
	 *
	 * @return	<code>true</code>
	 */

	public final boolean isEnabled()
	{	return true;
	}

	protected class MultiButtonPanel extends JPanel
	{
		protected boolean showMovers;
		protected JPanel enclosingPanel;
		protected JPanel optionPanel;
		protected LockableListModel elementModel;
		protected ShowDescriptionList elementList;

		protected JButton [] buttons;
		protected JRadioButton [] movers;

		public MultiButtonPanel( String title, LockableListModel elementModel, boolean showMovers )
		{
			existingPanels.add( new WeakReference( this ) );
			this.showMovers = showMovers;
			this.optionPanel = new JPanel();

			this.elementModel = elementModel;
			this.elementList = new ShowDescriptionList( elementModel );

			enclosingPanel = new JPanel( new BorderLayout( 10, 10 ) );
			enclosingPanel.add( JComponentUtilities.createLabel( title, JLabel.CENTER, Color.black, Color.white ), BorderLayout.NORTH );
			enclosingPanel.add( new JScrollPane( elementList, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER ), BorderLayout.CENTER );

			setLayout( new CardLayout( 10, 0 ) );
			add( enclosingPanel, "" );
		}

		public void setButtons( String [] buttonLabels, ActionListener [] buttonListeners )
		{
			JPanel buttonPanel = new JPanel();
			buttons = new JButton[ buttonLabels.length ];

			for ( int i = 0; i < buttonLabels.length; ++i )
			{
				buttons[i] = new JButton( buttonLabels[i] );
				buttons[i].addActionListener( buttonListeners[i] );
				buttonPanel.add( buttons[i] );
			}

			JPanel moverPanel = new JPanel();

			movers = new JRadioButton[4];
			movers[0] = new JRadioButton( "Move all", true );
			movers[1] = new JRadioButton( "Move all but one" );
			movers[2] = new JRadioButton( "Move multiple" );
			movers[3] = new JRadioButton( "Move exactly one" );

			ButtonGroup moverGroup = new ButtonGroup();
			for ( int i = 0; i < 4; ++i )
			{
				moverGroup.add( movers[i] );
				if ( showMovers )
					moverPanel.add( movers[i] );
			}

			JPanel northPanel = new JPanel( new BorderLayout() );
			northPanel.add( buttonPanel, BorderLayout.SOUTH );
			northPanel.add( moverPanel, BorderLayout.CENTER );
			northPanel.add( optionPanel, BorderLayout.NORTH );

			enclosingPanel.add( northPanel, BorderLayout.NORTH );
		}

		public void setEnabled( boolean isEnabled )
		{
			elementList.setEnabled( isEnabled );
			for ( int i = 0; i < buttons.length; ++i )
				buttons[i].setEnabled( isEnabled );

			for ( int i = 0; i < movers.length; ++i )
				movers[i].setEnabled( isEnabled );
		}

		protected Object [] getDesiredItems( String message )
		{
			Object [] items = elementList.getSelectedValues();
			if ( items.length == 0 )
				return null;

			int neededSize = items.length;
			AdventureResult currentItem;

			for ( int i = 0; i < items.length; ++i )
			{
				currentItem = (AdventureResult) items[i];

				int quantity = movers[0].isSelected() ? currentItem.getCount() : movers[1].isSelected() ?
					currentItem.getCount() - 1 : movers[2].isSelected() ? getQuantity( message + " " + currentItem.getName() + "...", currentItem.getCount() ) : 1;

				// If the user manually enters zero, return from
				// this, since they probably wanted to cancel.

				if ( quantity == 0 && movers[2].isSelected() )
					return null;

				// Otherwise, if it was not a manual entry, then reset
				// the entry to null so that it can be re-processed.

				if ( quantity == 0 )
				{
					items[i] = null;
					--neededSize;
				}
				else
				{
					items[i] = currentItem.getInstance( quantity );
				}
			}

			// If none of the array entries were nulled,
			// then return the array as-is.

			if ( neededSize == items.length )
				return items;

			// Otherwise, shrink the array which will be
			// returned so that it removes any nulled values.

			Object [] desiredItems = new Object[ neededSize ];
			neededSize = 0;

			for ( int i = 0; i < items.length; ++i )
				if ( items[i] != null )
					desiredItems[ neededSize++ ] = items[i];

			return desiredItems;
		}
	}

	/**
	 * In order to keep the user interface from freezing (or at least
	 * appearing to freeze), this internal class is used to process
	 * the request for viewing frames.
	 */

	protected class DisplayFrameButton extends JButton implements ActionListener
	{
		private Class frameClass;
		private CreateFrameRunnable displayer;

		public DisplayFrameButton( String text, Class frameClass )
		{
			super( text );

			addActionListener( this );
			this.frameClass = frameClass;

			Object [] parameters = new Object[0];

			this.displayer = new CreateFrameRunnable( frameClass, parameters );
		}

		public DisplayFrameButton( String tooltip, String icon, Class frameClass )
		{
			super( JComponentUtilities.getSharedImage( icon ) );
			JComponentUtilities.setComponentSize( this, 32, 32 );
			setToolTipText( tooltip );

			addActionListener( this );
			this.frameClass = frameClass;

			Object [] parameters = new Object[0];

			this.displayer = new CreateFrameRunnable( frameClass, parameters );
		}

		public void actionPerformed( ActionEvent e )
		{	SwingUtilities.invokeLater( displayer );
		}
	}

	/**
	 * Action listener responsible for handling links clicked
	 * inside of a <code>JEditorPane</code>.
	 */

	protected class KoLHyperlinkAdapter extends HyperlinkAdapter
	{
		protected void handleInternalLink( String location )
		{
			if ( location.startsWith( "desc" ) || location.startsWith( "doc" ) || location.startsWith( "searchp" ) )
			{
				// Certain requests should open in a new window.
				// These include description data, documentation
				// and player searches.

				openRequestFrame( location );
			}
			else if ( location.equals( "lchat.php" ) )
			{
				KoLMessenger.initialize();
			}
			else if ( KoLFrame.this instanceof RequestFrame )
			{
				// If this is a request frame, make sure that
				// you minimize the number of open windows by
				// making an attempt to refresh.

				((RequestFrame)KoLFrame.this).refresh( RequestEditorKit.extractRequest( location ) );
			}
			else
			{
				// Otherwise, if this isn't a request frame,
				// open up a new request frame in order to
				// display the appropriate data.

				openRequestFrame( location );
			}
		}
	}

	/**
	 * An internal class which opens a new <code>RequestFrame</code>
	 * to the given frame whenever an action event is triggered.
	 */

	protected class MiniBrowserButton extends JButton implements ActionListener
	{
		private String location;
		private boolean useSavedRequest;

		public MiniBrowserButton()
		{
			super( JComponentUtilities.getSharedImage( "browser.gif" ) );
			JComponentUtilities.setComponentSize( this, 32, 32 );
			addActionListener( this );
			setToolTipText( "Mini-Browser" );
		}

		public void actionPerformed( ActionEvent e )
		{
			StaticEntity.getClient().setCurrentRequest( null );
			openRequestFrame( "main.php" );
		}
	}

	/**
	 * Internal class used to invoke the given no-parameter
	 * method on the given object.  This is used whenever
	 * there is the need to invoke a method and the creation
	 * of an additional class is unnecessary.
	 */

	protected class InvocationButton extends JButton implements ActionListener, Runnable
	{
		private Object object;
		private Method method;

		public InvocationButton( String text, Object object, String methodName )
		{
			this( text, object == null ? null : object.getClass(), methodName );
			this.object = object;
		}

		public InvocationButton( String text, Class c, String methodName )
		{
			super( text );
			this.object = c;

			completeConstruction( c, methodName );
		}

		public InvocationButton( String tooltip, String icon, Object object, String methodName )
		{
			this( tooltip, icon, object == null ? null : object.getClass(), methodName );
			this.object = object;
		}

		public InvocationButton( String tooltip, String icon, Class c, String methodName )
		{
			super( JComponentUtilities.getSharedImage( icon ) );
			JComponentUtilities.setComponentSize( this, 32, 32 );

			this.object = c;
			setToolTipText( tooltip );
			completeConstruction( c, methodName );
		}

		private void completeConstruction( Class c, String methodName )
		{
			addActionListener( this );

			try
			{
				this.method = c.getMethod( methodName, NOPARAMS );
			}
			catch ( Exception e )
			{
				e.printStackTrace( KoLmafia.getLogStream() );
				e.printStackTrace();
			}
		}

		public void actionPerformed( ActionEvent e )
		{	(new RequestThread( this )).start();
		}

		public void run()
		{
			try
			{
				if ( method != null )
					method.invoke( object, null );
			}
			catch ( Exception e )
			{
				e.printStackTrace( KoLmafia.getLogStream() );
				e.printStackTrace();
			}
		}
	}

	/**
	 * A method used to open a new <code>RequestFrame</code> which displays
	 * the given location, relative to the KoL home directory for the current
	 * session.  This should be called whenever <code>RequestFrame</code>s
	 * need to be created in order to keep code modular.
	 */

	public void openRequestFrame( String location )
	{	openRequestFrame( RequestEditorKit.extractRequest( location ) );
	}

	public void openRequestFrame( KoLRequest request )
	{
		Object [] parameters;
		String location = request.getURLString();

		if ( location.startsWith( "search" ) ||
		     location.startsWith( "desc" ) ||
		     location.startsWith( "static" ) ||
		     location.startsWith( "showplayer" ) )
		{
			parameters = new Object[2];
			parameters[0] = this instanceof RequestFrame ? this : null;
			parameters[1] = request;
		}
		else if ( this instanceof RequestFrame )
		{
			((RequestFrame)this).refresh( request, true );
			return;
		}
		else if ( request.getURLString().equals( "main.php" ) )
		{
			parameters = new Object[1];
			parameters[0] = request;
		}
		else
		{
			// Search for an existing true request frame to open
			// the URL.

			KoLFrame [] frames = new KoLFrame[ existingFrames.size() ];
			existingFrames.toArray( frames );

			for ( int i = frames.length - 1; i >= 0; --i )
			{
				if ( frames[i].getClass() == RequestFrame.class )
				{
					frames[i].requestFocus();
					((RequestFrame)frames[i]).refresh( request );
					return;
				}
			}

			parameters = new Object[1];
			parameters[0] = request;
		}

		SwingUtilities.invokeLater( new CreateFrameRunnable( RequestFrame.class, parameters ) );
	}

	/**
	 * An internal class used to handle requests to open a new frame
	 * using a local panel inside of the adventure frame.
	 */

	protected class KoLPanelFrameButton extends JButton implements ActionListener
	{
		private CreateFrameRunnable creator;

		public KoLPanelFrameButton( String tooltip, String icon, ActionPanel panel )
		{
			super( JComponentUtilities.getSharedImage( icon ) );
			JComponentUtilities.setComponentSize( this, 32, 32 );
			setToolTipText( tooltip );
			addActionListener( this );

			Object [] parameters = new Object[3];
			parameters[0] = StaticEntity.getClient();
			parameters[1] = tooltip;
			parameters[2] = panel;

			creator = new CreateFrameRunnable( KoLPanelFrame.class, parameters );
		}

		public void actionPerformed( ActionEvent e )
		{	creator.run();
		}
	}

	/**
	 * This internal class is used to process the request for selecting
	 * a script using the file dialog.
	 */

	protected class ScriptSelectPanel extends JPanel implements ActionListener
	{
		private JTextField scriptField;

		public ScriptSelectPanel( JTextField scriptField )
		{
			setLayout( new BorderLayout( 0, 0 ) );

			add( scriptField, BorderLayout.CENTER );
			JButton scriptButton = new JButton( "..." );

			JComponentUtilities.setComponentSize( scriptButton, 20, 20 );
			scriptButton.addActionListener( this );
			add( scriptButton, BorderLayout.EAST );

			this.scriptField = scriptField;
		}

		public void actionPerformed( ActionEvent e )
		{
			JFileChooser chooser = new JFileChooser( SCRIPT_DIRECTORY.getAbsolutePath() );
			int returnVal = chooser.showOpenDialog( KoLFrame.this );

			if ( chooser.getSelectedFile() == null )
				return;

			scriptField.setText( chooser.getSelectedFile().getAbsolutePath() );
		}
	}

	protected class RequestButton extends JButton implements ActionListener
	{
		private KoLRequest request;

		public RequestButton( String title, KoLRequest request )
		{
			super( title );
			this.request = request;
			addActionListener( this );
		}

		public RequestButton( String title, String icon, KoLRequest request )
		{
			super( JComponentUtilities.getSharedImage( icon ) );
			setToolTipText( title );
			this.request = request;
			addActionListener( this );
		}

		public void actionPerformed( ActionEvent e )
		{	(new RequestThread( request )).start();
		}
	}

	/**
	 * Utility method which retrieves an integer value from the given
	 * field.  In the event that the field does not contain an integer
	 * value, the number "0" is returned instead.
	 */

	protected static final int getValue( JTextField field )
	{	return getValue( field, 0 );
	}

	/**
	 * Utility method which retrieves an integer value from the given
	 * field.  In the event that the field does not contain an integer
	 * value, the default value provided will be returned instead.
	 */

	protected static final int getValue( JTextField field, int defaultValue )
	{
		try
		{
			String currentValue = field.getText();

			if ( currentValue == null || currentValue.length() == 0 )
				return defaultValue;

			if ( currentValue.equals( "*" ) )
				return defaultValue;

			return df.parse( field.getText().trim() ).intValue();
		}
		catch ( Exception e )
		{
			e.printStackTrace( KoLmafia.getLogStream() );
			e.printStackTrace();

			return 0;
		}
	}

	protected static final int getQuantity( String title, int maximumValue, int defaultValue )
	{
		// Check parameters; avoid programmer error.
		if ( defaultValue > maximumValue )
			return 0;

		if ( maximumValue == 1 && maximumValue == defaultValue )
			return 1;

		try
		{
			String currentValue = JOptionPane.showInputDialog( title, df.format( defaultValue ) );
			if ( currentValue == null )
				return 0;

			if ( currentValue.equals( "*" ) )
				return maximumValue;

			int desiredValue = df.parse( currentValue ).intValue();
			return Math.max( 0, Math.min( desiredValue, maximumValue ) );
		}
		catch ( Exception e )
		{
			e.printStackTrace( KoLmafia.getLogStream() );
			e.printStackTrace();

			return 0;
		}
	}

	protected static final int getQuantity( String title, int maximumValue )
	{	return getQuantity( title, maximumValue, maximumValue );
	}

	protected final void setProperty( String name, String value )
	{	StaticEntity.setProperty( name, value );
	}

	protected final String getProperty( String name )
	{	return StaticEntity.getProperty( name );
	}

	protected class FilterCheckBox extends JCheckBox implements ActionListener
	{
		private boolean isTradeable;
		private JCheckBox [] filters;
		private ShowDescriptionList elementList;

		public FilterCheckBox( JCheckBox [] filters, ShowDescriptionList elementList, String label, boolean isSelected )
		{	this( filters, elementList, false, label, isSelected );
		}

		public FilterCheckBox( JCheckBox [] filters, ShowDescriptionList elementList, boolean isTradeable, String label, boolean isSelected )
		{
			super( label, isSelected );
			addActionListener( this );

			this.isTradeable = isTradeable;
			this.filters = filters;
			this.elementList = elementList;
		}

		public void actionPerformed( ActionEvent e )
		{
			if ( isTradeable )
			{
				if ( !filters[0].isSelected() && !filters[1].isSelected() && !filters[2].isSelected() )
				{
					filters[3].setEnabled( false );
					filters[4].setEnabled( false );
				}
				else
				{
					filters[3].setEnabled( true );
					filters[4].setEnabled( true );
				}

				elementList.setCellRenderer(
					AdventureResult.getAutoSellCellRenderer( filters[0].isSelected(), filters[1].isSelected(), filters[2].isSelected(), filters[3].isSelected(), filters[4].isSelected() ) );
			}
			else
			{
				elementList.setCellRenderer(
					AdventureResult.getConsumableCellRenderer( filters[0].isSelected(), filters[1].isSelected(), filters[2].isSelected() ) );
			}

			elementList.validate();
		}
	}

	private class UnanimatedLabel extends JLabel
	{
		public UnanimatedLabel()
		{	super( " ", null, CENTER );
		}

		public boolean imageUpdate( Image img, int infoflags, int x, int y, int width, int height )
		{
			if ( infoflags == FRAMEBITS )
				return true;

			super.imageUpdate( img, infoflags, x, y, width, height );
			return true;
		}
	}

	protected abstract class ListeningRunnable implements ActionListener, Runnable
	{
		public void actionPerformed( ActionEvent e )
		{	(new RequestThread( this )).start();
		}
	}

	protected void processWindowEvent( WindowEvent e )
	{
		if ( e.getID() == WindowEvent.WINDOW_CLOSING )
		{
			Point p = getLocation();
			KoLSettings settings = GLOBAL_SETTINGS.getProperty( "windowPositions" ).equals( "1" ) ? GLOBAL_SETTINGS : StaticEntity.getSettings();
			settings.setProperty( frameName, ((int)p.getX()) + "," + ((int)p.getY()) );
		}

		super.processWindowEvent( e );
	}
}
