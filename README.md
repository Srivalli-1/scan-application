# Scan Application

A Java console application that manages and executes scans sequentially in the background while remaining responsive to user commands.

## Features

- Add scans to the queue
- View all scans and their states
- Start scans sequentially
- Run scans in the background using multithreading
- Pause after a scan when configured
- Stop and cancel the currently running scan
- Automatically continue with the next scan
- Remove scans that have not started
- Exit the application

## Technologies

- Java 17
- IntelliJ IDEA
- Java Threads
- Git & GitHub

## Scan States

- IDLE
- RUNNING
- COMPLETE
- CANCELLED

## Commands

```text
add:<id>, <name>, <duration>, <pause>
view
start
stop
remove:<id>
exit
