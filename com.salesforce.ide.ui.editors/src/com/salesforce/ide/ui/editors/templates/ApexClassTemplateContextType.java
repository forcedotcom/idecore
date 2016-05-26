/*******************************************************************************
 * Copyright (c) 2015 Salesforce.com, inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *  
 * Contributors:
 *     Salesforce.com, inc. - initial API and implementation
 ******************************************************************************/
package com.salesforce.ide.ui.editors.templates;

public final class ApexClassTemplateContextType extends ForceTemplateContextType {

    public static final String ID = "com.salesforce.ide.ui.editors.templates.contextType.apexClass"; //$NON-NLS-1$

    public ApexClassTemplateContextType() {
        super();
        addLocalResolvers();
    }

    public ApexClassTemplateContextType(String id) {
        super(id);
        addLocalResolvers();
    }

    public ApexClassTemplateContextType(String id, String name) {
        super(id, name);
        addLocalResolvers();
    }

    private void addLocalResolvers() {
        addResolver(new ClassName());
    }

}
