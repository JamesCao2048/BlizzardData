//7, 28, 7, 34
package p;

import java.util.function.Consumer;

public class A {

    Consumer<Integer> a1 =  x -> {
        int x2 = x + 10;
        System.out.println(x2);
    };
}
