package com.frostcore.pvpmoney.config;

import org.bukkit.entity.Player;

/**
 * Description: part of PVPMoney
 * Created at: {22:09} on {26/07/2014}
 *
 * @author Connor Spencer Harries
 */
public enum Permissions {
    NORMAL("pvpmoney.reward"),
    EXTRA("pvpmoney.extra"),
    ADMIN("pvpmoney.admin"),
    EXEMPT("pvpmoney.exempt");

    private String node;

    private Permissions(String node) {
        this.node = node;
    }

    @Override
    public String toString() {
        return this.node;
    }

    public static boolean has(Player player, Permissions permission) {
        return player.hasPermission(permission.toString());
    }

    public boolean has(Player player) {
        return player.hasPermission(this.toString());
    }
}
