/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Utility;

import AMZ.AKC_Creds;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javax.mail.MessagingException;
import org.apache.commons.net.ftp.FTPClient;

/**
 *
 * @author AN2
 */
public class MyFTPClient {

    FTPClient client = new FTPClient();
    FileInputStream fis = null;
    boolean errorFree;

    public boolean sendFile(File theFile) {
        errorFree = true;
        try {
            client.connect("ftp.listrakbi.com", 21);

            if (client.login(AKC_Creds.LISTRAK_FTP_LOGIN, AKC_Creds.LISTRAK_FTP_PWD)) {
                System.out.println("FTP login Success");
            } else {
                System.out.println("FTP login Failed");
                try {
                    Emailer.Send(AKC_Creds.ANH_EMAIL, AKC_Creds.ANH_EMAIL_PWD, AKC_Creds.ANH_EMAIL, "Auto notification from server : ERROR", "\n\n Connection to Listrak FTP failed.");
                } catch (MessagingException ex) {
                    System.out.println(ex.getMessage());
                }
                errorFree = false;
            }

            //
            // Create an InputStream of the file to be uploaded
            //
            fis = new FileInputStream(theFile);
            String remoteFileName = theFile.getAbsolutePath().substring(theFile.getAbsolutePath().lastIndexOf("\\") + 1);
            //
            // Store file to server
            //
            if (client.storeFile(remoteFileName, fis)) {
                System.out.println("FTP storeFile "+remoteFileName+" Success");
            } else {
                System.out.println("FTP storeFile Failed");
                errorFree = false;
                try {
                    Emailer.Send(AKC_Creds.ANH_EMAIL, AKC_Creds.ANH_EMAIL_PWD, AKC_Creds.ANH_EMAIL, "Auto notification from server : ERROR", "\n\n The system was able to connect with Listrak but cannot send \"" + remoteFileName + "\" file via ftp.");
                } catch (MessagingException ex) {
                    System.out.println(ex.getMessage());
                }
            }
            client.logout();

        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            errorFree = false;
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
                client.disconnect();
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        }
        return errorFree;
    }
}