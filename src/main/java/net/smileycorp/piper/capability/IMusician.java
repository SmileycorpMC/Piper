package net.smileycorp.piper.capability;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.Direction;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.smileycorp.piper.Piper;

public interface IMusician {

	public LivingEntity getTarget();

	public void setTarget(LivingEntity target);

	public boolean isLeading(Mob entity);

	public boolean hasFollowers();

	public List<Mob> getFollowers();

	public void addFollower(Mob entity);

	public void removeFollower(Mob entity);

	public void removeAllFollowers();

	public ListTag writeNBT(ListTag nbt);

	public void readNBT(ListTag nbt);

	public static class Implementation implements IMusician {

		private List<Mob> followers = new ArrayList<Mob>();

		private final Mob user;
		private LivingEntity target = null;

		public Implementation() {
			user = null;
		}

		public Implementation(Mob entity) {
			this.user=entity;
		}

		@Override
		public LivingEntity getTarget() {
			return target;
		}

		@Override
		public void setTarget(LivingEntity target) {
			this.target=target;
			for (Mob entity : followers) {
				if (entity!=null)entity.setTarget(target);
			}
		}

		@Override
		public boolean isLeading(Mob entity) {
			return followers.contains(entity);
		}

		@Override
		public boolean hasFollowers() {
			return !followers.isEmpty();
		}

		@Override
		public List<Mob> getFollowers() {
			return new ArrayList<Mob>(followers);
		}

		@Override
		public void addFollower(Mob entity) {
			if (entity!=null) followers.add(entity);
		}

		@Override
		public void removeFollower(Mob entity) {
			followers.remove(entity);
		}

		@Override
		public void removeAllFollowers() {
			followers.clear();
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
			if (user!=null) {
				for (Tag tag : nbt) {
					if (tag instanceof IntTag) {
						followers.add((Mob) user.level.getEntity(((IntTag) tag).getAsInt()));
					}
				}
			}
		}

	}


	public static class Provider implements ICapabilitySerializable<ListTag> {

		private final IMusician impl;

		public Provider(Mob entity) {
			impl = new Implementation(entity);
		}

		@Override
		public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
			return cap == Piper.MUSICIAN_CAPABILITY ? LazyOptional.of(() -> impl).cast() : LazyOptional.empty();
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
