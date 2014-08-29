package com.salesforce.ide.resourcebundles.internal;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.salesforce.ide.core.project.ForceProject;
import com.salesforce.ide.core.project.ForceProjectException;
import com.salesforce.ide.core.project.ProjectController;
import com.salesforce.ide.ui.properties.BasePropertyPage;

/**
 * Allow users to turn off or on the static resource <-> resource bundle
 * synchronization on a project by project basis.
 */
public class ResourceBundlesPropertyPage extends BasePropertyPage {
    
    private Button unzip;
    private Button zip;
    
    public ResourceBundlesPropertyPage() throws ForceProjectException {    
        super();
    }
    
    @Override
    public void createControl(Composite parent) {
        super.createControl(parent);
        getDefaultsButton().setEnabled(false);
    }
    
    @Override
    protected Control createContents(Composite parent) {
        
        Composite composite = new Composite(parent, SWT.NONE);

        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 1;
        composite.setLayout(gridLayout);
        composite.setLayoutData(new GridData(SWT.BEGINNING, SWT.TOP, false, false));
        
        unzip = new Button(composite, SWT.CHECK);
        unzip.setText("Automatically un-ZIP static resources to resource bundles");
        
        zip = new Button(composite, SWT.CHECK);
        zip.setText("Automatically ZIP resource bundles to static resources");

        ForceProject fp = getForceProject();
        unzip.setSelection(fp.getUnzipStaticResourcesAutomatically());
        zip.setSelection(fp.getZipResourceBundlesAutomatically());
        
        return composite;
    }
    
    @Override
    public boolean performOk() {
        
        ForceProject fp = getForceProject();
        fp.setUnzipStaticResourcesAutomatically(unzip.getSelection());
        fp.setZipResourceBundlesAutomatically(zip.getSelection());
        
        ProjectController pc = new ProjectController();
        pc.getProjectModel().setForceProject(fp);
        try {
            pc.saveSettings(new NullProgressMonitor());
        } catch (InterruptedException e) {
            // Not possible with a NullProgressMonitor
        }

        return true;
    }
    
    private ForceProject getForceProject() {
        return getProjectService().getForceProject((IProject) getElement());
    }
}
