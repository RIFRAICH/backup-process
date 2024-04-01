package fr.rifraich.backup;

import fr.rifraich.backup.utils.CommandUtils;

import java.io.File;
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
        
        
    }

    @Override
    public void run() {
        System.out.println("Backup process ended");
    }
}
