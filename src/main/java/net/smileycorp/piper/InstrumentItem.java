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
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import net.smileycorp.followme.common.FollowHandler;
import net.smileycorp.piper.capability.IInstrument;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class InstrumentItem extends Item {

	protected SoundEvent sound = null;
	protected int cooldown = 30;
	protected float radius = 5f;
	protected boolean shiny = false;
	protected final List<ResourceLocation> entitiesBuilder = new ArrayList<ResourceLocation>();
	protected final List<EntityType<?>> entities = new ArrayList<EntityType<?>>();

	private InstrumentItem(Properties props) {
		super(props);
	}

	@Override
	public UseAction getUseAnimation(ItemStack stack) {
		return UseAction.BOW;
	}

	@Override
	public boolean isFoil(ItemStack stack) {
		return shiny || stack.isEnchanted();
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
	   if (duration<=getUseDuration(stack)-20) {
		   if (!world.isClientSide) {
			   LazyOptional<IInstrument> optional = stack.getCapability(Piper.INSTRUMENT_CAPABILITY);
			   if (optional.isPresent()) {
				   IInstrument cap = optional.resolve().get();
				   if (cap.hasFollowers()) cap.removeAllFollowers();
				   else {
					   for (MobEntity entity : getFollowEntities(world, user)) {
						   if (FollowHandler.processInteraction(world, user, entity, Hand.MAIN_HAND)) cap.addFollower(entity);
					   }
				   }
			   }
			   if (user instanceof PlayerEntity) {
				   PlayerEntity player = (PlayerEntity) user;
				   player.getCooldowns().addCooldown(this, cooldown);
				   player.awardStat(Stats.ITEM_USED.get(this));
			   }
			   if (stack.isDamageableItem()) stack.hurtAndBreak(1, user, (e) -> e.broadcastBreakEvent(e.getUsedItemHand()));
			   PacketHandler.NETWORK_INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(
						()->(Chunk)world.getChunk(user.blockPosition())), new PacketHandler.InstrumentMessage(user, this));
		   }
	   }
   }

   public List<MobEntity> getFollowEntities(World world, LivingEntity user) {
	   AxisAlignedBB hitbox = new AxisAlignedBB(user.getX() - radius, user.getY() - radius, user.getZ() - radius, user.getX() + radius, user.getY() + radius, user.getZ() + radius);
	   return world.getEntitiesOfClass(MobEntity.class, hitbox, (e) -> entities.contains(e.getType()) && e!=user);
   }

   public int getCooldown() {
	   return cooldown;
   }

   public SoundEvent getSound() {
	   return sound;
   }

   public void buildEntities() {
	   for (ResourceLocation resource : entitiesBuilder) {
		   if (ForgeRegistries.ENTITIES.containsKey(resource)) entities.add(ForgeRegistries.ENTITIES.getValue(resource));
	   }
   }

   public static InstrumentItem fromJson(String name, JsonObject json) {
	   Properties props = new Properties().stacksTo(1).tab(ItemGroup.TAB_TOOLS);
	   if (json.has("durability")) props.durability(JSONUtils.getAsInt(json, "durability"));
	   if (json.has("rarity")) {
		   String value = JSONUtils.getAsString(json, "rarity").toUpperCase();
		   try {
			   Rarity rarity = Rarity.valueOf(value);
			   props.rarity(rarity);
		   } catch (Exception e) {
			   TextFormatting format = TextFormatting.getByName(value);
			   if (format != null) props.rarity(Rarity.create(value, format));
		   }
	   }
	   InstrumentItem item = new InstrumentItem(props);
	   if (json.has("sound")) item.sound = new SoundEvent(new ResourceLocation(JSONUtils.getAsString(json, "sound")));
	   if (json.has("cooldown")) item.cooldown = JSONUtils.getAsInt(json, "cooldown");
	   if (json.has("radius")) item.radius = JSONUtils.getAsFloat(json, "radius");
	   if (json.has("enchanted")) item.shiny = JSONUtils.getAsBoolean(json, "enchanted");
	   if (json.has("entities")) {
		   for (JsonElement element : JSONUtils.getAsJsonArray(json, "entities")) {
			   ResourceLocation resource = new ResourceLocation(element.getAsString());
			   item.entitiesBuilder.add(resource);
		   }
	   }
	   item.setRegistryName(ModDefinitions.getResource(name));
	   return item;
   }

}
