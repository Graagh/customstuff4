package cubex2.cs4.plugins.vanilla.item;

import cubex2.cs4.plugins.vanilla.ContentItemAxe;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import java.util.Arrays;
import java.util.List;

public class ItemAxe extends net.minecraft.item.ItemAxe implements ItemTool
{
    private final ContentItemAxe content;

    public ItemAxe(ToolMaterial material, ContentItemAxe content)
    {
        super(material);
        this.content = content;
    }

    @Override
    public void setDamage(float damage)
    {
        damageVsEntity = damage;
    }

    @Override
    public void setAttackSpeed(float attackSpeed)
    {
        this.attackSpeed = attackSpeed;
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced)
    {
        tooltip.addAll(Arrays.asList(content.information));
    }
}
