/*
 * Copyright 2018 Medallia, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *     http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.medallia.merci.checkstyle.checks.annotation;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Checks methods and variables of classes for nullability annotations.
 */
public class RequiredNullabilityAnnotationCheck extends AbstractCheck {

    /* Missing access modifier is called 'package-private'. */
    private static final String PACKAGE_PRIVATE_ACCESS_MODIFIER = "package-private";

    /* Checkstyle message for a missing annotation. */
    private static final String MESSAGE_FOR_MUST_INCLUDE_EXACTLY_ONE_ANNOTATION = "Must include exactly one annotation from {0}.";

    /* By default, check static methods. */
    private static final boolean INCLUDE_STATIC_METHODS_BY_DEFAULT = true;

    /* By default, check instance variables. */
    private static final boolean INCLUDE_INSTANCE_VARIABLES_BY_DEFAULT = true;

    /* By default, do not check static variables. */
    private static final boolean INCLUDE_STATIC_VARIABLES_BY_DEFAULT = false;

    /* By default, do not check local variables. */
    private static final boolean INCLUDE_LOCAL_VARIABLES_BY_DEFAULT = false;

    /* Default nullability annotations. */
    private static final String[] DEFAULT_REQUIRED_ANNOTATIONS = { "Nonnull", "Nullable" };

    /* List of access modifiers that should be checked by default. */
    private static final String[] DEFAULT_INCLUDED_ACCESS_MODIFIERS = { "private", "protected", "public", PACKAGE_PRIVATE_ACCESS_MODIFIER };

    /* AST token types for checking the class definition, all methods and constructors. */
    private static final int[] CHECK_CLASS_AND_ALL_METHODS_AND_CONSTRUCTORS = { TokenTypes.CLASS_DEF, TokenTypes.CTOR_DEF, TokenTypes.METHOD_DEF };

    /* AST token types for checking the class definition, all methods, constructors and variables. */
    private static final int[] CHECK_CLASS_ALL_METHODS_CONSTRUCTORS_AND_VARIABLES = { TokenTypes.CLASS_DEF, TokenTypes.CTOR_DEF, TokenTypes.METHOD_DEF, TokenTypes.VARIABLE_DEF };

    private final Set<String> requiredAnnotations;
    private final Set<String> optionalClassAnnotations;
    private final Set<String> includedAccessModifiers;

    private int[] javaTokenTypesToCheck;
    private boolean includeStaticMethods;
    private boolean includeStaticVariables;
    private boolean includeInstanceVariables;
    private boolean includeLocalVariables;
    private boolean isClassAnnotated;

    /** Creates a new instance of the required nullability annotation check. */
    public RequiredNullabilityAnnotationCheck() {
        requiredAnnotations = new HashSet<>();
        Collections.addAll(requiredAnnotations, DEFAULT_REQUIRED_ANNOTATIONS);
        optionalClassAnnotations = new HashSet<>();
        includedAccessModifiers = new HashSet<>();
        Collections.addAll(includedAccessModifiers, DEFAULT_INCLUDED_ACCESS_MODIFIERS);
        includeStaticMethods = INCLUDE_STATIC_METHODS_BY_DEFAULT;
        includeStaticVariables = INCLUDE_STATIC_VARIABLES_BY_DEFAULT;
        includeInstanceVariables = INCLUDE_INSTANCE_VARIABLES_BY_DEFAULT;
        includeLocalVariables = INCLUDE_LOCAL_VARIABLES_BY_DEFAULT;
        isClassAnnotated = false;
        updateTokenTypesToCheck();
    }

    @Override
    public int[] getDefaultTokens() {
        return getAcceptableTokens();
    }

    @Override
    public int[] getAcceptableTokens() {
        return javaTokenTypesToCheck;
    }

    @Override
    public int[] getRequiredTokens() {
        return getAcceptableTokens();
    }

    @Override
    public void beginTree(DetailAST rootAST) {
    }

    @Override
    public void finishTree(DetailAST rootAST) {
    }

    @Override
    public void leaveToken(DetailAST ast) {
        if (ast.getType() == TokenTypes.CLASS_DEF) {
            isClassAnnotated = false;
        }
    }

    @Override
    public void visitToken(DetailAST ast) {
        if (ast.getType() == TokenTypes.CLASS_DEF) { // inner class?
            if (!isClassAnnotated) {
                isClassAnnotated(ast);
            }
            return;
        }
        if (isClassAnnotated || !includedAccessModifiers.contains(getAccessModifier(ast))) {
            return;
        }
        if (TokenTypes.METHOD_DEF == ast.getType() && !includeStaticMethods && isStatic(ast)) {
            return;
        }
        if (TokenTypes.VARIABLE_DEF == ast.getType() &&
                (!includeStaticVariables && isStatic(ast) || !includeLocalVariables && isLocal(ast) || !includeInstanceVariables)) {
            return;
        }
        checkModifierAnnotations(ast);
        checkParameterAnnotations(ast);
    }


    public void setRequiredAnnotations(String... annotations) {
        requiredAnnotations.clear();
        Collections.addAll(requiredAnnotations, annotations);
    }

    public void setOptionalClassAnnotations(String... annotations) {
        optionalClassAnnotations.clear();
        Collections.addAll(optionalClassAnnotations, annotations);
    }

    public void setIncludedAccessModifiers(String... modifiers) {
        includedAccessModifiers.clear();
        Collections.addAll(includedAccessModifiers, modifiers);
    }

