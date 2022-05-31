import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.JFrame;

public class GameEngine
{

	public static void main(String[] args)
    {
		String osName = System.getProperty("os.name").toLowerCase();
		if (osName.startsWith("mac os x")) 
		{
			//Makes Command+Q activate the windowClosing windowEvent
			System.setProperty("apple.eawt.quitStrategy","CLOSE_ALL_WINDOWS");
		}
    	
		Logic game = new Logic();
    	
		game.setMinimumSize(new Dimension(326+game.getInsets().left+game.getInsets().right,
				401+game.getInsets().top+game.getInsets().bottom));
		game.setVisible(true);
				
		game.addComponentListener(new ComponentListener() {
			public void componentResized(ComponentEvent e) {}
			public void componentMoved(ComponentEvent e) {}
			public void componentShown(ComponentEvent e) {}
			public void componentHidden(ComponentEvent e) {}
		});
		
		game.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e)
			{
				//TODO Client.stopClient();
				System.exit(0);
			}
		});
    }
}

@SuppressWarnings("serial")
class Logic extends JFrame implements MouseListener,MouseMotionListener{
	
	static ImageLoader il = new ImageLoader();
	static ArrayList<SmallBoard> bigBoard = new ArrayList<SmallBoard>();
	
    static RenderingHints rh;
	static BufferedImage offscreen = null;
    static Graphics g2;
	static Bot bot = null;
	
    static int mousex = 0;
    static int mousey = 0;
    static int clicks = 0;
	static int offsetx = 25;
	static int offsety = 25;
	static String turn = "O";
	static String player1 = "X";
	static String player2 = "O";
	static String display = turn;
	static String display2 = "";
	static String gameWinner = "";
	static Color bg = new Color(28,31,22);
	static Color fg = new Color(255,255,255,200);
	static Color hover = new Color(255,255,255,75);
	static Color disabled = new Color(0,0,0,75);
	static Color enabled = new Color(255,255,255,75);
	static String clientName = Long.toHexString(System.currentTimeMillis()*System.nanoTime()).substring(0,5);
	static boolean firstrun = true;
	static boolean gameOver = false;
	static boolean botMove = false;
	
	static boolean onePlayer = false;	//Toggle AI
	static boolean multiplayer = true;	//Toggle multiplayer

	static boolean debug = false;		//Number cells
	static boolean debug2 = false;		//Print board
	static boolean debug3 = false;		//Disable placement restrictions
	static boolean debug4 = false;		//Print AI placements
	
