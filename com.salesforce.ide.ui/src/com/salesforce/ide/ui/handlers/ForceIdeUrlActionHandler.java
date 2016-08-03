package com.salesforce.ide.ui.handlers;

import java.lang.reflect.InvocationTargetException;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.widgets.Display;

import com.salesforce.ide.core.internal.utils.ForceIdeUrlParser;
import com.salesforce.ide.core.project.BaseNature;
import com.salesforce.ide.core.project.ForceProject;
import com.salesforce.ide.core.project.ProjectController;
import com.salesforce.ide.core.project.ProjectModel;
import com.salesforce.ide.ui.wizards.project.ProjectCreateOperation;

/**
 * Urls can be dragged from the "Start Partner Debug Session" Dialog on Apex Debugger page, when an LMO logs into a
 * subscriber org. This handler uses the data in the Url to create a Force.comIDE Debug project for the debug session
 * @since 202
 * @author dbaker
 */
public class ForceIdeUrlActionHandler {
	
    private static final Logger logger = Logger.getLogger(BaseNature.class);

    private ForceIdeUrlParser urlParser;
	private Display display;
	
	@SuppressWarnings("unused")
	private ForceIdeUrlActionHandler(){}
	public ForceIdeUrlActionHandler(final ForceIdeUrlParser urlParser, final Display display){
		this.urlParser = urlParser;
		this.display = display;
	}
	
	/**
	 * Actions that can be taken upon a project by this handler
	 */
	public enum ProjectAction{
		UNSET,
        CREATE,
        UPDATE,
        INVALID,
        IGNORE
    };
   
    /**
     * Commands that can be passed as a parameter of of the Url
     */
    public enum Commands {
    	CREATE_PROJECT("createproject"),
    	INVALID("invalid");
    	
    	private final String command;
    	private Commands(String command){
    		this.command = command;
    	}
    	public String toString(){
    		return this.command;
    	}
    }
    
    public ProjectAction getResult() {
		return result;
	}
	private ProjectAction result = ProjectAction.UNSET;

    public static ProjectAction processCommand(final String url, Display display){
    	
    	if (display == null || display.isDisposed())
    		return ProjectAction.INVALID;
    	
    	ForceIdeUrlParser urlParser = new ForceIdeUrlParser(url);
    	
    	if (!urlParser.isValid())
    		return ProjectAction.INVALID;
   
     	ForceIdeUrlActionHandler forceUrlHandler = new ForceIdeUrlActionHandler(urlParser, display);
     	if (urlParser.getCommand().equals(Commands.CREATE_PROJECT.toString()))
     		forceUrlHandler.runCreateOrUpdate();
     	else
     		logger.error("Invalid command for forceUrlHandler: "+ urlParser.getCommand());
     	
     	return forceUrlHandler.getResult();
    }

    /**
     * Invoke createOrUpdateJob in display thread so user see dialog with progress
     */
    private ProjectAction runCreateOrUpdate(){
 
		if (Thread.currentThread() == display.getThread()) {
			return createOrUpdateJob(urlParser.asForceProject());
		} else {
			display.asyncExec(new Runnable() {
				public void run() {
					try {
						result = createOrUpdateJob(urlParser.asForceProject());
					} finally {
						result.notify();
					}
				}
			});
			synchronized (result) {
				try {
					result.wait();
				} catch (InterruptedException e) {
					logger.error("Wait for Project create was interupted");
					return ProjectAction.INVALID;
				}
			}
			return result;
		}

    }
    
    
    private ProjectAction createOrUpdateJob(ForceProject forceProject){
   
    	final String projectName = urlParser.getOrgName();
        IProgressMonitor monitor = null;
        monitor = new NullProgressMonitor();

        ProjectModel projModel = new ProjectModel(forceProject);
        projModel.setProjectName(projectName);
        projModel.setEnvironment("other");
        ProjectController projController = new ProjectController(null);
        projController.setModel(projModel);
        
        // No source code is down-loaded automatically, IDE users must select source to be down-loaded
        projModel.setContentSelection(ProjectController.NONE);
        
        ProjectCreateOperation createOperation = new ProjectCreateOperation(projController);        
        try {
        	createOperation.create();
        } catch (InvocationTargetException | InterruptedException e) {
			logger.error(e);
		} finally {
             monitor.done();
        }

        return result = ProjectAction.CREATE;
    }

}
