package de.albionco.pvpmoney.event;

import de.albionco.pvpmoney.Dictionary;
import de.albionco.pvpmoney.MoneyPlugin;
import de.albionco.pvpmoney.Statics;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.logging.Level;

/**
 * Subscribe to {@link org.bukkit.event.entity.PlayerDeathEvent} so that we can do our thing.
 *
 * @author Connor Spencer Harries
 */
@SuppressWarnings("unused")
public final class PlayerListener implements Listener {

    private final Economy economy = Statics.ECONOMY;

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
                Statics.METRICS_DEATHS++;

                if (Statics.ENABLE_REWARD) {
                    if (killer.hasPermission(Statics.PERMISSION_BASIC) || killer.hasPermission(Statics.PERMISSION_EXTRA)) {
                        boolean bonus = killer.hasPermission(Statics.PERMISSION_EXTRA);
                        double amount = bonus ? Statics.MONEY_EXTRA : Statics.MONEY_BASIC;
                        EconomyResponse r = economy.depositPlayer(killer, amount);

                        if (r.transactionSuccess()) {
                            Statics.METRICS_PAID += amount;
                            killer.sendMessage(Dictionary.format(Statics.MESSAGE_KILLER, "KILLER", killer.getName(), "VICTIM", victim.getName(), "AMOUNT", String.valueOf(amount)));
                        } else {
                            MoneyPlugin.getInstance().getLogger().log(Level.WARNING, "Unable to pay {0} player for their kill", killer.getName());
                        }
                    }
                }

                if (Statics.ENABLE_PUNISHMENT) {
                    if (!victim.hasPermission(Statics.PERMISSION_EXEMPT)) {
                        EconomyResponse p = economy.withdrawPlayer(victim, Statics.MONEY_PUNISH);

                        if (p.transactionSuccess()) {
                            Statics.METRICS_PUNISHED += Statics.MONEY_PUNISH;
                            victim.sendMessage(Dictionary.format(Statics.MESSAGE_PUNISHED, "KILLER", killer.getName(), "VICTIM", victim.getName(), "AMOUNT", String.valueOf(Statics.MONEY_PUNISH)));
                        } else {
                            MoneyPlugin.getInstance().getLogger().log(Level.WARNING, "Unable to withdraw money from {0}'s account", victim.getName());
                        }
                    }
                    // Stop any code after this point from executing
                    return;
                }

                victim.sendMessage(Dictionary.format(Statics.MESSAGE_DEATH, "KILLER", killer.getName(), "VICTIM", victim.getName()));
            }
        }
    }
}
