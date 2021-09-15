package net.smileycorp.piper.capability;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.smileycorp.piper.Piper;

public interface IMusician {

	public LivingEntity getTarget();

	public void setTarget(LivingEntity target);

	public boolean isLeading(MobEntity entity);

	public boolean hasFollowers();

	public List<MobEntity> getFollowers();

	public void addFollower(MobEntity entity);

	public void removeFollower(MobEntity entity);

	public void removeAllFollowers();

	public ListNBT writeNBT(ListNBT nbt);

	public void readNBT(ListNBT nbt);

	public static class Storage implements IStorage<IMusician> {

		@Override
		public INBT writeNBT(Capability<IMusician> capability, IMusician instance, Direction side) {
			return instance.writeNBT(new ListNBT());
		}

		@Override
		public void readNBT(Capability<IMusician> capability, IMusician instance, Direction side, INBT nbt) {
			if (nbt instanceof ListNBT)instance.readNBT((ListNBT) nbt);
		}

	}

	public static class Implementation implements IMusician {

		private List<MobEntity> followers = new ArrayList<MobEntity>();

		private final MobEntity user;
		private LivingEntity target = null;

		public Implementation() {
			user = null;
		}

		public Implementation(MobEntity user) {
			this.user=user;
		}

		@Override
		public LivingEntity getTarget() {
			return target;
		}

		@Override
		public void setTarget(LivingEntity target) {
			this.target=target;
			for (MobEntity entity : followers) {
				if (entity!=null)entity.setTarget(target);
			}
		}

		@Override
		public boolean isLeading(MobEntity entity) {
			return followers.contains(entity);
		}

		@Override
		public boolean hasFollowers() {
			return !followers.isEmpty();
		}

		@Override
		public List<MobEntity> getFollowers() {
			return new ArrayList<MobEntity>(followers);
		}

		@Override
		public void addFollower(MobEntity entity) {
			if (entity!=null)followers.add(entity);
		}

		@Override
		public void removeFollower(MobEntity entity) {
			followers.remove(entity);
		}

		@Override
		public void removeAllFollowers() {
			followers.clear();
		}

		@Override
		public ListNBT writeNBT(ListNBT nbt) {
			for (MobEntity entity : followers) {
				if (entity!=null)nbt.add(IntNBT.valueOf(entity.getId()));
			}
			return nbt;
		}

		@Override
		public void readNBT(ListNBT nbt) {
			if (user!=null) {
				for (INBT tag : nbt) {
					if (tag instanceof IntNBT) {
						followers.add((MobEntity) user.level.getEntity(((IntNBT) tag).getAsInt()));
					}
				}
			}
		}

	}


	public static class Provider implements ICapabilitySerializable<ListNBT> {

		private final IMusician impl;

		public Provider(MobEntity entity) {
			impl = new Implementation(entity);
		}

		@Override
		public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
			return cap == Piper.MUSICIAN_CAPABILITY ? LazyOptional.of(() -> impl).cast() : LazyOptional.empty();
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
