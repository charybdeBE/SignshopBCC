package org.wargamer2010.signshop.operations;

import org.bukkit.block.Sign;
import org.bukkit.inventory.ItemStack;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.configuration.Storage;
import org.wargamer2010.ssbcc.SSBCC;


public class updatePrice implements SignShopOperation {
    @Override
    public Boolean setupOperation(SignShopArguments ssArgs) {

        return true;
    }

    @Override
    public Boolean checkRequirements(SignShopArguments signShopArguments, Boolean aBoolean) {
        return true;
    }

    @Override
    public Boolean runOperation(SignShopArguments signShopArguments) {
        Sign sign = (Sign) signShopArguments.getSign().get().getState();
        double price = signShopArguments.getPrice().get();

        double nrItems = 0.0;
        ItemStack[] items = Storage.get().getSeller(signShopArguments.getSign().get().getLocation()).getItems();
        String itemName = items[0].getType().getKey().getKey();
        for (ItemStack is : items) {
            nrItems += is.getAmount();
        }

        double modif = Double.parseDouble(signShopArguments.miscSettings.get("modif"));
        if (modif == Double.NaN) {
            modif = 1;
        }
        price /= modif;
        double nrStack = nrItems / items[0].getMaxStackSize();
        price /= nrStack;

        if (sign.getLine(0).toLowerCase().contains("sell")) {
            price = this.updatePrice(price, nrStack, -1);
        } else {
            price = this.updatePrice(price, nrStack, 1);
        }

        String bankAccount = sign.getLine(1);
        String key = itemName + "." +  bankAccount;
        SSBCC.getStorage().set(key, price);

        SSBCC.updateSign(key, price);

        SSBCC.saveStorage();


        return true;
    }

    private double updatePrice(double initPerStack, double stacks, double isBuying) {
        double precis = initPerStack + isBuying * (Math.log(stacks + 1) / 2);
        return Math.round(precis * 100.0) / 100.0;
    }
}
