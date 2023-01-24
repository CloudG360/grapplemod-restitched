package com.yyon.grapplinghook.blockentity;

import com.yyon.grapplinghook.common.CommonSetup;
import com.yyon.grapplinghook.network.serverbound.GrappleModifierMessage;
import com.yyon.grapplinghook.registry.GrappleModBlockEntities;
import com.yyon.grapplinghook.util.GrappleCustomization;
import com.yyon.grapplinghook.util.GrappleCustomization.upgradeCategories;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class GrappleModifierBlockEntity extends BlockEntity {
	public HashMap<GrappleCustomization.upgradeCategories, Boolean> unlockedCategories = new HashMap<>();
	public GrappleCustomization customization;

	public GrappleModifierBlockEntity(BlockPos pos, BlockState state) {
		super(GrappleModBlockEntities.GRAPPLE_MODIFIER.get(), pos, state);
		this.customization = new GrappleCustomization();
	}

	public void unlockCategory(upgradeCategories category) {
		unlockedCategories.put(category, true);
		this.sendUpdates();
		this.getLevel().sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
	}

	public void setCustomizationClient(GrappleCustomization customization) {
		this.customization = customization;
		CommonSetup.network.sendToServer(new GrappleModifierMessage(this.worldPosition, this.customization));
		this.sendUpdates();
	}

	public void setCustomizationServer(GrappleCustomization customization) {
		this.customization = customization;
		this.sendUpdates();
	}

	private void sendUpdates() {
		this.setChanged();
	}

	public boolean isUnlocked(upgradeCategories category) {
		return this.unlockedCategories.containsKey(category) && this.unlockedCategories.get(category);
	}

	@Override
	public void saveAdditional(CompoundTag nbtTagCompound) {
		super.saveAdditional(nbtTagCompound);

		CompoundTag unlockedNBT = nbtTagCompound.getCompound("unlocked");

		for (GrappleCustomization.upgradeCategories category : GrappleCustomization.upgradeCategories.values()) {
			String num = String.valueOf(category.toInt());
			boolean unlocked = this.isUnlocked(category);

			unlockedNBT.putBoolean(num, unlocked);
		}

		nbtTagCompound.put("unlocked", unlockedNBT);
		nbtTagCompound.put("customization", this.customization.writeNBT());
	}

	@Override
	public void load(CompoundTag parentNBTTagCompound) {
		super.load(parentNBTTagCompound); // The super call is required to load the tiles location

		CompoundTag unlockedNBT = parentNBTTagCompound.getCompound("unlocked");

		for (GrappleCustomization.upgradeCategories category : GrappleCustomization.upgradeCategories.values()) {
			String num = String.valueOf(category.toInt());
			boolean unlocked = unlockedNBT.getBoolean(num);

			this.unlockedCategories.put(category, unlocked);
		}

		CompoundTag custom = parentNBTTagCompound.getCompound("customization");
		this.customization.loadNBT(custom);
	}


	// When the world loads from disk, the server needs to send the TileEntity information to the client
	//  it uses getUpdatePacket(), getUpdateTag(), onDataPacket(), and handleUpdateTag() to do this:
	//  getUpdatePacket() and onDataPacket() are used for one-at-a-time TileEntity updates
	//  getUpdateTag() and handleUpdateTag() are used by vanilla to collate together into a single chunk update packet
	//  Not really required for this example since we only use the timer on the client, but included anyway for illustration
	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		CompoundTag nbtTagCompound = new CompoundTag();
		this.saveAdditional(nbtTagCompound);
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
		BlockState blockState = this.level.getBlockState(this.worldPosition);
		this.load(pkt.getTag());   // read from the nbt in the packet
	}

	/* Creates a tag containing all of the TileEntity information, used by vanilla to transmit from server to client */
	@Override
	@NotNull
	public CompoundTag getUpdateTag()
	{
		CompoundTag nbtTagCompound = new CompoundTag();
		this.saveAdditional(nbtTagCompound);
		return nbtTagCompound;
	}

	@Override
	public void handleUpdateTag(CompoundTag tag) {
		this.load(tag);
	}
}
