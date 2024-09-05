package ca.bc.gov.nrs.wfprev.common.exceptions;

public class ServiceException extends Exception {

  public ServiceException(Throwable e) {
    super(e);
  }

  public ServiceException(String message, Throwable e) {
    super(message, e);
  }

  public ServiceException(String message) {
    super(message);
  }
  
}
