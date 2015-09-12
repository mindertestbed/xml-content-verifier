package org.beybunproject.xmlContentVerifier;

import iso_schematron_xslt2.SchematronClassResolver;

import javax.net.ssl.*;
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.security.cert.CertificateException;
import java.util.Properties;

/**
 * @author: yerlibilgin
 * @date: 12/09/15.
 */
public class XmlContentVerifier {

  static {
    System.setProperty("javax.xml.transform.TransformerFactory", "net.sf.saxon.TransformerFactoryImpl");
  }

  private static SchematronClassResolver resolver = new SchematronClassResolver();
  private static TransformerFactory tFactory = TransformerFactory.newInstance();

  static {
    tFactory.setURIResolver(resolver);
  }

  static {

    try {
    /*
 *  fix for
 *    Exception in thread "main" javax.net.ssl.SSLHandshakeException:
 *       sun.security.validator.ValidatorException:
 *           PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException:
 *               unable to find valid certification path to requested target
 */
      TrustManager[] trustAllCerts = new TrustManager[]{
          new X509TrustManager() {
            @Override
            public void checkClientTrusted(java.security.cert.X509Certificate[] x509Certificates, String s) throws CertificateException {

            }

            @Override
            public void checkServerTrusted(java.security.cert.X509Certificate[] x509Certificates, String s) throws CertificateException {

            }

            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
              return null;
            }
          }
      };

      SSLContext sc = SSLContext.getInstance("SSL");
      sc.init(null, trustAllCerts, new java.security.SecureRandom());
      HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

// Create all-trusting host name verifier
      HostnameVerifier allHostsValid = new HostnameVerifier() {
        public boolean verify(String hostname, SSLSession session) {
          return true;
        }
      };
// Install the all-trusting host verifier
      HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
/*
 * end of the fix
 */
    } catch (Exception ex) {
    }

  }

  /**
   * Checks the schema of the xml WRT the given xsd and returns the result
   *
   * @param xsd the schema definition that will be used for verification
   * @param xml the xml that will be verified
   * @return the result of the verification process
   */
  public static void verifyXsd(Xsd xsd, byte[] xml) {
    verifyXsdStream(xsd, new ByteArrayInputStream(xml));
  }

  public static void verifyXsdStream(Xsd xsd, InputStream inputStream) {
    Schema schema = null;
    try {
      SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
      schemaFactory.setResourceResolver(xsd);
      schema = schemaFactory.newSchema(new StreamSource(xsd.getInputStream(), xsd.getSystemId()));
    } catch (Exception ex) {
      throw new RuntimeException("Unable to parse schema", ex);
    }

    try {
      Source xmlFile = new StreamSource(inputStream);
      Validator validator = schema.newValidator();
      validator.validate(xmlFile);

    } catch (Exception ex) {
      throw new RuntimeException("XML Verification failed", ex);
    }
  }

  public static void verifyXsdURL(String url, InputStream inputStream) {
    Schema schema = null;
    try {
      SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
      schema = schemaFactory.newSchema(new URL(url));
    } catch (Exception ex) {
      throw ExceptionUtils.asRuntimeException(ex);
    }

    try {
      Source xmlFile = new StreamSource(inputStream);
      Validator validator = schema.newValidator();
      validator.validate(xmlFile);

    } catch (Exception ex) {
      throw new RuntimeException("XML Verification failed", ex);
    }
  }

  public static void verifySchematron(byte[] sch, byte[] xml, Properties properties) {
    ByteArrayInputStream bSchematron = new ByteArrayInputStream(sch);
    ByteArrayInputStream bXml = new ByteArrayInputStream(xml);
    ByteArrayOutputStream bOut = new ByteArrayOutputStream();
    verifySchematronStream(bSchematron, bXml, bOut, properties);
    byte[] result = bOut.toByteArray();
    String str = new String(result);
    if (str.contains("failed")) {
      throw new RuntimeException("Schematron verification failed");
    }
  }

  /**
   * Simple transformation method.
   *
   * @param xslStream    - The input stream that the xsl will be read from
   * @param sourceStream - Input that the xml for verification will be read from.
   * @param outputStream - The output stream that the result will be written into.
   */
  public static void simpleTransformStream(InputStream xslStream, InputStream sourceStream, OutputStream outputStream,
                                           Properties properties) {
    try {
      Transformer transformer = tFactory.newTransformer(new StreamSource(xslStream));
      for (String property : properties.stringPropertyNames()) {
        transformer.setParameter(property, properties.getProperty(property));
      }
      transformer.transform(new StreamSource(sourceStream), new StreamResult(outputStream));
    } catch (Exception ex) {
      throw ExceptionUtils.asRuntimeException(ex);
    }
  }

  /**
   * Simple transformation method.
   *
   * @param xsl - The byte array that includes the xsl
   * @param xml - The byte array that includes the xml
   * @return result as byte []
   */
  public static void simpleTransform(byte[] xsl, byte[] xml, Properties properties) {
    ByteArrayInputStream bXsl = new ByteArrayInputStream(xsl);
    ByteArrayInputStream bXml = new ByteArrayInputStream(xml);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    simpleTransformStream(bXsl, bXml, baos, properties);
    baos.toByteArray();
  }

  /**
   * Performs schematron verification with the given schematrno file on the provided xml
   *
   * @param schematron
   * @param xml
   * @param result
   */
  public static void verifySchematronStream(InputStream schematron, InputStream xml, OutputStream result, Properties properties) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    simpleTransformStream(resolver.rstrm("iso_schematron_xslt2/iso_dsdl_include.xsl"), schematron, baos, properties);
    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
    baos.reset();
    simpleTransformStream(resolver.rstrm("iso_schematron_xslt2/iso_abstract_expand.xsl"), bais, baos, properties);
    bais = new ByteArrayInputStream(baos.toByteArray());
    baos.reset();
    simpleTransformStream(resolver.rstrm("iso_schematron_xslt2/iso_svrl_for_xslt2.xsl"), bais, baos, properties);
    bais = new ByteArrayInputStream(baos.toByteArray());
    baos.reset();
    simpleTransformStream(bais, xml, result, properties);
  }


  public static Xsd xsdFromByteArray(byte[] bytes) {
    return xsdFromByteArray(null, bytes, ArchiveType.PLAIN);
  }

  public static Xsd xsdFromByteArray(String name, byte[] bytes, ArchiveType archiveType) {
    return XsdFactory.xsdFromByteArray(name, bytes, archiveType);
  }

  public static Xsd xsdFromURL(String url) {
    return xsdFromURL(null, url, ArchiveType.PLAIN);
  }

  public static Xsd xsdFromURL(String name, String url, ArchiveType archiveType) {
    return XsdFactory.xsdFromURL(name, url, archiveType);
  }
}
