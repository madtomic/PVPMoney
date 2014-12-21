package de.albionco.pvpmoney.command;

import com.sk89q.minecraft.util.commands.*;
import de.albionco.pvpmoney.Dictionary;
import de.albionco.pvpmoney.MoneyPlugin;
import de.albionco.pvpmoney.Statics;
import org.bukkit.command.CommandSender;

/**
 * Description: part of PVPMoney
 * Created at: {12:21} on {27/07/2014}
 *
 * @author Connor Spencer Harries
 */
@SuppressWarnings("unused")
public class Commands {

    @Command(
            aliases = { "money", "setmoney" },
            desc = "Edit the amount of money per kill.",
            usage = "<amount>",
            min = 1
    )
    public static void money(final CommandContext args, CommandSender sender) throws Exception {
        if (!sender.hasPermission(Statics.PERMISSION_ADMIN)) {
            throw new CommandPermissionsException();
        }

        Statics.MONEY_BASIC = args.getDouble(0);
        sender.sendMessage(Dictionary.format(Statics.ADMIN_MONEY_UPDATE, "AMOUNT", String.valueOf(Statics.MONEY_BASIC)));
    }

    @Command(
            aliases = { "bonus", "setbonus" },
            desc = "Edit the ranked amount of money per kill.",
            usage = "<amount>",
            min = 1
    )
    public static void extra(final CommandContext args, CommandSender sender) throws Exception {
        if (!sender.hasPermission(Statics.PERMISSION_ADMIN)) {
            throw new CommandPermissionsException();
        }

        Statics.MONEY_EXTRA = args.getDouble(0);
        sender.sendMessage(Dictionary.format(Statics.ADMIN_MONEY_UPDATE, "AMOUNT", String.valueOf(Statics.MONEY_EXTRA)));
    }

    @Command(
            aliases = { "reload", "rl" },
            desc = "Reload configuration file."
    )
    public static void reload(final CommandContext args, CommandSender sender) throws Exception {
        if (!sender.hasPermission(Statics.PERMISSION_ADMIN)) {
            throw new CommandPermissionsException();
        }

        MoneyPlugin.getInstance().reloadConfig();
        MoneyPlugin.getInstance().init();
        sender.sendMessage(Dictionary.colour("&aConfiguration file has been reloaded!"));
    }

    @Command(
            aliases = { "test" },
            desc = "Test plugin configuration."
    )
    public static void test(final CommandContext args, CommandSender sender) throws Exception {
        if (!sender.hasPermission(Statics.PERMISSION_ADMIN)) {
            throw new CommandPermissionsException();
        }

        sender.sendMessage(Dictionary.format(Statics.MESSAGE_DEATH, "VICTIM", sender.getName(), "KILLER", "Dinnerbone", "AMOUNT", String.valueOf(sender.hasPermission(Statics.PERMISSION_EXTRA) ? Statics.MONEY_EXTRA : Statics.MONEY_BASIC)));
        sender.sendMessage(Dictionary.format(Statics.MESSAGE_KILLER, "VICTIM", sender.getName(), "KILLER", "Dinnerbone", "AMOUNT", String.valueOf(sender.hasPermission(Statics.PERMISSION_EXTRA) ? Statics.MONEY_EXTRA : Statics.MONEY_BASIC)));
        sender.sendMessage(Dictionary.format(Statics.MESSAGE_PUNISHED, "VICTIM", sender.getName(), "KILLER", "Dinnerbone", "AMOUNT", String.valueOf(sender.hasPermission(Statics.PERMISSION_EXTRA) ? Statics.MONEY_EXTRA : Statics.MONEY_BASIC)));
    }

    @Command(
            aliases = { "toggle" },
            desc = "Toggle rewards and punishments",
            usage = "<rewards|punishments>",
            min = 1
    )
    public static void toggle(final CommandContext args, CommandSender sender) throws Exception {
        if (!sender.hasPermission(Statics.PERMISSION_ADMIN)) {
            throw new CommandPermissionsException();
        }

        String sub = args.getString(0);
        if (sub.equalsIgnoreCase("rewards")) {
            Statics.ENABLE_REWARD = !Statics.ENABLE_REWARD;
        } else if (sub.equalsIgnoreCase("punishments")) {
            Statics.ENABLE_PUNISHMENT = !Statics.ENABLE_PUNISHMENT;
        } else {
            throw new CommandUsageException("Invalid switch provided.", "/pvpmoney toggle <rewards|punishments>");
        }
    }

    public static class ParentCommand {
        @Console
        @Command(
                aliases = { "pvpmoney", "pvpm" },
                desc = "Administration command",
                min = 0
        )
        @NestedCommand(Commands.class)
        public static void pvpmoney(final CommandContext args, CommandSender sender) throws Exception {
            sender.sendMessage(Dictionary.colour("&7PVPMoney &av" + MoneyPlugin.getInstance().getDescription().getVersion() + " &7by &aAlbion"));
        }
    }
}
