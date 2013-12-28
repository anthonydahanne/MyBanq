package net.dahanne.banq.model;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by anthony on 13-08-15.
 */
public class BorrowedItem {

    private final String title;
    private final String authorInfo;
    private final String documentLocation;
    private final Date borrowedDate;
    private final Date toBeReturnedBefore;
    private final String docNo;
    private final boolean isRenewable;
    private final String lateFees;

    public BorrowedItem(String title, String authorInfo, String documentLocation, Date borrowedDate, Date toBeReturnedBefore, String docNo, boolean isRenewable, String lateFees) {
        this.title = title;
        this.authorInfo = authorInfo;
        this.documentLocation = documentLocation;
        this.borrowedDate = borrowedDate;
        this.toBeReturnedBefore = toBeReturnedBefore;
        this.docNo = docNo;
        this.isRenewable = isRenewable;
        this.lateFees = lateFees;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthorInfo() {
        return authorInfo;
    }

    public String getDocumentLocation() {
        return documentLocation;
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

    public boolean isRenewable() {
        return isRenewable;
    }

    public String getLateFees() {
        return lateFees;
    }

    public long getRemainingDays() {
        // not a regular borrowed item
        if (toBeReturnedBefore == null) {
            return -1;
        }

        Calendar instance = Calendar.getInstance();
        // to make sure we have the same number of remaining days when the user opens the app
        // several times the same day
        instance.set(Calendar.MILLISECOND, 0);
        instance.set(Calendar.SECOND, 0);
        instance.set(Calendar.MINUTE, 0);
        instance.set(Calendar.HOUR_OF_DAY, 0);

        return Math.round((toBeReturnedBefore.getTime() - instance.getTimeInMillis()) / (1000l * 60l * 60l * 24l));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BorrowedItem that = (BorrowedItem) o;

        if (isRenewable != that.isRenewable) return false;
        if (authorInfo != null ? !authorInfo.equals(that.authorInfo) : that.authorInfo != null)
            return false;
        if (borrowedDate != null ? !borrowedDate.equals(that.borrowedDate) : that.borrowedDate != null)
            return false;
        if (docNo != null ? !docNo.equals(that.docNo) : that.docNo != null) return false;
        if (documentLocation != null ? !documentLocation.equals(that.documentLocation) : that.documentLocation != null)
            return false;
        if (lateFees != null ? !lateFees.equals(that.lateFees) : that.lateFees != null)
            return false;
        if (title != null ? !title.equals(that.title) : that.title != null) return false;
        if (toBeReturnedBefore != null ? !toBeReturnedBefore.equals(that.toBeReturnedBefore) : that.toBeReturnedBefore != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = title != null ? title.hashCode() : 0;
        result = 31 * result + (authorInfo != null ? authorInfo.hashCode() : 0);
        result = 31 * result + (documentLocation != null ? documentLocation.hashCode() : 0);
        result = 31 * result + (borrowedDate != null ? borrowedDate.hashCode() : 0);
        result = 31 * result + (toBeReturnedBefore != null ? toBeReturnedBefore.hashCode() : 0);
        result = 31 * result + (docNo != null ? docNo.hashCode() : 0);
        result = 31 * result + (isRenewable ? 1 : 0);
        result = 31 * result + (lateFees != null ? lateFees.hashCode() : 0);
        return result;
    }


    @Override
    public String toString() {
        return "BorrowedItem{" +
                "title='" + title + '\'' +
                ", authorInfo='" + authorInfo + '\'' +
                ", documentLocation='" + documentLocation + '\'' +
                ", borrowedDate=" + borrowedDate +
                ", toBeReturnedBefore=" + toBeReturnedBefore +
                ", docNo='" + docNo + '\'' +
                ", isRenewable=" + isRenewable +
                ", lateFees='" + lateFees + '\'' +
                '}';
    }
}
