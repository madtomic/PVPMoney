package de.albionco.pvpmoney.async;

import de.albionco.pvpmoney.Dictionary;
import de.albionco.pvpmoney.MoneyPlugin;
import de.albionco.pvpmoney.Statics;
import de.albionco.pvpmoney.obj.Debt;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;

/**
 * Created by Connor Harries on 18/01/2015.
 *
 * @author Connor Spencer Harries
 */
public class DebtCollector implements Runnable {
    @Override
    public void run() {
        if (MoneyPlugin.getInstance().getDebts() != null && !MoneyPlugin.getInstance().getDebts().isEmpty()) {
            for (UUID uuid : MoneyPlugin.getInstance().getDebts().keySet()) {
                if (Bukkit.getPlayer(uuid) != null) {
                    Player debter = Bukkit.getPlayer(uuid);
                    Set<Debt<Player>> debts = MoneyPlugin.getInstance().getDebts().remove(uuid);
                    if (debts == null) {
                        throw new IllegalStateException(String.format("Player (\"%s\") debt collection was null", debter.getName()));
                    }
                    for (Debt<Player> debt : debts) {
                        if (debt.getEntity().getUniqueId() == uuid || !Statics.ECONOMY.has(debter, debt.getAmount())) {
                            break;
                        }
                        if (debt.getEntity().isOnline()) {
                            EconomyResponse response = Statics.ECONOMY.withdrawPlayer(debter, debt.getAmount());
                            if (response.transactionSuccess()) {
                                response = Statics.ECONOMY.depositPlayer(debt.getEntity(), debt.getAmount());
                                if (response.transactionSuccess()) {
                                    debts.remove(debt);
                                    debter.sendMessage(Dictionary.format(Statics.DEBT_PAID, "PLAYER", debt.getEntity().getName(), "AMOUNT", String.valueOf(debt.getAmount())));
                                    debt.getEntity().sendMessage(Dictionary.format(Statics.DEBT_PAID_KILLER, "PLAYER", debter.getName(), "AMOUNT", String.valueOf(debt.getAmount())));
                                }
                            }
                        }
                    }
                    if (!debts.isEmpty()) {
                        MoneyPlugin.getInstance().getDebts().put(uuid, debts);
                    }
                }
            }
        }
    }
}
