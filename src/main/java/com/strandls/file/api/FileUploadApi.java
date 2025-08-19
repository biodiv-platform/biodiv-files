package com.strandls.file.api;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.pac4j.core.profile.CommonProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.strandls.authentication_utility.filter.ValidateUser;
import com.strandls.authentication_utility.util.AuthUtil;
import com.strandls.file.ApiConstants;
import com.strandls.file.dto.FilesDTO;
import com.strandls.file.dto.StringObjectMap;
import com.strandls.file.model.FileUploadModel;
import com.strandls.file.model.MobileFileUpload;
import com.strandls.file.model.MyUpload;
import com.strandls.file.service.FileUploadService;
import com.strandls.file.util.AppUtil;
import com.strandls.file.util.AppUtil.BASE_FOLDERS;
import com.strandls.file.util.AppUtil.MODULE;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path(ApiConstants.UPLOAD)
@Tag(name = "Upload", description = "Operations related to file uploads")
public class FileUploadApi {

	private static final Logger logger = LoggerFactory.getLogger(FileUploadApi.class);

	@Inject
	private FileUploadService fileUploadService;

	@POST
	@Path(ApiConstants.MY_UPLOADS + ApiConstants.MOBILE)
	@ValidateUser
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Upload files to myUploads from mobile", description = "Returns uploaded file data")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Success", content = @Content(schema = @Schema(implementation = MyUpload.class))),
			@ApiResponse(responseCode = "400", description = "Bad Request") })
	public Response saveMobileUpload(@Context HttpServletRequest request,
			@Parameter(description = "Mobile upload data") MobileFileUpload mobileUpload) {
		try {
			if (mobileUpload.getHash() == null || mobileUpload.getHash().isEmpty()) {
				return Response.status(Response.Status.BAD_REQUEST).entity("Hash required").build();
			}
			if (mobileUpload.getFile() == null || mobileUpload.getFile().isEmpty()) {
				return Response.status(Response.Status.BAD_REQUEST).entity("Input upload Stream required").build();
			}
			CommonProfile profile = AuthUtil.getProfileFromRequest(request);
			var userId = Long.parseLong(profile.getId());
			MyUpload result = fileUploadService.saveFileEncoded(mobileUpload, userId);
			return Response.ok(result).build();
		} catch (Exception e) {
			return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	@POST
	@Path(ApiConstants.MY_UPLOADS)
	@ValidateUser
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Upload files to myUploads", description = "Returns uploaded file data")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Success", content = @Content(schema = @Schema(implementation = MyUpload.class))),
			@ApiResponse(responseCode = "400", description = "Bad Request") })
	public Response saveToMyUploads(@Context HttpServletRequest request,
			@FormDataParam("upload") InputStream inputStream,
			@FormDataParam("upload") FormDataContentDisposition fileDetails, @FormDataParam("hash") String hash,
			@FormDataParam("module") String module) {
		if (hash == null || hash.isEmpty()) {
			return Response.status(Response.Status.BAD_REQUEST).entity("Hash required").build();
		}
		if (inputStream == null) {
			return Response.status(Response.Status.BAD_REQUEST).entity("Input upload Stream required").build();
		}
		try {
			CommonProfile profile = AuthUtil.getProfileFromRequest(request);
			var userId = Long.parseLong(profile.getId());
			MODULE mod = AppUtil.getModule(module);
			if (mod == null) {
				return Response.status(Response.Status.BAD_REQUEST).entity("Invalid Module").build();
			}
			MyUpload uploadModel = fileUploadService.saveFile(inputStream, mod, fileDetails.getFileName(), hash,
					userId);
			return Response.ok(uploadModel).build();
		} catch (Exception ex) {
			return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage()).build();
		}
	}

	@GET
	@Path(ApiConstants.MY_UPLOADS)
	@ValidateUser
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Get files list from myUploads", description = "Returns uploaded file data")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Success", content = @Content(array = @ArraySchema(schema = @Schema(implementation = MyUpload.class)))),
			@ApiResponse(responseCode = "400", description = "Invalid Module or Error") })
	public Response getFilesFromUploads(@Context HttpServletRequest request,
			@Parameter(description = "Module to filter files") @QueryParam("module") String module) {
		try {
			CommonProfile profile = AuthUtil.getProfileFromRequest(request);
			var userId = Long.parseLong(profile.getId());
			MODULE mod = AppUtil.getModule(module);
			if (mod == null) {
				return Response.status(Response.Status.BAD_REQUEST).entity("Invalid Module").build();
			}
			List<MyUpload> files = fileUploadService.getFilesFromUploads(userId, mod);
			return Response.ok(files).build();
		} catch (Exception ex) {
			return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage()).build();
		}
	}

	@GET
	@Path(ApiConstants.DWCFILE)
	@Produces(MediaType.TEXT_PLAIN)
	@Operation(summary = "Mapping of Document", description = "Returns Document")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "File Created Successfully"),
			@ApiResponse(responseCode = "400", description = "File Created Failed"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error") })
	public Response createDwcFILE() {
		String filePath = "/app/configurations/scripts/";
		String csvFilePath = "/app/data/biodiv/data-archive/gbif/" + AppUtil.getDatePrefix() + "dWC.csv";
		String script = "gbif_dwc.sh";
		try {
			Process process = Runtime.getRuntime().exec("sh " + script + " " + csvFilePath, null, new File(filePath));
			int exitCode = process.waitFor();
			if (exitCode == 0)
				return Response.ok("File Creation Successful!").build();
		} catch (InterruptedException ie) {
			logger.error("InterruptedException: ", ie);
			Thread.currentThread().interrupt();
		} catch (Exception e) {
			return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
		return Response.status(Response.Status.BAD_REQUEST).entity("File Creation Failed").build();
	}

	@POST
	@Path(ApiConstants.MOVE_FILES)
	@ValidateUser
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Moves files from MyUploads to the appropriate folder", description = "Returns uploaded file data")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Success", content = @Content(schema = @Schema(implementation = StringObjectMap.class))),
			@ApiResponse(responseCode = "400", description = "Bad Request") })
	public Response moveFiles(@Context HttpServletRequest request,
			@Parameter(description = "Files data") FilesDTO filesDTO) {
		try {
			CommonProfile profile = AuthUtil.getProfileFromRequest(request);
			var userId = Long.parseLong(profile.getId());
			MODULE module = AppUtil.getModule(filesDTO.getModule());
			if (module == null) {
				return Response.status(Response.Status.BAD_REQUEST).entity("Invalid Module").build();
			}
			Map<String, Object> files = fileUploadService.moveFilesFromUploads(userId, filesDTO.getFiles(),
					filesDTO.getFolder(), module);
			return Response.ok(new StringObjectMap<Object>(files)).build();
		} catch (Exception ex) {
			return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage()).build();
		}
	}

	@POST
	@Path(ApiConstants.REMOVE_FILE)
	@ValidateUser
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Delete file from my-uploads folder", description = "Returns if the file was deleted")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "File deleted", content = @Content(schema = @Schema(implementation = Boolean.class))),
			@ApiResponse(responseCode = "400", description = "Bad Request") })
	public Response removeFile(@Context HttpServletRequest request,
			@Parameter(description = "File to remove") MyUpload file) {
		try {
			CommonProfile profile = AuthUtil.getProfileFromRequest(request);
			var userId = Long.parseLong(profile.getId());
			boolean deleted = fileUploadService.deleteFilesFromMyUploads(userId, file.getPath());
			return Response.ok(deleted).build();
		} catch (Exception ex) {
			return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage()).build();
		}
	}

	@POST
	@Path(ApiConstants.RESOURCE_UPLOAD)
	@ValidateUser
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Upload resources", description = "Returns uploaded file data")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Upload successful", content = @Content(schema = @Schema(implementation = FileUploadModel.class))),
			@ApiResponse(responseCode = "400", description = "Bad Request") })
	public Response uploadResource(@Context HttpServletRequest request,
			@FormDataParam("upload") InputStream inputStream,
			@FormDataParam("upload") FormDataContentDisposition fileDetails,
			@DefaultValue("") @FormDataParam("hash") String hash, @FormDataParam("directory") String directory,
			@FormDataParam("nestedFolder") String nestedFolder,
			@DefaultValue("false") @FormDataParam("resource") String resource) {
		try {
			boolean createResourceFolder = Boolean.parseBoolean(resource);
			BASE_FOLDERS folder = AppUtil.getFolder(directory);
			if (folder == null) {
				return Response.status(Response.Status.BAD_REQUEST).entity("Invalid directory").build();
			}
			if (inputStream == null) {
				return Response.status(Response.Status.BAD_REQUEST).entity("File required").build();
			}
			FileUploadModel model = fileUploadService.uploadFile(folder, inputStream, fileDetails, request,
					nestedFolder, hash, createResourceFolder);
			return Response.ok(model).build();
		} catch (Exception ex) {
			return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage()).build();
		}
	}

	@POST
	@Path(ApiConstants.BULK_UPLOAD)
	@ValidateUser
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Bulk Upload", description = "Returns uploaded file data")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Bulk upload success", content = @Content(schema = @Schema(implementation = StringObjectMap.class))),
			@ApiResponse(responseCode = "400", description = "Bad Request") })
	public Response handleBulkUpload(@Context HttpServletRequest httpServletRequest,
			FormDataMultiPart formDataMultiPart) {
		try {
			FormDataBodyPart moduleBodyPart = formDataMultiPart.getField("module");
			MODULE module = AppUtil.getModule(moduleBodyPart != null ? moduleBodyPart.getValue() : null);
			if (module == null) {
				return Response.status(Response.Status.BAD_REQUEST).entity("Invalid Module").build();
			}
			FormDataBodyPart folderBodyPart = formDataMultiPart.getField("folder");
			BASE_FOLDERS folder = AppUtil.getFolder(folderBodyPart != null ? folderBodyPart.getValue() : null);
			if (folder == null) {
				return Response.status(Response.Status.BAD_REQUEST).entity("Invalid directory").build();
			}
			List<FormDataBodyPart> filesBodyPart = formDataMultiPart.getFields("upload");
			if (filesBodyPart == null || filesBodyPart.isEmpty()) {
				return Response.status(Response.Status.BAD_REQUEST).entity("File(s) required").build();
			}
			Map<String, Object> response = new HashMap<>();
			List<MyUpload> files = fileUploadService.handleBulkUpload(httpServletRequest, module, filesBodyPart);
			response.put("status", !files.isEmpty());
			response.put("files", files);
			return Response.ok(new StringObjectMap<Object>(response)).build();
		} catch (Exception ex) {
			return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage()).build();
		}
	}

	@POST
	@Path(ApiConstants.BULK + ApiConstants.FILES_PATH)
	@ValidateUser
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Provides canonical hash map list of all files from myUploads for a given userId and Module", description = "Returns uploaded file data")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Success", content = @Content(schema = @Schema(implementation = StringObjectMap.class))),
			@ApiResponse(responseCode = "400", description = "Bad Request") })
	public Response getAllFilePathsByUser(@Context HttpServletRequest request,
			@Parameter(description = "Files DTO") FilesDTO filesDTO) {
		try {
			CommonProfile profile = AuthUtil.getProfileFromRequest(request);
			var userId = Long.parseLong(profile.getId());
			BASE_FOLDERS folder = AppUtil.getFolder(filesDTO.getFolder());
			if (folder == null) {
				throw new Exception("Invalid folder");
			}
			MODULE module = AppUtil.getModule(filesDTO.getModule());
			if (module == null) {
				throw new Exception("Invalid module");
			}
			Map<String, String> files = fileUploadService.getAllFilePathsByUser(userId, folder, module);
			return Response.ok(new StringObjectMap<String>(files)).build();
		} catch (Exception ex) {
			return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage()).build();
		}
	}

	@POST
	@Path(ApiConstants.BULK + ApiConstants.MOVE_FILES)
	@ValidateUser
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Bulk Upload - Moves files from MyUploads to the appropriate folder", description = "Returns uploaded file data")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Bulk move success", content = @Content(schema = @Schema(implementation = StringObjectMap.class))),
			@ApiResponse(responseCode = "400", description = "Bad Request") })
	public Response handleBulkUploadMoveFiles(@Context HttpServletRequest request,
			@Parameter(description = "Files DTO") FilesDTO filesDTO) {
		try {
			CommonProfile profile = AuthUtil.getProfileFromRequest(request);
			var userId = Long.parseLong(profile.getId());
			BASE_FOLDERS folder = AppUtil.getFolder(filesDTO.getFolder());
			if (folder == null) {
				throw new Exception("Invalid folder");
			}
			MODULE module = AppUtil.getModule(filesDTO.getModule());
			if (module == null) {
				throw new Exception("Invalid module");
			}
			Map<String, Object> files = fileUploadService.moveFilesFromUploads(userId, filesDTO.getFiles(), folder,
					module);
			return Response.ok(new StringObjectMap<Object>(files)).build();
		} catch (Exception ex) {
			return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage()).build();
		}
	}

	@POST
	@Path(ApiConstants.MOVE_TO_MYUPLOAD)
	@ValidateUser
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Moves files from specified directory source to myUploads", description = "Returns uploaded file data")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Move complete", content = @Content(schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "400", description = "Bad Request") })
	public Response moveFilesToMyUpload(@Context HttpServletRequest request,
			@Parameter(description = "Files DTO") FilesDTO filesDTO) {
		try {
			CommonProfile profile = AuthUtil.getProfileFromRequest(request);
			var userId = Long.parseLong(profile.getId());
			String folder = filesDTO.getFolder();
			if (folder == null) {
				throw new Exception("Invalid folder");
			}
			MODULE module = AppUtil.getModule(filesDTO.getModule());
			if (module == null) {
				throw new Exception("Invalid module");
			}
			fileUploadService.moveFilesToMyUploads(userId, module, folder);
			return Response.ok("File extraction in progress from " + folder).build();
		} catch (Exception ex) {
			return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage()).build();
		}
	}
}
