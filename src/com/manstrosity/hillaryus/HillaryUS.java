package com.manstrosity.hillaryus;

import java.util.ArrayList;
import java.util.Random;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Point3D;
import javafx.geometry.BoundingBox;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class HillaryUS extends Application {

    double hillaryX;
    double hillaryY;

    long votes = 0;
    boolean gameRunning = false;

    ArrayList<Point3D> emails = new ArrayList<>();

    @Override
    public void start(Stage primaryStage) throws Exception {
        
        // Window setup.
        primaryStage.setTitle("HillaryUS");
        primaryStage.setWidth(640.0);
        primaryStage.setHeight(800.0);
        primaryStage.setResizable(false);

        // We're using an AnchorPane for reasons.
        Group root = new Group();

        // Setup the scene
        Scene scene = new Scene(root);
        scene.setCursor(Cursor.NONE);
        primaryStage.setScene(scene);

        Image backgroundImg = new Image(getClass().getResourceAsStream("background.jpg"));
        Image hillaryImg = new Image(getClass().getResourceAsStream("hillary.png"));
        Image emailImg = new Image(getClass().getResourceAsStream("email.png"));

        // Setup the canvas.
        Canvas canvas = new Canvas(primaryStage.getWidth(), primaryStage.getHeight());

        // Setup the mouse motion handler.
        canvas.setOnMouseMoved((MouseEvent event) -> {
            hillaryX = Math.max(
                    hillaryImg.getWidth() / 2,
                    Math.min(
                            canvas.getWidth() - hillaryImg.getWidth() / 2,
                            event.getX()
                    )
            );
            hillaryY = Math.max(
                    hillaryImg.getWidth() / 2,
                    Math.min(
                            canvas.getHeight() - hillaryImg.getHeight() / 2,
                            event.getY()
                    )
            );
        });
        root.getChildren().add(canvas);

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFont(new Font(24.0));

        Canvas scoreCanvas = new Canvas(primaryStage.getWidth(), primaryStage.getHeight());
        scoreCanvas.setVisible(false);

        scoreCanvas.setOnMouseClicked((MouseEvent event) -> {
            emails.clear();
            votes = 0;
            gameRunning = true;
            scoreCanvas.setVisible(false);
        });

        root.getChildren().add(scoreCanvas);

        scoreCanvas.toFront();

        GraphicsContext sgc = scoreCanvas.getGraphicsContext2D();
        sgc.setFont(new Font(32.0));

        Canvas titleCanvas = new Canvas(primaryStage.getWidth(), primaryStage.getHeight());
        titleCanvas.setOnMouseClicked((MouseEvent event) -> {
            gameRunning = true;
            titleCanvas.setVisible(false);
        });

        GraphicsContext tgc = titleCanvas.getGraphicsContext2D();
        tgc.setFont(new Font(32));
        tgc.setFill(Color.GREENYELLOW);

        root.getChildren().add(titleCanvas);
        titleCanvas.toFront();

        Random random = new Random();
        ClockScheduler scheduler = new ClockScheduler();

        // Schedule the background animation.
        scheduler.schedule("Draw", (1000000000 / 60), (currentTime) -> {
            // Compute phase based on the current time.
            double backgroundPhase = (double) (currentTime % 5000000000l) / 5000000000.0;

            // Compute the offset based on the phase.
            double backgroundOffsetX = backgroundImg.getWidth() - (backgroundImg.getWidth() * backgroundPhase);
            double backgroundOffsetY = backgroundImg.getHeight() - (backgroundImg.getHeight() * backgroundPhase);

            // Compute the number of tiles to draw based on the screen size.
            double backgroundTilesX = Math.ceil(canvas.getWidth() / backgroundImg.getWidth()) + 1.0;
            double backgroundTilesY = Math.ceil(canvas.getHeight() / backgroundImg.getHeight()) + 1.0;            
            
            // Draw the tiles.
            for (double backgroundX = 0.0; backgroundX < backgroundTilesX; backgroundX += 1.0) {
                for (double backgroundY = 0.0; backgroundY < backgroundTilesY; backgroundY += 1.0) {
                    gc.drawImage(
                            backgroundImg,
                            backgroundX * backgroundImg.getWidth() - backgroundOffsetX,
                            backgroundY * backgroundImg.getHeight() - backgroundOffsetY
                    );
                }
            }

            // Draw Hillary
            gc.drawImage(
                    hillaryImg,
                    hillaryX - hillaryImg.getWidth() / 2,
                    hillaryY - hillaryImg.getHeight() / 2
            );

            // Draw emails.
            if (gameRunning) {
                emails.forEach((email) -> {
                    gc.drawImage(emailImg, email.getX(), email.getY());
                });
            }

            // Draw the score.
            gc.setFill(Color.WHITE);
            gc.fillText("Votes: " + Long.toString(votes), 20, 20);

            // Draw the score canvas, if necessary:
            if (!gameRunning) {
                sgc.clearRect(0, 0, scoreCanvas.getWidth(), scoreCanvas.getHeight());
                sgc.setFill(Color.rgb(0, 0, 0, 0.25));
                sgc.fillRect(0, 0, scoreCanvas.getWidth(), scoreCanvas.getHeight());
                sgc.setFill(Color.RED);
                sgc.fillText("You got caught!", 100, 70);
                sgc.setFill(Color.BLUE);
                sgc.fillText("Final Votes: " + Long.toString(votes), 230, 400);
                sgc.setFill(Color.CYAN);
                sgc.fillText("Click to try again!", 20, 500);
            }

            // Draw the title canvas, if necessary.
            if (titleCanvas.isVisible()) {
                tgc.fillText("You are Hillary and doing Election!", 20, 140);
                tgc.fillText("But you have email scandal!", 20, 240);
                tgc.fillText("Avoid emails or lose your election!", 20, 340);
            }
        });

        // Schedule the email updates and collision checks.
        scheduler.schedule("Email update and draw", (1000000000 / 60), new Schedulable() {
            private long lastEmailTime = System.nanoTime();
            private long lastEmailCreation = System.nanoTime();

            @Override
            public void run(long currentTime) {
                double deltaTime = (double) (currentTime - lastEmailTime);
                lastEmailTime = currentTime;

                BoundingBox hillaryBox = new BoundingBox(
                        hillaryX - hillaryImg.getWidth() / 2,
                        hillaryY - hillaryImg.getHeight() / 2,
                        hillaryImg.getWidth(),
                        hillaryImg.getHeight()
                );

                double frameFactor = (double) deltaTime / (200000000.0);
                double accelValue = 10.0 * frameFactor;

                for (int i = 0; i < emails.size(); i++) {
                    Point3D email = emails.get(i);

                    if (email.getY() > canvas.getHeight()) {
                        emails.remove(i);
                        continue;
                    }

                    BoundingBox emailBox = new BoundingBox(
                            email.getX(),
                            email.getY(),
                            emailImg.getWidth(),
                            emailImg.getHeight()
                    );

                    if (emailBox.intersects(hillaryBox)) {
                        gameRunning = false;
                        scoreCanvas.setVisible(true);
                    }

                    emails.set(i, new Point3D(
                            email.getX(),
                            email.getY() + (email.getZ() * frameFactor),
                            email.getZ() + accelValue
                    ));
                }

                if (currentTime > lastEmailCreation + Math.max(50000000, 1000000000 - (votes * 50))) {
                    lastEmailCreation = currentTime;
                    emails.add(new Point3D(
                            random.nextDouble() * (canvas.getWidth() - emailImg.getWidth()),
                            -(emailImg.getHeight()),
                            0.0
                    ));
                }
            }
        });

        scheduler.schedule("Vote Counter", (1000000000 / 10), new Schedulable() {
            long voteGrowth = 1;

            @Override
            public void run(long currentTime) {
                if (gameRunning) {
                    votes += voteGrowth;
                    if (votes > voteGrowth * 100) {
                        voteGrowth = voteGrowth * 5;
                    }
                } else {
                    voteGrowth = 1;
                }
            }

        });

        // Start the animation timer.
        new AnimationTimer() {

            @Override
            public void handle(long currentNanoTime) {
                scheduler.update(currentNanoTime);
            }
        }.start();

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
