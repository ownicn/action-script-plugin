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
                        .withLanguage(GroovyLanguage.INSTANCE),
                new CompletionProvider<>() {
                    @Override
                    protected void addCompletions(@NotNull CompletionParameters parameters,
                                                  @NotNull ProcessingContext context,
                                                  @NotNull CompletionResultSet result) {
                        PsiFile file = parameters.getOriginalFile();
                        if (!"dummy.groovy".equalsIgnoreCase(file.getName())) {
                            return;
                        }
                        Project project = parameters.getPosition().getProject();
                        Map<String, Object> bindingMap = BindingMaps.create(project, null).getBindingMap();

                        // 创建一个不区分大小写的结果集
                        CompletionResultSet caseInsensitiveResult = result.caseInsensitive();

                        // 添加内置变量
                        for (Map.Entry<String, Object> entry : bindingMap.entrySet()) {
                            if (entry.getValue() != null) {
                                String varName = entry.getKey();
                                Object value = entry.getValue();

                                LookupElementBuilder element = LookupElementBuilder.create(varName)
                                        .withTypeText(value.getClass().getSimpleName())
                                        .withIcon(AllIcons.Nodes.Variable)
                                        .withBoldness(true)
                                        .withTailText(" (built-in)", true);

                                caseInsensitiveResult.addElement(PrioritizedLookupElement.withPriority(element, 100));
                            }
                        }
                    }
                });
    }
}

