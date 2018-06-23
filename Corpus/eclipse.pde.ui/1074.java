/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package x.y.z;

import internal.x.y.z.Iinternal;

public interface Etest9 extends Iinternal {

    /**
 * @noimplement This interface is not intended to be implemented by clients.
 */
    interface inner extends Iinternal {

        /**
	 * @noimplement This interface is not intended to be implemented by clients.
	 */
        interface inner2 extends Iinternal {
        }
    }
}
