package de.albionco.pvpmoney;

import com.sk89q.bukkit.util.CommandsManagerRegistration;
import com.sk89q.minecraft.util.commands.*;
import de.albionco.pvpmoney.async.DebtCollector;
import de.albionco.pvpmoney.command.Commands;
import de.albionco.pvpmoney.event.PlayerListener;
import de.albionco.pvpmoney.obj.Debt;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.mcstats.Metrics;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Core of the PVPMoney plugin
 *
 * @author Connor Spencer Harries
 */
public class MoneyPlugin extends JavaPlugin {

    private static MoneyPlugin instance = null;

    private ConcurrentHashMap<UUID, Set<Debt<Player>>> debtHashMap;
    private CommandsManager<CommandSender> commands;
    private BukkitTask collectorTask;

    /**
     * Get the plugin via the Bukkit plugin manager
     *
     * @return {@link MoneyPlugin}
     */
    public static MoneyPlugin getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Something went wrong whilst loading the plugin");
        }
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        if (setupEconomy()) {
            getLogger().info("Using \"" + Statics.ECONOMY.getName() + "\" for rewards.");
        } else {
            getLogger().severe("No Vault-compatible Economy plugin could be found, plugin disabled.");
            getPluginLoader().disablePlugin(this);
            return;
        }

        init();

        try {
            Metrics metrics = new Metrics(this);

            Metrics.Graph deathGraph = metrics.createGraph("Bloody Deaths");
            deathGraph.addPlotter(new Metrics.Plotter() {
                @Override
                public int getValue() {
                    return Statics.METRICS_DEATHS;
                }
            });

            Metrics.Graph economyGraph = metrics.createGraph("Economy");
            economyGraph.addPlotter(new Metrics.Plotter("Money Earnt") {
                @Override
                public int getValue() {
                    return (int) Statics.METRICS_PAID;
                }
            });
            economyGraph.addPlotter(new Metrics.Plotter("Money Lost") {
                @Override
                public int getValue() {
                    return (int) Statics.METRICS_PUNISHED;
                }
            });

            if (metrics.start()) {
                getLogger().info("Metrics service started succesfully");
            } else {
                getLogger().info("Metrics could not be enabled");
            }
        } catch (IOException e) {
            getLogger().info("Metrics could not be enabled");
        }

        this.commands = new CommandsManager<CommandSender>() {
            @Override
            public boolean hasPermission(CommandSender sender, String perm) {
                return sender instanceof ConsoleCommandSender || sender.hasPermission(perm);
            }
        };

        CommandsManagerRegistration commands = new CommandsManagerRegistration(this, this.commands);
        commands.register(Commands.ParentCommand.class);

        Bukkit.getServer().getPluginManager().registerEvents(new PlayerListener(), this);
    }

    /**
     * Load the Economy plugin from Vault
     *
     * @return true if success
     */
    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);
        if(economyProvider != null) {
            Statics.ECONOMY = economyProvider.getProvider();
        }
        return Statics.ECONOMY != null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] args) {
        try {
            this.commands.execute(cmd.getName(), args, sender, sender);
        } catch (CommandPermissionsException e) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to do that.");
        } catch (MissingNestedCommandException e) {
            sender.sendMessage(ChatColor.RED + e.getUsage());
        } catch (CommandUsageException e) {
            sender.sendMessage(ChatColor.RED + e.getMessage());
            sender.sendMessage(ChatColor.RED + e.getUsage());
        } catch (WrappedCommandException e) {
            if (e.getCause() instanceof NumberFormatException) {
                sender.sendMessage(ChatColor.RED + "Number expected, string received instead.");
            } else {
                sender.sendMessage(ChatColor.RED + "An unknown error occured executing that command, please contact an administrator.");
                e.printStackTrace();
            }
        } catch (CommandException e) {
            sender.sendMessage(ChatColor.RED + e.getMessage());
        }
        return true;
    }

    /**
     * Simple method to load config values.
     */
    public void init() {
        if (debtHashMap != null) {
            debtHashMap.clear();
        } else {
            debtHashMap = new ConcurrentHashMap<>();
        }

        Statics.DEBUG = getConfig().getBoolean("debug", false);

        Statics.ENABLE_REWARD = getConfig().getBoolean("rewards.enable", true);
        Statics.ENABLE_PUNISHMENT = getConfig().getBoolean("punishments.enable", false);
        Statics.MONEY_CURRENCY = getConfig().getString("rewards.currency", "$");
        Statics.MESSAGE_DEATH = getConfig().getString("messages.victim", "&7You were killed by &a{{ KILLER }}&7!");

        Statics.MESSAGE_KILLER = getConfig().getString("messages.killer", "&7You killed &a{{ VICTIM }}&7 and got &a{{ CURRENCY }}{{ AMOUNT }}&7!");
        Statics.MONEY_BASIC = getConfig().getDouble("rewards.amount", 50.00);
        Statics.MONEY_EXTRA = getConfig().getDouble("rewards.bonus", 100.00);

        Statics.MESSAGE_PUNISHED = getConfig().getString("messages.punished", "&7You were killed by &a{{ KILLER }}&7 and lost &a{{ CURRENCY }}{{ AMOUNT }}!");
        Statics.MONEY_PUNISH = getConfig().getDouble("punishments.amount", 10.00);

        Statics.DEBT_SET = getConfig().getString("debts.owed.victim", "&cYou are now {{ CURRENCY }}{{ AMOUNT }} in debt to {{ PLAYER }}!");
        Statics.DEBT_SET_KILLER = getConfig().getString("debts.owed.killer", "&c{{ PLAYER }} is now {{ CURRENCY }}{{ AMOUNT }} in debt to you!");
        Statics.DEBT_PAID = getConfig().getString("debts.paid.victim", "&aYour debt of {{ CURRENCY }}{{ AMOUNT}} to {{ PLAYER }} has been paid!");
        Statics.DEBT_PAID_KILLER = getConfig().getString("debts.paid.killer", "&c{{ PLAYER }}'s debt of {{ CURRENCY }}{{ AMOUNT }} has been paid!");

        Statics.ENABLE_DEBTS = getConfig().getBoolean("punishments.fixed", true);
        if (Statics.ENABLE_DEBTS && !Statics.ENABLE_PUNISHMENT) {
            getLogger().log(Level.INFO, "Fixed mode is enabled but punishments are disabled,");
            getLogger().log(Level.INFO, "automatically enabling punishments for this session");
            Statics.ENABLE_PUNISHMENT = true;
        }

        if (Statics.ENABLE_DEBTS && Statics.ENABLE_PUNISHMENT) {
            if (collectorTask == null || collectorTask.getTaskId() == -1) {
                // Check for debts every 10 minutes
                scheduleTask();
            }
        }
    }

    public void scheduleTask() {
        cancelTask();
        if (Statics.DEBUG) {
            collectorTask = Bukkit.getScheduler().runTaskTimer(this, new DebtCollector(), 60L, 100L);
        } else {
            collectorTask = Bukkit.getScheduler().runTaskTimer(this, new DebtCollector(), 60L, 6000L);
        }
        getLogger().log(Level.INFO, "Scheduled debt collection runnable");
    }

    public void cancelTask() {
        if (collectorTask != null && collectorTask.getTaskId() != -1) {
            collectorTask.cancel();
            collectorTask = null;
        }
    }

    /**
     * @return a HashMap containing a Set of {@link de.albionco.pvpmoney.obj.Debt} objects that a player owes
     */
    public ConcurrentHashMap<UUID, Set<Debt<Player>>> getDebts() {
        return debtHashMap;
    }
}
