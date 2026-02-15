package it.hackhub.model.accounts;

public class StaffFactory extends AccountFactory {

    @Override
    protected Account makeAccount() {
        return new StaffProfile();
    }

    @Override
    protected void configureSpecifics(Account account, String name, String surname) {
        if (account instanceof StaffProfile) {
            StaffProfile staff = (StaffProfile) account;
            staff.setName(name);
            staff.setSurname(surname);
        }
    }
}