package org.wargamer2010.ssbcc;

import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.wargamer2010.signshop.events.SSCreatedEvent;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class SignShopListenerU implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onSSBuildEvent(SSCreatedEvent event) {
        if(event.isCancelled())
            return;

        boolean foundHotelSign = false;
        for(String item : SignShopConfig.getBlocks(event.getOperation())) {
            if (item.equalsIgnoreCase("updatePrice"))
                foundHotelSign = true;
        }
        if(!foundHotelSign)
            return;

        Material item =  event.getItems()[0].getType();
        String itemName = item.getKey().getKey();
        Sign sign = (Sign)event.getSign().getState();
        String key = itemName + "." + sign.getLine(1);
//        String keyI = key.replace(".", "-");

        try {
            File filename = new File(SSBCC.getInstance().getDataFolder(), "worth.yml");
            YamlConfiguration ymlThing = new YamlConfiguration();
            ymlThing.load(filename);
            double basePrice = ymlThing.getDouble(key);
            double nrItems = 0;
            for(ItemStack is : event.getItems()){
                nrItems += is.getAmount();
            }
            nrItems /= item.getMaxStackSize();
            if(basePrice == 0 || basePrice == Double.NaN) {
                basePrice = event.getPrice() / nrItems;
                ymlThing.set(key, basePrice);
            }
            double modif = (event.getPrice() / nrItems) / basePrice;

            event.setMiscSetting("BCCU", key );
            event.setMiscSetting("modif",Double.toString(modif));
            ymlThing.save(filename);
            sign.update();

        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
