package com.strandls.file.util;

import com.google.inject.Singleton;
import me.desair.tus.server.TusFileUploadService;

@Singleton
public class TusConfig {

	private final TusFileUploadService tusFileUploadService;

	public TusConfig() {
		this.tusFileUploadService = new TusFileUploadService().withStoragePath("/home/apps/biodiv-image/tus-tmp")
				.withMaxUploadSize(2L * 1024 * 1024 * 1024).withUploadExpirationPeriod(24 * 60 * 60 * 1000L);

	}

	public TusFileUploadService getService() {
		return tusFileUploadService;
	}
}