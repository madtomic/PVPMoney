package com.frostcore.pvpmoney.config;

/**
 * Description: part of PVPMoney
 * Created at: {13:15} on {27/07/2014}
 *
 * @author Connor Spencer Harries
 */
public enum Messages {
    DEATH("&4You were killed by &6%killer% &4and lost &c%currency%%amount%!"),
    PUNISHED("&4You were killed by &6%killer%!"),
    KILLER("&4You killed &c%victim%&4 and got &a%currency%%amount%!");

    private String message = "";
    private Messages(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return message;
    }

    public void setMessage(String newMessage) {
        this.message = newMessage;
    }
}
