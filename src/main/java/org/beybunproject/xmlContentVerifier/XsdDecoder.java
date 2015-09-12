package org.beybunproject.xmlContentVerifier;

import java.io.InputStream;

/**
 * @author: yerlibilgin
 * @date: 09/09/15.
 */
public interface XsdDecoder {
  public static final String MINDER_DUMMY_PROTOCOL = "minbase://";

  /**
   * @see org.w3c.dom.ls.LSResourceResolver#resolveResource(String, String, String, String, String)
   * @param type
   * @param namespaceURI
   * @param publicId
   * @param systemId
   * @param baseURI
   * @return
   */
  InputStream getStreamForResource(String type, String namespaceURI, String publicId, String systemId, String baseURI);

  void initialize(InputStream inputStream);
}
