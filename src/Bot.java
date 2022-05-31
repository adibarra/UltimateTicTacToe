import java.util.ArrayList;

public class Bot {
	
	Logic logic = null;
	Move move = null;
	
	ArrayList<GameTile> tiles = new ArrayList<GameTile>();
	SmallBoard tempBoard = null;
	GameTile tempTile = null;
	
	Bot(Logic logic)
	{
		this.logic = logic;
		
		new Thread() 
		{
			public void run()
			{
				while(!Logic.gameOver)
				{
					if(Logic.botMove)
					{
						Logic.botMove = false;
						findMoves();
						chooseMove();
						executeMove();
					}
					Logic.delay(100);
				}
			}
		}.start();
	}
	
	public void findMoves()
	{
		tiles.clear();
		tempBoard = null;
		tempTile = null;
		
		//Build options
		for(int k = 0; k < Logic.bigBoard.size(); k++)//TODO handle all tiles case
		{
			if(!Logic.bigBoard.get(k).isDisabled())
			{
				tempBoard = Logic.bigBoard.get(k);
			}	
		}
		
		for(int k = 0; k < tempBoard.getBoard().size(); k++)
		{
			if(!tempBoard.getBoard().get(k).isFilled() && !tempBoard.isFinished())
			{
				tiles.add(tempBoard.getBoard().get(k));
			}	
		}
	}
	
	public void chooseMove()
	{			
		//Find optimal placement //TODO
		if(tiles.size() >= 1)
		{
			//Pick random move
			tempTile = tiles.get((int)(Math.random()*tiles.size()));
		}
		else tempTile = null;
		
		//Set move
		if(tempBoard != null && tempTile != null)
		{
			move = new Move(tempBoard,tempTile);
			if(Logic.debug4)
				System.out.println("Making Move: "+tempBoard.getPos()+" "+tempTile.getPos());
		}
		
	}
	
	public void executeMove()
	{
		if(move != null)
		{
			logic.botMove(move.getSb().getPos(),move.getGt().getPos());
		}
	}

	
	private class Move
	{
		private SmallBoard sb = null;
		private GameTile gt = null;
		
		Move(SmallBoard sb, GameTile gt)
		{
			this.sb = sb;
			this.gt = gt;
		}

		public SmallBoard getSb() {
			return sb;
		}

		public GameTile getGt() {
			return gt;
		}
		
	}
	
}


