package org.beybunproject.xmlContentVerifier;

import java.io.InputStream;

/**
 * @author: yerlibilgin
 * @date: 09/09/15.
 */
public class TarGzDecoder extends ArchiveXsdDecoder {
  @Override
  protected ArchiveEntry getNextEntry() {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  protected void start(InputStream inputStream) {
    throw new UnsupportedOperationException("Not implemented");
  }
}
