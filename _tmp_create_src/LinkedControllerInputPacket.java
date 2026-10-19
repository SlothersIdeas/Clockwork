package com.simibubi.create.content.redstone.link.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;
import net.minecraft.class_1799;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_2540;
import net.minecraft.class_3222;

public class LinkedControllerInputPacket extends LinkedControllerPacketBase {

	private Collection<Integer> activatedButtons;
	private boolean press;

	public LinkedControllerInputPacket(Collection<Integer> activatedButtons, boolean press) {
		this(activatedButtons, press, null);
	}

	public LinkedControllerInputPacket(Collection<Integer> activatedButtons, boolean press, class_2338 lecternPos) {
		super(lecternPos);
		this.activatedButtons = activatedButtons;
		this.press = press;
	}

	public LinkedControllerInputPacket(class_2540 buffer) {
		super(buffer);
		activatedButtons = new ArrayList<>();
		press = buffer.readBoolean();
		int size = buffer.method_10816();
		for (int i = 0; i < size; i++)
			activatedButtons.add(buffer.method_10816());
	}

	@Override
	public void write(class_2540 buffer) {
		super.write(buffer);
		buffer.writeBoolean(press);
		buffer.method_10804(activatedButtons.size());
		activatedButtons.forEach(buffer::method_10804);
	}

	@Override
	protected void handleLectern(class_3222 player, LecternControllerBlockEntity lectern) {
		if (lectern.isUsedBy(player))
			handleItem(player, lectern.getController());
	}

	@Override
	protected void handleItem(class_3222 player, class_1799 heldItem) {
		class_1937 world = player.method_5770();
		UUID uniqueID = player.method_5667();
		class_2338 pos = player.method_24515();

		if (player.method_7325() && press)
			return;
		
		LinkedControllerServerHandler.receivePressed(world, pos, uniqueID, activatedButtons.stream()
			.map(i -> LinkedControllerItem.toFrequency(heldItem, i))
			.collect(Collectors.toList()), press);
	}

}
