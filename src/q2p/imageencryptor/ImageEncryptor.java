package q2p.imageencryptor;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public final class ImageEncryptor {
	public static void main(final String args[]) {
		final JFileChooser chooser = new JFileChooser();
		chooser.setMultiSelectionEnabled(false);
		chooser.setDialogTitle("Select image to encrypt");
		chooser.setApproveButtonText("Encrypt");
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) return;
		File inputFile = chooser.getSelectedFile();
		File outputFile;
		chooser.setDialogTitle("Save as...");
		chooser.setApproveButtonText("Save");
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		while(true) {
			if (chooser.showSaveDialog(null) != JFileChooser.APPROVE_OPTION) return;
			outputFile = chooser.getSelectedFile();
			if (!outputFile.exists()) break;
			if(JOptionPane.showConfirmDialog(null, "File already exists. Do you want to rewrite it?", "Rewrite", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) break;
		}
		
		BufferedImage image;
		Graphics g;
		int width;
		int height;
		
		try {
			BufferedImage inputImage = ImageIO.read(inputFile);
			image = new BufferedImage(inputImage.getWidth(), inputImage.getHeight(), BufferedImage.TYPE_INT_RGB);
			width = image.getWidth();
			height = image.getHeight();
			g = image.getGraphics();
			g.setColor(new Color(255, 255, 255));
			g.fillRect(0, 0, width, height);
			g.drawImage(inputImage, 0, 0, width, height, null);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Can't read image. Check if file is image or if it exists.", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		short min = 256;
		short max = -1;
		
		int buff;
		
		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				buff = image.getRGB(x, y);
				buff = (short)((((buff >> 16) & 0xff) + ((buff >> 8) & 0xff) + (buff & 0xff))/3);
				if(buff < min) min = (short)buff;
				else if(buff > max) max = (short)buff;
			}
		}
		
		short mid = (short)((float)(max-min)/2f+min);
		
		boolean[][] array = new boolean[image.getHeight()][image.getWidth()];
		
		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				buff = image.getRGB(x, y);
				buff = (short)((((buff >> 16) & 0xff) + ((buff >> 8) & 0xff) + (buff & 0xff))/3);
				array[y][x] = buff > mid;
			}
		}

		Random random = new Random();
		
		int[] cl = new int[3];
		boolean[] used;
		
		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {

				buff = array[y][x]?255:254;
				
				used = new boolean[]{false, false, false};
				
				for(byte i = 0; i != 3; i++) {
					byte f = (byte)random.nextInt(3-i);
					for(byte j = 0; j != 3; j++) {
						if(!used[j]) {
							if(f!=0) {
								f--;
							} else {
								used[j] = true;
								buff -= cl[j] = i==2?buff:(buff==0?0:random.nextInt(buff+1));
								break;
							}
						}
					}
				}
				
				g.setColor(new Color(cl[0], cl[1], cl[2]));
				g.fillRect(x, y, 1, 1);
			}
		}
		
		try {
			ImageIO.write(image, "png", outputFile);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Can't save image. Check if directory exists.", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		JOptionPane.showMessageDialog(null, "Encryption complete.", "Done", JOptionPane.INFORMATION_MESSAGE);
	}
}