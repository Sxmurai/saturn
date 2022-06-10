package wtf.saturn.util.render.scissor;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Vec4d {
    private double x, y, x2, y2;

    public Vec4d(double x, double y, double x2, double y2) {
        this.x = x;
        this.y = y;
        this.x2 = x2;
        this.y2 = y2;
    }
}
