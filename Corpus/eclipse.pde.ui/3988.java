/*******************************************************************************
 *  Copyright (c) 2006, 2016 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.ui.commands;

import java.util.*;
import org.eclipse.core.commands.Command;
import org.eclipse.core.runtime.ListenerList;

public class TagManager {

    private Hashtable<Command, String> fCommandToTags = new Hashtable();

    private ListenerList<Listener> fListeners = new ListenerList();

    public void update(Command command, String tags) {
        if (//$NON-NLS-1$
        (tags == null) || ("".equals(tags))) {
            fCommandToTags.remove(command);
        } else {
            fCommandToTags.put(command, tags);
        }
        fireTagManagerChanged();
    }

    private boolean hasTag(String tags, String tag) {
        //$NON-NLS-1$
        String[] tagArray = tags.split(",");
        for (int i = 0; i < tagArray.length; i++) {
            String trimTag = tagArray[i].trim();
            if (tag.equalsIgnoreCase(trimTag))
                return true;
        }
        return false;
    }

    public String[] getTags() {
        HashSet<String> tagSet = new HashSet();
        for (Iterator<String> i = fCommandToTags.values().iterator(); i.hasNext(); ) {
            String tags = i.next();
            //$NON-NLS-1$
            String[] tagArray = tags.split(",");
            for (int j = 0; j < tagArray.length; j++) {
                String trimTag = tagArray[j].trim();
                tagSet.add(trimTag);
            }
        }
        return tagSet.toArray(new String[tagSet.size()]);
    }

    public String getTags(Command command) {
        String tags = fCommandToTags.get(command);
        if (tags == null)
            //$NON-NLS-1$
            return "";
        return tags;
    }

    public Command[] getCommands(String tag) {
        ArrayList<Command> list = new ArrayList();
        for (Iterator<Command> i = fCommandToTags.keySet().iterator(); i.hasNext(); ) {
            Command command = i.next();
            String tags = fCommandToTags.get(command);
            if (hasTag(tags, tag)) {
                list.add(command);
            }
        }
        return list.toArray(new Command[list.size()]);
    }

    public interface Listener {

        void tagManagerChanged();
    }

    public void addListener(Listener listener) {
        fListeners.add(listener);
    }

    public void removeListener(Listener listener) {
        fListeners.remove(listener);
    }

    private void fireTagManagerChanged() {
        for (Listener listener : fListeners) {
            listener.tagManagerChanged();
        }
    }
}
