/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jesper Steen Moller - Bugs 341232, 427089
 *     Chris West (Faux) - Bug 45507
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.eval.ast.engine;

import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventFilter;
import org.eclipse.debug.core.model.ITerminate;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.debug.core.IEvaluationRunnable;
import org.eclipse.jdt.debug.core.IJavaArray;
import org.eclipse.jdt.debug.core.IJavaArrayType;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaReferenceType;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.debug.eval.IAstEvaluationEngine;
import org.eclipse.jdt.debug.eval.ICompiledExpression;
import org.eclipse.jdt.debug.eval.IEvaluationListener;
import org.eclipse.jdt.debug.eval.IEvaluationResult;
import org.eclipse.jdt.internal.debug.core.JDIDebugOptions;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;
import org.eclipse.jdt.internal.debug.core.model.JDIThread;
import org.eclipse.jdt.internal.debug.core.model.JDIValue;
import org.eclipse.jdt.internal.debug.eval.EvaluationResult;
import org.eclipse.jdt.internal.debug.eval.ast.instructions.InstructionSequence;
import com.sun.jdi.InvocationException;
import com.sun.jdi.ObjectReference;

public class ASTEvaluationEngine implements IAstEvaluationEngine {

    //$NON-NLS-1$
    public static final String ANONYMOUS_VAR_PREFIX = "val$";

    private IJavaProject fProject;

    private IJavaDebugTarget fDebugTarget;

    /**
	 * Regex to find occurrences of 'this' in a code snippet
	 */
    private static Pattern fgThisPattern = Pattern.compile(//$NON-NLS-1$
    "(.*[^a-zA-Z0-9]+|^)(this)([^a-zA-Z0-9]+|$).*");

    /**
	 * Filters variable change events during an evaluation to avoid refreshing
	 * the variables view until done.
	 */
    class EventFilter implements IDebugEventFilter {

        /*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.debug.core.IDebugEventFilter#filterDebugEvents(org.eclipse
		 * .debug.core.DebugEvent[])
		 */
        @Override
        public DebugEvent[] filterDebugEvents(DebugEvent[] events) {
            if (events.length == 1) {
                DebugEvent event = events[0];
                if (event.getSource() instanceof IJavaVariable && event.getKind() == DebugEvent.CHANGE) {
                    if (((IJavaVariable) event.getSource()).getDebugTarget().equals(getDebugTarget())) {
                        return null;
                    }
                }
            }
            return events;
        }
    }

    public  ASTEvaluationEngine(IJavaProject project, IJavaDebugTarget debugTarget) {
        setJavaProject(project);
        setDebugTarget(debugTarget);
    }

    public void setJavaProject(IJavaProject project) {
        fProject = project;
    }

    public void setDebugTarget(IJavaDebugTarget debugTarget) {
        fDebugTarget = debugTarget;
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jdt.debug.eval.IEvaluationEngine#evaluate(java.lang.String,
	 * org.eclipse.jdt.debug.core.IJavaStackFrame,
	 * org.eclipse.jdt.debug.eval.IEvaluationListener, int, boolean)
	 */
    @Override
    public void evaluate(String snippet, IJavaStackFrame frame, IEvaluationListener listener, int evaluationDetail, boolean hitBreakpoints) throws DebugException {
        traceCaller(snippet, frame.getThread());
        ICompiledExpression expression = getCompiledExpression(snippet, frame);
        evaluateExpression(expression, frame, listener, evaluationDetail, hitBreakpoints);
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jdt.debug.eval.IEvaluationEngine#evaluate(java.lang.String,
	 * org.eclipse.jdt.debug.core.IJavaObject,
	 * org.eclipse.jdt.debug.core.IJavaThread,
	 * org.eclipse.jdt.debug.eval.IEvaluationListener, int, boolean)
	 */
    @Override
    public void evaluate(String snippet, IJavaObject thisContext, IJavaThread thread, IEvaluationListener listener, int evaluationDetail, boolean hitBreakpoints) throws DebugException {
        traceCaller(snippet, thread);
        ICompiledExpression expression = getCompiledExpression(snippet, thisContext);
        evaluateExpression(expression, thisContext, thread, listener, evaluationDetail, hitBreakpoints);
    }

