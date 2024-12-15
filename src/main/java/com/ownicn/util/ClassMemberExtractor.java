package com.ownicn.util;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ClassMemberExtractor {
    public static List<String> getClassMembers(PsiClass psiClass) {
        List<String> members = new ArrayList<>();
        if (psiClass != null) {
            // 提取字段
            for (PsiField field : psiClass.getAllFields()) {
                members.add(field.getName() + " : " + field.getType().getCanonicalText());
            }
            // 提取方法
            for (PsiMethod method : psiClass.getAllMethods()) {
                String signature = method.getName() + "(";
                signature += Arrays.stream(method.getParameterList().getParameters())
                        .map(param -> param.getType().getCanonicalText())
                        .collect(Collectors.joining(", "));
                signature += ")";
                members.add(signature + " : " + Objects.requireNonNull(method.getReturnType()).getCanonicalText());
            }
        }
        return members;
    }
}
