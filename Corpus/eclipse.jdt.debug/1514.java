/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Holger Schill - Bug 455199 - [debug] Debugging doesn't work properly when inner classes are used
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.eval.ast.engine;

import java.util.Iterator;
import java.util.List;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.debug.core.IJavaReferenceType;
import org.eclipse.jdt.internal.debug.core.model.JDIReferenceType;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ClassNotPreparedException;
import com.sun.jdi.ClassType;
import com.sun.jdi.Field;
import com.sun.jdi.InterfaceType;
import com.sun.jdi.Method;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Type;

public class BinaryBasedSourceGenerator {

    //$NON-NLS-1$
    private static final String RUN_METHOD_NAME = "___run";

    //$NON-NLS-1$
    private static final String EVAL_METHOD_NAME = "___eval";

    //$NON-NLS-1$
    private static final String ANONYMOUS_CLASS_NAME = "___EvalClass";

    private String[] fLocalVariableTypeNames;

    private String[] fLocalVariableNames;

    private boolean fIsInStaticMethod;

    private StringBuffer fSource;

    private int fRunMethodStartOffset;

    private int fRunMethodLength;

    private int fCodeSnippetPosition;

    private String fCompilationUnitName;

    /**
	 * Level of source code to generate (major, minor). For example 1 and 4
	 * indicates 1.4.
	 */
    private int fSourceMajorLevel;

    private int fSourceMinorLevel;

    public  BinaryBasedSourceGenerator(String[] localTypesNames, String[] localVariables, boolean isInStaticMethod, String sourceLevel) {
        fLocalVariableTypeNames = localTypesNames;
        fLocalVariableNames = localVariables;
        fIsInStaticMethod = isInStaticMethod;
        int index = sourceLevel.indexOf('.');
        String num = sourceLevel.substring(0, index);
        fSourceMajorLevel = Integer.valueOf(num).intValue();
        num = sourceLevel.substring(index + 1);
        fSourceMinorLevel = Integer.valueOf(num).intValue();
    }

    /**
	 * Build source for an object value (instance context)
	 */
    public void buildSource(JDIReferenceType referenceType) {
        ReferenceType reference = (ReferenceType) referenceType.getUnderlyingType();
        fSource = buildTypeDeclaration(reference, buildRunMethod(reference), null);
    }

    /**
	 * Build source for a class type (static context)
	 */
    public void buildSourceStatic(IJavaReferenceType type) {
        Type underlyingType = ((JDIReferenceType) type).getUnderlyingType();
        if (!(underlyingType instanceof ReferenceType)) {
            return;
        }
        ReferenceType refType = (ReferenceType) underlyingType;
        fSource = buildTypeDeclaration(refType, buildRunMethod(refType), null, false);
        String packageName = getPackageName(refType.name());
        if (packageName != null) {
            //$NON-NLS-1$ //$NON-NLS-2$
            fSource.insert(0, "package " + packageName + ";\n");
            fCodeSnippetPosition += 10 + packageName.length();
        }
        fCompilationUnitName = getSimpleName(refType.name());
    }

    protected String getUniqueMethodName(String methodName, ReferenceType type) {
        List<Method> methods = type.methodsByName(methodName);
        while (!methods.isEmpty()) {
            methodName += '_';
            methods = type.methodsByName(methodName);
        }
        return methodName;
    }

    private StringBuffer buildRunMethod(ReferenceType type) {
        StringBuffer source = new StringBuffer();
        if (isInStaticMethod()) {
            //$NON-NLS-1$
            source.append("static ");
        }
        //$NON-NLS-1$
        source.append("void ");
        source.append(getUniqueMethodName(RUN_METHOD_NAME, type));
        source.append('(');
        for (int i = 0, length = fLocalVariableNames.length; i < length; i++) {
            source.append(getDotName(fLocalVariableTypeNames[i]));
            source.append(' ');
            source.append(fLocalVariableNames[i]);
            if (i + 1 < length) {
                //$NON-NLS-1$
                source.append(//$NON-NLS-1$
                ", ");
            }
        }
        //$NON-NLS-1$
        source.append(") throws Throwable {");
        source.append('\n');
        fCodeSnippetPosition = source.length();
        fRunMethodStartOffset = fCodeSnippetPosition;
        source.append('\n');
        source.append('}').append('\n');
        fRunMethodLength = source.length();
        return source;
    }