	Logic()
	{
		super("UltimateTicTacToe");
        setResizable(false);
        addMouseListener(this);
		addMouseMotionListener(this);
        rh = new RenderingHints(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
	}
	
	public void paint(Graphics g)
	{
		update(g);
	}
	
	public void setUp(Graphics g2)
	{		
		if(firstrun)
		{
			bigBoard.add(new SmallBoard(0,    offsetx,offsety));
			bigBoard.add(new SmallBoard(1,100+offsetx,offsety));
			bigBoard.add(new SmallBoard(2,200+offsetx,offsety));
			
			bigBoard.add(new SmallBoard(3,    offsetx,100+offsety));
			bigBoard.add(new SmallBoard(4,100+offsetx,100+offsety));
			bigBoard.add(new SmallBoard(5,200+offsetx,100+offsety));
			
			bigBoard.add(new SmallBoard(6,    offsetx,200+offsety));
			bigBoard.add(new SmallBoard(7,100+offsetx,200+offsety));
			bigBoard.add(new SmallBoard(8,200+offsetx,200+offsety));
			
			if(onePlayer && !multiplayer)
				bot = new Bot(this);
			
			//TODO Client.prepare();
			//TODO Client.start();
			
			firstrun = false;
		}
	}
	
	public void update(Graphics g)
	{
		offscreen = (BufferedImage)createImage(500,500);
		g2 = offscreen.getGraphics();
		g2.translate(0,22);
		((Graphics2D) g2).setRenderingHints(rh);
		
		setUp(g2);
		drawBoard(g2);
		drawText(g2);
		gameOver(g2);
		
		g.drawImage(offscreen,0,0,this);
	}
	
	public void gameOver(Graphics g2)
	{
		if(gameOver)
		{
			g2.setColor(disabled);
			g2.fillRect(0, 0, 500, 500);
			g2.setColor(fg);
			g2.setFont(new Font("Ariel",Font.BOLD,25));
			g2.drawString("Game Over", 100, 200);
			g2.drawString(gameWinner+" Wins!", 125, 225);
			
			for(int k = 0; k < bigBoard.size(); k++)
			{	
				bigBoard.get(k).setDisabled(true);
			}
		}
	}
	
	public void drawBoard(Graphics g2)
	{
		g2.setColor(bg);
		g2.fillRect(0, 0, 500, 500);
		
		g2.setColor(fg);
		
		g2.drawRect(0,   0, 325, 325);
		g2.drawRect(0, 325, 325,  53);
		
		g2.drawLine( 87+offsetx, offsety,  87+offsetx, 276+offsety);
		g2.drawLine(187+offsetx, offsety, 187+offsetx, 276+offsety);
		
		g2.drawLine(offsetx,  87+offsety, 276+offsetx,  87+offsety);
		g2.drawLine(offsetx, 187+offsety, 276+offsetx, 187+offsety);
		
		for(int k = 0; k < bigBoard.size(); k++)
		{				
			if(bigBoard.get(k).mouseOver() && !bigBoard.get(k).isDisabled())
			{
				g2.setColor(hover);
				g2.fillRect(bigBoard.get(k).getXx(),bigBoard.get(k).getYy(),77,77);
			}
			
			if(debug2)
			{
				g2.setColor(fg);
				g2.drawString(Integer.toString(k),bigBoard.get(k).getXx()-10,bigBoard.get(k).getYy()+18);//Debug
			}
			
			for(int j = 0; j < bigBoard.get(k).getBoard().size(); j++)
			{
				GameTile tempTile = bigBoard.get(k).getBoard().get(j);
				int x = tempTile.getX();
				int y = tempTile.getY();
				g2.setColor(fg);
				g2.drawRect(x,y,25,25);
				g2.drawImage(tempTile.image,x,y,this);
				
				if(debug)
				{
					g2.setColor(fg);
					g2.drawString(Integer.toString(j),x+8,y+18);//Debug
				}
				
				g2.drawImage(ImageLoader.getImage(bigBoard.get(k).getWinner()),bigBoard.get(k).getXx()-1,bigBoard.get(k).getYy()-1,77,77,this);
				
				if(bigBoard.get(k).getBoard().get(j).mouseOver() && !bigBoard.get(k).isDisabled() && !bigBoard.get(k).isFinished())
				{
					g2.setColor(hover);
					g2.fillRect(x,y,25,25);
				}
			}
			
			if(bigBoard.get(k).isDisabled() || bigBoard.get(k).isFinished())
			{
				g2.setColor(disabled);
				g2.fillRect(bigBoard.get(k).getXx(),bigBoard.get(k).getYy(),77,77);
			}
			else
			{
				g2.setColor(enabled);
				g2.fillRect(bigBoard.get(k).getXx(),bigBoard.get(k).getYy(),77,77);
			}
		}
	}
	
	public void drawText(Graphics g2)
	{
		if(turn.equals("X"))
			display = "O";
		else
			display = "X";
		g2.setColor(fg);
		g2.setFont(new Font("Ariel",Font.BOLD,12));
		g2.drawString(display+"'s Turn",25,340);
		g2.drawString(display2,25,355);
	}
	
	public static void checkBigBoard()
	{
		for(int k = 0; k < 3; k++)
		{
			//check horizontal
			int num0 = 0+k*3;
			int num1 = 1+k*3;
			int num2 = 2+k*3;
			
			if(bigBoard.get(num0).isFinished() && bigBoard.get(num1).isFinished() && bigBoard.get(num2).isFinished())
			{
				if(bigBoard.get(num0).getWinner().equals(bigBoard.get(num1).getWinner()) && bigBoard.get(num1).getWinner().equals(bigBoard.get(num2).getWinner()))
				{
					gameWinner = bigBoard.get(num0).getWinner();
					gameOver = true;
				}
			}
			
			//check vertical
			int num3 = 0+k;
			int num4 = 3+k;
			int num5 = 6+k;
			
			if(bigBoard.get(num3).isFinished() && bigBoard.get(num4).isFinished() && bigBoard.get(num5).isFinished())
			{
				if(bigBoard.get(num3).getWinner().equals(bigBoard.get(num4).getWinner()) && bigBoard.get(num4).getWinner().equals(bigBoard.get(num5).getWinner()))
				{
					gameWinner = bigBoard.get(num3).getWinner();
					gameOver = true;
				}
			}
		}
		
		//check diagonal /
		if(bigBoard.get(2).isFinished() && bigBoard.get(4).isFinished() && bigBoard.get(6).isFinished())
		{
			if(bigBoard.get(2).getWinner().equals(bigBoard.get(4).getWinner()) && bigBoard.get(4).getWinner().equals(bigBoard.get(6).getWinner()))
			{
				gameWinner = bigBoard.get(2).getWinner();
				gameOver = true;
			}
		}
		
		//check diagonal \
		if(bigBoard.get(0).isFinished() && bigBoard.get(4).isFinished() && bigBoard.get(8).isFinished())
		{
			if(bigBoard.get(0).getWinner().equals(bigBoard.get(4).getWinner()) && bigBoard.get(4).getWinner().equals(bigBoard.get(8).getWinner()))
			{
				gameWinner = bigBoard.get(0).getWinner();
				gameOver = true;
			}
		}
	}
	
	public GameTile findClickTile()
	{
		for(int k = 0; k < bigBoard.size(); k++)
		{	
			for(int j = 0; j < bigBoard.get(k).getBoard().size(); j++)
			{
				if(bigBoard.get(k).getBoard().get(j).mouseOver())
				{
					return bigBoard.get(k).getBoard().get(j);
				}
			}
		}
		return null;
	}
	
	public SmallBoard findClickBoard()
	{
		for(int k = 0; k < bigBoard.size(); k++)
		{	
			if(bigBoard.get(k).mouseOver())
			{
				return bigBoard.get(k);
			}
		}	
		return null;
	}
	
	public static void delay(long delay)
	{
		try {
			Thread.sleep(delay);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	
	public void botMove(int board, int pos)
	{	
		mousex = bigBoard.get(board).getBoard().get(pos).x+1;
		mousey = bigBoard.get(board).getBoard().get(pos).y+1;
		
		SmallBoard tempBoard = findClickBoard();
		GameTile tempTile = findClickTile();
		
		if(tempBoard != null)
		{
			if(!tempBoard.isFinished() && !tempBoard.isDisabled() && tempTile != null)
			{
				if(!tempTile.filled)
				{
					if(turn.equals("X"))
						turn = "O";
					else
						turn = "X";
					tempTile.image = ImageLoader.getImage(turn);
					tempTile.owner = turn;
					tempTile.filled = true;
					tempBoard.checkForWinner();
					tempBoard.checkForWinner();
					
					if(!debug3)
					{
						if(bigBoard.indexOf(tempBoard) == tempBoard.getBoard().indexOf(tempTile) || bigBoard.get(tempBoard.getBoard().indexOf(tempTile)).isFinished())
						{
							for(int k = 0; k < bigBoard.size(); k++)
							{	
								bigBoard.get(k).setDisabled(false);
							}
						}
						else
						{
							for(int k = 0; k < bigBoard.size(); k++)
							{	
								bigBoard.get(k).setDisabled(true);
							}
					
							bigBoard.get(tempBoard.getBoard().indexOf(tempTile)).setDisabled(false);
						}
					}
				}
			}
		}
		repaint();
	}

	public void mouseDragged(MouseEvent e) 
	{	
		mousex = e.getX();
		mousey = e.getY()-22;
		
		SmallBoard tempBoard = findClickBoard();
		GameTile tempTile = findClickTile();
		
		if(tempBoard != null && !botMove)
		{
			if(!tempBoard.isFinished() && !tempBoard.isDisabled() && tempTile != null)
			{
				if(!tempTile.filled)
				{
					if(turn.equals("X"))
						turn = "O";
					else
						turn = "X";
					tempTile.image = ImageLoader.getImage(turn);
					tempTile.owner = turn;
					tempTile.filled = true;
					tempBoard.checkForWinner();
					tempBoard.checkForWinner();
					
					if(!debug3)
					{
						if(bigBoard.indexOf(tempBoard) == tempBoard.getBoard().indexOf(tempTile) || bigBoard.get(tempBoard.getBoard().indexOf(tempTile)).isFinished())
						{
							for(int k = 0; k < bigBoard.size(); k++)
							{	
								bigBoard.get(k).setDisabled(false);
							}
						}
						else
						{
							for(int k = 0; k < bigBoard.size(); k++)
							{	
								bigBoard.get(k).setDisabled(true);
							}
					
							bigBoard.get(tempBoard.getBoard().indexOf(tempTile)).setDisabled(false);
						}
					}
					//botMove = true;	
				}
			}
		}
		repaint();
	}

	public void mouseMoved(MouseEvent e) 
	{
		mousex = e.getX();
		mousey = e.getY()-22;
		repaint();
	}

	public void mousePressed(MouseEvent e) 
	{
		mouseDragged(e);
	}
	
	public void mouseClicked(MouseEvent e) {}

	public void mouseReleased(MouseEvent e) {}

	public void mouseEntered(MouseEvent e) {}

	public void mouseExited(MouseEvent e) {}
}

class SmallBoard
{
	private ArrayList<GameTile> board = new ArrayList<GameTile>();
	private int xx = 0;
	private int yy = 0;
	private int pos = -1;
	private String winner = "None";
	private boolean finished = false;
	private boolean disabled = false;
	
	SmallBoard(int pos, int x, int y)
	{	
		this.pos = pos;
		xx = x;
		yy = y;
		
		genBoard();
	}
	
	public void genBoard()
	{	
		for(int k = 0; k < 9; k++)
		{
			int x = xx + 25 * (k % 3);
			int y = yy + 25 * (k / 3);
			
			board.add(new GameTile(k,x,y));
		}
	}
	
	public void checkForWinner()
	{
		for(int k = 0; k < 3; k++)
		{
			//check horizontal
			int num0 = 0+k*3;
			int num1 = 1+k*3;
			int num2 = 2+k*3;
			
			if(getBoard().get(num0).filled && getBoard().get(num1).filled && getBoard().get(num2).filled && !finished)
			{
				if(getBoard().get(num0).owner.equals(getBoard().get(num1).owner) && getBoard().get(num1).owner.equals(getBoard().get(num2).owner))
				{
					winner = getBoard().get(num0).owner;
					finished = true;
					return;
				}
			}
			
			//check vertical
			int num3 = 0+k;
			int num4 = 3+k;
			int num5 = 6+k;
			
			if(getBoard().get(num3).filled && getBoard().get(num4).filled && getBoard().get(num5).filled && !finished)
			{
				if(getBoard().get(num3).owner.equals(getBoard().get(num4).owner) && getBoard().get(num4).owner.equals(getBoard().get(num5).owner))
				{
					winner = getBoard().get(num3).owner;
					finished = true;
					return;
				}
			}
		}
		
		//check diagonal /
		if(getBoard().get(2).filled && getBoard().get(4).filled && getBoard().get(6).filled && !finished)
		{
			if(getBoard().get(2).owner.equals(getBoard().get(4).owner) && getBoard().get(4).owner.equals(getBoard().get(6).owner))
			{
				winner = getBoard().get(2).owner;
				finished = true;
				return;
			}
		}
		
		//check diagonal \
		if(getBoard().get(0).filled && getBoard().get(4).filled && getBoard().get(8).filled && !finished)
		{
			if(getBoard().get(0).owner.equals(getBoard().get(4).owner) && getBoard().get(4).owner.equals(getBoard().get(8).owner))
			{
				winner = getBoard().get(0).owner;
				finished = true;
				return;
			}
		}
		
		Logic.checkBigBoard();
	}
	
	public int getPos() {
		return pos;
	}

	public void setPos(int pos) {
		this.pos = pos;
	}

	public int getXx() {
		return xx;
	}

	public void setXx(int xx) {
		this.xx = xx;
	}

	public int getYy() {
		return yy;
	}

	public void setYy(int yy) {
		this.yy = yy;
	}

	public String getWinner() {
		return winner;
	}

	public void setWinner(String winner) {
		this.winner = winner;
	}

	public boolean isFinished() {
		return finished;
	}

	public void setFinished(boolean finished) {
		this.finished = finished;
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	public void setBoard(ArrayList<GameTile> board) {
		this.board = board;
	}

	public boolean mouseOver()
	{
		return new Rectangle(xx,yy,77,77).contains(Logic.mousex,Logic.mousey);
	}
	
	public ArrayList<GameTile> getBoard()
	{
		return board;
	}
}

class GameTile
{
	int pos = -1;
	int x = 0;
	int y = 0;
	BufferedImage image = null;
	String owner = "None";
	boolean filled = false;
	
	GameTile(int pos, int x, int y)
	{
		this.pos = pos;
		this.x = x;
		this.y = y;
	}
	
	public int getPos() {
		return pos;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}
	
	public boolean mouseOver()
	{
		return new Rectangle(x,y,25,25).contains(Logic.mousex,Logic.mousey);
	}
	
	public BufferedImage getImage() {
		return image;
	}

	public void setImage(BufferedImage image) {
		this.image = image;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public boolean isFilled() {
		return filled;
	}
	
	public void setPos(int pos) {
		this.pos = pos;
	}

	public void setFilled(boolean filled) {
		this.filled = filled;
	}

	public void setX(int x) {
		this.x = x;
	}

	public void setY(int y) {
		this.y = y;
	}

	public String toString()
	{
		return owner;
	}
}
