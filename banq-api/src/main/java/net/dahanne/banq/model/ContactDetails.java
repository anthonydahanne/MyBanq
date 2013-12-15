package net.dahanne.banq.model;

import java.util.Date;

/**
 * Created by anthony on 12/14/2013.
 */
public class ContactDetails {

    private String name;
    private Date expirationDate;
    private String accountNumber;
    private String address;
    private String phoneNumber;

    public ContactDetails(String name, Date expirationDate, String accountNumber, String address, String phoneNumber) {
        this.name = name;
        this.expirationDate = expirationDate;
        this.accountNumber = accountNumber;
        this.address = address;
        this.phoneNumber = phoneNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ContactDetails that = (ContactDetails) o;

        if (!accountNumber.equals(that.accountNumber)) return false;
        if (address != null ? !address.equals(that.address) : that.address != null) return false;
        if (expirationDate != null ? !expirationDate.equals(that.expirationDate) : that.expirationDate != null)
            return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (phoneNumber != null ? !phoneNumber.equals(that.phoneNumber) : that.phoneNumber != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (expirationDate != null ? expirationDate.hashCode() : 0);
        result = 31 * result + accountNumber.hashCode();
        result = 31 * result + (address != null ? address.hashCode() : 0);
        result = 31 * result + (phoneNumber != null ? phoneNumber.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ContactDetails{" +
                "name='" + name + '\'' +
                ", expirationDate=" + expirationDate +
                ", accountNumber='" + accountNumber + '\'' +
                ", address='" + address + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                '}';
    }
}
