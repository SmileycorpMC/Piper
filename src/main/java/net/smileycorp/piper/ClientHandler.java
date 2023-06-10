package net.smileycorp.piper;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.smileycorp.piper.PacketHandler.InstrumentMessage;

public class ClientHandler {

	public static void processInstrument(InstrumentMessage message) {
		Minecraft mc = Minecraft.getInstance();
		Level level = mc.level;
		playInstrument(message.getEntity(level), message.getSound());
	}

	public static void playInstrument(LivingEntity user, SoundEvent sound) {
		Minecraft mc = Minecraft.getInstance();
		Player player = mc.player;
		Level level = user.level();
		Vec3 facing = user.getLookAngle();
		double x =  user.getX() + facing.x;
		double y = user.getY() + user.getEyeHeight() + facing.y;
		double z = user.getZ() + facing.z;
		level.addParticle(ParticleTypes.NOTE,x, y, z, 0, 0.3f, 0);
		if (sound!=null)level.playSound(player, user, sound, user.getSoundSource(), 0.6f, level.random.nextFloat());
	}

}
