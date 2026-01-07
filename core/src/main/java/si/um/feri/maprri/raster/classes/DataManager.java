package si.um.feri.maprri.raster.classes;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DataManager {
    public final List<Transactions> allTransactions = new ArrayList<>();

    public void addTransactions(Transactions transactions) {
        if (allTransactions.isEmpty()) {
            allTransactions.add(transactions);
            return;
        }

        for (Transactions t : allTransactions) {
            if (t.getType().equals(transactions.getType())) {
                for (Transaction tr : transactions.getTransactions()) {
                    t.addTransaction(tr);
                }
                return;
            }
        }

        allTransactions.add(transactions);
    }

    public void extractTransactionsFromSimulateJson(String jsonArrayString) {
        JSONArray arr = new JSONArray(jsonArrayString);

        Transactions transactions = new Transactions("simulate");

        for (int i = 0; i < arr.length(); i++) {
            JSONObject obj = arr.getJSONObject(i);

            String id = obj.getString("id");
            String user = obj.getString("user");
            long datetime = obj.getLong("datetime");
            double change = obj.getDouble("change");
            boolean outgoing = obj.getBoolean("outgoing");

            JSONObject locObj = obj.getJSONObject("location");
            String locId = locObj.getString("id");
            String locName = locObj.getString("name");
            double lat = locObj.getDouble("lat");
            double lng = locObj.getDouble("lng");
            Location location = new Location(locId, locName, lat, lng);

            Transaction t = new Transaction(id, user, location, datetime, change, outgoing);
            transactions.addTransaction(t);
        }

        addTransactions(transactions);
    }

    @Override
    public String toString() {
        return "DataManager{" +
            "allTransactions=" + allTransactions.toString() +
            '}';
    }
    public void drawMarkers() {
        for (Transactions transactions : allTransactions) {
            for (Transaction transaction : transactions.getTransactions()) {
                System.out.println("Drawing marker for transaction: " + transaction.toString());
            }
        }
    }

}
