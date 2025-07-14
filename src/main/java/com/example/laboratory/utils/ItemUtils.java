package com.example.laboratory.utils;

import com.nexomc.nexo.api.NexoItems;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class ItemUtils {
    
    public static ItemStack createCustomItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(MessageUtils.colorize(name));
            if (lore.length > 0) {
                meta.setLore(Arrays.asList(lore));
            }
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    public static ItemStack createNexoItem(String itemId, int amount) {
        try {
            ItemStack item = NexoItems.itemFromId(itemId);
            if (item != null) {
                item.setAmount(amount);
                return item;
            }
        } catch (Exception e) {
            // Fallback to vanilla item if Nexo fails
        }
        return new ItemStack(Material.BARRIER);
    }
    
    public static boolean isNexoItem(ItemStack item, String itemId) {
        if (item == null) return false;
        
        try {
            String id = NexoItems.idFromItem(item);
            return itemId.equals(id);
        } catch (Exception e) {
            return false;
        }
    }
    
    public static String getNexoItemId(ItemStack item) {
        if (item == null) return null;
        
        try {
            return NexoItems.idFromItem(item);
        } catch (Exception e) {
            return null;
        }
    }
    
    public static void updateItemLore(ItemStack item, List<String> newLore) {
        if (item == null) return;
        
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setLore(newLore);
            item.setItemMeta(meta);
        }
    }
}