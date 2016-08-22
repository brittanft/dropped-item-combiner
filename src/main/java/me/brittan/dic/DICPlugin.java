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

/**
 * The Dropped Item Combiner Plugin.
 * @author Brittan Thomas
 */
public class DICPlugin extends JavaPlugin implements Listener {
	
	/**
	 * Instantiates a new DIC plugin.
	 */
	public DICPlugin() {
		instance = this;
	}
	
	/**
	 * The singleton instance.
	 */
	private static DICPlugin instance;
	
	/**
	 * Gets the single instance of DICPlugin.
	 *
	 * @return single instance of DICPlugin
	 */
	public static DICPlugin getInstance() {
		return instance;
	}
	
	/**
	 * The list of recipes loaded from the configuration file.
	 */
	private LinkedList<DICRecipe> recipes;
	
	/* (non-Javadoc)
	 * @see org.bukkit.plugin.java.JavaPlugin#onEnable()
	 */
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
			
			String message = config.contains("recipes." + recipeCount + ".message") ? config.getString("recipes." + recipeCount + ".message") : null;
			if (message != null)
				message = message.replaceAll("&", "" + ChatColor.COLOR_CHAR);
			
			String unsuccessfulMessage = config.contains("recipes." + recipeCount + ".unsuccessfulMessage") ? config.getString("recipes." + recipeCount + ".unsuccessfulMessage") : null;
			if (unsuccessfulMessage != null)
				unsuccessfulMessage = unsuccessfulMessage.replaceAll("&", "" + ChatColor.COLOR_CHAR);
			
			DICRecipe recipe = new DICRecipe(result, ingredients, permission, message, unsuccessfulMessage);
			recipes.add(recipe);
			recipeCount++;
		}
	}
	
	/**
	 * On player drop item event.
	 *
	 * @param event the triggered event
	 */
	@EventHandler
	public void onDropItem(PlayerDropItemEvent event) {
		/* Gets a list of nearby item entities. */
		LinkedList<Item> items = event.getItemDrop().getNearbyEntities(2D, 2D, 2D).stream().filter(e -> e.getType() == EntityType.DROPPED_ITEM).map(e -> (Item) e).collect(Collectors.toCollection(LinkedList::new));
		
		/* Parses through all loaded recipes. */
		for (DICRecipe recipe : recipes) {
			LinkedList<Item> found = new LinkedList<>();
			/* Parses through ingredients, if an item entity exists with a similar item stack, then it adds the item entity to the found list. */
			for (ItemStack ingredient : recipe.getIngredients())
				items.stream().filter(i -> i.getItemStack().isSimilar(ingredient) && i.getItemStack().getAmount() >= ingredient.getAmount()).findAny().ifPresent(i -> found.add(i));
			
			/* If the amount of found items equals the amount of ingredients, then there are sufficient items to be. */
			if (found.size() == recipe.getIngredients().size()) {
				combine(event.getPlayer(), recipe, found);
				break;
			}
		}
	}
	
	/**
	 * Combines the item entities into the recipe result.
	 *
	 * @param player  the player combining the objects.
	 * @param recipe  the recipe being performed
	 * @param found   the list of item entities found within the general proximity
	 */
	public void combine(Player player, DICRecipe recipe, LinkedList<Item> found) {
		/* Checks for the required permission, then continues. */
		if (recipe.getPermission() != null && !player.hasPermission(recipe.getPermission())) {
			if (recipe.getUnsuccessfulMessage() != null)
				player.sendMessage(recipe.getUnsuccessfulMessage());
			return;
		}
		
		/* Parses through all found item entities and removes the amount required to combine the items. */
		for (Item item : found) {
			recipe.getIngredients().stream().filter(i -> i.isSimilar(item.getItemStack())).findAny().ifPresent(i -> {
				if (item.getItemStack().getAmount() > i.getAmount())
					item.getItemStack().setAmount(item.getItemStack().getAmount() - i.getAmount());
				else
					item.remove();
			});
		}
		
		if (recipe.getMessage() != null)
			player.sendMessage(recipe.getMessage());
		
		/* Drops the resulting item onto the player. */
		player.getWorld().dropItem(player.getLocation(), recipe.getResult());
	}

}
