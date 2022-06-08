package wtf.saturn.feature.impl;

import lombok.Getter;
import wtf.saturn.util.Globals;

/**
 * A base feature
 *
 * @author aesthetical
 * @since 6/7/22
 */
@Getter
public class Feature implements Globals {
    private final String name;

    public Feature(String name) {
        this.name = name;
    }
}
