package net.dahanne.banq.model;

import java.util.Date;

/**
 * Created by anthony on 13-08-15.
 */
public class BorrowedItem {

    private String title ;
    private String shelfMark ;
    private Date borrowedDate;
    private Date toBeReturnedBefore ;

    public BorrowedItem(String title, String shelfMark, Date borrowedDate, Date toBeReturnedBefore) {
        this.title = title;
        this.shelfMark = shelfMark;
        this.borrowedDate = borrowedDate;
        this.toBeReturnedBefore = toBeReturnedBefore;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BorrowedItem that = (BorrowedItem) o;

        if (!borrowedDate.equals(that.borrowedDate)) return false;
        if (!shelfMark.equals(that.shelfMark)) return false;
        if (!title.equals(that.title)) return false;
        if (!toBeReturnedBefore.equals(that.toBeReturnedBefore)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = title.hashCode();
        result = 31 * result + shelfMark.hashCode();
        result = 31 * result + borrowedDate.hashCode();
        result = 31 * result + toBeReturnedBefore.hashCode();
        return result;
    }

    public String getTitle() {
        return title;
    }

    public String getShelfMark() {
        return shelfMark;
    }

    public Date getBorrowedDate() {
        return borrowedDate;
    }

    public Date getToBeReturnedBefore() {
        return toBeReturnedBefore;
    }
}
