/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Benjamin Muskalla <b.muskalla@gmx.net> - [spell checking][implementation] PersistentSpellDictionary closes wrong stream - https://bugs.eclipse.org/bugs/show_bug.cgi?id=236421
 *******************************************************************************/
package org.eclipse.jdt.internal.ui.text.spelling.engine;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import org.eclipse.jdt.internal.ui.JavaPlugin;

/**
 * Persistent modifiable word-list based dictionary.
 *
 * @since 3.0
 */
public class PersistentSpellDictionary extends AbstractSpellDictionary {

    /** The word list location */
    private final URL fLocation;

    /**
	 * Creates a new persistent spell dictionary.
	 *
	 * @param url the URL of the word list for this dictionary
	 */
    public  PersistentSpellDictionary(final URL url) {
        fLocation = url;
    }

    @Override
    public boolean acceptsWords() {
        return true;
    }

    @Override
    public void addWord(final String word) {
        if (isCorrect(word))
            return;
        FileOutputStream fileStream = null;
        try {
            Charset charset = Charset.forName(getEncoding());
            //$NON-NLS-1$
            ByteBuffer byteBuffer = charset.encode(word + "\n");
            int size = byteBuffer.limit();
            final byte[] byteArray;
            if (byteBuffer.hasArray())
                byteArray = byteBuffer.array();
            else {
                byteArray = new byte[size];
                byteBuffer.get(byteArray);
            }
            fileStream = new FileOutputStream(fLocation.getPath(), true);
            // Encoding UTF-16 charset writes a BOM. In which case we need to cut it away if the file isn't empty
            int bomCutSize = 0;
            if (//$NON-NLS-1$
            !isEmpty() && "UTF-16".equals(charset.name()))
                bomCutSize = 2;
            fileStream.write(byteArray, bomCutSize, size - bomCutSize);
        } catch (IOException exception) {
            JavaPlugin.log(exception);
            return;
        } finally {
            try {
                if (fileStream != null)
                    fileStream.close();
            } catch (IOException e) {
            }
        }
        hashWord(word);
    }

    @Override
    protected final URL getURL() {
        return fLocation;
    }
}
