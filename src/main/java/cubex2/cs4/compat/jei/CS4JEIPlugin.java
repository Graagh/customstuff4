package cubex2.cs4.compat.jei;

import cubex2.cs4.plugins.jei.JEICompatRegistry;
import cubex2.cs4.plugins.jei.JEIMachineRecipe;
import cubex2.cs4.plugins.vanilla.MachineRecipeImpl;
import cubex2.cs4.plugins.vanilla.crafting.MachineManager;
import mezz.jei.api.BlankModPlugin;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.recipe.IRecipeWrapperFactory;

@JEIPlugin
public class CS4JEIPlugin extends BlankModPlugin
{
    @SuppressWarnings("unchecked")
    @Override
    public void register(IModRegistry registry)
    {
        IJeiHelpers jeiHelpers = registry.getJeiHelpers();
        IRecipeWrapperFactory<MachineRecipeImpl> factory = new MachineRecipeWrapperFactory(jeiHelpers);

        for (JEIMachineRecipe recipe : JEICompatRegistry.machineRecipes)
        {
            registry.handleRecipes((Class<MachineRecipeImpl>) JEICompatRegistry.getMachineRecipeClass(recipe.recipeList), factory, recipe.recipeList.toString());
            registry.addRecipeCategories(new MachineRecipeCategory(recipe, jeiHelpers.getGuiHelper()));
            registry.addRecipes(MachineManager.getRecipes(recipe.recipeList), recipe.recipeList.toString());

            if (recipe.icon != null)
            {
                registry.addRecipeCategoryCraftingItem(recipe.icon.getItemStack(), recipe.recipeList.toString());
            }
        }
    }
}