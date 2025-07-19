package l2jorion.game.geo.geodriver.regions;

import java.nio.ByteBuffer;

import l2jorion.game.geo.geodriver.Block;
import l2jorion.game.geo.geodriver.Region;
import l2jorion.game.geo.geodriver.blocks.ComplexBlock;
import l2jorion.game.geo.geodriver.blocks.FlatBlock;
import l2jorion.game.geo.geodriver.blocks.MultilayerBlock;

public final class NormalRegion implements Region
{
	private final Block[] _blocks = new Block[Region.REGION_BLOCKS];
	
	public NormalRegion(ByteBuffer bb)
	{
		for (int blockOffset = 0; blockOffset < Region.REGION_BLOCKS; blockOffset++)
		{
			int blockType = bb.get();
			switch (blockType)
			{
				case Block.TYPE_FLAT:
				{
					_blocks[blockOffset] = new FlatBlock(bb);
					break;
				}
				case Block.TYPE_COMPLEX:
				{
					_blocks[blockOffset] = new ComplexBlock(bb);
					break;
				}
				case Block.TYPE_MULTILAYER:
				{
					_blocks[blockOffset] = new MultilayerBlock(bb);
					break;
				}
				default:
				{
					throw new RuntimeException("Invalid block type " + blockType + "!");
				}
			}
		}
	}
	
	private Block getBlock(int geoX, int geoY)
	{
		return _blocks[(((geoX / Block.BLOCK_CELLS_X) % Region.REGION_BLOCKS_X) * Region.REGION_BLOCKS_Y) + ((geoY / Block.BLOCK_CELLS_Y) % Region.REGION_BLOCKS_Y)];
	}
	
	@Override
	public boolean checkNearestNswe(int geoX, int geoY, int worldZ, int nswe)
	{
		return getBlock(geoX, geoY).checkNearestNswe(geoX, geoY, worldZ, nswe);
	}
	
	@Override
	public int getNearestZ(int geoX, int geoY, int worldZ)
	{
		return getBlock(geoX, geoY).getNearestZ(geoX, geoY, worldZ);
	}
	
	@Override
	public int getNextLowerZ(int geoX, int geoY, int worldZ)
	{
		return getBlock(geoX, geoY).getNextLowerZ(geoX, geoY, worldZ);
	}
	
	@Override
	public int getNextHigherZ(int geoX, int geoY, int worldZ)
	{
		return getBlock(geoX, geoY).getNextHigherZ(geoX, geoY, worldZ);
	}
	
	@Override
	public boolean hasGeo()
	{
		return true;
	}
}