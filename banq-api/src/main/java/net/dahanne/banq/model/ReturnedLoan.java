package net.dahanne.banq.model;

import java.util.Date;

/**
 * Created by anthony on 12/14/2013.
 */
public class ReturnedLoan {

    private String title;
    private Date borrowedDate;
    private Date returnedDate;

    public ReturnedLoan(String title, Date borrowedDate, Date returnedDate) {
        this.title = title;
        this.borrowedDate = borrowedDate;
        this.returnedDate = returnedDate;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ReturnedLoan returnedLoan = (ReturnedLoan) o;

        if (!borrowedDate.equals(returnedLoan.borrowedDate)) return false;
        if (!returnedDate.equals(returnedLoan.returnedDate)) return false;
        if (!title.equals(returnedLoan.title)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = title.hashCode();
        result = 31 * result + borrowedDate.hashCode();
        result = 31 * result + returnedDate.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ReturnedLoan{" +
                "title='" + title + '\'' +
                ", borrowedDate=" + borrowedDate +
                ", returnedDate=" + returnedDate +
                '}';
    }
}
