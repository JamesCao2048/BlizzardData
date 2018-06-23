/*******************************************************************************
 * Copyright (c) 2008, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 * Remy Chi Jian Suen <remy.suen@gmail.com> - Bug 243347 TarFile should not throw NPE in finalize()
 *******************************************************************************/
package org.eclipse.pde.api.tools.internal.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.GZIPInputStream;

/**
 * Reads a .tar or .tar.gz archive file, providing an index enumeration and
 * allows for accessing an InputStream for arbitrary files in the archive.
 */
public class TarFile {

    private static class TarInputStream extends FilterInputStream {

        private int nextEntry = 0;

        private int nextEOF = 0;

        private int filepos = 0;

        private int bytesread = 0;

        private TarEntry firstEntry = null;

        private String longLinkName = null;

        /**
		 * Creates a new tar input stream on the given input stream.
		 *
		 * @param in input stream
		 * @throws TarException
		 * @throws IOException
		 */
        public  TarInputStream(InputStream in) throws TarException, IOException {
            super(in);
            // Read in the first TarEntry to make sure
            // the input is a valid tar file stream.
            firstEntry = getNextEntry();
        }

        /**
		 * Create a new tar input stream, skipping ahead to the given entry in
		 * the file.
		 *
		 * @param in input stream
		 * @param entry skips to this entry in the file
		 * @throws TarException
		 * @throws IOException
		 */
         TarInputStream(InputStream in, TarEntry entry) throws TarException, IOException {
            super(in);
            skipToEntry(entry);
        }

        /**
		 * The checksum of a tar file header is simply the sum of the bytes in
		 * the header.
		 *
		 * @param header
		 * @return checksum
		 */
        private long headerChecksum(byte[] header) {
            long sum = 0;
            for (int i = 0; i < 512; i++) {
                sum += header[i] & 0xff;
            }
            return sum;
        }

        /**
		 * Skips ahead to the position of the given entry in the file.
		 *
		 * @param entry
		 * @returns false if the entry has already been passed
		 * @throws TarException
		 * @throws IOException
		 */
        boolean skipToEntry(TarEntry entry) throws TarException, IOException {
            int bytestoskip = entry.filepos - bytesread;
            if (bytestoskip < 0) {
                return false;
            }
            while (bytestoskip > 0) {
                long ret = in.skip(bytestoskip);
                if (ret < 0) {
                    throw new IOException("early end of stream");
                }
                bytestoskip -= ret;
                bytesread += ret;
            }
            filepos = entry.filepos;
            nextEntry = 0;
            nextEOF = 0;
            // Read next header to seek to file data.
            getNextEntry();
            return true;
        }

        /**
		 * Returns true if the header checksum is correct.
		 *
		 * @param header
		 * @return true if this header has a valid checksum
		 */
        private boolean isValidTarHeader(byte[] header) {
            long fileChecksum, calculatedChecksum;
            int pos, i;
            pos = 148;
            StringBuffer checksumString = new StringBuffer();
            for (i = 0; i < 8; i++) {
                if (header[pos + i] == ' ') {
                    continue;
                }
                if (header[pos + i] == 0 || !Character.isDigit((char) header[pos + i])) {
                    break;
                }
                checksumString.append((char) header[pos + i]);
            }
            if (checksumString.length() == 0) {
                return false;
            }
            if (checksumString.charAt(0) != '0') {
                checksumString.insert(0, '0');
            }
            try {
                fileChecksum = Long.decode(checksumString.toString()).longValue();
            } catch (NumberFormatException exception) {
                return false;
            }
            // Blank out the checksum.
            for (i = 0; i < 8; i++) {
                header[pos + i] = ' ';
            }
            calculatedChecksum = headerChecksum(header);
            return (fileChecksum == calculatedChecksum);
        }

