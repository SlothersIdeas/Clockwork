package com.simibubi.create.content.redstone.link.controller;

import com.simibubi.create.content.redstone.link.LinkBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandler;
import net.minecraft.class_1799;
import net.minecraft.class_2338;
import net.minecraft.class_2540;
import net.minecraft.class_3222;

public class LinkedControllerBindPacket extends LinkedControllerPacketBase {

	private int button;
	private class_2338 linkLocation;

	public LinkedControllerBindPacket(int button, class_2338 linkLocation) {
		super((class_2338) null);
		this.button = button;
		this.linkLocation = linkLocation;
	}

	public LinkedControllerBindPacket(class_2540 buffer) {
		super(buffer);
		this.button = buffer.method_10816();
		this.linkLocation = buffer.method_10811();
	}

	@Override
	public void write(class_2540 buffer) {
		super.write(buffer);
		buffer.method_10804(button);
		buffer.method_10807(linkLocation);
	}

	@Override
	protected void handleItem(class_3222 player, class_1799 heldItem) {
		if (player.method_7325())
			return;

		ItemStackHandler frequencyItems = LinkedControllerItem.getFrequencyItems(heldItem);
		LinkBehaviour linkBehaviour = BlockEntityBehaviour.get(player.method_37908(), linkLocation, LinkBehaviour.TYPE);
		if (linkBehaviour == null)
			return;

		linkBehaviour.getNetworkKey()
			.forEachWithContext((f, first) -> frequencyItems.setStackInSlot(button * 2 + (first ? 0 : 1), f.getStack()
				.method_7972()));

		heldItem.method_7969()
			.method_10566("Items", frequencyItems.serializeNBT());
	}

	@Override
	protected void handleLectern(class_3222 player, LecternControllerBlockEntity lectern) {}

}
