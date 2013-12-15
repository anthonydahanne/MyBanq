package net.dahanne.banq.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by anthony on 12/15/2013.
 */
public class LateFees {

    private final String currentDebt;
    private final List<LateFee> lateFees = new ArrayList<LateFee>();

    public LateFees(String currentDebt, List<LateFee> lateFees) {
        this.currentDebt = currentDebt;
        this.lateFees.addAll(lateFees);
    }

    public String getCurrentDebt() {
        return currentDebt;
    }

    public List<LateFee> getLateFees() {
        return lateFees;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LateFees lateFees1 = (LateFees) o;

        if (currentDebt != null ? !currentDebt.equals(lateFees1.currentDebt) : lateFees1.currentDebt != null)
            return false;
        if (lateFees != null ? !lateFees.equals(lateFees1.lateFees) : lateFees1.lateFees != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = currentDebt != null ? currentDebt.hashCode() : 0;
        result = 31 * result + (lateFees != null ? lateFees.hashCode() : 0);
        return result;
    }


    @Override
    public String toString() {
        return "LateFees{" +
                "currentDebt='" + currentDebt + '\'' +
                ", lateFees=" + lateFees +
                '}';
    }
}
