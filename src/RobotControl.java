/*ALGORITHM
 * BEGIN
 * 
 *COMPUTE the sum of all block sizes and store in variable blockSum
 * 
 *WHILE blockSum IS NOT EQUAL to 0
 * 
 *      COMPUTE each column size in an array of size 10.
 *
 *      MOVE to the blocks
 *           WHILE moving, check column ahead and adjust height, depth and width accordingly
 *
 *      LOWER depth to appropriate level to pick up a block               
 *
 *      PICKUP block
 * 
 *      COMPUTE block size that was picked up
 * 
 *      MOVE the block size to its appropriate destination
 *           WHILE moving, check the column behind and adjust height, depth and width accordingly
 *
 *      LOWER block down to appropriate column size level
 * 
 *      DROP block
 *
 *      COMPUTE blockSum
 *
 *END WHILE LOOP
 * 
 * END
 */

class RobotControl
{
	private Robot r;
	public static StringBuilder sb;

	// Examples of constants which values never change.
	private final int SOURCE_LOCATION = 10;
	private final int TARGET_1 = 1;
	private final int TARGET_2 = 2;
	private final int FIRST_BAR_POSITION = 3;
	
	private int h = 2; // Initial height of arm 1
    private int w = 1; // Initial width of arm 2
	private int d = 0; // Initial depth of arm 3
	
	private int oneBlocksPlaced = 0;
	private int twoBlocksPlaced = 0;
	private int threeBlocksPlaced = 0; // Calculates the blocks placed for each block of size: 1, 2 and 3.
	
	private boolean isPickedUp = false; //Boolean for checking whether a block is picked up or not.
	
	private int blockSum = 0;
	private int[] columnSize = new int[10];

	public RobotControl(Robot r)
	{
		this.r = r;
	}
	
	public void control(int barHeights[], int blockHeights[])
	{
		run(barHeights, blockHeights);
	}

	public void run(int barHeights[], int blockHeights[])
	{
		blockSum = calculateBlockSum(blockHeights, blockSum); // initial calculation of total block sizes needed to move.
		
		while(blockSum != 0) //Runs through an iteration until the blocks are all placed
		{
			columnSize = calculateColumnSize(barHeights, columnSize);
			int blockPickedUpSize = 0; //calculates the block size when the robot picks up a block.
			
			moveToBlocks();
			
			lowerDepth(blockSum);
			
			pickUpBlock();
			
			blockPickedUpSize = calculateBlockUpPickedSize(blockHeights); //Calculates the size of a block after picking it up
			
			moveToDestination(blockPickedUpSize);
			
			lowerBlockDown(blockPickedUpSize);
			
			dropBlock(blockPickedUpSize);
		}
	}
	
	private int calculateBlockSum(int blockHeights[], int blockSum) //Function that calculates the total block sizes 
	{
		//Adds up the contents of the array in blockHeights and stores it in blockSum
		for(int i = 0; i < blockHeights.length; i++) 
		{
			blockSum += blockHeights[i];
		}
		return blockSum;
	}
	
	private int[] calculateColumnSize(int[] barHeights, int[] columnSize)
	{
		columnSize[0] = oneBlocksPlaced; // First column size will always be the amount of one blocks placed.
		columnSize[1] = twoBlocksPlaced * 2; // second column size will always be the amount of two blocks placed multiplied by 2.
		columnSize[9] = blockSum; // last column size will always be the sum of blocks required to move.
		

		for(int i = 0; i < barHeights.length; i++)
		{
			// Calculates the initial column sizes
			if(threeBlocksPlaced == 0)
			{
				columnSize[i + 2] = barHeights[i];
			}
			// Calculates column sizes after a three block is placed.
			else if(i - 1 == threeBlocksPlaced)
			{
				if(columnSize[i] == barHeights[i - 2])
				{
					columnSize[i] = columnSize[i] + 3;
				}
			}
		}
		
		return columnSize;
	}
	private void moveToBlocks()
	{
		//Runs until the robot's width is at the location of the blocks.
		while(w < SOURCE_LOCATION) 
		{
			//Runs an iteration checking each move and column before moving the robot forwards.
			for(int i = w; i < columnSize.length; i++)
			{
				checkColumnWhileMoving(0, i);
			}
		}
	}
	private void lowerDepth(int blockSum)
	{
		while(h - 1 != blockSum + d) //keeps iterating until the depth + block sum equals the height.
		{
			r.lower();
			d++;
		}
	}
	private void pickUpBlock()
	{
		r.pick();
		isPickedUp = true;
	}
	private int calculateBlockUpPickedSize(int blockHeights[]) 
	{
		//Calculates amount of blocks placed
		int blocksPickedAndPlaced = oneBlocksPlaced + twoBlocksPlaced + threeBlocksPlaced;
		int blockPickedSize = 0;
		
		/*The block picked size is calculated by indexing through the BlockHeights[] array
		 *from the end minus the amount of blocks placed. */
		blockPickedSize = blockHeights[blockHeights.length - 1 - blocksPickedAndPlaced];
		return blockPickedSize;
		
	}
	private void moveToDestination(int blockPickedSize)
	{
		//Switch statement to move each block size to its allocated position
		switch(blockPickedSize) 
		{
		case 1: //Blocks of size 1 will go to column TARGET_1
			for(int i = 10;
					i != TARGET_1; 
					i--)
			{
				//This method checks the column sizes while moving to prevent collisions.
				checkColumnWhileMoving(blockPickedSize, i);
			}
			break;
			
		case 2: //Blocks of size 2 will go to column TARGET_2
			for(int i = 10;
					i != TARGET_2;
					i--)
			{
				checkColumnWhileMoving(blockPickedSize, i);
			}
			break;
			
		case 3:		
			for(int i = 10;
					/* Blocks of size 3 will go to the FIRST_BAR_POSITION followed by the amount of
					 * blocks of size 3 that are placed */
					i != FIRST_BAR_POSITION + threeBlocksPlaced; 
					i--)
			{
				checkColumnWhileMoving(blockPickedSize, i);
			}
		break;	
		}
	}
	private void checkColumnWhileMoving(int blockPickedSize, int i)
	{
		int index = 0;
		/*The index checks whether the column ahead or behind
		 *should be checked based on if you are moving forward
		 *or backwards. */
		if(isPickedUp) 
		{
			index = 2;
		}
		
		//Checks the columns and moves accordingly to the column sizes
		while(columnSize[i - index] >= h - d - blockPickedSize)
		{
			if(d > 0)
			{
				r.raise();
				d--;
			}
			else 
			{
				r.up();
				h++;
			}
		}
		
		//Moves forward or back depending if its returning or picking up a block.
		if(isPickedUp)
		{
			r.contract();
			w--;	
		}
		else 
		{
			r.extend();;
			w++;
		}
	}
	private void lowerBlockDown(int blockPickedSize)
	{
		//Moves block down until it reaches the column size.
		while(d + blockPickedSize + columnSize[w - 1] + 1 < h)
		{
			r.lower();
			d++;
		}
		
		//Increments the blocks placed once it moves it down successfully.
		switch(blockPickedSize)
		{
		case 1:
			oneBlocksPlaced++;
			break;
		case 2:
			twoBlocksPlaced++;
			break;
		case 3:
			threeBlocksPlaced++;
			break;
		}
	}
	
	private void dropBlock(int blockPickedSize)
	{
		r.drop();
		isPickedUp = false;
		
		/*Recalculates the block sum by removing
		 *the block that was picked up and placed. */
		blockSum = blockSum - blockPickedSize;
	}
	
}
