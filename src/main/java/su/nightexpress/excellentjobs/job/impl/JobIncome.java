package su.nightexpress.excellentjobs.job.impl;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nightcore.bridge.currency.Currency;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class JobIncome {

    private final Map<Currency, Double> currencyMap;

    public JobIncome() {
        this.currencyMap = new ConcurrentHashMap<>();
    }

    /**
     * Pays the current income to the player and clears the map.
     * Snapshots and clears first, then pays from the snapshot, so that any
     * re-entrant or async adds (e.g. from item currency give() triggering
     * grind events) cannot cause the same income to be paid multiple times.
     */
    public void payAndClear(@NotNull Player player) {
        Map<Currency, Double> snapshot = new HashMap<>(this.currencyMap);
        this.clear();
        snapshot.forEach((currency, amount) -> currency.give(player, amount));
    }

    public void pay(@NotNull Player player) {
        this.currencyMap.forEach((currency, amount) -> {
            currency.give(player, amount);
        });
    }

    public void clear() {
        this.currencyMap.clear();
    }

    public void set(@NotNull Currency currency, double amount) {
        if (amount <= 0) {
            this.remove(currency);
            return;
        }

        this.currencyMap.put(currency, amount);
    }

    public void add(@NotNull Currency currency, double amount) {
        if (amount == 0) return;

        double has = this.currencyMap.getOrDefault(currency, 0D);

        this.set(currency, has + amount);
    }

    public void reduce(@NotNull Currency currency, double amount) {
        this.add(currency, -amount);
    }

    public void remove(@NotNull Currency currency) {
        this.currencyMap.remove(currency);
    }

    public boolean isEmpty() {
        return this.currencyMap.isEmpty() || this.currencyMap.values().stream().mapToDouble(amount -> amount).sum() == 0D;
    }

    @NotNull
    public Map<Currency, Double> getCurrencyMap() {
        return this.currencyMap;
    }
}
