package com.simibubi.create.content.redstone.link.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import org.lwjgl.glfw.GLFW;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllPackets;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.redstone.link.LinkBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.utility.ControlsUtil;
import com.simibubi.create.foundation.utility.CreateLang;

import net.createmod.catnip.lang.FontHelper.Palette;
import net.createmod.catnip.lang.Lang;
import net.createmod.catnip.outliner.Outliner;
import net.minecraft.class_1041;
import net.minecraft.class_124;
import net.minecraft.class_1799;
import net.minecraft.class_2338;
import net.minecraft.class_2561;
import net.minecraft.class_265;
import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_3675;
import net.minecraft.class_437;
import net.minecraft.class_4587;
import net.minecraft.class_5244;
import net.minecraft.class_746;

public class LinkedControllerClientHandler {

	public static Mode MODE = Mode.IDLE;
	public static int PACKET_RATE = 5;
	public static Collection<Integer> currentlyPressed = new HashSet<>();
	private static class_2338 lecternPos;
	private static class_2338 selectedLocation = class_2338.field_10980;
	private static int packetCooldown;

	public static void toggleBindMode(class_2338 location) {
		if (MODE == Mode.IDLE) {
			MODE = Mode.BIND;
			selectedLocation = location;
		} else {
			MODE = Mode.IDLE;
			onReset();
		}
	}

	public static void toggle() {
		if (MODE == Mode.IDLE) {
			MODE = Mode.ACTIVE;
			lecternPos = null;
		} else {
			MODE = Mode.IDLE;
			onReset();
		}
	}

	public static void activateInLectern(class_2338 lecternAt) {
		if (MODE == Mode.IDLE) {
			MODE = Mode.ACTIVE;
			lecternPos = lecternAt;
		}
	}

	public static void deactivateInLectern() {
		if (MODE == Mode.ACTIVE && inLectern()) {
			MODE = Mode.IDLE;
			onReset();
		}
	}

	public static boolean inLectern() {
		return lecternPos != null;
	}

	protected static void onReset() {
		ControlsUtil.getControls()
			.forEach(kb -> kb.method_23481(ControlsUtil.isActuallyPressed(kb)));
		packetCooldown = 0;
		selectedLocation = class_2338.field_10980;

		if (inLectern())
			AllPackets.getChannel().sendToServer(new LinkedControllerStopLecternPacket(lecternPos));
		lecternPos = null;

		if (!currentlyPressed.isEmpty())
			AllPackets.getChannel().sendToServer(new LinkedControllerInputPacket(currentlyPressed, false));
		currentlyPressed.clear();

		LinkedControllerItemRenderer.resetButtons();
	}

