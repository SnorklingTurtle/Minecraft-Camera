package main.java.water.of.cup.cameras.listeners;

import main.java.water.of.cup.cameras.Camera;
import main.java.water.of.cup.cameras.Picture;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityRemoveEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
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

        instance.getLogger().info("Picture despawned Id: " + mapId );
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

                instance.getLogger().info("Picture destroyed Id: " + mapId);
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
