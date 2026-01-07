package si.um.feri.maprri.raster.utils;

import si.um.feri.maprri.raster.classes.Map;
import si.um.feri.maprri.raster.classes.Marker;
import si.um.feri.maprri.raster.classes.SimulatedTransaction;
import si.um.feri.maprri.raster.classes.Transactions;

import java.util.List;

public class markerUtil {

    /** Dispose and clear all markers from the given list. */
    public static void clearMarkers(List<Marker> markers) {
        if (markers == null) return;
        for (Marker m : markers) {
            if (m != null) {
                m.dispose();
            }
        }
        markers.clear();
    }

    /**
     * Create markers from all transactions and add them to the provided list.
     */
    public static void createMarkersFromTransactions(List<Marker> markers,
                                                     List<Transactions> allTransactions,
                                                     Map map) {
        if (markers == null || allTransactions == null || map == null) return;

        for (Transactions trGroup : allTransactions) {
            for (SimulatedTransaction t : trGroup.getTransactions()) {
                markers.add(new Marker(t, map));
            }
        }
    }
}