    /**
	 * Writes a stack dump to trace the calling thread.
	 * 
	 * @param snippet
	 *            expression to evaluate
	 * @param thread
	 *            thread to evaluate in
	 */
    private void traceCaller(String snippet, IThread thread) {
        if (JDIDebugOptions.DEBUG_AST_EVAL_THREAD_TRACE) {
            StringBuffer buf = new StringBuffer();
            buf.append(JDIDebugOptions.FORMAT.format(new Date()));
            //$NON-NLS-1$
            buf.append(" : Evaluation Request Trace - Expression: ");
            buf.append(snippet);
            //$NON-NLS-1$
            buf.append("\n\tThread: ");
            try {
                String name = thread.getName();
                buf.append('[');
                buf.append(name);
                //$NON-NLS-1$
                buf.append(//$NON-NLS-1$
                "] ");
            } catch (DebugException e) {
                buf.append(thread.toString());
            }
            JDIDebugOptions.trace(buf.toString());
        }
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jdt.debug.eval.IAstEvaluationEngine#evaluateExpression(org
	 * .eclipse.jdt.debug.eval.ICompiledExpression,
	 * org.eclipse.jdt.debug.core.IJavaStackFrame,
	 * org.eclipse.jdt.debug.eval.IEvaluationListener, int, boolean)
	 */
    @Override
    public void evaluateExpression(ICompiledExpression expression, IJavaStackFrame frame, IEvaluationListener listener, int evaluationDetail, boolean hitBreakpoints) throws DebugException {
        traceCaller(expression.getSnippet(), frame.getThread());
        RuntimeContext context = new RuntimeContext(getJavaProject(), frame);
        doEvaluation(expression, context, (IJavaThread) frame.getThread(), listener, evaluationDetail, hitBreakpoints);
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jdt.debug.eval.IAstEvaluationEngine#evaluateExpression(org
	 * .eclipse.jdt.debug.eval.ICompiledExpression,
	 * org.eclipse.jdt.debug.core.IJavaObject,
	 * org.eclipse.jdt.debug.core.IJavaThread,
	 * org.eclipse.jdt.debug.eval.IEvaluationListener, int, boolean)
	 */
    @Override
    public void evaluateExpression(ICompiledExpression expression, IJavaObject thisContext, IJavaThread thread, IEvaluationListener listener, int evaluationDetail, boolean hitBreakpoints) throws DebugException {
        traceCaller(expression.getSnippet(), thread);
        IRuntimeContext context = null;
        if (thisContext instanceof IJavaArray) {
            context = new ArrayRuntimeContext((IJavaArray) thisContext, thread, getJavaProject());
        } else {
            context = new JavaObjectRuntimeContext(thisContext, getJavaProject(), thread);
        }
        doEvaluation(expression, context, thread, listener, evaluationDetail, hitBreakpoints);
    }

