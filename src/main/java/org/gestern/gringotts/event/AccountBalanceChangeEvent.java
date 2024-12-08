package org.gestern.gringotts.event;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.gestern.gringotts.accountholder.AccountHolder;
import org.jetbrains.annotations.NotNull;

/**
 * The type balance change event.
 */
public class AccountBalanceChangeEvent extends Event {
    /**
     * The constant handlers.
     */
    public static final HandlerList handlers = new HandlerList();
    /**
     * The Holder.
     */
    public final AccountHolder holder;
    /**
     * Account balance.
     */
    public final long balance;

    /**
     * Instantiates a new Calculate start balance event.
     *
     * @param holder the holder
     */
    public AccountBalanceChangeEvent(AccountHolder holder, long balance) {
        super(!Bukkit.getServer().isPrimaryThread());

        this.holder = holder;
        this.balance = balance;
    }

    /**
     * Gets handler list.
     *
     * @return the handler list
     */
    @SuppressWarnings("unused")
    public static HandlerList getHandlerList() {
        return handlers;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public AccountHolder getHolder() {
        return holder;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{holder=" + this.holder.getId() + ",value=" + this.balance + "}";
    }

}
