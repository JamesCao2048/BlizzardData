/*
 * Copyright (c) OSGi Alliance (2014). All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.osgi.service.remoteserviceadmin.namespace;

import org.osgi.resource.Namespace;

/**
 * Remote Services Distribution Provider Capability and Requirement Namespace.
 * 
 * <p>
 * This class defines the names for the attributes and directives for this
 * namespace.
 * 
 * @Immutable
 * @author $Id: e87df13b4a2cc069621bf286945b66eac1ab072b $
 */
public final class DistributionNamespace extends Namespace {

    /**
	 * Namespace name for Remote Services distribution provider capabilities and
	 * requirements.
	 */
    public static final String DISTRIBUTION_NAMESPACE = "osgi.remoteserviceadmin.distribution";

    /**
	 * The capability attribute used to specify the config types supported by
	 * this distribution provider. The value of this attribute must be of type
	 * {@code String} or {@code List<String>}.
	 */
    public static final String CAPABILITY_CONFIGS_ATTRIBUTE = "configs";

    private  DistributionNamespace() {
    // empty
    }
}
