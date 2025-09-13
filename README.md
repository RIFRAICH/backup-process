# BackupProcess
Nightly process that creates backups and sends them to a remote server

## Requirements
This project requires the following to get started:
- Java version `17` or higher
- Maven version `3.9.11` or higher

## Getting Started

### Setup
1. Go to `src/main/resources`
2. Copy `config.example.json` and rename it to `config.json`
3. Update the values in `config.json` to match your environment

### Compilation
```bash
mvn clean package
```

### Running
```bash
java -jar target/BackupProcess-1.0-SNAPSHOT-jar-with-dependencies.jar
```
