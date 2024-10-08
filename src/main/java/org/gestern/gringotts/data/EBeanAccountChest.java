package org.gestern.gringotts.data;

import io.ebean.annotation.DbDefault;
import io.ebean.annotation.NotNull;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@SuppressWarnings("unused")
@Entity
@Table(name = "gringotts_accountchest")
@UniqueConstraint(columnNames = {"world", "x", "y", "z"})
public class EBeanAccountChest {
    @Id
    int id;
    @NotNull
    String world;
    @NotNull
    int x;
    @NotNull
    int y;
    @NotNull
    int z;
    @NotNull
    int account;
    /**
    * Virtual balance.
    */
   @NotNull
   @DbDefault(value = "0")
   long totalValue;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getWorld() {
        return world;
    }

    public void setWorld(String world) {
        this.world = world;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public int getAccount() {
        return account;
    }

    public void setAccount(int account) {
        this.account = account;
    }

    public long getTotalValue() {
        return this.totalValue;
    }

    public void setTotalValue(long totalValue) {
        this.totalValue = totalValue;
    }

    @Override
    public String toString() {
        return "EBeanAccountChest(" + account + "," + totalValue + "," + world + ": " + x + "," + y + "," + z + ")";
    }

}