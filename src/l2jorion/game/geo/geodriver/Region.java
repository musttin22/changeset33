package l2jorion.game.geo.geodriver;

public interface Region
{
	/** Blocks in a region on the x-axis */
	int REGION_BLOCKS_X = 256;
	/** Blocks in a region on the y-axis */
	int REGION_BLOCKS_Y = 256;
	/** Blocks in a region */
	int REGION_BLOCKS = REGION_BLOCKS_X * REGION_BLOCKS_Y;
	
	/** Cells in a region on the x-axis */
	int REGION_CELLS_X = REGION_BLOCKS_X * Block.BLOCK_CELLS_X;
	/** Cells in a region on the y-axis */
	int REGION_CELLS_Y = REGION_BLOCKS_Y * Block.BLOCK_CELLS_Y;
	/** Cells in a region */
	int REGION_CELLS = REGION_CELLS_X * REGION_CELLS_Y;
	
	boolean checkNearestNswe(int geoX, int geoY, int worldZ, int nswe);
	
	int getNearestZ(int geoX, int geoY, int worldZ);
	
	int getNextLowerZ(int geoX, int geoY, int worldZ);
	
	int getNextHigherZ(int geoX, int geoY, int worldZ);
	
	boolean hasGeo();
}
