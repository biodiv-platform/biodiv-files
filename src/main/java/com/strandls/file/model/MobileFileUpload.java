/** */
package com.strandls.file.model;

/**
 * @author Abhishek Rudra
 */
public class MobileFileUpload {

	private String file;
	private String module;
	private String filename;
	private String hash;

	/** */
	public MobileFileUpload() {
		super();
	}

	/**
	 * @param file
	 * @param module
	 * @param filename
	 * @param hash
	 */
	public MobileFileUpload(String file, String module, String filename, String hash) {
		super();
		this.file = file;
		this.module = module;
		this.filename = filename;
		this.hash = hash;
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}
}
