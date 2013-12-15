package net.dahanne.banq.model;

import java.util.Date;

/**
 * Created by anthony on 12/12/2013.
 */
public class Reservation {

    private int id;
    private String title;
    private Date bookedSince;
    private String status;
    private int rank;

    public Reservation(int id, String title, Date bookedSince, String status, int rank) {
        this.id = id;
        this.title = title;
        this.bookedSince = bookedSince;
        this.status = status;
        this.rank = rank;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public Date getBookedSince() {
        return bookedSince;
    }

    public String getStatus() {
        return status;
    }

    public int getRank() {
        return rank;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Reservation that = (Reservation) o;

        if (id != that.id) return false;
        if (rank != that.rank) return false;
        if (bookedSince != null ? !bookedSince.equals(that.bookedSince) : that.bookedSince != null)
            return false;
        if (status != null ? !status.equals(that.status) : that.status != null) return false;
        if (title != null ? !title.equals(that.title) : that.title != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (bookedSince != null ? bookedSince.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + rank;
        return result;
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", bookedSince=" + bookedSince +
                ", status='" + status + '\'' +
                ", rank=" + rank +
                '}';
    }
}
