package org.beybunproject.xmlContentVerifier;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * @author: yerlibilgin
 * @date: 09/09/15.
 */
public class XsdDecoderFactory {
  public static XsdDecoder createDecoderFor(InputStream inputStream, ArchiveType archiveType) {
    XsdDecoder target = null;
    switch (archiveType) {
      case PLAIN:
        target = new PlainXsdDecoder();
        break;
      case RAR:
      case LZMA:
      case LZMA2:
        throw new UnsupportedOperationException("Archive Type " + archiveType + " is not supported yet");

      case ZIP:
        target = new ZipXsdDecoder();
        break;

      case JAR:
        target = new JarXsdDecoder();
        break;

      case TARGZ:
        target = new TarGzDecoder();
        break;
    }

    target.initialize(inputStream);
    return target;
  }

  public static XsdDecoder createDecoderFor(byte[] archiveBytes, ArchiveType archiveType) {
    return createDecoderFor(new ByteArrayInputStream(archiveBytes), archiveType);
  }
}
