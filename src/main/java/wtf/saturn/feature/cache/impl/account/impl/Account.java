package wtf.saturn.feature.cache.impl.account.impl;

import lombok.Getter;

/**
 * Represents an alt account
 *
 * @author aesthetical
 * @since 6/7/22
 */
@Getter
public class Account {
    private final String email, password;
    private final AccountType altType;

    public Account(String email, String password, AccountType altType) {
        this.email = email;
        this.password = password;
        this.altType = altType;
    }
}