    private StringBuffer buildTypeDeclaration(ReferenceType referenceType, StringBuffer buffer, String nestedTypeName) {
        Field thisField = null;
        List<Field> fields = referenceType.visibleFields();
        for (Iterator<Field> iterator = fields.iterator(); iterator.hasNext(); ) {
            Field field = iterator.next();
            if (//$NON-NLS-1$
            field.name().startsWith("this$")) {
                thisField = field;
                break;
            }
        }
        StringBuffer source = buildTypeDeclaration(referenceType, buffer, nestedTypeName, thisField != null);
        if (thisField == null) {
            String packageName = getPackageName(referenceType.name());
            if (packageName != null) {
                //$NON-NLS-1$ //$NON-NLS-2$
                source.insert(0, "package " + packageName + ";\n");
                fCodeSnippetPosition += 10 + packageName.length();
            }
            if (isAnonymousTypeName(referenceType.name())) {
                fCompilationUnitName = ANONYMOUS_CLASS_NAME;
            } else {
                fCompilationUnitName = getSimpleName(referenceType.name());
            }
        } else {
            try {
                return buildTypeDeclaration((ReferenceType) thisField.type(), source, referenceType.name());
            } catch (ClassNotLoadedException e) {
            }
        }
        return source;
    }

    private StringBuffer buildTypeDeclaration(ReferenceType referenceType, StringBuffer buffer, String nestedTypeName, boolean hasEnclosingInstance) {
        StringBuffer source = new StringBuffer();
        String typeName = referenceType.name();
        boolean isAnonymousType = isAnonymousTypeName(typeName);
        if (isAnonymousType) {
            ClassType classType = (ClassType) referenceType;
            List<InterfaceType> interfaceList = classType.interfaces();
            String superClassName = classType.superclass().name();
            if (hasEnclosingInstance) {
                //$NON-NLS-1$
                source.append(//$NON-NLS-1$
                "void ");
                source.append(getUniqueMethodName(EVAL_METHOD_NAME, referenceType));
                //$NON-NLS-1$
                source.append(//$NON-NLS-1$
                "() {\nnew ");
                if (interfaceList.size() != 0) {
                    source.append(getDotName(interfaceList.get(0).name()));
                } else {
                    source.append(getDotName(superClassName));
                }
                //$NON-NLS-1$
                source.append(//$NON-NLS-1$
                "()");
            } else {
                //$NON-NLS-1$ //$NON-NLS-2$
                source.append("public class ").append(ANONYMOUS_CLASS_NAME).append(" ");
                if (interfaceList.size() != 0) {
                    source.append(" implements ").append(getDotName(//$NON-NLS-1$
                    interfaceList.get(0).name()));
                } else {
                    source.append(" extends ").append(getDotName(//$NON-NLS-1$
                    superClassName));
                }
            }
        } else {
            if (referenceType.isFinal()) {
                //$NON-NLS-1$
                source.append(//$NON-NLS-1$
                "final ");
            }
            if (referenceType.isStatic()) {
                //$NON-NLS-1$
                source.append(//$NON-NLS-1$
                "static ");
            }
            if (referenceType instanceof ClassType) {
                ClassType classType = (ClassType) referenceType;
                if (classType.isAbstract()) {
                    //$NON-NLS-1$
                    source.append(//$NON-NLS-1$
                    "abstract ");
                }
                //$NON-NLS-1$
                source.append(//$NON-NLS-1$
                "class ");
                source.append(getSimpleName(typeName)).append(' ');
                String genericSignature = referenceType.genericSignature();
                if (genericSignature != null && isSourceLevelGreaterOrEqual(1, 5)) {
                    String[] typeParameters = Signature.getTypeParameters(genericSignature);
                    if (typeParameters.length > 0) {
                        source.append('<');
                        source.append(Signature.getTypeVariable(typeParameters[0]));
                        String[] typeParameterBounds = Signature.getTypeParameterBounds(typeParameters[0]);
                        source.append(" extends ").append(//$NON-NLS-1$
                        Signature.toString(typeParameterBounds[0]).replace(//$NON-NLS-1$
                        '/', //$NON-NLS-1$
                        '.'));
                        for (int i = 1; i < typeParameterBounds.length; i++) {
                            source.append(" & ").append(//$NON-NLS-1$
                            Signature.toString(typeParameterBounds[i]).replace(//$NON-NLS-1$
                            '/', //$NON-NLS-1$
                            '.'));
                        }
                        for (int j = 1; j < typeParameters.length; j++) {
                            source.append(',').append(Signature.getTypeVariable(typeParameters[j]));
                            typeParameterBounds = Signature.getTypeParameterBounds(typeParameters[j]);
                            source.append(" extends ").append(//$NON-NLS-1$
                            Signature.toString(typeParameterBounds[0]).replace(//$NON-NLS-1$
                            '/', //$NON-NLS-1$
                            '.'));
                            for (int i = 1; i < typeParameterBounds.length; i++) {
                                source.append(" & ").append(//$NON-NLS-1$
                                Signature.toString(typeParameterBounds[i]).replace(//$NON-NLS-1$
                                '/', //$NON-NLS-1$
                                '.'));
                            }
                        }
                        //$NON-NLS-1$
                        source.append("> ");
                    }
                    String[] superClassInterfaces = SignatureExt.getTypeSuperClassInterfaces(genericSignature);
                    int length = superClassInterfaces.length;
                    if (length > 0) {
                        source.append("extends ").append(//$NON-NLS-1$
                        Signature.toString(superClassInterfaces[0]).replace(//$NON-NLS-1$
                        '/', //$NON-NLS-1$
                        '.'));
                        if (length > 1) {
                            source.append(" implements ").append(//$NON-NLS-1$
                            Signature.toString(superClassInterfaces[1]).replace(//$NON-NLS-1$
                            '/', //$NON-NLS-1$
                            '.'));
                            for (int i = 2; i < length; i++) {
                                source.append(',').append(Signature.toString(superClassInterfaces[1]));
                            }
                        }
                    }
                } else {
                    ClassType superClass = classType.superclass();
                    if (superClass != null) {
                        //$NON-NLS-1$
                        source.append("extends ").append(getDotName(superClass.name())).append(//$NON-NLS-1$
                        ' ');
                    }
                    List<InterfaceType> interfaces;
                    try {
                        interfaces = classType.interfaces();
                    } catch (ClassNotPreparedException e) {
                        return new StringBuffer();
                    }
                    if (interfaces.size() != 0) {
                        //$NON-NLS-1$
                        source.append(//$NON-NLS-1$
                        "implements ");
                        Iterator<InterfaceType> iterator = interfaces.iterator();
                        InterfaceType interface_ = iterator.next();
                        source.append(getDotName(interface_.name()));
                        while (iterator.hasNext()) {
                            source.append(',').append(getDotName(iterator.next().name()));
                        }
                    }
                }
            } else if (referenceType instanceof InterfaceType) {
                if (buffer != null) {
                    source.append("abstract class ");
                    source.append(getSimpleName(typeName)).append("___ implements ");
                    //$NON-NLS-1$
                    source.append(typeName.replace('$', '.')).append(//$NON-NLS-1$
                    " {\n");
                    fCodeSnippetPosition += source.length();
                    //$NON-NLS-1$
                    source.append(buffer).append(//$NON-NLS-1$
                    "}\n");
                    return source;
                }
                //$NON-NLS-1$
                source.append(//$NON-NLS-1$
                "interface ");
                source.append(getSimpleName(typeName));
            }
        }
        //$NON-NLS-1$
        source.append(" {\n");
        if (buffer != null && !(referenceType instanceof InterfaceType)) {
            fCodeSnippetPosition += source.length();
            source.append(buffer);
        }
        List<Field> fields = referenceType.fields();
        for (Iterator<Field> iterator = fields.iterator(); iterator.hasNext(); ) {
            Field field = iterator.next();
            if (//$NON-NLS-1$
            !field.name().startsWith("this$")) {
                source.append(buildFieldDeclaration(field));
            }
        }
        List<Method> methods = referenceType.methods();
        for (Iterator<Method> iterator = methods.iterator(); iterator.hasNext(); ) {
            Method method = iterator.next();
            if (!method.isConstructor() && !method.isStaticInitializer() && !method.isBridge()) {
                source.append(buildMethodDeclaration(method));
            }
        }
        List<ReferenceType> nestedTypes = referenceType.nestedTypes();
        if (nestedTypeName == null) {
            for (Iterator<ReferenceType> iterator = nestedTypes.iterator(); iterator.hasNext(); ) {
                ReferenceType nestedType = iterator.next();
                if (isADirectInnerType(typeName, nestedType.name())) {
                    source.append(buildTypeDeclaration(nestedType, null, null, true));
                }
            }
        } else {
            for (Iterator<ReferenceType> iterator = nestedTypes.iterator(); iterator.hasNext(); ) {
                ReferenceType nestedType = iterator.next();
                if (!nestedTypeName.equals(nestedType.name()) && isADirectInnerType(typeName, nestedType.name())) {
                    source.append(buildTypeDeclaration(nestedType, null, null, true));
                }
            }
        }
        if (isAnonymousType & hasEnclosingInstance) {
            //$NON-NLS-1$
            source.append("};\n");
        }
        //$NON-NLS-1$
        source.append("}\n");
        return source;
    }

