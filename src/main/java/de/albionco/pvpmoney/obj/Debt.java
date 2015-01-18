package de.albionco.pvpmoney.obj;

/**
 * Created by Connor Harries on 18/01/2015.
 *
 * @author Connor Spencer Harries
 */
public class Debt<T> {
    private final T owes;
    private final double amount;

    public Debt(T owes, double owed) {
        this.amount = owed;
        this.owes = owes;
    }

    public T getEntity() {
        return owes;
    }

    public double getAmount() {
        return amount;
    }
}
