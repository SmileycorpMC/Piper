package net.smileycorp.piper.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.smileycorp.followme.common.FollowHandler;
import net.smileycorp.followme.common.ai.FollowUserGoal;
import net.smileycorp.piper.Piper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public interface IInstrument {

	public boolean hasFollowers();

	public void addFollower(Mob entity);

	public void removeAllFollowers();

	public ListTag writeNBT(ListTag nbt);

	public void readNBT(ListTag nbt);

	public static class Implementation implements IInstrument {

		private List<Mob> followers = new ArrayList<Mob>();

		@Override
		public boolean hasFollowers() {
			return !followers.isEmpty();
		}

		@Override
		public void addFollower(Mob entity) {
			if (entity!=null) followers.add(entity);
		}

		@Override
		public void removeAllFollowers() {
			Set<FollowUserGoal> toRemove = new HashSet<FollowUserGoal>();
			for (Mob entity : followers) {
				if (entity!=null) {
					for (WrappedGoal entry : entity.goalSelector.getRunningGoals().toArray(WrappedGoal[]::new)) {
						if (entry.getGoal() instanceof FollowUserGoal) toRemove.add((FollowUserGoal) entry.getGoal());
					}
				}
			}
			followers.clear();
			for (FollowUserGoal goal : toRemove) FollowHandler.removeAI(goal);
		}

		@Override
		public ListTag writeNBT(ListTag nbt) {
			for (Mob entity : followers) {
				if (entity!=null) nbt.add(IntTag.valueOf(entity.getId()));
			}
			return nbt;
		}

		@Override
		public void readNBT(ListTag nbt) {
			for (Tag tag : nbt) {
				if (tag instanceof IntTag) {
					followers.add((Mob) ServerLifecycleHooks.getCurrentServer().overworld().getEntity(((IntTag) tag).getAsInt()));
				}
			}
		}

	}


	public static class Provider implements ICapabilitySerializable<ListTag> {

		private final IInstrument impl;

		public Provider() {
			impl = new Implementation();
		}

		@Override
		public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
			return cap == Piper.INSTRUMENT_CAPABILITY ? LazyOptional.of(() -> impl).cast() : LazyOptional.empty();
		}

		@Override
		public ListTag serializeNBT() {
			return impl.writeNBT(new ListTag());
		}

		@Override
		public void deserializeNBT(ListTag nbt) {
			impl.readNBT(nbt);
		}

	}

}
