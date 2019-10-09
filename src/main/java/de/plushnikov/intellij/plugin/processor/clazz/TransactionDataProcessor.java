package de.plushnikov.intellij.plugin.processor.clazz;

import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiType;
import de.plushnikov.intellij.plugin.problem.ProblemBuilder;
import de.plushnikov.intellij.plugin.processor.LombokPsiElementUsage;
import de.plushnikov.intellij.plugin.psi.LombokLightMethodBuilder;
import de.plushnikov.intellij.plugin.util.LombokProcessorUtil;
import lombok.TransactionData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Inspect and validate @Setter lombok annotation on a class
 * Creates setter methods for fields of this class
 *
 * @author Plushnikov Michail
 */
public class TransactionDataProcessor extends AbstractClassProcessor {

  public TransactionDataProcessor() {

    super(PsiMethod.class, TransactionData.class);
  }

  @Override
  protected boolean validate(@NotNull PsiAnnotation psiAnnotation, @NotNull PsiClass psiClass,
    @NotNull ProblemBuilder builder) {

    return validateAnnotationOnRightType(psiAnnotation, psiClass, builder) && validateVisibility(psiAnnotation);
  }

  private boolean validateAnnotationOnRightType(@NotNull PsiAnnotation psiAnnotation, @NotNull PsiClass psiClass,
    @NotNull ProblemBuilder builder) {

    boolean result = true;
    if (psiClass.isAnnotationType() || psiClass.isInterface() || psiClass.isEnum()) {
      builder.addError("'@%s' is only supported on a class", psiAnnotation.getQualifiedName());
      result = false;
    }
    return result;
  }

  private boolean validateVisibility(@NotNull PsiAnnotation psiAnnotation) {

    final String methodVisibility = LombokProcessorUtil.getMethodModifier(psiAnnotation);
    return null != methodVisibility;
  }

  @Override
  protected void generatePsiElements(@NotNull PsiClass psiClass, @NotNull PsiAnnotation psiAnnotation,
    @NotNull List<? super PsiElement> target) {

    final String methodVisibility = LombokProcessorUtil.getMethodModifier(psiAnnotation);
    if (methodVisibility != null) {
      target.addAll(createTransactionMethods(psiClass, methodVisibility));
    }
  }

  public Collection<PsiMethod> createTransactionMethods(@NotNull PsiClass psiClass, @NotNull String methodModifier) {

    Collection<PsiMethod> result = new ArrayList<>();

    LombokLightMethodBuilder beginMethodBuilder = new LombokLightMethodBuilder(psiClass.getManager(), "begin")
      .withMethodReturnType(PsiType.VOID)
      .withContainingClass(psiClass);

    result.add(beginMethodBuilder);

    LombokLightMethodBuilder commitMethodBuilder = new LombokLightMethodBuilder(psiClass.getManager(), "commit")
      .withMethodReturnType(PsiType.VOID)
      .withContainingClass(psiClass);

    result.add(commitMethodBuilder);

    LombokLightMethodBuilder rollbackMethodBuilder = new LombokLightMethodBuilder(psiClass.getManager(), "rollback")
      .withMethodReturnType(PsiType.VOID)
      .withContainingClass(psiClass);

    result.add(rollbackMethodBuilder);

    return result;
  }

  @Override
  public LombokPsiElementUsage checkFieldUsage(@NotNull PsiField psiField, @NotNull PsiAnnotation psiAnnotation) {

    return LombokPsiElementUsage.NONE;
  }
}
