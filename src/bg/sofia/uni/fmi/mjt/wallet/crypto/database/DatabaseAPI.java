package bg.sofia.uni.fmi.mjt.wallet.crypto.database;

import bg.sofia.uni.fmi.mjt.wallet.crypto.account.Account;

import java.util.Set;

public interface DatabaseAPI {

    /**
     * Updates database sata and then saves to file
     *
     * @param accounts Accounts to be added or updated in the database
     */
    void updateData(Set<Account> accounts);

    /**
     * Shuts the scheduler down and then saves to file
     *
     * @param accounts Accounts to be added or updated in the database
     */
    void shutdownScheduler(Set<Account> accounts);
}
