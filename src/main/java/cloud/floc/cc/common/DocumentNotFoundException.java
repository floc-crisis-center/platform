package cloud.floc.cc.common;

public class DocumentNotFoundException
    extends Exception {

  public DocumentNotFoundException(String pMsg) {
    super(pMsg);
  }

  public DocumentNotFoundException(String pMsg, Throwable pCause) {
    super(pMsg, pCause);
  }
}