        /**
		 * Returns the next entry in the tar file. Does not handle GNU @LongLink
		 * extensions.
		 *
		 * @return the next entry in the tar file
		 * @throws TarException
		 * @throws IOException
		 */
        TarEntry getNextEntryInternal() throws TarException, IOException {
            byte[] header = new byte[512];
            int pos = 0;
            int i;
            if (firstEntry != null) {
                TarEntry entryReturn = firstEntry;
                firstEntry = null;
                return entryReturn;
            }
            while (nextEntry > 0) {
                long ret = in.skip(nextEntry);
                if (ret < 0) {
                    throw new IOException("early end of stream");
                }
                nextEntry -= ret;
                bytesread += ret;
            }
            int bytestoread = 512;
            while (bytestoread > 0) {
                int ret = super.read(header, 512 - bytestoread, bytestoread);
                if (ret < 0) {
                    throw new IOException("early end of stream");
                }
                bytestoread -= ret;
                bytesread += ret;
            }
            // If we have a header of all zeros, this marks the end of the file.
            if (headerChecksum(header) == 0) {
                // We are at the end of the file.
                if (filepos > 0) {
                    return null;
                }
                // Invalid stream.
                throw new //$NON-NLS-1$
                TarException(//$NON-NLS-1$
                "not in tar format");
            }
            // Validate checksum.
            if (!isValidTarHeader(header)) {
                throw new //$NON-NLS-1$
                TarException(//$NON-NLS-1$
                "not in tar format");
            }
            while (pos < 100 && header[pos] != 0) {
                pos++;
            }
            //$NON-NLS-1$
            String name = new String(header, 0, pos, "UTF8");
            // Prepend the prefix here.
            pos = 345;
            if (header[pos] != 0) {
                while (pos < 500 && header[pos] != 0) {
                    pos++;
                }
                String prefix = new //$NON-NLS-1$
                String(//$NON-NLS-1$
                header, //$NON-NLS-1$
                345, //$NON-NLS-1$
                pos - 345, //$NON-NLS-1$
                "UTF8");
                name = //$NON-NLS-1$
                prefix + "/" + //$NON-NLS-1$
                name;
            }
            TarEntry entry;
            if (longLinkName != null) {
                entry = new TarEntry(longLinkName, filepos);
                longLinkName = null;
            } else {
                entry = new TarEntry(name, filepos);
            }
            if (header[156] != 0) {
                entry.setFileType(header[156]);
            }
            pos = 100;
            StringBuffer mode = new StringBuffer();
            for (i = 0; i < 8; i++) {
                if (header[pos + i] == 0) {
                    break;
                }
                if (header[pos + i] == ' ') {
                    continue;
                }
                mode.append((char) header[pos + i]);
            }
            if (mode.length() > 0 && mode.charAt(0) != '0') {
                mode.insert(0, '0');
            }
            try {
                long fileMode = Long.decode(mode.toString()).longValue();
                entry.setMode(fileMode);
            } catch (NumberFormatException nfe) {
                throw new TarException("Not a valid tar format", nfe);
            }
            pos = 100 + 24;
            StringBuffer size = new StringBuffer();
            for (i = 0; i < 12; i++) {
                if (header[pos + i] == 0) {
                    break;
                }
                if (header[pos + i] == ' ') {
                    continue;
                }
                size.append((char) header[pos + i]);
            }
            if (size.charAt(0) != '0') {
                size.insert(0, '0');
            }
            int fileSize;
            try {
                fileSize = Integer.decode(size.toString()).intValue();
            } catch (NumberFormatException nfe) {
                throw new TarException("Not a valid tar format", nfe);
            }
            entry.setSize(fileSize);
            nextEOF = fileSize;
            if (fileSize % 512 > 0) {
                nextEntry = fileSize + (512 - (fileSize % 512));
            } else {
                nextEntry = fileSize;
            }
            filepos += (nextEntry + 512);
            return entry;
        }

