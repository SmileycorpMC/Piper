package net.smileycorp.piper;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fmllegacy.network.NetworkEvent.Context;
import net.minecraftforge.fmllegacy.network.NetworkRegistry;
import net.minecraftforge.fmllegacy.network.simple.SimpleChannel;
import net.smileycorp.atlas.api.network.SimpleAbstractMessage;
import net.smileycorp.atlas.api.network.SimpleMessageDecoder;
import net.smileycorp.atlas.api.network.SimpleMessageEncoder;

public class PacketHandler {

	public static SimpleChannel NETWORK_INSTANCE;

	public static void initPackets() {
		NETWORK_INSTANCE = NetworkRegistry.newSimpleChannel(ModDefinitions.getResource("main"), ()-> "1", "1"::equals, "1"::equals);
		NETWORK_INSTANCE.registerMessage(0, InstrumentMessage.class, new SimpleMessageEncoder<InstrumentMessage>(),
				new SimpleMessageDecoder<InstrumentMessage>(InstrumentMessage.class), (T, K)-> processInstrumentMessage(T, K.get()));
	}

	public static void processInstrumentMessage(InstrumentMessage message, Context ctx) {
		ctx.enqueueWork(() ->  DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> () -> ClientHandler.processInstrument(message)));
		ctx.setPacketHandled(true);
	}

	public static class InstrumentMessage extends SimpleAbstractMessage {

			public InstrumentMessage() {}

			private int entity;
			private String item;

			public InstrumentMessage(Mob entity, Instrument item) {
				this.entity = entity.getId();
				this.item = item.getRegistryName().getPath();
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

			public Mob getEntity(Level level) {
				return (Mob) level.getEntity(entity);
			}

			public SoundEvent getSound() {
				return Piper.ITEMS.containsKey(item) ? Piper.ITEMS.get(item).getSound() : null;
			}

			@Override
			public void handle(PacketListener handler) {}

	}
}
