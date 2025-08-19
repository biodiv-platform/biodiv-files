package com.strandls.file.api;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.strandls.file.ApiConstants;
import com.strandls.file.model.FileUploadModel;
import com.strandls.file.service.FileAccessService;
import com.strandls.file.service.FileDownloadService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path(ApiConstants.GET)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Download", description = "File and Image download endpoints")
public class FileDownloadApi {

	private static final Logger logger = LoggerFactory.getLogger(FileDownloadApi.class);

	@Inject
	private FileDownloadService fileDownloadService;

	@Inject
	private FileAccessService accessService;

	@GET
	@Path("ping")
	@Produces(MediaType.TEXT_PLAIN)
	@Operation(summary = "Health check", description = "Dummy endpoint to test connectivity")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "pong", content = @Content(mediaType = "text/plain")) })
	public String ping() {
		return "pong";
	}

	@GET
	@Path("model")
	@Operation(summary = "File upload model", description = "Dummy endpoint to preview FileUploadModel")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "FileUploadModel preview", content = @Content(mediaType = "application/json", schema = @Schema(implementation = FileUploadModel.class))) })
	public FileUploadModel model() {
		return new FileUploadModel();
	}

	@GET
	@Path("crop/{directory:.+}/{fileName}")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces("image/*")
	@Operation(summary = "Get cropped image", description = "Get the image with specified width and height")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Cropped Image", content = @Content(mediaType = "image/*")),
			@ApiResponse(responseCode = "400", description = "Bad Request"), })
	public Response getImage(@Context HttpServletRequest request, @PathParam("directory") String directory,
			@PathParam("fileName") String fileName, @QueryParam("w") Integer width, @QueryParam("h") Integer height,
			@DefaultValue("webp") @QueryParam("fm") String format, @DefaultValue("") @QueryParam("fit") String fit,
			@DefaultValue("false") @QueryParam("preserve") String preserve) throws UnsupportedEncodingException {

		fileName = URLDecoder.decode(fileName, StandardCharsets.UTF_8.name());

		if ((height == null && width == null) || isInvalid(directory) || isInvalid(fileName)) {
			return Response.status(Response.Status.BAD_REQUEST)
					.entity("Valid directory, fileName, and dimensions required").build();
		}

		String accept = request.getHeader(HttpHeaders.ACCEPT);
		boolean preserveFormat = Boolean.parseBoolean(preserve);
		String userRequestedFormat = accept.contains("webp") && format.equalsIgnoreCase("webp") ? "webp"
				: (!format.equalsIgnoreCase("webp") ? format : "jpg");

		return fileDownloadService.getImage(request, directory, fileName, width, height, userRequestedFormat, fit,
				preserveFormat);
	}

	@GET
	@Path("crop/plantnet/{directory:.+}/{fileName}")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces("image/*")
	@Operation(summary = "Resize image for PlantNet", description = "Provides resized image suitable for PlantNet")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Resized Image", content = @Content(mediaType = "image/*")),
			@ApiResponse(responseCode = "400", description = "Bad Request"), })
	public Response getImageResizedForPlantnet(@PathParam("directory") String directory,
			@PathParam("fileName") String fileName, @DefaultValue("webp") @QueryParam("fm") String format,
			@DefaultValue("") @QueryParam("fit") String fit,
			@DefaultValue("false") @QueryParam("preserve") String preserve) throws UnsupportedEncodingException {

		fileName = URLDecoder.decode(fileName, StandardCharsets.UTF_8.name());

		if (isInvalid(directory) || isInvalid(fileName)) {
			return Response.status(Response.Status.BAD_REQUEST).build();
		}

		return fileDownloadService.getImagePlantnet(directory, fileName, fit);
	}

	@GET
	@Path(ApiConstants.ICON + "/{height}/{width}/{directory:.+}/{fileName}")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces("image/*")
	@Operation(summary = "Generate mail icon", description = "Returns resized icon for email display")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Mail Icon", content = @Content(mediaType = "image/*")),
			@ApiResponse(responseCode = "400", description = "Bad Request"),
			@ApiResponse(responseCode = "500", description = "Server Error") })
	public Response getMailIcon(@Context HttpServletRequest request, @PathParam("directory") String directory,
			@PathParam("fileName") String fileName, @PathParam("height") Integer height,
			@PathParam("width") Integer width) {

		try {
			fileName = URLDecoder.decode(fileName, StandardCharsets.UTF_8.name());

			if (isInvalid(directory) || isInvalid(fileName)) {
				return Response.status(Response.Status.BAD_REQUEST).build();
			}

			String fileExtension = com.google.common.io.Files.getFileExtension(fileName);
			return fileDownloadService.getImage(request, directory, fileName, width, height, fileExtension, "center",
					true);

		} catch (Exception e) {
			logger.error("Error generating mail icon", e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		}
	}

	@GET
	@Path("raw/{directory:.+}/{fileName}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Operation(summary = "Download raw file", description = "Get the unmodified raw resource")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Raw File", content = @Content(mediaType = "application/octet-stream")),
			@ApiResponse(responseCode = "400", description = "Bad Request"), })
	public Response getRawResource(@PathParam("directory") String directory, @PathParam("fileName") String fileName)
			throws UnsupportedEncodingException {

		fileName = URLDecoder.decode(fileName, StandardCharsets.UTF_8.name());

		if (isInvalid(directory) || isInvalid(fileName)) {
			return Response.status(Response.Status.BAD_REQUEST).build();
		}

		return fileDownloadService.getRawResource(directory, fileName);
	}

	@GET
	@Path(ApiConstants.LOGO + "/{directory:.+}/{fileName}")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces("image/*")
	@Operation(summary = "Get user group logo", description = "Resized logo for a user group")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "User Group Logo", content = @Content(mediaType = "image/*")),
			@ApiResponse(responseCode = "400", description = "Bad Request") })
	public Response getUserGroupLogo(@Context HttpServletRequest request, @PathParam("directory") String directory,
			@PathParam("fileName") String fileName, @QueryParam("w") Integer width, @QueryParam("h") Integer height)
			throws Exception {

		fileName = URLDecoder.decode(fileName, StandardCharsets.UTF_8.name());

		if ((height == null && width == null) || isInvalid(directory) || isInvalid(fileName)) {
			return Response.status(Response.Status.BAD_REQUEST).build();
		}

		return fileDownloadService.getLogo(directory, fileName, width, height);
	}

	@GET
	@Path(ApiConstants.DOWNLOAD + "/requestfile")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Operation(summary = "Download archived file")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "File downloaded successfully", content = @Content(mediaType = "application/octet-stream")),
			@ApiResponse(responseCode = "400", description = "Bad request"),
			@ApiResponse(responseCode = "500", description = "Internal server error") })
	public Response downloadFileGivenPath(@QueryParam("type") String fileType, @QueryParam("name") String fileName) {
		String path = null;

		if ("observation".equalsIgnoreCase(fileType)) {
			path = "/app/data/biodiv/data-archive/listpagecsv";
		}

		try {
			if (path != null && Files.exists(Paths.get(path, fileName), LinkOption.NOFOLLOW_LINKS)) {
				return accessService.genericFileDownload(path + File.separator + fileName);
			}
		} catch (IOException e) {
			logger.error("File download error", e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		}

		return Response.status(Response.Status.BAD_REQUEST).build();
	}

	private boolean isInvalid(String value) {
		return value == null || value.isEmpty() || value.contains("..");
	}
}
