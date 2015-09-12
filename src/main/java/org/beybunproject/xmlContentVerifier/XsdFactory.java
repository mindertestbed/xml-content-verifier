package org.beybunproject.xmlContentVerifier;

import org.beybunproject.xmlContentVerifier.utils.Utils;

import java.net.URL;

/**
 * @author: yerlibilgin
 * @date: 11/09/15.
 */
public class XsdFactory {
  public static Xsd xsdFromByteArray(String name, byte[] source, ArchiveType type) {
    XsdResourceResolver archiveResourceResolver = new XsdResourceResolver(source, type);
    Xsd xsd = new Xsd(archiveResourceResolver, XsdDecoder.MINDER_DUMMY_PROTOCOL + name);
    return xsd;
  }

  /**
   * Read the stream of the url and create a memory resource resolver
   * @param name
   * @param url
   * @param archiveType
   * @return
   */
  public static Xsd xsdFromURL(String name, String url, ArchiveType archiveType) {
    try {
      if(archiveType == ArchiveType.PLAIN){
        //we need to make sure that if the xsd references others, we provide them
        //URLXsdResolver xsdResourceResolver = new URLXsdResolver(urlO.toURI().get);

      }else {
        final URL urlO = new URL(url);
        byte[] bytes = Utils.readStream(urlO.openStream());
        XsdResourceResolver xsdResourceResolver = new XsdResourceResolver(bytes, archiveType);
        return new Xsd(xsdResourceResolver, XsdDecoder.MINDER_DUMMY_PROTOCOL + name);
      }
    } catch (Exception ex) {
      throw ExceptionUtils.asRuntimeException(ex);
    }

    return null;
  }
}
