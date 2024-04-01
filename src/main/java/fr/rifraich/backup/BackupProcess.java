package fr.rifraich.backup;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.jcraft.jsch.*;
import fr.rifraich.backup.utils.CommandUtils;
import fr.rifraich.backup.utils.SFTPUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class BackupProcess implements Runnable {

    public static void main(String[] args) {
        BackupProcess backupProcess = new BackupProcess();
        new Thread(backupProcess, "main").start();
    }

    public BackupProcess(){
        System.out.println("Backup process started");

        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedDate = currentDate.format(formatter);

        File backupDirectory = new File("/mnt/Backup/build/backup_" + formattedDate);
        backupDirectory.mkdirs();

        String backupDatabaseCmd = "mysqldump --all-databases > " + backupDirectory.getAbsolutePath() + "/databases.sql";
        System.out.println("Running command => \"" + backupDatabaseCmd + "\"");
        CommandUtils.runCommand(backupDatabaseCmd);
        System.out.println("Backup database done => " + backupDirectory.getAbsolutePath() + "/databases.sql");

        System.out.println("Connecting to remote server...");
        Gson gson = new GsonBuilder().create();
        JsonObject jsonObject = gson.fromJson(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("config.json")), JsonObject.class);
        String host = jsonObject.get("host").getAsString();
        int port = jsonObject.get("port").getAsInt();
        String user = jsonObject.get("user").getAsString();
        String password = jsonObject.get("password").getAsString();
        String remoteDirectory = jsonObject.get("remoteDirectory").getAsString();
        try{
            JSch jsch = new JSch();
            Session session = jsch.getSession(user, host, port);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            Channel channel = session.openChannel("sftp");
            channel.connect();
            ChannelSftp sftpChannel = (ChannelSftp) channel;
            sftpChannel.cd(remoteDirectory);
            System.out.println("Connected to remote server");

            System.out.println("Uploading backup...");
            String newRemoteDir = remoteDirectory + "/" + backupDirectory.getName();
            sftpChannel.mkdir(newRemoteDir);
            SFTPUtils.uploadFolder(sftpChannel, backupDirectory.getAbsolutePath(), newRemoteDir);
            System.out.println("Backup uploaded");

            System.out.println("Disconnecting from remote server");
            sftpChannel.disconnect();
            session.disconnect();

            System.out.println("Delete locale backup");
            CommandUtils.runCommand("rm -r " + backupDirectory.getAbsolutePath());
        } catch (JSchException | SftpException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        System.out.println("Backup process ended");
    }
}
