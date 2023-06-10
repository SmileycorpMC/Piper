package net.smileycorp.piper;

import net.minecraft.resources.ResourceLocation;

public class Constants {

	public static final String MODID = "piper";
	public static final String NAME = "Piper";

	public static ResourceLocation loc(String name) {
		return new ResourceLocation(MODID, name.toLowerCase().replace(" ", "_"));
	}
}
