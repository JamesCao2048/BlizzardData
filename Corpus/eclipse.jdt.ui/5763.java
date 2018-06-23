/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.text.tests.performance;

import org.eclipse.test.performance.PerformanceMeter;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;

public abstract class UndoEditorTest extends TextPerformanceTestCase {

    private PerformanceMeter fPerformanceMeter;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        fPerformanceMeter = createMeter();
        EditorTestHelper.runEventQueue();
    }

    protected abstract PerformanceMeter createMeter();

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        EditorTestHelper.closeAllEditors();
        fPerformanceMeter.dispose();
    }

    protected void measureUndo(IFile file) throws PartInitException {
        AbstractTextEditor editor = (AbstractTextEditor) EditorTestHelper.openInEditor(file, true);
        // TODO: remove when undo does no longer trigger timing issue
        editor.showChangeInformation(false);
        IAction selectAll = editor.getAction(ITextEditorActionConstants.SELECT_ALL);
        IAction shiftRight = editor.getAction(ITextEditorActionConstants.SHIFT_RIGHT);
        IAction undo = editor.getAction(ITextEditorActionConstants.UNDO);
        int warmUpRuns = getWarmUpRuns();
        int measuredRuns = getMeasuredRuns();
        for (int i = 0; i < warmUpRuns + measuredRuns; i++) {
            runAction(selectAll);
            runAction(shiftRight);
            sleep(5000);
            EditorTestHelper.runEventQueue();
            if (i >= warmUpRuns)
                fPerformanceMeter.start();
            runAction(undo);
            if (i >= warmUpRuns)
                fPerformanceMeter.stop();
            // NOTE: runnables posted from other threads, while the main thread waits here, are not measured at all
            sleep(5000);
        }
        fPerformanceMeter.commit();
        assertPerformance(fPerformanceMeter);
    }

    private void runAction(IAction action) {
        action.run();
        EditorTestHelper.runEventQueue();
    }

    private synchronized void sleep(int time) {
        try {
            wait(time);
        } catch (InterruptedException e) {
        }
    }
}
