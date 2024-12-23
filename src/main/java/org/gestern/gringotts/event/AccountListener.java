package org.gestern.gringotts.event;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.scheduler.BukkitRunnable;
import org.gestern.gringotts.AccountChest;
import org.gestern.gringotts.Configuration;
import org.gestern.gringotts.Gringotts;
import org.gestern.gringotts.GringottsAccount;
import org.gestern.gringotts.Util;

import com.destroystokyo.paper.event.block.BlockDestroyEvent;

import io.papermc.paper.event.player.PlayerOpenSignEvent;

/**
 * Listens for chest creation, destruction and change events.
 *
 * @author jast
 */
public class AccountListener implements Listener {

    private final Pattern VAULT_PATTERN = Pattern.compile(Configuration.CONF.vaultPattern, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    /**
     * Create an account chest by adding a sign marker over it.
     *
     * @param event Event data.
     */
    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        String line0String = ChatColor.stripColor(event.getLine(0)).trim();

        if (line0String == null) {
            return;
        }

        Matcher match = VAULT_PATTERN.matcher(line0String);

        // consider only signs with proper formatting
        if (!match.matches()) {
            return;
        }

        String typeStr = match.group(1).toUpperCase();

        String type;

        // default vault is player
        if (typeStr.isEmpty()) {
            type = "player";
        } else {
            type = typeStr.toLowerCase();
        }

        Optional<Sign> optionalSign = Util.getBlockStateAs(
                event.getBlock(),
                Sign.class
        );

        if (optionalSign.isPresent() && Util.chestBlock(optionalSign.get()) != null) {
            // we made it this far, throw the event to manage vault creation
            final VaultCreationEvent creation = new PlayerVaultCreationEvent(type, event);

            Bukkit.getServer().getPluginManager().callEvent(creation);

            if (creation.isValid()) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        AccountChest chest = getAccountChestFromSign(event.getBlock().getLocation());
                        if (chest.balance(true) > 0) {
                            Bukkit.getPluginManager().callEvent(new AccountBalanceChangeEvent(chest.account.owner, chest.account.getBalance()));
                        }
                    }
                }.runTask(Gringotts.instance);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockDestroy(BlockDestroyEvent event) {
        onBlockBreak(event.getBlock());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        onBlockBreak(event.getBlock());
    }

    private void onBlockBreak(Block block) {
        if (Tag.SIGNS.isTagged(block.getType())) {
            AccountChest chest = getAccountChestFromSign(block.getLocation());
            if (chest == null) return;
            
            if (Gringotts.instance.getDao().deleteAccountChest(chest)) {
                GringottsAccount account = chest.getAccount();
                Bukkit.getPluginManager().callEvent(new AccountBalanceChangeEvent(account.owner, account.getBalance()));
            }
        } else if (Util.isValidContainer(block.getType())) {
            AccountChest chest = getAccountChestFromContainer(block.getLocation(), true);
            if (chest == null) return;
            GringottsAccount account = chest.getAccount();
            Bukkit.getPluginManager().callEvent(new AccountBalanceChangeEvent(account.owner, account.getBalance()));
        }
    }


    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().getLocation() == null || !Util.isValidInventory(event.getInventory().getType())) return;

        AccountChest chest = getAccountChestFromContainer(event.getInventory().getLocation());
        if (chest == null) return;

        chest.setCachedBalance(chest.balance(true));
        if (Gringotts.instance.getDao().updateChestBalance(chest, chest.getCachedBalance())) {
            Bukkit.getPluginManager().callEvent(new AccountBalanceChangeEvent(chest.account.owner, chest.account.getBalance()));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryMoveItem(InventoryMoveItemEvent event) {
        if (Util.isValidInventory(event.getSource().getType())) {
            AccountChest chest = getAccountChestFromContainer(event.getSource().getLocation());
            if (chest != null) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        chest.setCachedBalance(chest.balance(true));
                        if (Gringotts.instance.getDao().updateChestBalance(chest, chest.getCachedBalance())) {
                            Bukkit.getPluginManager().callEvent(new AccountBalanceChangeEvent(chest.account.owner, chest.account.getBalance()));
                        }
                    }
                }.runTask(Gringotts.instance);
            }
        }
        if (event.getDestination() != null && Util.isValidInventory(event.getDestination().getType())) {
            AccountChest chest = getAccountChestFromContainer(event.getDestination().getLocation());
            if (chest != null) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        chest.setCachedBalance(chest.balance(true));
                        if (Gringotts.instance.getDao().updateChestBalance(chest, chest.getCachedBalance())) {
                            Bukkit.getPluginManager().callEvent(new AccountBalanceChangeEvent(chest.account.owner, chest.account.getBalance()));
                        }
                    }
                }.runTask(Gringotts.instance);
            }
        }
    }

    @EventHandler
    public void onDispenseEvent(BlockDispenseEvent event) {
        AccountChest chest = getAccountChestFromContainer(event.getBlock().getLocation());
        if (chest != null) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    chest.setCachedBalance(chest.balance(true));
                    if (Gringotts.instance.getDao().updateChestBalance(chest, chest.getCachedBalance())) {
                        Bukkit.getPluginManager().callEvent(new AccountBalanceChangeEvent(chest.account.owner, chest.account.getBalance()));
                    }
                }
            }.runTask(Gringotts.instance);
        }
    }

    @EventHandler
    public void onSignEdit(PlayerOpenSignEvent event) {
        for (AccountChest chest : Gringotts.instance.getDao().retrieveChests()) {
            if (!chest.isChestLoaded()) continue; // For a sign to be changed, it needs to be loaded
            if (event.getSign().getLocation().equals(chest.sign.getLocation())) {
                event.setCancelled(true);
                return;
            }
        }
    }

    /**
     * Get the AccountChest associated with this {@link InventoryHolder}
     * @param holder
     * @return the {@link AccountChest} or null if none was found
     */
    private AccountChest getAccountChestFromContainer(Location location) {
        return getAccountChestFromContainer(location, false);
    }

    private AccountChest getAccountChestFromContainer(Location location, boolean updateContainerLocations) {
        for (AccountChest chest : Gringotts.instance.getDao().retrieveChests()) {
            if (!chest.isChestLoaded()) continue; // For a chest to be open or interacted with, it needs to be loaded

            if (chest.matchesLocation(location, updateContainerLocations)) {
                return chest;
            }
        }
        return null;
    }

    /**
     * Get the AccountChest from its sign's location
     * @param location
     * @return the {@link AccountChest} or null if none was found
     */
    private AccountChest getAccountChestFromSign(Location location) {
        for (AccountChest chest : Gringotts.instance.getDao().retrieveChests()) {
            if (!chest.isChestLoaded()) continue; // For a chest to be open or interacted with, it needs to be loaded

            if (chest.sign.getLocation().equals(location)) {
                return chest;
            }
        }
        return null;
    } 
}
