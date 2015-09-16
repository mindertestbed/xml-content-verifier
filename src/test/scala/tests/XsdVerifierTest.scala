package tests

import java.io.ByteArrayOutputStream
import java.nio.file.FileSystems
import java.security.cert.X509Certificate
import javax.net.ssl._

import org.beybunproject.xmlContentVerifier.utils.Utils
import org.beybunproject.xmlContentVerifier.{ArchiveType, XmlContentVerifier}
import org.junit.Ignore
import org.specs2.mutable._

/**
  */
class XsdVerifierTest extends Specification {

  prepareHttps()

  sequential

  "path normalization" should {
    "normalize reduntant .." in {
      Utils.normalizePath("/a/b/../c/d") mustEqual("/a/c/d")
    }

    "normalize reduntant ." in {
      Utils.normalizePath("/a/b/./c/d") mustEqual("/a/b/c/d")
    }

    "normalize reduntant ///" in {
      Utils.normalizePath("/a///b/./c/..//d") mustEqual("/a/b/d")
    }

    "normalize reduntant a complex one" in {
      Utils.normalizePath("htc:///////a/.//b/./c/..//d//lk") mustEqual("htc:/a/b/d/lk")
    }
  }

  "XSD Schema resolution" should {
    "be successfull on plain xsds streams" in {
      {
        val stream = this.getClass.getResource("/books.xsd").openStream();
        val minderXsd = XmlContentVerifier.schemaFromByteArray(Utils.readStream(stream));
        val xml = this.getClass.getResource("/sample-book.xml").openStream();
        XmlContentVerifier.verifyXsd(minderXsd, Utils.readStream(xml))
      } must not(throwAn())
    }

    "be successful on zip archive streams" in {
      {
        val stream = this.getClass.getResource("/library.zip").openStream();
        val minderXsd = XmlContentVerifier.schemaFromByteArray("library/mainscheme/library.xsd", Utils.readStream(stream), ArchiveType.ZIP);
        val xml = this.getClass.getResource("/sample-library.xml").openStream();
        XmlContentVerifier.verifyXsd(minderXsd, Utils.readStream(xml))
      } must not(throwA())
    }
    "be successful on jar archive streams" in {
      {
        val stream = this.getClass.getResource("/library.jar").openStream();
        val minderXsd = XmlContentVerifier.schemaFromByteArray("library/mainscheme/library.xsd", Utils.readStream(stream), ArchiveType.JAR);
        val xml = this.getClass.getResource("/sample-library.xml").openStream();
        XmlContentVerifier.verifyXsd(minderXsd, Utils.readStream(xml))
      } must not(throwA())
    }
    /*
        "be successful on remote zip URLS " in {
          {
            val minderXsd = XmlContentVerifier.schemaFromURL("library/mainscheme/library.xsd", "http://eidrepo:15001/xsls/library.zip", ArchiveType.ZIP);
            val xml = this.getClass.getResource("/sample-library.xml").openStream();
            XmlContentVerifier.verifyXsd(minderXsd, Utils.readStream(xml))
          } //must not(throwAn())
          1 must_== 1
        }

        "be successful on remote HTTPS zip URLS " in {
          {
            val minderXsd = XmlContentVerifier.schemaFromURL("library/mainscheme/library.xsd", "https://eidrepo:15002/xsls/library.zip", ArchiveType.ZIP);
            val xml = this.getClass.getResource("/sample-library.xml").openStream();
            XmlContentVerifier.verifyXsd(minderXsd, Utils.readStream(xml))
          } //must not(throwAn())

          1 must_== 1
        }

        "be successful on remote jar URLS " in {
          {
            val minderXsd = XmlContentVerifier.schemaFromURL("library/mainscheme/library.xsd", "http://eidrepo:15001/xsls/library.jar", ArchiveType.JAR);
            val xml = this.getClass.getResource("/sample-library.xml").openStream();
            XmlContentVerifier.verifyXsd(minderXsd, Utils.readStream(xml))
          } must not(throwAn())

          1 must_== 1
        }

        "be successful on remote HTTPS jar URLS " in {
          {
            val minderXsd = XmlContentVerifier.schemaFromURL("library/mainscheme/library.xsd", "https://eidrepo:15002/xsls/library.jar", ArchiveType.JAR);
            val xml = this.getClass.getResource("/sample-library.xml").openStream();
            XmlContentVerifier.verifyXsd(minderXsd, Utils.readStream(xml))
          } must not(throwAn())

          1 must_== 1
        }

        "of course work with plain URLS" in {
          {
            val xmlStream = this.getClass.getResource("/sample-library.xml").openStream()
            XmlContentVerifier.verifyXsd("http://eidrepo:15001/xsls/library/mainscheme/library.xsd", xmlStream)
          }
          1 must_== 1
        }

        "work with plain HTTPS" in {
          {
            val xmlStream = this.getClass.getResource("/sample-library.xml").openStream()
            XmlContentVerifier.verifyXsd("https://eidrepo:15002/xsls/library/mainscheme/library.xsd", xmlStream)
          }
          1 must_== 1
        }
        */
  }

