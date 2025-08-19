package com.strandls.file.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.strandls.file.ApiConstants;
import com.strandls.file.model.FileDownloadCredentials;
import com.strandls.file.service.FileAccessService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

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
			logger.error(ex.getMessage(), ex);
			return Response.status(Status.BAD_REQUEST).entity(ex.getMessage()).build();
		}
	}
}
