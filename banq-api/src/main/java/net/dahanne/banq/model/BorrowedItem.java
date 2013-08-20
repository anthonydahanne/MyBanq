package net.dahanne.banq.model;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by anthony on 13-08-15.
 */
public class BorrowedItem {

    private final String title;
    private final String shelfMark;
    private final Date borrowedDate;
    private final Date toBeReturnedBefore;
    private final String docNo;
    private final String userID;

    public BorrowedItem(String title, String shelfMark, Date borrowedDate, Date toBeReturnedBefore, String docNo, String userID) {
        this.title = title;
        this.shelfMark = shelfMark;
        this.borrowedDate = borrowedDate;
        this.toBeReturnedBefore = toBeReturnedBefore;
        this.docNo = docNo;
        this.userID = userID;
    }

    public String getUserID() {
        return userID;
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

    public String getDocNo() {
        return docNo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BorrowedItem that = (BorrowedItem) o;

        if (!borrowedDate.equals(that.borrowedDate)) return false;
        if (!docNo.equals(that.docNo)) return false;
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
        result = 31 * result + docNo.hashCode();
        return result;
    }

    public long getRemainingDays() {
        return Math.round((toBeReturnedBefore.getTime() - Calendar.getInstance().getTimeInMillis()) / (1000l * 60l * 60l * 24l));
    }
}
