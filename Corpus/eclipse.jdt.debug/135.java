import java.math.BigInteger;

public class StepFilterFour {

    public  StepFilterFour() {
    }

    byte b;

    int i;

    short s;

    long l;

    double d;

    float f;

    Object o;

    public byte getB() {
        return b;
    }

    public void setB(byte b) {
        this.b = b;
    }

    public int getI() {
        return i;
    }

    public void setI(int i) {
        this.i = i;
    }

    public short getS() {
        return s;
    }

    public void setS(short s) {
        this.s = s;
    }

    public long getL() {
        return l;
    }

    public void setL(long l) {
        this.l = l;
    }

    public double getD() {
        return d;
    }

    public void setD(double d) {
        this.d = d;
    }

    public float getF() {
        return f;
    }

    public void setF(float f) {
        this.f = f;
    }

    public Object getO() {
        return o;
    }

    public void setO(Object o) {
        this.o = o;
    }

    private double sum() {
        return b + i + s + l + d + f;
    }

    public static void main(String[] args) {
        StepFilterFour sf1 = new StepFilterFour();
        sf1.go();
    }

    void go() {
        Object o2 = new Object();
        // All these statements should single step nicely with the
        // getter and setter filter turned on.
        this.setI(22);
        this.setL(32);
        this.setD(123.0);
        this.setF(32.0F);
        this.setO(o2);
        // these, too
        boolean isBig = this.getI() > 20;
        isBig &= this.getL() < 20;
        isBig &= this.getD() % 5.0 == 4;
        isBig &= this.getF() % 3.0F == 4.0F;
        isBig &= this.getO() instanceof BigInteger;
        if (// single stepping into this should land us in sum()
        this.getI() == 22 && this.sum() > 50) {
            this.doNothing("fun " + (isBig ? " and big" : " and medium"));
        }
        this.doNothing("f was = " + this.getF());
        // even this should fall right through
        this.setF(this.getF() + 1.0F);
        this.doNothing("f is = " + this.getF());
    }

    private void doNothing(String string) {
    }

    // This added so w don't suddenly step into the classloader from Launcher
    private BigInteger triggerLoad = BigInteger.valueOf(42);
}
