package net.smileycorp.piper.mixin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.resources.FolderPack;
import net.minecraft.resources.IResourcePack;
import net.minecraft.resources.ResourcePackList;
import net.minecraftforge.fml.loading.FMLPaths;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ResourcePackList.class)
public class MixinResourcePackList {

	@Inject(at = @At("TAIL"), method = "openAllSelected()Ljava/util/List;", cancellable = true)
	private void createFullReload(CallbackInfoReturnable<List<IResourcePack>> callback) {
		List<IResourcePack> packs = new ArrayList<IResourcePack>();
		packs.addAll(callback.getReturnValue());
		File pack = FMLPaths.GAMEDIR.get().resolve("config").resolve("piper").resolve("resources").toFile();
		if (!pack.exists()) {

		}
		packs.add(new FolderPack(pack));
		callback.setReturnValue(packs);
	}

}
