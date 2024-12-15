package com.ownicn.util;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.psi.impl.light.LightVariableBuilder;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.groovy.GroovyLanguage;

import java.util.List;

public class DynamicCompletionContributor extends CompletionContributor {
    public DynamicCompletionContributor() {
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(), new CompletionProvider<>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context,
                                          @NotNull CompletionResultSet result) {
                PsiElement element = parameters.getPosition();
                PsiType variableType = VariableTypeResolver.getVariableType(element);
                if (variableType != null) {
                    PsiClass psiClass = PsiClassUtil.resolvePsiClass(variableType, parameters.getEditor().getProject());
                    if (psiClass != null) {
                        List<String> members = ClassMemberExtractor.getClassMembers(psiClass);
                        for (String member : members) {
                            result.addElement(LookupElementBuilder.create(member));
                        }
                    }
                }
                PsiVariable variable = createVariable(parameters.getEditor().getProject());
                if (variable != null) {
                    result.addElement(LookupElementBuilder.create(variable).withTypeText(variable.getType().getPresentableText())
                            .withTailText(" - Project variable", true));
                }
            }
        });
    }

    public PsiVariable createVariable(Project project) {
        // 创建一个名为 `project` 的变量，类型为 `Project`
        JavaPsiFacade facade = JavaPsiFacade.getInstance(project);
        GlobalSearchScope scope = GlobalSearchScope.allScope(project);
        PsiType type = facade.getElementFactory().createTypeByFQClassName("com.intellij.openapi.project.impl.ProjectImpl", scope);
        return new LightVariableBuilder<>(PsiManager.getInstance(project), "project", type, GroovyLanguage.INSTANCE);
    }
}
