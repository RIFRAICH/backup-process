package fr.rifraich.backup;

import com.jcraft.jsch.*;
import fr.rifraich.backup.config.BackupConfig;
import fr.rifraich.backup.utils.CommandUtils;
import fr.rifraich.backup.utils.SFTPUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class BackupProcess implements Runnable {

    public static void main(String[] args) {
        BackupProcess backupProcess = new BackupProcess();
        new Thread(backupProcess, "main").start();
    }

    public BackupProcess() {
        System.out.println("Backup process started");

        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedDate = currentDate.format(formatter);

        File backupDirectory = new File("/mnt/Backup/build/backup_" + formattedDate);
        backupDirectory.mkdirs();

        String backupDatabaseCmd = "mysqldump --all-databases > \"" + backupDirectory.getAbsolutePath() + "/databases.sql\"";
        System.out.println("Running command => \"" + backupDatabaseCmd + "\"");
        CommandUtils.runCommand(backupDatabaseCmd);
        System.out.println("Backup database done => " + backupDirectory.getAbsolutePath() + "/databases.sql");

        BackupConfig cfg = BackupConfig.load("config.json");

        if (cfg == null) {
            System.out.println("[INFO] Config absente → skip upload.");
            return;
        }

        if (!cfg.isValidNonDefault()) {
            System.out.println("[WARN] Config invalide ou par défaut → skip upload.");
            return;
        }

        try {
            System.out.println("Connecting to remote server...");
            JSch jsch = new JSch();
            Session session = jsch.getSession(cfg.user, cfg.host, cfg.port);
            session.setPassword(cfg.password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            Channel channel = session.openChannel("sftp");
            channel.connect();
            ChannelSftp sftpChannel = (ChannelSftp) channel;

            sftpChannel.cd(cfg.remoteDirectory);
            System.out.println("Connected to remote server");

            System.out.println("Uploading backup...");
            String newRemoteDir = cfg.remoteDirectory + "/" + backupDirectory.getName();
            try {
                sftpChannel.mkdir(newRemoteDir);
                System.out.println("Created remote directory: " + newRemoteDir);
            } catch (SftpException e) {
                if (e.id == ChannelSftp.SSH_FX_FAILURE) {
                    System.out.println("Remote directory already exists: " + newRemoteDir);
                } else {
                    throw e;
                }
            }
            SFTPUtils.uploadFolder(sftpChannel, backupDirectory.getAbsolutePath(), newRemoteDir);
            System.out.println("Backup uploaded");

            System.out.println("Disconnecting from remote server");
            sftpChannel.disconnect();
            session.disconnect();

            // cleanupLocalBackup(backupDirectory);

        } catch (JSchException | SftpException | FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("[ERROR] Upload SFTP échoué. La sauvegarde locale est conservée: " + backupDirectory.getAbsolutePath());
        }

        System.out.println("Backup process ended");
    }

    @Override
    public void run() {
        // void
    }

    private void cleanupLocalBackup(File backupDirectory) {
        System.out.println("Delete local backup");
        CommandUtils.runCommand("rm -r \"" + backupDirectory.getAbsolutePath() + "\"");
    }
}
