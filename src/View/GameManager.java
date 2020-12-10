package View;

import data.*;
import javafx.animation.AnimationTimer;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.effect.BoxBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import model.*;

import java.awt.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;

public class GameManager {
    private static final int GAME_WIDTH = 400;
    private static final int GAME_HEIGHT = 450;
    private AnchorPane gamePane;
    private Scene gameScene;
    private Stage gameStage;
    private AnchorPane gp1;
    private boolean pauseClicked = false;
    private AnchorPane gp2;
    private final static String BACKGROUND_IMAGE = "View/Resources/dark_background.jpg";
    private final static String PAUSE_IMAGE = "model/Resources/pause.png";
    private GameSubScenes pauseScreen;
    private GameSubScenes defeatScreen;
    private ImageView pauseButton;
    private GameButtons resumeButton;
    private GameButtons SaveAndExitButton;
    private GameButtons restartButton;
    private GameButtons exitToMainMenuButtonPause;
    private GameButtons exitToMainMenuButtonDefeat;
    private GameButtons SpendPointsToContinue;
    private AnimationTimer gameTimer;
    private ArrayList<Arc> colorSwitch;
    private Stage menuStage;
    private boolean jumplock = false;
    private GameObstacles temp1;
    private GameObstacles temp2;
    private GameObstacles curObstacle;
    private GameObstacles prevObstacle;
    private Points curPoints;
    private Points nextPoints;
    private AnchorPane curPane;
    private GameObstacles gp1_obstacle;
    private GameObstacles gp2_obstacle;
    private Queue<GameObstacles> queue_obs;
    private Ball start_ball_obj;
    private ColorSwitch cur_colorSwitch_obj;
    private Points points_obj;
    private int startFlag = 1;
    private int score;
    private boolean isPaused = false;
    private int requiredRevivingScore = 5;
    private LeaderBoard highScoresdata;
    private InfoLabel scoreDisplay;
    private HashSet<Integer> leaderBoardScores;
    private PriorityQueue<PlayerData> updatedLeaderboard;

    public GameManager(){
        gamePane = new AnchorPane();
        gameStage = new Stage();
        gameScene = new Scene(gamePane, GAME_WIDTH, GAME_HEIGHT);
        gameStage.setScene(gameScene);
        createSpaceListener();
        queue_obs = new LinkedList<>();
    }

