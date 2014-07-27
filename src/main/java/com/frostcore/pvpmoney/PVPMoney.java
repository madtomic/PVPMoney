package com.frostcore.pvpmoney;

import com.frostcore.pvpmoney.config.Messages;
import com.frostcore.pvpmoney.config.Settings;
import com.frostcore.pvpmoney.events.Death;
import com.sk89q.bukkit.util.CommandsManagerRegistration;
import com.sk89q.minecraft.util.commands.*;
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
 * Description: part of PVPMoney
 * Created at: {12:12} on {27/07/2014}
 *
 * @author Connor Spencer Harries
 */
public final class PVPMoney extends JavaPlugin {
    public static Economy ECONOMY = null;

    private CommandsManager<CommandSender> commands;
    private static PVPMoney instance = null;
    private static String VERSION = "0.7";
    private static int DEATHS = 0;
    private static double PAID = 0;
    private static double LOST = 0;

    @Override
    public void onEnable() {
        instance = this;

        this.saveDefaultConfig();

        if(setupEconomy()) {
            getLogger().info("Using \"" + ECONOMY.getName() + "\" for rewards.");
        } else {
            getLogger().severe("Unable to find a Vault compatible economy.");
            getLogger().severe("Please make sure Vault & a compatible plugin");
            getLogger().severe("installed or this plugin will not function.");
            getPluginLoader().disablePlugin(this);
            return;
        }

        init();
        setupCommands();

        try {
            Metrics metrics = new Metrics(this);
            metrics.start();

            getLogger().info("Yay, Metrics started successfully!");

            Metrics.Graph deathGraph = metrics.createGraph("Bloody Deaths");
            deathGraph.addPlotter(new Metrics.Plotter() {
                @Override
                public int getValue() {
                    return DEATHS;
                }
            });

            Metrics.Graph economyGraph = metrics.createGraph("Economy");

            economyGraph.addPlotter(new Metrics.Plotter("Money Earnt") {
                @Override
                public int getValue() {
                    return (int)PAID;
                }
            });

            economyGraph.addPlotter(new Metrics.Plotter("Money Lost") {
                @Override
                public int getValue() {
                    return (int)LOST;
                }
            });

        } catch (IOException e) {
            getLogger().info("Metrics could not be enabled. :(");
        }

        VERSION = getDescription().getVersion();
        getServer().getPluginManager().registerEvents(new Death(), this);
    }

    private void setupCommands() {
        this.commands = new CommandsManager<CommandSender>() {
            @Override
            public boolean hasPermission(CommandSender sender, String perm) {
                return sender instanceof ConsoleCommandSender || sender.hasPermission(perm);
            }
        };
        CommandsManagerRegistration cmdRegister = new CommandsManagerRegistration(this, this.commands);
        cmdRegister.register(Commands.ParentCommand.class);

    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);
        if (economyProvider != null) {
            ECONOMY = economyProvider.getProvider();
        }
        return (ECONOMY != null);
    }

    /**
     * Use sk89q's command-framework to handle commands.
     * @param sender command sender
     * @param cmd command sent
     * @param alias alias used
     * @param args arguments to use
     * @return true
     */
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
     * Let's be lazy and make a helper to add just one death.
     */
    public void addDeath() {
        DEATHS++;
    }

    /**
     * A stat that tracks how much money PVPMoney
     * has paid over time.
     *
     * @param money amount of money to add
     */
    public void addMoney(Double money) {
        PAID = PAID + money;
    }


    public void addMoneyLost(Double money) {
        LOST = LOST + money;
    }

    /**
     * Lazy cross-class access.
     * @return {@link com.frostcore.pvpmoney.PVPMoney}
     */
    public static PVPMoney getInstance() {
        return instance;
    }

    /**
     * Get the version of the plugin.
     * @return
     */
    public String getVersion() {
        return VERSION;
    }

    /**
     * Send a formatted message
     * @param sender who the message will be sent to
     * @param message message to send
     */
    public static void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Settings.PREFIX + " " + message));
    }

    /**
     * Send a formatted message based on the type of {@link com.frostcore.pvpmoney.config.Messages} received.
     * @param sender who the message will be sent to
     * @param message message type
     * @param args arguments
     */
    public static void sendMessage(CommandSender sender, Messages message, String... args) {
        String msg = message.toString();
        switch(message) {
            case DEATH:
                sendMessage(sender,
                        msg.replace("%killer%", args[0])
                );
                break;
            case PUNISHED:
                sendMessage(sender,
                        msg.replace("%killer%", args[0])
                        .replace("%currency%", Settings.CURRENCY)
                        .replace("%amount%", String.valueOf(Settings.PUNISHMENT))
                );
                break;
            case KILLER:
                sendMessage(sender,
                        msg.replace("%victim%", args[0])
                        .replace("%currency%", Settings.CURRENCY)
                        .replace("%amount%", args[1])
                );
                break;
            default:
                break;
        }
    }

    /**
     * Simple method to load config values.
     */
    protected void init() {
        Settings.PREFIX = getConfig().getString("pvpmoney.prefix", "&8[&6PVPMoney&8]&6");

        Settings.REWARDS_ENABLED = getConfig().getBoolean("pvpmoney.rewards.enabled", true);
        Settings.CURRENCY = getConfig().getString("pvpmoney.rewards.currency", "$");
        Settings.BONUS = getConfig().getDouble("pvpmoney.rewards.ranked", 50.0);
        Settings.BASE = getConfig().getDouble("pvpmoney.rewards.amount", 50.0);

        Settings.PUNISHMENTS_ENABLED = getConfig().getBoolean("pvpmoney.punishments.enabled", false);
        Settings.PUNISHMENT = getConfig().getDouble("pvpmoney.punishments.amount", 5.0);

        Messages.PUNISHED.setMessage(getConfig().getString("pvpmoney.messages.punished", "&4You were killed by &6%killer% &4and lost &c%currency%%amount%!"));
        Messages.DEATH.setMessage(getConfig().getString("pvpmoney.messages.victim", "&4You killed &c%victim%&4 and got &a%currency%%amount%!"));
        Messages.KILLER.setMessage(getConfig().getString("pvpmoney.messages.killer", "&4You were killed by &6%killer%!"));
    }
}
