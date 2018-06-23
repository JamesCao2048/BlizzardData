/*******************************************************************************
 *  Copyright (c) 2005, 2012 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *     Les Jones <lesojones@gmail.com> - Bug 208967
 *     Benjamin Cabe <benjamin.cabe@anyware-techc.com> - bug 218618
 *******************************************************************************/
package org.eclipse.pde.internal.core.text.bundle;

import java.io.PrintWriter;
import java.io.Serializable;
import java.util.*;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.osgi.util.ManifestElement;
import org.eclipse.pde.core.IModelChangedEvent;
import org.eclipse.pde.internal.core.ICoreConstants;
import org.eclipse.pde.internal.core.ibundle.IBundleModel;
import org.osgi.framework.Constants;

public class ExportPackageObject extends PackageObject {

    private static final int NEWLINE_LIMIT = 3;

    private static final int NEWLINE_LIMIT_BOTH = 1;

    private static final long serialVersionUID = 1L;

    private TreeMap<String, PackageFriend> fFriends = new TreeMap();

    public  ExportPackageObject(ManifestHeader header, ManifestElement element, String versionAttribute) {
        super(header, element, versionAttribute);
        processFriends();
    }

    public  ExportPackageObject(ManifestHeader header, IPackageFragment fragment, String versionAttribute) {
        super(header, fragment.getElementName(), null, versionAttribute);
    }

    public  ExportPackageObject(ManifestHeader header, String id, String version, String versionAttribute) {
        super(header, id, version, versionAttribute);
    }

    protected void processFriends() {
        String[] friends = getDirectives(ICoreConstants.FRIENDS_DIRECTIVE);
        if (friends != null) {
            for (int i = 0; i < friends.length; i++) {
                fFriends.put(friends[i], new PackageFriend(this, friends[i]));
            }
        }
    }

    public boolean isInternal() {
        //$NON-NLS-1$
        return "true".equals(getDirective(ICoreConstants.INTERNAL_DIRECTIVE)) || getDirective(ICoreConstants.FRIENDS_DIRECTIVE) != null;
    }

    public void removeInternalDirective() {
        setDirective(ICoreConstants.INTERNAL_DIRECTIVE, null);
        ((CompositeManifestHeader) fHeader).update(true);
    }

    public void setInternal(boolean internal) {
        boolean old = isInternal();
        if (!internal) {
            setDirective(ICoreConstants.INTERNAL_DIRECTIVE, null);
            setDirective(ICoreConstants.FRIENDS_DIRECTIVE, null);
        } else {
            if (fFriends.size() == 0)
                setDirective(//$NON-NLS-1$
                ICoreConstants.INTERNAL_DIRECTIVE, //$NON-NLS-1$
                "true");
            else {
                Iterator<String> iter = fFriends.keySet().iterator();
                while (iter.hasNext()) addDirective(ICoreConstants.FRIENDS_DIRECTIVE, iter.next().toString());
            }
        }
        fHeader.update();
        firePropertyChanged(this, ICoreConstants.INTERNAL_DIRECTIVE, Boolean.toString(old), Boolean.toString(internal));
    }

    public PackageFriend[] getFriends() {
        return fFriends.values().toArray(new PackageFriend[fFriends.size()]);
    }

    public void addFriend(PackageFriend friend) {
        boolean oldIsInternal = isInternal();
        fFriends.put(friend.getName(), friend);
        addDirective(ICoreConstants.FRIENDS_DIRECTIVE, friend.getName());
        setDirective(ICoreConstants.INTERNAL_DIRECTIVE, null);
        fHeader.update();
        fireStructureChanged(friend, IModelChangedEvent.INSERT);
        firePropertyChanged(this, ICoreConstants.INTERNAL_DIRECTIVE, Boolean.toString(oldIsInternal), Boolean.FALSE.toString());
    }

    public void removeFriend(PackageFriend friend) {
        boolean hasInternalChanged = false;
        fFriends.remove(friend.getName());
        setDirective(ICoreConstants.FRIENDS_DIRECTIVE, null);
        if (fFriends.size() == 0) {
            //$NON-NLS-1$
            setDirective(ICoreConstants.INTERNAL_DIRECTIVE, "true");
            hasInternalChanged = true;
        } else {
            Iterator<String> iter = fFriends.keySet().iterator();
            while (iter.hasNext()) addDirective(ICoreConstants.FRIENDS_DIRECTIVE, iter.next().toString());
        }
        fHeader.update();
        fireStructureChanged(friend, IModelChangedEvent.REMOVE);
        if (hasInternalChanged)
            firePropertyChanged(this, ICoreConstants.INTERNAL_DIRECTIVE, Boolean.FALSE.toString(), Boolean.TRUE.toString());
    }

    public boolean hasFriend(String name) {
        return fFriends.containsKey(name);
    }

    public boolean hasSameVisibility(ExportPackageObject object) {
        if (object.isInternal() != isInternal())
            return false;
        if (fFriends.size() != object.fFriends.size())
            return false;
        Iterator<String> iter = fFriends.keySet().iterator();
        while (iter.hasNext()) {
            if (!object.fFriends.containsKey(iter.next()))
                return false;
        }
        return true;
    }

