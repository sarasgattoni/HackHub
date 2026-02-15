package it.hackhub.model.accounts;

public class UserFactory extends AccountFactory {

    @Override
    protected Account makeAccount() {
        return new User();
    }

    @Override
    protected void configureSpecifics(Account account, String name, String surname) {
        if (account instanceof User) {
            ((User) account).setUsername(name);
        }
    }
}