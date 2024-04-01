package fr.rifraich.backup.utils;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class SFTPUtils {

    public static void uploadFolder(ChannelSftp channel, String localFolderPath, String remoteFolderPath) throws SftpException, FileNotFoundException {
        File[] files = new File(localFolderPath).listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    channel.put(new FileInputStream(file), remoteFolderPath + "/" + file.getName());
                } else if (file.isDirectory()) {
                    String newRemoteDir = remoteFolderPath + "/" + file.getName();
                    channel.mkdir(newRemoteDir);
                    channel.cd(newRemoteDir);
                    uploadFolder(channel, file.getAbsolutePath(), newRemoteDir);
                    channel.cd("..");
                }
            }
        }
    }

}
