//6, 32, 6, 32
package p;

import java.util.function.Supplier;

public class A {

    Supplier<String> a2 = () -> {
        Integer integer = new Integer(0);
        return integer.toString();
    };
}
