package de.albionco.pvpmoney;

import org.bukkit.ChatColor;

/**
 * Simple utility class to provide String formatting methods
 *
 * @author Connor Spencer Harries
 */
public class Dictionary {

    public static String format(String input, boolean colour, String... args) {
        if (args.length % 2 == 0) {

            input = input.replace("{{ PREFIX }}", Statics.MESSAGE_PREFIX + " ");
            input = input.replace("{{ CURRENCY }}", Statics.MONEY_CURRENCY);

            for (int i = 0; i < args.length; i += 2) {
                input = input.replace("{{ " + args[i].toUpperCase() + " }}", args[i + 1]);
            }
        }

        return colour ? colour(input) : input;
    }

    public static String format(String input, String... args) {
        return Dictionary.format(input, true, args);
    }

    public static String colour(String str) {
        return ChatColor.translateAlternateColorCodes('&', str);
    }

}
