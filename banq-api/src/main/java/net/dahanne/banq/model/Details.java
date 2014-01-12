package net.dahanne.banq.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by anthony on 13-08-15.
 */
public class Details {

    private final String name;
    private final String currentDebt;
    private final String lateFeesToCome;
    private final int messagesNumber;
    private final int reservationsNumber;
    private final String importantMessage;
    private final String objId;


    private final List<BorrowedItem> borrowedItems = new ArrayList<BorrowedItem>();

    public Details(String name, String currentDebt, String lateFeesToCome, int messagesNumber, int reservationsNumber, String importantMessage, String objId, List<BorrowedItem> borrowedItems) {
        this.name = name;
        this.currentDebt = currentDebt;
        this.lateFeesToCome = lateFeesToCome;
        this.messagesNumber = messagesNumber;
        this.reservationsNumber = reservationsNumber;
        this.importantMessage = importantMessage;
        this.objId = objId;
        this.borrowedItems.addAll(borrowedItems);
    }

    public Details(String name, String currentDebt, String lateFeesToCome, int messagesNumber, int reservationsNumber, String importantMessage, String objId) {
        this.name = name;
        this.currentDebt = currentDebt;
        this.lateFeesToCome = lateFeesToCome;
        this.messagesNumber = messagesNumber;
        this.reservationsNumber = reservationsNumber;
        this.importantMessage = importantMessage;
        this.objId = objId;

    }

    public String getName() {
        return name;
    }

    public String getCurrentDebt() {
        return currentDebt;
    }

    public String getLateFeesToCome() {
        return lateFeesToCome;
    }

    public int getMessagesNumber() {
        return messagesNumber;
    }

    public int getReservationsNumber() {
        return reservationsNumber;
    }

    public List<BorrowedItem> getBorrowedItems() {
        return borrowedItems;
    }

    public String getImportantMessage() {
        return importantMessage;
    }

    public String getObjId() {
        return objId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Details details = (Details) o;

        if (messagesNumber != details.messagesNumber) return false;
        if (reservationsNumber != details.reservationsNumber) return false;
        if (borrowedItems != null ? !borrowedItems.equals(details.borrowedItems) : details.borrowedItems != null)
            return false;
        if (currentDebt != null ? !currentDebt.equals(details.currentDebt) : details.currentDebt != null)
            return false;
        if (importantMessage != null ? !importantMessage.equals(details.importantMessage) : details.importantMessage != null)
            return false;
        if (lateFeesToCome != null ? !lateFeesToCome.equals(details.lateFeesToCome) : details.lateFeesToCome != null)
            return false;
        if (name != null ? !name.equals(details.name) : details.name != null) return false;
        if (objId != null ? !objId.equals(details.objId) : details.objId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (currentDebt != null ? currentDebt.hashCode() : 0);
        result = 31 * result + (lateFeesToCome != null ? lateFeesToCome.hashCode() : 0);
        result = 31 * result + messagesNumber;
        result = 31 * result + reservationsNumber;
        result = 31 * result + (importantMessage != null ? importantMessage.hashCode() : 0);
        result = 31 * result + (objId != null ? objId.hashCode() : 0);
        result = 31 * result + (borrowedItems != null ? borrowedItems.hashCode() : 0);
        return result;
    }


    @Override
    public String toString() {
        return "Details{" +
                "name='" + name + '\'' +
                ", currentDebt='" + currentDebt + '\'' +
                ", lateFeesToCome='" + lateFeesToCome + '\'' +
                ", messagesNumber=" + messagesNumber +
                ", reservationsNumber=" + reservationsNumber +
                ", importantMessage='" + importantMessage + '\'' +
                ", objId='" + objId + '\'' +
                ", borrowedItems=" + borrowedItems +
                '}';
    }
}
