/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt McCutchen - partial fix for https://bugs.eclipse.org/bugs/show_bug.cgi?id=122995
 *     Karen Moore - fix for https://bugs.eclipse.org/bugs/show_bug.cgi?id=207411
 *     Stephan Herrmann <stephan@cs.tu-berlin.de> - Contributions for 
 *     							bug 185682 - Increment/decrement operators mark local variables as read
 *     							bug 186342 - [compiler][null] Using annotations for null checking
 *								bug 365519 - editorial cleanup after bug 186342 and bug 365387
 *								bug 374605 - Unreasonable warning for enum-based switch statements
 *								bug 384870 - [compiler] @Deprecated annotation not detected if preceded by other annotation
 *								bug 393719 - [compiler] inconsistent warnings on iteration variables
 *								Bug 392099 - [1.8][compiler][null] Apply null annotation on types for null analysis
 *								Bug 417295 - [1.8[[null] Massage type annotated null analysis to gel well with deep encoded type bindings.
 *								Bug 400874 - [1.8][compiler] Inference infrastructure should evolve to meet JLS8 18.x (Part G of JSR335 spec)
 *								Bug 424742 - [1.8] NPE in LambdaExpression.isCompatibleWith
 *								Bug 424710 - [1.8][compiler] CCE in SingleNameReference.localVariableBinding
 *								Bug 424205 - [1.8] Cannot infer type for diamond type with lambda on method invocation
 *								Bug 424415 - [1.8][compiler] Eventual resolution of ReferenceExpression is not seen to be happening.
 *								Bug 426366 - [1.8][compiler] Type inference doesn't handle multiple candidate target types in outer overload context
 *								Bug 427282 - [1.8][compiler] AIOOB (-1) at org.eclipse.jdt.internal.compiler.ClassFile.traverse(ClassFile.java:6209)
 *								Bug 427483 - [Java 8] Variables in lambdas sometimes can't be resolved
 *								Bug 428352 - [1.8][compiler] Resolution errors don't always surface
 *								Bug 427163 - [1.8][null] bogus error "Contradictory null specification" on varags
 *								Bug 432348 - [1.8] Internal compiler error (NPE) after upgrade to 1.8
 *								Bug 440143 - [1.8][null] one more case of contradictory null annotations regarding type variables
 *								Bug 441693 - [1.8][null] Bogus warning for type argument annotated with @NonNull
 *								Bug 434483 - [1.8][compiler][inference] Type inference not picked up with method reference
 *								Bug 446442 - [1.8] merge null annotations from super methods
 *								Bug 437072 - [compiler][null] Null analysis emits possibly incorrect warning for new int[][] despite @NonNullByDefault 
 *     Jesper S Moller - Contributions for
 *								bug 382721 - [1.8][compiler] Effectively final variables needs special treatment
 *								bug 412153 - [1.8][compiler] Check validity of annotations which may be repeatable
 *								bug 412153 - [1.8][compiler] Check validity of annotations which may be repeatable
 *								bug 412149 - [1.8][compiler] Emit repeated annotations into the designated container
 *								bug 419209 - [1.8] Repeating container annotations should be rejected in the presence of annotation it contains
 *     Till Brychcy - Contributions for
 *     						    bug 467094 - [1.8][null] TYPE_USE NullAnnotations of array contents are applied to field.
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.env.AccessRestriction;
import org.eclipse.jdt.internal.compiler.lookup.AnnotationBinding;
import org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ExtraCompilerModifiers;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.InferenceContext18;
import org.eclipse.jdt.internal.compiler.lookup.InvocationSite;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.PackageBinding;
import org.eclipse.jdt.internal.compiler.lookup.ParameterizedGenericMethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemMethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReasons;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TagBits;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.WildcardBinding;

@SuppressWarnings({ "rawtypes", "unchecked" })
public abstract class ASTNode implements TypeConstants, TypeIds {

    public int sourceStart, sourceEnd;

    // storage for internal flags (32 bits)				BIT USAGE
    // return type (operator) | name reference kind (name ref) | add assertion (type decl) | useful empty statement (empty statement)
    public static final int Bit1 = 0x1;

    // return type (operator) | name reference kind (name ref) | has local type (type, method, field decl) | if type elided (local)
    public static final int Bit2 = 0x2;

    // return type (operator) | name reference kind (name ref) | implicit this (this ref) | is argument(local)
    public static final int Bit3 = 0x4;

    // return type (operator) | first assignment to local (name ref,local decl) | undocumented empty block (block, type and method decl)
    public static final int Bit4 = 0x8;

    // value for return (expression) | has all method bodies (unit) | supertype ref (type ref) | resolved (field decl)
    public static final int Bit5 = 0x10;

    // depth (name ref, msg) | ignore need cast check (cast expression) | error in signature (method declaration/ initializer) | is recovered (annotation reference)
    public static final int Bit6 = 0x20;

    // depth (name ref, msg) | operator (operator) | need runtime checkcast (cast expression) | label used (labelStatement) | needFreeReturn (AbstractMethodDeclaration)
    public static final int Bit7 = 0x40;

    // depth (name ref, msg) | operator (operator) | unsafe cast (cast expression) | is default constructor (constructor declaration) | isElseStatementUnreachable (if statement)
    public static final int Bit8 = 0x80;

    // depth (name ref, msg) | operator (operator) | is local type (type decl) | isThenStatementUnreachable (if statement) | can be static
    public static final int Bit9 = 0x100;

    // depth (name ref, msg) | operator (operator) | is anonymous type (type decl)
    public static final int Bit10 = 0x200;

    // depth (name ref, msg) | operator (operator) | is member type (type decl)
    public static final int Bit11 = 0x400;

    // depth (name ref, msg) | operator (operator) | has abstract methods (type decl)
    public static final int Bit12 = 0x800;

    // depth (name ref, msg) | is secondary type (type decl)
    public static final int Bit13 = 0x1000;

    // strictly assigned (reference lhs) | discard enclosing instance (explicit constr call) | hasBeenGenerated (type decl)
    public static final int Bit14 = 0x2000;

    // is unnecessary cast (expression) | is varargs (type ref) | isSubRoutineEscaping (try statement) | superAccess (javadoc allocation expression/javadoc message send/javadoc return statement)
    public static final int Bit15 = 0x4000;

    // in javadoc comment (name ref, type ref, msg)
    public static final int Bit16 = 0x8000;

    // compound assigned (reference lhs) | unchecked (msg, alloc, explicit constr call)
    public static final int Bit17 = 0x10000;

    // non null (expression) | onDemand (import reference)
    public static final int Bit18 = 0x20000;

    // didResolve (parameterized qualified type ref/parameterized single type ref)  | empty (javadoc return statement) | needReceiverGenericCast (msg/fieldref)
    public static final int Bit19 = 0x40000;

    // contains syntax errors (method declaration, type declaration, field declarations, initializer), typeref: <> name ref: lambda capture)
    public static final int Bit20 = 0x80000;

    public static final int Bit21 = 0x100000;

    // parenthesis count (expression) | used (import reference) shadows outer local (local declarations)
    public static final int Bit22 = 0x200000;

    // parenthesis count (expression)
    public static final int Bit23 = 0x400000;

    // parenthesis count (expression)
    public static final int Bit24 = 0x800000;

    // parenthesis count (expression)
    public static final int Bit25 = 0x1000000;

    // parenthesis count (expression)
    public static final int Bit26 = 0x2000000;

    // parenthesis count (expression)
    public static final int Bit27 = 0x4000000;

    // parenthesis count (expression)
    public static final int Bit28 = 0x8000000;

    // parenthesis count (expression)
    public static final int Bit29 = 0x10000000;

    // elseif (if statement) | try block exit (try statement) | fall-through (case statement) | ignore no effect assign (expression ref) | needScope (for statement) | isAnySubRoutineEscaping (return statement) | blockExit (synchronized statement)
    public static final int Bit30 = 0x20000000;

    // local declaration reachable (local decl) | ignore raw type check (type ref) | discard entire assignment (assignment) | isSynchronized (return statement) | thenExit (if statement)
    public static final int Bit31 = 0x40000000;

    // reachable (statement)
    public static final int Bit32 = 0x80000000;

    public static final long Bit32L = 0x80000000L;

    public static final long Bit33L = 0x100000000L;

    public static final long Bit34L = 0x200000000L;

    public static final long Bit35L = 0x400000000L;

    public static final long Bit36L = 0x800000000L;

    public static final long Bit37L = 0x1000000000L;

    public static final long Bit38L = 0x2000000000L;

    public static final long Bit39L = 0x4000000000L;

    public static final long Bit40L = 0x8000000000L;

    public static final long Bit41L = 0x10000000000L;

    public static final long Bit42L = 0x20000000000L;

    public static final long Bit43L = 0x40000000000L;

    public static final long Bit44L = 0x80000000000L;

    public static final long Bit45L = 0x100000000000L;

    public static final long Bit46L = 0x200000000000L;

    public static final long Bit47L = 0x400000000000L;

    public static final long Bit48L = 0x800000000000L;

    public static final long Bit49L = 0x1000000000000L;

    public static final long Bit50L = 0x2000000000000L;

    public static final long Bit51L = 0x4000000000000L;

    public static final long Bit52L = 0x8000000000000L;

    public static final long Bit53L = 0x10000000000000L;

    public static final long Bit54L = 0x20000000000000L;

    public static final long Bit55L = 0x40000000000000L;

    public static final long Bit56L = 0x80000000000000L;

    public static final long Bit57L = 0x100000000000000L;

    public static final long Bit58L = 0x200000000000000L;

    public static final long Bit59L = 0x400000000000000L;

    public static final long Bit60L = 0x800000000000000L;

    public static final long Bit61L = 0x1000000000000000L;

    public static final long Bit62L = 0x2000000000000000L;

    public static final long Bit63L = 0x4000000000000000L;

    public static final long Bit64L = 0x8000000000000000L;

    // reachable by default
    public int bits = IsReachable;

    // for operators
    public static final int ReturnTypeIDMASK = Bit1 | Bit2 | Bit3 | Bit4;

    // Bit7 -> Bit12
    public static final int OperatorSHIFT = 6;

    // 6 bits for operator ID
    public static final int OperatorMASK = Bit7 | Bit8 | Bit9 | Bit10 | Bit11 | Bit12;

    // for binary expressions
    public static final int IsReturnedValue = Bit5;

    // for cast expressions
    public static final int UnnecessaryCast = Bit15;

    public static final int DisableUnnecessaryCastCheck = Bit6;

    public static final int GenerateCheckcast = Bit7;

    public static final int UnsafeCast = Bit8;

    // for name references
    public static final int RestrictiveFlagMASK = Bit1 | Bit2 | Bit3;

    // for local decls
    // type elided lambda argument.
    public static final int IsTypeElided = Bit2;

    public static final int IsArgument = Bit3;

    public static final int IsLocalDeclarationReachable = Bit31;

    public static final int IsForeachElementVariable = Bit5;

    public static final int ShadowsOuterLocal = Bit22;

    // for name refs or local decls
    public static final int FirstAssignmentToLocal = Bit4;

    // for msg or field references
    public static final int NeedReceiverGenericCast = Bit19;

    // for this reference
    public static final int IsImplicitThis = Bit3;

    // for single name references
    // Bit6 -> Bit13
    public static final int DepthSHIFT = 5;

    // 8 bits for actual depth value (max. 255)
    public static final int DepthMASK = Bit6 | Bit7 | Bit8 | Bit9 | Bit10 | Bit11 | Bit12 | Bit13;

    public static final int IsCapturedOuterLocal = Bit20;

    // for statements
    public static final int IsReachable = Bit32;

    public static final int LabelUsed = Bit7;

    // switch statement
    public static final int DocumentedFallthrough = Bit30;

    // switch statement
    public static final int DocumentedCasesOmitted = Bit31;

    // try statements
    public static final int IsSubRoutineEscaping = Bit15;

    public static final int IsTryBlockExiting = Bit30;

    // for type declaration
    public static final int ContainsAssertion = Bit1;

    public static final int IsLocalType = Bit9;

    // used to test for anonymous
    public static final int IsAnonymousType = Bit10;

    // local member do not know it is local at parse time (need to look at binding)
    public static final int IsMemberType = Bit11;

    // used to promote abstract enums
    public static final int HasAbstractMethods = Bit12;

    // used to test for secondary
    public static final int IsSecondaryType = Bit13;

    public static final int HasBeenGenerated = Bit14;

    // for type, method and field declarations
    // cannot conflict with AddAssertionMASK
    public static final int HasLocalType = Bit2;

    // field decl only (to handle forward references)
    public static final int HasBeenResolved = Bit5;

    // for expression
    // Bit22 -> Bit29
    public static final int ParenthesizedSHIFT = 21;

    // 8 bits for parenthesis count value (max. 255)
    public static final int ParenthesizedMASK = Bit22 | Bit23 | Bit24 | Bit25 | Bit26 | Bit27 | Bit28 | Bit29;

    public static final int IgnoreNoEffectAssignCheck = Bit30;

    // for references on lhs of assignment
    // set only for true assignments, as opposed to compound ones
    public static final int IsStrictlyAssigned = Bit14;

    // set only for compound assignments, as opposed to other ones
    public static final int IsCompoundAssigned = Bit17;

    // for explicit constructor call
    // used for codegen
    public static final int DiscardEnclosingInstance = Bit14;

    // for all method/constructor invocations (msg, alloc, expl. constr call)
    public static final int Unchecked = Bit17;

    // for javadoc - used to indicate whether the javadoc has to be resolved
    public static final int ResolveJavadoc = Bit17;

    // for empty statement
    public static final int IsUsefulEmptyStatement = Bit1;

    // for block and method declaration
    public static final int UndocumentedEmptyBlock = Bit4;

    public static final int OverridingMethodWithSupercall = Bit5;

    // used to flag a method that can be declared static
    public static final int CanBeStatic = Bit9;

    // for initializer and method declaration
    public static final int ErrorInSignature = Bit6;

    // for abstract method declaration
    // abstract method declaration
    public static final int NeedFreeReturn = Bit7;

    // for constructor declaration
    public static final int IsDefaultConstructor = Bit8;

    // for compilation unit
    public static final int HasAllMethodBodies = Bit5;

    public static final int IsImplicitUnit = Bit1;

    // for references in Javadoc comments
    public static final int InsideJavadoc = Bit16;

    // for javadoc allocation expression/javadoc message send/javadoc return statement
    public static final int SuperAccess = Bit15;

    // for javadoc return statement
    public static final int Empty = Bit19;

    // for if statement
    public static final int IsElseIfStatement = Bit30;

    public static final int ThenExit = Bit31;

    // as computed by control flow analysis or null analysis.
    public static final int IsElseStatementUnreachable = Bit8;

    // as computed by control flow analysis or null analysis
    public static final int IsThenStatementUnreachable = Bit9;

    // for type reference
    public static final int IsSuperType = Bit5;

    public static final int IsVarArgs = Bit15;

    public static final int IgnoreRawTypeCheck = Bit31;

    // for array initializer
    public static final int IsAnnotationDefaultValue = Bit1;

    // for null reference analysis
    public static final int IsNonNull = Bit18;

    // for for statement
    public static final int NeededScope = Bit30;

    // for import reference
    public static final int OnDemand = Bit18;

    public static final int Used = Bit2;

    // for parameterized qualified/single type ref
    public static final int DidResolve = Bit19;

    // for return statement
    public static final int IsAnySubRoutineEscaping = Bit30;

    public static final int IsSynchronized = Bit31;

    // for synchronized statement
    public static final int BlockExit = Bit30;

    // for annotation reference
    public static final int IsRecovered = Bit6;

    // for type declaration, initializer and method declaration
    public static final int HasSyntaxErrors = Bit20;

    // constants used when checking invocation arguments
    public static final int INVOCATION_ARGUMENT_OK = 0;

    public static final int INVOCATION_ARGUMENT_UNCHECKED = 1;

    public static final int INVOCATION_ARGUMENT_WILDCARD = 2;

    // for all declarations that can contain type references that have type annotations
    public static final int HasTypeAnnotations = Bit21;

    // for type reference (diamond case) - Java 7
    public static final int IsUnionType = Bit30;

    // Used to tag ParameterizedSingleTypeReference or ParameterizedQualifiedTypeReference when they are
    // used without any type args. It is also used to tag CompletionOnQualifiedExpression when the
    // generics inference has failed and the resolved type still has <>.
    public static final int IsDiamond = Bit20;

    // this is only used for method invocation as the expression inside an expression statement
    public static final int InsideExpressionStatement = Bit5;

    // for annotation reference, signal if annotation was created from a default:
    public static final int IsSynthetic = ASTNode.Bit7;

    // for all reference context entries.
    public static final int HasFunctionalInterfaceTypes = ASTNode.Bit22;

    public static final Argument[] NO_ARGUMENTS = new Argument[0];

    public  ASTNode() {
        super();
    }

    private static int checkInvocationArgument(BlockScope scope, Expression argument, TypeBinding parameterType, TypeBinding argumentType, TypeBinding originalParameterType) {
        argument.computeConversion(scope, parameterType, argumentType);
        if (// intersection types are tolerated
        argumentType != TypeBinding.NULL && parameterType.kind() == Binding.WILDCARD_TYPE) {
            WildcardBinding wildcard = (WildcardBinding) parameterType;
            if (wildcard.boundKind != Wildcard.SUPER) {
                return INVOCATION_ARGUMENT_WILDCARD;
            }
        }
        // originalParameterType == null ? parameterType : originalParameterType;
        TypeBinding checkedParameterType = parameterType;
        if (TypeBinding.notEquals(argumentType, checkedParameterType) && argumentType.needsUncheckedConversion(checkedParameterType)) {
            scope.problemReporter().unsafeTypeConversion(argument, argumentType, checkedParameterType);
            return INVOCATION_ARGUMENT_UNCHECKED;
        }
        return INVOCATION_ARGUMENT_OK;
    }

    public static boolean checkInvocationArguments(BlockScope scope, Expression receiver, TypeBinding receiverType, MethodBinding method, Expression[] arguments, TypeBinding[] argumentTypes, boolean argsContainCast, InvocationSite invocationSite) {
        long sourceLevel = scope.compilerOptions().sourceLevel;
        boolean is1_7 = sourceLevel >= ClassFileConstants.JDK1_7;
        TypeBinding[] params = method.parameters;
        int paramLength = params.length;
        boolean isRawMemberInvocation = !method.isStatic() && !receiverType.isUnboundWildcard() && method.declaringClass.isRawType() && method.hasSubstitutedParameters();
        boolean uncheckedBoundCheck = (method.tagBits & TagBits.HasUncheckedTypeArgumentForBoundCheck) != 0;
        MethodBinding rawOriginalGenericMethod = null;
        if (!isRawMemberInvocation) {
            if (method instanceof ParameterizedGenericMethodBinding) {
                ParameterizedGenericMethodBinding paramMethod = (ParameterizedGenericMethodBinding) method;
                if (paramMethod.isRaw && method.hasSubstitutedParameters()) {
                    rawOriginalGenericMethod = method.original();
                }
            }
        }
        int invocationStatus = INVOCATION_ARGUMENT_OK;
        if (arguments == null) {
            if (method.isVarargs()) {
                // no element was supplied for vararg parameter
                TypeBinding parameterType = ((ArrayBinding) params[paramLength - 1]).elementsType();
                if (!parameterType.isReifiable() && (!is1_7 || ((method.tagBits & TagBits.AnnotationSafeVarargs) == 0))) {
                    scope.problemReporter().unsafeGenericArrayForVarargs(parameterType, (ASTNode) invocationSite);
                }
            }
        } else {
            if (method.isVarargs()) {
                // 4 possibilities exist for a call to the vararg method foo(int i, long ... value) : foo(1), foo(1, 2), foo(1, 2, 3, 4) & foo(1, new long[] {1, 2})
                int lastIndex = paramLength - 1;
                for (int i = 0; i < lastIndex; i++) {
                    TypeBinding originalRawParam = rawOriginalGenericMethod == null ? null : rawOriginalGenericMethod.parameters[i];
                    invocationStatus |= checkInvocationArgument(scope, arguments[i], params[i], argumentTypes[i], originalRawParam);
                }
                int argLength = arguments.length;
                if (// https://bugs.eclipse.org/bugs/show_bug.cgi?id=337093
                lastIndex <= argLength) {
                    TypeBinding parameterType = params[lastIndex];
                    TypeBinding originalRawParam = null;
                    if (paramLength != argLength || parameterType.dimensions() != argumentTypes[lastIndex].dimensions()) {
                        // single element was provided for vararg parameter
                        parameterType = ((ArrayBinding) parameterType).elementsType();
                        if (!parameterType.isReifiable() && (!is1_7 || ((method.tagBits & TagBits.AnnotationSafeVarargs) == 0))) {
                            scope.problemReporter().unsafeGenericArrayForVarargs(parameterType, (ASTNode) invocationSite);
                        }
                        originalRawParam = rawOriginalGenericMethod == null ? null : ((ArrayBinding) rawOriginalGenericMethod.parameters[lastIndex]).elementsType();
                    }
                    for (int i = lastIndex; i < argLength; i++) {
                        invocationStatus |= checkInvocationArgument(scope, arguments[i], parameterType, argumentTypes[i], originalRawParam);
                    }
                }
                if (// 70056
                paramLength == // 70056
                argLength) {
                    int varargsIndex = paramLength - 1;
                    ArrayBinding varargsType = (ArrayBinding) params[varargsIndex];
                    TypeBinding lastArgType = argumentTypes[varargsIndex];
                    int dimensions;
                    if (lastArgType == TypeBinding.NULL) {
                        if (!(varargsType.leafComponentType().isBaseType() && varargsType.dimensions() == 1))
                            scope.problemReporter().varargsArgumentNeedCast(method, lastArgType, invocationSite);
                    } else if (varargsType.dimensions <= (dimensions = lastArgType.dimensions())) {
                        if (lastArgType.leafComponentType().isBaseType()) {
                            dimensions--;
                        }
                        if (varargsType.dimensions < dimensions) {
                            scope.problemReporter().varargsArgumentNeedCast(method, lastArgType, invocationSite);
                        } else if (varargsType.dimensions == dimensions && TypeBinding.notEquals(lastArgType, varargsType) && TypeBinding.notEquals(lastArgType.leafComponentType().erasure(), varargsType.leafComponentType.erasure()) && lastArgType.isCompatibleWith(varargsType.elementsType()) && lastArgType.isCompatibleWith(varargsType)) {
                            scope.problemReporter().varargsArgumentNeedCast(method, lastArgType, invocationSite);
                        }
                    }
                }
            } else {
                for (int i = 0; i < paramLength; i++) {
                    TypeBinding originalRawParam = rawOriginalGenericMethod == null ? null : rawOriginalGenericMethod.parameters[i];
                    invocationStatus |= checkInvocationArgument(scope, arguments[i], params[i], argumentTypes[i], originalRawParam);
                }
            }
            if (argsContainCast) {
                CastExpression.checkNeedForArgumentCasts(scope, receiver, receiverType, method, arguments, argumentTypes, invocationSite);
            }
        }
        if ((invocationStatus & INVOCATION_ARGUMENT_WILDCARD) != 0) {
            scope.problemReporter().wildcardInvocation((ASTNode) invocationSite, receiverType, method, argumentTypes);
        } else if (!method.isStatic() && !receiverType.isUnboundWildcard() && method.declaringClass.isRawType() && method.hasSubstitutedParameters()) {
            if (scope.compilerOptions().reportUnavoidableGenericTypeProblems || receiver == null || !receiver.forcedToBeRaw(scope.referenceContext())) {
                scope.problemReporter().unsafeRawInvocation((ASTNode) invocationSite, method);
            }
        } else if (rawOriginalGenericMethod != null || uncheckedBoundCheck || ((invocationStatus & INVOCATION_ARGUMENT_UNCHECKED) != 0)) {
            if (method instanceof ParameterizedGenericMethodBinding) {
                scope.problemReporter().unsafeRawGenericMethodInvocation((ASTNode) invocationSite, method, argumentTypes);
                return true;
            }
            if (sourceLevel >= ClassFileConstants.JDK1_8)
                // signal to erase return type and exceptions, while keeping javac compatibility at 1.7-
                return true;
        }
        return false;
    }

    public ASTNode concreteStatement() {
        return this;
    }

    public final boolean isFieldUseDeprecated(FieldBinding field, Scope scope, int filteredBits) {
        if (// ignore references inside Javadoc comments 
        (this.bits & ASTNode.InsideJavadoc) == 0 && // ignore write access
        (filteredBits & IsStrictlyAssigned) == 0 && field.isOrEnclosedByPrivateType() && // ignore cases where field is used from inside itself 
        !scope.isDefinedInField(field)) {
            if (((filteredBits & IsCompoundAssigned) != 0))
                // used, but usage may not be relevant
                field.original().compoundUseFlag++;
            else
                field.original().modifiers |= ExtraCompilerModifiers.AccLocallyUsed;
        }
        if ((field.modifiers & ExtraCompilerModifiers.AccRestrictedAccess) != 0) {
            AccessRestriction restriction = scope.environment().getAccessRestriction(field.declaringClass.erasure());
            if (restriction != null) {
                scope.problemReporter().forbiddenReference(field, this, restriction.classpathEntryType, restriction.classpathEntryName, restriction.getProblemId());
            }
        }
        if (!field.isViewedAsDeprecated())
            return false;
        // inside same unit - no report
        if (scope.isDefinedInSameUnit(field.declaringClass))
            return false;
        // if context is deprecated, may avoid reporting
        if (!scope.compilerOptions().reportDeprecationInsideDeprecatedCode && scope.isInsideDeprecatedCode())
            return false;
        return true;
    }

    public boolean isImplicitThis() {
        return false;
    }

    public boolean receiverIsImplicitThis() {
        return false;
    }

    /* Answer true if the method use is considered deprecated.
	* An access in the same compilation unit is allowed.
	*/
    public final boolean isMethodUseDeprecated(MethodBinding method, Scope scope, boolean isExplicitUse) {
        // ignore references insing Javadoc comments
        if ((this.bits & ASTNode.InsideJavadoc) == 0 && method.isOrEnclosedByPrivateType() && !scope.isDefinedInMethod(method)) {
            // ignore cases where method is used from inside itself (e.g. direct recursions)
            method.original().modifiers |= ExtraCompilerModifiers.AccLocallyUsed;
        }
        // 				 Caveat: this was not the case when access restriction funtion was added.
        if (isExplicitUse && (method.modifiers & ExtraCompilerModifiers.AccRestrictedAccess) != 0) {
            // note: explicit constructors calls warnings are kept despite the 'new C1()' case (two
            //       warnings, one on type, the other on constructor), because of the 'super()' case.
            AccessRestriction restriction = scope.environment().getAccessRestriction(method.declaringClass.erasure());
            if (restriction != null) {
                scope.problemReporter().forbiddenReference(method, this, restriction.classpathEntryType, restriction.classpathEntryName, restriction.getProblemId());
            }
        }
        if (!method.isViewedAsDeprecated())
            return false;
        // inside same unit - no report
        if (scope.isDefinedInSameUnit(method.declaringClass))
            return false;
        // non explicit use and non explicitly deprecated - no report
        if (!isExplicitUse && (method.modifiers & ClassFileConstants.AccDeprecated) == 0) {
            return false;
        }
        // if context is deprecated, may avoid reporting
        if (!scope.compilerOptions().reportDeprecationInsideDeprecatedCode && scope.isInsideDeprecatedCode())
            return false;
        return true;
    }

    public boolean isSuper() {
        return false;
    }

    public boolean isQualifiedSuper() {
        return false;
    }

    public boolean isThis() {
        return false;
    }

    public boolean isUnqualifiedSuper() {
        return false;
    }

    /* Answer true if the type use is considered deprecated.
	* An access in the same compilation unit is allowed.
	*/
    public final boolean isTypeUseDeprecated(TypeBinding type, Scope scope) {
        if (type.isArrayType()) {
            type = ((ArrayBinding) type).leafComponentType;
        }
        if (type.isBaseType())
            return false;
        ReferenceBinding refType = (ReferenceBinding) type;
        // https://bugs.eclipse.org/bugs/show_bug.cgi?id=385780
        if ((this.bits & ASTNode.InsideJavadoc) == 0 && refType instanceof TypeVariableBinding) {
            refType.modifiers |= ExtraCompilerModifiers.AccLocallyUsed;
        }
        // ignore references insing Javadoc comments
        if ((this.bits & ASTNode.InsideJavadoc) == 0 && refType.isOrEnclosedByPrivateType() && !scope.isDefinedInType(refType)) {
            // ignore cases where type is used from inside itself
            ((ReferenceBinding) refType.erasure()).modifiers |= ExtraCompilerModifiers.AccLocallyUsed;
        }
        if (refType.hasRestrictedAccess()) {
            AccessRestriction restriction = scope.environment().getAccessRestriction(type.erasure());
            if (restriction != null) {
                scope.problemReporter().forbiddenReference(type, this, restriction.classpathEntryType, restriction.classpathEntryName, restriction.getProblemId());
            }
        }
        // force annotations resolution before deciding whether the type may be deprecated
        refType.initializeDeprecatedAnnotationTagBits();
        if (!refType.isViewedAsDeprecated())
            return false;
        // inside same unit - no report
        if (scope.isDefinedInSameUnit(refType))
            return false;
        // if context is deprecated, may avoid reporting
        if (!scope.compilerOptions().reportDeprecationInsideDeprecatedCode && scope.isInsideDeprecatedCode())
            return false;
        return true;
    }

    public abstract StringBuffer print(int indent, StringBuffer output);

    public static StringBuffer printAnnotations(Annotation[] annotations, StringBuffer output) {
        int length = annotations.length;
        for (int i = 0; i < length; i++) {
            if (i > 0) {
                //$NON-NLS-1$
                output.append(//$NON-NLS-1$
                " ");
            }
            Annotation annotation2 = annotations[i];
            if (annotation2 != null) {
                annotation2.print(0, output);
            } else {
                output.append('?');
            }
        }
        return output;
    }

    public static StringBuffer printIndent(int indent, StringBuffer output) {
        //$NON-NLS-1$
        for (int i = indent; i > 0; i--) output.append("  ");
        return output;
    }

    public static StringBuffer printModifiers(int modifiers, StringBuffer output) {
        if ((modifiers & ClassFileConstants.AccPublic) != 0)
            //$NON-NLS-1$
            output.append("public ");
        if ((modifiers & ClassFileConstants.AccPrivate) != 0)
            //$NON-NLS-1$
            output.append("private ");
        if ((modifiers & ClassFileConstants.AccProtected) != 0)
            //$NON-NLS-1$
            output.append("protected ");
        if ((modifiers & ClassFileConstants.AccStatic) != 0)
            //$NON-NLS-1$
            output.append("static ");
        if ((modifiers & ClassFileConstants.AccFinal) != 0)
            //$NON-NLS-1$
            output.append("final ");
        if ((modifiers & ClassFileConstants.AccSynchronized) != 0)
            //$NON-NLS-1$
            output.append("synchronized ");
        if ((modifiers & ClassFileConstants.AccVolatile) != 0)
            //$NON-NLS-1$
            output.append("volatile ");
        if ((modifiers & ClassFileConstants.AccTransient) != 0)
            //$NON-NLS-1$
            output.append("transient ");
        if ((modifiers & ClassFileConstants.AccNative) != 0)
            //$NON-NLS-1$
            output.append("native ");
        if ((modifiers & ClassFileConstants.AccAbstract) != 0)
            //$NON-NLS-1$
            output.append("abstract ");
        if ((modifiers & ExtraCompilerModifiers.AccDefaultMethod) != 0)
            //$NON-NLS-1$
            output.append("default ");
        return output;
    }

    /**
	 * After method lookup has produced 'methodBinding' but when poly expressions have been seen as arguments,
	 * inspect the arguments to trigger another round of resolving with improved target types from the methods parameters.
	 * If this resolving produces better types for any arguments, update the 'argumentTypes' array in-place as an
	 * intended side effect that will feed better type information in checkInvocationArguments() and others. 
	 * @param invocation the outer invocation which is being resolved
	 * @param method the method produced by lookup (possibly involving type inference).
	 * @param argumentTypes the argument types as collected from first resolving the invocation arguments and as used for the method lookup.
	 * @param scope scope for resolution.
	 */
    public static void resolvePolyExpressionArguments(Invocation invocation, MethodBinding method, TypeBinding[] argumentTypes, BlockScope scope) {
        MethodBinding candidateMethod = method.isValidBinding() ? method : method instanceof ProblemMethodBinding ? ((ProblemMethodBinding) method).closestMatch : null;
        if (candidateMethod == null)
            return;
        boolean variableArity = candidateMethod.isVarargs();
        final TypeBinding[] parameters = candidateMethod.parameters;
        Expression[] arguments = invocation.arguments();
        if (variableArity && arguments != null && parameters.length == arguments.length) {
            if (arguments[arguments.length - 1].isCompatibleWith(parameters[parameters.length - 1], scope)) {
                variableArity = false;
            }
        }
        for (int i = 0, length = arguments == null ? 0 : arguments.length; i < length; i++) {
            Expression argument = arguments[i];
            TypeBinding parameterType = InferenceContext18.getParameter(parameters, i, variableArity);
            if (parameterType == null)
                // not much we can do without a target type, assume it only happens after some resolve error
                continue;
            if (argumentTypes[i] != null && argumentTypes[i].isPolyType()) {
                argument.setExpectedType(parameterType);
                TypeBinding updatedArgumentType;
                if (argument instanceof LambdaExpression) {
                    LambdaExpression lambda = (LambdaExpression) argument;
                    // avoid complaining about non-kosher descriptor as secondary problem
                    boolean skipKosherCheck = method.problemId() == ProblemReasons.Ambiguous;
                    updatedArgumentType = lambda.resolveType(scope, skipKosherCheck);
                    // additional checks, because LE.resolveType may return a valid binding even in the presence of structural errors
                    if (!lambda.isCompatibleWith(parameterType, scope) || lambda.hasErrors())
                        continue;
                } else {
                    updatedArgumentType = argument.resolveType(scope);
                }
                if (updatedArgumentType != null && updatedArgumentType.kind() != Binding.POLY_TYPE)
                    argumentTypes[i] = updatedArgumentType;
            }
        }
    }

    public static void resolveAnnotations(BlockScope scope, Annotation[] sourceAnnotations, Binding recipient) {
        resolveAnnotations(scope, sourceAnnotations, recipient, false);
        if (recipient instanceof SourceTypeBinding)
            ((SourceTypeBinding) recipient).evaluateNullAnnotations();
    }

    /**
	 * Resolve annotations, and check duplicates, answers combined tagBits
	 * for recognized standard annotations. Return null if nothing new is
	 * resolved.
	 */
    public static AnnotationBinding[] resolveAnnotations(BlockScope scope, Annotation[] sourceAnnotations, Binding recipient, boolean copySE8AnnotationsToType) {
        AnnotationBinding[] annotations = null;
        int length = sourceAnnotations == null ? 0 : sourceAnnotations.length;
        if (recipient != null) {
            switch(recipient.kind()) {
                case Binding.PACKAGE:
                    PackageBinding packageBinding = (PackageBinding) recipient;
                    if ((packageBinding.tagBits & TagBits.AnnotationResolved) != 0)
                        return annotations;
                    packageBinding.tagBits |= (TagBits.AnnotationResolved | TagBits.DeprecatedAnnotationResolved);
                    break;
                case Binding.TYPE:
                case Binding.GENERIC_TYPE:
                    ReferenceBinding type = (ReferenceBinding) recipient;
                    if ((type.tagBits & TagBits.AnnotationResolved) != 0)
                        return annotations;
                    type.tagBits |= (TagBits.AnnotationResolved | TagBits.DeprecatedAnnotationResolved);
                    if (length > 0) {
                        annotations = new AnnotationBinding[length];
                        type.setAnnotations(annotations);
                    }
                    break;
                case Binding.METHOD:
                    MethodBinding method = (MethodBinding) recipient;
                    if ((method.tagBits & TagBits.AnnotationResolved) != 0)
                        return annotations;
                    method.tagBits |= (TagBits.AnnotationResolved | TagBits.DeprecatedAnnotationResolved);
                    if (length > 0) {
                        annotations = new AnnotationBinding[length];
                        method.setAnnotations(annotations);
                    }
                    break;
                case Binding.FIELD:
                    FieldBinding field = (FieldBinding) recipient;
                    if ((field.tagBits & TagBits.AnnotationResolved) != 0)
                        return annotations;
                    field.tagBits |= (TagBits.AnnotationResolved | TagBits.DeprecatedAnnotationResolved);
                    if (length > 0) {
                        annotations = new AnnotationBinding[length];
                        field.setAnnotations(annotations);
                    }
                    break;
                case Binding.LOCAL:
                    LocalVariableBinding local = (LocalVariableBinding) recipient;
                    if ((local.tagBits & TagBits.AnnotationResolved) != 0)
                        return annotations;
                    local.tagBits |= (TagBits.AnnotationResolved | TagBits.DeprecatedAnnotationResolved);
                    if (length > 0) {
                        annotations = new AnnotationBinding[length];
                        local.setAnnotations(annotations, scope);
                    }
                    break;
                case Binding.TYPE_PARAMETER:
                case Binding.TYPE_USE:
                    // deliberately don't set the annotation resolved tagbits, it is not material and also we are working with a dummy static object.
                    annotations = new AnnotationBinding[length];
                    break;
                default:
                    return annotations;
            }
        }
        if (sourceAnnotations == null)
            return annotations;
        for (int i = 0; i < length; i++) {
            Annotation annotation = sourceAnnotations[i];
            final Binding annotationRecipient = annotation.recipient;
            if (annotationRecipient != null && recipient != null) {
                // only local and field can share annotations and their types.
                switch(recipient.kind()) {
                    case Binding.TYPE_USE:
                        if (annotations != null) {
                            // need to fill the instances array
                            for (int j = 0; j < length; j++) {
                                annotations[j] = sourceAnnotations[j].getCompilerAnnotation();
                            }
                        }
                        break;
                    case Binding.FIELD:
                        FieldBinding field = (FieldBinding) recipient;
                        field.tagBits = ((FieldBinding) annotationRecipient).tagBits;
                        if (annotations != null) {
                            // need to fill the instances array
                            for (int j = 0; j < length; j++) {
                                Annotation annot = sourceAnnotations[j];
                                annotations[j] = annot.getCompilerAnnotation();
                            }
                        }
                        break;
                    case Binding.LOCAL:
                        LocalVariableBinding local = (LocalVariableBinding) recipient;
                        long otherLocalTagBits = ((LocalVariableBinding) annotationRecipient).tagBits;
                        local.tagBits = otherLocalTagBits;
                        if ((otherLocalTagBits & TagBits.AnnotationSuppressWarnings) == 0) {
                            // need to fill the instances array
                            if (annotations != null) {
                                for (int j = 0; j < length; j++) {
                                    Annotation annot = sourceAnnotations[j];
                                    annotations[j] = annot.getCompilerAnnotation();
                                }
                            }
                        } else if (annotations != null) {
                            // One of the annotations at least is a SuppressWarnings annotation
                            LocalDeclaration localDeclaration = local.declaration;
                            int declarationSourceEnd = localDeclaration.declarationSourceEnd;
                            int declarationSourceStart = localDeclaration.declarationSourceStart;
                            for (int j = 0; j < length; j++) {
                                Annotation annot = sourceAnnotations[j];
                                /*
								 * Annotations are shared between two locals, but we still need to record
								 * the suppress annotation range for the second local
								 */
                                AnnotationBinding annotationBinding = annot.getCompilerAnnotation();
                                annotations[j] = annotationBinding;
                                if (annotationBinding != null) {
                                    final ReferenceBinding annotationType = annotationBinding.getAnnotationType();
                                    if (annotationType != null && annotationType.id == TypeIds.T_JavaLangSuppressWarnings) {
                                        annot.recordSuppressWarnings(scope, declarationSourceStart, declarationSourceEnd, scope.compilerOptions().suppressWarnings);
                                    }
                                }
                            }
                        }
                        break;
                }
                return annotations;
            } else {
                annotation.recipient = recipient;
                annotation.resolveType(scope);
                // null if receiver is a package binding
                if (annotations != null) {
                    annotations[i] = annotation.getCompilerAnnotation();
                }
            }
        }
        /* See if the recipient is meta-annotated with @Repeatable and if so validate constraints. We can't do this during resolution of @Repeatable itself as @Target and 
		   @Retention etc could come later
		*/
        if (recipient != null && recipient.isTaggedRepeatable()) {
            for (int i = 0; i < length; i++) {
                Annotation annotation = sourceAnnotations[i];
                ReferenceBinding annotationType = annotations[i] != null ? annotations[i].getAnnotationType() : null;
                if (annotationType != null && annotationType.id == TypeIds.T_JavaLangAnnotationRepeatable)
                    annotation.checkRepeatableMetaAnnotation(scope);
            }
        }
        // check duplicate annotations
        if (annotations != null && length > 1) {
            // only copy after 1st duplicate is detected
            AnnotationBinding[] distinctAnnotations = annotations;
            Map implicitContainerAnnotations = null;
            for (int i = 0; i < length; i++) {
                AnnotationBinding annotation = distinctAnnotations[i];
                if (annotation == null)
                    continue;
                ReferenceBinding annotationType = annotation.getAnnotationType();
                boolean foundDuplicate = false;
                ContainerAnnotation container = null;
                for (int j = i + 1; j < length; j++) {
                    AnnotationBinding otherAnnotation = distinctAnnotations[j];
                    if (otherAnnotation == null)
                        continue;
                    if (TypeBinding.equalsEquals(otherAnnotation.getAnnotationType(), annotationType)) {
                        if (distinctAnnotations == annotations) {
                            System.arraycopy(distinctAnnotations, 0, distinctAnnotations = new AnnotationBinding[length], 0, length);
                        }
                        // report/process it only once
                        distinctAnnotations[j] = null;
                        if (annotationType.isRepeatableAnnotationType()) {
                            Annotation persistibleAnnotation = sourceAnnotations[i].getPersistibleAnnotation();
                            if (persistibleAnnotation instanceof ContainerAnnotation)
                                container = (ContainerAnnotation) persistibleAnnotation;
                            // first encounter with a duplicate.
                            if (container == null) {
                                ReferenceBinding containerAnnotationType = annotationType.containerAnnotationType();
                                container = new ContainerAnnotation(sourceAnnotations[i], containerAnnotationType, scope);
                                if (implicitContainerAnnotations == null)
                                    implicitContainerAnnotations = new HashMap(3);
                                implicitContainerAnnotations.put(containerAnnotationType, sourceAnnotations[i]);
                                Annotation.checkForInstancesOfRepeatableWithRepeatingContainerAnnotation(scope, annotationType, sourceAnnotations);
                            }
                            container.addContainee(sourceAnnotations[j]);
                        } else {
                            foundDuplicate = true;
                            scope.problemReporter().duplicateAnnotation(sourceAnnotations[j], scope.compilerOptions().sourceLevel);
                        }
                    }
                }
                if (container != null) {
                    container.resolveType(scope);
                }
                if (foundDuplicate) {
                    scope.problemReporter().duplicateAnnotation(sourceAnnotations[i], scope.compilerOptions().sourceLevel);
                }
            }
            // Check for presence of repeating annotation together with the containing annotation
            if (implicitContainerAnnotations != null) {
                for (int i = 0; i < length; i++) {
                    if (distinctAnnotations[i] == null)
                        continue;
                    Annotation annotation = sourceAnnotations[i];
                    ReferenceBinding annotationType = distinctAnnotations[i].getAnnotationType();
                    if (implicitContainerAnnotations.containsKey(annotationType)) {
                        scope.problemReporter().repeatedAnnotationWithContainer((Annotation) implicitContainerAnnotations.get(annotationType), annotation);
                    }
                }
            }
        }
        if (copySE8AnnotationsToType)
            copySE8AnnotationsToType(scope, recipient, sourceAnnotations, false);
        return annotations;
    }

    /**	Resolve JSR308 annotations on a type reference, array creation expression or a wildcard. Type parameters go directly to the subroutine,
	    By construction the bindings associated with QTR, PQTR etc get resolved first and then annotations for different levels get resolved
	    and applied at one go. Likewise for multidimensional arrays.
	    
	    @Returns the annotated type binding. 
	*/
    public static TypeBinding resolveAnnotations(BlockScope scope, Annotation[][] sourceAnnotations, TypeBinding type) {
        int levels = sourceAnnotations == null ? 0 : sourceAnnotations.length;
        if (type == null || levels == 0)
            return type;
        AnnotationBinding[][] annotationBindings = new AnnotationBinding[levels][];
        for (int i = 0; i < levels; i++) {
            Annotation[] annotations = sourceAnnotations[i];
            if (annotations != null && annotations.length > 0) {
                annotationBindings[i] = resolveAnnotations(scope, annotations, TypeBinding.TYPE_USE_BINDING, false);
            }
        }
        return scope.environment().createAnnotatedType(type, annotationBindings);
    }

    // When SE8 annotations feature in SE7 locations, they get attributed to the declared entity. Copy/move these to the type of the declared entity (field, local, argument etc.)
    public static void copySE8AnnotationsToType(BlockScope scope, Binding recipient, Annotation[] annotations, boolean annotatingEnumerator) {
        if (annotations == null || annotations.length == 0 || recipient == null)
            return;
        long recipientTargetMask = 0;
        switch(recipient.kind()) {
            case Binding.LOCAL:
                recipientTargetMask = recipient.isParameter() ? TagBits.AnnotationForParameter : TagBits.AnnotationForLocalVariable;
                break;
            case Binding.FIELD:
                recipientTargetMask = TagBits.AnnotationForField;
                break;
            case Binding.METHOD:
                MethodBinding method = (MethodBinding) recipient;
                recipientTargetMask = method.isConstructor() ? TagBits.AnnotationForConstructor : TagBits.AnnotationForMethod;
                break;
            default:
                return;
        }
        AnnotationBinding[] se8Annotations = null;
        int se8count = 0;
        long se8nullBits = 0;
        // just any involved annotation so we have a location for error reporting
        Annotation se8NullAnnotation = null;
        int firstSE8 = -1;
        for (int i = 0, length = annotations.length; i < length; i++) {
            AnnotationBinding annotation = annotations[i].getCompilerAnnotation();
            if (annotation == null)
                continue;
            final ReferenceBinding annotationType = annotation.getAnnotationType();
            long metaTagBits = annotationType.getAnnotationTagBits();
            if ((metaTagBits & TagBits.AnnotationForTypeUse) != 0) {
                if (annotatingEnumerator) {
                    if ((metaTagBits & recipientTargetMask) == 0) {
                        scope.problemReporter().misplacedTypeAnnotations(annotations[i], annotations[i]);
                    }
                    continue;
                }
                if (firstSE8 == -1)
                    firstSE8 = i;
                if (se8Annotations == null) {
                    se8Annotations = new AnnotationBinding[] { annotation };
                    se8count = 1;
                } else {
                    System.arraycopy(se8Annotations, 0, se8Annotations = new AnnotationBinding[se8count + 1], 0, se8count);
                    se8Annotations[se8count++] = annotation;
                }
                if (annotationType.hasNullBit(TypeIds.BitNonNullAnnotation)) {
                    se8nullBits |= TagBits.AnnotationNonNull;
                    se8NullAnnotation = annotations[i];
                } else if (annotationType.hasNullBit(TypeIds.BitNullableAnnotation)) {
                    se8nullBits |= TagBits.AnnotationNullable;
                    se8NullAnnotation = annotations[i];
                }
            }
        }
        if (se8Annotations != null) {
            switch(recipient.kind()) {
                case Binding.LOCAL:
                    LocalVariableBinding local = (LocalVariableBinding) recipient;
                    TypeReference typeRef = local.declaration.type;
                    if (// discard hybrid annotations on name qualified types.
                    Annotation.isTypeUseCompatible(typeRef, scope)) {
                        local.declaration.bits |= HasTypeAnnotations;
                        typeRef.bits |= HasTypeAnnotations;
                        local.type = mergeAnnotationsIntoType(scope, se8Annotations, se8nullBits, se8NullAnnotation, typeRef, local.type);
                        if (scope.environment().usesNullTypeAnnotations()) {
                            local.tagBits &= ~(se8nullBits);
                        }
                    }
                    break;
                case Binding.FIELD:
                    FieldBinding field = (FieldBinding) recipient;
                    SourceTypeBinding sourceType = (SourceTypeBinding) field.declaringClass;
                    FieldDeclaration fieldDeclaration = sourceType.scope.referenceContext.declarationOf(field);
                    if (// discard hybrid annotations on name qualified types.
                    Annotation.isTypeUseCompatible(fieldDeclaration.type, scope)) {
                        fieldDeclaration.bits |= HasTypeAnnotations;
                        fieldDeclaration.type.bits |= HasTypeAnnotations;
                        field.type = mergeAnnotationsIntoType(scope, se8Annotations, se8nullBits, se8NullAnnotation, fieldDeclaration.type, field.type);
                        if (scope.environment().usesNullTypeAnnotations()) {
                            field.tagBits &= ~(se8nullBits);
                        }
                    }
                    break;
                case Binding.METHOD:
                    MethodBinding method = (MethodBinding) recipient;
                    if (!method.isConstructor()) {
                        sourceType = (SourceTypeBinding) method.declaringClass;
                        MethodDeclaration methodDecl = (MethodDeclaration) sourceType.scope.referenceContext.declarationOf(method);
                        if (Annotation.isTypeUseCompatible(methodDecl.returnType, scope)) {
                            methodDecl.bits |= HasTypeAnnotations;
                            methodDecl.returnType.bits |= HasTypeAnnotations;
                            method.returnType = mergeAnnotationsIntoType(scope, se8Annotations, se8nullBits, se8NullAnnotation, methodDecl.returnType, method.returnType);
                            if (scope.environment().usesNullTypeAnnotations()) {
                                method.tagBits &= ~(se8nullBits);
                            }
                        }
                    } else {
                        method.setTypeAnnotations(se8Annotations);
                    }
                    break;
            }
            AnnotationBinding[] recipientAnnotations = recipient.getAnnotations();
            int length = recipientAnnotations == null ? 0 : recipientAnnotations.length;
            int newLength = 0;
            for (int i = 0; i < length; i++) {
                final AnnotationBinding recipientAnnotation = recipientAnnotations[i];
                if (recipientAnnotation == null)
                    continue;
                long annotationTargetMask = recipientAnnotation.getAnnotationType().getAnnotationTagBits() & TagBits.AnnotationTargetMASK;
                if (annotationTargetMask == 0 || (annotationTargetMask & recipientTargetMask) != 0)
                    recipientAnnotations[newLength++] = recipientAnnotation;
            }
            if (newLength != length) {
                System.arraycopy(recipientAnnotations, 0, recipientAnnotations = new AnnotationBinding[newLength], 0, newLength);
                recipient.setAnnotations(recipientAnnotations, scope);
            }
        }
    }

    private static TypeBinding mergeAnnotationsIntoType(BlockScope scope, AnnotationBinding[] se8Annotations, long se8nullBits, Annotation se8NullAnnotation, TypeReference typeRef, TypeBinding existingType) {
        if (existingType == null || !existingType.isValidBinding())
            return existingType;
        TypeReference unionRef = typeRef.isUnionType() ? ((UnionTypeReference) typeRef).typeReferences[0] : null;
        // for arrays: @T X[] SE7 associates @T to the type, but in SE8 it affects the leaf component type
        TypeBinding oldLeafType = (unionRef == null) ? existingType.leafComponentType() : unionRef.resolvedType;
        if (se8nullBits != 0 && oldLeafType.isBaseType()) {
            scope.problemReporter().illegalAnnotationForBaseType(typeRef, new Annotation[] { se8NullAnnotation }, se8nullBits);
            return existingType;
        }
        long prevNullBits = oldLeafType.tagBits & TagBits.AnnotationNullMASK;
        if (// contradiction after merge?
        (prevNullBits | se8nullBits) == TagBits.AnnotationNullMASK) {
            if (// let type-use annotations override annotations on the type parameter declaration
            !(oldLeafType instanceof TypeVariableBinding)) {
                if (// conflict caused by the merge?
                prevNullBits != TagBits.AnnotationNullMASK && se8nullBits != TagBits.AnnotationNullMASK) {
                    scope.problemReporter().contradictoryNullAnnotations(se8NullAnnotation);
                }
                se8Annotations = Binding.NO_ANNOTATIONS;
                se8nullBits = 0;
            }
            oldLeafType = oldLeafType.withoutToplevelNullAnnotation();
        }
        AnnotationBinding[][] goodies = new AnnotationBinding[typeRef.getAnnotatableLevels()][];
        // @T X.Y.Z local; ==> @T should annotate X
        goodies[0] = se8Annotations;
        TypeBinding newLeafType = scope.environment().createAnnotatedType(oldLeafType, goodies);
        if (unionRef == null) {
            typeRef.resolvedType = existingType.isArrayType() ? scope.environment().createArrayType(newLeafType, existingType.dimensions(), existingType.getTypeAnnotations()) : newLeafType;
        } else {
            unionRef.resolvedType = newLeafType;
            unionRef.bits |= HasTypeAnnotations;
        }
        return typeRef.resolvedType;
    }

    /**
 * Figures if @Deprecated annotation is specified, do not resolve entire annotations.
 */
    public static void resolveDeprecatedAnnotations(BlockScope scope, Annotation[] annotations, Binding recipient) {
        if (recipient != null) {
            int kind = recipient.kind();
            if (annotations != null) {
                int length;
                if ((length = annotations.length) >= 0) {
                    switch(kind) {
                        case Binding.PACKAGE:
                            PackageBinding packageBinding = (PackageBinding) recipient;
                            if ((packageBinding.tagBits & TagBits.DeprecatedAnnotationResolved) != 0)
                                return;
                            break;
                        case Binding.TYPE:
                        case Binding.GENERIC_TYPE:
                            ReferenceBinding type = (ReferenceBinding) recipient;
                            if ((type.tagBits & TagBits.DeprecatedAnnotationResolved) != 0)
                                return;
                            break;
                        case Binding.METHOD:
                            MethodBinding method = (MethodBinding) recipient;
                            if ((method.tagBits & TagBits.DeprecatedAnnotationResolved) != 0)
                                return;
                            break;
                        case Binding.FIELD:
                            FieldBinding field = (FieldBinding) recipient;
                            if ((field.tagBits & TagBits.DeprecatedAnnotationResolved) != 0)
                                return;
                            break;
                        case Binding.LOCAL:
                            LocalVariableBinding local = (LocalVariableBinding) recipient;
                            if ((local.tagBits & TagBits.DeprecatedAnnotationResolved) != 0)
                                return;
                            break;
                        default:
                            return;
                    }
                    for (int i = 0; i < length; i++) {
                        TypeReference annotationTypeRef = annotations[i].type;
                        // only resolve type name if 'Deprecated' last token
                        if (!CharOperation.equals(TypeConstants.JAVA_LANG_DEPRECATED[2], annotationTypeRef.getLastToken()))
                            continue;
                        TypeBinding annotationType = annotations[i].type.resolveType(scope);
                        if (annotationType != null && annotationType.isValidBinding() && annotationType.id == TypeIds.T_JavaLangDeprecated) {
                            switch(kind) {
                                case Binding.PACKAGE:
                                    PackageBinding packageBinding = (PackageBinding) recipient;
                                    packageBinding.tagBits |= (TagBits.AnnotationDeprecated | TagBits.DeprecatedAnnotationResolved);
                                    return;
                                case Binding.TYPE:
                                case Binding.GENERIC_TYPE:
                                case Binding.TYPE_PARAMETER:
                                    ReferenceBinding type = (ReferenceBinding) recipient;
                                    type.tagBits |= (TagBits.AnnotationDeprecated | TagBits.DeprecatedAnnotationResolved);
                                    return;
                                case Binding.METHOD:
                                    MethodBinding method = (MethodBinding) recipient;
                                    method.tagBits |= (TagBits.AnnotationDeprecated | TagBits.DeprecatedAnnotationResolved);
                                    return;
                                case Binding.FIELD:
                                    FieldBinding field = (FieldBinding) recipient;
                                    field.tagBits |= (TagBits.AnnotationDeprecated | TagBits.DeprecatedAnnotationResolved);
                                    return;
                                case Binding.LOCAL:
                                    LocalVariableBinding local = (LocalVariableBinding) recipient;
                                    local.tagBits |= (TagBits.AnnotationDeprecated | TagBits.DeprecatedAnnotationResolved);
                                    return;
                                default:
                                    return;
                            }
                        }
                    }
                }
            }
            switch(kind) {
                case Binding.PACKAGE:
                    PackageBinding packageBinding = (PackageBinding) recipient;
                    packageBinding.tagBits |= TagBits.DeprecatedAnnotationResolved;
                    return;
                case Binding.TYPE:
                case Binding.GENERIC_TYPE:
                case Binding.TYPE_PARAMETER:
                    ReferenceBinding type = (ReferenceBinding) recipient;
                    type.tagBits |= TagBits.DeprecatedAnnotationResolved;
                    return;
                case Binding.METHOD:
                    MethodBinding method = (MethodBinding) recipient;
                    method.tagBits |= TagBits.DeprecatedAnnotationResolved;
                    return;
                case Binding.FIELD:
                    FieldBinding field = (FieldBinding) recipient;
                    field.tagBits |= TagBits.DeprecatedAnnotationResolved;
                    return;
                case Binding.LOCAL:
                    LocalVariableBinding local = (LocalVariableBinding) recipient;
                    local.tagBits |= TagBits.DeprecatedAnnotationResolved;
                    return;
                default:
                    return;
            }
        }
    }

    // ---- "default methods" for InvocationSite. Can we move to 1.8 and spare ourselves this ugliness please ?
    public boolean checkingPotentialCompatibility() {
        return false;
    }

    public void acceptPotentiallyCompatibleMethods(MethodBinding[] methods) {
    // Discard. Interested subclasses should override and grab these goodies. 
    }

    // --- "default methods" for InvocationSite
    public int sourceStart() {
        return this.sourceStart;
    }

    public int sourceEnd() {
        return this.sourceEnd;
    }

    public String toString() {
        return print(0, new StringBuffer(30)).toString();
    }

    public void traverse(ASTVisitor visitor, BlockScope scope) {
    // do nothing by default
    }
}
