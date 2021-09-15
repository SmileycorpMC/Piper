package net.smileycorp.piper;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.smileycorp.piper.PacketHandler.InstrumentMessage;

public class ClientHandler {

	public static void processInstrument(InstrumentMessage message) {
		World world = Minecraft.getInstance().level;
		playInstrument(message.getEntity(world), message.getSound());
	}

	public static void playInstrument(LivingEntity user, SoundEvent sound) {
		Minecraft mc = Minecraft.getInstance();
		PlayerEntity player = mc.player;
		World world = user.level;
		Vector3d facing = user.getLookAngle();
		double x =  user.getX() + facing.x;
		double y = user.getY() + user.getEyeHeight() + facing.y;
		double z = user.getZ() + facing.z;
		world.addParticle(ParticleTypes.NOTE,x, y, z, 0, 0.3f, 0);
		if (sound!=null)world.playSound(player, user, sound, user.getSoundSource(), 0.6f, world.random.nextFloat());
	}

}
