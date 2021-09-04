package net.smileycorp.piper;

import net.minecraft.util.ResourceLocation;

public class ModDefinitions {

	public static final String MODID = "piper";
	public static final String NAME = "Piper";

	public static ResourceLocation getResource(String name) {
		return new ResourceLocation(MODID, name.toLowerCase().replace(" ", "_"));
	}
}
