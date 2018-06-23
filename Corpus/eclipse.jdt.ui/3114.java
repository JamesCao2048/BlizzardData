/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.ui.tests.packageview;

import java.io.File;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.eclipse.jdt.testplugin.JavaProjectHelper;
import org.eclipse.jdt.testplugin.JavaTestPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.internal.ui.util.CoreUtility;

/**
 * Tests for the PackageExplorerContentProvider.
 *
 * @since 2.1
 */
public class ContentProviderTests4 extends TestCase {

    public static Test suite() {
        TestSuite suite = new TestSuite(ContentProviderTests4.class.getName());
        //$JUnit-BEGIN$
        suite.addTestSuite(ContentProviderTests4.class);
        //$JUnit-END$
        return suite;
    }

    private IPackageFragmentRoot fRoot1;

    private IPackageFragment fPack1;

    private IPackageFragment fPack2;

    private IPackageFragment fPack4;

    private IPackageFragment fPack3;

    private IWorkspace fWorkspace;

    private IWorkbench fWorkbench;

    private MockPluginView fMyPart;

    private ITreeContentProvider fProvider;

    private IPackageFragment fPack5;

    private IJavaProject fJProject3;

    private IPackageFragment fPack6;

    private IPackageFragmentRoot fInternalRoot1;

    private IPackageFragment fA;

    private IPackageFragment fX;

    private IPackageFragment fC;

    private IPackageFragment fD;

    private IPackageFragment fY;

    private IPackageFragment fDefaultPackage;

    private ICompilationUnit fCU1;

    private ICompilationUnit fCU2;

    private IClassFile fYClassFile;

    private IPackageFragmentRoot jdk;

    private IFile fFile1;

    private IFile fFile2;

    private IClassFile fAClassFile;

    private IWorkbenchPage page;

    private ICompilationUnit fCUinDefault;

    private File myInternalLibJar;

    private boolean fEnableAutoBuildAfterTesting;

    public  ContentProviderTests4(String name) {
        super(name);
    }

    public void testGetChildrenProject() throws Exception {
        Object[] expectedChildren = new Object[] { fPack1, fPack2, fPack3, fDefaultPackage, fFile1, fFile2, fInternalRoot1, jdk };
        Object[] children = fProvider.getChildren(fJProject3);
        //$NON-NLS-1$
        assertTrue("Wrong children found for project with folding", compareArrays(children, expectedChildren));
    }

    public void testGetChildrenDefaultProject() {
        Object[] expectedChildren = new Object[] { fCUinDefault };
        Object[] children = fProvider.getChildren(fDefaultPackage);
        //$NON-NLS-1$
        assertTrue("Wrong children found for default package with folding", compareArrays(children, expectedChildren));
    }

    public void testGetChildrentMidLevelFragment() throws Exception {
        Object[] expectedChildren = new Object[] { fPack4, fPack6 };
        Object[] children = fProvider.getChildren(fPack3);
        //$NON-NLS-1$
        assertTrue("Wrong children found for PackageFragment with folding", compareArrays(children, expectedChildren));
    }

    public void testGetChildrenBottomLevelFragment() throws Exception {
        Object[] expectedChildren = new Object[] {};
        Object[] children = fProvider.getChildren(fPack1);
        //$NON-NLS-1$
        assertTrue("Wrong children found for PackageFragment with folding", compareArrays(children, expectedChildren));
    }

    public void testGetChildrenBottomLevelFragmentWithCU() throws Exception {
        Object[] expectedChildren = new Object[] { fCU1 };
        Object[] children = fProvider.getChildren(fPack2);
        //$NON-NLS-1$
        assertTrue("Wrong children found for PackageFragment with folding", compareArrays(children, expectedChildren));
    }

    public void testGetChildrenBottomLevelFragmenWithCU2() throws Exception {
        Object[] expectedChildren = new Object[] { fCU2 };
        Object[] children = fProvider.getChildren(fPack6);
        //$NON-NLS-1$
        assertTrue("Wrong children found for PackageFragment with folding", compareArrays(children, expectedChildren));
    }

    public void testGetChildrenMidLevelFragmentInInternalArchive() throws Exception {
        Object[] expectedChildren = new Object[] { fC, fD, fAClassFile };
        Object[] children = fProvider.getChildren(fA);
        //$NON-NLS-1$
        assertTrue("wrong children found for a NON bottom PackageFragment in PackageFragmentRoot Internal Archive with folding", compareArrays(children, expectedChildren));
    }

    public void testGetChildrenBottomLevelFragmentInInternalArchive() throws Exception {
        Object[] expectedChildren = new Object[] { fYClassFile };
        Object[] children = fProvider.getChildren(fY);
        //$NON-NLS-1$
        assertTrue("wrong children found for a bottom PackageFragment in PackageFragmentRoot Internal Archive with folding", compareArrays(children, expectedChildren));
    }

