package si.um.feri.maprri.raster.classes;

import org.json.JSONArray;
import org.json.JSONObject;
import si.um.feri.maprri.raster.utils.markerUtil;

import java.util.ArrayList;
import java.util.List;

public class DataManager {
    public final List<Transactions> allTransactions = new ArrayList<>();
    public final List<Marker> markers = new ArrayList<>();

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
        try {
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
                String userId = locObj.getString("userId");

                Location location = new Location(locId, locName, lat, lng, userId);

                Transaction t = new Transaction(id, user, location, datetime, change, outgoing);
                transactions.addTransaction(t);
            }
            addTransactions(transactions);
            System.out.println("DataManager after: " + allTransactions.toString());
        } catch (Exception e) {
            System.err.println("Failed to parse simulate json: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "DataManager{" + "allTransactions=" + allTransactions.toString() + '}';
    }

    public Transactions getUsersTransactions(String userId) {
        Transactions userTransactions = new Transactions(userId);
        for (Transactions tr : allTransactions) {
            for (Transaction t : tr.getTransactions()) {
                if (t.getLocation().getUserId().equals(userId)) {
                    userTransactions.addTransaction(t);
                }
            }
        }
        return userTransactions;
    }

    public void addMarker(Marker marker) {
        markers.add(marker);
    }

    /** Uses markerUtil to recreate markers from all transactions. */
    public void createMarkers(Map map) {
        markerUtil.clearMarkers(markers);
        markerUtil.createMarkersFromTransactions(markers, allTransactions, map);
    }

    public List<Marker> getMarkers() {
        return markers;
    }
}