    private StringBuffer buildFieldDeclaration(Field field) {
        StringBuffer source = new StringBuffer();
        if (field.isFinal()) {
            //$NON-NLS-1$
            source.append("final ");
        }
        if (field.isStatic()) {
            //$NON-NLS-1$
            source.append("static ");
        }
        if (field.isPublic()) {
            //$NON-NLS-1$
            source.append("public ");
        } else if (field.isPrivate()) {
            //$NON-NLS-1$
            source.append("private ");
        } else if (field.isProtected()) {
            //$NON-NLS-1$
            source.append("protected ");
        }
        source.append(getDotName(field.typeName())).append(' ').append(field.name()).append(';').append('\n');
        return source;
    }

    private StringBuffer buildMethodDeclaration(Method method) {
        StringBuffer source = new StringBuffer();
        if (method.isFinal()) {
            //$NON-NLS-1$
            source.append("final ");
        }
        if (method.isStatic()) {
            //$NON-NLS-1$
            source.append("static ");
        }
        if (method.isNative()) {
            //$NON-NLS-1$
            source.append("native ");
        } else if (method.isAbstract()) {
            //$NON-NLS-1$
            source.append("abstract ");
        }
        if (method.isPublic()) {
            //$NON-NLS-1$
            source.append("public ");
        } else if (method.isPrivate()) {
            //$NON-NLS-1$
            source.append("private ");
        } else if (method.isProtected()) {
            //$NON-NLS-1$
            source.append("protected ");
        }
        String genericSignature = method.genericSignature();
        if (genericSignature != null && isSourceLevelGreaterOrEqual(1, 5)) {
            String[] typeParameters = Signature.getTypeParameters(genericSignature);
            if (typeParameters.length > 0) {
                source.append('<');
                source.append(Signature.getTypeVariable(typeParameters[0]));
                String[] typeParameterBounds = Signature.getTypeParameterBounds(typeParameters[0]);
                //$NON-NLS-1$
                source.append(" extends ").append(//$NON-NLS-1$
                Signature.toString(typeParameterBounds[0]).replace('/', '.'));
                for (int i = 1; i < typeParameterBounds.length; i++) {
                    source.append(" & ").append(//$NON-NLS-1$
                    Signature.toString(typeParameterBounds[i]).replace(//$NON-NLS-1$
                    '/', //$NON-NLS-1$
                    '.'));
                }
                for (int j = 1; j < typeParameters.length; j++) {
                    source.append(',').append(Signature.getTypeVariable(typeParameters[j]));
                    typeParameterBounds = Signature.getTypeParameterBounds(typeParameters[j]);
                    source.append(" extends ").append(//$NON-NLS-1$
                    Signature.toString(typeParameterBounds[0]).replace(//$NON-NLS-1$
                    '/', //$NON-NLS-1$
                    '.'));
                    for (int i = 1; i < typeParameterBounds.length; i++) {
                        source.append(" & ").append(//$NON-NLS-1$
                        Signature.toString(typeParameterBounds[i]).replace(//$NON-NLS-1$
                        '/', //$NON-NLS-1$
                        '.'));
                    }
                }
                //$NON-NLS-1$
                source.append(//$NON-NLS-1$
                "> ");
            }
            source.append(Signature.toString(Signature.getReturnType(genericSignature)).replace('/', '.')).append(' ').append(method.name()).append('(');
            String[] parameterTypes = Signature.getParameterTypes(genericSignature);
            int i = 0;
            if (parameterTypes.length != 0) {
                source.append(Signature.toString(parameterTypes[0]).replace('/', '.')).append(//$NON-NLS-1$
                " arg").append(//$NON-NLS-1$
                i++);
                if (method.isVarArgs()) {
                    for (int j = 1; j < parameterTypes.length - 1; j++) {
                        source.append(',').append(Signature.toString(parameterTypes[j]).replace('/', '.')).append(//$NON-NLS-1$
                        " arg").append(i++);
                    }
                    String typeName = Signature.toString(parameterTypes[parameterTypes.length - 1]).replace('/', '.');
                    source.append(',').append(typeName.substring(0, typeName.length() - 2)).append("...").append(" arg").append(//$NON-NLS-1$ //$NON-NLS-2$
                    i++);
                } else {
                    for (int j = 1; j < parameterTypes.length; j++) {
                        source.append(',').append(Signature.toString(parameterTypes[j]).replace('/', '.')).append(//$NON-NLS-1$
                        " arg").append(i++);
                    }
                }
            }
            source.append(')');
        } else {
            source.append(getDotName(method.returnTypeName())).append(' ').append(method.name()).append('(');
            List<String> arguments = method.argumentTypeNames();
            int i = 0;
            if (arguments.size() != 0) {
                Iterator<String> iterator = arguments.iterator();
                source.append(getDotName(iterator.next())).append(//$NON-NLS-1$
                " arg").append(//$NON-NLS-1$
                i++);
                if (method.isVarArgs()) {
                    while (iterator.hasNext()) {
                        source.append(',');
                        String argName = getDotName(iterator.next());
                        if (!iterator.hasNext()) {
                            source.append(argName.substring(0, argName.length() - 2)).append(//$NON-NLS-1$
                            "...");
                        } else {
                            source.append(argName);
                        }
                        //$NON-NLS-1$
                        source.append(" arg").append(//$NON-NLS-1$
                        i++);
                    }
                } else {
                    while (iterator.hasNext()) {
                        source.append(',').append(getDotName(iterator.next())).append(//$NON-NLS-1$
                        " arg").append(i++);
                    }
                }
            }
            source.append(')');
        }
        if (method.isAbstract() || method.isNative()) {
            // No body for abstract and native methods
            //$NON-NLS-1$
            source.append(";\n");
        } else {
            source.append('{').append('\n');
            source.append(getReturnStatement(method.returnTypeName()));
            source.append('}').append('\n');
        }
        return source;
    }

