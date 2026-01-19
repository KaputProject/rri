package si.um.feri.maprri.raster.manager;

import org.json.JSONArray;
import org.json.JSONObject;
import si.um.feri.maprri.raster.classes.*;
import si.um.feri.maprri.raster.classes.graphics.ColumnMode;
import si.um.feri.maprri.raster.classes.graphics.ColumnVisual;
import si.um.feri.maprri.raster.utils.markerUtil;

import java.util.ArrayList;
import java.util.List;

public class DataManager {
    public final List<Transactions> allTransactions = new ArrayList<>();
    public final List<Location> BaseLocations = new ArrayList<>();
    public final List<Marker> markers = new ArrayList<>();
    public final List<Person> family = new ArrayList<>();
    public final List<Person> mainUser = new ArrayList<>();

    public void addTransactions(Transactions transactions) {
        if (allTransactions.isEmpty()) {
            allTransactions.add(transactions);
            return;
        }
        for (Transactions t : allTransactions) {
            if (t.getType().equals(transactions.getType())) {
                for (SimulatedTransaction tr : transactions.getTransactions()) {
                    t.addTransaction(tr);
                }
                return;
            }
        }
        allTransactions.add(transactions);
    }

    public void loadBaseData(String BaseData, String FamilyId) {
        try {
            if (FamilyId != null) {
                System.out.println("Base Data: " + BaseData);
                JSONObject data = new JSONObject(BaseData);
                JSONArray familyMembers = data.getJSONArray("familyMembers");
                for (int i = 0; i < familyMembers.length(); i++) {
                    if(i == 0){
                        JSONObject user = familyMembers.getJSONObject(i);
                        String userId = user.getString("_id");
                        String userName = user.optString("username", "");
                        this.mainUser.add(new Person(userId, userName));
                    }
                    JSONObject member = familyMembers.getJSONObject(i);
                    String memberId = member.getString("_id");
                    // Use "username" field from JSON (not "name")
                    String memberName = member.optString("username", member.optString("name", ""));
                    family.add(new Person(memberId, memberName));
                }
                JSONArray Locations = data.getJSONArray("statistics");
                for (int i = 0; i < Locations.length(); i++) {
                    JSONObject s = Locations.getJSONObject(i);
                    String locId = s.optString("_id", s.optString("id", ""));
                    String identifier = s.optString("identifier", s.optString("name", ""));
                    String address = s.optString("address", "");
                    double lat = s.optDouble("lat", 0.0);
                    double lng = s.optDouble("lng", 0.0);
                    double total_inflow = s.optDouble("total_inflow", s.optDouble("total_inflow", 0.0));
                    double total_outflow = s.optDouble("total_outflow", s.optDouble("total_outflow", 0.0));
                    int numberOfTrans = s.optInt("number_of_transactions", s.optInt("numberOfTransactions", 0));

                    List<LocationUser> users = new ArrayList<>();
                    JSONArray usersArr = s.optJSONArray("users");
                    if (usersArr != null) {
                        for (int ui = 0; ui < usersArr.length(); ui++) {
                            JSONObject uo = usersArr.getJSONObject(ui);
                            String uid = uo.optString("userId", uo.optString("_id", ""));
                            String username = uo.optString("username", "");
                            int numbOfTrans = uo.optInt("numbOfTrans", uo.optInt("numbOfTrans", 0));
                            double inflow = uo.optDouble("inflow", 0.0);
                            double outflow = uo.optDouble("outflow", 0.0);
                            users.add(new LocationUser(uid, username, numbOfTrans, inflow, outflow));
                        }
                    }
                    Location location = new Location(locId, identifier, address, lat, lng, users, total_inflow, total_outflow, numberOfTrans);
                    BaseLocations.add(location);
                }
            } else {
                System.out.println("Base Data: " + BaseData);
                JSONObject data = new JSONObject(BaseData);
                JSONObject user = data.getJSONObject("user");
                Person person = new Person(user.getString("_id"), user.getString("username"));
                this.mainUser.add(person);
                JSONArray Locations = user.getJSONArray("locations");
                for (int i = 0; i < Locations.length(); i++) {
                    JSONObject s = Locations.getJSONObject(i);
                    String locId = s.optString("_id", s.optString("id", ""));
                    String identifier = s.optString("identifier", s.optString("name", ""));
                    String address = s.optString("address", "");
                    double lat = s.optDouble("lat", 0.0);
                    double lng = s.optDouble("lng", 0.0);
                    double total_inflow = s.optDouble("inflow", 0.0);
                    double total_outflow = s.optDouble("outflow", 0.0);
                    int numberOfTrans = s.optInt("number_of_transactions", s.optInt("numberOfTransactions", 0));

                    List<LocationUser> users = new ArrayList<>();

                    String uid = user.optString("_id", "");
                    String username = user.optString("username", "");
                    int numbOfTrans = s.optInt("number_of_transactions", 0);
                    double inflow = s.optDouble("inflow", 0.0);
                    double outflow = s.optDouble("outflow", 0.0);
                    users.add(new LocationUser(uid, username, numbOfTrans, inflow, outflow));

                    Location location = new Location(locId, identifier, address, lat, lng, users,total_inflow, total_outflow  , numberOfTrans);
                    BaseLocations.add(location);

                }
            }
            System.out.println(BaseLocations);
        } catch (Exception e) {
            System.err.println("Failed to parse base data json: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void extractTransactionsFromSimulateJson(String jsonArrayString) {
        try {
            JSONArray arr = new JSONArray(jsonArrayString);
            Transactions transactions = new Transactions("simulate");

            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);

                String id = obj.getString("_id");
                long datetime = obj.getLong("datetime");

                JSONObject locObj = obj.getJSONObject("location");
                String locId = locObj.getString("_id");
                String identifier = locObj.optString("identifier", "");
                String address = locObj.optString("address", "");
                double lat = locObj.getDouble("lat");
                double lng = locObj.getDouble("lng");
                double total_inflow = locObj.getDouble("total_inflow");
                double total_outflow = locObj.getDouble("total_outflow");
                int numberOfTrans = locObj.getInt("numbOfTrans");



                List<LocationUser> users = new ArrayList<>();
                JSONArray usersArr = locObj.optJSONArray("users");
                if (usersArr != null) {
                    for (int ui = 0; ui < usersArr.length(); ui++) {
                        JSONObject uo = usersArr.getJSONObject(ui);
                        String uid = uo.optString("userId", uo.optString("_id", ""));
                        String username = uo.optString("username", "");
                        int numbOfTrans = uo.optInt("numbOfTrans", 0);
                        double inflow = uo.optDouble("inflow", 0.0);
                        double outflow = uo.optDouble("outflow", 0.0);
                        users.add(new LocationUser(uid, username, numbOfTrans, inflow, outflow));
                    }
                }
                Location location = new Location(locId, identifier, address, lat, lng, users, total_inflow, total_outflow, numberOfTrans);
                //Location location = new Location(locId, identifier, address, lat, lng, users);
                SimulatedTransaction t = new SimulatedTransaction(id, location, datetime);
                transactions.addTransaction(t);
            }

            addTransactions(transactions);
            System.out.println("DataManager after: " + allTransactions);
        } catch (Exception e) {
            System.err.println("Failed to parse simulate json: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        JSONArray arr = new JSONArray();
        for (Transactions t : allTransactions) {
            arr.put(t.toJson());
        }
        return new JSONObject().put("allTransactions", arr).toString(2);
    }

    public Transactions getUsersTransactions(String userIdOrUsername) {
        Transactions userTransactions = new Transactions(userIdOrUsername);
        for (Transactions tr : allTransactions) {
            for (SimulatedTransaction t : tr.getTransactions()) {
                if (t.getLocation().hasUser(userIdOrUsername)) {
                    userTransactions.addTransaction(t);
                }
            }
        }
        return userTransactions;
    }

    public void addMarker(Marker marker) {
        markers.add(marker);
    }

    public void createMarkers(Map map) {
        markerUtil.clearMarkers(markers);
        markerUtil.createMarkersFromTransactions(markers, allTransactions, map);
    }

    public List<Marker> getMarkers() {
        return markers;
    }

    public List<ColumnVisual> getColumnVisuals(ColumnMode mode, String userId) {
        List<ColumnVisual> result = new ArrayList<>();
        if (userId == null) {
            // FAMILY VIEW: For each location, for each family member, add a column if they have data
            for (Location loc : BaseLocations) {
                for (Person member : family) {
                    LocationUser u = loc.getUser(member.getId());
                    if (u == null) continue;
                    double value;
                    switch (mode) {
                        case INFLOW: value = u.getInflow(); break;
                        case OUTFLOW: value = u.getOutflow(); break;
                        case COMBINED: value = u.getInflow() - u.getOutflow(); break;
                        default: value = 0.0; break;
                    }
                    // Only add if the value is significant
                    if (Math.abs(value) > 0.001) {
                        result.add(new ColumnVisual(loc, member.getId(), value, mode));
                    }
                }
            }
        } else {
            // USER VIEW: Only show the main user's column per location
            for (Location loc : BaseLocations) {
                LocationUser u = loc.getUser(userId);
                if (u == null) continue;
                double inflow = u.getInflow();
                double outflow = u.getOutflow();
                double value = 0.0;
                ColumnMode colMode = mode;

                if (inflow > 0 && outflow == 0) {
                    value = inflow;
                    colMode = ColumnMode.INFLOW;
                } else if (outflow > 0 && inflow == 0) {
                    value = -outflow;
                    colMode = ColumnMode.OUTFLOW;
                } else if (inflow > 0 && outflow > 0) {
                    value = inflow - outflow;
                    colMode = ColumnMode.COMBINED;
                } else {
                    continue; // skip if both are zero
                }
                result.add(new ColumnVisual(loc, userId, value, colMode));
            }
        }
        return result;
    }

    public double getMaxValue(ColumnMode mode, String userId) {
        double max = 0;
        for (Location loc : BaseLocations) {
            if (userId == null) {
                double value;
                switch (mode) {
                    case INFLOW:
                        value = loc.getTotal_inflow();
                        break;
                    case OUTFLOW:
                        value = loc.getTotal_outflow();
                        break;
                    case COMBINED:
                        value = Math.abs(loc.getTotal_inflow() - loc.getTotal_outflow());
                        break;
                    default:
                        value = 0.0;
                        break;
                }
                max = Math.max(max, value);
            } else {
                LocationUser u = loc.getUser(userId);
                if (u == null) continue;

                double value;
                switch (mode) {
                    case INFLOW:
                        value = u.getInflow();
                        break;
                    case OUTFLOW:
                        value = u.getOutflow();
                        break;
                    case COMBINED:
                        value = Math.abs(u.getInflow() - u.getOutflow());
                        break;
                    default:
                        value = 0.0;
                        break;
                }
                max = Math.max(max, value);
            }
        }
        return max;
    }
    public Transactions getTransactionsByType(String type) {
        for (Transactions t : allTransactions) {
            if (t.getType().equals(type)) {
                return t;
            }
        }
        return null;
    }
    public void removeTransactionsByTypeAndId(String type, String id) {
       Transactions transactionsOfInterest = getTransactionsByType(type);
        List<SimulatedTransaction> txList = transactionsOfInterest.internalList();
        for (SimulatedTransaction t : new ArrayList<>(txList)) {
            if (t.getId().equals(id)) {
                txList.remove(t);
                break;
            }
        }
    }

}
