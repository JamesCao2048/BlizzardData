package $packageName$;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;

public class $wizardClassName$ extends Wizard implements IImportWizard {

    $wizardPageClassName$ mainPage;

    public  $wizardClassName$() {
        super();
    }

    /* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
    public boolean performFinish() {
        IFile file = mainPage.createNewFile();
        if (file == null)
            return false;
        return true;
    }

    /* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
	 */
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        //$NON-NLS-1$
        setWindowTitle("File Import Wizard");
        setNeedsProgressMonitor(true);
        //$NON-NLS-1$
        mainPage = new $wizardPageClassName$("$wizardImportName$", selection);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.IWizard#addPages()
     */
    public void addPages() {
        super.addPages();
        addPage(mainPage);
    }
}
