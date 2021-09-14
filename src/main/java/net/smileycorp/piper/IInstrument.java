package net.smileycorp.piper;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.PrioritizedGoal;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.smileycorp.followme.common.FollowHandler;
import net.smileycorp.followme.common.ai.FollowUserGoal;

public interface IInstrument {

	public boolean hasFollowers();

	public void addFollower(MobEntity entity);

	public void removeAllFollowers();

	public ListNBT writeNBT(ListNBT nbt);

	public void readNBT(ListNBT nbt);

	public static class Storage implements IStorage<IInstrument> {

		@Override
		public INBT writeNBT(Capability<IInstrument> capability, IInstrument instance, Direction side) {
			return instance.writeNBT(new ListNBT());
		}

		@Override
		public void readNBT(Capability<IInstrument> capability, IInstrument instance, Direction side, INBT nbt) {
			if (nbt instanceof ListNBT)instance.readNBT((ListNBT) nbt);
		}

	}

	public static class Implementation implements IInstrument {

		private List<MobEntity> followers = new ArrayList<MobEntity>();

		@Override
		public boolean hasFollowers() {
			return !followers.isEmpty();
		}

		@Override
		public void addFollower(MobEntity entity) {
			followers.add(entity);
		}

		@Override
		public void removeAllFollowers() {
			for (MobEntity entity : followers) {
				for (PrioritizedGoal entry : entity.goalSelector.getRunningGoals().toArray(PrioritizedGoal[]::new)) {
					if (entry.getGoal() instanceof FollowUserGoal) FollowHandler.removeAI((FollowUserGoal) entry.getGoal());
				}
			}
			followers.clear();
		}

		@Override
		public ListNBT writeNBT(ListNBT nbt) {
			for (MobEntity entity : followers) {
				nbt.add(IntNBT.valueOf(entity.getId()));
			}
			return nbt;
		}

		@Override
		public void readNBT(ListNBT nbt) {
			for (INBT tag : nbt) {
				if (tag instanceof IntNBT) {
					followers.add((MobEntity) ServerLifecycleHooks.getCurrentServer().overworld().getEntity(((IntNBT) tag).getAsInt()));
				}
			}
		}

	}


	public static class Provider implements ICapabilitySerializable<ListNBT> {

		private final IInstrument impl;

		public Provider() {
			impl = new Implementation();
		}

		@Override
		public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
			return cap == Piper.INSTRUMENT_CAPABILITY ? LazyOptional.of(() -> impl).cast() : LazyOptional.empty();
		}

		@Override
		public ListNBT serializeNBT() {
			return impl.writeNBT(new ListNBT());
		}

		@Override
		public void deserializeNBT(ListNBT nbt) {
			impl.readNBT(nbt);
		}

	}

}
