package org.beybunproject.xmlContentVerifier;

import java.io.InputStream;

/**
 * @author: yerlibilgin
 * @date: 11/09/15.
 */
public class XsdResourceResolver {
  private final XsdDecoder decoder;

  public XsdResourceResolver(byte[] source, ArchiveType type) {
    this.decoder = XsdDecoderFactory.createDecoderFor(source, type);
  }

  public InputStream getStreamForResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
    return this.decoder.getStreamForResource(type, namespaceURI, publicId, systemId, baseURI);
  }
}