	public static void tick() {
		LinkedControllerItemRenderer.tick();

		if (MODE == Mode.IDLE)
			return;
		if (packetCooldown > 0)
			packetCooldown--;

		class_310 mc = class_310.method_1551();
		class_746 player = mc.field_1724;
		class_1799 heldItem = player.method_6047();

		if (player.method_7325()) {
			MODE = Mode.IDLE;
			onReset();
			return;
		}

		if (!inLectern() && !AllItems.LINKED_CONTROLLER.isIn(heldItem)) {
			heldItem = player.method_6079();
			if (!AllItems.LINKED_CONTROLLER.isIn(heldItem)) {
				MODE = Mode.IDLE;
				onReset();
				return;
			}
		}

		if (inLectern() && AllBlocks.LECTERN_CONTROLLER.get()
			.getBlockEntityOptional(mc.field_1687, lecternPos)
			.map(be -> !be.isUsedBy(mc.field_1724))
			.orElse(true)) {
			deactivateInLectern();
			return;
		}

		if (mc.field_1755 != null) {
			MODE = Mode.IDLE;
			onReset();
			return;
		}

		if (class_3675.method_15987(mc.method_22683()
			.method_4490(), GLFW.GLFW_KEY_ESCAPE)) {
			MODE = Mode.IDLE;
			onReset();
			return;
		}

		Vector<class_304> controls = ControlsUtil.getControls();
		Collection<Integer> pressedKeys = new HashSet<>();
		for (int i = 0; i < controls.size(); i++) {
			if (ControlsUtil.isActuallyPressed(controls.get(i)))
				pressedKeys.add(i);
		}

		Collection<Integer> newKeys = new HashSet<>(pressedKeys);
		Collection<Integer> releasedKeys = currentlyPressed;
		newKeys.removeAll(releasedKeys);
		releasedKeys.removeAll(pressedKeys);

		if (MODE == Mode.ACTIVE) {
			// Released Keys
			if (!releasedKeys.isEmpty()) {
				AllPackets.getChannel().sendToServer(new LinkedControllerInputPacket(releasedKeys, false, lecternPos));
				AllSoundEvents.CONTROLLER_CLICK.playAt(player.method_37908(), player.method_24515(), 1f, .5f, true);
			}

			// Newly Pressed Keys
			if (!newKeys.isEmpty()) {
				AllPackets.getChannel().sendToServer(new LinkedControllerInputPacket(newKeys, true, lecternPos));
				packetCooldown = PACKET_RATE;
				AllSoundEvents.CONTROLLER_CLICK.playAt(player.method_37908(), player.method_24515(), 1f, .75f, true);
			}

			// Keepalive Pressed Keys
			if (packetCooldown == 0) {
				if (!pressedKeys.isEmpty()) {
					AllPackets.getChannel().sendToServer(new LinkedControllerInputPacket(pressedKeys, true, lecternPos));
					packetCooldown = PACKET_RATE;
				}
			}
		}

		if (MODE == Mode.BIND) {
			class_265 shape = mc.field_1687.method_8320(selectedLocation)
				.method_26218(mc.field_1687, selectedLocation);
			if (!shape.method_1110())
				Outliner.getInstance().showAABB("controller", shape.method_1107()
						.method_996(selectedLocation))
					.colored(0xB73C2D)
					.lineWidth(1 / 16f);

			for (Integer integer : newKeys) {
				LinkBehaviour linkBehaviour = BlockEntityBehaviour.get(mc.field_1687, selectedLocation, LinkBehaviour.TYPE);
				if (linkBehaviour != null) {
					AllPackets.getChannel().sendToServer(new LinkedControllerBindPacket(integer, selectedLocation));
					CreateLang.translate("linked_controller.key_bound", controls.get(integer)
							.method_16007()
							.getString())
						.sendStatus(mc.field_1724);
				}
				MODE = Mode.IDLE;
				break;
			}
		}

		currentlyPressed = pressedKeys;
		controls.forEach(kb -> kb.method_23481(false));
	}

	public static void renderOverlay(class_332 graphics, float partialTicks, class_1041 window) {
		class_310 mc = class_310.method_1551();
		if (mc.field_1690.field_1842)
			return;
		if (MODE != Mode.BIND)
			return;

		class_4587 poseStack = graphics.method_51448();
		poseStack.method_22903();
		class_437 tooltipScreen = new class_437(class_5244.field_39003) {
		};
		tooltipScreen.method_25423(mc, window.method_4486(), window.method_4502());

		Object[] keys = new Object[6];
		Vector<class_304> controls = ControlsUtil.getControls();
		for (int i = 0; i < controls.size(); i++) {
			class_304 keyBinding = controls.get(i);
			keys[i] = keyBinding.method_16007()
				.getString();
		}

		List<class_2561> list = new ArrayList<>();
		list.add(CreateLang.translateDirect("linked_controller.bind_mode")
			.method_27692(class_124.field_1065));
		list.addAll(TooltipHelper.cutTextComponent(CreateLang.translateDirect("linked_controller.press_keybind", keys),
			Palette.ALL_GRAY));

		int width = 0;
		int height = list.size() * mc.field_1772.field_2000;
		for (class_2561 iTextComponent : list)
			width = Math.max(width, mc.field_1772.method_27525(iTextComponent));
		int x = (mc.method_22683().method_4486() / 3) - width / 2;
		int y = mc.method_22683().method_4502() - height - 24;

		// TODO
		graphics.method_51434(class_310.method_1551().field_1772, list, x, y);

		poseStack.method_22909();
	}

	public enum Mode {
		IDLE, ACTIVE, BIND
	}

}
