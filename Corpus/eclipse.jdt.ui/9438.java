/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Benjamin Muskalla - [quick fix] Create Method in void context should 'box' void. - https://bugs.eclipse.org/bugs/show_bug.cgi?id=107985
 *******************************************************************************/
package org.eclipse.jdt.internal.ui.text.correction.proposals;

import java.util.List;
import org.eclipse.swt.graphics.Image;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.NamingConventions;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite.ImportRewriteContext;
import org.eclipse.jdt.internal.corext.codemanipulation.ContextSensitiveImportRewriteContext;
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility;
import org.eclipse.jdt.internal.corext.dom.ASTNodes;
import org.eclipse.jdt.internal.corext.dom.Bindings;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.jdt.internal.corext.util.JdtFlags;
import org.eclipse.jdt.internal.ui.text.correction.ASTResolving;
import org.eclipse.jdt.internal.ui.text.correction.ModifierCorrectionSubProcessor;

public class NewMethodCorrectionProposal extends AbstractMethodCorrectionProposal {

    //$NON-NLS-1$
    private static final String KEY_NAME = "name";

    //$NON-NLS-1$
    private static final String KEY_TYPE = "type";

    private List<Expression> fArguments;

    //	invocationNode is MethodInvocation, ConstructorInvocation, SuperConstructorInvocation, ClassInstanceCreation, SuperMethodInvocation
    public  NewMethodCorrectionProposal(String label, ICompilationUnit targetCU, ASTNode invocationNode, List<Expression> arguments, ITypeBinding binding, int relevance, Image image) {
        super(label, targetCU, invocationNode, binding, relevance, image);
        fArguments = arguments;
    }

    private int evaluateModifiers(ASTNode targetTypeDecl) {
        if (getSenderBinding().isAnnotation()) {
            return 0;
        }
        boolean isTargetInterface = getSenderBinding().isInterface();
        if (isTargetInterface && !JavaModelUtil.is18OrHigher(getCompilationUnit().getJavaProject())) {
            // only abstract methods are allowed for interface present in less than Java 1.8
            return getInterfaceMethodModifiers(targetTypeDecl, true);
        }
        ASTNode invocationNode = getInvocationNode();
        if (invocationNode instanceof MethodInvocation) {
            int modifiers = 0;
            Expression expression = ((MethodInvocation) invocationNode).getExpression();
            if (expression != null) {
                if (expression instanceof Name && ((Name) expression).resolveBinding().getKind() == IBinding.TYPE) {
                    modifiers |= Modifier.STATIC;
                }
            } else if (ASTResolving.isInStaticContext(invocationNode)) {
                modifiers |= Modifier.STATIC;
            }
            ASTNode node = ASTResolving.findParentType(invocationNode);
            boolean isParentInterface = node instanceof TypeDeclaration && ((TypeDeclaration) node).isInterface();
            if (isTargetInterface || isParentInterface) {
                if (expression == null && !targetTypeDecl.equals(node)) {
                    modifiers |= Modifier.STATIC;
                    if (isTargetInterface) {
                        modifiers |= getInterfaceMethodModifiers(targetTypeDecl, false);
                    } else {
                        modifiers |= Modifier.PROTECTED;
                    }
                } else if (modifiers == Modifier.STATIC) {
                    modifiers = getInterfaceMethodModifiers(targetTypeDecl, false) | Modifier.STATIC;
                } else {
                    modifiers = getInterfaceMethodModifiers(targetTypeDecl, true);
                }
            } else if (targetTypeDecl.equals(node)) {
                modifiers |= Modifier.PRIVATE;
            } else if (node instanceof AnonymousClassDeclaration && ASTNodes.isParent(node, targetTypeDecl)) {
                modifiers |= Modifier.PROTECTED;
                if (ASTResolving.isInStaticContext(node) && expression == null) {
                    modifiers |= Modifier.STATIC;
                }
            } else {
                modifiers |= Modifier.PUBLIC;
            }
            return modifiers;
        }
        return Modifier.PUBLIC;
    }

