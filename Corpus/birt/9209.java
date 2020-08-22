/*******************************************************************************
 * Copyright (c) 2013 Actuate Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * Contributors:
 *  Actuate Corporation  - initial API and implementation
 *******************************************************************************/

package org.eclipse.birt.report.engine.emitter.pptx.writer;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.birt.report.engine.ooxml.IPart;
import org.eclipse.birt.report.engine.ooxml.ImageManager;
import org.eclipse.birt.report.engine.ooxml.Package;
import org.eclipse.birt.report.engine.ooxml.constants.ContentTypes;
import org.eclipse.birt.report.engine.ooxml.constants.NameSpaces;
import org.eclipse.birt.report.engine.ooxml.constants.RelationshipTypes;
import org.eclipse.birt.report.engine.ooxml.util.OOXmlUtil;


public class Presentation extends Component
{
	private static final String TAG_CY = "cy";

	private static final String TAG_CX = "cx";

	private static final String TAG_NOTES_SZ = "p:notesSz";

	private static final String TAG_SLIDE_SZ = "p:sldSz";

	private static final String TAG_SLIDE_ID = "p:sldId";

	private static final String TAG_SLIDE_ID_LIST = "p:sldIdLst";

	private static final String TAG_RELATIONSHIP_ID = "r:id";

	private static final String TAG_ID = "id";

	private static final String TAG_SLIDE_MASTER_ID = "p:sldMasterId";

	private static final String TAG_SLIDE_MASTER_ID_LIST = "p:sldMasterIdLst";

	private int width = 0, height = 0;

	private Package pkg;

	private SlideMaster slideMaster;
	private List<Slide> slides = new ArrayList<Slide>( );
	private String author, title, description, subject;
	
	public Presentation( OutputStream out, String tempFileDir,
			int compressionMode )
	{
		pkg = Package.createInstance( out, tempFileDir, compressionMode );
		String uri = "ppt/presentation.xml";
		String type = ContentTypes.PRESENTATIONML;
		String relationshipType = RelationshipTypes.DOCUMENT;
		this.part = pkg.getPart( uri, type, relationshipType );
		pkg.setExtensionData( new ImageManager() );
		try
		{
			writer = part.getCacheWriter( );
			initialize( );
			Theme theme = new Theme( part );
			createSlideMaster( );
			slideMaster.referTo( theme );
		}
		catch ( IOException e )
		{
			e.printStackTrace( );
		}
	}

	private void createSlideMaster( ) throws IOException
	{
		slideMaster = new SlideMaster( this );
		writer.openTag( TAG_SLIDE_MASTER_ID_LIST );
		writer.openTag( TAG_SLIDE_MASTER_ID );
		writer.attribute( TAG_ID, "2147483648" );
		writer.attribute( TAG_RELATIONSHIP_ID, slideMaster.getPart( )
				.getRelationshipId( ) );
		writer.closeTag( TAG_SLIDE_MASTER_ID );
		writer.closeTag( TAG_SLIDE_MASTER_ID_LIST );

	}

	public void initialize( )
	{
		writer.startWriter( );
		writer.openTag( "p:presentation" );
		writer.nameSpace( "a", NameSpaces.DRAWINGML );
		writer.nameSpace( "r", NameSpaces.RELATIONSHIPS );
		writer.nameSpace( "p", NameSpaces.PRESENTATIONML );

	}

	public void setSize( int width, int height )
	{
		this.width = width;
		this.height = height;
	}

	public Slide createSlide( int pageWidth, int pageHeight, Color bgColor )
			throws IOException
	{
		if ( pageWidth > width )
		{
			width = pageWidth;
		}
		if ( pageHeight > height )
		{
			height = pageHeight;
		}
		return createSlide( bgColor );
	}

	public Slide createSlide( Color bgColor ) throws IOException
	{
		int slideIndex = slides.size( ) + 1;
		Slide slide = new Slide( this, slideIndex, bgColor );
		slides.add( slide );
		return slide;
	}

	private void outputSlides( )
	{
		writer.openTag( TAG_SLIDE_ID_LIST );
		for ( Slide slide : slides )
		{
			writer.openTag( TAG_SLIDE_ID );
			writer.attribute( TAG_ID, slide.getSlideId( ) );
			writer.attribute( TAG_RELATIONSHIP_ID, slide.getPart( )
					.getRelationshipId( ) );
			writer.closeTag( TAG_SLIDE_ID );
		}
		writer.closeTag( TAG_SLIDE_ID_LIST );
	}

	public void close( ) throws IOException
	{
		new Core( this, author, title, description, subject );
		slideMaster.close( );
		outputSlides( );
		writer.openTag( TAG_SLIDE_SZ );
		// Set default page size to A4.
		if ( width == 0 )
		{
			width = 612;
			height = 792;
		}
		long convertedWidth = OOXmlUtil.convertPointerToEmus( width );
		long convertedHeight = OOXmlUtil.convertPointerToEmus( height );
		writer.attribute( TAG_CX, convertedWidth );
		writer.attribute( TAG_CY, convertedHeight );
		writer.closeTag( TAG_SLIDE_SZ );

		writer.openTag( TAG_NOTES_SZ );
		writer.attribute( TAG_CX, convertedHeight );
		writer.attribute( TAG_CY, convertedWidth );
		writer.closeTag( TAG_NOTES_SZ );
		writer.closeTag( "p:presentation" );
		
		copyPropertyFile( "viewProps" );
		copyPropertyFile( "tableStyles" );
		copyPropertyFile( "presProps" );
	    
		writer.close( );
		pkg.close( );
	}

	public void copyPropertyFile( String propFile ) throws IOException
	{
		String url = "ppt/" + propFile + ".xml";
		String type = "application/vnd.openxmlformats-officedocument.presentationml."
                  + propFile + "+xml";
		IPart part = pkg.getPart( url, type, null );
		copyPartContent( propFile + ".xml", part );
	}

	private void copyPartContent(String file, IPart part) throws IOException
	{
		InputStream is = this.getClass( ).getResourceAsStream( file );
		OutputStream os = part.getOutputStream( );
	    // Transfer bytes from in to out
	    byte[] buf = new byte[1024];
	    int len;
	    while ((len = is.read(buf)) > 0) {
	        os.write(buf, 0, len);
	    }
	    is.close();
	    os.close();
	}
	
	public Package getPackage( )
	{
		return pkg;
	}

	public SlideMaster getSlideMaster( )
	{
		return slideMaster;
	}
	
	public void setAuthor( String author )
	{
		this.author = author;
	}

	public void setDescription( String description )
	{
		this.description = description;
	}

	public void setTitle( String title )
	{
		this.title = title;
	}

	public void setSubject( String subject )
	{
		this.subject = subject;
	}
}