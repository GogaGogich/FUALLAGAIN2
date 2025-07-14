package com.example.laboratory.managers;

import com.example.laboratory.LaboratoryPlugin;
import com.nexomc.nexo.api.NexoItems;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

public class RecipeManager {
    
    private final LaboratoryPlugin plugin;
    
    public RecipeManager(LaboratoryPlugin plugin) {
        this.plugin = plugin;
    }
    
    public void registerRecipes() {
        registerUraniumIngotRecipe();
        registerUraniumBlockRecipe();
        registerLaboratoryTerminalRecipe();
        registerAssemblerRecipe();
        registerGeigerCounterRecipe();
        registerBasicCapsuleRecipe();
    }
    
    private void registerUraniumIngotRecipe() {
        ItemStack uraniumIngot = NexoItems.itemFromId("uranium_ingot").build();
        if (uraniumIngot == null) return;
        
        NamespacedKey key = new NamespacedKey(plugin, "uranium_ingot");
        ShapedRecipe recipe = new ShapedRecipe(key, uraniumIngot);
        recipe.shape("UUU", "UUU", "UUU");
        
        ItemStack uraniumDust = NexoItems.itemFromId("uranium_dust").build();
        if (uraniumDust != null) {
            recipe.setIngredient('U', uraniumDust.getType());
        }
        
        Bukkit.addRecipe(recipe);
    }
    
    private void registerUraniumBlockRecipe() {
        ItemStack uraniumBlock = NexoItems.itemFromId("uranium_block").build();
        if (uraniumBlock == null) return;
        
        NamespacedKey key = new NamespacedKey(plugin, "uranium_block");
        ShapedRecipe recipe = new ShapedRecipe(key, uraniumBlock);
        recipe.shape("III", "III", "III");
        
        ItemStack uraniumIngot = NexoItems.itemFromId("uranium_ingot").build();
        if (uraniumIngot != null) {
            recipe.setIngredient('I', uraniumIngot.getType());
        }
        
        Bukkit.addRecipe(recipe);
    }
    
    private void registerLaboratoryTerminalRecipe() {
        ItemStack terminal = NexoItems.itemFromId("laboratory_terminal").build();
        if (terminal == null) return;
        
        NamespacedKey key = new NamespacedKey(plugin, "laboratory_terminal");
        ShapedRecipe recipe = new ShapedRecipe(key, terminal);
        recipe.shape("IRI", "RCR", "IRI");
        recipe.setIngredient('I', Material.IRON_INGOT);
        recipe.setIngredient('R', Material.REDSTONE);
        recipe.setIngredient('C', Material.CRAFTING_TABLE);
        
        Bukkit.addRecipe(recipe);
    }
    
    private void registerAssemblerRecipe() {
        ItemStack assembler = NexoItems.itemFromId("assembler").build();
        if (assembler == null) return;
        
        NamespacedKey key = new NamespacedKey(plugin, "assembler");
        ShapedRecipe recipe = new ShapedRecipe(key, assembler);
        recipe.shape("IRI", "RSR", "IRI");
        recipe.setIngredient('I', Material.IRON_INGOT);
        recipe.setIngredient('R', Material.REDSTONE);
        recipe.setIngredient('S', Material.SMITHING_TABLE);
        
        Bukkit.addRecipe(recipe);
    }
    
    private void registerGeigerCounterRecipe() {
        ItemStack geiger = NexoItems.itemFromId("geiger_counter").build();
        if (geiger == null) return;
        
        NamespacedKey key = new NamespacedKey(plugin, "geiger_counter");
        ShapedRecipe recipe = new ShapedRecipe(key, geiger);
        recipe.shape(" R ", "ICI", " I ");
        recipe.setIngredient('I', Material.IRON_INGOT);
        recipe.setIngredient('R', Material.REDSTONE);
        recipe.setIngredient('C', Material.CLOCK);
        
        Bukkit.addRecipe(recipe);
    }
    
    private void registerBasicCapsuleRecipe() {
        ItemStack capsule = NexoItems.itemFromId("uranium_capsule").build();
        if (capsule == null) return;
        
        NamespacedKey key = new NamespacedKey(plugin, "basic_uranium_capsule");
        ShapedRecipe recipe = new ShapedRecipe(key, capsule);
        recipe.shape(" I ", "IGI", " I ");
        recipe.setIngredient('I', Material.IRON_INGOT);
        recipe.setIngredient('G', Material.GLASS);
        
        Bukkit.addRecipe(recipe);
    }
}