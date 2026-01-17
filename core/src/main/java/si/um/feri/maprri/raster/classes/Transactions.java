// java
package si.um.feri.maprri.raster.classes;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Transactions {
    private String type;
    private final List<SimulatedTransaction> simulatedTransactions = new ArrayList<>();

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

    public List<SimulatedTransaction> getTransactions() {
        return Collections.unmodifiableList(simulatedTransactions);
    }
    public List<SimulatedTransaction> internalList() {
        return simulatedTransactions;
    }
    public void setTransactions(List<SimulatedTransaction> simulatedTransactions) {
        this.simulatedTransactions.clear();
        if (simulatedTransactions != null) {
            this.simulatedTransactions.addAll(simulatedTransactions);
        }
    }

    public Transactions withType(String type) {
        this.type = type;
        return this;
    }

    public void addTransaction(SimulatedTransaction t) {
        Objects.requireNonNull(t, "transaction must not be null");
        this.simulatedTransactions.add(t);
    }

    public boolean removeTransaction(SimulatedTransaction t) {
        return this.simulatedTransactions.remove(t);
    }

    public JSONObject toJson() {
        JSONArray arr = new JSONArray();
        for (SimulatedTransaction t : simulatedTransactions) {
            arr.put(t.toJson());
        }
        return new JSONObject()
            .put("type", type)
            .put("transactions", arr);
    }

    @Override
    public String toString() {
        return toJson().toString(2);
    }
}
