package net.smileycorp.piper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import net.minecraftforge.fml.common.thread.SidedThreadGroups;
import net.minecraftforge.fml.loading.FMLPaths;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ItemLoader {

	public static void loadFiles() {
		File directory = FMLPaths.GAMEDIR.get().resolve("config").resolve("piper").toFile();
		if (!(directory.exists() || directory.isDirectory())) {
			try {
				createDefaultFile(directory);
			} catch (IOException e) {
				Piper.logError("failed to write file", e);
			}
		}
		for (File file : directory.listFiles()) {
			try {
				readFile(file);
			} catch (Exception e) {
				Piper.logError("failed to load file " + file.getName(), e);
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
		Piper.ITEMS.put(name.split("\\.")[0], InstrumentItem.fromJson(name, json));
	}

	private static void createDefaultFile(File directory) throws IOException {
		directory.mkdir();
		File file = new File(directory, "pipe.json");
		file.createNewFile();
		FileWriter writer = new FileWriter(file);
		writer.append("{\n");
		writer.append("	\"sound\": \"minecraft:block.note_block.flute\",\n");
		writer.append("	\"entities\": [\n");
		writer.append("		\"minecraft:pig\",\n");
		writer.append("		\"minecraft:cow\",\n");
		writer.append("		\"minecraft:sheep\",\n");
		writer.append("		\"minecraft:chicken\",\n");
		writer.append("		\"minecraft:llama\",\n");
		writer.append("		\"minecraft:donkey\",\n");
		writer.append("		\"minecraft:mule\"\n");
		writer.append("	]\n");
		writer.append("}\n");
		writer.close();
		if (Thread.currentThread().getThreadGroup() == SidedThreadGroups.CLIENT) {

		}
	}

}
