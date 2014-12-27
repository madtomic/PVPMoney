package de.albionco.pvpmoney.event;

import de.albionco.pvpmoney.Dictionary;
import de.albionco.pvpmoney.MoneyPlugin;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.logging.Level;

import static de.albionco.pvpmoney.Statics.*;

/**
 * Subscribe to {@link org.bukkit.event.entity.PlayerDeathEvent} so that we can do our thing.
 *
 * @author Connor Spencer Harries
 */
@SuppressWarnings("unused")
public final class PlayerListener implements Listener {

    @EventHandler
    public void playerKilledPlayer(PlayerDeathEvent event) {
        if (event.getEntity().getKiller() != null) {
            /*
             * Apparently committing suicide by jumping in lava would reward the player.
             * Thanks @BanKz4G for finding.
             */
            if(event.getEntity().getKiller() != event.getEntity()) {
                Player victim = event.getEntity();
                Player killer = victim.getKiller();
                METRICS_DEATHS++;

                if (ENABLE_REWARD) {
                    if (killer.hasPermission(PERMISSION_BASIC) || killer.hasPermission(PERMISSION_EXTRA)) {
                        boolean bonus = killer.hasPermission(PERMISSION_EXTRA);
                        double amount = bonus ? MONEY_EXTRA : MONEY_BASIC;
                        EconomyResponse r = ECONOMY.depositPlayer(killer, amount);

                        if (r.transactionSuccess()) {
                            METRICS_PAID += amount;
                            killer.sendMessage(Dictionary.format(MESSAGE_KILLER, "KILLER", killer.getName(), "VICTIM", victim.getName(), "AMOUNT", String.valueOf(amount)));
                        } else {
                            MoneyPlugin.getInstance().getLogger().log(Level.WARNING, "Unable to pay {0} player for their kill", killer.getName());
                        }
                    }
                }

                if (ENABLE_PUNISHMENT) {
                    if (!victim.hasPermission(PERMISSION_EXEMPT)) {
                        EconomyResponse p = ECONOMY.withdrawPlayer(victim, MONEY_PUNISH);

                        if (p.transactionSuccess()) {
                            METRICS_PUNISHED += MONEY_PUNISH;
                            victim.sendMessage(Dictionary.format(MESSAGE_PUNISHED, "KILLER", killer.getName(), "VICTIM", victim.getName(), "AMOUNT", String.valueOf(MONEY_PUNISH)));
                        } else {
                            MoneyPlugin.getInstance().getLogger().log(Level.WARNING, "Unable to withdraw money from {0}'s account", victim.getName());
                        }
                    }
                    // Stop any code after this point from executing
                    return;
                }

                victim.sendMessage(Dictionary.format(MESSAGE_DEATH, "KILLER", killer.getName(), "VICTIM", victim.getName()));
            }
        }
    }
}
