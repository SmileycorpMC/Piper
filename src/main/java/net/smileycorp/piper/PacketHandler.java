package net.smileycorp.piper;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent.Context;
import net.minecraftforge.network.simple.SimpleChannel;
import net.smileycorp.atlas.api.network.AbstractMessage;
import net.smileycorp.atlas.api.network.NetworkUtils;

public class PacketHandler {

	public static SimpleChannel NETWORK_INSTANCE;

	public static void initPackets() {
		NETWORK_INSTANCE = NetworkUtils.createChannel(Constants.loc("main"));
		NetworkUtils.registerMessage(NETWORK_INSTANCE, 0, InstrumentMessage.class);
	}

	public static class InstrumentMessage extends AbstractMessage {

		public InstrumentMessage() {}

		private int entity;
		private String item;

		public InstrumentMessage(LivingEntity entity, Instrument item) {
			this.entity = entity.getId();
			this.item = item.getInstrumentName().getPath();
		}

		@Override
		public void read(FriendlyByteBuf buf) {
			entity = buf.readInt();
			item = buf.readUtf();
		}

		@Override
		public void write(FriendlyByteBuf buf) {
			buf.writeInt(entity);
			buf.writeUtf(item);
		}

		public LivingEntity getEntity(Level level) {
			return (LivingEntity) level.getEntity(entity);
		}

		public SoundEvent getSound() {
			return InstrumentRegistry.isInstrumentRegistered(item) ? InstrumentRegistry.getInstrument(item).getSound() : null;
		}

		@Override
		public void handle(PacketListener handler) {}

		@Override
		public void process(Context ctx) {
			ctx.enqueueWork(() ->  DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> () -> ClientHandler.processInstrument(this)));
			ctx.setPacketHandled(true);
		}

	}
}
