package basic_sbfl_engine.model;

public class Suspiciousness implements Comparable<Suspiciousness> {

    private final String className;
    private final int line;
    private final double value;

    public Suspiciousness(String className, int line, double value) {
        this.className = className;
        this.line = line;
        this.value = value;
    }

    public String getClassName() {
        return className;
    }

    public int getLine() {
        return line;
    }

    public double getValue() {
        return value;
    }

    /* =====================
     * 並び替え（降順）
     * ===================== */

    @Override
    public int compareTo(Suspiciousness other) {
        return Double.compare(other.value, this.value);
    }

    @Override
    public String toString() {
        return String.format(
            "%s:%d -> %f",
            className,
            line,
            value
        );
    }
}
