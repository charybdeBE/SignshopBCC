package org.wargamer2010.ssbcc.command;

import be.charybde.bank.entities.Account;
import org.bukkit.entity.Player;
import org.wargamer2010.signshop.commands.ICommandHandler;
import org.wargamer2010.signshop.player.SignShopPlayer;
import org.wargamer2010.ssbcc.SSBCC;

public class AjustCommand implements ICommandHandler {
    private static AjustCommand instance = new AjustCommand();

    public static AjustCommand getInstance() {
        return AjustCommand.instance;
    }

    @Override
    public boolean handle(String s, String[] strings, SignShopPlayer signShopPlayer) {
        // 1e = compte en banque
        // 2e = item
        // 3e = nouveau prix
        Account account = Account.fetch(strings[0]);
        if(!account.isAllowed(signShopPlayer.getName())) {
            return false;
        }

        String key = strings[1] + "." + strings[0];
        double price = Double.parseDouble(strings[2]);

        SSBCC.getStorage().set(key, price);
        SSBCC.saveStorage();

        SSBCC.updateSign(key, price);

        return true;
    }
}
