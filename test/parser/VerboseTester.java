package parser;

import org.junit.runner.*;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;


public class VerboseTester extends RunListener {
  private enum ResultType {
    PASSED, FAILED, EXCEPTION;
  }
  private ResultType result;
  private long startMillis;
  private int fail, err;
  /*+******************************************************************/
  public void testRunStarted(Description desc) {
    fail = err = 0;
  }
  public void testStarted(Description desc) {
    //System.out.printf("Starting %s", desc.getDisplayName());
    result = ResultType.PASSED;
    startMillis = System.currentTimeMillis();
  }
  public void testFailure(Failure failure) {
    String trace = failure.getTrace();
    if( failure.getException() instanceof AssertionError) {
      trace = filter(trace);
      result = ResultType.FAILED;
      fail += 1;
    } else {
      result = ResultType.EXCEPTION;
      err += 1;
    }
    System.out.printf("%n%s: %s%n%s", result, failure.getMessage(), trace);
  }
  public void testRunFinished(Result r) {
    System.out.printf("%3d PASSED (of %d)%n", r.getRunCount()-fail-err, 
                      r.getRunCount());
    System.out.printf("%3d FAILED%n", fail);
    System.out.printf("%3d EXCEPTIONS%n", err);
  }
  /*+******************************************************************/
  private String filter(String stackTrace) {
    String[] traceLines = stackTrace.split("[\r\n]+");
    StringBuilder sb = new StringBuilder(stackTrace.length());
    int i = 1;
    while( traceLines[i].indexOf("org.junit.Assert")>=0 ) i+=1;
    for(; i<traceLines.length; i++) {
      sb.append(traceLines[i]).append('\n');
    }
    return sb.toString();
  }
  /*+******************************************************************/
  public void testFinished(Description desc) {
    long end = System.currentTimeMillis();
    System.out.printf("**** %s: %d ms (%s)%n", 
                      desc.getDisplayName(), end-startMillis, result);
  }
  /*+******************************************************************/
  public static void main(String[] argv) throws Exception {
    JUnitCore jucore = new JUnitCore();
    jucore.addListener(new VerboseTester());
    for(String className : argv) {
      Class c = Class.forName(className);
      jucore.run(c);
    }
  }
}
