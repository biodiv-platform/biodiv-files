package com.strandls.file;

import java.io.File;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ModifyOpenApi {
	public static void main(String[] args) throws Exception {
		for (String string : args) {
			System.err.println(string);
		}
		File openApiFile = new File(args[2]);
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode root = (ObjectNode) mapper.readTree(openApiFile);

		ArrayNode servers = mapper.createArrayNode();
		ObjectNode server = mapper.createObjectNode();
		server.put("url", "http://localhost:8080/files-api/api");
		servers.add(server);

		root.set("servers", servers);
		mapper.writerWithDefaultPrettyPrinter().writeValue(openApiFile, root);
	}
}
