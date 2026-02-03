package com.strandls.file.api;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.pac4j.core.profile.CommonProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.strandls.authentication_utility.filter.ValidateUser;
import com.strandls.authentication_utility.util.AuthUtil;
import com.strandls.file.ApiContants;
import com.strandls.file.model.FileDownloadCredentials;
import com.strandls.file.model.FileDownloads;
import com.strandls.file.service.FileAccessService;

import net.minidev.json.JSONArray;

@Path(ApiContants.DOWNLOAD)
public class FileDownloadOthers {

	private static final Logger logger = LoggerFactory.getLogger(FileDownloadOthers.class);

	@Inject
	private FileAccessService accessService;

	@Path("file")
	@GET
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
	public Response listFile(@Context HttpServletRequest request) {
		try {

			CommonProfile profile = AuthUtil.getProfileFromRequest(request);

			JSONArray roles = (JSONArray) profile.getAttribute("roles");
			if (!roles.contains("ROLE_ADMIN")) {
				return Response.status(Status.UNAUTHORIZED).build();

			}
			Map<String, Object> result = accessService.listFile();
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
