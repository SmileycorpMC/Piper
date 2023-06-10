package net.smileycorp.piper;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.RegistryObject;
import net.smileycorp.followme.common.FollowHandler;
import net.smileycorp.piper.capability.IInstrument;
import net.smileycorp.piper.capability.IMusician;

@EventBusSubscriber(modid = Constants.MODID, bus = EventBusSubscriber.Bus.MOD)
public class EventListener {

	@SubscribeEvent
	public void registerCapabilities(RegisterCapabilitiesEvent event) {
		event.register(IMusician.class);
		event.register(IInstrument.class);
	}

	@SubscribeEvent
	public void attachEntityCapabilities(AttachCapabilitiesEvent<Entity> event) {
		Entity entity = event.getObject();
		if (entity instanceof Mob) {
			event.addCapability(Constants.loc("musician"), new IMusician.Provider((Mob)entity));
		}
	}

	@SubscribeEvent
	public void attachStackCapabilities(AttachCapabilitiesEvent<ItemStack> event) {
		ItemStack stack = event.getObject();
		if (stack.getItem() instanceof Instrument) {
			event.addCapability(Constants.loc("instrument"), new IInstrument.Provider());
		}
	}

	@SubscribeEvent
	public static void addCreative(BuildCreativeModeTabContentsEvent event) {
		if (event.getTab() == BuiltInRegistries.CREATIVE_MODE_TAB.get(CreativeModeTabs.TOOLS_AND_UTILITIES))
			for (RegistryObject<Item> item : InstrumentRegistry.ITEMS.values()) event.accept(item.get());
	}

	@SubscribeEvent
	public void entityTick(LivingTickEvent event) {
		LivingEntity entity = event.getEntity();
		Level level = entity.level();
		LazyOptional<IMusician> optional = entity.getCapability(Piper.MUSICIAN_CAPABILITY);
		if (optional.isPresent()) {
			if (!level.isClientSide) {
				for (ItemStack stack : entity.getHandSlots()) {
					if (stack.getItem() instanceof Instrument) {
						Instrument item = ((Instrument) stack.getItem());
						if (entity.tickCount % (item.getCooldown()+20) == 0) {
							IMusician cap = optional.resolve().get();
							LivingEntity target = ((Mob) entity).getTarget();
							boolean playInstrument = false;
							for (Mob follower : item.getFollowEntities(level, entity)) {
								if (!cap.isLeading(follower)) {
									follower.setTarget(target);
									cap.addFollower(follower);
									playInstrument = FollowHandler.processInteraction(level, entity, follower, InteractionHand.MAIN_HAND, false) || playInstrument ;
								}
							}
							if (playInstrument) {
								PacketHandler.NETWORK_INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(
										()->(LevelChunk)level.getChunk(entity.blockPosition())),new PacketHandler.InstrumentMessage(entity, item));
							}
							if (cap.getTarget()!=target) cap.setTarget(target);
						}
					}
				}
			}
		}
	}

}
