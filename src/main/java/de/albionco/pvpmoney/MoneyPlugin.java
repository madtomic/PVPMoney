package de.albionco.pvpmoney;

import com.sk89q.bukkit.util.CommandsManagerRegistration;
import com.sk89q.minecraft.util.commands.*;
import de.albionco.pvpmoney.command.Commands;
import de.albionco.pvpmoney.event.PlayerListener;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;

import java.io.IOException;

/**
 * Core of the PVPMoney plugin
 *
 * @author Connor Spencer Harries
 */
public class MoneyPlugin extends JavaPlugin {

    private static MoneyPlugin instance;
    private CommandsManager<CommandSender> commands;

    /**
     * Get the plugin via the Bukkit plugin manager
     *
     * @return {@link MoneyPlugin}
     */
    public static MoneyPlugin getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        this.saveDefaultConfig();

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

        /*
         * Fix the player listener not registering correctly,
         * thanks to @Live4Redline on GitHub for reporting.
         */
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
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
        Statics.ENABLE_REWARD = getConfig().getBoolean("rewards.enable", true);
        Statics.ENABLE_PUNISHMENT = getConfig().getBoolean("punishments.enable", false);
        Statics.MONEY_CURRENCY = getConfig().getString("rewards.currency", "$");
        Statics.MESSAGE_DEATH = getConfig().getString("messages.victim", "&7You were killed by &a{{ KILLER }}&7!");

        if (Statics.ENABLE_REWARD) {
            Statics.MESSAGE_KILLER = getConfig().getString("messages.killer", "&7You killed &a{{ VICTIM }}&7 and got &a{{ CURRENCY }}{{ AMOUNT }}&7!");
            Statics.MONEY_BASIC = getConfig().getDouble("rewards.amount", 50.00);
            Statics.MONEY_EXTRA = getConfig().getDouble("rewards.bonus", 100.00);
        }

        if (Statics.ENABLE_PUNISHMENT) {
            Statics.MESSAGE_PUNISHED = getConfig().getString("messages.punished", "&7You were killed by &a{{ KILLER }}&7 and lost &a{{ CURRENCY }}{{ AMOUNT }}!");
            Statics.MONEY_PUNISH = getConfig().getDouble("punishments.amount", 10.00);
        }
    }
}
