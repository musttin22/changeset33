package l2jorion.game.geo.geodriver;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReferenceArray;

import l2jorion.game.geo.geodriver.regions.NormalRegion;
import l2jorion.game.geo.geodriver.regions.NullRegion;

public final class GeoDriver
{
	
	private static final int WORLD_MIN_X = -655360;
	private static final int WORLD_MAX_X = 393215;
	private static final int WORLD_MIN_Y = -589824;
	private static final int WORLD_MAX_Y = 458751;
	
	/** Regions in the world on the x-axis */
	public static final int GEO_REGIONS_X = 32;
	/** Regions in the world on the y-axis */
	public static final int GEO_REGIONS_Y = 32;
	/** Region in the world */
	public static final int GEO_REGIONS = GEO_REGIONS_X * GEO_REGIONS_Y;
	
	/** Blocks in the world on the x-axis */
	public static final int GEO_BLOCKS_X = GEO_REGIONS_X * Region.REGION_BLOCKS_X;
	/** Blocks in the world on the y-axis */
	public static final int GEO_BLOCKS_Y = GEO_REGIONS_Y * Region.REGION_BLOCKS_Y;
	/** Blocks in the world */
	public static final int GEO_BLOCKS = GEO_REGIONS * Region.REGION_BLOCKS;
	
	/** Cells in the world on the x-axis */
	public static final int GEO_CELLS_X = GEO_BLOCKS_X * Block.BLOCK_CELLS_X;
	/** Cells in the world in the y-axis */
	public static final int GEO_CELLS_Y = GEO_BLOCKS_Y * Block.BLOCK_CELLS_Y;
	
	/** The regions array */
	private final AtomicReferenceArray<Region> _regions = new AtomicReferenceArray<>(GEO_REGIONS);
	
	public GeoDriver()
	{
		for (int i = 0; i < _regions.length(); i++)
		{
			_regions.set(i, NullRegion.INSTANCE);
		}
	}
	
	private void checkGeoX(int geoX)
	{
		if ((geoX < 0) || (geoX >= GEO_CELLS_X))
		{
			throw new IllegalArgumentException();
		}
	}
	
	private void checkGeoY(int geoY)
	{
		if ((geoY < 0) || (geoY >= GEO_CELLS_Y))
		{
			throw new IllegalArgumentException();
		}
	}
	
	private Region getRegion(int geoX, int geoY)
	{
		checkGeoX(geoX);
		checkGeoY(geoY);
		return _regions.get(((geoX / Region.REGION_CELLS_X) * GEO_REGIONS_Y) + (geoY / Region.REGION_CELLS_Y));
	}
	
	public void loadRegion(Path filePath, int regionX, int regionY) throws IOException
	{
		final int regionOffset = (regionX * GEO_REGIONS_Y) + regionY;
		
		try (RandomAccessFile raf = new RandomAccessFile(filePath.toFile(), "r"))
		{
			_regions.set(regionOffset, new NormalRegion(raf.getChannel().map(MapMode.READ_ONLY, 0, raf.length()).order(ByteOrder.LITTLE_ENDIAN)));
		}
	}
	
	public void unloadRegion(int regionX, int regionY)
	{
		_regions.set((regionX * GEO_REGIONS_Y) + regionY, NullRegion.INSTANCE);
	}
	
	public boolean hasGeoPos(int geoX, int geoY)
	{
		return getRegion(geoX, geoY).hasGeo();
	}
	
	public boolean checkNearestNswe(int geoX, int geoY, int worldZ, int nswe)
	{
		return getRegion(geoX, geoY).checkNearestNswe(geoX, geoY, worldZ, nswe);
	}
	
	public int getNearestZ(int geoX, int geoY, int worldZ)
	{
		return getRegion(geoX, geoY).getNearestZ(geoX, geoY, worldZ);
	}
	
	public int getNextLowerZ(int geoX, int geoY, int worldZ)
	{
		return getRegion(geoX, geoY).getNextLowerZ(geoX, geoY, worldZ);
	}
	
	public int getNextHigherZ(int geoX, int geoY, int worldZ)
	{
		return getRegion(geoX, geoY).getNextHigherZ(geoX, geoY, worldZ);
	}
	
	public int getGeoX(int worldX)
	{
		if ((worldX < WORLD_MIN_X) || (worldX > WORLD_MAX_X))
		{
			throw new IllegalArgumentException();
		}
		return (worldX - WORLD_MIN_X) / 16;
	}
	
	public int getGeoY(int worldY)
	{
		if ((worldY < WORLD_MIN_Y) || (worldY > WORLD_MAX_Y))
		{
			throw new IllegalArgumentException();
		}
		return (worldY - WORLD_MIN_Y) / 16;
	}
	
	public int getWorldX(int geoX)
	{
		checkGeoX(geoX);
		return (geoX * 16) + WORLD_MIN_X + 8;
	}
	
	public int getWorldY(int geoY)
	{
		checkGeoY(geoY);
		return (geoY * 16) + WORLD_MIN_Y + 8;
	}
}
