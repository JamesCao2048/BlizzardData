import java.lang.reflect.Field;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect public class Foo {

 static class X {
  public void m() {
    new RuntimeException("hello");
  }
 }
	
  public static void main(String[] argv) {
	    new X().m();
  }
		
  @Pointcut("call(Throwable+.new(String, ..)) && this(caller) && if()")
  public static boolean exceptionInitializer(Object caller) {
      System.out.println("In if(), is there a caller? "+(caller!=null?"yes":"no"));
      return true;
  }

  @Around("exceptionInitializer(caller)")
  public Object annotateException(ProceedingJoinPoint jp, Object caller) {
      return null;
  }
}
