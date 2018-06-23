/*******************************************************************************
 * Copyright (c) 2009, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.ui.tests.util;

import java.io.IOException;
import java.io.Writer;

public class PDEPerfTesterUtil {

    private String fTag;

    private long fDuration;

    private long fStart;

    private long fEnd;

    private long fIteration;

    private long fTotalDuration;

    private long fAverageDuration;

    private static final long F_SECOND_IN_MS = 1000;

    private static final long F_MINUTE_IN_MS = 60000;

    private static final long F_HOUR_IN_MS = 3600000;

    /**
	 * @param tag
	 */
    public  PDEPerfTesterUtil(String tag) {
        fTag = tag;
        reset();
    }

    /**
	 *
	 */
    public void reset() {
        fDuration = 0;
        fStart = 0;
        fEnd = 0;
        fIteration = 0;
        fTotalDuration = 0;
        fAverageDuration = 0;
    }

    /**
	 *
	 */
    public void start() {
        fIteration++;
        fStart = System.currentTimeMillis();
    }

    /**
	 *
	 */
    public void stop() {
        fEnd = System.currentTimeMillis();
        calculateDuration();
    }

    /**
	 *
	 */
    private void calculateDuration() {
        fDuration = (fEnd - fStart);
        fTotalDuration = fTotalDuration + fDuration;
        if (fIteration > 0) {
            fAverageDuration = fTotalDuration / fIteration;
        }
    }

    /**
	 * @param duration
	 * @return
	 */
    private String formatDuration(long duration) {
        String output = null;
        int hours = 0;
        int minutes = 0;
        int seconds = 0;
        long milliseconds = 0;
        long timeDifference = duration;
        hours = (int) Math.rint(timeDifference / F_HOUR_IN_MS);
        if (hours > 0) {
            timeDifference = timeDifference - (hours * F_HOUR_IN_MS);
        }
        minutes = (int) Math.rint(timeDifference / F_MINUTE_IN_MS);
        if (minutes > 0) {
            timeDifference = timeDifference - (minutes * F_MINUTE_IN_MS);
        }
        seconds = (int) Math.rint(timeDifference / F_SECOND_IN_MS);
        if (seconds > 0) {
            timeDifference = timeDifference - (seconds * F_SECOND_IN_MS);
        }
        milliseconds = timeDifference;
        output = //$NON-NLS-1$
        hours + " h " + minutes + //$NON-NLS-1$
        " m " + seconds + //$NON-NLS-1$
        " s " + //$NON-NLS-1$
        milliseconds + //$NON-NLS-1$
        " ms";
        return output;
    }

    /**
	 * @param writer
	 */
    public void printDuration(Writer writer) {
        String output = //$NON-NLS-1$
        formatTag() + "(" + fIteration + //$NON-NLS-1$
        "): " + //$NON-NLS-1$
        formatDuration(fDuration) + //$NON-NLS-1$
        "\n";
        try {
            writer.write(output);
            writer.flush();
        } catch (IOException e) {
        }
    }

    /**
	 * @param writer
	 */
    public void printTotalDuration(Writer writer) {
        String output = //$NON-NLS-1$
        formatTag() + "(TOTAL " + fIteration + //$NON-NLS-1$
        "): " + //$NON-NLS-1$
        formatDuration(fTotalDuration) + //$NON-NLS-1$
        "\n";
        try {
            writer.write(output);
            writer.flush();
        } catch (IOException e) {
        }
    }

    /**
	 * @param writer
	 */
    public void printAverageDuration(Writer writer) {
        String output = //$NON-NLS-1$
        formatTag() + "(AVERAGE " + fIteration + //$NON-NLS-1$
        "): " + //$NON-NLS-1$
        formatDuration(fAverageDuration) + //$NON-NLS-1$
        "\n";
        try {
            writer.write(output);
            writer.flush();
        } catch (IOException e) {
        }
    }

    /**
	 * @return
	 */
    private String formatTag() {
        //$NON-NLS-1$ //$NON-NLS-2$
        return "[" + fTag + "]: ";
    }
}
