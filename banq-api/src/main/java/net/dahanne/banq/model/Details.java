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

    private final List<BorrowedItem> borrowedItems;

    public Details(String name, Date expirationDate, String currentDebt, List<BorrowedItem> borrowedItems) {
        this.name = name;
        this.expirationDate = expirationDate;
        this.currentDebt = currentDebt;
        this.borrowedItems = borrowedItems;
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
}
