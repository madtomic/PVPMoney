package de.albionco.pvpmoney.async;

import de.albionco.pvpmoney.Dictionary;
import de.albionco.pvpmoney.MoneyPlugin;
import de.albionco.pvpmoney.Statics;
import de.albionco.pvpmoney.obj.Debt;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Created by Connor Harries on 18/01/2015.
 *
 * @author Connor Spencer Harries
 */
public class DebtCollector implements Runnable {
    @Override
    public void run() {
        if (!MoneyPlugin.getInstance().getDebts().isEmpty()) {
            if (Statics.DEBUG) {
                MoneyPlugin.getInstance().getLogger().log(Level.INFO, "Beginning debt collection");
            }
            for (UUID uuid : MoneyPlugin.getInstance().getDebts().keySet()) {
                if (Bukkit.getPlayer(uuid) != null) {
                    Player debter = Bukkit.getPlayer(uuid);
                    Set<Debt<Player>> debts = MoneyPlugin.getInstance().getDebts().remove(uuid);
                    if (debts == null) {
                        continue;
                    }
                    Iterator<Debt<Player>> debtIterator = debts.iterator();
                    while (debtIterator.hasNext()) {
                        Debt<Player> debt = debtIterator.next();
                        if (debt.getEntity().getUniqueId() == uuid || !Statics.ECONOMY.has(debter, debt.getAmount())) {
                            continue;
                        }
                        if (debt.getEntity().isOnline()) {
                            EconomyResponse response = Statics.ECONOMY.withdrawPlayer(debter, debt.getAmount());
                            if (response.transactionSuccess()) {
                                response = Statics.ECONOMY.depositPlayer(debt.getEntity(), debt.getAmount());
                                if (response.transactionSuccess()) {
                                    debtIterator.remove();
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
