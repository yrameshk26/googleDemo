/**
 * Copyright 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.appengine.mail;

// [START mail_handler_servlet]
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.util.Date;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.cloud.storage.*;
import com.google.cloud.storage.Acl.User;
import com.google.cloud.storage.Bucket.BucketSourceOption;
import com.google.cloud.storage.Storage.BlobGetOption;

public class MailHandlerServlet extends HttpServlet {

  private static final Logger log = Logger.getLogger(MailHandlerServlet.class.getName());
  Storage storage = StorageOptions.getDefaultInstance().getService();
  Date date= new Date();
  long time = date.getTime();
  final String bucketName = "telusmailpoc.appspot.com";

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    Properties props = new Properties();
    Session session = Session.getDefaultInstance(props, null);
    Bucket bucket = storage.get(bucketName);
    String blobName = "mail_"+time+".eml";
    try {
      MimeMessage message = new MimeMessage(session, req.getInputStream());
      log.info("Received mail message: " + message.getSubject());
      log.info("Received mail message's ContentType: " + message.getContentType());
      Blob uploadedBlob = uploadMail(bucket, blobName, message);
      log.info("uploadedBlob : " + uploadedBlob);
    } catch (MessagingException e) {
      log.info ("MessagingException : " + e);
    } catch (IOException e1){
      log.info ("IOException : " + e1);
    }
  }

  public Blob uploadMail(Bucket bucket , String blobName, MimeMessage message) throws IOException, MessagingException {
    // [START createBlobFromInputStream]
    InputStream content = message.getInputStream();
    Blob blob = bucket.create(blobName, content, "multipart/mixed");
    // [END createBlobFromInputStream]
    return blob;
  }

}
// [END mail_handler_servlet]