    public void setIncludeStaticMethods(boolean include) {
        includeStaticMethods = include;
        updateTokenTypesToCheck();
    }

    public void setIncludeStaticVariables(boolean include) {
        includeStaticVariables = include;
        updateTokenTypesToCheck();
    }

    public void setIncludeInstanceVariables(boolean include) {
        includeInstanceVariables = include;
        updateTokenTypesToCheck();
    }

    public void setIncludeLocalVariables(boolean include) {
        includeLocalVariables = include;
        updateTokenTypesToCheck();
    }

    private void updateTokenTypesToCheck() {
        if (includeLocalVariables || includeInstanceVariables || includeStaticVariables) {
            javaTokenTypesToCheck = CHECK_CLASS_ALL_METHODS_CONSTRUCTORS_AND_VARIABLES;
        } else {
            javaTokenTypesToCheck = CHECK_CLASS_AND_ALL_METHODS_AND_CONSTRUCTORS;
        }
    }

    private void isClassAnnotated(DetailAST classDef) {
        isClassAnnotated = false;
        DetailAST modifiersNode = classDef.findFirstToken(TokenTypes.MODIFIERS);
        if (!hasAnnotations(modifiersNode)) {
            return;
        }
        for (DetailAST annotationDef = modifiersNode.findFirstToken(TokenTypes.ANNOTATION);
             annotationDef != null && annotationDef.getType() == TokenTypes.ANNOTATION;
             annotationDef = annotationDef.getNextSibling()) {
            String annotationName = getAnnotationName(annotationDef);
            if (optionalClassAnnotations.contains(annotationName)) {
                isClassAnnotated = true;
                return;
            }
        }
    }

    private String getAnnotationName(DetailAST annotation) {
        DetailAST identNode = annotation.findFirstToken(TokenTypes.IDENT);
        if (identNode == null) {
            identNode = annotation.findFirstToken(TokenTypes.DOT).findFirstToken(TokenTypes.IDENT);
        }
        return identNode.getText();
    }

    private boolean hasAnnotations(DetailAST modifierNode) {
        return modifierNode != null && modifierNode.findFirstToken(TokenTypes.ANNOTATION) != null;
    }

    private String getAccessModifier(DetailAST ast) {
        DetailAST modifiers = ast.findFirstToken(TokenTypes.MODIFIERS);
        for (DetailAST child = modifiers.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child.getType() == TokenTypes.LITERAL_PUBLIC || child.getType() == TokenTypes.LITERAL_PRIVATE || child.getType() == TokenTypes.LITERAL_PROTECTED) {
                return child.getText();
            }
        }
        return PACKAGE_PRIVATE_ACCESS_MODIFIER;
    }

    private boolean isStatic(DetailAST ast) {
        DetailAST modifiers = ast.findFirstToken(TokenTypes.MODIFIERS);
        for (DetailAST child = modifiers.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child.getType() == TokenTypes.LITERAL_STATIC) {
                return true;
            }
        }
        return false;
    }

    private boolean isLocal(DetailAST ast) {
        for (DetailAST parent = ast.getParent(); parent != null; parent = parent.getParent()) {
            if (parent.getType() == TokenTypes.METHOD_DEF) {
                return true;
            }
            if (parent.getType() == TokenTypes.CLASS_DEF) {
                return false;
            }
        }
        return true;
    }

    private void checkParameterAnnotations(DetailAST ast) {
        if (ast.getType() != TokenTypes.METHOD_DEF && ast.getType() != TokenTypes.CTOR_DEF) {
            return;
        }
        DetailAST methodParams = ast.findFirstToken(TokenTypes.PARAMETERS);
        if (!requiredAnnotations.isEmpty() && methodParams != null) {
            for (DetailAST child = methodParams.getFirstChild(); child != null; child = child.getNextSibling()) {
                if (child.getType() == TokenTypes.PARAMETER_DEF) {
                    checkModifierAnnotations(child);
                }
            }
        }
    }

    private void checkModifierAnnotations(DetailAST ast) {
        if (ast.getType() == TokenTypes.CTOR_DEF) {
            return;
        }

        DetailAST modifiers = ast.findFirstToken(TokenTypes.MODIFIERS);
        if (!requiredAnnotations.isEmpty() && modifiers != null && isObjectType(modifiers.getNextSibling())) {
            int annotationCount = 0;
            for (DetailAST child = modifiers.getFirstChild(); child != null; child = child.getNextSibling()) {
                if (child.getType() == TokenTypes.ANNOTATION) {
                    DetailAST firstChild = child.getFirstChild();
                    String annotation = FullIdent.createFullIdent(firstChild.getNextSibling()).getText();
                    if (requiredAnnotations.contains(annotation)) {
                        annotationCount++;
                    }
                }
            }
            if (annotationCount != 1) {
                log(ast.getLineNo(), MESSAGE_FOR_MUST_INCLUDE_EXACTLY_ONE_ANNOTATION,
                        requiredAnnotations.stream().map(name -> "@" + name).collect(Collectors.joining(", ")));
            }
        }
    }

    private boolean isObjectType(DetailAST type) {
        int objectType = type.getFirstChild().getType();
        return objectType == TokenTypes.IDENT || objectType == TokenTypes.DOT || objectType == TokenTypes.ARRAY_DECLARATOR;
    }
}
