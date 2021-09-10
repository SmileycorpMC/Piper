package net.smileycorp.piper;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.item.UseAction;
import net.minecraft.stats.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;
import net.smileycorp.followme.common.FollowHandler;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class InstrumentItem extends Item {

	protected SoundEvent sound = null;
	protected final List<EntityType<?>> entities = new ArrayList<EntityType<?>>();

	private InstrumentItem(Properties props) {
		super(props);
	}

	@Override
	public UseAction getUseAnimation(ItemStack stack) {
		return UseAction.BOW;
	}

	@Override
	public ActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack stack = user.getItemInHand(hand);
		user.startUsingItem(hand);
		return ActionResult.success(stack);
	}

	@Override
	public int getUseDuration(ItemStack stack) {
		return 120000;
	}

   @Override
   public void releaseUsing(ItemStack stack, World world, LivingEntity user, int duration) {
	   if (!world.isClientSide) {
		   for (MobEntity entity : user.level.getEntitiesOfClass(MobEntity.class, user.getBoundingBox().inflate(5), (e) -> entities.contains(e.getType()))) {
			   FollowHandler.processInteraction(world, user, entity, Hand.MAIN_HAND);
		   }
		   if (user instanceof PlayerEntity) ((PlayerEntity) user).awardStat(Stats.ITEM_USED.get(this));
	   } else if (sound != null) {
		   world.playLocalSound(user.getX(), user.getY(), user.getZ(), sound, user.getSoundSource(), 0.5f, world.random.nextFloat(), true);
	   }
   }

   public static InstrumentItem fromJson(String name, JsonObject json) {
	Properties props = new Properties().stacksTo(1).tab(ItemGroup.TAB_TOOLS);
	if (json.has("durability")) props.durability(JSONUtils.getAsInt(json, "durability"));
	if (json.has("rarity")) {
		Rarity rarity = Rarity.valueOf(JSONUtils.getAsString(json, "rarity"));
		if (rarity!=null)props.rarity(rarity);
	}
	InstrumentItem item = new InstrumentItem(props);
	if (json.has("sound")) item.sound = new SoundEvent(new ResourceLocation(JSONUtils.getAsString(json, "sound")));
	if (json.has("entities")) {
		for (JsonElement element : JSONUtils.getAsJsonArray(json, "entities")) {
			ResourceLocation resource = new ResourceLocation(element.getAsString());
			if (ForgeRegistries.ENTITIES.containsKey(resource)) item.entities.add(ForgeRegistries.ENTITIES.getValue(resource));
		}
	}
	item.setRegistryName(ModDefinitions.getResource(name));
	return item;
   }

}
