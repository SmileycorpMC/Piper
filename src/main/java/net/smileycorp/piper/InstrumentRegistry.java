package net.smileycorp.piper;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class InstrumentRegistry {

	static final Map<String, RegistryObject<Item>> ITEMS = new HashMap<String, RegistryObject<Item>>();
	static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, ModDefinitions.MODID);

	public static void register(String name, Supplier<Instrument> supplier) {
		try {
			ITEMS.put(name, REGISTRY.register(name, supplier));
		} catch (Exception e) {
			Piper.logError("failed to register item piper:"+name, e);
		}
	}
	public static boolean isInstrumentRegistered(String item) {
		return ITEMS.containsKey(item);
	}
	public static Instrument getInstrument(String item) {
		return (Instrument) ITEMS.get(item).get();
	}

}
