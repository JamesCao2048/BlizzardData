import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@interface Anno {
  Class value();
}

public class C {

  @Anno(String.class)
  public int i;

  @Anno(Integer.class)
  public int j;

  public static void main(String []argv) {
    System.out.println(new C().i);
    System.out.println(new C().j);
  }
}
aspect X {
  before(): get(@Anno(value=String.class) * *) {}
}
