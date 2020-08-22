// TESTING: testing a pure marker interface - no methods added
import org.aspectj.lang.annotation.*;

public class CaseR {
  public static void main(String[]argv) { 
	  CaseR cr = new CaseR();
	  System.out.println(cr instanceof I);
	  System.out.println(cr instanceof J);
  }
}

aspect X {
  @DeclareMixin(value="CaseR",interfaces={I.class})
  public static C createImplementation1() {return null;}
}

interface I {}
interface J {}

class C implements I,J {
  public void foo() {
    System.out.println("foo() running");
  }
  public void goo() {
    System.out.println("goo() running");
  }
}
