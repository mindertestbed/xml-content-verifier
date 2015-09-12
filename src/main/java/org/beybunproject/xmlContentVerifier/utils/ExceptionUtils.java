package org.beybunproject.xmlContentVerifier.utils;

import java.io.IOException;

/**
 * @author: yerlibilgin
 * @date: 11/09/15.
 */
public class ExceptionUtils {
  public static void throwAsRuntimeException(Exception ex) {
    throw asRuntimeException(ex);
  }

  public static RuntimeException asRuntimeException(Exception ex) {
    if (ex instanceof RuntimeException) return (RuntimeException) ex;
    return new RuntimeException(ex);
  }
}