    public void getChildrenInternalArchive() throws Exception {
        //$NON-NLS-1$
        Object[] expectedChildren = new Object[] { fX, fA, fInternalRoot1.getPackageFragment("") };
        Object[] children = fProvider.getChildren(fInternalRoot1);
        //$NON-NLS-1$
        assertTrue("Wrong child found for PackageFragmentRoot Internal Archive with folding", compareArrays(children, expectedChildren));
    }

    //---------------Get Parent Tests-----------------------------
    public void testGetParentArchive() throws Exception {
        Object parent = fProvider.getParent(fInternalRoot1);
        //$NON-NLS-1$
        assertTrue("Wrong parent found for PackageFragmentRoot Archive with folding", parent == fJProject3);
    }

    public void testGetParentMidLevelFragmentInArchive() throws Exception {
        Object expectedParent = fA;
        Object parent = fProvider.getParent(fC);
        //$NON-NLS-1$
        assertTrue("Wrong parent found for a NON top level PackageFragment in an Archive with folding", expectedParent.equals(parent));
    }

    public void testGetParentTopLevelFragmentInArchive() throws Exception {
        Object expectedParent = fInternalRoot1;
        Object parent = fProvider.getParent(fA);
        //$NON-NLS-1$
        assertTrue("Wrong parent found for a top level PackageFragment in an Archive with folding", expectedParent.equals(parent));
    }

    public void testGetParentTopLevelFragment() throws Exception {
        Object expectedParent = fJProject3;
        Object parent = fProvider.getParent(fPack3);
        //$NON-NLS-1$
        assertTrue("Wrong parent found for a top level PackageFragment with folding", expectedParent.equals(parent));
    }

    public void testGetParentMidLevelFragment() throws Exception {
        Object expectedParent = fPack3;
        Object parent = fProvider.getParent(fPack6);
        //$NON-NLS-1$
        assertTrue("Wrong parent found for a NON top level PackageFragment with folding", expectedParent.equals(parent));
    }

    public void testGetParentMidLevelFragment2() throws Exception {
        Object expectedParent = fPack3;
        Object parent = fProvider.getParent(fPack5);
        //$NON-NLS-1$
        assertTrue("Wrong parent found for a NON top level PackageFragment with folding", expectedParent.equals(parent));
    }

