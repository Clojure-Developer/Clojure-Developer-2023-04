package otus_05;

import clojure.lang.Counted;
import clojure.lang.Indexed;

public class Point implements Indexed {
    public Double x;
    public Double y;

    public Point() {
        this.x = 0.0;
        this.y = 0.0;
    }

    public Point(Double x, Double y) {
        this.x = x;
        this.y = y;
    }

    public String toString() {
        return "Point(" + x.toString() + ", " + y.toString() + ")";
    }

    public int count() {
        return 2;
    }

    public Object nth(int i) {
        return nth(i, null);
    }

    public Object nth(int i, Object notFound) {
        if (i == 0) { return x; }
        else if (i == 1) { return y; }
        else { return notFound; }
    }

    public Point offX(Double dx) {
        return new Point(x + dx, y);
    }

    public Point offY(Double dy) {
        return new Point(x, y + dy);
    }
}
