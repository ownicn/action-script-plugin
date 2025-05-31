package com.ownicn.util;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.StandardPatterns;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.ProcessingContext;
import com.ownicn.extensions.BindingMaps;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrReferenceExpression;
import org.jetbrains.plugins.groovy.GroovyLanguage;

import java.util.Map;

public class DynamicCompletionContributor extends CompletionContributor {
    public DynamicCompletionContributor() {
        extend(CompletionType.BASIC,
                PlatformPatterns.psiElement()
                        .withLanguage(GroovyLanguage.INSTANCE)
                        .withParent(StandardPatterns.instanceOf(GrReferenceExpression.class)),
                new CompletionProvider<>() {
                    @Override
                    protected void addCompletions(@NotNull CompletionParameters parameters,
                                                @NotNull ProcessingContext context,
                                                @NotNull CompletionResultSet result) {
                        System.out.println("DynamicCompletionContributor initialized");
                        Project project = parameters.getPosition().getProject();
                        PsiElement element = parameters.getPosition().getParent();

                        System.out.println("Position: " + parameters.getPosition());
                        System.out.println("Element: " + element);
                        System.out.println("Project: " + project);

                        if (element instanceof GrReferenceExpression refExpr) {
                            if (refExpr.getQualifierExpression() != null) {
                                System.out.println("Processing member completion");
                                handleMemberCompletion(refExpr, project, result);
                            } else {
                                System.out.println("Processing top level completion");
                                handleTopLevelCompletion(project, result);
                            }
                        }
                    }
                });
    }

    private void handleTopLevelCompletion(Project project, CompletionResultSet result) {
        Map<String, Object> bindingMap = BindingMaps.create(project, null).getBindingMap();
        JavaPsiFacade facade = JavaPsiFacade.getInstance(project);
        GlobalSearchScope scope = GlobalSearchScope.allScope(project);
        System.out.println("in handleMemberCompletion 2");
        for (Map.Entry<String, Object> entry : bindingMap.entrySet()) {
            if (entry.getValue() != null) {
                String varName = entry.getKey();
                Class<?> varClass = entry.getValue().getClass();
                
                PsiClass psiClass = facade.findClass(varClass.getName(), scope);
                if (psiClass != null) {
                    PsiType type = facade.getElementFactory().createType(psiClass);
                    
                    result.addElement(LookupElementBuilder.create(varName)
                            .withTypeText(type.getPresentableText())
                            .withIcon(AllIcons.Nodes.Variable)
                            .withBoldness(true)
                            .withTailText(" (built-in)", true));
                }
            }
        }
    }

    private void handleMemberCompletion(GrReferenceExpression refExpr, Project project, CompletionResultSet result) {
        PsiClassType qualifierType = null;
        GrReferenceExpression qualifier = (GrReferenceExpression) refExpr.getQualifierExpression();
        System.out.println("in handleMemberCompletion 3");
        
        if (qualifier != null) {
            String qualifierName = qualifier.getReferenceName();
            if (qualifierName != null) {
                Map<String, Object> bindingMap = BindingMaps.create(project, null).getBindingMap();
                Object value = bindingMap.get(qualifierName);
                if (value != null) {
                    JavaPsiFacade facade = JavaPsiFacade.getInstance(project);
                    GlobalSearchScope scope = GlobalSearchScope.allScope(project);
                    PsiClass psiClass = facade.findClass(value.getClass().getName(), scope);
                    if (psiClass != null) {
                        qualifierType = facade.getElementFactory().createType(psiClass);
                    }
                }
            }
        }

        if (qualifierType != null) {
            PsiClass psiClass = qualifierType.resolve();
            if (psiClass != null) {
                // 添加方法补全
                for (PsiMethod method : psiClass.getAllMethods()) {
                    if (!method.isConstructor()) {
                        String returnTypeText = method.getReturnType() != null ?
                                method.getReturnType().getPresentableText() : "void";
                        
                        result.addElement(LookupElementBuilder.create(method)
                                .withTypeText(returnTypeText)
                                .withTailText(method.getParameterList().getText())
                                .withIcon(method.hasModifierProperty(PsiModifier.STATIC) ?
                                        AllIcons.Nodes.Static : AllIcons.Nodes.Method)
                                .withInsertHandler((context, item) -> {
                                    if (context.getCompletionChar() != '(') {
                                        context.getDocument().insertString(context.getTailOffset(), "()");
                                        if (method.getParameterList().getParametersCount() > 0) {
                                            context.getEditor().getCaretModel().moveToOffset(context.getTailOffset() - 1);
                                        }
                                    }
                                }));
                    }
                }

                // 添加字段补全
                for (PsiField field : psiClass.getAllFields()) {
                    result.addElement(LookupElementBuilder.create(field)
                            .withTypeText(field.getType().getPresentableText())
                            .withIcon(field.hasModifierProperty(PsiModifier.STATIC) ?
                                    AllIcons.Nodes.Static : AllIcons.Nodes.Field));
                }
            }
        }
    }
}