    //-------------------Set up methods--------------------------------
    /**
	 * @see TestCase#setUp()
	 */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        fWorkspace = ResourcesPlugin.getWorkspace();
        assertNotNull(fWorkspace);
        IWorkspaceDescription workspaceDesc = fWorkspace.getDescription();
        fEnableAutoBuildAfterTesting = workspaceDesc.isAutoBuilding();
        if (fEnableAutoBuildAfterTesting)
            CoreUtility.setAutoBuilding(false);
        //create project
        //$NON-NLS-1$//$NON-NLS-2$
        fJProject3 = JavaProjectHelper.createJavaProject("TestProject3", "bin");
        //$NON-NLS-1$
        assertNotNull("project3 null", fJProject3);
        Object[] resource = fJProject3.getNonJavaResources();
        for (int i = 0; i < resource.length; i++) {
            Object object = resource[i];
            if (object instanceof IFile) {
                IFile file = (IFile) object;
                if (".classpath".equals(//$NON-NLS-1$
                file.getName()))
                    fFile1 = file;
                else if (".project".equals(//$NON-NLS-1$
                file.getName()))
                    fFile2 = file;
            }
        }
        assertNotNull(fFile1);
        assertNotNull(fFile2);
        //add rt.jar
        //$NON-NLS-1$
        jdk = JavaProjectHelper.addVariableRTJar(fJProject3, "JRE_LIB_TEST", null, null);
        //$NON-NLS-1$
        assertTrue("jdk not found", jdk != null);
        //create the PackageFragmentRoot that represents the project as source folder
        //$NON-NLS-1$
        fRoot1 = JavaProjectHelper.addSourceContainer(fJProject3, "");
        //$NON-NLS-1$
        assertNotNull("getting default package", fRoot1);
        //$NON-NLS-1$
        fDefaultPackage = fRoot1.createPackageFragment("", true, null);
        //$NON-NLS-1$//$NON-NLS-2$
        fCUinDefault = fDefaultPackage.createCompilationUnit("Object.java", "", true, null);
        //set up project #3: file system structure with project as source folder
        //add an internal jar
        //$NON-NLS-1$
        myInternalLibJar = JavaTestPlugin.getDefault().getFileInPlugin(new Path("testresources/myinternallib.jar"));
        //$NON-NLS-1$
        assertTrue("lib not found", myInternalLibJar != null && myInternalLibJar.exists());
        fInternalRoot1 = JavaProjectHelper.addLibraryWithImport(fJProject3, Path.fromOSString(myInternalLibJar.getPath()), null, null);
        //create internal PackageFragments
        //$NON-NLS-1$
        fA = fInternalRoot1.getPackageFragment("a");
        //$NON-NLS-1$
        fX = fInternalRoot1.getPackageFragment("x");
        //$NON-NLS-1$
        fInternalRoot1.getPackageFragment("a.b");
        //$NON-NLS-1$
        fC = fInternalRoot1.getPackageFragment("a.b.c");
        //$NON-NLS-1$
        fD = fInternalRoot1.getPackageFragment("a.d");
        //$NON-NLS-1$
        fY = fInternalRoot1.getPackageFragment("x.y");
        //$NON-NLS-1$
        fYClassFile = fY.getClassFile("Y.class");
        //$NON-NLS-1$
        fAClassFile = fA.getClassFile("A.class");
        //create PackageFragments
        //$NON-NLS-1$
        fPack1 = fRoot1.createPackageFragment("pack1", true, null);
        //$NON-NLS-1$
        fPack2 = fRoot1.createPackageFragment("pack2", true, null);
        //$NON-NLS-1$
        fPack3 = fRoot1.createPackageFragment("pack3", true, null);
        //$NON-NLS-1$
        fPack4 = fRoot1.createPackageFragment("pack3.pack4", true, null);
        //$NON-NLS-1$
        fPack5 = fRoot1.createPackageFragment("pack3.pack5", true, null);
        //$NON-NLS-1$
        fPack6 = fRoot1.createPackageFragment("pack3.pack5.pack6", true, null);
        //$NON-NLS-1$//$NON-NLS-2$
        fCU1 = fPack2.createCompilationUnit("Object.java", "", true, null);
        //$NON-NLS-1$//$NON-NLS-2$
        fCU2 = fPack6.createCompilationUnit("Object.java", "", true, null);
        //set up the mock view
        setUpMockView();
    }

    public void setUpMockView() throws Exception {
        //		fWorkspace = ResourcesPlugin.getWorkspace();
        //		assertNotNull(fWorkspace);
        fWorkbench = PlatformUI.getWorkbench();
        assertNotNull(fWorkbench);
        page = fWorkbench.getActiveWorkbenchWindow().getActivePage();
        assertNotNull(page);
        //just testing to make sure my part can be created
        IViewPart myPart = new MockPluginView();
        assertNotNull(myPart);
        //$NON-NLS-1$
        myPart = page.showView("org.eclipse.jdt.ui.tests.packageview.MockPluginView");
        if (myPart instanceof MockPluginView) {
            fMyPart = (MockPluginView) myPart;
            fMyPart.setFolding(true);
            fMyPart.setFlatLayout(false);
            // above call might cause a property change event being sent
            fMyPart.clear();
            fProvider = (ITreeContentProvider) fMyPart.getTreeViewer().getContentProvider();
        } else
            //$NON-NLS-1$
            assertTrue("Unable to get view", false);
        assertNotNull(fProvider);
    }

    /**
	 * @see TestCase#tearDown()
	 */
    @Override
    protected void tearDown() throws Exception {
        fInternalRoot1.close();
        JavaProjectHelper.delete(fJProject3);
        page.hideView(fMyPart);
        if (fEnableAutoBuildAfterTesting)
            CoreUtility.setAutoBuilding(true);
        super.tearDown();
    }

    /**
	 * Method compareArrays. Both arrays must be of IPackageFragments or compare will fail.
	 * @param children
	 * @param expectedChildren
	 * @return boolean
	 */
    private boolean compareArrays(Object[] children, Object[] expectedChildren) {
        if (children.length != expectedChildren.length)
            return false;
        for (int i = 0; i < children.length; i++) {
            Object child = children[i];
            if (child instanceof IJavaElement) {
                IJavaElement el = (IJavaElement) child;
                if (!contains(el, expectedChildren))
                    return false;
            } else if (child instanceof IResource) {
                IResource res = (IResource) child;
                if (!contains(res, expectedChildren)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
	 * Method contains.
	 * @param res
	 * @param expectedChildren
	 * @return boolean
	 */
    private boolean contains(IResource res, Object[] expectedChildren) {
        for (int i = 0; i < expectedChildren.length; i++) {
            Object object = expectedChildren[i];
            if (object instanceof IResource) {
                IResource expres = (IResource) object;
                if (expres.equals(res))
                    return true;
            }
        }
        return false;
    }

    /**
	 * Method contains.
	 * @param fragment
	 * @param expectedChildren
	 * @return boolean
	 */
    private boolean contains(IJavaElement fragment, Object[] expectedChildren) {
        for (int i = 0; i < expectedChildren.length; i++) {
            Object object = expectedChildren[i];
            if (object instanceof IJavaElement) {
                IJavaElement expfrag = (IJavaElement) object;
                if (expfrag.equals(fragment))
                    return true;
            }
        }
        return false;
    }
}
