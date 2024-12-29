package com.ownicn.util;

import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.ownicn.extensions.BindingMaps;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrReferenceExpression;

import java.util.Map;

public class VariableTypeResolver {
    public static PsiType getVariableType(PsiElement element) {
        if (element.getParent() instanceof GrReferenceExpression refExpr) {

            // 如果是成员访问（例如：PROJECT.xxx）
            if (refExpr.getQualifierExpression() != null) {
                String varName = refExpr.getQualifierExpression().getText();
                Project project = element.getProject();
                
                // 从 BindingMaps 获取变量
                Map<String, Object> bindingMap = BindingMaps.create(project).getBindingMap();
                Object value = bindingMap.get(varName);
                
                if (value != null) {
                    // 获取变量的实际类型
                    String typeFqn = value.getClass().getName();
                    JavaPsiFacade facade = JavaPsiFacade.getInstance(project);
                    GlobalSearchScope scope = GlobalSearchScope.allScope(project);
                    return facade.getElementFactory().createTypeByFQClassName(typeFqn, scope);
                }
            }
            
            // 让 IDEA 自己处理引用解析
            return null;
        }
        return null;
    }
}