package p;

import org.eclipse.jdt.annotation.NonNull;

public class B implements A {

    @Override
    public Integer getArea(@NonNull Integer length) {
        return new Integer(length * length);
    }
}
