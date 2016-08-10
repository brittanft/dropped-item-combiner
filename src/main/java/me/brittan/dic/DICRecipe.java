package me.brittan.dic;

import java.util.LinkedList;

import org.bukkit.inventory.ItemStack;

public class DICRecipe {
	
	public DICRecipe(ItemStack result, LinkedList<ItemStack> ingredients, String permission) {
		this.result = result;
		this.ingredients = ingredients;
		this.permission = permission;
	}
	
	private ItemStack result;
	
	public ItemStack getResult() {
		return result;
	}
	
	private LinkedList<ItemStack> ingredients;
	
	public LinkedList<ItemStack> getIngredients() {
		return ingredients;
	}
	
	private String permission;
	
	public String getPermission() {
		return permission;
	}

}
