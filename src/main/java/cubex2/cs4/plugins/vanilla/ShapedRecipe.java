package cubex2.cs4.plugins.vanilla;

import com.google.common.collect.Maps;
import cubex2.cs4.api.ContentHelper;
import cubex2.cs4.api.InitPhase;
import cubex2.cs4.api.RecipeInput;
import cubex2.cs4.api.WrappedItemStack;
import cubex2.cs4.data.SimpleContent;
import cubex2.cs4.plugins.jei.JEICompatRegistry;
import cubex2.cs4.plugins.vanilla.crafting.CraftingManagerCS4;
import cubex2.cs4.util.ItemHelper;
import cubex2.cs4.util.ReflectionHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class ShapedRecipe extends SimpleContent
{
    String[] shape;
    Map<Character, RecipeInput> items = Maps.newHashMap();
    WrappedItemStack result;
    boolean mirrored = true;
    boolean remove = false;
    ResourceLocation recipeList = new ResourceLocation("minecraft", "vanilla");
    Map<Character, Integer> damage = Maps.newHashMap();

    @Override
    protected void doInit(InitPhase phase, ContentHelper helper)
    {
        if (remove)
        {
            removeRecipe(CraftingManagerCS4.getRecipes(recipeList));
        } else
        {
            addRecipe();
        }
    }

    @Override
    protected boolean isReady()
    {
        if (result != null && !result.isItemLoaded())
            return false;

        return items.values().stream().allMatch(input -> input.isOreClass() || (input.isItemStack() && input.getStack().isItemLoaded()));
    }

    private void addRecipe()
    {
        Class<DamageableShapedOreRecipe> recipeClass = JEICompatRegistry.getShapedCraftingRecipeClass(recipeList);
        Constructor<DamageableShapedOreRecipe> constructor = ReflectionHelper.getConstructor(recipeClass, int[].class, ItemStack.class, Object[].class);
        DamageableShapedOreRecipe recipe = ReflectionHelper.newInstance(constructor, createDamageAmounts(), result.getItemStack(), getInputForRecipe());
        recipe.setMirrored(mirrored);

        CraftingManagerCS4.addRecipe(recipeList, recipe);
    }

    boolean removeRecipe(Collection<IRecipe> from)
    {
        if (items.size() == 0)
        {
            return removeWithResult(from);
        } else if (result == null)
        {
            return removeWithInput(from);
        } else
        {
            return removeWithBoth(from);
        }
    }

    private boolean removeWithResult(Collection<IRecipe> from)
    {
        return from.removeIf(this::matchesOutput);
    }

    private boolean removeWithInput(Collection<IRecipe> from)
    {
        List<IRecipe> recipes = from.stream()
                                    .filter(this::matchesInput)
                                    .collect(Collectors.toList());

        return from.removeAll(recipes);
    }

    private boolean removeWithBoth(Collection<IRecipe> from)
    {
        List<IRecipe> recipes = from.stream()
                                    .filter(this::matchesOutput)
                                    .filter(this::matchesInput)
                                    .collect(Collectors.toList());

        return from.removeAll(recipes);
    }

    private boolean matchesOutput(IRecipe recipe)
    {
        return OreDictionary.itemMatches(recipe.getRecipeOutput(), result.getItemStack(), false);
    }

    private boolean matchesInput(IRecipe recipe)
    {
        if (recipe instanceof ShapedRecipes)
        {
            return matchesInput((ShapedRecipes) recipe);
        } else if (recipe instanceof ShapedOreRecipe)
        {
            return matchesInput((ShapedOreRecipe) recipe);
        }

        return false;
    }

    private boolean matchesInput(ShapedRecipes recipe)
    {
        if (isOreRecipe())
            return false;
        if (recipe.recipeWidth != getRecipeWidth())
            return false;
        if (recipe.recipeHeight != getRecipeHeight())
            return false;

        return isSameInputs(recipe.recipeItems);
    }

    private boolean matchesInput(ShapedOreRecipe recipe)
    {
        if (recipe.getWidth() != getRecipeWidth())
            return false;
        if (recipe.getHeight() != getRecipeHeight())
            return false;

        return isSameInputs(recipe.getInput());
    }

    private boolean isSameInputs(Object[] targetInput)
    {
        Object[] sourceInput = getRecipeInput();

        for (int i = 0; i < targetInput.length; i++)
        {
            Object target = targetInput[i];
            Object source = sourceInput[i];

            if (!ItemHelper.isSameRecipeInput(target, source))
                return false;
        }
        return true;
    }

    private boolean isOreRecipe()
    {
        return items.values().stream().anyMatch(RecipeInput::isOreClass);
    }

    private int getRecipeWidth()
    {
        return shape[0].length();
    }

    private int getRecipeHeight()
    {
        return shape.length;
    }

    Object[] getInputForRecipe()
    {
        Object[] result = new Object[shape.length + items.size() * 2];

        System.arraycopy(shape, 0, result, 0, shape.length);

        int i = shape.length;
        for (Map.Entry<Character, RecipeInput> entry : items.entrySet())
        {
            RecipeInput input = entry.getValue();

            result[i] = entry.getKey();
            result[i + 1] = input.isOreClass() ? input.getOreClass() : input.getStack().getItemStack();

            i += 2;
        }

        return result;
    }

    /**
     * Gets recipe input without the chars for the shape.
     */
    Object[] getRecipeInput()
    {
        Object[] result = new Object[getRecipeWidth() * getRecipeHeight()];

        for (int row = 0; row < shape.length; row++)
        {
            for (int col = 0; col < shape[0].length(); col++)
            {
                RecipeInput input = items.get(shape[row].charAt(col));

                int index = col + row * shape[0].length();

                if (input != null)
                {
                    result[index] = input.isOreClass() ? OreDictionary.getOres(input.getOreClass().getOreName()) : input.getStack().getItemStack();
                } else
                {
                    result[index] = ItemStack.EMPTY;
                }
            }
        }

        return result;
    }

    int[] createDamageAmounts()
    {
        int[] result = new int[getRecipeWidth() * getRecipeHeight()];

        for (int row = 0; row < shape.length; row++)
        {
            for (int col = 0; col < shape[0].length(); col++)
            {
                int amount = damage.getOrDefault(shape[row].charAt(col), 0);

                int index = col + row * shape[0].length();

                result[index] = amount;
            }
        }

        return result;
    }

}
