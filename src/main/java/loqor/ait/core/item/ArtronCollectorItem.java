package loqor.ait.core.item;

import loqor.ait.core.AITDataComponents;
import loqor.ait.core.blockentities.ExteriorBlockEntity;
import loqor.ait.core.blockentities.ConsoleBlockEntity;
import loqor.ait.tardis.link.LinkableBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.client.item.TooltipType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class ArtronCollectorItem extends Item {
	public static final String AU_LEVEL = "au_level";
	public static final String UUID_KEY = "uuid";
	public static final Integer COLLECTOR_MAX_FUEL = 1500;

	public ArtronCollectorItem(Settings settings) {
		super(settings);
	}

	@Override
	public ItemStack getDefaultStack() {
		ItemStack stack = new ItemStack(this);
		stack.set(AITDataComponents.AU_LEVEL, 0.0f);
		return super.getDefaultStack();
	}

	public static UUID getUuid(ItemStack stack) {
		if (stack.contains(AITDataComponents.UUID_KEY)) return UUID.fromString(stack.get(AITDataComponents.UUID_KEY));
		UUID uuid = UUID.randomUUID();
		stack.set(AITDataComponents.UUID_KEY, uuid.toString());
		return uuid;
	}

	public static float getFuel(ItemStack stack) {
		if (stack.contains(AITDataComponents.AU_LEVEL)) return stack.get(AITDataComponents.AU_LEVEL);
		stack.set(AITDataComponents.AU_LEVEL, 0.0f);
		return 0f;
	}

	public static float addFuel(ItemStack stack, float fuel) {
		float currentFuel = getFuel(stack);
		stack.set(AITDataComponents.AU_LEVEL, getFuel(stack) <= COLLECTOR_MAX_FUEL ? getFuel(stack) + fuel : COLLECTOR_MAX_FUEL);
		if (getFuel(stack) > COLLECTOR_MAX_FUEL) stack.set(AITDataComponents.AU_LEVEL, (float)COLLECTOR_MAX_FUEL);
		if (getFuel(stack) == COLLECTOR_MAX_FUEL)
			return fuel - (COLLECTOR_MAX_FUEL - currentFuel);
		return 0;
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext context) {

		PlayerEntity player = context.getPlayer();
		World world = context.getWorld();
		BlockPos clickedPos = context.getBlockPos();
		Block block = world.getBlockState(clickedPos).getBlock();
		ItemStack cellItemStack = context.getStack();
		if (world.isClient()) return ActionResult.SUCCESS;

		if (player.isSneaking()) {
			if (world.getBlockEntity(clickedPos) instanceof LinkableBlockEntity exterior) {
				if (exterior.findTardis().isEmpty())
					return ActionResult.FAIL;
				double residual = exterior.findTardis().get().addFuel(cellItemStack.getOrDefault(AITDataComponents.AU_LEVEL, 0f));
				cellItemStack.set(AITDataComponents.AU_LEVEL, (float) residual);
				return ActionResult.CONSUME;
			}
			return ActionResult.FAIL;
		}

		return ActionResult.FAIL;
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.add(Text.literal(stack.getOrDefault(AITDataComponents.AU_LEVEL, 0f) + "au / " + COLLECTOR_MAX_FUEL + ".0au").formatted(Formatting.BLUE));
	}
}
