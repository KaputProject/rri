package si.um.feri.maprri.raster.classes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Transactions {
    private String type;
    private final List<Transaction> transactions = new ArrayList<>();

    public Transactions() {}

    public Transactions(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Transaction> getTransactions() {
        return Collections.unmodifiableList(transactions);
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions.clear();
        if (transactions != null) {
            this.transactions.addAll(transactions);
        }
    }

    public Transactions withType(String type) {
        this.type = type;
        return this;
    }


    public void addTransaction(Transaction t) {
        Objects.requireNonNull(t, "transaction must not be null");
        this.transactions.add(t);
    }

    public boolean removeTransaction(Transaction t) {
        return this.transactions.remove(t);
    }

    @Override
    public String toString() {
        return "Transactions{" +
            "type='" + type + '\'' +
            ", transactions=" + transactions +
            '}';
    }
}
