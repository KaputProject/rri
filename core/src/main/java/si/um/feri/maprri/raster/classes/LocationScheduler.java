package si.um.feri.maprri.raster.classes;

import si.um.feri.maprri.raster.classes.graphics.ColumnMarker;
import si.um.feri.maprri.raster.manager.ColumnManager;
import si.um.feri.maprri.raster.manager.DataManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class LocationScheduler  {

    private List<SimulatedTransaction> transactionQueue;
    private DataManager dataManager;
    private ColumnManager columnManager;
    public LocationScheduler(DataManager dataManager, ColumnManager columnManager) {
        this.dataManager = dataManager;
        this.columnManager = columnManager;
    }


    public void updateTransactions(Transactions transactions) {
        if (transactionQueue == null) {
            transactionQueue = new ArrayList<>();
        }

        // dodaj vse nove transakcije
        for (SimulatedTransaction transaction : transactions.getTransactions()) {
            if (!transactionQueue.contains(transaction)) {
                transactionQueue.add(transaction);
            }
        }

        // sortiraj po času: od največjega do najmanjšega
        transactionQueue.sort(
            Comparator.comparingLong(SimulatedTransaction::getDatetime).reversed()
        );
        System.out.println("Sorted transactions" + transactionQueue.toString());
    }

    /**
     * Vrne zadnjo transakcijo (naslednjo na vrsti) in jo odstrani iz seznama.
     * @return SimulatedTransaction ali null, če je seznam prazen
     */
    public SimulatedTransaction pop() {
        if (transactionQueue == null || transactionQueue.isEmpty()) {
            return null;
        }
        SimulatedTransaction removed = transactionQueue.remove(transactionQueue.size() - 1);
        dataManager.removeTransactionsByTypeAndId("simulate", removed.getId());
        return removed;
    }

    public boolean isEmpty() {
        return transactionQueue == null || transactionQueue.isEmpty();
    }

    public List<SimulatedTransaction> getAll() {
        return transactionQueue;
    }
    public void processAllDue() {
        if (transactionQueue == null) {
            return;
        }

        long currentTime = System.currentTimeMillis();

        while (!transactionQueue.isEmpty()) {
            SimulatedTransaction next = transactionQueue.get(transactionQueue.size() - 1);

            if (currentTime >= next.getDatetime()) {
                System.out.println("Executing transaction:");
                System.out.println(transactionQueue.size());
                SimulatedTransaction poped = pop();
                System.out.println(transactionQueue.size());
                System.out.println(poped);
                for(Location loc : dataManager.BaseLocations){
                    if (Objects.equals(loc.getIdentifier(), poped.getLocation().getIdentifier())) {
                        System.out.println("location before:" + loc);
                        loc.addTransactionToLocation(poped.getLocation());
                        System.out.println("location after:" + loc);
                        columnManager.rebuild();
                    }
                }

            } else {
                break;
            }
        }
    }
}
