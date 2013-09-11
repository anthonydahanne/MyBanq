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
    private final ItemType itemType;

    public BorrowedItem(String title, String shelfMark, Date borrowedDate, Date toBeReturnedBefore, String docNo, String userID, ItemType itemType) {
        this.title = title;
        this.shelfMark = shelfMark;
        this.borrowedDate = borrowedDate;
        this.toBeReturnedBefore = toBeReturnedBefore;
        this.docNo = docNo;
        this.userID = userID;
        this.itemType = itemType;
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

    public ItemType getItemType() {
        return itemType;
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
        // not a regular borrowed item
        if(toBeReturnedBefore ==  null) {
            return -1;
        }

        Calendar instance = Calendar.getInstance();
        // to make sure we have the same number of remaining days when the user opens the app
        // several times the same day
        instance.set(Calendar.MILLISECOND,0);
        instance.set(Calendar.SECOND,0);
        instance.set(Calendar.MINUTE,0);
        instance.set(Calendar.HOUR_OF_DAY,0);

        return Math.round((toBeReturnedBefore.getTime() - instance.getTimeInMillis()   ) / (1000l * 60l * 60l * 24l));
    }

    @Override
    public String toString() {
        return "BorrowedItem{" +
                "title='" + title + '\'' +
                ", shelfMark='" + shelfMark + '\'' +
                ", borrowedDate=" + borrowedDate +
                ", toBeReturnedBefore=" + toBeReturnedBefore +
                ", docNo='" + docNo + '\'' +
                ", userID='" + userID + '\'' +
                ", itemType='" + itemType + '\'' +
                '}';
    }
}
