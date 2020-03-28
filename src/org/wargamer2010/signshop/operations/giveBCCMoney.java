package org.wargamer2010.signshop.operations;


import be.charybde.bank.entities.Account;
import org.bukkit.block.Sign;
import org.wargamer2010.signshop.configuration.SignShopConfig;

public class giveBCCMoney implements SignShopOperation {

    public giveBCCMoney() {

    }

    // At link time
    public Boolean setupOperation(SignShopArguments ssArgs) {
        Sign sign = (Sign)ssArgs.getSign().get().getState();
        String accountName = sign.getLine(1);
        Account linked = Account.fetch(accountName.toLowerCase());

        if(linked == null){
            ssArgs.getPlayer().get().sendMessage(SignShopConfig.getError("account_not_found", ssArgs.getMessageParts()));
        } else {
            ssArgs.setMessagePart("!account", accountName);
        }
        return linked != null;
    }

    //Before buy (twice ?)
    public Boolean checkRequirements(SignShopArguments ssArgs, Boolean activeCheck) {
        Sign sign = (Sign)ssArgs.getSign().get().getState();
        String accountName = sign.getLine(1);
        Account linked = Account.fetch(accountName.toLowerCase());

        if(linked == null){
            ssArgs.getPlayer().get().sendMessage(SignShopConfig.getError("account_not_found", ssArgs.getMessageParts()));
        }

        return linked != null;
    }

    // Buy operation
    public Boolean runOperation(SignShopArguments ssArgs) {
        Sign sign = (Sign)ssArgs.getSign().get().getState();
        String accountName = sign.getLine(1);
        Account linked = Account.fetch(accountName.toLowerCase());
        double newBalance = linked.getBalance() + ssArgs.getPrice().get();
        linked.setBalance(newBalance);
        linked.save(true);
        ssArgs.setMessagePart("!account", accountName);
        return true;
    }
}