    private void createSaveGameListener(){
        pauseButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                isPaused = true;
                curObstacle.getAnimation().pause();
                if (prevObstacle != null)
                    prevObstacle.getAnimation().pause();
                BoxBlur bb = new BoxBlur();
                bb.setWidth(5);
                bb.setHeight(5);
                bb.setIterations(3);
                gp1.setEffect(bb);
                gp2.setEffect(bb);
                scoreDisplay.setEffect(bb);
                pauseButton.setEffect(bb);
                start_ball_obj.getStart_ball().setEffect(bb);
                pauseScreen.moveSubScene(-1*GAME_WIDTH);
            }
        });

        SaveAndExitButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                SaveFile saveFile = new SaveFile();
                GameData saveSlot = new GameData(start_ball_obj, obstacleColorList(curObstacle), obstacleAnglesList(curObstacle), curObstacle.getObstacle_id(), obstacleColorList(prevObstacle), obstacleAnglesList(prevObstacle), prevObstacle.getObstacle_id(),  curPoints.getFlag(), nextPoints.getFlag(), gp1.getLayoutY(), gp2.getLayoutY(), cur_colorSwitch_obj.getCs_flag(), score);
                System.out.println(saveSlot.getScore());
                saveFile.saveGameData(saveSlot);
                new ViewManager().showMainMenu(gameStage);
            }
        });

        exitToMainMenuButtonPause.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                new ViewManager().showMainMenu(gameStage);
            }
        });

        resumeButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                isPaused = false;
                start_ball_obj.getStart_ball().setEffect(null);
                gp1.setEffect(null);
                gp2.setEffect(null);
                scoreDisplay.setEffect(null);
                pauseButton.setEffect(null );
                curObstacle.getAnimation().play();
                if(prevObstacle != null)
                    prevObstacle.getAnimation().play();
                gamePane.requestFocus();
                pauseScreen.moveSubScene(GAME_WIDTH);
                //createSpaceListener();
            }
        });

        SpendPointsToContinue.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if(score >= requiredRevivingScore){
                    isPaused = false;
                    score -= requiredRevivingScore;
                    requiredRevivingScore += 5;
                    scoreDisplay.setText(Integer.toString(score));
                    start_ball_obj.getStart_ball().setEffect(null);
                    gp1.setEffect(null);
                    gp2.setEffect(null);
                    scoreDisplay.setEffect(null);
                    pauseButton.setEffect(null );
                    curObstacle.getAnimation().play();
                    if(prevObstacle != null)
                        prevObstacle.getAnimation().play();
                    gamePane.requestFocus();
                    defeatScreen.moveSubScene(GAME_WIDTH);
                }
                else {
                    Text error = new Text("Required points to revive - " + Integer.toString(requiredRevivingScore));
                    error.setLayoutY(80+25+49+110 );
                    error.setLayoutX(52);
                    try{
                        error.setFont(Font.loadFont(new FileInputStream("src/model/Resources/kenvector_future.ttf"),10));
                    } catch (FileNotFoundException e) {
                        error.setFont(Font.font("Verdana",10));
                    }
                    defeatScreen.subPane.getChildren().add(error);
                }
            }
        });

        exitToMainMenuButtonDefeat.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                updatedLeaderboard = highScoresdata.getLeaderboard();
                if(highScoresdata.getLeaderboard().size()==0){
                    TextField playerNameField = new TextField("Enter name");
                    playerNameField.
                    playerName.
                    updatedLeaderboard.add(new PlayerData(someName, score));
                }
            }
        });

        restartButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                try {
                    isPaused = false;
                    gamePane.requestFocus();
                    score = 0;
                    startFlag = 1;
                    start_ball_obj = null;
                    curObstacle = null;
                    prevObstacle = null;
                    curPoints = null;
                    nextPoints = null;
                    cur_colorSwitch_obj = null;
                    gp1 = null;
                    gp2 = null;
                    gamePane = null;
                    createNewGame(gameStage);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private ArrayList<Double> obstacleAnglesList(GameObstacles obstacle){
        ArrayList<Double> angleList = new ArrayList<>();

        if(obstacle.getArc_components().size()!=0) {
            for (int i = 0; i < obstacle.getArc_components().size(); ++i) {
                Rotate curRotate = (Rotate) obstacle.getArc_components().get(i).getTransforms().get(0);
                angleList.add(curRotate.getAngle());
            }
        }

        if(obstacle.getLine_components().size()!=0) {
            for (int i = 0; i < obstacle.getLine_components().size(); ++i) {
                Rotate curRotate = (Rotate)obstacle.getLine_components().get(i).getTransforms().get(0);
                angleList.add(curRotate.getAngle());
            }
        }

        return angleList;
    }

    private ArrayList<Integer> obstacleColorList(GameObstacles obstacle){
        ArrayList<Integer> colorList = new ArrayList<>();

        if(obstacle.getArc_components().size()!=0) {
            for (int i = 0; i < obstacle.getArc_components().size(); ++i) {
                if (obstacle.getArc_components().get(i).getStroke() == Color.BLUE)
                    colorList.add(0);
                if (obstacle.getArc_components().get(i).getStroke() == Color.RED)
                    colorList.add(1);
                if (obstacle.getArc_components().get(i).getStroke() == Color.GREEN)
                    colorList.add(2);
                if (obstacle.getArc_components().get(i).getStroke() == Color.YELLOW)
                    colorList.add(3);
            }
        }

        if(obstacle.getLine_components().size()!=0) {
            for (int i = 0; i < obstacle.getLine_components().size(); ++i) {
                if (obstacle.getLine_components().get(i).getStroke() == Color.BLUE)
                    colorList.add(0);
                if (obstacle.getLine_components().get(i).getStroke() == Color.RED)
                    colorList.add(1);
                if (obstacle.getLine_components().get(i).getStroke() == Color.GREEN)
                    colorList.add(2);
                if (obstacle.getLine_components().get(i).getStroke() == Color.YELLOW)
                    colorList.add(3);
            }
        }

        return colorList;
    }

    private GameObstacles animateObstacle1(AnchorPane gp, float x, float y, ArrayList<Double> anglesList, ArrayList<Integer> colorList){
        GameObstacles obstacles = new Obstacle_1();             //calls game Obstacles
        Rotate rotation1 = new Rotate();
        Rotate rotation2 = new Rotate();
        Rotate rotation3 = new Rotate();
        Rotate rotation4 = new Rotate();
        if(anglesList == null)
            obstacles.createObstacle(x, y, start_ball_obj.getStart_ball());
        else
            obstacles.reconstructObstacle(x, y, anglesList, colorList);
        obstacles.getArc_components().get(0).getTransforms().add(rotation1);
        obstacles.getArc_components().get(1).getTransforms().add(rotation2);
        obstacles.getArc_components().get(2).getTransforms().add(rotation3);
        obstacles.getArc_components().get(3).getTransforms().add(rotation4);
        gp.getChildren().addAll(obstacles.getArc_components());

        start_ball_obj.setBlue_flag(false);
        start_ball_obj.setRed_flag(false);
        start_ball_obj.setGreen_flag(false);
        start_ball_obj.setYellow_flag(false);

        for(int i = 0; i<obstacles.getArc_components().size(); ++i){
            if(obstacles.getArc_components().get(i).getStroke() == Color.BLUE)
                start_ball_obj.setBlue_flag(true);
            if(obstacles.getArc_components().get(i).getStroke() == Color.RED)
                start_ball_obj.setRed_flag(true);
            if(obstacles.getArc_components().get(i).getStroke() == Color.GREEN)
                start_ball_obj.setGreen_flag(true);
            if(obstacles.getArc_components().get(i).getStroke() == Color.YELLOW)
                start_ball_obj.setYellow_flag(true);
        }

        obstacles.addAnimation(x, y, gp);
        return obstacles;
    }

    private GameObstacles animateObstacle2(AnchorPane gp, float x, float y){
        GameObstacles obstacles = new Obstacle_2();                  //calls game Obstacles
        Rotate rotation1 = new Rotate();
        Rotate rotation2 = new Rotate();
        Rotate rotation3 = new Rotate();
        //System.out.println(start_ball_obj.getStart_ball());
        obstacles.createObstacle(x, y, start_ball_obj.getStart_ball());
        obstacles.getLine_components().get(0).getTransforms().add(rotation1);
        obstacles.getLine_components().get(1).getTransforms().add(rotation2);
        obstacles.getLine_components().get(2).getTransforms().add(rotation3);
        gp.getChildren().addAll(obstacles.getLine_components());

        start_ball_obj.setBlue_flag(false);
        start_ball_obj.setRed_flag(false);
        start_ball_obj.setGreen_flag(false);
        start_ball_obj.setYellow_flag(false);

        for(int i = 0; i<obstacles.getLine_components().size(); ++i){
            if(obstacles.getLine_components().get(i).getStroke() == Color.BLUE)
                start_ball_obj.setBlue_flag(true);
            else if(obstacles.getLine_components().get(i).getStroke() == Color.RED)
                start_ball_obj.setRed_flag(true);
            else if(obstacles.getLine_components().get(i).getStroke() == Color.GREEN)
                start_ball_obj.setGreen_flag(true);
            else if(obstacles.getLine_components().get(i).getStroke() == Color.YELLOW)
                start_ball_obj.setYellow_flag(true);
        }

        obstacles.addAnimation(x, y, gp);
        return  obstacles;
    }


    private GameObstacles animateObstacle3(AnchorPane gp, float x, float y){
        GameObstacles obstacles = new Obstacle_3();                  //calls game Obstacles
        Rotate rotation1 = new Rotate();
        Rotate rotation2 = new Rotate();
        obstacles.createObstacle(x, y, start_ball_obj.getStart_ball());
        obstacles.getLine_components().get(0).getTransforms().add(rotation1);
        obstacles.getLine_components().get(1).getTransforms().add(rotation2);
        gp.getChildren().addAll(obstacles.getLine_components());

        start_ball_obj.setBlue_flag(false);
        start_ball_obj.setRed_flag(false);
        start_ball_obj.setGreen_flag(false);
        start_ball_obj.setYellow_flag(false);

        for(int i = 0; i<obstacles.getLine_components().size(); ++i){
            if(obstacles.getLine_components().get(i).getStroke() == Color.BLUE)
                start_ball_obj.setBlue_flag(true);
            else if(obstacles.getLine_components().get(i).getStroke() == Color.RED)
                start_ball_obj.setRed_flag(true);
            else if(obstacles.getLine_components().get(i).getStroke() == Color.GREEN)
                start_ball_obj.setGreen_flag(true);
            else if(obstacles.getLine_components().get(i).getStroke() == Color.YELLOW)
                start_ball_obj.setYellow_flag(true);
        }

        obstacles.addAnimation(x, y, gp);
        return obstacles;
    }


    private void createSpaceListener(){
        gameScene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (keyEvent.getCode() == KeyCode.SPACE && !jumplock && !isPaused ) {
                    start_ball_obj.setStart_ball_vel_Y(-70.0f);
                    jumplock = true;
                }
            }
        });

        gameScene.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if(keyEvent.getCode() == KeyCode.SPACE)
                    jumplock = false;
            }
        });
    }

    private void createStartBall(){
        start_ball_obj = new Ball(200.0f, 390.0f);           //creates game obstacles object
        start_ball_obj.makeStartBall();
    }

    private void createGameLoop(){
        gameTimer = new AnimationTimer() {                  //Create Animation Timer object
            long prev_time = -1;
            //@Override
            public void   handle(long curTime) {

                    if (prev_time == -1) {
                        prev_time = curTime;
                        return;
                    }

                    double time_per = (curTime - prev_time) / 1000000.0;

                    prev_time = curTime;
                    time_per /= 300.0;
                if(!isPaused) {
                    start_ball_obj.getStart_ball().relocate(start_ball_obj.getStart_ball_pos_X() - 10, start_ball_obj.getStart_ball_pos_Y());
                    start_ball_obj.jump(time_per);
                    start_ball_obj.getStart_ball().relocate(start_ball_obj.getStart_ball_pos_X() - 10, start_ball_obj.getStart_ball_pos_Y());
                    moveBackground();
                    checkCollisionObstacles();
                    checkCollisionPoints();
                    checkCollisionColorSwitch();
                    System.out.println(start_ball_obj.getStart_ball_pos_Y() + " " + start_ball_obj.getStart_ball_vel_Y() + " " + time_per);
                }
            }
        };

        gameTimer.start();
    }


    public void createNewGame(Stage menuStage) throws FileNotFoundException {
        LoadFile loadFile = new LoadFile();
        if(loadFile.loadLeaderboard() != null)
            highScoresdata = loadFile.loadLeaderboard();
        else
            highScoresdata = new LeaderBoard();
        leaderBoardScores = new HashSet<>();
        Iterator<PlayerData> itr = highScoresdata.getLeaderboard().iterator();
        while(itr.hasNext()){
            PlayerData temp = itr.next();
            leaderBoardScores.add(temp.getScore());
        }
        this.menuStage = menuStage;
        this.menuStage.close();
        createStartBall();
        createBackGround();
        createScoreDisplay();
        createPauseButton();
        createSubScenes();
        createSaveGameListener();
        createGameLoop();
        gameStage.show();
}

    public void resumeGame(Stage menuStage, GameData gameData) throws FileNotFoundException {
        this.menuStage = menuStage;
        this.menuStage.hide();
        start_ball_obj = gameData.getStartBall();
        start_ball_obj.reconstructStartBall(gameData);
        createResumeGameBackground(gameData);
        score = gameData.getScore();
        createScoreDisplay();
        createPauseButton();
        createSubScenes();
        createSaveGameListener();
        createGameLoop();
        gameStage.show();
    }

    private void createPauseButton(){
        pauseButton = new ImageView(PAUSE_IMAGE);
        pauseButton.setLayoutX(GAME_WIDTH - 50);
        pauseButton.setLayoutY(15);
        gamePane.getChildren().add(pauseButton);
    }

    private void createSubScenes() throws FileNotFoundException {


        defeatScreen = new GameSubScenes(50 + GAME_WIDTH, 50, GAME_WIDTH - 100, GAME_HEIGHT - 100);
        gamePane.getChildren().add(defeatScreen);

        pauseScreen = new GameSubScenes(50 + GAME_WIDTH, 50, GAME_WIDTH - 100, GAME_HEIGHT - 100);
        gamePane.getChildren().add(pauseScreen);

        Text pauseLabel = new Text("Pause");
        pauseLabel.setLayoutX(110);
        pauseLabel.setLayoutY(55);
        pauseLabel.setFont(Font.loadFont(new FileInputStream("src/model/Resources/AlexBrush-Regular.ttf"), 30));
        pauseScreen.subPane.getChildren().add(pauseLabel);

        Text gameOverLabel = new Text("Game Over");
        gameOverLabel.setLayoutX(90);
        gameOverLabel.setLayoutY(55);
        gameOverLabel.setFont(Font.loadFont(new FileInputStream("src/model/Resources/AlexBrush-Regular.ttf"), 30));
        defeatScreen.subPane.getChildren().add(gameOverLabel);

        resumeButton = new GameButtons("RESUME");
        resumeButton.setLayoutX(55);
        resumeButton.setLayoutY(80);
        pauseScreen.subPane.getChildren().add(resumeButton);

        SpendPointsToContinue = new GameButtons("REVIVE");
        SpendPointsToContinue.setLayoutX(55);
        SpendPointsToContinue.setLayoutY(80);
        defeatScreen.subPane.getChildren().add(SpendPointsToContinue);

        exitToMainMenuButtonDefeat = new GameButtons("EXIT");
        exitToMainMenuButtonDefeat.setLayoutX(55);
        exitToMainMenuButtonDefeat.setLayoutY(80 + 25 + 49);
        defeatScreen.subPane.getChildren().add(exitToMainMenuButtonDefeat);

        restartButton = new GameButtons("RESTART");
        restartButton.setLayoutX(55);
        restartButton.setLayoutY(80 + 15 + 49);
        pauseScreen.subPane.getChildren().add(restartButton);

        exitToMainMenuButtonPause = new GameButtons("EXIT");
        exitToMainMenuButtonPause.setLayoutX(55);
        exitToMainMenuButtonPause.setLayoutY(80 + 30 + 98);
        pauseScreen.subPane.getChildren().add(exitToMainMenuButtonPause);

        SaveAndExitButton = new GameButtons("SAVE/EXIT");
        SaveAndExitButton.setLayoutX(55);
        SaveAndExitButton.setLayoutY(80 + 45 + 147);
        pauseScreen.subPane.getChildren().add(SaveAndExitButton);

        Text finalScore = new Text("Score - " + Integer.toString(score));
        finalScore.setLayoutY(80+25+49+90);
        finalScore.setLayoutX(120);
        try{
            finalScore.setFont(Font.loadFont(new FileInputStream("src/model/Resources/kenvector_future.ttf"), 10));
        } catch (FileNotFoundException e) {
            finalScore.setFont(Font.font("Verdana",23));
        }
        defeatScreen.subPane.getChildren().add(finalScore);
    }

    private void createScoreDisplay(){
        scoreDisplay = new InfoLabel(Integer.toString(score));
        scoreDisplay.setLayoutX(10);
        scoreDisplay.setLayoutY(7);
        scoreDisplay.setTextFill(Color.WHITE);
        gamePane.getChildren().add(scoreDisplay);
    }

    private void createResumeGameScoreDisplay(GameData gameData){
        scoreDisplay = new InfoLabel(Integer.toString(gameData.getScore()));
        scoreDisplay.setLayoutX(10);
        scoreDisplay.setLayoutY(7);
        scoreDisplay.setTextFill(Color.WHITE);
        gamePane.getChildren().add(scoreDisplay);
    }

    private void createResumeGameBackground(GameData gamedata){
        gp1 = new AnchorPane();
        gp2 = new AnchorPane();
        Image background_image = new Image(BACKGROUND_IMAGE, GAME_WIDTH, GAME_HEIGHT, false, true);
        ImageView background_image_gp1 = new ImageView(background_image);
        ImageView background_image_gp2 = new ImageView(background_image);
        gp1.getChildren().add(background_image_gp1);
        gp2.getChildren().add(background_image_gp2);
        if(gamedata.getGp1_layout() > gamedata.getGp2_layout()){
            gp1.setLayoutY(gamedata.getGp1_layout());
            gp2.setLayoutY(gamedata.getGp2_layout()+2);
        }
        else{
            gp1.setLayoutY(gamedata.getGp2_layout());
            gp2.setLayoutY(gamedata.getGp1_layout()+2);
        }
        if(gamedata.isCurPt())
            curPoints = createPoints(gp2);
        else
            curPoints = createPoints(gp1);
        if(gamedata.iscSwitch_flag())
            cur_colorSwitch_obj = createColorSwitch(gp2);
        else
            cur_colorSwitch_obj = createColorSwitch(gp1);
        curObstacle = chooseObstacleUsingLoadData(gp1, GAME_WIDTH/2, 200.0f, gamedata.getCurObstacleAngles(), gamedata.getCurObstacleColors(), gamedata.getCurObstacleID());
        prevObstacle = chooseObstacleUsingLoadData(gp2, GAME_WIDTH/2, 200.0f, gamedata.getPrevObstacleAngles(), gamedata.getPrevObstacleColors(), gamedata.getPrevObstacleID());
        gamePane.getChildren().addAll(gp1, gp2);
        gamePane.getChildren().add(start_ball_obj.getStart_ball());
    }



    private void createBackGround(){
        gp1 = new AnchorPane();
        gp2 = new AnchorPane();
        Image background_image = new Image(BACKGROUND_IMAGE, GAME_WIDTH, GAME_HEIGHT, false, true);
        ImageView background_image_gp1 = new ImageView(background_image);
        ImageView background_image_gp2 = new ImageView(background_image);
        gp1.getChildren().add(background_image_gp1);
        curObstacle = chooseObstacleRandom(gp1, GAME_WIDTH/2, 200.0f);
        curPoints = createPoints(gp1);
        gp2.getChildren().add(background_image_gp2);
        temp2 = chooseObstacleRandom(gp2, GAME_WIDTH/2, 200.0f) ;
        queue_obs.add(temp2);
        nextPoints = createPoints(gp2);
        gp2.setLayoutY(-1 * GAME_HEIGHT);
        cur_colorSwitch_obj = createColorSwitch(gp2);
        gamePane.getChildren().addAll(gp1, gp2);
        gamePane.getChildren().add(start_ball_obj.getStart_ball());
    }

    private void moveBackground(){
        curPane = gp1;
        Image background_image = new Image(BACKGROUND_IMAGE, GAME_WIDTH, GAME_HEIGHT + 20, false, true);
        ImageView background_image_gp = new ImageView(background_image);

        if(start_ball_obj.getStart_ball_pos_Y() < GAME_HEIGHT/2){
            gp1.setLayoutY(gp1.getLayoutY() + 4);
            gp2.setLayoutY(gp2.getLayoutY() + 4);
        }

        if(gp1.getLayoutY() == -270.0f  && startFlag == 0){
            prevObstacle = curObstacle;
            curObstacle = queue_obs.poll();
            curPoints = nextPoints;
            //curPane = gp1;
        }

        if(gp2.getLayoutY()  == -270.0f){
            prevObstacle = curObstacle;
            curObstacle = queue_obs.poll();
            curPoints = nextPoints;
            //curPane = gp2;
        }

        if(gp1.getLayoutY() >= GAME_HEIGHT){
            gp1.setLayoutY(-1 * GAME_HEIGHT);
            gp1.getChildren().removeAll();
            gp1.getChildren().add(background_image_gp);
            gp1_obstacle = chooseObstacleRandom(gp1, GAME_WIDTH/2, 200.0f);
            nextPoints = createPoints(gp1);
            queue_obs.add(gp1_obstacle);
            cur_colorSwitch_obj = createColorSwitch(gp1);
            if(startFlag == 1)
                startFlag = 0;
            curPane = gp2;
        }

        if(gp2.getLayoutY() >= GAME_HEIGHT) {
            gp2.setLayoutY(-1 * GAME_HEIGHT);
            gp2.getChildren().removeAll();
            gp2.getChildren().add(background_image_gp);
            gp2_obstacle = chooseObstacleRandom(gp2, GAME_WIDTH/2, 200.0f);
            nextPoints = createPoints(gp2);
            queue_obs.add(gp2_obstacle);
            cur_colorSwitch_obj = createColorSwitch(gp2);
            curPane = gp1;
        }
    }

    private GameObstacles chooseObstacleUsingLoadData(AnchorPane gp, float x, float y, ArrayList<Double> anglesList, ArrayList<Integer> colorList, int obstacle_id){
        if(obstacle_id == 1){
            return animateObstacle1(gp, x, y, anglesList, colorList);
        }
        return null;
    }

    private GameObstacles chooseObstacleRandom(AnchorPane gp, float x, float y){     //creates random obstacles

        Random chooseObstacle = new Random();
        int obstacle_id = chooseObstacle.nextInt(1)+1;
        if(obstacle_id==1){
            return animateObstacle1(gp, x, y, null, null);
        }

        else if(obstacle_id==2) {
           return animateObstacle2(gp, x, y);
        }

        else if(obstacle_id==3){
            return animateObstacle3(gp, x, y);
        }

         return null;
        //GameObstacles obstacles = animateObstacle2(gp, x ,y);
    }

    private void checkCollisionObstacles() {
        Shape intersect = null;
        if(curObstacle.getArc_components().size() != 0) {
            for (int i = 0; i < curObstacle.getArc_components().size(); ++i) {
                intersect = Shape.intersect(start_ball_obj.getStart_ball(), curObstacle.getArc_components().get(i));
                if ((intersect.getBoundsInLocal().getWidth() != -1) && start_ball_obj.getStart_ball().getFill() != curObstacle.getArc_components().get(i).getStroke() && start_ball_obj.getStart_ball().getFill() != curObstacle.getArc_components().get(i).getFill()) {
                    isPaused = true;
                    curObstacle.getAnimation().pause();
                    if (prevObstacle != null)
                        prevObstacle.getAnimation().pause();
                    BoxBlur bb = new BoxBlur();
                    bb.setWidth(5);
                    bb.setHeight(5);
                    bb.setIterations(3);
                    gp1.setEffect(bb);
                    gp2.setEffect(bb);
                    scoreDisplay.setEffect(bb);
                    pauseButton.setEffect(bb);
                    start_ball_obj.getStart_ball().setEffect(bb);
                    defeatScreen.moveSubScene(-1*GAME_WIDTH);
                    //gameStage.close();
                    //gameTimer.stop();
                }
            }
        }

        if(curObstacle.getLine_components().size() != 0) {
            for (int i = 0; i < curObstacle.getLine_components().size(); ++i) {
                intersect = Shape.intersect(start_ball_obj.getStart_ball(), curObstacle.getLine_components().get(i));
                if ((intersect.getBoundsInLocal().getWidth() != -1) && start_ball_obj.getStart_ball().getFill() != curObstacle.getLine_components().get(i).getStroke() && start_ball_obj.getStart_ball().getFill() != curObstacle.getLine_components().get(i).getFill()) {
                    isPaused = true;
                    curObstacle.getAnimation().pause();
                    if (prevObstacle != null)
                        prevObstacle.getAnimation().pause();
                    BoxBlur bb = new BoxBlur();
                    bb.setWidth(5);
                    bb.setHeight(5);
                    bb.setIterations(3);
                    gp1.setEffect(bb);
                    gp2.setEffect(bb);
                    scoreDisplay.setEffect(bb);
                    pauseButton.setEffect(bb);
                    start_ball_obj.getStart_ball().setEffect(bb);
                    defeatScreen.moveSubScene(-1*GAME_WIDTH);
                    //gameStage.close();
                    //gameTimer.stop();
                }
            }
        }

        if(prevObstacle != null) {
            if(prevObstacle.getArc_components().size() != 0) {
                for (int i = 0; i < prevObstacle.getArc_components().size(); ++i) {
                    intersect = Shape.intersect(start_ball_obj.getStart_ball(), prevObstacle.getArc_components().get(i));
                    if ((intersect.getBoundsInLocal().getWidth() != -1) && start_ball_obj.getStart_ball().getFill() != prevObstacle.getArc_components().get(i).getStroke() && start_ball_obj.getStart_ball().getFill() != prevObstacle.getArc_components().get(i).getFill()) {
                        isPaused = true;
                        curObstacle.getAnimation().pause();
                        if (prevObstacle != null)
                            prevObstacle.getAnimation().pause();
                        BoxBlur bb = new BoxBlur();
                        bb.setWidth(5);
                        bb.setHeight(5);
                        bb.setIterations(3);
                        gp1.setEffect(bb);
                        gp2.setEffect(bb);
                        scoreDisplay.setEffect(bb);
                        pauseButton.setEffect(bb);
                        start_ball_obj.getStart_ball().setEffect(bb);
                        defeatScreen.moveSubScene(-1*GAME_WIDTH);
                        //gameStage.close();
                        //gameTimer.stop();
                    }
                }
            }

            if(prevObstacle.getLine_components().size() != 0) {
                for (int i = 0; i < prevObstacle.getArc_components().size(); ++i) {
                    intersect = Shape.intersect(start_ball_obj.getStart_ball(), prevObstacle.getLine_components().get(i));
                    if ((intersect.getBoundsInLocal().getWidth() != -1) && start_ball_obj.getStart_ball().getFill() != prevObstacle.getLine_components().get(i).getStroke() && start_ball_obj.getStart_ball().getFill() != prevObstacle.getLine_components().get(i).getFill()) {
                        isPaused = true;
                        curObstacle.getAnimation().pause();
                        if (prevObstacle != null)
                            prevObstacle.getAnimation().pause();
                        BoxBlur bb = new BoxBlur();
                        bb.setWidth(5);
                        bb.setHeight(5);
                        bb.setIterations(3);
                        gp1.setEffect(bb);
                        gp2.setEffect(bb);
                        scoreDisplay.setEffect(bb);
                        pauseButton.setEffect(bb);
                        start_ball_obj.getStart_ball().setEffect(bb);
                        defeatScreen.moveSubScene(-1*GAME_WIDTH);
                      //gameStage.close();
                      //gameTimer.stop();
                    }
                }
            }
        }

    }

    private void checkCollisionPoints(){
        Shape intersect;
        //System.out.println(points_obj.getFlag());
        if(curPoints.getFlag() == true){
            intersect = Shape.intersect(start_ball_obj.getStart_ball(), curPoints.getPoint());
            //System.out.println((intersect.getBoundsInLocal().getWidth() != -1));
            if(intersect.getBoundsInLocal().getWidth() != -1){
                ++score;
                scoreDisplay.setText(Integer.toString(score));
                curPoints.setFlag(false);
                curPoints.getPoint().setOpacity(0);
            }
        }
    }

    private void checkCollisionColorSwitch(){
        Shape intersect;
        GameAnimations colorAnimation = new GameAnimations();
        if(cur_colorSwitch_obj.getCs_flag() == true){
            intersect = Shape.intersect(start_ball_obj.getStart_ball(), colorSwitch.get(2));
            if(intersect.getBoundsInLocal().getWidth() != -1){
                colorAnimation.changeColor(start_ball_obj);
                cur_colorSwitch_obj.setCs_flag(false);
                for(int i=0; i<colorSwitch.size(); ++i){
                    colorSwitch.get(i).setOpacity(0);
                }
            }
        }
    }



    private ColorSwitch createColorSwitch(AnchorPane gp){
        ColorSwitch colorSwitch_obj = new ColorSwitch();
        colorSwitch_obj.setCs_flag(true);
        colorSwitch = colorSwitch_obj.makeColorSwitch(GAME_WIDTH/2, GAME_HEIGHT - 20.0f);
        gp.getChildren().addAll(colorSwitch);
        return colorSwitch_obj;
    }

    private Points createPoints(AnchorPane gp){
        points_obj = new Points();
        points_obj.setFlag(true);
        points_obj.makePoints(GAME_WIDTH/2, 200.0f);
        gp.getChildren().add(points_obj.getPoint());
        return points_obj;
    }
}
