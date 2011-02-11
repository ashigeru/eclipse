/*
 * Copyright 2011 @ashigeru.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package com.ashigeru.eclipse.util.jdt.internal.ui.handlers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Inserts assertion statements "each method parameter should not be null" into the current method.
 * @author ashigeru
 */
public class InsertAssertionHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IEditorPart editor = HandlerUtil.getActiveEditorChecked(event);
        if ((editor instanceof ITextEditor) == false) {
            return null;
        }

        ITextEditor textEditor = (ITextEditor) editor;
        TextSelection text = getTextSelection(textEditor);
        if (text == null) {
            return null;
        }
        ICompilationUnit compilationUnit = getJavaElement(editor);
        if (compilationUnit == null) {
            return null;
        }

        CompilationUnit ast = parse(compilationUnit);
        MethodDeclaration method = findSelectedExecutable(ast, text);
        if (method == null) {
            return null;
        }

        IDocument document = getDocument(textEditor);
        if (document == null) {
            return null;
        }

        TextEdit edit = createEdit(ast, method, document);
        if (edit == null) {
            return null;
        }
        try {
            edit.apply(document);
        }
        catch (MalformedTreeException e) {
            return null;
        }
        catch (BadLocationException e) {
            return null;
        }

        return null;
    }

    private TextEdit createEdit(
            CompilationUnit ast,
            MethodDeclaration method,
            IDocument target) {
        assert ast != null;
        assert method != null;
        assert target != null;
        List<String> objectParams = new ArrayList<String>();
        for (Object o : method.parameters()) {
            SingleVariableDeclaration var = (SingleVariableDeclaration) o;
            if (var.getType().getNodeType() != ASTNode.PRIMITIVE_TYPE) {
                objectParams.add(var.getName().getIdentifier());
            }
        }
        if (objectParams.isEmpty()) {
            return null;
        }
        AST factory = ast.getAST();
        ast.recordModifications();
        List<Statement> toInsert = new ArrayList<Statement>();
        for (String name : objectParams) {
            AssertStatement assertion = createAssertion(factory, name);
            toInsert.add(assertion);
        }

        Block body = method.getBody();
        @SuppressWarnings("unchecked")
        List<Statement> statements = body.statements();

        int offset = 0;
        if (statements.isEmpty() == false) {
            Statement first = statements.get(0);
            int type = first.getNodeType();
            if (type == ASTNode.CONSTRUCTOR_INVOCATION ||
                    type == ASTNode.SUPER_CONSTRUCTOR_INVOCATION) {
                offset ++;
            }
        }

        statements.addAll(offset, toInsert);

        return ast.rewrite(target, null);
    }

    private AssertStatement createAssertion(AST factory, String paramName) {
        assert factory != null;
        assert paramName != null;
        AssertStatement assertion = factory.newAssertStatement();
        InfixExpression notNull = factory.newInfixExpression();
        notNull.setLeftOperand(factory.newSimpleName(paramName));
        notNull.setOperator(Operator.NOT_EQUALS);
        notNull.setRightOperand(factory.newNullLiteral());
        assertion.setExpression(notNull);
        return assertion;
    }

    private MethodDeclaration findSelectedExecutable(
            CompilationUnit ast,
            TextSelection selection) {
        assert ast != null;
        assert selection != null;
        final MethodDeclaration[] results = new MethodDeclaration[1];
        final int carret = selection.getOffset();
        ast.accept(new ASTVisitor(false) {
            @Override
            public boolean visit(MethodDeclaration node) {
                if (node.getBody() == null) {
                    return false;
                }
                int start = node.getStartPosition();
                if (start < 0) {
                    return true;
                }
                int end = start + node.getLength();
                if (start <= carret && carret <= end) {
                    results[0] = node;
                }
                return true;
            }
        });
        return results[0];
    }

    private CompilationUnit parse(ICompilationUnit compilationUnit) {
        assert compilationUnit != null;
        ASTParser parser = ASTParser.newParser(AST.JLS3);
        parser.setSource(compilationUnit);
        return (CompilationUnit) parser.createAST(new NullProgressMonitor());
    }

    private ICompilationUnit getJavaElement(IEditorPart editor) {
        assert editor != null;
        IEditorInput input = editor.getEditorInput();
        ITypeRoot element = JavaUI.getEditorInputTypeRoot(input);
        if (element == null ||
                element.getElementType() != IJavaElement.COMPILATION_UNIT) {
            return null;
        }
        return (ICompilationUnit) element;
    }

    private TextSelection getTextSelection(ITextEditor textEditor) {
        assert textEditor != null;
        ISelectionProvider selections = textEditor.getSelectionProvider();
        if (selections == null) {
            return null;
        }
        ISelection selection = selections.getSelection();
        if ((selection instanceof TextSelection) == false) {
            return null;
        }
        return (TextSelection) selection;
    }

    private IDocument getDocument(ITextEditor textEditor) {
        assert textEditor != null;
        IDocumentProvider documents = textEditor.getDocumentProvider();
        if (documents == null) {
            return null;
        }
        IDocument document = documents.getDocument(textEditor.getEditorInput());
        if (document == null) {
            return null;
        }
        return document;
    }
}
