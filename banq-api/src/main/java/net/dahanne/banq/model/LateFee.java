package net.dahanne.banq.model;

/**
 * Created by anthony on 12/15/2013.
 */
public class LateFee {

    private String title;
    private String dateAsString;
    private String fee;
    private String feeType;
    private String feeId;

    public LateFee(String title, String dateAsString, String fee, String feeType, String feeId) {
        this.title = title;
        this.dateAsString = dateAsString;
        this.fee = fee;
        this.feeType = feeType;
        this.feeId = feeId;
    }

    public String getTitle() {
        return title;
    }

    public String getDateAsString() {
        return dateAsString;
    }

    public String getFee() {
        return fee;
    }

    public String getFeeType() {
        return feeType;
    }

    public String getFeeId() {
        return feeId;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LateFee lateFee = (LateFee) o;

        if (dateAsString != null ? !dateAsString.equals(lateFee.dateAsString) : lateFee.dateAsString != null)
            return false;
        if (fee != null ? !fee.equals(lateFee.fee) : lateFee.fee != null) return false;
        if (feeId != null ? !feeId.equals(lateFee.feeId) : lateFee.feeId != null) return false;
        if (feeType != null ? !feeType.equals(lateFee.feeType) : lateFee.feeType != null)
            return false;
        if (title != null ? !title.equals(lateFee.title) : lateFee.title != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = title != null ? title.hashCode() : 0;
        result = 31 * result + (dateAsString != null ? dateAsString.hashCode() : 0);
        result = 31 * result + (fee != null ? fee.hashCode() : 0);
        result = 31 * result + (feeType != null ? feeType.hashCode() : 0);
        result = 31 * result + (feeId != null ? feeId.hashCode() : 0);
        return result;
    }


    @Override
    public String toString() {
        return "LateFee{" +
                "title='" + title + '\'' +
                ", dateAsString='" + dateAsString + '\'' +
                ", fee='" + fee + '\'' +
                ", feeType='" + feeType + '\'' +
                ", feeId='" + feeId + '\'' +
                '}';
    }
}