    private int getInterfaceMethodModifiers(ASTNode targetTypeDecl, boolean createAbstractMethod) {
        // for interface and annotation members copy the modifiers from an existing member
        if (targetTypeDecl instanceof TypeDeclaration) {
            TypeDeclaration type = (TypeDeclaration) targetTypeDecl;
            MethodDeclaration[] methodDecls = type.getMethods();
            if (methodDecls.length > 0) {
                if (createAbstractMethod) {
                    for (MethodDeclaration methodDeclaration : methodDecls) {
                        IMethodBinding methodBinding = methodDeclaration.resolveBinding();
                        if (methodBinding != null && JdtFlags.isAbstract(methodBinding)) {
                            return methodDeclaration.getModifiers();
                        }
                    }
                }
                return methodDecls[0].getModifiers() & Modifier.PUBLIC;
            }
            List<BodyDeclaration> bodyDecls = type.bodyDeclarations();
            if (bodyDecls.size() > 0) {
                return bodyDecls.get(0).getModifiers() & Modifier.PUBLIC;
            }
        }
        return 0;
    }

    @Override
    protected void addNewModifiers(ASTRewrite rewrite, ASTNode targetTypeDecl, List<IExtendedModifier> modifiers) {
        modifiers.addAll(rewrite.getAST().newModifiers(evaluateModifiers(targetTypeDecl)));
        ModifierCorrectionSubProcessor.installLinkedVisibilityProposals(getLinkedProposalModel(), rewrite, modifiers, getSenderBinding().isInterface());
    }

    @Override
    protected boolean isConstructor() {
        ASTNode node = getInvocationNode();
        return node.getNodeType() != ASTNode.METHOD_INVOCATION && node.getNodeType() != ASTNode.SUPER_METHOD_INVOCATION;
    }

    @Override
    protected SimpleName getNewName(ASTRewrite rewrite) {
        ASTNode invocationNode = getInvocationNode();
        String name;
        if (invocationNode instanceof MethodInvocation) {
            name = ((MethodInvocation) invocationNode).getName().getIdentifier();
        } else if (invocationNode instanceof SuperMethodInvocation) {
            name = ((SuperMethodInvocation) invocationNode).getName().getIdentifier();
        } else {
            // name of the class
            name = getSenderBinding().getName();
        }
        AST ast = rewrite.getAST();
        SimpleName newNameNode = ast.newSimpleName(name);
        addLinkedPosition(rewrite.track(newNameNode), false, KEY_NAME);
        ASTNode invocationName = getInvocationNameNode();
        if (// in the same CU
        invocationName != null && invocationName.getAST() == ast) {
            addLinkedPosition(rewrite.track(invocationName), true, KEY_NAME);
        }
        return newNameNode;
    }

    private ASTNode getInvocationNameNode() {
        ASTNode node = getInvocationNode();
        if (node instanceof MethodInvocation) {
            return ((MethodInvocation) node).getName();
        } else if (node instanceof SuperMethodInvocation) {
            return ((SuperMethodInvocation) node).getName();
        } else if (node instanceof ClassInstanceCreation) {
            Type type = ((ClassInstanceCreation) node).getType();
            while (type instanceof ParameterizedType) {
                type = ((ParameterizedType) type).getType();
            }
            return type;
        }
        return null;
    }

