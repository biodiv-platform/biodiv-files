package com.strandls.file.service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.imageio.ImageIO;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import com.google.common.io.Files;
import com.strandls.file.ApiContants;

public class FileDownloadService {
	
	public Response getFile(String hashKey, String fileName, String imageVariation) throws FileNotFoundException {
		if(!ApiContants.ORIGINAL.equals(imageVariation)) {
			 String extension = Files.getFileExtension(fileName);
			 String fileNameWithoutExtension  = Files.getNameWithoutExtension(fileName);
			 fileName = fileNameWithoutExtension + "_" + imageVariation + "." + extension;
		}
		
		String fileLocation = ApiContants.ROOT_PATH + File.separatorChar + hashKey +
				File.separatorChar + fileName;
		
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
	
	public Response getCustomSizeFile(String hashKey, String fileName, int outputWidth, int outputHeight) throws IOException {
		
		String dirPath = ApiContants.ROOT_PATH + File.separatorChar + hashKey + File.separatorChar;
		String fileLocation = dirPath + fileName;
		
		File file = new File(fileLocation);
		
		BufferedImage image = ImageIO.read(file);
		int imageHeight = image.getHeight();
		int imageWidth  = image.getWidth();
		double hRatio = ((double) imageHeight)/outputHeight;
		double wRatio = ((double) imageWidth)/outputWidth;
		
		
		int subImageHeight = imageHeight, subImageWidth = imageWidth;
		int x = 0, y = 0;
		
		if(hRatio < wRatio) {
			subImageWidth  = (int) (hRatio * outputWidth);
			x = (imageWidth - subImageWidth)/2;
		} else {
			subImageHeight = (int) (wRatio * outputHeight);
			y = (imageHeight - subImageHeight)/2;
		}
		
		image = image.getSubimage(x, y, subImageWidth, subImageHeight);
		BufferedImage outputImage = FileUploadService.getScaledImage(image, outputWidth, outputHeight);
		
		String extension = Files.getFileExtension(fileName);
		String fileNameWithoutExtension  = Files.getNameWithoutExtension(fileName);
		File output = new File(dirPath + fileNameWithoutExtension + "_" + imageWidth + "*" + imageHeight + "." + extension);
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

}
