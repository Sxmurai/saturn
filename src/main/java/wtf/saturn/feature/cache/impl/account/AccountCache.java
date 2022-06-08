package wtf.saturn.feature.cache.impl.account;

import wtf.saturn.feature.cache.BaseCache;
import wtf.saturn.feature.cache.Caches;
import wtf.saturn.feature.cache.impl.account.impl.Account;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class AccountCache extends BaseCache {
    private final List<Account> accounts = new CopyOnWriteArrayList<>();

    @Override
    public void init() {
        // TODO: load from file
    }

    public void add(Account alt) {
        accounts.add(alt);
    }

    public void remove(Account alt) {
        accounts.remove(alt);
    }

    public static AccountCache get() {
        return Caches.getCache(AccountCache.class);
    }
}
