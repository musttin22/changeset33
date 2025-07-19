package l2jorion.game.geo.geodriver.blocks;

import java.nio.ByteBuffer;

import l2jorion.game.geo.geodriver.Block;

public final class ComplexBlock implements Block
{
	private final short[] _data;
	
	public ComplexBlock(ByteBuffer bb)
	{
		_data = new short[Block.BLOCK_CELLS];
		for (int cellOffset = 0; cellOffset < Block.BLOCK_CELLS; cellOffset++)
		{
			_data[cellOffset] = bb.getShort();
		}
	}
	
	private short _getCellData(int geoX, int geoY)
	{
		return _data[((geoX % Block.BLOCK_CELLS_X) * Block.BLOCK_CELLS_Y) + (geoY % Block.BLOCK_CELLS_Y)];
	}
	
	private byte _getCellNSWE(int geoX, int geoY)
	{
		return (byte) (_getCellData(geoX, geoY) & 0x000F);
	}
	
	private int _getCellHeight(int geoX, int geoY)
	{
		short height = (short) (_getCellData(geoX, geoY) & 0x0FFF0);
		return height >> 1;
	}
	
	@Override
	public boolean checkNearestNswe(int geoX, int geoY, int worldZ, int nswe)
	{
		return (_getCellNSWE(geoX, geoY) & nswe) == nswe;
	}
	
	@Override
	public int getNearestZ(int geoX, int geoY, int worldZ)
	{
		return _getCellHeight(geoX, geoY);
	}
	
	@Override
	public int getNextLowerZ(int geoX, int geoY, int worldZ)
	{
		int cellHeight = _getCellHeight(geoX, geoY);
		return Math.min(cellHeight, worldZ);
	}
	
	@Override
	public int getNextHigherZ(int geoX, int geoY, int worldZ)
	{
		int cellHeight = _getCellHeight(geoX, geoY);
		return Math.max(cellHeight, worldZ);
	}
}