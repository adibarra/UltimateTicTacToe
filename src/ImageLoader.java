import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

/**
 * The ImageLoader Class provides a quick an easy way to import and use images anywhere.
 * @author Alec Ibarra
 */
public class ImageLoader {
	
	private static ArrayList<GameIcon> icons = new ArrayList<GameIcon>();
	
	/**
	 * Loads all of the images into memory
	 */
	public ImageLoader()
	{	
		icons.add(new GameIcon("BigBoard"));
		icons.add(new GameIcon("Board"));
		icons.add(new GameIcon("O"));
		icons.add(new GameIcon("X"));
		icons.add(new GameIcon("None"));
	}
	
	/**
	 * Used to get image by name
	 * @return BufferedImage
	 */
	public static BufferedImage getImage(String imageName)
	{
		for(int k = 0; k < icons.size(); k++)
		{
			if(icons.get(k).getIconName().equalsIgnoreCase(imageName))
			{
				return icons.get(k).getIcon();
			}
		}
		
		return getImage("noTexture");
	}

	/**
	 * Used by the ImageLoader Class. Used to keep a look-up table of every image and its name.
	 */
	private class GameIcon
	{
		private BufferedImage icon;
		private String iconName;
		
		public GameIcon(String iconName)
		{
			try
			{
				setIconName(iconName);
				setIcon(ImageIO.read(getClass().getClassLoader().getResource(iconName+".png")));
				
			} catch (IOException | IllegalArgumentException e)
			{
				System.out.println("Failed to load: "+iconName+".png");
			}
		}

		public BufferedImage getIcon() {
			return icon;
		}

		public void setIcon(BufferedImage icon) {
			this.icon = icon;
		}

		public String getIconName() {
			return iconName;
		}

		public void setIconName(String iconName) {
			this.iconName = iconName;
		}
	}
}