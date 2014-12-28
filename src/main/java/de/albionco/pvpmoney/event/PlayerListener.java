package de.albionco.pvpmoney.event;

import de.albionco.pvpmoney.Dictionary;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.logging.Level;

import static de.albionco.pvpmoney.MoneyPlugin.getInstance;
import static de.albionco.pvpmoney.Statics.*;

/**
 * Subscribe to {@link org.bukkit.event.entity.PlayerDeathEvent} so that we can do our thing.
 *
 * @author Connor Spencer Harries
 */
@SuppressWarnings("unused")
public class PlayerListener implements Listener {

    @EventHandler
    public void playerKilledPlayer(PlayerDeathEvent event) {
        if (event.getEntity().getKiller() != null) {
            if (DEBUG) {
                getInstance().getLogger().log(Level.INFO, "Player \"{0}\" tested successful for a killer", event.getEntity().getName());
            }
            /*
             * Apparently committing suicide by jumping in lava would reward the player.
             * Thanks @BanKz4G for finding.
             */
            if(event.getEntity().getKiller() != event.getEntity()) {
                if (DEBUG) {
                    getInstance().getLogger().log(Level.INFO, "Player {0}\'s death was not suicide", event.getEntity().getName());
                }

                Player victim = event.getEntity();
                Player killer = victim.getKiller();
                if (DEBUG) {
                    getInstance().getLogger().log(Level.INFO, "Incrementing METRICS_DEATHS in Statics class");
                }
                METRICS_DEATHS++;

                if (DEBUG) {
                    getInstance().getLogger().log(Level.INFO, "Rewards enabled: {0}", ENABLE_REWARD);
                    getInstance().getLogger().log(Level.INFO, "Punishments enabled: {0}", ENABLE_PUNISHMENT);
                    getInstance().getLogger().log(Level.INFO, "Economy == null: {0}", ECONOMY == null);
                }

                if (ENABLE_REWARD) {
                    if (killer.hasPermission(PERMISSION_BASIC) || killer.hasPermission(PERMISSION_EXTRA)) {
                        double amount = killer.hasPermission(PERMISSION_EXTRA) ? MONEY_EXTRA : MONEY_BASIC;
                        EconomyResponse r = ECONOMY.depositPlayer(killer, amount);

                        if (r.transactionSuccess()) {
                            if (DEBUG) {
                                getInstance().getLogger().log(Level.INFO, "Transaction successful, paid \"{0}\" {1}", new Object[]{killer.getName(), amount});
                            }
                            METRICS_PAID += amount;
                            killer.sendMessage(Dictionary.format(MESSAGE_KILLER, "KILLER", killer.getName(), "VICTIM", victim.getName(), "AMOUNT", String.valueOf(amount)));
                        } else {
                            killer.sendMessage(Dictionary.colour("&cAn internal error occurred whilst processing your reward."));
                            getInstance().getLogger().log(Level.WARNING, "Unable to pay {0} player for their kill", killer.getName());
                        }
                    } else {
                        if (DEBUG) {
                            getInstance().getLogger().log(Level.INFO, "Killer does not have permission to get rewards");
                        }
                    }
                }

                if (ENABLE_PUNISHMENT) {
                    if (!victim.hasPermission(PERMISSION_EXEMPT)) {
                        EconomyResponse p = ECONOMY.withdrawPlayer(victim, MONEY_PUNISH);

                        if (p.transactionSuccess()) {
                            if (DEBUG) {
                                getInstance().getLogger().log(Level.INFO, "Transaction successful, deducted {0} from player \"{1}\"", new Object[]{MONEY_PUNISH, victim.getName()});
                            }
                            METRICS_PUNISHED += MONEY_PUNISH;
                            victim.sendMessage(Dictionary.format(MESSAGE_PUNISHED, "KILLER", killer.getName(), "VICTIM", victim.getName(), "AMOUNT", String.valueOf(MONEY_PUNISH)));
                            // Stop any code after this point from executing
                            return;
                        } else {
                            getInstance().getLogger().log(Level.WARNING, "Unable to withdraw money from {0}'s account", victim.getName());
                        }
                    } else {
                        if (DEBUG) {
                            getInstance().getLogger().log(Level.INFO, "Victim is exempt from punishments");
                        }
                    }
                }

                if (DEBUG) {
                    getInstance().getLogger().log(Level.INFO, "Sending death message to player \"{0}\"", victim.getName());
                }
                victim.sendMessage(Dictionary.format(MESSAGE_DEATH, "KILLER", killer.getName(), "VICTIM", victim.getName()));
            } else {
                if (DEBUG) {
                    getInstance().getLogger().log(Level.INFO, "Detected suicide, ignored");
                }
            }
        } else {
            if (DEBUG) {
                getInstance().getLogger().log(Level.INFO, "Killer is null");
            }
        }
    }
}
