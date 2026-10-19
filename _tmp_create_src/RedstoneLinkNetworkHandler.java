package com.simibubi.create.content.redstone.link;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.simibubi.create.Create;
import com.simibubi.create.infrastructure.config.AllConfigs;

import net.createmod.catnip.data.Couple;
import net.createmod.catnip.levelWrappers.WorldHelper;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1936;
import net.minecraft.class_2487;

public class RedstoneLinkNetworkHandler {

	static final Map<class_1936, Map<Couple<Frequency>, Set<IRedstoneLinkable>>> connections =
			new IdentityHashMap<>();

	public final AtomicInteger globalPowerVersion = new AtomicInteger();

	public static class Frequency {
		public static final Frequency EMPTY = new Frequency(class_1799.field_8037);
		private static final Map<class_1792, Frequency> simpleFrequencies = new IdentityHashMap<>();
		private class_1799 stack;
		private class_1792 item;
		private int color;

		public static Frequency of(class_1799 stack) {
			if (stack.method_7960())
				return EMPTY;
			if (!stack.method_7985())
				return simpleFrequencies.computeIfAbsent(stack.method_7909(), $ -> new Frequency(stack));
			return new Frequency(stack);
		}

		private Frequency(class_1799 stack) {
			this.stack = stack;
			item = stack.method_7909();
			class_2487 displayTag = stack.method_7941("display");
			color = displayTag != null && displayTag.method_10545("color") ? displayTag.method_10550("color") : -1;
		}

		public class_1799 getStack() {
			return stack;
		}

		@Override
		public int hashCode() {
			return (item.hashCode() * 31) ^ color;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			return obj instanceof Frequency ? ((Frequency) obj).item == item && ((Frequency) obj).color == color
					: false;
		}

	}

	public void onLoadWorld(class_1936 world) {
		connections.put(world, new HashMap<>());
		Create.LOGGER.debug("Prepared Redstone Network Space for " + WorldHelper.getDimensionID(world));
	}

	public void onUnloadWorld(class_1936 world) {
		connections.remove(world);
		Create.LOGGER.debug("Removed Redstone Network Space for " + WorldHelper.getDimensionID(world));
	}

	public Set<IRedstoneLinkable> getNetworkOf(class_1936 world, IRedstoneLinkable actor) {
		Map<Couple<Frequency>, Set<IRedstoneLinkable>> networksInWorld = networksIn(world);
		Couple<Frequency> key = actor.getNetworkKey();
		if (!networksInWorld.containsKey(key))
			networksInWorld.put(key, new LinkedHashSet<>());
		return networksInWorld.get(key);
	}

	public void addToNetwork(class_1936 world, IRedstoneLinkable actor) {
		getNetworkOf(world, actor).add(actor);
		updateNetworkOf(world, actor);
	}

	public void removeFromNetwork(class_1936 world, IRedstoneLinkable actor) {
		Set<IRedstoneLinkable> network = getNetworkOf(world, actor);
		network.remove(actor);
		if (network.isEmpty()) {
			networksIn(world).remove(actor.getNetworkKey());
			return;
		}
		updateNetworkOf(world, actor);
	}

	public void updateNetworkOf(class_1936 world, IRedstoneLinkable actor) {
		Set<IRedstoneLinkable> network = getNetworkOf(world, actor);
		globalPowerVersion.incrementAndGet();
		int power = 0;

		for (Iterator<IRedstoneLinkable> iterator = network.iterator(); iterator.hasNext(); ) {
			IRedstoneLinkable other = iterator.next();
			if (!other.isAlive()) {
				iterator.remove();
				continue;
			}

			if (!withinRange(actor, other))
				continue;

			if (power < 15)
				power = Math.max(other.getTransmittedStrength(), power);
		}

		if (actor instanceof LinkBehaviour linkBehaviour) {
			// fix one-to-one loading order problem
			if (linkBehaviour.isListening()) {
				linkBehaviour.newPosition = true;
				linkBehaviour.setReceivedStrength(power);
			}
		}

		for (IRedstoneLinkable other : network) {
			if (other != actor && other.isListening() && withinRange(actor, other))
				other.setReceivedStrength(power);
		}
	}

	public static boolean withinRange(IRedstoneLinkable from, IRedstoneLinkable to) {
		if (from == to)
			return true;
		return from.getLocation()
				.method_19771(to.getLocation(), AllConfigs.server().logistics.linkRange.get());
	}

	public Map<Couple<Frequency>, Set<IRedstoneLinkable>> networksIn(class_1936 world) {
		if (!connections.containsKey(world)) {
			Create.LOGGER.warn("Tried to Access unprepared network space of " + WorldHelper.getDimensionID(world));
			return new HashMap<>();
		}
		return connections.get(world);
	}

	public boolean hasAnyLoadedPower(Couple<Frequency> frequency) {
		for (Map<Couple<Frequency>, Set<IRedstoneLinkable>> map : connections.values()) {
			Set<IRedstoneLinkable> set = map.get(frequency);
			if (set == null || set.isEmpty())
				continue;
			for (IRedstoneLinkable link : set)
				if (link.getTransmittedStrength() > 0)
					return true;
		}
		return false;
	}

}
