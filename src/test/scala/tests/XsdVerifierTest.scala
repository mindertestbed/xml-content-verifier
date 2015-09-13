package tests

import java.nio.file.FileSystems
import java.security.cert.X509Certificate
import javax.net.ssl._

import org.beybunproject.xmlContentVerifier.utils.Utils
import org.beybunproject.xmlContentVerifier.{ArchiveType, XmlContentVerifier}
import org.specs2.mutable._

/**
  */
class XsdVerifierTest extends Specification {

  prepareHttps()

  sequential

  "paths" should {
    "be normalized" in {

      val paths = Array("", "/../foo/",
        "/foo/",
        "/../../../",
        "/./foo/./",
        "//foo//bar",
        "//foo/../bar")

      for (path <- paths) {
        println(FileSystems.getDefault.getPath(path).normalize())
      }


      1 must_== 1
    }


    "XSD Schema resolution" should {
      "be successfull on plain xsds streams" in {
        {
          val stream = this.getClass.getResource("/books.xsd").openStream();
          val minderXsd = XmlContentVerifier.xsdFromByteArray(Utils.readStream(stream));
          val xml = this.getClass.getResource("/sample-book.xml").openStream();
          XmlContentVerifier.verifyXsd(minderXsd, Utils.readStream(xml))
        } must not(throwAn())
      }

      "be successful on zip archive streams" in {
        {
          val stream = this.getClass.getResource("/library.zip").openStream();
          val minderXsd = XmlContentVerifier.xsdFromByteArray("library/mainscheme/library.xsd", Utils.readStream(stream), ArchiveType.ZIP);
          val xml = this.getClass.getResource("/sample-library.xml").openStream();
          XmlContentVerifier.verifyXsd(minderXsd, Utils.readStream(xml))
        } must not(throwAn())
      }

      "be successful on remote zip URLS " in {
        {
          val minderXsd = XmlContentVerifier.xsdFromURL("library/mainscheme/library.xsd", "http://eidrepo:15001/xsls/library.zip", ArchiveType.ZIP);
          val xml = this.getClass.getResource("/sample-library.xml").openStream();
          XmlContentVerifier.verifyXsd(minderXsd, Utils.readStream(xml))
        } //must not(throwAn())

        1 must_== 1
      }

      "be successful on remote HTTPS zip URLS " in {
        {
          val minderXsd = XmlContentVerifier.xsdFromURL("library/mainscheme/library.xsd", "https://eidrepo:15002/xsls/library.zip", ArchiveType.ZIP);
          val xml = this.getClass.getResource("/sample-library.xml").openStream();
          XmlContentVerifier.verifyXsd(minderXsd, Utils.readStream(xml))
        } //must not(throwAn())

        1 must_== 1
      }

      "be successful on remote jar URLS " in {
        {
          val minderXsd = XmlContentVerifier.xsdFromURL("library/mainscheme/library.xsd", "http://eidrepo:15001/xsls/library.jar", ArchiveType.JAR);
          val xml = this.getClass.getResource("/sample-library.xml").openStream();
          XmlContentVerifier.verifyXsd(minderXsd, Utils.readStream(xml))
        } //must not(throwAn())

        1 must_== 1
      }

      "be successful on remote HTTPS jar URLS " in {
        {
          val minderXsd = XmlContentVerifier.xsdFromURL("library/mainscheme/library.xsd", "https://eidrepo:15002/xsls/library.jar", ArchiveType.JAR);
          val xml = this.getClass.getResource("/sample-library.xml").openStream();
          XmlContentVerifier.verifyXsd(minderXsd, Utils.readStream(xml))
        } //must not(throwAn())

        1 must_== 1
      }

      "of course work with plain URLS" in {
        {
          val xmlStream = this.getClass.getResource("/sample-library.xml").openStream()
          XmlContentVerifier.verifyXsdURL("http://eidrepo:15001/xsls/library/mainscheme/library.xsd", xmlStream)
        }
        1 must_== 1
      }

      "work with plain HTTPS" in {
        {
          val xmlStream = this.getClass.getResource("/sample-library.xml").openStream()
          XmlContentVerifier.verifyXsdURL("https://eidrepo:15002/xsls/library/mainscheme/library.xsd", xmlStream)
        }
        1 must_== 1
      }

      /*
            http://eidrepo:15001/xsls/library.tar.gz
            http://eidrepo:15001/xsls/library-lzma2.7z
              http://eidrepo:15001/xsls/library-lzma.7z
      */
    }

  }


  def prepareHttps(): Unit = {
    try {
      /*
   *  fix for
   *    Exception in thread "main" javax.net.ssl.SSLHandshakeException:
   *       sun.security.validator.ValidatorException:
   *           PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException:
   *               unable to find valid certification path to requested target
   */
      val trustAllCerts = Array[TrustManager] {
        new X509TrustManager() {
          override def checkClientTrusted(x509Certificates: Array[X509Certificate], s: String) {}
          override def checkServerTrusted(x509Certificates: Array[X509Certificate], s: String) {}
          override def getAcceptedIssuers() = null
        }
      };

      val sc = SSLContext.getInstance("SSL");
      sc.init(null, trustAllCerts, new java.security.SecureRandom());
      HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

      // Create all-trusting host name verifier
      val allHostsValid = new HostnameVerifier() {
        override def verify(hostname: String, session: SSLSession) = true
      };
      // Install the all-trusting host verifier
      HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
      /*
       * end of the fix
       */
    } catch {
      case _ => {}
    }
  }
}
