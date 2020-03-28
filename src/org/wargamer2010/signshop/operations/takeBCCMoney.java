package org.wargamer2010.signshop.operations;


import be.charybde.bank.entities.Account;
import org.bukkit.block.Sign;
import org.wargamer2010.signshop.configuration.SignShopConfig;

public class takeBCCMoney implements SignShopOperation {

    public takeBCCMoney() {

    }

    // At link time
    public Boolean setupOperation(SignShopArguments ssArgs) {
        Sign sign = (Sign) ssArgs.getSign().get().getState();
        String accountName = sign.getLine(1);
        Account linked = Account.fetch(accountName.toLowerCase());

        boolean toRet = false;
        if (linked == null) {
            ssArgs.getPlayer().get().sendMessage(SignShopConfig.getError("account_not_found", ssArgs.getMessageParts()));
        } else if (!linked.getAuthorizedPlayers().contains(ssArgs.getPlayer().get().getName().toLowerCase())) {
            ssArgs.getPlayer().get().sendMessage(SignShopConfig.getError("account_not_authorized", ssArgs.getMessageParts()));
        } else {
            ssArgs.setMessagePart("!account", accountName);
            toRet = true;
        }
        return toRet;
    }

    //Before buy (twice ?)
    public Boolean checkRequirements(SignShopArguments ssArgs, Boolean activeCheck) {
        Sign sign = (Sign) ssArgs.getSign().get().getState();
        String accountName = sign.getLine(1);
        Account linked = Account.fetch(accountName.toLowerCase());

        boolean toRet = false;
        if (linked == null) {
            ssArgs.getPlayer().get().sendMessage(SignShopConfig.getError("account_not_found", ssArgs.getMessageParts()));
        } else if (linked.getBalance() < ssArgs.getPrice().get()){
            ssArgs.getPlayer().get().sendMessage(SignShopConfig.getError("account_empty", ssArgs.getMessageParts()));
        } else {
            toRet = true;
        }

        return toRet;
    }

    // Buy operation
    public Boolean runOperation(SignShopArguments ssArgs) {
        Sign sign = (Sign) ssArgs.getSign().get().getState();
        String accountName = sign.getLine(1);
        Account linked = Account.fetch(accountName.toLowerCase());
        double newBalance = linked.getBalance() - ssArgs.getPrice().get();
        linked.setBalance(newBalance);
        linked.save(true);
        ssArgs.setMessagePart("!account", accountName);
        return true;
    }
}
