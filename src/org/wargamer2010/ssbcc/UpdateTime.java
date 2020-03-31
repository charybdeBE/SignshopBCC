package org.wargamer2010.ssbcc;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.configuration.Storage;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class UpdateTime implements Runnable {
    private Plugin pp;

    UpdateTime(Plugin pp) {
        this.pp = pp;
        this.reschedule();
    }

    public void run() {
        HashMap<String, Double> capacity = new HashMap<>();
        HashMap<String, Double> contains = new HashMap<>();

        Storage.get().getSellers().forEach(seller -> {
            if (seller.hasMisc("BCCU")) {
                String key = seller.getMisc("BCCU");
                double stock = 0;
                double stockSize = 0;
                for (Block chestB : seller.getContainables()) {
                    Chest chest = (Chest) chestB.getState();
                    stockSize += chest.getInventory().getSize();
                    for (ItemStack is : chest.getSnapshotInventory().getStorageContents()) {
                        if (is != null) {
                            stock += is.getAmount() / is.getMaxStackSize();
                        }
                    }
                }
                this.sumOrAdd(capacity, key, stockSize);
                this.sumOrAdd(contains, key, stock);
            }
        });
        capacity.forEach((key, value) -> {
            double stock = contains.get(key);
            double remplissage = stock / value;
            System.out.println(key + " est rempli a " + (remplissage * 100.0) + "%");
            if (remplissage < 0.25 || remplissage > 0.75) {
                double price = SSBCC.getStorage().getDouble(key);
                if (remplissage < 0.25) {
                    price *= 1.01;
                } else {
                    price *= 0.99;
                }
                SSBCC.updateSign(key, price);
                SSBCC.getStorage().set(key, price);
            }
        });
        SSBCC.saveStorage();

        this.reschedule();

    }

    private void reschedule() {
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(pp, this, (6 *60 * 60 * 20));
    }

    private void sumOrAdd(HashMap<String, Double> map, String key, double value) {
        Double sum = 0.0;
        if (map.containsKey(key)) {
            sum = map.get(key);
        }
        sum += value;
        map.put(key, sum);
    }

}
