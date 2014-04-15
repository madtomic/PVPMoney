package io.github.charries96.pvpmoney;

import java.io.IOException;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class Notifier extends JavaPlugin implements Listener {

	private String prefix = ChatColor.DARK_RED + "[" + ChatColor.GOLD
			+ "PvPMoney" + ChatColor.DARK_RED + "]";
	public static Economy economy = null;
	public static Permission permissions = null;

	private String killmsg = "";
	private String deathmsg = "";
	private String punishmsg = "";
	private String value = "50";
	private String extra = "100";
	private String currency = "£";
	private String punishment = "50";
	private Boolean usePunishments = false;
	private Boolean useRewards = false;

	@Override
	public void onEnable() {
		this.saveDefaultConfig();

		getServer().getPluginManager().registerEvents(this, this);
		getLogger().info("Registered \"onKill\" event");
		getLogger().info("Registered \"onJoin\" event");

		// Lets try hooking into an economy & permissions system
		if (!setupEconomy())
			getLogger()
					.warning(
							"Economy could not be enabled, money will not be given for kills.");
		else
			getLogger().info(
					"Economy found, using " + economy.getName()
							+ " for rewards.");
		if (!setupPermissions())
			getLogger()
					.warning(
							"A vault compatible Permissions system could not be found, extra funds will not be given even if a compatible Economy is installed.");
		else
			getLogger().info(
					"Permissions found, using " + permissions.getName() + ".");

		try {
			Metrics metrics = new Metrics(this);
			metrics.start();
			getLogger().info("Metrics enabled");
		} catch (IOException e) {
			getLogger().info("Metrics could not be enabled :(");
		}

		loadConfig();

	}

	private void loadConfig() {
		prefix = getString("pvpmoney.prefix");

		currency = getString("pvpmoney.rewards.currency");
		deathmsg = getString("pvpmoney.messages.victim");
		extra = getString("pvpmoney.rewards.ranked");
		killmsg = getString("pvpmoney.messages.killer");
		punishment = getString("pvpmoney.punishments.amount");
		punishmsg = getString("pvpmoney.messages.punished");
		value = getString("pvpmoney.rewards.amount");

		if (this.getConfig().getBoolean("pvpmoney.rewards.enabled"))
			useRewards = true;
		if (this.getConfig().getBoolean("pvpmoney.punishments.enabled"))
			usePunishments = true;
	}

	private String getString(String node) {
		if (this.getConfig().getString(node) != ""
				&& this.getConfig().getString(node).length() >= 1)
			return this.getConfig().getString(node);
		return "";
	}

	@Override
	public void onDisable() {
		this.saveConfig();
		getLogger().info("Saved configuration file.");
	}

	private void doHelp(CommandSender sender) {
		sender.sendMessage(ChatColor.AQUA + "------ " + ChatColor.DARK_AQUA
				+ "[" + ChatColor.GOLD + "PvPMoney by charries96"
				+ ChatColor.DARK_AQUA + "]" + ChatColor.AQUA + " ------");

		sendHelp(sender, "help", "Display this page");
		sendHelp(sender, "extra", "<amount>",
				"Set money per kill (w/ pvpmoney.extra)");
		sendHelp(sender, "money", "<amount>",
				"Set money per kill (w/o pvpmoney.extra)");
		sendHelp(sender, "punish", "<amount>", "Set money lost on death");
		sendHelp(sender, "money", "<true/false>",
				"Enable or disable punishments");
		sendHelp(sender, "test", "Display messages users would see");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label,
			String[] args) {
		if (cmd.getName().equalsIgnoreCase("pvpmoney")) {
			if (args.length < 1 || args == null)
				doHelp(sender);
			if (args != null && args.length > 0) {
				if (args[0].equalsIgnoreCase("extra")) {
					if (args[1] != null && args[1].length() >= 1) {
						try {
							double nmoney = Double.parseDouble(args[1]);
							extra = "" + nmoney;
							sender.sendMessage(ChatColor.GREEN
									+ replaceValue(
											replaceCurrency("Players will now receive %currency%%amount% per kill."),
											AMOUNT.Extra));
							this.getConfig().set("pvpmoney.rewards.ranked",
									Double.parseDouble(extra));
							this.saveConfig();
							return true;
						} catch (NumberFormatException e) {
							sender.sendMessage(ChatColor.RED
									+ "Error parsing value.");
							return false;
						}
					} else {
						sender.sendMessage(ChatColor.GREEN
								+ "Players currently receive " + currency
								+ extra + " per kill.");
					}
					return false;
				} else if (args[0].equalsIgnoreCase("money")) {
					if (args[1] != null && args[1].length() >= 1) {
						try {
							double nmoney = Double.parseDouble(args[1]);
							value = "" + nmoney;
							sender.sendMessage(ChatColor.GREEN
									+ replaceValue(
											replaceCurrency("Players will now receive %currency%%amount% per kill."),
											AMOUNT.Reward));
							this.getConfig().set("pvpmoney.rewards.amount",
									Double.parseDouble(value));
							this.saveConfig();
							return true;
						} catch (NumberFormatException e) {
							sender.sendMessage(ChatColor.RED
									+ "Error parsing value.");
							return false;
						}
					} else {
						sender.sendMessage(ChatColor.GREEN
								+ "Players currently receive " + currency
								+ value + " per kill.");
					}
				} else if (args[0].equalsIgnoreCase("punish")) {
					if (args[1] != null && args[1].length() >= 1) {
						if (args[1].equalsIgnoreCase("true")) {
							usePunishments = true;
							sender.sendMessage(ChatColor.GREEN
									+ "Punishments Enabled.");
							this.getConfig().set("pvpmoney.punishments.enable",
									usePunishments);
							this.saveConfig();
						} else if (args[1].equalsIgnoreCase("false")) {
							usePunishments = false;
							sender.sendMessage(ChatColor.RED
									+ "Punishments disabled.");
							this.getConfig().set("pvpmoney.punishments.enable",
									usePunishments);
							this.saveConfig();
						} else {
							try {
								double nmoney = Double.parseDouble(args[1]);
								punishment = "" + nmoney;
								sender.sendMessage(ChatColor.GREEN
										+ replaceValue(
												replaceCurrency("Players will now lose %currency%%amount% on death."),
												AMOUNT.Punishment));
								this.getConfig().set(
										"pvpmoney.punishments.amount",
										Double.parseDouble(punishment));
								this.saveConfig();
								return true;
							} catch (NumberFormatException e) {
								sender.sendMessage(ChatColor.RED
										+ "Error parsing value.");
								return false;
							}
						}
					} else
						sender.sendMessage(ChatColor.GREEN
								+ "Players currently lose " + currency
								+ punishment + " per death.");

				} else if (args[0].equalsIgnoreCase("reload")) {
					this.reloadConfig();
					sender.sendMessage(ChatColor.GREEN + "Config.yml reloaded.");
				} else if (args[0].equalsIgnoreCase("test")) {
					spoof(sender, "Dinnerbone", "Dinnerbone");
					return true;
				} else {
					doHelp(sender);
					return false;
				}
			}
		}
		return false;
	}

	public void spoof(CommandSender sender, String killer, String victim) {
		sender.sendMessage(replaceValue(
				replaceCurrency(replaceVictim(colourise(killmsg), victim)),
				AMOUNT.Reward));
		sender.sendMessage(replaceValue(
				replaceCurrency(replaceVictim(colourise(killmsg), victim)),
				AMOUNT.Extra));
		sender.sendMessage(replaceValue(
				replaceCurrency(replaceKiller(colourise(punishmsg), killer)),
				AMOUNT.Punishment));
		sender.sendMessage(colourise(replaceKiller(deathmsg, killer)));
		sender.sendMessage(ChatColor.RED
				+ replaceCurrency("Currency Symbol: %currency%"));
		sender.sendMessage(ChatColor.RED
				+ replaceValue("Regular Reward: %amount%", AMOUNT.Reward));
		sender.sendMessage(ChatColor.RED
				+ replaceValue("\"Extra\" Reward: %amount%", AMOUNT.Extra));
		sender.sendMessage(ChatColor.RED
				+ replaceValue("Punishment: %amount%", AMOUNT.Punishment));
	}

	public void sendHelp(CommandSender sender, String subcommand,
			String description) {
		sender.sendMessage(ChatColor.AQUA + "/pvpmoney " + subcommand + " - "
				+ description);
	}

	public void sendHelp(CommandSender sender, String subcommand, String args,
			String description) {
		sender.sendMessage(ChatColor.AQUA + "/pvpmoney " + subcommand + " "
				+ args + " - " + description);
	}

	@EventHandler
	public void onKill(PlayerDeathEvent e) {
		if (e.getEntity() instanceof Player
				&& e.getEntity().getKiller() instanceof Player) {

			// BEGIN: Get Killer & Victim name
			Player killer = e.getEntity().getKiller();
			Player victim = e.getEntity();
			String killern = e.getEntity().getKiller().getName();
			String victimn = e.getEntity().getName();
			// END: Get Killer & Victim name

			// Tell our Victim about their death.
			if (usePunishments) {
				EconomyResponse p = null;
				if (!permissions.has(victim, "pvpmoney.punishments.exempt")) {
					p = economy.withdrawPlayer(victimn,
							Double.parseDouble(punishment));
					if (p.transactionSuccess())
						victim.sendMessage(prefix()
								+ replaceCurrency(colourise(replaceValue(
										replaceKiller(punishmsg, killern),
										AMOUNT.Punishment))));
					else
						victim.sendMessage(prefix()
								+ colourise("&cError removing funds from your account."));
				} else {
					victim.sendMessage(prefix()
							+ colourise(replaceKiller(deathmsg, killern)));
				}
			}

			if (useRewards) {
				EconomyResponse r = null;

				if (permissions.has(killer, "pvpmoney.extra"))
					r = economy.depositPlayer(killern,
							Double.parseDouble(extra));
				else
					r = economy.depositPlayer(killern,
							Double.parseDouble(value));

				if (r.transactionSuccess()) {
					if (permissions.has(killer, "pvpmoney.extra"))
						killer.sendMessage(replaceCurrency(prefix()
								+ colourise(replaceValue(
										replaceVictim(killmsg, victimn),
										AMOUNT.Extra))));
					else
						killer.sendMessage(replaceCurrency(prefix()
								+ colourise(replaceValue(
										replaceVictim(killmsg, victimn),
										AMOUNT.Reward))));
					return;
				} else {
					// Oh no, better compensate them!
					killer.sendMessage(prefix() + ChatColor.DARK_RED
							+ " An error occured when rewarding you.");

					// Blame the Owner
					if ((economy == null))
						killer.sendMessage(prefix()
								+ ChatColor.RED
								+ " Ask your server administrator to install a Vault compatible economy.");

					// Give them a diamond to compensate
					killer.getInventory().addItem(
							new ItemStack(Material.DIAMOND, 1));
				}
			}
		}
		return;
	}

	private String prefix() {
		return colourise(prefix) + " ";
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		e.getPlayer().sendMessage(
				prefix() + ChatColor.GRAY + " PvPMoney Enabled");
	}

	public String colourise(String str) {
		if (!str.isEmpty() || str != null)
			if (str.contains("&"))
				return ChatColor.translateAlternateColorCodes('&', str);
		if (str.contains("§"))
			return ChatColor.translateAlternateColorCodes('§', str);
		return str;
	}

	public String replaceKiller(String str, String name) {
		if (str.contains("%killer%"))
			return str.replace("%killer%", name);
		return str;
	}

	public String replaceVictim(String str, String name) {
		if (str.contains("%victim%"))
			return str.replace("%victim%", name);
		return str;
	}

	public String replaceCurrency(String str) {
		if (str.contains("%currency%"))
			return str.replace("%currency%", currency);
		return str;
	}

	public String replaceValue(String str, AMOUNT amount) {
		switch (amount) {
		case Reward:
			return str.replace("%amount%", this.value);
		case Punishment:
			return str.replace("%amount%", this.punishment);
		case Extra:
			return str.replace("%amount%", this.extra);
		default:
			getLogger().warning("Could not replace amount variable.");
			return str;
		}
	}

	private boolean setupEconomy() {
		RegisteredServiceProvider<Economy> economyProvider = getServer()
				.getServicesManager().getRegistration(Economy.class);
		if (economyProvider != null) {
			economy = economyProvider.getProvider();
		}
		return (economy != null);
	}

	private boolean setupPermissions() {
		RegisteredServiceProvider<Permission> permProvider = getServer()
				.getServicesManager().getRegistration(Permission.class);
		if (permProvider != null) {
			permissions = permProvider.getProvider();
		}
		return (permissions != null);
	}

	public enum AMOUNT {
		Reward, Extra, Punishment
	}
}
