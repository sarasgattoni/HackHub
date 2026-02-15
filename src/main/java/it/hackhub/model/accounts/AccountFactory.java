package it.hackhub.model.accounts;

import it.hackhub.model.valueobjs.Email;
import it.hackhub.model.valueobjs.UserPassword;

public abstract class AccountFactory {

    public final Account createAccount(String name, String surname, Email email, UserPassword password) {

        Account account = makeAccount();
        account.setEmail(email);
        account.setPassword(password);
        configureSpecifics(account, name, surname);

        return account;
    }

    protected abstract Account makeAccount();

    protected abstract void configureSpecifics(Account account, String name, String surname);
}