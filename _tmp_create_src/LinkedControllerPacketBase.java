package com.simibubi.create.content.redstone.link.controller;

import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.networking.SimplePacketBase;
import net.minecraft.class_1799;
import net.minecraft.class_2338;
import net.minecraft.class_2540;
import net.minecraft.class_2586;
import net.minecraft.class_3222;

public abstract class LinkedControllerPacketBase extends SimplePacketBase {

	private class_2338 lecternPos;

	public LinkedControllerPacketBase(class_2338 lecternPos) {
		this.lecternPos = lecternPos;
	}

	public LinkedControllerPacketBase(class_2540 buffer) {
		if (buffer.readBoolean()) {
			lecternPos = new class_2338(buffer.readInt(), buffer.readInt(), buffer.readInt());
		}
	}

	protected boolean inLectern() {
		return lecternPos != null;
	}

	@Override
	public void write(class_2540 buffer) {
		buffer.writeBoolean(inLectern());
		if (inLectern()) {
			buffer.writeInt(lecternPos.method_10263());
			buffer.writeInt(lecternPos.method_10264());
			buffer.writeInt(lecternPos.method_10260());
		}
	}

	@Override
	public boolean handle(Context context) {
		context.enqueueWork(() -> {
			class_3222 player = context.getSender();
			if (player == null)
				return;

			if (inLectern()) {
				class_2586 be = player.method_37908().method_8321(lecternPos);
				if (!(be instanceof LecternControllerBlockEntity))
					return;
				handleLectern(player, (LecternControllerBlockEntity) be);
			} else {
				class_1799 controller = player.method_6047();
				if (!AllItems.LINKED_CONTROLLER.isIn(controller)) {
					controller = player.method_6079();
					if (!AllItems.LINKED_CONTROLLER.isIn(controller))
						return;
				}
				handleItem(player, controller);
			}
		});
		return true;
	}

	protected abstract void handleItem(class_3222 player, class_1799 heldItem);
	protected abstract void handleLectern(class_3222 player, LecternControllerBlockEntity lectern);

}