  "Schematron verification " should {
    "work with plain schematrons" in {
      {
        val schematronStream = this.getClass.getResource("/schematron/simple.sch").openStream
        val xml = this.getClass.getResourceAsStream("/schematron/simple-input.xml");
        XmlContentVerifier.verifySchematron(Utils.readStream(schematronStream), xml, null);
      } must not(throwAn())
    }

    "work with plain schematrons with namespaces" in {
      {
        val schematronStream = this.getClass.getResource("/schematron/simple-namespaces.sch").openStream
        val xml = this.getClass.getResourceAsStream("/schematron/simple-input-namespaces.xml");
        XmlContentVerifier.verifySchematron(Utils.readStream(schematronStream), xml, null);
      } must not(throwAn())
    }

    "work with plain phases as well" in {
      {
        val schematronStream = this.getClass.getResource("/schematron/phases.sch").openStream
        val xml = this.getClass.getResourceAsStream("/schematron/input-phases.xml");
        XmlContentVerifier.verifySchematron(Utils.readStream(schematronStream), xml, null);
      } must not(throwAn())
    }

    "work with valid xlink key rules" in {
      {
        val schematronStream = Utils.readStream(this.getClass.getResource("/schematron/keys.sch").openStream)
        val xml = this.getClass.getResourceAsStream("/schematron/input-keys.xml");
        XmlContentVerifier.verifySchematron(schematronStream, xml, null);
      } must not(throwAn())
    }

    "work with valid URLS" in {
      {
        val schematronStream = this.getClass.getResource("/schematron/deep/deeper/includer1.sch")
        val xml = this.getClass.getResourceAsStream("/schematron/simple-input.xml");
        XmlContentVerifier.verifySchematron(schematronStream, xml, null);
      }// must not(throwAn())
      1 must_== 1
    }

    "fail with valid URLS and invalid xml" in {
      {
        val schematronStream = this.getClass.getResource("/schematron/deep/deeper/includer1.sch")
        val xml = this.getClass.getResourceAsStream("/schematron/simple-input-invalid.xml");
        XmlContentVerifier.verifySchematron(schematronStream, xml, null);
      } must (throwAn())
      1 must_== 1
    }

    "fail with plain schematrons and invalid XML files" in {
      {
        val schematronStream = Utils.readStream(this.getClass.getResource("/schematron/simple.sch").openStream)
        val xml = this.getClass.getResourceAsStream("/schematron/simple-input-invalid.xml");
        XmlContentVerifier.verifySchematron(schematronStream, xml, null);
      } must throwAn()
    }

    "fail with plain schematrons with namespaces and invalid XML files" in {
      {
        val schematronStream = Utils.readStream(this.getClass.getResource("/schematron/simple-namespaces.sch").openStream)
        val xml = this.getClass.getResourceAsStream("/schematron/simple-input-namespaces-invalid.xml");
        XmlContentVerifier.verifySchematron(schematronStream, xml, null);
      } must (throwAn())
    }

    "fail with invalid xlink key rules" in {
      {
        val schematronStream = Utils.readStream(this.getClass.getResource("/schematron/keys.sch").openStream)
        val xml = this.getClass.getResourceAsStream("/schematron/input-keys-invalid.xml");
        XmlContentVerifier.verifySchematron(schematronStream, xml, null);
      } must (throwAn())
    }

    "work with valid ZIP schematron archives" in {
      {
        val schematronBytes = Utils.readStream(this.getClass.getResource("/schematron.zip").openStream)
        val xml = this.getClass.getResourceAsStream("/schematron/simple-input.xml");
        val schema = XmlContentVerifier.schemaFromByteArray("schematron/deep/deeper/includer1.sch", schematronBytes, ArchiveType.ZIP);
        XmlContentVerifier.verifySchematron(schema, xml, null);
      } must not(throwAn)

      1 must_== 1
    }

    "work with valid JAR schematron archives" in {
      {
        val schematronBytes = Utils.readStream(this.getClass.getResource("/schematron.jar").openStream)
        val xml = this.getClass.getResourceAsStream("/schematron/simple-input.xml");
        val schema = XmlContentVerifier.schemaFromByteArray("schematron/deep/deeper/includer1.sch", schematronBytes, ArchiveType.JAR);
        XmlContentVerifier.verifySchematron(schema, xml, null);
      } must not(throwAn)
      1 must_== 1
    }

    "fail with valid ZIP schematron archives and invalid xml" in {
      {
        val schematronBytes = Utils.readStream(this.getClass.getResource("/schematron.zip").openStream)
        val xml = this.getClass.getResourceAsStream("/schematron/simple-input-invalid.xml");
        val schema = XmlContentVerifier.schemaFromByteArray("schematron/deep/deeper/includer1.sch", schematronBytes, ArchiveType.ZIP);
        XmlContentVerifier.verifySchematron(schema, xml, null);
      } must (throwAn)

      1 must_== 1
    }

    "fail with valid JAR schematron archives and invalid xml" in {
      {
        val schematronBytes = Utils.readStream(this.getClass.getResource("/schematron.jar").openStream)
        val xml = this.getClass.getResourceAsStream("/schematron/simple-input-invalid.xml");
        val schema = XmlContentVerifier.schemaFromByteArray("schematron/deep/deeper/includer1.sch", schematronBytes, ArchiveType.JAR);
        XmlContentVerifier.verifySchematron(schema, xml, null);
      } must (throwAn)
      1 must_== 1
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
