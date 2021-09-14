package net.smileycorp.piper;


import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.fmllegacy.network.PacketDistributor;
import net.minecraftforge.registries.IForgeRegistry;
import net.smileycorp.followme.common.FollowHandler;
import net.smileycorp.piper.capability.IInstrument;
import net.smileycorp.piper.capability.IMusician;

@Mod(value = ModDefinitions.MODID)
@Mod.EventBusSubscriber(modid = ModDefinitions.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Piper {

	private static Logger logger = LogManager.getLogger(ModDefinitions.NAME);

	protected static final Map<String, Instrument> ITEMS = new HashMap<String, Instrument>();

	@CapabilityInject(IMusician.class)
	public static Capability<IMusician> MUSICIAN_CAPABILITY = null;

	@CapabilityInject(IInstrument.class)
	public static Capability<IInstrument> INSTRUMENT_CAPABILITY = null;

	@SubscribeEvent
	public static void onModConstruction(FMLConstructModEvent event) {
		ItemLoader.loadFiles();
		PacketHandler.initPackets();
		MinecraftForge.EVENT_BUS.register(new Piper());
	}

	
	@SubscribeEvent
	@SuppressWarnings("removal")
	public static void onModConstruction(FMLCommonSetupEvent event) {
		CapabilityManager.INSTANCE.register(IMusician.class);
		CapabilityManager.INSTANCE.register(IInstrument.class);
	}

	@SubscribeEvent
	public static void registerItems(Register<Item> event) {
		IForgeRegistry<Item> registry = event.getRegistry();
		for (Item item : ITEMS.values()) registry.register(item);
	}

	@SubscribeEvent
	public void attachEntityCapabilities(AttachCapabilitiesEvent<Entity> event) {
		Entity entity = event.getObject();
		if (entity instanceof Mob) {
			event.addCapability(ModDefinitions.getResource("musician"), new IMusician.Provider((Mob)entity));
		}
	}

	@SubscribeEvent
	public void attachStackCapabilities(AttachCapabilitiesEvent<ItemStack> event) {
		ItemStack stack = event.getObject();
		if (stack.getItem() instanceof Instrument) {
			event.addCapability(ModDefinitions.getResource("instrument"), new IInstrument.Provider());
		}
	}

	@SubscribeEvent
	public void entityTick(LivingUpdateEvent event) {
		LivingEntity entity = event.getEntityLiving();
		Level level = entity.level;
		LazyOptional<IMusician> optional = entity.getCapability(MUSICIAN_CAPABILITY);
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
									playInstrument = FollowHandler.processInteraction(level, entity, follower, InteractionHand.MAIN_HAND) || playInstrument ;
								}
							}
							if (playInstrument) {
								PacketHandler.NETWORK_INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(
										()->(LevelChunk)level.getChunk(entity.blockPosition())),new PacketHandler.InstrumentMessage((Mob) entity, item));
							}
							if (cap.getTarget()!=target) cap.setTarget(target);
						}
					}
				}
			}
		}
	}

	public static void logInfo(Object message) {
		logger.info(message);
	}

	public static void logError(Object message, Exception e) {
		logger.error(message);
		e.printStackTrace();
	}

}
