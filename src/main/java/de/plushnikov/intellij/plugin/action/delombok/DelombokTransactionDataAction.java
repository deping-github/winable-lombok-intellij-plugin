package de.plushnikov.intellij.plugin.action.delombok;

import com.intellij.openapi.components.ServiceManager;
import de.plushnikov.intellij.plugin.processor.clazz.TransactionDataProcessor;
import org.jetbrains.annotations.NotNull;

public class DelombokTransactionDataAction extends AbstractDelombokAction {

  @Override
  @NotNull
  protected DelombokHandler createHandler() {

    return new DelombokHandler(
      ServiceManager.getService(TransactionDataProcessor.class));
  }
}
