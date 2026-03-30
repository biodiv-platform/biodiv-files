package com.strandls.file.api;

import java.util.List;
import java.util.Map;

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
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import net.minidev.json.JSONArray;

import org.pac4j.core.profile.CommonProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.strandls.authentication_utility.filter.ValidateUser;
import com.strandls.authentication_utility.util.AuthUtil;
import com.strandls.file.ApiConstants;
import com.strandls.file.model.FileDownloadCredentials;
import com.strandls.file.model.FileDownloads;
import com.strandls.file.service.FileAccessService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Path(ApiConstants.DOWNLOAD)
@Tag(name = "Download", description = "File download via access key")
public class FileDownloadOthers {

	private static final Logger logger = LoggerFactory.getLogger(FileDownloadOthers.class);

	@Inject
	private FileAccessService accessService;

	@GET
	@Path("file")
	@Operation(summary = "Download file with access key", description = "Downloads a file by verifying the access key")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "File downloaded successfully", content = @Content(mediaType = "application/octet-stream")),
			@ApiResponse(responseCode = "403", description = "Forbidden (credentials invalid)"),
			@ApiResponse(responseCode = "400", description = "Bad request (server error or missing key)") })
	public Response getFile(@QueryParam("accessKey") String accessKey) {
		try {
			FileDownloadCredentials credentials = accessService.getCredentials(accessKey);
			if (credentials != null) {
				return accessService.downloadFile(credentials);
			}
			return Response.status(Status.FORBIDDEN).build();
		} catch (Exception ex) {
			logger.error(ex.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(ex.getMessage()).build();
		}
	}

	@Path("file/list")
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)

	@ValidateUser
	public Response listFile(@Context HttpServletRequest request,
			@DefaultValue("0") @QueryParam("offset") String Offset,
			@DefaultValue("15") @QueryParam("limit") String Limit, @QueryParam("deleted") Boolean deleted) {
		try {

			CommonProfile profile = AuthUtil.getProfileFromRequest(request);

			JSONArray roles = (JSONArray) profile.getAttribute("roles");
			if (!roles.contains("ROLE_ADMIN")) {
				return Response.status(Status.UNAUTHORIZED).build();

			}
			Integer offset = Integer.parseInt(Offset);
			Integer limit = Integer.parseInt(Limit);
			Map<String, Object> result = accessService.listFile(offset, limit, deleted);
			return Response.status(Status.OK).entity(result).build();

		} catch (Exception ex) {
			logger.error(ex.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(ex.getMessage()).build();
		}
	}

	@Path("file/delete" + "/{fileName}")
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)

	@ValidateUser
	public Response deleteFile(@Context HttpServletRequest request, @PathParam("fileName") String fileName) {
		try {

			CommonProfile profile = AuthUtil.getProfileFromRequest(request);

			JSONArray roles = (JSONArray) profile.getAttribute("roles");
			if (!roles.contains("ROLE_ADMIN")) {
				return Response.status(Status.UNAUTHORIZED).build();

			}
			List<FileDownloads> result = accessService.deleteFile(fileName);
			return Response.status(Status.OK).entity(result).build();

		} catch (Exception ex) {
			logger.error(ex.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(ex.getMessage()).build();
		}
	}
}
