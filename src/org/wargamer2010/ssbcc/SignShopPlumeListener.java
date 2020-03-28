package org.wargamer2010.ssbcc;

import be.charybde.bank.entities.Account;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.block.Sign;

import java.util.Arrays;
import java.util.HashMap;

public class SignShopPlumeListener implements Listener {

    private HashMap<Player, Block> hashmap = new HashMap<>();

    @EventHandler
    public void onClick(PlayerInteractEvent e) {
        if(!e.getAction().equals(Action.LEFT_CLICK_BLOCK))
            return;
        if(!isWallSign(e.getClickedBlock().getType()))
            return;
        if(!e.getPlayer().getItemInHand().getType().equals(Material.FEATHER))
            return;
        Sign sign = (Sign) e.getClickedBlock().getState();
        if(!(sign.getLine(0).toLowerCase().contains("[bccsell]") || sign.getLine(0).toLowerCase().contains("[bccbuy]")))
            return;
        Account account = Account.fetch(sign.getLine(1));
        Player player = e.getPlayer();
        if(!account.isAllowed(player.getName()))
            return;

        hashmap.put(player, e.getClickedBlock());
        player.sendMessage("Quel prix souhaitez vous mettre ?");

    }

    @EventHandler
    public void onPlayerChat(PlayerChatEvent event){
        if (hashmap.containsKey(event.getPlayer())) {
            try {
                double prix = Double.parseDouble(event.getMessage());
                Sign sign = (Sign) hashmap.get(event.getPlayer()).getState();
                sign.setLine(3, Double.toString(prix));
                sign.update();
                event.getPlayer().sendMessage("Prix mis a jour");
            } catch (Exception e) {
                System.out.println(e);
                event.getPlayer().sendMessage("Erreur de prix");
            }
            hashmap.remove(event.getPlayer());
            event.setCancelled(true);
        }
    }

    private boolean isWallSign(Material m) {
        Material[] wallSign = {Material.ACACIA_WALL_SIGN,
                Material.SPRUCE_WALL_SIGN,
                Material.BIRCH_WALL_SIGN,
                Material.OAK_WALL_SIGN,
                Material.JUNGLE_WALL_SIGN,
                Material.DARK_OAK_WALL_SIGN};
        return Arrays.stream(wallSign).anyMatch(ws -> ws == m);
    }

}
