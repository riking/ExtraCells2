package extracells.part;

import appeng.api.config.RedstoneMode;
import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartCollsionHelper;
import appeng.api.parts.IPartRenderHelper;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import extracells.container.ContainerBusIOFluid;
import extracells.gui.GuiBusIOFluid;
import extracells.network.packet.PacketBusIOFluid;
import extracells.util.FluidMode;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

public abstract class PartFluidIO extends PartECBase implements IGridTickable
{
	protected Fluid[] filterFluids = new Fluid[8];
	private RedstoneMode redstoneMode = RedstoneMode.IGNORE;
	private FluidMode fluidMode = FluidMode.DROPS;

	@Override
	public abstract void renderInventory(IPartRenderHelper rh, RenderBlocks renderer);

	@Override
	public abstract void renderStatic(int x, int y, int z, IPartRenderHelper rh, RenderBlocks renderer);

	@Override
	public final void renderDynamic(double x, double y, double z, IPartRenderHelper rh, RenderBlocks renderer)
	{
	}

	@Override
	public final void writeToNBT(NBTTagCompound data)
	{
		data.setInteger("fluidMode", fluidMode.ordinal());
		data.setInteger("redstoneMode", redstoneMode.ordinal());
	}

	@Override
	public final void readFromNBT(NBTTagCompound data)
	{
		redstoneMode = RedstoneMode.values()[data.getInteger("redstoneMode")];
		fluidMode = FluidMode.values()[data.getInteger("fluidMode")];
	}

	@Override
	public final void writeToStream(DataOutputStream data) throws IOException
	{
	}

	@Override
	public final boolean readFromStream(DataInputStream data) throws IOException
	{
		return false;
	}

	@Override
	public abstract void getBoxes(IPartCollsionHelper bch);

	@Override
	public int cableConnectionRenderTo()
	{
		return 5;
	}

	@Override
	public final TickingRequest getTickingRequest(IGridNode node)
	{
		return new TickingRequest(1, 20, false, false);
	}

	@Override
	public final TickRateModulation tickingRequest(IGridNode node, int TicksSinceLastCall)
	{
		return doWork(250, TicksSinceLastCall) ? TickRateModulation.FASTER : TickRateModulation.SLOWER;
	}

	public abstract boolean doWork(int rate, int TicksSinceLastCall);

	public final void setFilterFluid(int index, Fluid fluid, EntityPlayer player)
	{
		filterFluids[index] = fluid;
		PacketDispatcher.sendPacketToPlayer(new PacketBusIOFluid(Arrays.asList(filterFluids)).makePacket(), (Player) player);
	}

	public FluidMode getFluidMode()
	{
		return fluidMode;
	}

	public RedstoneMode getRedstoneMode()
	{
		return redstoneMode;
	}

	public void loopRedstoneMode(EntityPlayer player)
	{
		if (redstoneMode.ordinal() + 1 < RedstoneMode.values().length)
			redstoneMode = RedstoneMode.values()[redstoneMode.ordinal() + 1];
		else
			redstoneMode = RedstoneMode.values()[0];
		PacketDispatcher.sendPacketToPlayer(new PacketBusIOFluid((byte) 0, (byte) redstoneMode.ordinal()).makePacket(), (Player) player);
	}

	public void loopFluidMode(EntityPlayer player)
	{
		if (fluidMode.ordinal() + 1 < FluidMode.values().length)
			fluidMode = FluidMode.values()[fluidMode.ordinal() + 1];
		else
			fluidMode = FluidMode.values()[0];
		PacketDispatcher.sendPacketToPlayer(new PacketBusIOFluid((byte) 1, (byte) fluidMode.ordinal()).makePacket(), (Player) player);
	}

	public Object getServerGuiElement(EntityPlayer player)
	{
		return new ContainerBusIOFluid(this, player);
	}

	public Object getClientGuiElement(EntityPlayer player)
	{
		return new GuiBusIOFluid(this, player);
	}
}