    /**
	 * Evaluates the given expression in the given thread and the given runtime
	 * context.
	 */
    private void doEvaluation(ICompiledExpression expression, IRuntimeContext context, IJavaThread thread, IEvaluationListener listener, int evaluationDetail, boolean hitBreakpoints) throws DebugException {
        if (expression instanceof InstructionSequence) {
            // performing an evaluation.
            if (thread.isSuspended() && ((JDIThread) thread).isInvokingMethod() || thread.isPerformingEvaluation() && evaluationDetail == DebugEvent.EVALUATION) {
                EvaluationResult result = new EvaluationResult(this, expression.getSnippet(), thread);
                result.addError(EvaluationEngineMessages.ASTEvaluationEngine_Cannot_perform_nested_evaluations);
                listener.evaluationComplete(result);
                return;
            }
            thread.queueRunnable(new EvalRunnable((InstructionSequence) expression, thread, context, listener, evaluationDetail, hitBreakpoints));
        } else {
            throw new DebugException(new Status(IStatus.ERROR, JDIDebugPlugin.getUniqueIdentifier(), IStatus.OK, EvaluationEngineMessages.ASTEvaluationEngine_AST_evaluation_engine_cannot_evaluate_expression, null));
        }
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jdt.debug.eval.IAstEvaluationEngine#getCompiledExpression
	 * (java.lang.String, org.eclipse.jdt.debug.core.IJavaStackFrame)
	 */
    @Override
    public ICompiledExpression getCompiledExpression(String snippet, IJavaStackFrame frame) {
        IJavaProject javaProject = getJavaProject();
        RuntimeContext context = new RuntimeContext(javaProject, frame);
        EvaluationSourceGenerator mapper = null;
        CompilationUnit unit = null;
        try {
            IJavaVariable[] localsVar = context.getLocals();
            IJavaObject thisClass = context.getThis();
            // For anonymous classes, getting variables from outer class
            IVariable[] innerClassFields;
            if (null != thisClass) {
                innerClassFields = thisClass.getVariables();
            } else {
                innerClassFields = new IVariable[0];
            }
            int numLocalsVar = localsVar.length;
            Set<String> names = new HashSet<String>();
            // ******
            // to hide problems with local variable declare as instance of Local
            // Types
            // and to remove locals with duplicate names
            // IJavaVariable[] locals = new IJavaVariable[numLocalsVar];
            IJavaVariable[] locals = new IJavaVariable[numLocalsVar + innerClassFields.length];
            String[] localVariablesWithNull = new String[numLocalsVar + innerClassFields.length];
            int numLocals = 0;
            for (int i = 0; i < numLocalsVar; i++) {
                if (!isLocalType(localsVar[i].getSignature()) && !names.contains(localsVar[i].getName())) {
                    locals[numLocals] = localsVar[i];
                    names.add(localsVar[i].getName());
                    localVariablesWithNull[numLocals++] = localsVar[i].getName();
                }
            }
            // Adding outer class variables to inner class scope
            for (int i = 0; i < innerClassFields.length; i++) {
                IVariable var = innerClassFields[i];
                if (var instanceof IJavaVariable && var.getName().startsWith(ANONYMOUS_VAR_PREFIX)) {
                    String name = var.getName().substring(ANONYMOUS_VAR_PREFIX.length());
                    if (!names.contains(name)) {
                        locals[numLocals] = (IJavaVariable) var;
                        names.add(name);
                        localVariablesWithNull[numLocals++] = name;
                    }
                }
            }
            // to solve and remove
            // ******
            String[] localTypesNames = new String[numLocals];
            for (int i = 0; i < numLocals; i++) {
                localTypesNames[i] = Signature.toString(locals[i].getGenericSignature()).replace('/', '.');
            }
            // Copying local variables removing the nulls in the last
            // String[] localVariables = Arrays.clonesub(localVariablesWithNull, names.size());
            String[] localVariables = new String[names.size()];
            System.arraycopy(localVariablesWithNull, 0, localVariables, 0, localVariables.length);
            mapper = new EvaluationSourceGenerator(localTypesNames, localVariables, snippet, getJavaProject());
            // Compile in context of declaring type to get proper visibility of
            // locals and members.
            // Compiling in context of receiving type potentially provides
            // access to more members,
            // but does not allow access to privates members in declaring type
            IJavaReferenceType receivingType = frame.getReferenceType();
            // currently disabled - see bugs 99416 and 106492
            // if (frame.isStatic()) {
            // receivingType= frame.getReferenceType();
            // } else {
            // receivingType= (IJavaReferenceType)
            // frame.getThis().getJavaType();
            // }
            unit = parseCompilationUnit(mapper.getSource(receivingType, frame.getLineNumber(), javaProject, frame.isStatic()).toCharArray(), mapper.getCompilationUnitName(), javaProject);
        } catch (CoreException e) {
            InstructionSequence expression = new InstructionSequence(snippet);
            expression.addError(e.getStatus().getMessage());
            return expression;
        }
        return createExpressionFromAST(snippet, mapper, unit);
    }

    private CompilationUnit parseCompilationUnit(char[] source, String unitName, IJavaProject project) {
        ASTParser parser = ASTParser.newParser(AST.JLS8);
        parser.setSource(source);
        parser.setUnitName(unitName);
        parser.setProject(project);
        parser.setResolveBindings(true);
        Map<String, String> options = EvaluationSourceGenerator.getCompilerOptions(project);
        parser.setCompilerOptions(options);
        return (CompilationUnit) parser.createAST(null);
    }

    // ******
    // to hide problems with local variable declare as instance of Local Types
    private boolean isLocalType(String typeName) {
        //$NON-NLS-1$
        StringTokenizer strTok = new StringTokenizer(typeName, "$");
        strTok.nextToken();
        while (strTok.hasMoreTokens()) {
            char char0 = strTok.nextToken().charAt(0);
            if ('0' <= char0 && char0 <= '9') {
                return true;
            }
        }
        return false;
    }

    // ******
    /**
	 * Returns a compiled expression for an evaluation in the context of an
	 * array as a receiver.
	 */
    private ICompiledExpression getCompiledExpression(String snippet, IJavaArrayType arrayType) {
        EvaluationSourceGenerator mapper = null;
        CompilationUnit unit = null;
        try {
            IJavaProject javaProject = getJavaProject();
            // replace all occurrences of 'this' with '_a_t'
            String newSnippet = replaceThisReferences(snippet);
            int dimension = 1;
            IJavaType componentType = arrayType.getComponentType();
            while (componentType instanceof IJavaArrayType) {
                componentType = ((IJavaArrayType) componentType).getComponentType();
                dimension++;
            }
            // Primitive arrays are evaluated in the context of Object.
            // Arrays with a base component type of a class or interface are
            // treated
            // as Object arrays and evaluated in Object.
            //$NON-NLS-1$
            String recTypeName = "java.lang.Object";
            String typeName = arrayType.getName();
            if (componentType instanceof IJavaReferenceType) {
                StringBuffer buf = new StringBuffer();
                //$NON-NLS-1$
                buf.append(//$NON-NLS-1$
                "java.lang.Object");
                for (int i = 0; i < dimension; i++) {
                    //$NON-NLS-1$
                    buf.append("[]");
                }
                typeName = buf.toString();
            }
            String[] localTypesNames = new String[] { typeName };
            String[] localVariables = new String[] { ArrayRuntimeContext.ARRAY_THIS_VARIABLE };
            mapper = new EvaluationSourceGenerator(localTypesNames, localVariables, newSnippet, getJavaProject());
            int index = typeName.indexOf('$');
            // type so type is visible
            if (index >= 0) {
                recTypeName = typeName.substring(0, index);
            }
            IJavaType[] javaTypes = getDebugTarget().getJavaTypes(recTypeName);
            if (javaTypes.length > 0) {
                IJavaReferenceType recType = (IJavaReferenceType) javaTypes[0];
                unit = parseCompilationUnit(mapper.getSource(recType, -1, getJavaProject(), false).toCharArray(), mapper.getCompilationUnitName(), javaProject);
            } else {
                IStatus status = new Status(IStatus.ERROR, JDIDebugPlugin.getUniqueIdentifier(), JDIDebugPlugin.ERROR, EvaluationEngineMessages.ASTEvaluationEngine_1, null);
                throw new CoreException(status);
            }
        } catch (CoreException e) {
            InstructionSequence expression = new InstructionSequence(snippet);
            expression.addError(e.getStatus().getMessage());
            return expression;
        }
        return createExpressionFromAST(snippet, mapper, unit);
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jdt.debug.eval.IAstEvaluationEngine#getCompiledExpression
	 * (java.lang.String, org.eclipse.jdt.debug.core.IJavaObject)
	 */
    @Override
    public ICompiledExpression getCompiledExpression(String snippet, IJavaObject thisContext) {
        try {
            if (thisContext instanceof IJavaArray) {
                return getCompiledExpression(snippet, (IJavaArrayType) thisContext.getJavaType());
            }
            return getCompiledExpression(snippet, (IJavaReferenceType) thisContext.getJavaType());
        } catch (DebugException e) {
            InstructionSequence expression = new InstructionSequence(snippet);
            expression.addError(e.getStatus().getMessage());
            return expression;
        }
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jdt.debug.eval.IAstEvaluationEngine#getCompiledExpression
	 * (java.lang.String, org.eclipse.jdt.debug.core.IJavaType)
	 */
    @Override
    public ICompiledExpression getCompiledExpression(String snippet, IJavaReferenceType type) {
        if (type instanceof IJavaArrayType) {
            return getCompiledExpression(snippet, (IJavaArrayType) type);
        }
        IJavaProject javaProject = getJavaProject();
        EvaluationSourceGenerator mapper = null;
        CompilationUnit unit = null;
        mapper = new EvaluationSourceGenerator(new String[0], new String[0], snippet, getJavaProject());
        try {
            unit = parseCompilationUnit(mapper.getSource(type, -1, javaProject, false).toCharArray(), mapper.getCompilationUnitName(), javaProject);
        } catch (CoreException e) {
            InstructionSequence expression = new InstructionSequence(snippet);
            expression.addError(e.getStatus().getMessage());
            return expression;
        }
        return createExpressionFromAST(snippet, mapper, unit);
    }

    /**
	 * Creates a compiled expression for the given snippet using the given
	 * mapper and compilation unit (AST).
	 * 
	 * @param snippet
	 *            the code snippet to be compiled
	 * @param mapper
	 *            the object which will be used to create the expression
	 * @param unit
	 *            the compilation unit (AST) generated for the snippet
	 */
    private ICompiledExpression createExpressionFromAST(String snippet, EvaluationSourceGenerator mapper, CompilationUnit unit) {
        IProblem[] problems = unit.getProblems();
        if (problems.length != 0) {
            boolean snippetError = false;
            boolean runMethodError = false;
            InstructionSequence errorSequence = new InstructionSequence(snippet);
            int codeSnippetStart = mapper.getSnippetStart();
            int codeSnippetEnd = codeSnippetStart + mapper.getSnippet().length();
            int runMethodStart = mapper.getRunMethodStart();
            int runMethodEnd = runMethodStart + mapper.getRunMethodLength();
            for (IProblem problem : problems) {
                int errorOffset = problem.getSourceStart();
                int problemId = problem.getID();
                if (problemId == IProblem.IsClassPathCorrect) {
                    errorSequence.addError(problem.getMessage());
                    snippetError = true;
                }
                if (problemId == IProblem.VoidMethodReturnsValue || problemId == IProblem.NotVisibleMethod || problemId == IProblem.NotVisibleConstructor || problemId == IProblem.NotVisibleField || problemId == IProblem.NotVisibleType) {
                    continue;
                }
                if (problem.isError()) {
                    if (codeSnippetStart <= errorOffset && errorOffset <= codeSnippetEnd) {
                        errorSequence.addError(problem.getMessage());
                        snippetError = true;
                    } else if (runMethodStart <= errorOffset && errorOffset <= runMethodEnd) {
                        runMethodError = true;
                    }
                }
            }
            if (snippetError || runMethodError) {
                if (runMethodError) {
                    errorSequence.addError(EvaluationEngineMessages.ASTEvaluationEngine_Evaluations_must_contain_either_an_expression_or_a_block_of_well_formed_statements_1);
                }
                return errorSequence;
            }
        }
        ASTInstructionCompiler visitor = new ASTInstructionCompiler(mapper.getSnippetStart(), snippet);
        unit.accept(visitor);
        return visitor.getInstructions();
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.debug.eval.IEvaluationEngine#getJavaProject()
	 */
    @Override
    public IJavaProject getJavaProject() {
        return fProject;
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.debug.eval.IEvaluationEngine#getDebugTarget()
	 */
    @Override
    public IJavaDebugTarget getDebugTarget() {
        return fDebugTarget;
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.debug.eval.IEvaluationEngine#dispose()
	 */
    @Override
    public void dispose() {
    }

    class EvalRunnable implements Runnable {

        private InstructionSequence fExpression;

        private IJavaThread fThread;

        private int fEvaluationDetail;

        private boolean fHitBreakpoints;

        private IRuntimeContext fContext;

        private IEvaluationListener fListener;

        public  EvalRunnable(InstructionSequence expression, IJavaThread thread, IRuntimeContext context, IEvaluationListener listener, int evaluationDetail, boolean hitBreakpoints) {
            fExpression = expression;
            fThread = thread;
            fContext = context;
            fListener = listener;
            fEvaluationDetail = evaluationDetail;
            fHitBreakpoints = hitBreakpoints;
        }

        @Override
        public void run() {
            if (JDIDebugOptions.DEBUG_AST_EVAL) {
                StringBuffer buf = new StringBuffer();
                buf.append(JDIDebugOptions.FORMAT.format(new Date()));
                //$NON-NLS-1$
                buf.append(//$NON-NLS-1$
                " : AST Evaluation");
                //$NON-NLS-1$
                buf.append(//$NON-NLS-1$
                "\n\tExpression: ");
                buf.append(fExpression.getSnippet());
                //$NON-NLS-1$
                buf.append(//$NON-NLS-1$
                "\n\tThread: ");
                try {
                    String name = fThread.getName();
                    buf.append('[');
                    buf.append(name);
                    //$NON-NLS-1$
                    buf.append("] ");
                } catch (DebugException e) {
                }
                buf.append(fThread.toString());
                //$NON-NLS-1$
                buf.append(//$NON-NLS-1$
                "\n\tDetail: ");
                if (fEvaluationDetail == DebugEvent.EVALUATION) {
                    //$NON-NLS-1$
                    buf.append(//$NON-NLS-1$
                    "EVALUATION");
                } else if (fEvaluationDetail == DebugEvent.EVALUATION_IMPLICIT) {
                    buf.append("EVALUATION_IMPLICIT");
                } else {
                    buf.append(fEvaluationDetail);
                }
                //$NON-NLS-1$
                buf.append(//$NON-NLS-1$
                " Hit Breakpoints: ");
                buf.append(fHitBreakpoints);
                JDIDebugOptions.trace(buf.toString());
            }
            EvaluationResult result = new EvaluationResult(ASTEvaluationEngine.this, fExpression.getSnippet(), fThread);
            if (fExpression.hasErrors()) {
                String[] errors = fExpression.getErrorMessages();
                for (String error : errors) {
                    result.addError(error);
                }
                evaluationFinished(result);
                if (JDIDebugOptions.DEBUG_AST_EVAL) {
                    StringBuffer buf = new StringBuffer();
                    //$NON-NLS-1$
                    buf.append(//$NON-NLS-1$
                    "\tErrors: ");
                    for (int i = 0; i < errors.length; i++) {
                        if (i > 0) {
                            buf.append('\n');
                        }
                        //$NON-NLS-1$
                        buf.append("\t\t");
                        buf.append(errors[i]);
                    }
                    JDIDebugOptions.trace(buf.toString());
                }
                return;
            }
            final Interpreter interpreter = new Interpreter(fExpression, fContext);
            class EvaluationRunnable implements IEvaluationRunnable, ITerminate {

                CoreException fException;

                boolean fTerminated = false;

                @Override
                public void run(IJavaThread jt, IProgressMonitor pm) {
                    EventFilter filter = new EventFilter();
                    try {
                        DebugPlugin.getDefault().addDebugEventFilter(filter);
                        interpreter.execute();
                    } catch (CoreException exception) {
                        fException = exception;
                        if (fEvaluationDetail == DebugEvent.EVALUATION && exception.getStatus().getException() instanceof InvocationException) {
                            InvocationException invocationException = (InvocationException) exception.getStatus().getException();
                            ObjectReference exObject = invocationException.exception();
                            IJavaObject modelObject = (IJavaObject) JDIValue.createValue((JDIDebugTarget) getDebugTarget(), exObject);
                            try {
                                modelObject.sendMessage("printStackTrace", "()V", null, jt, false);
                            } catch (DebugException e) {
                            }
                        }
                    } finally {
                        DebugPlugin.getDefault().removeDebugEventFilter(filter);
                    }
                }

                @Override
                public void terminate() {
                    fTerminated = true;
                    interpreter.stop();
                }

                @Override
                public boolean canTerminate() {
                    return true;
                }

                @Override
                public boolean isTerminated() {
                    return false;
                }

                public CoreException getException() {
                    return fException;
                }
            }
            EvaluationRunnable er = new EvaluationRunnable();
            CoreException exception = null;
            long start = System.currentTimeMillis();
            try {
                fThread.runEvaluation(er, null, fEvaluationDetail, fHitBreakpoints);
            } catch (DebugException e) {
                exception = e;
            }
            long end = System.currentTimeMillis();
            IJavaValue value = interpreter.getResult();
            if (exception == null) {
                exception = er.getException();
            }
            result.setTerminated(er.fTerminated);
            if (exception != null) {
                if (JDIDebugOptions.DEBUG_AST_EVAL) {
                    StringBuffer buf = new StringBuffer();
                    //$NON-NLS-1$
                    buf.append(//$NON-NLS-1$
                    "\tException: ");
                    buf.append(exception.toString());
                    JDIDebugOptions.trace(buf.toString());
                }
                if (exception instanceof DebugException) {
                    result.setException((DebugException) exception);
                } else {
                    result.setException(new DebugException(exception.getStatus()));
                }
            } else {
                if (value != null) {
                    result.setValue(value);
                    if (JDIDebugOptions.DEBUG_AST_EVAL) {
                        StringBuffer buf = new StringBuffer();
                        //$NON-NLS-1$
                        buf.append(//$NON-NLS-1$
                        "\tResult: ");
                        buf.append(value);
                        JDIDebugOptions.trace(buf.toString());
                    }
                } else {
                    result.addError(EvaluationEngineMessages.ASTEvaluationEngine_An_unknown_error_occurred_during_evaluation);
                }
            }
            if (JDIDebugOptions.DEBUG_AST_EVAL) {
                StringBuffer buf = new StringBuffer();
                //$NON-NLS-1$
                buf.append(//$NON-NLS-1$
                "\tDuration: ");
                buf.append(end - start);
                //$NON-NLS-1$
                buf.append(//$NON-NLS-1$
                "ms");
                JDIDebugOptions.trace(buf.toString());
            }
            evaluationFinished(result);
        }

        private void evaluationFinished(IEvaluationResult result) {
            // only notify if plug-in not yet shutdown - bug# 8693
            if (JDIDebugPlugin.getDefault() != null) {
                fListener.evaluationComplete(result);
            }
        }
    }

    /**
	 * Replaces references to 'this' with the 'array_this' variable.
	 * 
	 * @param snippet
	 *            code snippet
	 * @return snippet with 'this' references replaced
	 */
    public static String replaceThisReferences(String snippet) {
        // replace all occurrences of 'this' with 'array_this'
        StringBuffer updatedSnippet = new StringBuffer();
        Matcher matcher = fgThisPattern.matcher(snippet);
        int start = 0;
        while (matcher.find()) {
            int end = matcher.start(2);
            updatedSnippet.append(snippet.substring(start, end));
            updatedSnippet.append(ArrayRuntimeContext.ARRAY_THIS_VARIABLE);
            start = end + 4;
        }
        if (start < snippet.length()) {
            updatedSnippet.append(snippet.substring(start, snippet.length()));
        }
        return updatedSnippet.toString();
    }
}
