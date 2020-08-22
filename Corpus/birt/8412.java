/***********************************************************************
 * Copyright (c) 2004 Actuate Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Actuate Corporation - initial API and implementation
 ***********************************************************************/
package org.eclipse.birt.report.engine.layout.pdf.hyphen;

public class Word
{
    protected int start;
    protected int end;
    protected String text;
    public Word(String text, int start, int end)
    {
        this.text = text;
        this.start = start;
        this.end = end;
    }
    
    public int getStart()
    {
        return this.start;
    }
    
    public int getEnd()
    {
        return this.end;
    }
    
    public String getValue()
    {
        return text.substring(start, end);
    }
    
    public int getLength()
    {
    	return end-start;
    }
    
    public String toString()
    {
        StringBuffer str = new StringBuffer();
        str.append("value: [" + getValue() + "]"); //$NON-NLS-1$ //$NON-NLS-2$
        str.append(" StartIndex:" + start); //$NON-NLS-1$
        str.append(" EndIndex:" + end ); //$NON-NLS-1$
        return str.toString();
    }
}
