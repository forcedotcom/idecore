/*******************************************************************************
 * Copyright (c) 2014 Salesforce.com, inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Salesforce.com, inc. - initial API and implementation
 ******************************************************************************/
package com.salesforce.ide.ui.views.log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

/**
 * Tails given file
 * 
 * @author cwall
 * 
 */
public class TailInputStream extends InputStream {

    private final RandomAccessFile randomAccessFile;
    private final long tailLen;

    public TailInputStream(File logFile, long maxLength) throws IOException {
        super();
        tailLen = maxLength;
        randomAccessFile = new RandomAccessFile(logFile, "r");
        skipHead(logFile);
    }

    private void skipHead(File file) throws IOException {
        if (file.length() > tailLen) {
            randomAccessFile.seek(file.length() - tailLen);
            int c = read();
            while (c != '\n' && c != 'r' && c != -1) {
                c = read();
            }

        }
    }

    @Override
    public int read() throws IOException {
        byte[] b = new byte[1];
        int len = randomAccessFile.read(b, 0, 1);
        if (len < 0) {
            return len;
        }
        return b[0];
    }

    @Override
    public int read(byte[] b) throws IOException {
        return randomAccessFile.read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return randomAccessFile.read(b, off, len);
    }

    @Override
    public void close() throws IOException {
        randomAccessFile.close();
    }

}
