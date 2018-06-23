import java.util.List;
import java.util.ArrayList;

public class BreakpointsLocation {

    public void test1() {
        System.out.println("test");
        System.out.println("test");
    }

    public class InnerClass {

        public int foo() {
            return 1;
        }
    }

    private List fList = new ArrayList();

    public void test2(List list) {
        System.out.println(list);
    }

    public void randomCode() {
        new Runnable() {

            public void run() {
                System.out.println("test");
            }
        };
        int s = 3;
    }

    private int i = 3;

    public void code() {
        boolean i = 1 > 2;
        int j = 22;
        int s = j - 12;
    }

    public static void testMethodWithInnerClass(Object type) {
        class StaticInnerClass {

            protected  StaticInnerClass(Object t) {
                System.out.println("test");
            }
        }
    }
}
