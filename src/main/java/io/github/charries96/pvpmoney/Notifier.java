package io.github.charries96.pvpmoney;

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
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class Notifier extends JavaPlugin implements Listener {
	
	private String prefix = ChatColor.DARK_RED + "[" + ChatColor.GOLD + "PvPMoney" + ChatColor.DARK_RED + "]";
    public static Economy economy = null;
    public static Permission permissions = null;
    
    private String killmsg = "&4You killed &c%victim%&4 and got &a%currency%%reward%!";
    private String deathmsg = "&4You were killed by %killer%!";
    private String value = "10";
    private String extra = "15";
    private String currency = "£";
    private Boolean debug = false;
	
	@Override
	public void onEnable() {
		this.saveDefaultConfig();
		
		getServer().getPluginManager().registerEvents(this, this);
		getLogger().info("Registered \"onKill\" event");
		getLogger().info("Registered \"onJoin\" event");
		
		// Lets try hooking into an economy & permissions system
		if(!setupEconomy())
			getLogger().warning("Economy could not be enabled, money will not be given for kills.");
		else 
			getLogger().info("Economy found, using " + economy.getName() + " for rewards.");
		if(!setupPermissions())
			getLogger().warning("A vault compatible Permissions system could not be found, extra funds will not be given even if a compatible Economy is installed.");
		else
			getLogger().info("Permissions found, using " + permissions.getName() + ".");
		
		loadConfig();
		getLogger().info("Currency symbol: " + currency);
		getLogger().info("Reward (w/o Extra): " + value);
		getLogger().info("Reward (w/ Extra): " + extra);
		getLogger().info("Loaded config.yml values");
	}
	
	private void loadConfig() {
		
		if(this.getConfig().getString("pvpmoney.prefix") != "" && this.getConfig().getString("pvpmoney.prefix").length() >= 1)
			prefix = this.getConfig().getString("pvpmoney.prefix");
		
		if(this.getConfig().getString("pvpmoney.messages.killer") != "" && this.getConfig().getString("pvpmoney.messages.killer").length() >= 1)
			killmsg = this.getConfig().getString("pvpmoney.messages.killer");
		
		if(this.getConfig().getString("pvpmoney.messages.victim") != "" && this.getConfig().getString("pvpmoney.messages.victim").length() >= 1)
			deathmsg = this.getConfig().getString("pvpmoney.messages.victim");
		
		if(this.getConfig().getString("pvpmoney.rewards.amount") != "" && this.getConfig().getString("pvpmoney.rewards.amount").length() >= 1)
			value = this.getConfig().getString("pvpmoney.rewards.amount");
		
		if(this.getConfig().getString("pvpmoney.rewards.currency") != "" && this.getConfig().getString("pvpmoney.rewards.currency").length() >= 1)
			currency = this.getConfig().getString("pvpmoney.rewards.currency");
		
		if(this.getConfig().getString("pvpmoney.rewards.ranked") != "" && this.getConfig().getString("pvpmoney.rewards.ranked").length() >= 1)
			extra = this.getConfig().getString("pvpmoney.rewards.ranked");
		
		debug = this.getConfig().getBoolean("pvpmoney.debug");
	}

	@Override
	public void onDisable() {
		this.saveConfig();
		getLogger().info("Saved configuration file.");
	}
			
	private void doHelp(CommandSender sender) {
		sender.sendMessage(ChatColor.AQUA + "------ " + ChatColor.DARK_AQUA + "[" + ChatColor.GOLD + "PvPMoney by charries96" + ChatColor.DARK_AQUA + "]" + ChatColor.AQUA + " ------");

		sendHelp(sender, "help", "Display this page");
		sendHelp(sender, "extra", "<amount>", "Set money per kill (w/ pvpmoney.extra)");
		sendHelp(sender, "money", "<amount>", "Set money per kill (w/o pvpmoney.extra)");
		sendHelp(sender, "test", "Display messages users would see");
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("pvpmoney")) {
			if(args.length < 1 || args == null)
				doHelp(sender);
			if(args != null && args.length > 0) {
				if(args[0].equalsIgnoreCase("extra")) {
					if(args[1] != null && args[1].length() >= 1) {
						try {
							double nmoney = Double.parseDouble(args[1]);
							extra = "" + nmoney;
							sender.sendMessage(ChatColor.GREEN + replaceValue(replaceCurrency("Players will now receive %currency%%reward% per kill."), true));
							this.getConfig().set("pvpmoney.rewards.ranked", Double.parseDouble(extra));
							this.saveConfig();
							return true;
						} catch (NumberFormatException e) {
							sender.sendMessage(ChatColor.RED + "Error parsing value.");
							return false;
						}
					} else {
						sender.sendMessage(ChatColor.GREEN + "Value: " + extra);
						sender.sendMessage(ChatColor.GREEN + "To set: /pvpmoney extra <amount>");
					}
					return false;
				} else if(args[0].equalsIgnoreCase("money")) {
					if(args[1] != null && args[1].length() >= 1) {
						try {
							double nmoney = Double.parseDouble(args[1]);
							value = "" + nmoney;
							sender.sendMessage(ChatColor.GREEN + replaceValue(replaceCurrency("Players will now receive %currency%%reward% per kill."), false));
							this.getConfig().set("pvpmoney.rewards.amount", Double.parseDouble(value));
							this.saveConfig();
							return true;
						} catch (NumberFormatException e) {
							sender.sendMessage(ChatColor.RED + "Error parsing value.");
							return false;
						}
					} else {
						sender.sendMessage(ChatColor.GREEN + "Value: " + value);
						sender.sendMessage(ChatColor.GREEN + "To set: /pvpmoney money <amount>");
					}
				} else if(args[0].equalsIgnoreCase("test")) {
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
		sender.sendMessage(colourise(replaceValue(replaceCurrency(replaceVictim(replaceKiller(killmsg, killer), victim)), false)));
		sender.sendMessage(colourise(replaceValue(replaceCurrency(replaceVictim(replaceKiller(killmsg, killer), victim)), true)));
		sender.sendMessage(colourise(replaceKiller(deathmsg, killer)));
		sender.sendMessage(ChatColor.RED + replaceCurrency("Currency Symbol: %currency%"));
		sender.sendMessage(ChatColor.RED + replaceValue("Regular Reward: %reward%", false));
		sender.sendMessage(ChatColor.RED + replaceValue("\"Extra\" Reward: %reward%", true));
	}
	
	public void sendHelp(CommandSender sender, String subcommand, String description) {
		sender.sendMessage(ChatColor.AQUA + "/pvpmoney " + subcommand + " - " + description);
	}
	
	public void sendHelp(CommandSender sender, String subcommand, String args, String description) {
		sender.sendMessage(ChatColor.AQUA + "/pvpmoney " + subcommand + " " + args + " - " + description);
	}
	
	@EventHandler
	public void onKill(PlayerDeathEvent e)
	{
		if(e.getEntity() instanceof Player && e.getEntity().getKiller() instanceof Player) {
			
			// BEGIN: Get Killer & Victim name
			Player killer = e.getEntity().getKiller();
			Player victim = e.getEntity();
			String killern = e.getEntity().getKiller().getName();
			String victimn = e.getEntity().getName();
			// END: Get Killer & Victim name
			
			// Tell our Victim about their death.
			victim.sendMessage(colourise(prefix + " " + replaceKiller(deathmsg, killern)));
						
			EconomyResponse r = null;
			if(permissions.has(killer, "pvpmoney.extra"))
				r = economy.depositPlayer(killern, Double.parseDouble(extra));
			else
				r = economy.depositPlayer(killern, Double.parseDouble(value));
			
			if(r.transactionSuccess()) {
				// Make the Killers day
				if(permissions.has(killer, ""))
					killer.sendMessage(replaceCurrency(colourise(prefix) + " " + replaceValue(replaceVictim(killmsg, victimn), true)));
				else
					killer.sendMessage(replaceCurrency(colourise(prefix) + " " + replaceValue(replaceVictim(killmsg, victimn), false)));
            } else {
            	// Oh no, better compensate them!
            	killer.sendMessage(prefix + ChatColor.DARK_RED + " An error occured when rewarding you.");
            	
            	// Blame the Owner
            	if((economy == null))
            		killer.sendMessage(prefix + ChatColor.RED + " Ask your server administrator to install a Vault compatible economy.");
            	
            	// Give them a diamond to compensate
            	killer.getInventory().addItem(new ItemStack(Material.DIAMOND, 1));
            }
			return;
		}
		return;
	}
		
	@EventHandler
	public void onJoin(PlayerJoinEvent e)
	{
		e.getPlayer().sendMessage(colourise(prefix) + ChatColor.GRAY + "PvPMoney Enabled");
		if(permissions.has(e.getPlayer(), "pvpmoney.admin")) {
			e.getPlayer().sendMessage("Using currency symbol: " + currency);
			e.getPlayer().sendMessage("Money per kill: " + currency + value);
			e.getPlayer().sendMessage("Money per kill (w/Extra): " + currency + extra);
			e.getPlayer().sendMessage("Extra money: " + (permissions.has(e.getPlayer(), "pvpmoney.extra")));
		}
	}
	
	public String colourise(String str) {
		return ChatColor.translateAlternateColorCodes('&', str);
	}
	
	public String replaceKiller(String str, String name) {
		if(str.contains("%killer%"))
			return str.replace("%killer%", name);
		return str;
	}
	
	public String replaceVictim(String str, String name) {
		if(str.contains("%victim%"))
			return str.replace("%victim%", name);
		return str;
	}
	
	public String replaceCurrency(String str) {
		if(str.contains("%currency%"))
			return str.replace("%currency%", currency);
		return str;
	}
	
	public String replaceValue(String str, Boolean extra) {
		if(str.contains("%reward%"))
			return (extra ? str.replace("%reward%", this.extra) : str.replace("%reward%", value));
		return str;
	}
		
	private boolean setupEconomy()
    {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }
        return (economy != null);
    }
	private boolean setupPermissions()
    {
        RegisteredServiceProvider<Permission> permProvider = getServer().getServicesManager().getRegistration(Permission.class);
        if (permProvider != null) {
            permissions = permProvider.getProvider();
        }
        return (permissions != null);
    }
}
