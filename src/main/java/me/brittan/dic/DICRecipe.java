package me.brittan.dic;

import java.util.LinkedList;

import org.bukkit.inventory.ItemStack;

/**
 * The Recipe class for item combining.
 * @author Brittan Thomas
 */
public class DICRecipe {
	
	/**
	 * Instantiates a new DIC recipe.
	 *
	 * @param result the result
	 * @param ingredients the ingredients
	 * @param permission the permission
	 * @param message the message
	 * @param unsuccessfulMessage the unsuccessful message
	 */
	public DICRecipe(ItemStack result, LinkedList<ItemStack> ingredients, String permission, String message, String unsuccessfulMessage) {
		this.result = result;
		this.ingredients = ingredients;
		this.permission = permission;
		this.message = message;
		this.unsuccessfulMessage = unsuccessfulMessage;
	}
	
	/**
	 * The resulting item from the recipe.
	 */
	private ItemStack result;
	
	/**
	 * Gets the resulting item from the recipe.
	 *
	 * @return the resulting item from the recipe
	 */
	public ItemStack getResult() {
		return result;
	}
	
	/**
	 * The ingredients for the recipe.
	 */
	private LinkedList<ItemStack> ingredients;
	
	/**
	 * Gets the ingredients for the recipe.
	 *
	 * @return the ingredients for the recipe
	 */
	public LinkedList<ItemStack> getIngredients() {
		return ingredients;
	}
	
	/**
	 * The permission required to complete the recipe.
	 */
	private String permission;
	
	/**
	 * Gets the permission required to complete the recipe.
	 *
	 * @return the permission required to complete the recipe
	 */
	public String getPermission() {
		return permission;
	}
	
	/**
	 * The message triggered upon combination completion.
	 */
	private String message;
	
	/**
	 * Gets the message triggered upon combination completion.
	 *
	 * @return the message triggered upon combination completion
	 */
	public String getMessage() {
		return message;
	}
	
	/**
	 * The message triggered upon an unsuccessful combination.
	 */
	private String unsuccessfulMessage;
	
	/**
	 * Gets the message triggered upon an unsuccessful combination.
	 *
	 * @return the message triggered upon an unsuccessful combination
	 */
	public String getUnsuccessfulMessage() {
		return unsuccessfulMessage;
	}

}
