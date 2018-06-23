/*******************************************************************************
 * Copyright (c) 2008, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.api.tools.ui.internal.wizards;

import java.util.HashSet;
import java.util.Iterator;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.NullChange;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

/**
 * A refactoring to change Javadoc tags during API Tools setup
 *
 * @since 1.0.0
 */
public class ApiToolingSetupRefactoring extends Refactoring {

    /**
	 * The current set of changes
	 */
    private HashSet<Change> fChanges = null;

    @Override
    public RefactoringStatus checkFinalConditions(IProgressMonitor pm) throws CoreException, OperationCanceledException {
        if (fChanges == null || fChanges.size() < 1) {
            return RefactoringStatus.createErrorStatus(WizardMessages.JavadocTagRefactoring_0);
        }
        return RefactoringStatus.create(Status.OK_STATUS);
    }

    @Override
    public RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException, OperationCanceledException {
        return RefactoringStatus.create(Status.OK_STATUS);
    }

    @Override
    public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
        if (fChanges == null) {
            return new NullChange();
        }
        CompositeChange change = new CompositeChange(WizardMessages.JavadocTagRefactoring_1);
        for (Iterator<Change> iter = fChanges.iterator(); iter.hasNext(); ) {
            change.add(iter.next());
        }
        return change;
    }

    public void addChange(Change change) {
        if (fChanges == null) {
            fChanges = new HashSet<Change>();
        }
        fChanges.add(change);
    }

    public void resetRefactoring() {
        if (fChanges != null) {
            fChanges.clear();
            fChanges = null;
        }
    }

    @Override
    public String getName() {
        return WizardMessages.JavadocTagRefactoring_3;
    }
}
