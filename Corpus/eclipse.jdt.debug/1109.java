/**
 * test case for https://bugs.eclipse.org/bugs/show_bug.cgi?id=384458
 */
public class LocalVariableTests2 {

    static String[] a = new String[] { "0", "1", "2" };

    public static String att = "something";

    public void m1() {
        int[] a = new int[] { 101, 102 };
        System.err.println(a[1]);
    }

    public void m2(String att) {
        if (att == null) {
            System.out.println("att is null");
        } else {
            System.out.println("att is not null");
        }
    }

    public static void main(String[] args) {
        LocalVariableTests2 t2 = new LocalVariableTests2();
        t2.m1();
        t2.m2(null);
    }
}
