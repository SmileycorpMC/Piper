package net.smileycorp.piper;


import java.util.HashMap;
import java.util.Map;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
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
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.registries.IForgeRegistry;
import net.smileycorp.followme.common.FollowHandler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(value = ModDefinitions.MODID)
@Mod.EventBusSubscriber(modid = ModDefinitions.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Piper {

	private static Logger logger = LogManager.getLogger(ModDefinitions.NAME);

	protected static final Map<String, InstrumentItem> ITEMS = new HashMap<String, InstrumentItem>();

	@CapabilityInject(IMusician.class)
	public static Capability<IMusician> MUSICIAN_CAPABILITY = null;

	@CapabilityInject(IInstrument.class)
	public static Capability<IInstrument> INSTRUMENT_CAPABILITY = null;

	@SubscribeEvent
	public static void modConstruction(FMLConstructModEvent event) {
		ItemLoader.loadFiles();
		PacketHandler.initPackets();
		MinecraftForge.EVENT_BUS.register(new Piper());
	}

	@SubscribeEvent
	public static void modSetup(FMLCommonSetupEvent event) {
		CapabilityManager.INSTANCE.register(IMusician.class, new IMusician.Storage(), () -> new IMusician.Implementation());
		CapabilityManager.INSTANCE.register(IInstrument.class, new IInstrument.Storage(), () -> new IInstrument.Implementation());
	}

	@SubscribeEvent
	public static void modLoadEnd(FMLLoadCompleteEvent event) {
		for (InstrumentItem item : ITEMS.values()) item.buildEntities();
	}


	@SubscribeEvent
	public static void registerItems(Register<Item> event) {
		IForgeRegistry<Item> registry = event.getRegistry();
		for (Item item : ITEMS.values()) registry.register(item);
	}

	@SubscribeEvent
	public void attachEntityCapabilities(AttachCapabilitiesEvent<Entity> event) {
		Entity entity = event.getObject();
		if (entity instanceof MobEntity) {
			event.addCapability(ModDefinitions.getResource("musician"), new IMusician.Provider((MobEntity)entity));
		}
	}

	@SubscribeEvent
	public void attachStackCapabilities(AttachCapabilitiesEvent<ItemStack> event) {
		ItemStack stack = event.getObject();
		if (stack.getItem() instanceof InstrumentItem) {
			event.addCapability(ModDefinitions.getResource("instrument"), new IInstrument.Provider());
		}
	}

	@SubscribeEvent
	public void entityTick(LivingUpdateEvent event) {
		LivingEntity entity = event.getEntityLiving();
		World world = entity.level;
		LazyOptional<IMusician> optional = entity.getCapability(MUSICIAN_CAPABILITY);
		if (optional.isPresent()) {
			if (!world.isClientSide) {
				for (ItemStack stack : entity.getHandSlots()) {
					if (stack.getItem() instanceof InstrumentItem) {
						InstrumentItem item = ((InstrumentItem) stack.getItem());
						if (entity.tickCount % (item.getCooldown()+20) == 0) {
							IMusician cap = optional.resolve().get();
							LivingEntity target = ((MobEntity) entity).getTarget();
							boolean playInstrument = false;
							for (MobEntity follower : item.getFollowEntities(world, entity)) {
								if (!cap.isLeading(follower)) {
									follower.setTarget(target);
									cap.addFollower(follower);
									playInstrument = FollowHandler.processInteraction(world, entity, follower, Hand.MAIN_HAND) || playInstrument ;
								}
							}
							if (playInstrument) {
								PacketHandler.NETWORK_INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(
										()->(Chunk)world.getChunk(entity.blockPosition())),new PacketHandler.InstrumentMessage((MobEntity) entity, item));
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
