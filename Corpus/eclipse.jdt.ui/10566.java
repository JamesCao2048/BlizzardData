package p;

interface SAM1 {

    X apply(X x1);
}

interface SAM2 {

    X apply(X x1);
}

interface SAM3 {

    X apply(X x1);
}

interface SAM4 {

    X apply(X x1);
}

interface SAM5 {

    X apply(X x1);
}

interface SAM6 {

    X apply(X x1);
}

interface SAM7 {

    X apply(X x1);
}

class Base {

    X method1(X x) {
        return x;
    }

    X method2(X x) {
        return x;
    }

    X method3(X x2) {
        return x2;
    }
}

public class Util extends Base {

    X method1(X x) {
        return new X("created in method1");
    }

    X method2(X x) {
        return new X("created in method2");
    }

    String static_method(X x1) {
        SAM1 sam1 = X::<>staticMethod;
        return sam1.apply(x1).m2();
    }

    String instance_method_via_type(X x2) {
        SAM2 sam2 = p.X::<>instanceMethod;
        return sam2.apply(x2).m2();
    }

    String constructor_ref(X x3) {
        SAM3 sam3 = X::<>new;
        return sam3.apply(x3).m2();
    }

    String this_method1(X x4) {
        SAM4 sam4 = this::<>method1;
        return sam4.apply(x4).m2();
    }

    String super_method2(X x5) {
        SAM5 sam5 = super::<>method2;
        return sam5.apply(x5).m2();
    }

    String instance_method2(X x6) {
        SAM6 sam6 = x6::<>instanceMethod2;
        return sam6.apply(x6).m2();
    }

    String instance_method2_via_field(X x7) {
        SAM7 sam7 = x7.field::<>instanceMethod2;
        return sam7.apply(x7).m2();
    }

    public static void check(String expected, String actual) {
        if (!expected.equals(actual)) {
            System.out.println(expected + " != " + actual);
        } else {
            System.out.println("OK: " + expected);
        }
    }

    public static void main(String[] args) {
        Util util = new Util();
        check("static_method", util.static_method(new X("static_method")));
        check("instance_method_via_type", util.instance_method_via_type(new X("instance_method_via_type")));
        check("copy of constructor_ref", util.constructor_ref(new X("constructor_ref")));
        check("created in method1", util.this_method1(new X("this_method1")));
        check("super_method2", util.super_method2(new X("super_method2")));
        check("instance_method2", util.instance_method2(new X("instance_method2")));
        check("instance_method2_via_field", util.instance_method2_via_field(new X("instance_method2_via_field")));
    }
}
