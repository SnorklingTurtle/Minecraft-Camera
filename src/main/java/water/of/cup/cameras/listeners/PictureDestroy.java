package main.java.water.of.cup.cameras.listeners;

import main.java.water.of.cup.cameras.Camera;
import main.java.water.of.cup.cameras.MapStorageDB;
import main.java.water.of.cup.cameras.Picture;
import org.bukkit.entity.Item;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;

public class PictureDestroy implements Listener {

    private final Camera instance = Camera.getInstance();
    private static final HashSet<Item> damagedItems = new HashSet<>();

    @EventHandler
    public void onItemDespawnEvent(ItemDespawnEvent event) {
        ItemStack itemStack = event.getEntity().getItemStack();

        if (!Picture.isPicture(itemStack))
            return;

        Integer mapId = Picture.getMapId(itemStack);
        if (mapId == null)
            return;

        long worldSeed = event.getEntity().getWorld().getSeed();
        MapStorageDB.updateCounter(instance.getDbConnection(), mapId, worldSeed, false);
    }

    @EventHandler
    public void onEntityDamageEvent(EntityDamageEvent event) {

        if (!(event.getEntity() instanceof Item))
            return;

        Item itemEntity = (Item) event.getEntity();
        ItemStack itemStack = itemEntity.getItemStack();

        if (!Picture.isPicture(itemStack))
            return;

        damagedItems.add(itemEntity);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!itemEntity.isDead() || !damagedItems.contains(itemEntity))
                    return;

                damagedItems.remove(itemEntity);

                Integer mapId = Picture.getMapId(itemStack);
                if (mapId == null)
                    return;

                long worldSeed = itemEntity.getWorld().getSeed();
                MapStorageDB.updateCounter(instance.getDbConnection(), mapId, worldSeed, false);
            }
        }.runTaskLater(instance, 1);
    }


//    @EventHandler
//    public void onEntityDeathEvent(EntityDeathEvent event) {
//
//        instance.getLogger().info("EntityDeathEvent " + event.getEntityType().name() + ", " + event.getEventName());
//    }
//
//    @EventHandler
//    public void onEntityRemoveEvent(InventoryClickEvent event)
//    {
//        instance.getLogger().info("InventoryClickEvent Slot: " + event.getSlot());
//    }
//
//    @EventHandler
//    public void onInventoryCreative(InventoryCreativeEvent event)
//    {
//        instance.getLogger().info("Slot: " + event.getSlot() + " - inventory: " + event.getClickedInventory().getType().name());
//    }

}
