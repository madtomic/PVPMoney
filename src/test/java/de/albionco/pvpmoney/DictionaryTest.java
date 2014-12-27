package de.albionco.pvpmoney;

import org.bukkit.ChatColor;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Simple class to test if {@link de.albionco.pvpmoney.Dictionary#format(String, String...)} and {@link de.albionco.pvpmoney.Dictionary#format(String, boolean, String...)} are working correctly
 *
 * @author Connor Spencer Harries
 */
public class DictionaryTest {

    private static String name = "Albion";

    @Test
    public void testFormat() throws Exception {
        String message = "Hello {{ NAME }}";
        message = Dictionary.format(message, "NAME", name);
        assertEquals(message, "Hello " + name);
    }

    @Test
    public void testFormatOpt() throws Exception {
        String message = "&aHello {{ NAME }}";
        message = Dictionary.format(message, "NAME", name);
        assertEquals(message, ChatColor.GREEN + "Hello " + name);
    }
}