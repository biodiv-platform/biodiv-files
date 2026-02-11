package com.strandls.file.util;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThumbnailUtil implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(ThumbnailUtil.class);

	private static final int GALLERY_WIDTH = 500;
	private static final int GALLERY_HEIGHT = 300;
	private static final int GALLERY_TH_WIDTH = 50;
	private static final int GALLERY_TH_HEIGHT = 50;

	private static final int TH1_WIDTH = 200;
	private static final int TH1_HEIGHT = 200;
	private static final int TH2_WIDTH = 320;
	private static final int TH2_HEIGHT = 320;

	private final String filePath;
	private final String dirPath;
	private final String filename;
	private final String extension;

	public ThumbnailUtil(String filePath, String dirPath, String filename, String extension) {
		this.filePath = filePath;
		this.dirPath = dirPath;
		this.filename = filename;
		this.extension = extension;
	}

	@Override
	public void run() {
		logger.info("[THUMBNAIL] Starting thumbnail generation for: {}", filePath);
		try {
			generateMultipleFiles(filePath, dirPath, filename, extension);
			logger.info("[THUMBNAIL] Successfully generated thumbnails for: {}", filename);
		} catch (IOException e) {
			logger.error("[THUMBNAIL] Failed to generate thumbnails for: " + filePath, e);
		}
	}

	private static void generateMultipleFiles(String filePath, String dirPath, String filename, String extension)
			throws IOException {
		logger.info("[THUMBNAIL] Reading image file: {}", filePath);
		File file = new File(filePath);

		BufferedImage image = ImageIO.read(file);

		// If unable to parse the image file
		if (image == null) {
			logger.error("[THUMBNAIL] Unable to read image file (ImageIO.read returned null): {}", filePath);
			return;
		}

		logger.info("[THUMBNAIL] Image loaded successfully. Dimensions: {}x{}, Format: {}",
					image.getWidth(), image.getHeight(), extension);

		// Generate the gallery image.
		logger.info("[THUMBNAIL] Generating gallery image ({}x{})", GALLERY_WIDTH, GALLERY_HEIGHT);
		generateGallaryImage(image, dirPath, filename, extension);

		// crop the image to square size
		int h = image.getHeight();
		int w = image.getWidth();
		int x = 0;
		int y = 0;
		if (w > h) {
			x = (w - h) / 2;
			w = h;
		} else if (h > w) {
			y = (h - w) / 2;
			h = w;
		}
		logger.info("[THUMBNAIL] Cropping image to square: {}x{} at position ({},{})", w, h, x, y);
		BufferedImage cropedImage = image.getSubimage(x, y, w, h);

		// Generate Thumb nail images
		logger.info("[THUMBNAIL] Generating thumbnail variants");
		generateGalleryThumbnailImage(cropedImage, dirPath, filename, extension);
		generateThumbnail1Image(cropedImage, dirPath, filename, extension);
		generateThumbnail2Image(cropedImage, dirPath, filename, extension);
		logger.info("[THUMBNAIL] All thumbnail variants generated successfully");
	}

	private static void generateGallaryImage(BufferedImage image, String dirPath, String filename, String extension)
			throws IOException {
		int h = image.getHeight();
		int w = image.getWidth();
		double hRatio = ((double) h) / GALLERY_HEIGHT;
		double wRatio = ((double) w) / GALLERY_WIDTH;
		int gall_w;
		int gall_h;
		if (hRatio > wRatio) {
			gall_w = (int) (w / hRatio);
			gall_h = GALLERY_HEIGHT;
		} else {
			gall_w = GALLERY_WIDTH;
			gall_h = (int) (h / wRatio);
		}
		BufferedImage galleryImage = getScaledImage(image, gall_w, gall_h);
		File output = new File(dirPath + File.separatorChar + filename + "_gall." + extension);
		ImageIO.write(galleryImage, extension, output);
	}

	private static void generateGalleryThumbnailImage(BufferedImage cropedImage, String dirPath, String filename,
			String extension) throws IOException {
		BufferedImage image = getScaledImage(cropedImage, GALLERY_TH_WIDTH, GALLERY_TH_HEIGHT);
		File output = new File(dirPath + File.separatorChar + filename + "_gall_th." + extension);
		ImageIO.write(image, extension, output);
	}

	private static void generateThumbnail1Image(BufferedImage cropedImage, String dirPath, String filename,
			String extension) throws IOException {
		BufferedImage image = getScaledImage(cropedImage, TH1_WIDTH, TH1_HEIGHT);
		File output = new File(dirPath + File.separatorChar + filename + "_th1." + extension);
		ImageIO.write(image, extension, output);
	}

	private static void generateThumbnail2Image(BufferedImage cropedImage, String dirPath, String filename,
			String extension) throws IOException {
		BufferedImage image = getScaledImage(cropedImage, TH2_WIDTH, TH2_HEIGHT);
		File output = new File(dirPath + File.separatorChar + filename + "_th2." + extension);
		ImageIO.write(image, extension, output);
	}

	public static BufferedImage getScaledImage(BufferedImage image, int w, int h) {
		Image scaledImage = image.getScaledInstance(w, h, Image.SCALE_SMOOTH);
		BufferedImage resizedImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = resizedImage.createGraphics();
		g2.drawImage(scaledImage, 0, 0, null);
		g2.dispose();
		return resizedImage;
	}

	public static BufferedImage getScaledImage(BufferedImage image, int w, int h, int rgbType) {
		Image scaledImage = image.getScaledInstance(w, h, Image.SCALE_SMOOTH);
		BufferedImage resizedImage = new BufferedImage(w, h, rgbType);
		Graphics2D g2 = resizedImage.createGraphics();
		g2.drawImage(scaledImage, 0, 0, null);
		g2.dispose();
		return resizedImage;
	}
}