    private String getReturnStatement(String returnTypeName) {
        String typeName = getSimpleName(returnTypeName);
        if (typeName.charAt(typeName.length() - 1) == ']') {
            //$NON-NLS-1$
            return "return null;\n";
        }
        switch(typeName.charAt(0)) {
            case 'v':
                //$NON-NLS-1$
                return "";
            case 'b':
                if (typeName.length() >= 1 && typeName.charAt(1) == 'o') {
                    return "return false;\n";
                }
            case 's':
            case 'c':
            case 'i':
            case 'l':
            case 'd':
            case 'f':
                //$NON-NLS-1$
                return "return 0;\n";
            default:
                //$NON-NLS-1$
                return "return null;\n";
        }
    }

    private String getDotName(String typeName) {
        return typeName.replace('$', '.');
    }

    private boolean isAnonymousTypeName(String typeName) {
        char char0 = getSimpleName(typeName).charAt(0);
        return '0' <= char0 && char0 <= '9';
    }

    private String getSimpleName(String qualifiedName) {
        int pos = qualifiedName.lastIndexOf('$');
        if (pos == -1) {
            pos = qualifiedName.lastIndexOf('.');
        }
        return ((pos == -1) ? qualifiedName : qualifiedName.substring(pos + 1));
    }

    private String getPackageName(String qualifiedName) {
        int pos = qualifiedName.lastIndexOf('.');
        return ((pos == -1) ? null : qualifiedName.substring(0, pos));
    }

    private boolean isADirectInnerType(String typeName, String nestedTypeName) {
        String end = nestedTypeName.substring(typeName.length() + 1);
        return end.indexOf('$') == -1;
    }

    private boolean isInStaticMethod() {
        return fIsInStaticMethod;
    }

    public StringBuffer getSource() {
        return fSource;
    }

    public int getCodeSnippetPosition() {
        return fCodeSnippetPosition;
    }

    public String getCompilationUnitName() {
        return fCompilationUnitName;
    }

    public int getSnippetStart() {
        return fCodeSnippetPosition - 2;
    }

    public int getRunMethodStart() {
        return fCodeSnippetPosition - fRunMethodStartOffset;
    }

    public int getRunMethodLength() {
        return fRunMethodLength;
    }

    /**
	 * Returns whether the source to be generated is greater than or equal to
	 * the given source level.
	 * 
	 * @param major
	 *            major level - e.g. 1 from 1.4
	 * @param minor
	 *            minor level - e.g. 4 from 1.4
	 * @return
	 */
    public boolean isSourceLevelGreaterOrEqual(int major, int minor) {
        return (fSourceMajorLevel > major) || (fSourceMajorLevel == major && fSourceMinorLevel >= minor);
    }
}
