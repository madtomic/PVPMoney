package com.frostcore.pvpmoney.events;

import com.frostcore.pvpmoney.config.Messages;
import com.frostcore.pvpmoney.config.Permissions;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import static com.frostcore.pvpmoney.PVPMoney.*;
import static com.frostcore.pvpmoney.config.Settings.*;

/**
 * Description: part of PVPMoney
 * Created at: {12:13} on {27/07/2014}
 *
 * @author Connor Spencer Harries
 */
public class Death implements Listener {
    @EventHandler
    public void playerKilledPlayer(PlayerDeathEvent event) {
        boolean punished = false;
        if (event.getEntity().getKiller() != null) {
            getInstance().addDeath();
            Player killer = event.getEntity().getKiller();
            Player victim = event.getEntity();
            String killern = event.getEntity().getKiller().getName();
            String victimn = event.getEntity().getName();

            if (PUNISHMENTS_ENABLED) {
                if (!Permissions.has(victim, Permissions.EXEMPT)) {
                    EconomyResponse p = ECONOMY.withdrawPlayer(Bukkit.getOfflinePlayer(victim.getUniqueId()), PUNISHMENT);
                    if (p.transactionSuccess()) {
                        punished = true;
                        sendMessage(victim, Messages.PUNISHED, killern);
                        getInstance().addMoneyLost(PUNISHMENT);
                    } else {
                        sendMessage(victim, "Error removing funds from your account.");
                    }
                }
            }

            if (REWARDS_ENABLED) {
                if(Permissions.has(killer, Permissions.NORMAL)) {
                    boolean bonus = Permissions.has(killer, Permissions.EXTRA);
                    EconomyResponse r = ECONOMY.depositPlayer(Bukkit.getOfflinePlayer(killer.getUniqueId()), (bonus ? BONUS : BASE));
                    if (r.transactionSuccess()) {
                        Double amount = (bonus ? BONUS : BASE);
                        sendMessage(killer, Messages.KILLER, victimn, String.valueOf(amount));
                        getInstance().addMoney(amount);
                    } else {
                        killer.getInventory().addItem(new ItemStack(Material.DIAMOND, 1));
                    }
                }
            }

            if(!punished) {
                sendMessage(victim, Messages.DEATH, killern);
            }

        }
    }
}
