package net.smileycorp.piper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import org.apache.commons.io.FileUtils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.loading.moddiscovery.ModFile;

public class ItemLoader {

	public static void loadFiles() {
		File directory = FMLPaths.GAMEDIR.get().resolve("config").resolve("piper").toFile();
		if (!(directory.exists() || directory.isDirectory())) {
			try {
				createDefaultFiles(directory);
			} catch (Exception e) {
				Piper.logError("failed to write file", e);
			}
		}
		for (File file : directory.listFiles()) {
			if (file.getName().endsWith(".json")) {
				try {
					readFile(file);
				} catch (Exception e) {
					Piper.logError("failed to load file " + file.getName(), e);
				}
			}
		}
	}

	private static void readFile(File file) throws Exception {
		String name = file.getName().replace(".json", "");
		StringBuilder builder = new StringBuilder();
		BufferedReader reader = new BufferedReader(new FileReader(file));
		reader.lines().forEach((s)->builder.append(s));
		reader.close();
		JsonObject json = new JsonParser().parse(builder.toString()).getAsJsonObject();
		Piper.ITEMS.put(name.split("\\.")[0], Instrument.fromJson(name, json));
	}

	private static void createDefaultFiles(File directory) throws Exception {
		ModFile mod = FMLLoader.getLoadingModList().getModFileById("piper").getFile();
		copyFile(mod, directory, "pipe.json");
		copyFile(mod, directory, "resources/pack.mcmeta");
		copyFile(mod, directory, "resources/assets/piper/sounds.json");
		copyFile(mod, directory, "resources/assets/piper/lang/en_us.json");
		copyFile(mod, directory, "resources/assets/piper/models/item/pipe.json");
		copyFile(mod, directory, "resources/assets/piper/sounds/item/pipe0.ogg");
		copyFile(mod, directory, "resources/assets/piper/sounds/item/pipe1.ogg");
		copyFile(mod, directory, "resources/assets/piper/textures/item/pipe.png");
		copyFile(mod, directory, "resources/data/piper/recipes/pipe.json");
	}

	private static void copyFile(ModFile mod, File directory, String path) throws Exception {
		File output = new File(directory, path);
		File dir = output.getParentFile();
		if (dir!=null)dir.mkdirs();
		FileUtils.copyInputStreamToFile(Files.newInputStream(mod.findResource("config-defaults/"+path), StandardOpenOption.READ), new File(directory, path));
	}

}
