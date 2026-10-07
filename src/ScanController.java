import java.util.*;

public class ScanController {

    private final List<Scan> scans = new ArrayList<>();
    private Scan currentScan;
    private Thread scanThread;
    private boolean running = false;

    public synchronized void handleCommand(String command) {

        command = command.trim();

        if (command.equalsIgnoreCase("view")) {
            view();
        } else if (command.equalsIgnoreCase("start")) {
            start();
        } else if (command.equalsIgnoreCase("stop")) {
            stop();
        } else if (command.equalsIgnoreCase("exit")) {
            exit();
        } else if (command.toLowerCase().startsWith("add:")) {
            add(command.substring(4));
        } else if (command.toLowerCase().startsWith("remove:")) {
            remove(command.substring(7));
        } else {
            System.out.println("Invalid command");
        }
    }

    private synchronized void add(String data) {

        try {
            String[] parts = data.split(",");

            if (parts.length != 4) {
                System.out.println("Invalid add command");
                return;
            }

            int id = Integer.parseInt(parts[0].trim());
            String name = parts[1].trim();
            int duration = Integer.parseInt(parts[2].trim());
            boolean pause = parts[3].trim().equalsIgnoreCase("Yes");

            for (Scan scan : scans) {
                if (scan.getId() == id) {
                    System.out.println("Scan ID already exists");
                    return;
                }
            }

            Scan scan = new Scan(id, name, duration, pause);

            scans.add(scan);

            System.out.println("Added " + name);

        } catch (Exception e) {
            System.out.println("Invalid scan details");
        }
    }

    private synchronized void view() {

        if (scans.isEmpty()) {
            System.out.println("Queue is empty");
            return;
        }

        System.out.println("Current Queue -->");

        for (Scan scan : scans) {
            System.out.println(
                    "Scan:" + scan.getId()
                            + ", " + scan.getName()
                            + ", " + scan.getDuration()
                            + ", " + (scan.isPause() ? "Yes" : "No")
                            + ", " + scan.getState()
            );
        }
    }

    private synchronized void start() {

        if (running) {
            System.out.println("A scan is already running");
            return;
        }

        Scan nextScan = findNextScan();

        if (nextScan == null) {
            System.out.println("No scans available");
            return;
        }

        running = true;

        scanThread = new Thread(() -> runScans());

        scanThread.start();
    }

    private void runScans() {

        while (true) {

            Scan scan;

            synchronized (this) {

                scan = findNextScan();

                if (scan == null) {
                    currentScan = null;
                    running = false;
                    return;
                }

                currentScan = scan;
                scan.setState(Scan.State.RUNNING);
            }

            System.out.println("Starting " + scan.getName());

            try {
                Thread.sleep(scan.getDuration() * 1000L);

            } catch (InterruptedException e) {

                synchronized (this) {

                    if (scan.getState() == Scan.State.CANCELLED) {
                        System.out.println("Cancelled " + scan.getName());
                        currentScan = null;
                        running = false;
                        return;
                    }
                }
            }

            synchronized (this) {

                if (scan.getState() == Scan.State.CANCELLED) {
                    System.out.println("Cancelled " + scan.getName());
                    currentScan = null;
                    running = false;
                    return;
                }

                scan.setState(Scan.State.COMPLETE);
            }

            System.out.println("Completed " + scan.getName());

            if (scan.isPause()) {

                synchronized (this) {
                    currentScan = null;
                    running = false;
                }

                return;
            }
        }
    }

    private synchronized Scan findNextScan() {

        for (Scan scan : scans) {
            if (scan.getState() == Scan.State.IDLE) {
                return scan;
            }
        }

        return null;
    }

    private synchronized void stop() {

        if (currentScan == null || !running) {
            System.out.println("No scan is currently running");
            return;
        }

        currentScan.setState(Scan.State.CANCELLED);

        if (scanThread != null) {
            scanThread.interrupt();
        }

        running = false;

        Scan nextScan = findNextScan();

        if (nextScan != null) {
            running = true;
            scanThread = new Thread(() -> runScans());
            scanThread.start();
        }
    }

    private synchronized void remove(String idText) {

        try {

            int id = Integer.parseInt(idText.trim());

            Scan scanToRemove = null;

            for (Scan scan : scans) {

                if (scan.getId() == id) {
                    scanToRemove = scan;
                    break;
                }
            }

            if (scanToRemove == null) {
                System.out.println("Scan not found");
                return;
            }

            if (scanToRemove.getState() != Scan.State.IDLE) {
                System.out.println("Cannot remove scan because it has already started");
                return;
            }

            scans.remove(scanToRemove);

            System.out.println("Removed " + scanToRemove.getName());

        } catch (NumberFormatException e) {
            System.out.println("Invalid scan ID");
        }
    }

    private synchronized void exit() {

        if (scanThread != null && scanThread.isAlive()) {
            scanThread.interrupt();
        }

        System.out.println("Application exited");

        System.exit(0);
    }
}