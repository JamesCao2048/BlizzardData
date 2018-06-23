/*******************************************************************************
 * Copyright (c) 2005, 2015 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    tyeung@bea.com - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.apt.core.internal.env;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.apt.core.env.Phase;
import org.eclipse.jdt.apt.core.internal.AptPlugin;
import org.eclipse.jdt.apt.core.internal.declaration.EclipseMirrorObject;
import org.eclipse.jdt.apt.core.internal.declaration.TypeDeclarationImpl;
import org.eclipse.jdt.apt.core.internal.env.MessagerImpl.Severity;
import org.eclipse.jdt.apt.core.internal.util.Factory;
import org.eclipse.jdt.apt.core.internal.util.Visitors.AnnotatedNodeVisitor;
import org.eclipse.jdt.apt.core.internal.util.Visitors.AnnotationVisitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.compiler.BuildContext;
import org.eclipse.jdt.core.dom.*;
import com.sun.mirror.apt.Filer;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;
import com.sun.mirror.declaration.PackageDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;

public class BuildEnv extends AbstractCompilationEnv {

    private boolean _hasRaisedErrors = false;

    private final BuildFilerImpl _filer;

    /**
	 * Set of strings that indicate new type dependencies introduced on the file
	 * each string is a fully-qualified type name.
	 */
    private Set<String> _typeDependencies = new HashSet();

    /**
	 * Indicates whether we are in batch mode or not. This gets flipped only 
	 * during build and could be flipped back and forth. 
	 */
    // off by default.
    private boolean _batchMode = false;

    /** 
	 * Holds all the files that contains annotation that are to be processed during build.
	 * If we are not in batch mode, <code>super._file</code> holds the file 
	 * being processed at the time. 
	 */
    private BuildContext[] _filesWithAnnotation = null;

    /**
	 * These are files that are part of a build but does not have annotations on it.
	 * During batch mode processing, these files still also need to be included. 
	 */
    private BuildContext[] _additionFiles = null;

    /** 
	 * This is intialized when <code>_batchMode</code> is set to be <code>true</code> or
	 * when batch processing is expected. <p>
	 * It is also set in build mode for perf reason rather than parsing and resolving
	 * each file individually.
	 * @see #getAllAnnotationTypes(Map)
	 */
    private CompilationUnit[] _astRoots = null;

    private List<MarkerInfo> _markerInfos = null;

    /**
     * Constructor for creating a processor environment used during build.
     * @param filesWithAnnotations
     * @param additionalFiles
     * @param units
     * @param javaProj
     * @param phase
     */
     BuildEnv(final BuildContext[] filesWithAnnotations, final BuildContext[] additionalFiles, final IJavaProject javaProj) {
        super(null, null, javaProj, Phase.BUILD);
        _filer = new BuildFilerImpl(this);
        _filesWithAnnotation = filesWithAnnotations;
        _additionFiles = additionalFiles;
        _problems = new ArrayList();
        _markerInfos = new ArrayList();
        if (AptPlugin.DEBUG_COMPILATION_ENV)
            AptPlugin.trace(//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            "constructed " + this + " for " + _filesWithAnnotation.length + " files");
    }

    public Filer getFiler() {
        checkValid();
        return _filer;
    }

    public PackageDeclaration getPackage(String name) {
        checkValid();
        return super.getPackage(name);
    }

    public TypeDeclaration getTypeDeclaration(String name) {
        checkValid();
        TypeDeclaration decl = super.getTypeDeclaration(name);
        if (!_batchMode)
            addTypeDependency(name);
        return decl;
    }

    /**
	 * @return true iff errors (MessagerImpl.Severity.Error) has been posted
	 *         Always return false when this environment is closed.
	 */
    public boolean hasRaisedErrors() {
        return _hasRaisedErrors;
    }

    public static InputStreamReader getFileReader(final IFile file) throws IOException, CoreException {
        return new InputStreamReader(getInputStream(file), file.getCharset());
    }

    public static InputStream getInputStream(final IFile file) throws IOException, CoreException {
        return new BufferedInputStream(file.getContents());
    }

    /**
	 * @return true iff class files has been generated.
	 *         Always return false when this environment is closed.
	 */
    public boolean hasGeneratedClassFiles() {
        return _filer.hasGeneratedClassFile();
    }

    /* (non-Javadoc)
	 *  Once the environment is closed the following is not allowed
	 *  1) posting messge
	 *  2) generating file
	 *  3) retrieving type or package by name
	 *  4) add or remove listeners
	 */
    public void close() {
        if (isClosed())
            return;
        _markerInfos = null;
        _astRoot = null;
        _file = null;
        _astRoots = null;
        _filesWithAnnotation = null;
        _problems = null;
        _modelCompUnit2astCompUnit.clear();
        _hasRaisedErrors = false;
        super.close();
    }

    /**
     * 
     * @param resource null to indicate current resource
     * @param start the starting offset of the marker
     * @param end -1 to indicate unknow ending offset.
     * @param severity the severity of the marker
     * @param msg the message on the marker
     * @param line the line number of where the marker should be
     */
    void addMessage(IFile resource, int start, int end, Severity severity, String msg, int line, String[] arguments) {
        checkValid();
        if (resource == null)
            resource = getFile();
        _hasRaisedErrors |= severity == MessagerImpl.Severity.ERROR;
        // Eclipse doesn't support INFO-level IProblems, so we send them to the log instead.
        if (severity == Severity.INFO) {
            StringBuilder sb = new StringBuilder();
            //$NON-NLS-1$
            sb.append("Informational message reported by annotation processor:\n");
            sb.append(msg);
            //$NON-NLS-1$
            sb.append("\n");
            if (resource != null) {
                //$NON-NLS-1$
                sb.append("Resource=");
                sb.append(resource.getName());
                //$NON-NLS-1$
                sb.append("; ");
            }
            //$NON-NLS-1$
            sb.append("starting offset=");
            sb.append(start);
            //$NON-NLS-1$
            sb.append("; ending offset=");
            sb.append(end);
            //$NON-NLS-1$
            sb.append("; line=");
            sb.append(line);
            if (arguments != null) {
                //$NON-NLS-1$
                sb.append("; arguments:");
                for (String s : arguments) {
                    //$NON-NLS-1$
                    sb.append("\n");
                    sb.append(s);
                }
            } else {
                //$NON-NLS-1$
                sb.append("\n");
            }
            IStatus status = AptPlugin.createInfoStatus(null, sb.toString());
            AptPlugin.log(status);
            return;
        }
        if (resource == null) {
            //$NON-NLS-1$
            assert _batchMode : "not in batch mode but don't know about current resource";
            addMarker(start, end, severity, msg, line, arguments);
        } else
            addProblem(resource, start, end, severity, msg, line, arguments);
    }

    private void addProblem(IFile resource, int start, int end, Severity severity, String msg, int line, String[] arguments) {
        APTProblem problem = createProblem(resource, start, end, severity, msg, line, arguments);
        _problems.add(problem);
    }

    private void addMarker(int start, int end, Severity severity, String msg, int line, String[] arguments) {
        // Note that the arguments are ignored -- no quick-fix for markers.
        _markerInfos.add(new MarkerInfo(start, end, severity, msg, line));
    }

    public Map<String, AnnotationTypeDeclaration> getAnnotationTypes() {
        checkValid();
        assert _astRoot != null && _file != null && !_batchMode : //$NON-NLS-1$
        "operation not available under batch mode.";
        return super.getAnnotationTypes();
    }

    /**
	 * Return all annotations at declaration level within all compilation unit(s)
	 * associated with this environment. All the files associated with this environment will 
	 * be parsed and resolved for all declaration level elements at the return of this call.
	 * 
	 * @param file2Annotations populated by this method to map files to the annotation types
	 *        if contains. May be null.
	 * @return the map containing all annotation types found within this environment.
	 */
    public Map<String, AnnotationTypeDeclaration> getAllAnnotationTypes(final Map<BuildContext, Set<AnnotationTypeDeclaration>> file2Annotations) {
        checkValid();
        if (_filesWithAnnotation == null)
            return getAnnotationTypes();
        final List<Annotation> instances = new ArrayList();
        final Map<String, AnnotationTypeDeclaration> decls = new HashMap();
        final AnnotationVisitor visitor = new AnnotationVisitor(instances);
        for (int astIndex = 0, len = _astRoots.length; astIndex < len; astIndex++) {
            if (_astRoots == null || _astRoots[astIndex] == null)
                System.err.println();
            _astRoots[astIndex].accept(visitor);
            final Set<AnnotationTypeDeclaration> perFileAnnos = new HashSet();
            for (int instanceIndex = 0, size = instances.size(); instanceIndex < size; instanceIndex++) {
                final Annotation instance = instances.get(instanceIndex);
                final ITypeBinding annoType = instance.resolveTypeBinding();
                if (annoType == null)
                    continue;
                final TypeDeclarationImpl decl = Factory.createReferenceType(annoType, this);
                if (decl != null && decl.kind() == EclipseMirrorObject.MirrorKind.TYPE_ANNOTATION) {
                    final AnnotationTypeDeclaration annoDecl = (AnnotationTypeDeclaration) decl;
                    decls.put(annoDecl.getQualifiedName(), annoDecl);
                    perFileAnnos.add(annoDecl);
                }
            }
            if (file2Annotations != null && !perFileAnnos.isEmpty())
                file2Annotations.put(_filesWithAnnotation[astIndex], perFileAnnos);
            visitor.reset();
        }
        return decls;
    }

    /**
	 * @return - the extra type dependencies for the files under compilation
	 */
    public Set<String> getTypeDependencies() {
        return _typeDependencies;
    }

    /**
	 * Switch to batch processing mode. 
	 * Note: Call to this method will cause all files associated with this environment to be 
	 * read and parsed.
	 */
    public void beginBatchProcessing() {
        if (_phase != Phase.BUILD)
            //$NON-NLS-1$
            throw new IllegalStateException("No batch processing outside build.");
        if (_batchMode)
            return;
        checkValid();
        _batchMode = true;
        _file = null;
        _astRoot = null;
    }

    public void completedBatchProcessing() {
        postMarkers();
        completedProcessing();
    }

    void createASTs(BuildContext[] cpResults) {
        final int len = cpResults.length;
        final ICompilationUnit[] units = new ICompilationUnit[len];
        for (int i = 0; i < len; i++) {
            // may return null if creation failed. this may occur if
            // the file does not exists.
            units[i] = JavaCore.createCompilationUnitFrom(cpResults[i].getFile());
        }
        createASTs(_javaProject, units, _requestor = new CallbackRequestor(units));
    }

    public void beginFileProcessing(BuildContext result) {
        if (result == null)
            //$NON-NLS-1$
            throw new IllegalStateException("missing compilation result");
        _batchMode = false;
        final IFile file = result.getFile();
        if (// this is a no-op
        file.equals(_file))
            return;
        _astRoot = null;
        _file = null;
        // need to match up the file with the ast.
        if (_filesWithAnnotation != null) {
            for (int i = 0, len = _filesWithAnnotation.length; i < len; i++) {
                if (file.equals(_filesWithAnnotation[i].getFile())) {
                    _file = file;
                    _astRoot = _astRoots[i];
                }
            }
        }
        if (_file == null || _astRoot == null)
            throw new IllegalStateException(//$NON-NLS-1$
            "file " + file.getName() + " is not in the list to be processed.");
    }

    public void completedFileProcessing() {
        completedProcessing();
    }

    @Override
    protected void completedProcessing() {
        _problems.clear();
        _typeDependencies.clear();
        super.completedProcessing();
    }

    public List<? extends CategorizedProblem> getProblems() {
        if (!_problems.isEmpty())
            EnvUtil.updateProblemLength(_problems, getAstCompilationUnit());
        return _problems;
    }

    // Implementation for EclipseAnnotationProcessorEnvironment
    public CompilationUnit getAST() {
        if (_batchMode)
            return null;
        return _astRoot;
    }

    public void addTypeDependency(final String fullyQualifiedTypeName) {
        if (!_batchMode) {
            _typeDependencies.add(fullyQualifiedTypeName);
        }
    }

    // End of implementation for EclipseAnnotationProcessorEnvironment
    /**
	 * Include all the types from all files, files with and without annotations on it
	 * if we are in batch mode. Otherwise, just the types from the file that's currently
	 * being processed.
	 */
    protected List<AbstractTypeDeclaration> searchLocallyForTypeDeclarations() {
        if (!_batchMode)
            return super.searchLocallyForTypeDeclarations();
        final List<AbstractTypeDeclaration> typeDecls = new ArrayList();
        for (int i = 0, len = _astRoots.length; i < len; i++) typeDecls.addAll(_astRoots[i].types());
        getTypeDeclarationsFromAdditionFiles(typeDecls);
        return typeDecls;
    }

    private void getTypeDeclarationsFromAdditionFiles(List<AbstractTypeDeclaration> typeDecls) {
        if (_additionFiles == null || _additionFiles.length == 0)
            return;
        final int len = _additionFiles.length;
        final ICompilationUnit[] units = new ICompilationUnit[len];
        for (int i = 0; i < len; i++) {
            // may return null if creation failed. this may occur if
            // the file does not exists.
            units[i] = JavaCore.createCompilationUnitFrom(_additionFiles[i].getFile());
        }
        BaseRequestor r = new BaseRequestor(units);
        createASTs(_javaProject, units, r);
        CompilationUnit[] asts = r.asts;
        for (CompilationUnit ast : asts) {
            if (ast != null) {
                typeDecls.addAll(ast.types());
            }
        }
    }

    protected Map<ASTNode, List<Annotation>> getASTNodesWithAnnotations() {
        if (!_batchMode)
            return super.getASTNodesWithAnnotations();
        final Map<ASTNode, List<Annotation>> astNode2Anno = new HashMap();
        final AnnotatedNodeVisitor visitor = new AnnotatedNodeVisitor(astNode2Anno);
        for (int i = 0, len = _astRoots.length; i < len; i++) _astRoots[i].accept(visitor);
        return astNode2Anno;
    }

    protected IFile getFileForNode(final ASTNode node) {
        if (!_batchMode)
            return super.getFileForNode(node);
        final CompilationUnit curAST = (CompilationUnit) node.getRoot();
        for (int i = 0, len = _astRoots.length; i < len; i++) {
            if (_astRoots[i] == curAST)
                return _filesWithAnnotation[i].getFile();
        }
        throw new IllegalStateException();
    }

    /**
	 * Go through the list of compilation unit in this environment and looking for
	 * the declaration node of the given binding.
	 * @param binding 
	 * @return the compilation unit that defines the given binding or null if no 
	 * match is found.
	 */
    protected CompilationUnit searchLocallyForBinding(final IBinding binding) {
        if (!_batchMode)
            return super.searchLocallyForBinding(binding);
        for (int i = 0, len = _astRoots.length; i < len; i++) {
            ASTNode node = _astRoots[i].findDeclaringNode(binding);
            if (node != null)
                return _astRoots[i];
        }
        return null;
    }

    /**
	 * Go through the list of compilation unit in this environment and looking for
	 * the declaration node of the given binding.
	 * @param binding 
	 * @return the compilation unit that defines the given binding or null if no 
	 * match is found.
	 */
    protected IFile searchLocallyForIFile(final IBinding binding) {
        if (!_batchMode)
            return super.searchLocallyForIFile(binding);
        for (int i = 0, len = _astRoots.length; i < len; i++) {
            ASTNode node = _astRoots[i].findDeclaringNode(binding);
            if (node != null)
                return _filesWithAnnotation[i].getFile();
        }
        return null;
    }

    /**
     * @param file
     * @return the compilation unit associated with the given file.
     * If the file is not one of those that this environment is currently processing,
     * return null;
     */
    public CompilationUnit getASTFrom(final IFile file) {
        if (file == null)
            return null;
        else if (file.equals(_file))
            return _astRoot;
        else if (_astRoots != null) {
            for (int i = 0, len = _filesWithAnnotation.length; i < len; i++) {
                if (file.equals(_filesWithAnnotation[i].getFile()))
                    return _astRoots[i];
            }
        }
        return null;
    }

    /**
	 * @return the current ast being processed if in per-file mode.
	 * If in batch mode, one of the asts being processed (no guarantee which
	 * one will be returned.  
	 */
    protected AST getCurrentDietAST() {
        if (_astRoot != null)
            return _astRoot.getAST();
        else {
            if (_astRoots == null)
                throw new //$NON-NLS-1$
                IllegalStateException(//$NON-NLS-1$
                "no AST is available");
            return _astRoots[0].getAST();
        }
    }

    void postMarkers() {
        if (_markerInfos == null || _markerInfos.size() == 0)
            return;
        // to minimize the amount of notification.
        try {
            final IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

                public void run(IProgressMonitor monitor) {
                    for (MarkerInfo markerInfo : _markerInfos) {
                        try {
                            final IMarker marker = _javaProject.getProject().createMarker(AptPlugin.APT_BATCH_PROCESSOR_PROBLEM_MARKER);
                            markerInfo.copyIntoMarker(marker);
                        } catch (CoreException e) {
                            AptPlugin.log(e, "Failure posting markers");
                        }
                    }
                }
            };
            IWorkspace ws = _javaProject.getProject().getWorkspace();
            ws.run(runnable, null);
        } catch (CoreException e) {
            AptPlugin.log(e, "Failed to post markers");
        } finally {
            _markerInfos.clear();
        }
    }

    public BuildContext[] getFilesWithAnnotation() {
        return _filesWithAnnotation;
    }

    public BuildContext[] getFilesWithoutAnnotation() {
        return _additionFiles;
    }

    private class CallbackRequestor extends BaseRequestor {

         CallbackRequestor(ICompilationUnit[] parseUnits) {
            super(parseUnits);
        }

        public void acceptBinding(String bindingKey, IBinding binding) {
            // If we have recieved the last ast we have requested,
            // then assign the asts, then begin dispatch
            _astRoots = asts;
            _callback.run(BuildEnv.this);
        }
    }
}
