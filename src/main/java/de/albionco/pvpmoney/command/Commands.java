package de.albionco.pvpmoney.command;

import com.sk89q.minecraft.util.commands.*;
import de.albionco.pvpmoney.Dictionary;
import de.albionco.pvpmoney.MoneyPlugin;
import de.albionco.pvpmoney.Statics;
import org.bukkit.command.CommandSender;

/**
 * First time using sk89q's command framework, don't judge me.
 * Not 100% pleased at the result but not unhappy either.
 *
 * @author Connor Spencer Harries
 */
@SuppressWarnings("unused")
public class Commands {

    @Command(
            aliases = { "money", "setmoney" },
            desc = "edit the amount of money per kill.",
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
            desc = "edit the ranked amount of money per kill.",
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
            desc = "reload configuration file."
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
            desc = "test plugin configuration."
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
            desc = "toggle rewards and punishments",
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
        } else if (sub.equalsIgnoreCase("debts")) {
            if (!Statics.ENABLE_DEBTS) {
                if (!Statics.ENABLE_PUNISHMENT) {
                    Statics.ENABLE_PUNISHMENT = true;
                }
            }
            Statics.ENABLE_DEBTS = !Statics.ENABLE_DEBTS;
            if (!Statics.ENABLE_DEBTS) {
                MoneyPlugin.getInstance().cancelTask();
            } else {
                MoneyPlugin.getInstance().scheduleTask();
            }
        } else {
            throw new CommandUsageException("Invalid switch provided.", "/pvpmoney toggle <rewards|punishments>");
        }
    }

    public static class ParentCommand {
        @Console
        @Command(
                aliases = { "pvpmoney", "pvpm" },
                desc = "administration command for PVPMoney",
                min = 0
        )
        @NestedCommand(Commands.class)
        public static void pvpmoney(final CommandContext args, CommandSender sender) throws Exception {
            sender.sendMessage(Dictionary.colour("&7PVPMoney &av" + MoneyPlugin.getInstance().getDescription().getVersion() + " &7by &aAlbion"));
        }
    }
}
