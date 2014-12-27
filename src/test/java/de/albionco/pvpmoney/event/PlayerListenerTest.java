package de.albionco.pvpmoney.event;

import de.albionco.pvpmoney.Statics;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.UUID;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({PlayerDeathEvent.class})
public class PlayerListenerTest {

    @Test
    public void testPayments() throws Exception {
        Economy economy = mockEconomy();

        /*
         * Setup our killer and victim
         */
        Player killer = mockKiller();
        when(killer.hasPermission(Statics.PERMISSION_BASIC)).thenReturn(true);
        Player victim = mockVictim(killer);

        /*
         * Setup our event
         */
        PlayerDeathEvent event = mock(PlayerDeathEvent.class);
        when(event.getEntity()).thenReturn(victim);

        /*
         * Setup our listener
         */
        PlayerListener listener = new PlayerListener(economy);
        listener.playerKilledPlayer(event);

        /*
         * Verify the listener tried to deposit the value of Statics.MONEY_BASIC
         */
        verify(economy, only()).depositPlayer(killer, Statics.MONEY_BASIC);

        /*
         * Setup our new killer and victim players
         */
        Player killerTwo = mockKiller();
        when(killerTwo.hasPermission(Statics.PERMISSION_EXTRA)).thenReturn(true);
        Player victimTwo = mockVictim(killerTwo);

        /*
         * Prepare the event
         */
        PlayerDeathEvent eventTwo = mock(PlayerDeathEvent.class);
        when(eventTwo.getEntity()).thenReturn(victimTwo);

        /*
         * Fire the event
         */
        listener.playerKilledPlayer(eventTwo);

        /*
         * Verify the listener tried to deposit the value of Statics.MONEY_EXTRA
         */
        verify(economy, atLeastOnce()).depositPlayer(killerTwo, Statics.MONEY_EXTRA);
    }

    @Test
    public void testExemption() throws Exception {
        Economy economy = mockEconomy();

        Player killer = mockKiller();
        Player victim = mockVictim(killer);
        when(victim.hasPermission(Statics.PERMISSION_EXEMPT)).thenReturn(true);

        PlayerDeathEvent event = mock(PlayerDeathEvent.class);
        when(event.getEntity()).thenReturn(victim);

        PlayerListener listener = new PlayerListener(economy);
        listener.playerKilledPlayer(event);

        verify(economy, never()).withdrawPlayer(victim, Statics.MONEY_PUNISH);
    }

    /**
     * Helper method to create/mock a Vault Economy
     *
     * @return a Vault Economy object
     */
    private Economy mockEconomy() {
        Economy economy = mock(Economy.class);
        when(economy.withdrawPlayer(any(OfflinePlayer.class), anyDouble())).thenReturn(new EconomyResponse(15, 15, EconomyResponse.ResponseType.SUCCESS, ""));
        when(economy.depositPlayer(any(OfflinePlayer.class), anyDouble())).thenReturn(new EconomyResponse(15, 15, EconomyResponse.ResponseType.SUCCESS, ""));
        when(economy.getName()).thenReturn("AlbionTesting");
        return economy;
    }

    /**
     * Helper method to create/mock a Player object
     *
     * @return a Player object
     */
    private Player mockKiller() {
        Player killer = mock(Player.class);
        when(killer.hasPermission(anyString())).thenReturn(false);
        when(killer.getUniqueId()).thenReturn(UUID.randomUUID());
        when(killer.getName()).thenReturn("charries96");
        return killer;
    }

    /**
     * Helper method to create/mock a Player object
     *
     * @param killer see {@link #mockKiller()}
     * @return a Player object
     */
    private Player mockVictim(Player killer) {
        Player victim = mock(Player.class);
        when(victim.hasPermission(anyString())).thenReturn(false);
        when(victim.getUniqueId()).thenReturn(UUID.randomUUID());
        when(victim.getName()).thenReturn("ImAlbion");
        when(victim.getKiller()).thenReturn(killer);
        return victim;
    }
}