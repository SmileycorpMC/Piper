package net.smileycorp.piper;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.Stats;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import net.smileycorp.followme.common.FollowHandler;
import net.smileycorp.piper.capability.IInstrument;

public class Instrument extends Item {

	protected SoundEvent sound = null;
	protected int cooldown = 30;
	protected float radius = 5f;
	protected boolean shiny = false;
	protected final List<EntityType<?>> entities = new ArrayList<EntityType<?>>();
	protected final List<ResourceLocation> entitiesBuilder = new ArrayList<ResourceLocation>();
	protected final ResourceLocation name;

	private Instrument(Properties props, ResourceLocation name) {
		super(props);
		this.name = name;
	}

	@Override
	public UseAnim getUseAnimation(ItemStack stack) {
		return UseAnim.BOW;
	}

	@Override
	public boolean isFoil(ItemStack stack) {
		return shiny || stack.isEnchanted();
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player user, InteractionHand hand) {
		ItemStack stack = user.getItemInHand(hand);
		user.startUsingItem(hand);
		return InteractionResultHolder.success(stack);
	}

	@Override
	public int getUseDuration(ItemStack stack) {
		return 120000;
	}

	@Override
	public void releaseUsing(ItemStack stack, Level level, LivingEntity user, int duration) {
		if (duration<=getUseDuration(stack)-20) {
			if (!level.isClientSide) {
				LazyOptional<IInstrument> optional = stack.getCapability(Piper.INSTRUMENT_CAPABILITY);
				if (optional.isPresent()) {
					IInstrument cap = optional.resolve().get();
					if (cap.hasFollowers()) cap.removeAllFollowers();
					else {
						for (Mob entity : getFollowEntities(level, user)) {
							if (FollowHandler.processInteraction(level, user, entity, InteractionHand.MAIN_HAND)) cap.addFollower(entity);
						}
					}
				}
				if (user instanceof Player) {
					Player player = (Player) user;
					player.getCooldowns().addCooldown(this, cooldown);
					player.awardStat(Stats.ITEM_USED.get(this));
				}
				if (stack.isDamageableItem()) stack.hurtAndBreak(1, user, (e) -> e.broadcastBreakEvent(e.getUsedItemHand()));
				PacketHandler.NETWORK_INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(
						()->(LevelChunk)level.getChunk(user.blockPosition())),new PacketHandler.InstrumentMessage(user, this));
			}
		}
	}

	public List<Mob> getFollowEntities(Level level, LivingEntity user) {
		AABB hitbox = new AABB(user.getX() - radius, user.getY() - radius, user.getZ() - radius, user.getX() + radius, user.getY() + radius, user.getZ() + radius);
		return level.getEntitiesOfClass(Mob.class, hitbox, (e) -> entities.contains(e.getType()) && e!=user);
	}

	public int getCooldown() {
		return cooldown;
	}

	public SoundEvent getSound() {
		return sound;
	}

	public void buildEntities() {
		entities.clear();
		for (ResourceLocation resource : entitiesBuilder) {
			if (ForgeRegistries.ENTITY_TYPES.containsKey(resource)) entities.add(ForgeRegistries.ENTITY_TYPES.getValue(resource));
		}
	}

	public ResourceLocation getInstrumentName() {
		return name;
	}

	public static Instrument fromJson(String name, JsonObject json) {
		Properties props = new Properties().stacksTo(1);
		if (json.has("durability")) props.durability(GsonHelper.getAsInt(json, "durability"));
		if (json.has("rarity")) {
			String value = GsonHelper.getAsString(json, "rarity").toUpperCase();
			try {
				Rarity rarity = Rarity.valueOf(value);
				props.rarity(rarity);
			} catch (Exception e) {
				ChatFormatting format = ChatFormatting.getByName(value);
				if (format != null) props.rarity(Rarity.create(value, format));
			}
		}
		Instrument item = new Instrument(props, ModDefinitions.getResource(name));
		if (json.has("sound")) item.sound = SoundEvent.m_262824_(new ResourceLocation(GsonHelper.getAsString(json, "sound")));
		if (json.has("cooldown")) item.cooldown = GsonHelper.getAsInt(json, "cooldown");
		if (json.has("radius")) item.radius = GsonHelper.getAsFloat(json, "radius");
		if (json.has("enchanted")) item.shiny = GsonHelper.getAsBoolean(json, "enchanted");
		if (json.has("entities")) {
			for (JsonElement element : GsonHelper.getAsJsonArray(json, "entities")) {
				ResourceLocation resource = new ResourceLocation(element.getAsString());
				item.entitiesBuilder.add(resource);
			}
		}
		return item;
	}

}
