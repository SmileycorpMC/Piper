package net.smileycorp.piper;


import java.util.HashMap;
import java.util.Map;

import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.registries.IForgeRegistry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(value = ModDefinitions.MODID)
@Mod.EventBusSubscriber(modid = ModDefinitions.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Piper {

	private static Logger logger = LogManager.getLogger(ModDefinitions.NAME);

	protected static Map<String, InstrumentItem> ITEMS = new HashMap<String, InstrumentItem>();

	@SubscribeEvent
	public static void onModConstruction(FMLConstructModEvent event) {
		ItemLoader.loadFiles();
	}

	@SubscribeEvent
	public static void registerItems(Register<Item> event) {
		IForgeRegistry<Item> registry = event.getRegistry();
		for (Item item : ITEMS.values()) registry.register(item);
	}

	public static void logInfo(Object message) {
		logger.info(message);
	}

	public static void logError(Object message, Exception e) {
		logger.error(message);
		e.printStackTrace();
	}

}
