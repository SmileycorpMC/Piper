package net.smileycorp.piper.mixin;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraftforge.fml.loading.FMLPaths;

@Mixin(PackRepository.class)
public class MixinPackRepository {

	@Inject(at = @At("TAIL"), method = "openAllSelected()Ljava/util/List;", cancellable = true)
	private void openAllSelected(CallbackInfoReturnable<List<PackResources>> callback) {
		List<PackResources> packs = new ArrayList<PackResources>();
		packs.addAll(callback.getReturnValue());
		Path pack = FMLPaths.GAMEDIR.get().resolve("config").resolve("piper").resolve("resources");
		packs.add(new PathPackResources("piper", pack, true));
		callback.setReturnValue(packs);
	}

}