    @Override
    protected Type getNewMethodType(ASTRewrite rewrite) throws CoreException {
        ASTNode node = getInvocationNode();
        AST ast = rewrite.getAST();
        Type newTypeNode = null;
        ITypeBinding[] otherProposals = null;
        ImportRewriteContext importRewriteContext = new ContextSensitiveImportRewriteContext(node, getImportRewrite());
        if (node.getParent() instanceof MethodInvocation) {
            MethodInvocation parent = (MethodInvocation) node.getParent();
            if (parent.getExpression() == node) {
                ITypeBinding[] bindings = ASTResolving.getQualifierGuess(node.getRoot(), parent.getName().getIdentifier(), parent.arguments(), getSenderBinding());
                if (bindings.length > 0) {
                    newTypeNode = getImportRewrite().addImport(bindings[0], ast, importRewriteContext);
                    otherProposals = bindings;
                }
            }
        }
        if (newTypeNode == null) {
            ITypeBinding binding = ASTResolving.guessBindingForReference(node);
            if (binding != null && binding.isWildcardType()) {
                binding = ASTResolving.normalizeWildcardType(binding, false, ast);
            }
            if (binding != null) {
                newTypeNode = getImportRewrite().addImport(binding, ast, importRewriteContext);
            } else {
                ASTNode parent = node.getParent();
                if (parent instanceof ExpressionStatement) {
                    newTypeNode = ast.newPrimitiveType(PrimitiveType.VOID);
                } else {
                    newTypeNode = ASTResolving.guessTypeForReference(ast, node);
                    if (newTypeNode == null) {
                        newTypeNode = ast.newSimpleType(//$NON-NLS-1$
                        ast.newSimpleName(//$NON-NLS-1$
                        "Object"));
                    }
                }
            }
        }
        addLinkedPosition(rewrite.track(newTypeNode), false, KEY_TYPE);
        if (otherProposals != null) {
            for (int i = 0; i < otherProposals.length; i++) {
                addLinkedPositionProposal(KEY_TYPE, otherProposals[i]);
            }
        }
        return newTypeNode;
    }

    @Override
    protected void addNewParameters(ASTRewrite rewrite, List<String> takenNames, List<SingleVariableDeclaration> params) throws CoreException {
        AST ast = rewrite.getAST();
        List<Expression> arguments = fArguments;
        ImportRewriteContext context = new ContextSensitiveImportRewriteContext(ASTResolving.findParentBodyDeclaration(getInvocationNode()), getImportRewrite());
        for (int i = 0; i < arguments.size(); i++) {
            Expression elem = arguments.get(i);
            SingleVariableDeclaration param = ast.newSingleVariableDeclaration();
            // argument type
            //$NON-NLS-1$
            String argTypeKey = "arg_type_" + i;
            Type type = evaluateParameterType(ast, elem, argTypeKey, context);
            param.setType(type);
            // argument name
            //$NON-NLS-1$
            String argNameKey = "arg_name_" + i;
            String name = evaluateParameterName(takenNames, elem, type, argNameKey);
            param.setName(ast.newSimpleName(name));
            params.add(param);
            addLinkedPosition(rewrite.track(param.getType()), false, argTypeKey);
            addLinkedPosition(rewrite.track(param.getName()), false, argNameKey);
        }
    }

    private Type evaluateParameterType(AST ast, Expression elem, String key, ImportRewriteContext context) {
        ITypeBinding binding = Bindings.normalizeTypeBinding(elem.resolveTypeBinding());
        if (binding != null && binding.isWildcardType()) {
            binding = ASTResolving.normalizeWildcardType(binding, true, ast);
        }
        if (binding != null) {
            ITypeBinding[] typeProposals = ASTResolving.getRelaxingTypes(ast, binding);
            for (int i = 0; i < typeProposals.length; i++) {
                addLinkedPositionProposal(key, typeProposals[i]);
            }
            return getImportRewrite().addImport(binding, ast, context);
        }
        //$NON-NLS-1$
        return ast.newSimpleType(ast.newSimpleName("Object"));
    }

    private String evaluateParameterName(List<String> takenNames, Expression argNode, Type type, String key) {
        IJavaProject project = getCompilationUnit().getJavaProject();
        String[] names = StubUtility.getVariableNameSuggestions(NamingConventions.VK_PARAMETER, project, type, argNode, takenNames);
        for (int i = 0; i < names.length; i++) {
            addLinkedPositionProposal(key, names[i], null);
        }
        String favourite = names[0];
        takenNames.add(favourite);
        return favourite;
    }

    @Override
    protected void addNewExceptions(ASTRewrite rewrite, List<Type> exceptions) throws CoreException {
    }

    @Override
    protected void addNewTypeParameters(ASTRewrite rewrite, List<String> takenNames, List<TypeParameter> params) throws CoreException {
    }
}
