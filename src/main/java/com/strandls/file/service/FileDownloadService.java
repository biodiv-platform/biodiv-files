	package com.strandls.file.service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import com.google.common.io.Files;
import com.strandls.file.ApiContants;
import com.strandls.file.util.ImageUtil;

public class FileDownloadService {
	
	String storageBasePath = null;
	
	public FileDownloadService() {
		InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("config.properties");

		Properties properties = new Properties();
		try {
			properties.load(in);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		storageBasePath = properties.getProperty("storage_dir", "/home/apps/biodiv-image");
	}

	public Response getFile(String hashKey, String fileName, String imageVariation) throws FileNotFoundException {
		if (!ApiContants.ORIGINAL.equals(imageVariation)) {
			String extension = Files.getFileExtension(fileName);
			String fileNameWithoutExtension = Files.getNameWithoutExtension(fileName);
			fileName = fileNameWithoutExtension + "_" + imageVariation + "." + extension;
		}

		String fileLocation = storageBasePath + File.separatorChar + hashKey + File.separatorChar + fileName;

		@SuppressWarnings("resource")
		InputStream in = new FileInputStream(new File(fileLocation));
		StreamingOutput sout;
		sout = new StreamingOutput() {
			@Override
			public void write(OutputStream out) throws IOException, WebApplicationException {
				byte[] buf = new byte[8192];
				int c;
				while ((c = in.read(buf, 0, buf.length)) > 0) {
					out.write(buf, 0, c);
					out.flush();
				}
				out.close();
			}
		};
		return Response.ok(sout).type("image/" + Files.getFileExtension(fileLocation)).build();
	}

	public Response getCustomSizeFile(String hashKey, String fileName, int outputWidth, int outputHeight)
			throws IOException {

		String dirPath = storageBasePath + File.separatorChar + hashKey + File.separatorChar;
		String fileLocation = dirPath + fileName;

		File file = new File(fileLocation);

		BufferedImage image = ImageIO.read(file);
		int imageHeight = image.getHeight();
		int imageWidth = image.getWidth();
		double hRatio = ((double) imageHeight) / outputHeight;
		double wRatio = ((double) imageWidth) / outputWidth;

		int subImageHeight = imageHeight, subImageWidth = imageWidth;
		int x = 0, y = 0;

		if (hRatio < wRatio) {
			subImageWidth = (int) (hRatio * outputWidth);
			x = (imageWidth - subImageWidth) / 2;
		} else {
			subImageHeight = (int) (wRatio * outputHeight);
			y = (imageHeight - subImageHeight) / 2;
		}

		image = image.getSubimage(x, y, subImageWidth, subImageHeight);
		BufferedImage outputImage = FileUploadService.getScaledImage(image, outputWidth, outputHeight);

		String extension = Files.getFileExtension(fileName);
		String fileNameWithoutExtension = Files.getNameWithoutExtension(fileName);
		File output = new File(
				dirPath + fileNameWithoutExtension + "_" + imageWidth + "*" + imageHeight + "." + extension);
		ImageIO.write(outputImage, extension, output);

		@SuppressWarnings("resource")
		InputStream in = new FileInputStream(output);
		StreamingOutput sout;
		sout = new StreamingOutput() {
			@Override
			public void write(OutputStream out) throws IOException, WebApplicationException {
				byte[] buf = new byte[8192];
				int c;
				while ((c = in.read(buf, 0, buf.length)) > 0) {
					out.write(buf, 0, c);
					out.flush();
				}
				out.close();
			}
		};
		return Response.ok(sout).type("image/" + Files.getFileExtension(fileLocation)).build();
	}

	public Response getImageResource(HttpServletRequest req, String hashKey, String fileName, Integer width, Integer height, String format) throws Exception {

		String dirPath = storageBasePath + File.separatorChar + hashKey + File.separatorChar;
		String fileLocation = dirPath + fileName;
		File file = new File(fileLocation);
		boolean isWebp = format.equalsIgnoreCase("webp");
		BufferedImage image = ImageIO.read(file);
		int imgHeight = image.getHeight();
		int imgWidth = image.getWidth();
		int newHeight = image.getHeight();
		int newWidth = image.getWidth();
		if (height != null) {
			if (imgHeight > height) {
				newHeight = height;
				newWidth = (height * imgWidth) / imgHeight;
			}			
		}
		if (width != null) {
			if (imgWidth > width) {
				newWidth = width;
				newHeight = (width * imgHeight) / imgWidth;
			}
		}
		image = image.getSubimage(0, 0, imgWidth, imgHeight);
		BufferedImage outputImage = FileUploadService.getScaledImage(image, width == null ? newWidth : width, height == null ? newHeight : height);
		String extension = Files.getFileExtension(fileName);
		String fileNameWithoutExtension = Files.getNameWithoutExtension(fileName);
		File output = new File(dirPath + fileNameWithoutExtension + "_" + imgWidth + "*" + imgHeight + "." + format);
		ImageIO.write(outputImage, extension, output);
		File webpOutput = null;
		if (isWebp) {
			webpOutput = new File(dirPath + fileNameWithoutExtension + "_" + imgWidth + "*" + imgHeight + "." + format);
			ImageUtil.toWEBP(req, output, webpOutput);
		}
		InputStream in = new FileInputStream(isWebp ? webpOutput : output);
		StreamingOutput sout;
		sout = new StreamingOutput() {
			@Override
			public void write(OutputStream out) throws IOException, WebApplicationException {
				byte[] buf = new byte[8192];
				int c;
				while ((c = in.read(buf, 0, buf.length)) > 0) {
					out.write(buf, 0, c);
					out.flush();
				}
				in.close();
				out.close();
			}
		};
		return Response.ok(sout).type("image/" + format).build();
	}

}