    public void setUsesDirective(String value) {
        String oldValue = getUsesDirective();
        setDirective(Constants.USES_DIRECTIVE, value);
        fHeader.update();
        firePropertyChanged(this, Constants.USES_DIRECTIVE, oldValue, value);
    }

    public String getUsesDirective() {
        return getDirective(Constants.USES_DIRECTIVE);
    }

    @Override
    protected void appendValuesToBuffer(StringBuffer sb, TreeMap<String, Serializable> table) {
        if (table == null) {
            return;
        }
        Serializable usesValue = null;
        // remove the Uses directive, we will make sure to put it at the end
        if (table.containsKey(Constants.USES_DIRECTIVE)) {
            usesValue = table.remove(Constants.USES_DIRECTIVE);
        }
        Serializable friendsValue = null;
        // remove the friends directive, ensure it's appropriately formatted
        if (table.containsKey(ICoreConstants.FRIENDS_DIRECTIVE)) {
            friendsValue = table.remove(ICoreConstants.FRIENDS_DIRECTIVE);
        }
        super.appendValuesToBuffer(sb, table);
        // If only one of uses and x-friends is specified, then the directives
        // have new lines at commas if there are more than 3 of them; if they're
        // both specified then they insert new lines for more than 1.
        int newLineLimit = NEWLINE_LIMIT;
        if (friendsValue != null && usesValue != null) {
            newLineLimit = NEWLINE_LIMIT_BOTH;
        }
        if (friendsValue != null) {
            table.put(ICoreConstants.FRIENDS_DIRECTIVE, friendsValue);
            formatDirective(ICoreConstants.FRIENDS_DIRECTIVE, sb, friendsValue, newLineLimit);
        }
        // uses goes last
        if (usesValue != null) {
            table.put(Constants.USES_DIRECTIVE, usesValue);
            formatDirective(Constants.USES_DIRECTIVE, sb, usesValue, newLineLimit);
        }
    }

    /**
	 * Format the specified directive of the Export-Package manifest header.
	 *
	 * @param directiveName
	 *            The name of the directive, e.g. x-friends or uses
	 * @param sb
	 *            buffer to append the directives
	 * @param usesValue
	 *            The value of the uses directive, expected to be a String or a
	 *            List.
	 * @param newLineLimit
	 *            The number of items, above which, a new line would be needed
	 *            between all values.
	 */
    private void formatDirective(String directiveName, StringBuffer sb, Object usesValue, final int newLineLimit) {
        //$NON-NLS-1$
        final String INDENT2 = "  ";
        //$NON-NLS-1$
        final String INDENT3 = "   ";
        StringTokenizer tokenizer = null;
        boolean newLine = false;
        if (usesValue instanceof String) {
            // break the string down at commas
            //$NON-NLS-1$
            tokenizer = new StringTokenizer((String) usesValue, ",");
            if (tokenizer.countTokens() > newLineLimit) {
                newLine = true;
            }
        } else if (usesValue instanceof List) {
            List<?> usesList = (List<?>) usesValue;
            if (usesList.size() > newLineLimit) {
                newLine = true;
            }
        } else {
            // wrong type for usesValue! - in this situation the old
            // formatUsesDirective() would throw a ClassCastException.
            // So for consistency! :-(
            Object foo = usesValue;
            // To remove 'non-usage' error :-(
            foo.getClass();
            // return should be unreachable
            return;
        }
        final String EOL = getHeader().getLineLimiter();
        sb.append(';');
        if (newLine) {
            sb.append(EOL).append(INDENT2);
        }
        sb.append(directiveName);
        //$NON-NLS-1$
        sb.append(":=\"");
        if (tokenizer != null) {
            while (tokenizer.hasMoreTokens()) {
                sb.append(tokenizer.nextToken());
                if (tokenizer.hasMoreTokens()) {
                    sb.append(',');
                    if (newLine) {
                        sb.append(EOL).append(INDENT3);
                    }
                }
            }
        } else {
            List<?> usesList = (List<?>) usesValue;
            for (ListIterator<?> iterator = usesList.listIterator(); true; ) {
                sb.append(iterator.next());
                if (iterator.hasNext()) {
                    sb.append(',');
                    if (newLine) {
                        sb.append(EOL).append(INDENT3);
                    }
                } else {
                    break;
                }
            }
        }
        //$NON-NLS-1$
        sb.append("\"");
    }

    @Override
    public void write(String indent, PrintWriter writer) {
        // Used for text transfers for copy, cut, paste operations
        writer.write(write());
    }

    /**
	 * @param model
	 * @param header
	 * @param versionAttribute
	 */
    public void reconnect(IBundleModel model, ExportPackageHeader header, String versionAttribute) {
        super.reconnect(model, header, versionAttribute);
        // Non-Transient Field:  Friends
        reconnectFriends();
    }

    /**
	 *
	 */
    private void reconnectFriends() {
        // Get all the friends
        Iterator<String> keys = fFriends.keySet().iterator();
        // Fill in appropriate transient field values for all friends
        while (keys.hasNext()) {
            String key = keys.next();
            PackageFriend friend = fFriends.get(key);
            friend.reconnect(this);
        }
    }
}
