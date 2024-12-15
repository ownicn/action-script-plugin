package com.ownicn.util;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceExpression;
import com.intellij.psi.PsiType;
import com.intellij.psi.PsiVariable;

public class VariableTypeResolver {
    public static PsiType getVariableType(PsiElement element) {
        if (element instanceof PsiReferenceExpression) {
            System.out.println("element1111 = " + element);
            PsiElement resolved = ((PsiReferenceExpression) element).resolve();
            if (resolved instanceof PsiVariable) {
                return ((PsiVariable) resolved).getType();
            }
        }
        return null;
    }
}