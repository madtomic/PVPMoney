package com.frostcore.pvpmoney;

import com.frostcore.pvpmoney.config.Messages;
import com.frostcore.pvpmoney.config.Permissions;
import com.frostcore.pvpmoney.config.Settings;
import com.sk89q.minecraft.util.commands.*;
import org.bukkit.command.CommandSender;

/**
 * Description: part of PVPMoney
 * Created at: {12:21} on {27/07/2014}
 *
 * @author Connor Spencer Harries
 */
@SuppressWarnings("unused")
public class Commands {

    public static class ParentCommand {
        @Command(
                aliases = {"pvpmoney", "pvpm"},
                desc = "Administration command",
                min = 0
        )
        @NestedCommand(Commands.class)
        public static void pvpmoney(final CommandContext args, CommandSender sender) throws Exception {
            sender.sendMessage("&7----- &8[&6PVPMoney v" + PVPMoney.getInstance().getVersion() + " by charries96&8] &7-----");
        }
    }

    @Command(
            aliases = { "money", "setmoney" },
            desc = "Edit the amount of money per kill.",
            usage = "<amount>",
            min = 1
    )
    public static void money(final CommandContext args, CommandSender sender) throws Exception {
        if(!sender.hasPermission(Permissions.ADMIN.toString())) {
            throw new CommandPermissionsException();
        }

        Settings.BASE = args.getDouble(0);
        PVPMoney.sendMessage(sender, "Bonus set: " + Settings.CURRENCY + Settings.BASE);
    }

    @Command(
            aliases = { "bonus", "setbonus" },
            desc = "Edit the ranked amount of money per kill.",
            usage = "<amount>",
            min = 1
    )
    public static void extra(final CommandContext args, CommandSender sender) throws Exception {
        if(!sender.hasPermission(Permissions.ADMIN.toString())) {
            throw new CommandPermissionsException();
        }

        Settings.BONUS = args.getDouble(0);
        PVPMoney.sendMessage(sender, "&7Amount set: &6" + Settings.CURRENCY + Settings.BONUS);
    }

    @Command(
            aliases = { "reload", "rl" },
            desc = "Reload configuration file."
    )
    public static void reload(final CommandContext args, CommandSender sender) throws Exception {
        if(!sender.hasPermission(Permissions.ADMIN.toString())) {
            throw new CommandPermissionsException();
        }

        PVPMoney.getInstance().reloadConfig();
        PVPMoney.getInstance().init();
        PVPMoney.sendMessage(sender, "&aConfiguration reloaded.");
    }

    @Command(
            aliases = { "test" },
            desc = "Test plugin configuration."
    )
    public static void test(final CommandContext args, CommandSender sender) throws Exception {
        if(!sender.hasPermission(Permissions.ADMIN.toString())) {
            throw new CommandPermissionsException();
        }

        PVPMoney.sendMessage(sender, Messages.DEATH, sender.getName());
        PVPMoney.sendMessage(sender, Messages.KILLER, sender.getName(), String.valueOf(Settings.BASE));
        PVPMoney.sendMessage(sender, Messages.KILLER, sender.getName(), String.valueOf(Settings.BONUS));
        PVPMoney.sendMessage(sender, Messages.PUNISHED, sender.getName());
    }


    @Command(
            aliases = { "toggle" },
            desc = "Toggle rewards and punishments",
            usage = "<rewards|punishments>",
            min = 1
    )
    public static void toggle(final CommandContext args, CommandSender sender) throws Exception {
        if(!sender.hasPermission(Permissions.ADMIN.toString())) {
            throw new CommandPermissionsException();
        }

        String sub = args.getString(0);
        if(sub.equalsIgnoreCase("rewards")) {
            Settings.REWARDS_ENABLED = !Settings.REWARDS_ENABLED;
        } else if(sub.equalsIgnoreCase("punishments")) {
            Settings.PUNISHMENTS_ENABLED = !Settings.PUNISHMENTS_ENABLED;
        } else {
            throw new CommandUsageException("Invalid switch provided.", "/pvpmoney toggle <rewards|punishments>");
        }
    }
}
