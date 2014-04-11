package io.github.charries96.pvpmoney;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class Notifier  extends JavaPlugin implements Listener  {
	
	private String prefix = ChatColor.DARK_RED + "[" + ChatColor.GOLD + "PvPMoney" + ChatColor.DARK_RED + "]";
    public static Economy economy = null;

    private String killmsg = "&bYou killed &6%victim%&b and got &a%currency%%reward%!";
    private String deathmsg = "&4You were killed by %killer%!";
    private Boolean debug = false;
    private String value = "10";
	
	@Override
	public void onEnable() {
		this.saveDefaultConfig();
		
		getServer().getPluginManager().registerEvents(this, this);
		getLogger().info("Registered \"onKill\" event");
		getLogger().info("Registered \"onJoin\" event");
		
		// Lets try hooking into an economy
		if(!setupEconomy())
			getLogger().warning("Economy could not be enabled, money will not be given for kills.");
		else 
			getLogger().info("Economy found, using " + economy.getName() + " for rewards.");
				
		// All that configuration loading junk
		if(this.getConfig().getBoolean("pvpmoney.debug"))
			debug = true;
		if(this.getConfig().getString("pvpmoney.prefix") != "" && this.getConfig().getString("pvpmoney.prefix").length() >= 1)
			prefix = this.getConfig().getString("pvpmoney.prefix") + " ";
		if(this.getConfig().getString("pvpmoney.messages.killer") != "" && this.getConfig().getString("pvpmoney.messages.killer").length() >= 1)
			killmsg = this.getConfig().getString("pvpmoney.messages.killer");
		if(this.getConfig().getString("pvpmoney.messages.victim") != "" && this.getConfig().getString("pvpmoney.messages.victim").length() >= 1)
			deathmsg = this.getConfig().getString("pvpmoney.messages.victim");
		if(this.getConfig().getString("pvpmoney.rewards.amount") != "" && this.getConfig().getString("pvpmoney.rewards.amount").length() >= 1)
			value = this.getConfig().getString("pvpmoney.rewards.amount");
		
		getLogger().info("Loaded config.yml values");
	}

	@Override
	public void onDisable() {
		this.saveConfig();
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
			victim.sendMessage(colourise(prefix + " " + colourise(replaceKiller(deathmsg, killern))));
			
			getLogger().warning(colourise(replaceKiller(deathmsg, killern)));
			
			EconomyResponse r = economy.depositPlayer(killern, Double.parseDouble(value));
			
			if(r.transactionSuccess()) {
				// Make the Killers day
				killer.sendMessage(colourise(prefix + replaceValue(replaceCurrency(replaceVictim(killmsg, victimn)))));
            } else {
            	// Oh no, better compensate them!
            	killer.sendMessage(prefix + ChatColor.DARK_RED + "An error occured when rewarding you.");
            	
            	// Blame the Owner
            	if((economy == null))
            		killer.sendMessage(prefix + ChatColor.RED + "Tell your server administrator to add a Vault compatible economy");
            	
            	// Give them a diamond to compensate
            	killer.getInventory().addItem(new ItemStack(Material.GOLD_INGOT, 1));
            }
			return;
		}
		return;
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e)
	{
		e.getPlayer().sendMessage(colourise(prefix) + ChatColor.GRAY + "PvPMoney Enabled");
		if(debug)
			e.getPlayer().sendMessage(colourise(prefix) + ChatColor.GRAY + "Version 0.3 by charries96");
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
			return str.replace("%currency%", "£");
		return str;
	}
	
	public String replaceValue(String str) {
		if(str.contains("%reward%"))
			return str.replace("%reward%", value);
		return str;
	}
		
	private boolean setupEconomy()
    {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }
        return (economy != null);
    }
}
