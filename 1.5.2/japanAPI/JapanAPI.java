package mods.japanAPI;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.registry.GameRegistry;
import mods.japanAPI.events.EntityItemPickupEventHook;
import mods.japanAPI.recipes.CommonRecipeHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.oredict.ShapedOreRecipe;

import java.util.*;
import java.util.logging.Level;

@Mod(modid = "JapanAPI",name = "JapanAPI 1.5.2",version = "0.0.10")
@NetworkMod(clientSideRequired = true,serverSideRequired = true)
public class JapanAPI
{

	public static Random RANDOM;

	public static boolean CONFIG_itemConversion;

	public static EntityItemPickupEventHook EVENT_entityItemPickupEventHook;

	/**
	 * レシピ（クラフト）の削除
	 * @param itemStacks リザルトアイテムリスト
	 */
	public static void DeleteCraftingRecipe(ItemStack... itemStacks)
    {
		List recipes = CraftingManager.getInstance().getRecipeList();

		for(ItemStack itemStack : itemStacks)
        {
			if(itemStack == null)
                continue;
			for(Iterator i = recipes.listIterator(); i.hasNext();)
            {
				IRecipe recipe = (IRecipe)i.next();
				ItemStack is = recipe.getRecipeOutput();

				if(is != null && is.isItemEqual(itemStack))
                {
					i.remove();
				}
			}
		}
	}

	/**
	 * レシピ（精錬）の削除
	 * @param itemStacks リザルトアイテムリスト
	 */
	public static void DeleteSmeltingRecipe(ItemStack... itemStacks)
    {
		Map<List<Integer>, ItemStack> recipesMeta = FurnaceRecipes.smelting().getMetaSmeltingList();
		Map<Integer, ItemStack> recipes = FurnaceRecipes.smelting().getSmeltingList();

		for(ItemStack itemStack : itemStacks)
        {
			if(itemStack == null)
                continue;
			if(itemStack.isItemDamaged() && recipesMeta.containsKey(Arrays.asList(itemStack.itemID, itemStack.getItemDamage())))
            {
					recipesMeta.remove(Arrays.asList(itemStack.itemID, itemStack.getItemDamage()));
			}
			if(!itemStack.isItemDamaged() && recipes.containsKey(itemStack.itemID))
            {
				recipes.remove(itemStack.itemID);
			}
		}
	}

	@Mod.PreInit
	public void preInit(FMLPreInitializationEvent event)
    {
		Configuration cfg = new Configuration(event.getSuggestedConfigurationFile());
		EVENT_entityItemPickupEventHook = new EntityItemPickupEventHook();
		try
		{
			cfg.load();
			CONFIG_itemConversion = cfg.get("EventHook", "DropItem AutoConversion", true).getBoolean(true);

		} catch (Exception e) {
			FMLLog.log(Level.SEVERE, e, "Error Message");

		} finally {
			cfg.save();
		}

		RANDOM = new Random(152009);
		if(CONFIG_itemConversion) {
			MinecraftForge.EVENT_BUS.register(EVENT_entityItemPickupEventHook);
		}


	}

	@Mod.Init
	public void Init(FMLInitializationEvent event)
    {
//		ITEM_autoConversionSymbol = new AutoConversionSymbolItem(AutoConversionSymbolItem.itemID).setCreativeTab(CreativeTabs.tabTools);
//		GameRegistry.registerItem(ITEM_autoConversionSymbol, "AutoConversionSymbol");
//		LanguageRegistry.addName(ITEM_autoConversionSymbol, "AutoConversion Symbol");
//		LanguageRegistry.instance().addNameForObject(ITEM_autoConversionSymbol, "ja_JP",
//				"\u81EA\u52D5\u5909\u63DB\u30B7\u30F3\u30DC\u30EB");

	}

	@Mod.PostInit
	public void postInit(FMLPostInitializationEvent event)
    {
		CraftRecipeConversion();
	}

	public static void CraftRecipeConversion()
    {
		ArrayList<ShapedOreRecipe> oreRecipe = new ArrayList<ShapedOreRecipe>();
		ArrayList<ItemStack> remItemStack = new ArrayList<ItemStack>();

		List recipes = CraftingManager.getInstance().getRecipeList();
		for(Iterator i = recipes.listIterator(); i.hasNext();)
        {
			IRecipe recipe = (IRecipe)i.next();
			if(recipe instanceof ShapedRecipes)
            {
				ShapedOreRecipe convRecipe = CommonRecipeHandler.ConversionShapedRecipeV2((ShapedRecipes)recipe);
				if(convRecipe != null)
                {
					oreRecipe.add(convRecipe);
					remItemStack.add(recipe.getRecipeOutput());
				}
			}
		}

//		DeleteCraftingRecipe(remItemStack.toArray(new ItemStack[remItemStack.size()]));

		for(ShapedOreRecipe recipe : oreRecipe.toArray(new ShapedOreRecipe[oreRecipe.size()]))
        {
			GameRegistry.addRecipe(recipe);
		}
	}
}
