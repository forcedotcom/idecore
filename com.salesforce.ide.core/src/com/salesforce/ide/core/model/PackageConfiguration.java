/*******************************************************************************
* Copyright (c) 2016 Salesforce.com, inc..
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
* 
* Contributors:
*     Salesforce.com, inc. - initial API and implementation
*******************************************************************************/
package com.salesforce.ide.core.model;

/**
 * Class to store any configurations that are passed to project package for processing.
 * 
 * @author nchen
 */
public class PackageConfiguration {
    final boolean includeComposite;
    final boolean removeComponent;
    final boolean replaceComponent;
    
    private PackageConfiguration(final Builder builder) {
        includeComposite = builder.includeComposite;
        removeComponent = builder.removeComposite;
        replaceComponent = builder.replaceComponent;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private boolean includeComposite;
        private boolean removeComposite;
        private boolean replaceComponent;
        
        private Builder() {
            includeComposite = false;
            removeComposite = false;
            replaceComponent = false;
        }
        
        public Builder setIncludeComposite(boolean includeComposite) {
            this.includeComposite = includeComposite;
            return this;
        }
        
        public Builder setRemoveComposite(boolean removeComposite) {
            this.removeComposite = removeComposite;
            return this;
        }

        public Builder setReplaceComponent(boolean replaceComponent) {
            this.replaceComponent = replaceComponent;
            return this;
        }
        
        public PackageConfiguration build() {
            return new PackageConfiguration(this);
        }
    }
}
