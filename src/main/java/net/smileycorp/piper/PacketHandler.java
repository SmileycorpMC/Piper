package net.smileycorp.piper;

import java.io.IOException;

import net.minecraft.entity.LivingEntity;
import net.minecraft.network.INetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
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

	public static class InstrumentMessage implements IPacket<INetHandler> {

			public InstrumentMessage() {}

			private int entity;
			private String item;

			public InstrumentMessage(LivingEntity user, InstrumentItem item) {
				this.entity = user.getId();
				this.item = item.getRegistryName().getPath();
			}

			@Override
			public void read(PacketBuffer buf) throws IOException {
				entity = buf.readInt();
				item = buf.readUtf();
			}

			@Override
			public void write(PacketBuffer buf) throws IOException {
				buf.writeInt(entity);
				buf.writeUtf(item);
			}

			public LivingEntity getEntity(World world) {
				return (LivingEntity) world.getEntity(entity);
			}

			public SoundEvent getSound() {
				return Piper.ITEMS.containsKey(item) ? Piper.ITEMS.get(item).getSound() : null;
			}

			@Override
			public void handle(INetHandler handler) {}

	}
}
