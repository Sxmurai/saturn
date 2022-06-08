package wtf.saturn.launch;

/**
 * The environment the client was launched in
 *
 * @author aesthetical
 * @since 6/7/22
 */
public enum ClientEnvironment {
    STABLE("Stable"),
    DEVELOPER("Developer");

    public final String friendlyName;

    ClientEnvironment(String friendlyName) {
        this.friendlyName = friendlyName;
    }
}
