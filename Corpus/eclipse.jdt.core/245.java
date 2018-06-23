/*******************************************************************************
 * Copyright (c) 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Kevin Pollet - SERLI - (kevin.pollet@serli.com) - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.compiler.apt.tests.processors.AnnotationProcessorTests;

import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleTypeVisitor6;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;

@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SupportedAnnotationTypes("annotation.GetType")
public class Bug340635Proc extends AbstractProcessor {

    private static final boolean ALLOW_OTHER_PROCESSORS_TO_PROCESS = false;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        final Types types = processingEnv.getTypeUtils();
        final Messager messager = processingEnv.getMessager();
        for (TypeElement annotation : annotations) {
            final Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(annotation);
            // process the annotations
            for (Element element : elements) {
                // class or interface hierarchy.
                if (element.getKind().isInterface() || element.getKind().isClass()) {
                    DeclaredType genericType = element.asType().accept(new GenericTypeVisitor(types), null);
                    DeclaredType erasedType = (DeclaredType) types.erasure(genericType);
                    StringBuffer message = new StringBuffer();
                    message.append("Erased type: " + erasedType);
                    message.append(" - type arguments: ");
                    for (TypeMirror typeArgument : erasedType.getTypeArguments()) {
                        message.append(typeArgument + ",");
                    }
                    messager.printMessage(Kind.WARNING, message.toString(), element);
                }
            }
        }
        return ALLOW_OTHER_PROCESSORS_TO_PROCESS;
    }

    private class GenericTypeVisitor extends SimpleTypeVisitor6<DeclaredType, Void> {

        private final Types types;

        public  GenericTypeVisitor(Types types) {
            this.types = types;
        }

        @Override
        public DeclaredType visitDeclared(DeclaredType t, Void p) {
            if (t.getTypeArguments().size() > 0) {
                return t;
            }
            for (TypeMirror superType : types.directSupertypes(t)) {
                DeclaredType tmp = superType.accept(this, p);
                if (tmp != null) {
                    return tmp;
                }
            }
            return null;
        }
    }
}
