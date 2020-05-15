package cloud.floc.cc.common;

public class DocumentException
    extends Exception {

  public DocumentException(String pMsg) {
    super(pMsg);
  }

  public DocumentException(String pMsg, Throwable pCause) {
    super(pMsg, pCause);
  }
}