        /**
		 * Moves ahead to the next file in the tar archive and returns a
		 * TarEntry object describing it.
		 *
		 * @return the next entry in the tar file
		 * @throws TarException
		 * @throws IOException
		 */
        public TarEntry getNextEntry() throws TarException, IOException {
            TarEntry entry = getNextEntryInternal();
            if (//$NON-NLS-1$
            entry != null && entry.getName().equals("././@LongLink")) {
                // This is a GNU extension for doing long filenames.
                // We get a file called ././@LongLink which just contains
                // the real pathname.
                byte[] longNameData = new byte[(int) entry.getSize()];
                int lbytesread = 0;
                while (lbytesread < longNameData.length) {
                    int cur = read(longNameData, lbytesread, longNameData.length - lbytesread);
                    if (cur < 0) {
                        throw new IOException("early end of stream");
                    }
                    lbytesread += cur;
                }
                int pos = 0;
                while (pos < longNameData.length && longNameData[pos] != 0) {
                    pos++;
                }
                longLinkName = new //$NON-NLS-1$
                String(//$NON-NLS-1$
                longNameData, //$NON-NLS-1$
                0, //$NON-NLS-1$
                pos, //$NON-NLS-1$
                "UTF8");
                return getNextEntryInternal();
            }
            return entry;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            if (nextEOF == 0) {
                return -1;
            }
            int size = super.read(b, off, (len > nextEOF ? nextEOF : len));
            nextEntry -= size;
            nextEOF -= size;
            bytesread += size;
            return size;
        }

        @Override
        public int read() throws IOException {
            byte[] data = new byte[1];
            int size = read(data, 0, 1);
            if (size < 0) {
                return size;
            }
            return data[0];
        }
    }

    private File file;

    TarInputStream entryEnumerationStream;

    TarEntry curEntry;

    private TarInputStream entryStream;

    private InputStream internalEntryStream;

    /**
	 * Create a new TarFile for the given file.
	 *
	 * @param file
	 * @throws TarException
	 * @throws IOException
	 */
    public  TarFile(File file) throws TarException, IOException {
        this.file = file;
        InputStream in = new FileInputStream(file);
        // First, check if it's a GZIPInputStream.
        try {
            in = new GZIPInputStream(in);
        } catch (IOException e) {
            in.close();
            in = new FileInputStream(file);
        }
        try {
            entryEnumerationStream = new TarInputStream(in);
        } catch (TarException ex) {
            in.close();
            throw ex;
        }
        curEntry = entryEnumerationStream.getNextEntry();
    }

    /**
	 * Close the tar file input stream.
	 *
	 * @throws IOException if the file cannot be successfully closed
	 */
    public void close() throws IOException {
        if (entryEnumerationStream != null) {
            entryEnumerationStream.close();
        }
        if (internalEntryStream != null) {
            internalEntryStream.close();
        }
    }

    /**
	 * Create a new TarFile for the given path name.
	 *
	 * @param filename
	 * @throws TarException
	 * @throws IOException
	 */
    public  TarFile(String filename) throws TarException, IOException {
        this(new File(filename));
    }

    /**
	 * Returns an enumeration cataloguing the tar archive.
	 *
	 * @return enumeration of all files in the archive
	 */
    public Enumeration<?> entries() {
        return new Enumeration<Object>() {

            @Override
            public boolean hasMoreElements() {
                return (curEntry != null);
            }

            @Override
            public Object nextElement() {
                TarEntry oldEntry = curEntry;
                try {
                    curEntry = entryEnumerationStream.getNextEntry();
                } catch (TarException e) {
                    curEntry = null;
                } catch (IOException e) {
                    curEntry = null;
                }
                return oldEntry;
            }
        };
    }

    /**
	 * Returns a new InputStream for the given file in the tar archive.
	 *
	 * @param entry
	 * @return an input stream for the given file
	 * @throws TarException
	 * @throws IOException
	 */
    public InputStream getInputStream(TarEntry entry) throws TarException, IOException {
        if (entryStream == null || !entryStream.skipToEntry(entry)) {
            if (internalEntryStream != null) {
                internalEntryStream.close();
            }
            internalEntryStream = new FileInputStream(file);
            // First, check if it's a GZIPInputStream.
            try {
                internalEntryStream = new GZIPInputStream(internalEntryStream);
            } catch (IOException e) {
                internalEntryStream.close();
                internalEntryStream = new FileInputStream(file);
            }
            entryStream = new TarInputStream(internalEntryStream, entry) {

                @Override
                public void close() {
                // Ignore close() since we want to reuse the stream.
                }
            };
        }
        return entryStream;
    }

    /**
	 * Returns the path name of the file this archive represents.
	 *
	 * @return path
	 */
    public String getName() {
        return file.getPath();
    }

    @Override
    protected void finalize() throws Throwable {
        close();
    }
}
