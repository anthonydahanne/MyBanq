package net.dahanne.banq.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by anthony on 13-08-15.
 */
public class Details {

    private final String name;
    private final Date expirationDate;
    private final String currentDebt;
    private final String userID;
    private final List<BorrowedItem> borrowedItems;

    public Details(String name, Date expirationDate, String currentDebt, String userID, List<BorrowedItem> borrowedItems) {
        this.name = name;
        this.expirationDate = expirationDate;
        this.currentDebt = currentDebt;
        this.userID = userID;
        this.borrowedItems = borrowedItems;
    }

    public String getUserID() {
        return userID;
    }

    public String getName() {
        return name;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public String getCurrentDebt() {
        return currentDebt;
    }

    public List<BorrowedItem> getBorrowedItems() {
        return borrowedItems;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Details details = (Details) o;

        if (!currentDebt.equals(details.currentDebt)) return false;
        if (!expirationDate.equals(details.expirationDate)) return false;
        if (!name.equals(details.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + expirationDate.hashCode();
        result = 31 * result + currentDebt.hashCode();
        return result;
    }
}
