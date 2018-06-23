/**
 * Test unsupported @NoInstantiate annotation on fields in a class in the default package
 */
public class test12 {

    @NoInstantiate
    public Object f1 = null;

    @NoInstantiate
    protected int f2 = 0;

    @NoInstantiate
    private static char[] f3 = {};

    @NoInstantiate
    long f4 = 0L;
}
