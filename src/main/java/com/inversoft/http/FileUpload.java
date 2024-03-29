/*
 * Copyright (c) 2021, Inversoft Inc., All Rights Reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
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
