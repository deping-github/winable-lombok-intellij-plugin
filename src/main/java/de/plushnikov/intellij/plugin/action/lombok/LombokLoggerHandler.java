package de.plushnikov.intellij.plugin.action.lombok;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiModifier;
import com.intellij.refactoring.rename.RenameProcessor;
import de.plushnikov.intellij.plugin.processor.clazz.log.AbstractLogProcessor;
import de.plushnikov.intellij.plugin.processor.clazz.log.CommonsLogProcessor;
import de.plushnikov.intellij.plugin.processor.clazz.log.FloggerProcessor;
import de.plushnikov.intellij.plugin.processor.clazz.log.JBossLogProcessor;
import de.plushnikov.intellij.plugin.processor.clazz.log.Log4j2Processor;
import de.plushnikov.intellij.plugin.processor.clazz.log.Log4jProcessor;
import de.plushnikov.intellij.plugin.processor.clazz.log.LogProcessor;
import de.plushnikov.intellij.plugin.processor.clazz.log.Slf4jProcessor;
import de.plushnikov.intellij.plugin.processor.clazz.log.XSlf4jProcessor;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;

public class LombokLoggerHandler extends BaseLombokHandler {

  protected void processClass(@NotNull PsiClass psiClass) {
    final Collection<AbstractLogProcessor> logProcessors = Arrays.asList(
      ServiceManager.getService(CommonsLogProcessor.class), ServiceManager.getService(JBossLogProcessor.class),
      ServiceManager.getService(Log4jProcessor.class), ServiceManager.getService(Log4j2Processor.class), ServiceManager.getService(LogProcessor.class),
      ServiceManager.getService(Slf4jProcessor.class), ServiceManager.getService(XSlf4jProcessor.class), ServiceManager.getService(FloggerProcessor.class));

    final String lombokLoggerName = AbstractLogProcessor.getLoggerName(psiClass);
    final boolean lombokLoggerIsStatic = AbstractLogProcessor.isLoggerStatic(psiClass);

    for (AbstractLogProcessor logProcessor : logProcessors) {
      for (PsiField psiField : psiClass.getFields()) {
        if (psiField.getType().equalsToText(logProcessor.getLoggerType()) && checkLoggerField(psiField, lombokLoggerName, lombokLoggerIsStatic)) {
          processLoggerField(psiField, psiClass, logProcessor, lombokLoggerName);
        }
      }
    }
  }

  private void processLoggerField(@NotNull PsiField psiField, @NotNull PsiClass psiClass, @NotNull AbstractLogProcessor logProcessor, @NotNull String lombokLoggerName) {
    if (!lombokLoggerName.equals(psiField.getName())) {
      RenameProcessor processor = new RenameProcessor(psiField.getProject(), psiField, lombokLoggerName, false, false);
      processor.doRun();
    }

    addAnnotation(psiClass, logProcessor.getSupportedAnnotationClasses()[0]);

    psiField.delete();
  }

  private boolean checkLoggerField(@NotNull PsiField psiField, @NotNull String lombokLoggerName, boolean lombokLoggerIsStatic) {
    if (!isValidLoggerField(psiField, lombokLoggerName, lombokLoggerIsStatic)) {
      int result = Messages.showOkCancelDialog(
        String.format("Logger field: \"%s\" Is not private %s final field named \"%s\". Refactor anyway?",
          psiField.getName(), lombokLoggerIsStatic ? "static" : "", lombokLoggerName),
        "Attention!", Messages.getQuestionIcon());
      return DialogWrapper.OK_EXIT_CODE == result;
    }
    return true;
  }

  private boolean isValidLoggerField(@NotNull PsiField psiField, @NotNull String lombokLoggerName, boolean lombokLoggerIsStatic) {
    boolean isPrivate = psiField.hasModifierProperty(PsiModifier.PRIVATE);
    boolean isStatic = lombokLoggerIsStatic == psiField.hasModifierProperty(PsiModifier.STATIC);
    boolean isFinal = psiField.hasModifierProperty(PsiModifier.FINAL);
    boolean isProperlyNamed = lombokLoggerName.equals(psiField.getName());

    return isPrivate & isStatic & isFinal & isProperlyNamed;
  }
}
