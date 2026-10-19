package com.simibubi.create.content.redstone.link;

import com.simibubi.create.content.redstone.link.RedstoneLinkNetworkHandler.Frequency;

import net.createmod.catnip.data.Couple;
import net.minecraft.class_2338;

public interface IRedstoneLinkable {

	public int getTransmittedStrength();

	public void setReceivedStrength(int power);

	public boolean isListening();

	public boolean isAlive();

	public Couple<Frequency> getNetworkKey();

	public class_2338 getLocation();

}
