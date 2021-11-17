/*
 * Copyright (c) 2021, Inversoft Inc., All Rights Reserved
 */
package com.inversoft.http;

import java.nio.file.Path;

/**
 * Models a file upload.
 *
 * @author Brian Pontarelli
 */
public class FileUpload {
  public final String contentType;

  public final Path file;

  public final String fileName;

  public final String name;

  public FileUpload(String contentType, Path file, String fileName, String name) {
    this.contentType = contentType;
    this.file = file;
    this.fileName = fileName != null ? fileName : file.getFileName().toString();
    this.name = name;
  }
}
