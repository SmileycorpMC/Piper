package net.smileycorp.piper;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.world.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegistryObject;
import net.smileycorp.piper.capability.IInstrument;
import net.smileycorp.piper.capability.IMusician;

@Mod(value = ModDefinitions.MODID)
@Mod.EventBusSubscriber(modid = ModDefinitions.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Piper {

	private static Logger logger = LogManager.getLogger(ModDefinitions.NAME);

	public static Capability<IMusician> MUSICIAN_CAPABILITY = CapabilityManager.get(new CapabilityToken<IMusician>(){});

	public static Capability<IInstrument> INSTRUMENT_CAPABILITY = CapabilityManager.get(new CapabilityToken<IInstrument>(){});

	@SubscribeEvent
	public static void onModConstruction(FMLConstructModEvent event) {
		ItemLoader.loadFiles();
		PacketHandler.initPackets();
		MinecraftForge.EVENT_BUS.register(new EventListener());
		InstrumentRegistry.REGISTRY.register(FMLJavaModLoadingContext.get().getModEventBus());
	}

	@SubscribeEvent
	public static void onModSetup(FMLCommonSetupEvent event) {
		for (RegistryObject<Item> item : InstrumentRegistry.ITEMS.values()) ((Instrument) item.get()).buildEntities();
	}

	public static void logInfo(Object message) {
		logger.info(message);
	}

	public static void logError(Object message, Exception e) {
		logger.error(message, e);
	}

}
