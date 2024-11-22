package edu.mondragon.os.wastent;

public class App {

    public App() {
    }

    public void createThreads() {
    }

    public void startThreads() {
    }

    public void interruptThreads() {
    }

    public void waitEndOfThreads() {
    }

    public static void main(String[] args) {

        App app = new App();

        app.createThreads();
        app.startThreads();

        try {
            Thread.sleep(20000);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }

        app.interruptThreads();
        app.waitEndOfThreads();
    }
}