package com.ownicn.util;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.*;
import com.intellij.psi.impl.light.LightPsiClassBuilder;
import com.intellij.psi.impl.light.LightVariableBuilder;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.ownicn.extensions.BindingMaps;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.groovy.GroovyLanguage;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrReferenceExpression;

import java.util.Map;
public class DynamicCompletionContributor extends CompletionContributor {
    public DynamicCompletionContributor() {
        // 简单变量引用模式
        PsiElementPattern.Capture<PsiElement> variablePattern = PlatformPatterns.psiElement()
                .withParent(GrReferenceExpression.class)
                .andNot(PlatformPatterns.psiElement().afterLeaf("."));

        // 成员访问模式
        PsiElementPattern.Capture<PsiElement> memberAccessPattern = PlatformPatterns.psiElement()
                .withParent(GrReferenceExpression.class)
                .afterLeaf(".");

        // 简单变量补全
        extend(CompletionType.BASIC, variablePattern, new CompletionProvider<>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters parameters,
                                          @NotNull ProcessingContext context,
                                          @NotNull CompletionResultSet result) {
                Project project = parameters.getEditor().getProject();
                if (project == null) return;

                Map<String, Object> bindingMap = BindingMaps.create(project).getBindingMap();
                for (Map.Entry<String, Object> entry : bindingMap.entrySet()) {
                    if (entry.getValue() != null) {
                        addBuiltInVariable(result, project, entry.getKey(), entry.getValue().getClass().getName());
                    }
                }
            }
        });

        // 成员访问补全
        extend(CompletionType.BASIC, memberAccessPattern, new CompletionProvider<>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters parameters,
                                          @NotNull ProcessingContext context,
                                          @NotNull CompletionResultSet result) {
                Project project = parameters.getEditor().getProject();
                if (project == null) return;

                PsiElement position = parameters.getPosition();
                GrReferenceExpression expression = PsiTreeUtil.getParentOfType(position, GrReferenceExpression.class);
                if (expression == null) return;

                PsiType qualifierType = expression.getQualifierExpression() != null
                        ? expression.getQualifierExpression().getType()
                        : null;

                if (qualifierType == null && expression.getQualifierExpression() instanceof GrReferenceExpression) {
                    String qualifierName = ((GrReferenceExpression) expression.getQualifierExpression()).getReferenceName();
                    if (qualifierName != null) {
                        qualifierType = getTypeFromBindingMap(project, qualifierName);
                    }
                }

                if (qualifierType instanceof PsiClassType classType) {
                    PsiClass psiClass = classType.resolve();
                    if (psiClass != null) {
                        for (PsiField field : psiClass.getAllFields()) {
                            result.addElement(LookupElementBuilder.create(field)
                                    .withTypeText(field.getType().getPresentableText())
                                    .withIcon(AllIcons.Nodes.Field));
                        }
                        for (PsiMethod method : psiClass.getAllMethods()) {
                            if (!method.isConstructor()) {
                                result.addElement(LookupElementBuilder.create(method)
                                        .withTypeText(method.getReturnType() != null
                                                ? method.getReturnType().getPresentableText()
                                                : "void")
                                        .withTailText(method.getParameterList().getText(), true)
                                        .withIcon(AllIcons.Nodes.Method));
                            }
                        }
                    }
                }
            }
        });
    }

    private void addBuiltInVariable(@NotNull CompletionResultSet result,
                                    @NotNull Project project,
                                    @NotNull String name,
                                    @NotNull String typeFqn) {
        JavaPsiFacade facade = JavaPsiFacade.getInstance(project);
        GlobalSearchScope scope = GlobalSearchScope.allScope(project);

        PsiType type = null;
        try {
            type = facade.getElementFactory().createTypeByFQClassName(typeFqn, scope);
        } catch (Exception e) {
            PsiClass lightClass = createLightPsiClass(project, name, typeFqn);
            if (lightClass != null) {
                type = facade.getElementFactory().createType(lightClass);
            }
        }

        if (type == null) return;

        PsiVariable variable = new LightVariableBuilder<>(
                PsiManager.getInstance(project),
                name,
                type,
                GroovyLanguage.INSTANCE
        );

        result.addElement(LookupElementBuilder.create(variable)
                .withTypeText(type.getPresentableText())
                .withTailText(" - Built-in variable", true)
                .withBoldness(true)
                .withIcon(AllIcons.Nodes.Variable));
    }

    private PsiClass createLightPsiClass(@NotNull Project project, @NotNull String name, @NotNull String typeFqn) {
        PsiElement context = PsiManager.getInstance(project).findDirectory(project.getBaseDir());
        if (context == null) return null;

        LightPsiClassBuilder lightClassBuilder = new LightPsiClassBuilder(context, typeFqn);
        lightClassBuilder.setName(name);
        return lightClassBuilder;
    }

    private PsiType getTypeFromBindingMap(@NotNull Project project, @NotNull String name) {
        Map<String, Object> bindingMap = BindingMaps.create(project).getBindingMap();
        Object value = bindingMap.get(name);
        if (value != null) {
            String typeFqn = value.getClass().getName();
            JavaPsiFacade facade = JavaPsiFacade.getInstance(project);
            return facade.getElementFactory().createTypeByFQClassName(typeFqn, GlobalSearchScope.allScope(project));
        }
        return PsiType.getJavaLangObject(PsiManager.getInstance(project), GlobalSearchScope.allScope(project));
    }
}

