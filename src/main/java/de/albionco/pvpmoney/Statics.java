package de.albionco.pvpmoney;

import net.milkbowl.vault.economy.Economy;

/**
 * Class to store variables used in various parts of the plugin
 *
 * @author Connor Spencer Harries
 */
public class Statics {

    /**
     * Permission used to allow players to receive rewards
     */
    public final static String PERMISSION_BASIC = "pvpmoney.basic";

    /**
     * Permission used to allow players to receive "extra" rewards
     */
    public final static String PERMISSION_EXTRA = "pvpmoney.extra";

    /**
     * Permission used to allow players to edit features of the plugin
     */
    public final static String PERMISSION_ADMIN = "pvpmoney.extra";

    /**
     * Permission used to bypass punishments
     */
    public final static String PERMISSION_EXEMPT = "pvpmoney.exempt";

    /**
     * String prepended to a message sent via the dictionary class
     */
    public static String MESSAGE_PREFIX = "&8[&6PVPMoney&8]";

    /**
     * Message displayed to players when they die
     */
    public static String MESSAGE_DEATH = "&7You were killed by &a{{ KILLER }} &7and lost &a{{ CURRENCY }}{{ AMOUNT }}&7!";

    /**
     * Message displayed to players when they kill another player
     */
    public static String MESSAGE_KILLER = "&7You were killed by &a{{ KILLER }} &7and lost &a{{ CURRENCY }}{{ AMOUNT }}&7!";

    /**
     * Message displayed to players when they are punished, overrides #MESSAGE_DEATH
     */
    public static String MESSAGE_PUNISHED = "&7You were killed by &a{{ KILLER }} &7and lost &a{{ CURRENCY }}{{ AMOUNT }}&7!";

    /**
     * Amount of money awarded to players who can receive rewards
     */
    public static double MONEY_BASIC = 50.0;

    /**
     * Amount of money awarded to players who can receive "bonus" rewards
     */
    public static double MONEY_EXTRA = 100.0;

    /**
     * Amount of money deducted from players upon death
     */
    public static double MONEY_PUNISH = 10.0;

    /**
     * Currency character to be used if there is one
     */
    public static String MONEY_CURRENCY = "$";

    /**
     * Store whether the plugin should reward players or not
     */
    public static boolean ENABLE_REWARD = true;

    /**
     * Store whether the plugin should punish players or not
     */
    public static boolean ENABLE_PUNISHMENT = false;

    /**
     * Store the amount of money paid to players for usage in Hidendra's Metrics plugin
     */
    public static double METRICS_PAID = 0.0;

    /**
     * Store the amount of money taken from players for usage in Hidendra's Metrics plugin
     */
    public static double METRICS_PUNISHED = 0.0;

    /**
     * Store the amount of players killed for usage in Hidendra's Metrics plugin
     */
    public static int METRICS_DEATHS = 0;

    /**
     * Store the Economy to use with Vault
     */
    public static Economy ECONOMY = null;

    /**
     * Message sent back to the sender when #MONEY_BASIC is updated
     */
    public static String ADMIN_MONEY_UPDATE = "{{ PREFIX }} Value set to: {{ CURRENCY }}{{ AMOUNT }}";

}
