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
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.appidentity.AppIdentityService;
import com.google.appengine.api.appidentity.AppIdentityServiceFactory;
import com.google.auth.ServiceAccountSigner;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.cloud.storage.*;
import com.google.cloud.storage.Acl.User;
import com.google.cloud.storage.Acl.Role;
import com.google.cloud.storage.Storage.SignUrlOption;

public class MailHandlerServlet extends HttpServlet {

  private static final Logger log = Logger.getLogger(MailHandlerServlet.class.getName());
  static final AppIdentityService identityService = AppIdentityServiceFactory.getAppIdentityService();
  Storage storage = StorageOptions.getDefaultInstance().getService();
  Date date= new Date();
  long time = date.getTime();
  final String bucketName = "telusmailpoc.appspot.com";

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) {
    Properties props = new Properties();
    Session session = Session.getDefaultInstance(props, null);
    try {
      MimeMessage message = new MimeMessage(session, req.getInputStream());
      File eml = File.createTempFile("mail", ".eml");
      OutputStream out = new FileOutputStream(eml);
      message.writeTo(out);
      InputStream emlMessage = new FileInputStream(eml);
      out.close();
      eml.delete();
      log.info("Received mail message: " + message.getSubject());
      uploadFile(message.getHeader("rcpDeliveryId", ","),bucketName, emlMessage);
    } catch (MessagingException e) {
      log.info ("MessagingException : " + e);
    } catch (IOException e1){
      log.info ("IOException : " + e1);
    }
  }

  @SuppressWarnings("deprecation")
  public void uploadFile(String fileKey, final String bucketName, InputStream source) throws MessagingException {
    final String fileName =  fileKey + ".eml";

    // the inputstream is closed by default, so we don't need to close it here
    BlobInfo blobInfo =
            storage.create(
                    BlobInfo
                            .newBuilder(bucketName, fileName)
                            // Modify access list to allow all users with link to read file
//                            .setAcl(new ArrayList<>(Arrays.asList(Acl.of(User.ofAllUsers(), Role.READER))))
                            .build(),
                    source);

    // Unsigned download link
    log.info("uploadedFileurl USUAL: " + blobInfo.getMediaLink());


    SignUrlOption signWith = SignUrlOption.signWith(new ServiceAccountSigner() {
      @Override
      public byte[] sign(byte[] toSign) {
        return identityService.signForApp(toSign).getSignature();
      }

      @Override
      public String getAccount() {
        return identityService.getServiceAccountName();
      }
    });

    // create signed URL
    URL signedUrl =  storage.get(blobInfo.getBlobId()).signUrl(10000,TimeUnit.DAYS, signWith);
    log.info("uploadedFileurl SIGNED: " + signedUrl);
  }

}