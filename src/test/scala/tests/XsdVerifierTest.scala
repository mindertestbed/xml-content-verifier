package tests

import java.nio.file.FileSystems

import org.beybunproject.xmlContentVerifier.utils.Utils
import org.beybunproject.xmlContentVerifier.{ArchiveType, XmlContentVerifier}
import org.specs2.mutable._

/**
  */
class XsdVerifierTest  extends Specification{
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
}
