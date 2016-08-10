package me.brittan.dic;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class DICPlugin extends JavaPlugin implements Listener {
	
	public DICPlugin() {
		instance = this;
	}
	
	private static DICPlugin instance;
	
	public static DICPlugin getInstance() {
		return instance;
	}
	
	private LinkedList<DICRecipe> recipes;
	
	@Override
	public void onEnable() {
		PluginManager pm = Bukkit.getPluginManager();
		pm.registerEvents(this, this);
		
		recipes = new LinkedList<>();
		FileConfiguration config = getConfig(); int recipeCount = 0;
		while (config.contains("recipes." + recipeCount)) {
			LinkedList<ItemStack> ingredients = new LinkedList<>(); int ingredientCount = 0;
			while (config.contains("recipes." + recipeCount + ".ingredients." + ingredientCount)) {
				Material material = Material.getMaterial(config.getString("recipes." + recipeCount + ".ingredients." + ingredientCount + ".material"));
				int amount = config.getInt("recipes." + recipeCount + ".ingredients." + ingredientCount + ".amount");
				short data = (short) config.getInt("recipes." + recipeCount + ".ingredients." + ingredientCount + ".data");
				String displayName = config.getString("recipes." + recipeCount + ".ingredients." + ingredientCount + ".display-name").replaceAll("&", "" + ChatColor.COLOR_CHAR);
				List<String> lore = config.getStringList("recipes." + recipeCount + ".ingredients." + ingredientCount + ".lore").stream().map(s -> s.replaceAll("&", "" + ChatColor.COLOR_CHAR)).collect(Collectors.toCollection(LinkedList::new));
				
				ItemStack ingredient = new ItemStack(material, amount, data);
				ItemMeta meta = ingredient.getItemMeta();
				if (displayName != null)
					meta.setDisplayName(displayName);
				if (lore != null)
					meta.setLore(lore);
				ingredient.setItemMeta(meta);
				ingredients.add(ingredient);
				ingredientCount++;
			}
			
			Material material = Material.getMaterial(config.getString("recipes." + recipeCount + ".result.material"));
			int amount = config.getInt("recipes." + recipeCount + ".result.amount");
			short data = (short) config.getInt("recipes." + recipeCount + ".result.data");
			String displayName = config.getString("recipes." + recipeCount + ".result.display-name").replaceAll("&", "" + ChatColor.COLOR_CHAR);
			List<String> lore = config.getStringList("recipes." + recipeCount + ".result.lore").stream().map(s -> s.replaceAll("&", "" + ChatColor.COLOR_CHAR)).collect(Collectors.toCollection(LinkedList::new));
			
			ItemStack result = new ItemStack(material, amount, data);
			ItemMeta meta = result.getItemMeta();
			if (displayName != null)
				meta.setDisplayName(displayName);
			if (lore != null)
				meta.setLore(lore);
			result.setItemMeta(meta);
			
			String permission = config.contains("recipes." + recipeCount + ".permission") ? config.getString("recipes." + recipeCount + ".permission") : null;
			if (permission != null && pm.getPermission(permission) != null)
				pm.getPermissions().add(new Permission(permission));
			
			DICRecipe recipe = new DICRecipe(result, ingredients, permission);
			recipes.add(recipe);
			recipeCount++;
		}
	}
	
	@EventHandler
	public void onDropItem(PlayerDropItemEvent event) {
		LinkedList<Item> items = event.getItemDrop().getNearbyEntities(2D, 2D, 2D).stream().filter(e -> e.getType() == EntityType.DROPPED_ITEM).map(e -> (Item) e).collect(Collectors.toCollection(LinkedList::new));
		for (DICRecipe recipe : recipes) {
			LinkedList<Item> found = new LinkedList<>();
			for (ItemStack ingredient : recipe.getIngredients())
				items.stream().filter(i -> i.getItemStack().isSimilar(ingredient) && i.getItemStack().getAmount() >= ingredient.getAmount()).findAny().ifPresent(i -> found.add(i));
			
			if (found.size() == recipe.getIngredients().size()) {
				combine(event.getPlayer(), recipe, found);
				break;
			}
		}
	}
	
	public void combine(Player player, DICRecipe recipe, LinkedList<Item> found) {
		if (!player.hasPermission(recipe.getPermission()))
			return;
		
		for (Item item : found) {
			recipe.getIngredients().stream().filter(i -> i.isSimilar(item.getItemStack())).findAny().ifPresent(i -> {
				if (item.getItemStack().getAmount() > i.getAmount())
					item.getItemStack().setAmount(item.getItemStack().getAmount() - i.getAmount());
				else
					item.remove();
			});
		}
		
		player.getWorld().dropItem(player.getLocation(), recipe.getResult());
	}

}
