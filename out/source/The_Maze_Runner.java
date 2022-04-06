import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.*; 
import java.text.DecimalFormat; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class The_Maze_Runner extends PApplet {



MazeMaker mainMaze;
Player mainPlayer;

ArrayList<Ray> allRays = new ArrayList<Ray>();

float mainSceneW = 810; // 3D scene's width
float mainSceneH = 420; // 3D scene's height

boolean[] direction = new boolean[4]; // users' input

PFont font;
StopWatch clock;
ArrayList<Button> allButtons = new ArrayList<Button>();

int gameStatus = 0; // 0 - intro; 1 - instructions; 2 - in game; 3 - game over
boolean hardCoreMode = false;
PImage[] arrowsImg = new PImage[4];
boolean isMoving = false;

int completions = 0; // number of times the players has gone through the maze
float bestTime = 0;  // the best/fastest time of completion
public void setup() {
    
    mainMaze = new MazeMaker(width/2-225, height-250, 450, 240);
    mainPlayer = new Player(mainMaze, mainMaze.getSquare(0,0));

    clock = new StopWatch();

    allButtons.add(new Button("Start", new PVector(width/2, height/2), 30, true, color(0,123,255), color(255,255,255)));
    allButtons.add(new Button("How to Play", new PVector(width/2, height*7/10), 30, true, color(0,123,255), color(255,255,255))); 
    allButtons.add(new Button("Back", new PVector(width/2, height*8/10 + 50), 30, false, color(0,123,255), color(255,255,255))); 
    allButtons.add(new Button("Solution", new PVector(949, 529), 20, false, color(0,123,255), color(0)));
    allButtons.add(new Button("HardCore", new PVector(949, 599), 20, false, color(0,123,255), color(0)));
    allButtons.add(new Button("Quit", new PVector(949, 669), 20, false, color(0,123,255), color(0)));

    arrowsImg[0] = loadImage("up_arrow.png");
    arrowsImg[1] = loadImage("right_arrow.png");
    arrowsImg[2] = loadImage("down_arrow.png");
    arrowsImg[3] = loadImage("left_arrow.png");
    font = createFont("MunaBold", 16, true);
    textFont(font);   
}

public void draw() {
    background(100);    
    drawMainScene(); // always draw this scene in the background;
    
    if(gameStatus == 0)
      startMenuScene();
    else if(gameStatus == 1)
      instructionScene();
    else if(gameStatus == 3)
      endScene();
    
    displayButtons();
    println("completions: "+completions);
    // rect(mouseX, mouseY, 40, 20);
    // println("mouseX: "+mouseX + " mouseY: " + mouseY);
}

public void startMenuScene(){
  rectMode(CORNER);
  fill(0, 255);
  rect(0,0,width, height);
}

public void endScene(){

}

public void drawMainScene(){ 
    if(isMoving == true && !clock.running) // preliminary check
      clock.start();
    if(mainPlayer.isDone){
      clock.stop();
      isMoving = false;
      completions++;
      bestTime = (clock.getEllapsedTime() > bestTime) ? clock.getEllapsedTime() : bestTime;

      mainPlayer.isDone = false;
    }
      
    mainMaze.display(hardCoreMode);
    mainPlayer.action(direction);
    clock.display();
    

    rectMode(CENTER); // draw the 3D scene
    noStroke();
    
    pushMatrix();
    translate(width/2, 231);
    fill(0xff039be5);
    rect(0, 0, mainSceneW, mainSceneH); // draw the background
    
    // rectMode(CORNER);
    float sliceWidth = mainSceneW/mainPlayer.playerVisibility.size(); 
    float max = 100;
    for(int x = 0; x < mainPlayer.playerVisibility.size();x++){ // each slice corresponds to one ray
        float dist = mainPlayer.playerVisibility.get(x).distanceToIntersection();

        // projection of ray onto the camera --> fix the fish eye effects    
        dist *= cos(radians(mainPlayer.playerVisibility.get(x).heading - mainPlayer.heading)); 
        if(dist>max) max = dist; // change the maximum distance to avoid random rendering bug
        float brightness = map(dist - mainPlayer.size/2, 0, max, 255, 0);
        float sliceHeight = map(dist - mainPlayer.size/2, 0, max, mainSceneH, 0);
        if(mainPlayer.playerVisibility.get(x).facingEntry)
          fill(0,255,0,brightness);
        else if(mainPlayer.playerVisibility.get(x).facingExit)
          fill(0,0,255,brightness); 
        else fill(0xffC07F80, brightness);
        rect(x*sliceWidth - mainSceneW/2 + sliceWidth/2, 0, sliceWidth, sliceHeight);
    }
    popMatrix();
}

public boolean setDirection (int k, boolean isOn) { // record pressed keys (direction)
  switch(k) {
  case LEFT:
    direction[0] = isOn;    
    break;
  case UP:
    direction[1] = isOn;
    break;
  case RIGHT:
    direction[2] = isOn;
    break;
  case DOWN:
    direction[3] = isOn;
    break;
  default:
    return false; // no arrows have been pressed
  }
  return true;
}

public void displayButtons(){
  for(int x = 0; x < allButtons.size(); x++){
    if(allButtons.get(x).isActive)
      allButtons.get(x).display();
  }
}

// main event functions that control the game -- BUTTONS
public void startGame(){
  gameStatus = 2;

  allButtons.get(0).deactivate();
  allButtons.get(1).deactivate();
  allButtons.get(3).activate();
  allButtons.get(4).activate();
  allButtons.get(5).activate();
}

public void howToPlay(){
  allButtons.get(0).deactivate();
  allButtons.get(1).deactivate();
  allButtons.get(2).activate();
  
  gameStatus = 1;
}

public void returnToIntro(){
  allButtons.get(2).deactivate();
  allButtons.get(0).activate();
  allButtons.get(1).activate();  

  gameStatus = 0;
}

public void instructionScene(){
  startMenuScene(); // borrow the black background in the start menu
  imageMode(CENTER);
  
  pushMatrix();
  translate(width/2, height/3 + 50);
  for(int x = 0; x <= 270; x+=90){ // just a smart way to cycle through the different directions and offset them
    float xOffSet = sin(radians(x)) * 150;
    float yOffSet = cos(radians(x)) * -150;  
    int c = 255;
    if(direction[(x/90 + 1)%4])
      c = 0xff00FFFF;
    tint(c);
    arrowsImg[x/90].resize(150, 0);
    image(arrowsImg[x/90], xOffSet, yOffSet);
    
    fill(c);
    if(x == 0){ // label the arrows
      text("Forward", xOffSet, yOffSet - 80);
    }else if(x/90 % 2 != 0){
      pushMatrix();
      translate(xOffSet + sin(radians(x))*80, yOffSet + cos(radians(x))*-80);
      rotate(radians(x)); // make them appear vertically
      text("Rotate", 0, 0);
      popMatrix();
      
    }else{
      text("Backward", xOffSet, yOffSet + 100);
    }
  }
  popMatrix();
}

public void buttonEvent(int idx){
  switch(idx){
    case 0:
      startGame();
      break;
    case 1:
      howToPlay();
      break;
    case 2:
      returnToIntro();
      break;
    case 3:
      mainMaze.revealSolution();
      break;
    case 4:
      hardCoreMode = !hardCoreMode; // able to toggle the mode
      break;
  }
}

public void mouseClicked(){
  for(int x = 0; x < allButtons.size(); x++){
    if(allButtons.get(x).isActive && allButtons.get(x).overBox()){
      buttonEvent(x);
      break;
    }
  }
}

public void keyPressed() {
  if (key == CODED) 
    isMoving = setDirection(keyCode, true) && gameStatus == 2;
}

public void keyReleased() {
  if(key==CODED) setDirection(keyCode, false);
}
public class Button {
    final String message;
    private float buttonHeight;
    private float buttonWidth;
    private int buttonColor;
    private int txtColor;
    private float widthScalar = 2;
    private float heightScalar = 2;
    boolean isActive = false; // depends one which scene, a button can be either activated or deactivated
    PVector pos;
    float fontSize;
    
    public Button (String message, PVector pos, float fontSize, boolean isActive, int buttonColor, int txtColor) {
        this.message = message;
        this.pos = pos;
        this.fontSize = fontSize;
        this.buttonColor = buttonColor;
        this.txtColor = txtColor;
        this.isActive = isActive;
    }

    public void calculateTextBox(){
        textSize(fontSize);
        buttonHeight = textAscent() * heightScalar;
        buttonWidth = textWidth(message) * widthScalar;
    }

    public boolean overBox(){ // detect if the mouse is hovering over the box
        return(mouseX > pos.x - buttonWidth/2 && mouseX < pos.x + buttonWidth/2)
            && mouseY > (pos.y - buttonHeight/2)
                && mouseY < (pos.y + buttonHeight/2);
    }

    public void display(){
        calculateTextBox();
        rectMode(CENTER);

        noStroke();
        fill(buttonColor); // draw plain button
        rect(pos.x, pos.y, buttonWidth, buttonHeight);
        
        textAlign(CENTER);
        fill(txtColor); // write the text onto the button
        text(message, pos.x, pos.y + buttonHeight/(heightScalar * 2)); // padding (or scale) * 2 will make the text appear in the middle
    }

    public void activate(){
        isActive = true;
    }

    public void deactivate(){
        isActive = false;
    }
}
public class MazeMaker { // create the maze
    PVector size; 
    final PVector loc;    
    int rows;
    int columns;

    int squareSize = 15;
    ArrayList<ArrayList<MazeSquare>> allSquares = new ArrayList<ArrayList<MazeSquare>>(); // 2D array to replicate the grid
    ArrayList<MazeSquare> solution = new ArrayList<MazeSquare>();
    Stack<MazeSquare> visitedSquareStack = new Stack<MazeSquare>();

    final float mazeWidth;
    final float mazeHeight;
    public MazeMaker (float xPos, float yPos, float mazeWidth, float mazeHeight) {
        loc = new PVector(xPos, yPos);
        size = new PVector(mazeWidth, mazeHeight);
        rows = PApplet.parseInt(mazeHeight/squareSize); // 10 = square's size
        columns = PApplet.parseInt(mazeWidth/squareSize);
        this.mazeWidth = mazeWidth;
        this.mazeHeight = mazeHeight;
        createGrid();
        makeMaze();
    }

    public void makeMaze(){
        visitedSquareStack.push(allSquares.get(0).get(0)); // inital cell is always the first square on top left
        allSquares.get(0).get(0).visit();
        allSquares.get(0).get(0).removeSide(0);

        ArrayList<MazeSquare> availableNeighbor = new ArrayList<MazeSquare>();
        MazeSquare lastRightSquare = allSquares.get(0).get(0);
        boolean reached = false;
        while(!visitedSquareStack.empty()){
            MazeSquare currSquare = visitedSquareStack.pop();

            int[] neighborIdx = checkNeighbor(currSquare);
            
            for(int x = 0; x < 4; x++){
                if(neighborIdx[x] == -1) continue;
                    
                if(x%2 == 0 && !allSquares.get(currSquare.getIdx()[1]).get(neighborIdx[x]).hasVisited())
                    availableNeighbor.add(allSquares.get(currSquare.getIdx()[1]).get(neighborIdx[x]));
                else if(x%2 != 0 && !allSquares.get(neighborIdx[x]).get(currSquare.getIdx()[0]).hasVisited())   
                    availableNeighbor.add(allSquares.get(neighborIdx[x]).get(currSquare.getIdx()[0]));
            }

            if(reached) {
                solution.add(currSquare);
            }
            
            if(availableNeighbor.size() == 0) {
                if(currSquare.getIdx()[0] == columns-1 && currSquare.getIdx()[1] == rows-1 || currSquare == lastRightSquare){
                    
                    reached = true;
                }
                continue;
            }else if(availableNeighbor.size() != 0 && reached){
                lastRightSquare = currSquare;
                reached = false;
            }

            visitedSquareStack.push(currSquare);

            int tempRandomNeighborIdx = PApplet.parseInt(round(random(0, availableNeighbor.size()-1)));
            currSquare.removeSide(availableNeighbor.get(tempRandomNeighborIdx)); // remove adjacent sides
            availableNeighbor.get(tempRandomNeighborIdx).removeSide(currSquare);


            availableNeighbor.get(tempRandomNeighborIdx).visit(); // mark the chosen neighbor visited
            visitedSquareStack.push(availableNeighbor.get(tempRandomNeighborIdx));      
            availableNeighbor.clear(); // reset
        }

        allSquares.get(rows-1).get(columns-1).removeSide(2);
        allSquares.get(rows-1).get(columns-1).isCorrect = true;
    }

    public void revealSolution(){
        for(int x = 0; x < solution.size(); x++){
            solution.get(x).isCorrect = true;   
        }
    }

    public void reset(){ // delete all the squares + create a new maze
        solution.clear();
        allSquares.clear();
        createGrid();
        makeMaze();
    }

    public int[] checkNeighbor(MazeSquare square){
        int[] possibleNeighbors = new int[4];
        for(int x = 0; x<4;x++) possibleNeighbors[x] = -1;
        if(square.getIdx()[0] - 1 >= 0){
            possibleNeighbors[0] = square.getIdx()[0] - 1;
        }
        if(square.getIdx()[1] - 1 >= 0){
            possibleNeighbors[1] = square.getIdx()[1] - 1;
        }
        if(square.getIdx()[0] + 1  < columns){
           possibleNeighbors[2] = square.getIdx()[0] + 1; 
        }            
        if(square.getIdx()[1] + 1 < rows){
           possibleNeighbors[3] = square.getIdx()[1] + 1;
        }        
        return possibleNeighbors;
    }

    public void createGrid(){
        for(int y = 0; y < rows; y++){            
            allSquares.add(new ArrayList<MazeSquare>());
            for(int x = 0; x < columns; x++){    
                allSquares.get(y).add(new MazeSquare(x*squareSize, y*squareSize, squareSize, new int[] {x,y}));
            }
        }
    }

    public void drawGrid(){
        pushMatrix();
        translate(loc.x, loc.y); // reset the grid to make it easer to draw the squares
        for(int y = 0; y < rows; y++){
            for(int x = 0; x<columns; x++){
                allSquares.get(y).get(x).display();
                // println(allSquares.get(y).get(x).info().x + " " + allSquares.get(y).get(x).info().y);
            }
        }
        popMatrix();
    }

    public void drawBackground(){
        fill(0);
        rectMode(CORNER);
        noStroke();
        rect(loc.x, loc.y, size.x, size.y);        
    }

    public void display(boolean isHardCore){
        drawBackground();
        if(!isHardCore) drawGrid(); // the 2D map is invisible in hardocre mode (except for visited ones)
    }

    public MazeSquare getSquare(int rowIdx, int colIdx){ // return the specified square
        if(rowIdx <= -1)
            return allSquares.get(rows-1).get(columns-1);
        else if(rowIdx >= rows)
            return allSquares.get(0).get(0); 
        return allSquares.get(rowIdx).get(colIdx);
    }

    public PVector getLoc(){
        return loc; 
    }
}
PVector[] verticies = {new PVector(0,0), new PVector(1,0),
    new PVector(1,1), new PVector(0,1)}; // starts top-left then go clock-wise

public class MazeSquare{
    final PVector loc; // prevent these from being changed later on
    final int[] idx;
    boolean[] isClosed = {true, true, true, true};
    int wallColor = color(255);

    float size;
    boolean alreadyVisited = false;
    boolean isCorrect = false;
    public MazeSquare (float xPos, float yPos, float size, int[] idx) {
        loc = new PVector(xPos, yPos);
        this.size = size;
        this.idx = idx;
    }

    public void display(){
        pushMatrix();
        translate(loc.x, loc.y); // move to desired location
        if(isCorrect){
            fill(255,0,0);
            noStroke();
            rectMode(CORNER);
            rect(0, 0, size, size);
        }

        stroke(wallColor);
        for(int x = 0; x < 4; x++){
            if(isClosed[x]){
                if(x != 3)
                    line(verticies[x].x*size, verticies[x].y*size, verticies[x+1].x*size, verticies[x+1].y*size);
                else
                    line(verticies[x].x*size, verticies[x].y*size, verticies[0].x*size, verticies[0].y*size);  
            }
        }
        
        popMatrix();
        
    }

    public void changeColor(int wallColor){
        this.wallColor = wallColor;
    }

    public PVector getLocation(){
        return loc.copy();
    }

    public float[] getBoundary(){
        float[] boundary = new float[4];
        for(int x = 0; x < 4; x++){
            if(x%2 == 0){ // top and bottom
                    boundary[x] = loc.y + verticies[x].y*size;
                }else{
                    boundary[x] = loc.x + verticies[x].x*size;
                }
        }
        return boundary;
    }

    public PVector[] getBoundaryVerticies(){ 
       PVector[] boundary = new PVector[4];
       
       for(int x = 0; x < 4; x++){
           boundary[x] = new PVector(loc.x + verticies[x].x*size, loc.y + verticies[x].y*size);
        }
        return boundary;
    }

    public int[] getIdx(){
        return idx.clone();
    }
    
    public void removeSide(MazeSquare neighbor){
        // 0 - left; 1 - top; 2 - right; 3 - bottom
        int tempColumnIdxDiff = neighbor.getIdx()[0] - idx[0]; 
        int tempRowIdxDiff = neighbor.getIdx()[1] - idx[1];

        if(tempColumnIdxDiff < 0)
            isClosed[3] = false;
        else if(tempRowIdxDiff < 0)
            isClosed[0] = false;
        else if(tempColumnIdxDiff > 0)
            isClosed[1] = false;
        else if(tempRowIdxDiff > 0)
            isClosed[2] = false;
    }

    public void removeSide(int side){
        isClosed[side] = false;
    }

    public void addSide(int side){
        // 3 - left; 0 - top; 1 - right; 2 - bottom        
        isClosed[side] = true;
    }

    public void visit(){
        alreadyVisited = true;
    }

    public boolean hasVisited(){
        return alreadyVisited;
    }
}
public class Player {
    PVector loc = new PVector(0,0);
    float speed = 0.5f;
    int size = 6;
    float heading = 0;
    boolean isDone = false;

    MazeSquare currSquare;
    int[] currSquareIdx;
    MazeMaker maze;
    boolean[] bufferZones = {false, false, false, false}; // the zone between actual boundary and collision boundary
                                                            // top - right - bottom - left
    HashSet<MazeSquare> track = new HashSet<MazeSquare>();
    ArrayList<Ray> playerVisibility = new ArrayList<Ray>();
    public Player (MazeMaker maze, MazeSquare firstSquare) {
        this.maze = maze;
        currSquare = firstSquare;
        currSquareIdx = currSquare.getIdx();
        loc.x = firstSquare.getLocation().x + firstSquare.size/2;
        loc.y = firstSquare.getLocation().y + firstSquare.size/2;
    }

    public void move(boolean[] input){
        if(gameStatus != 2) return;
        currSquare = maze.getSquare(currSquareIdx[1], currSquareIdx[0]);

        PVector direction;
        if(input[0]) // rotate
            heading += -2;
        else if(input[2])
            heading += 2;
        direction = PVector.fromAngle(radians(heading));
        
        if(input[1]) // move
            direction.setMag(speed);
        else if(input[3])
            direction.setMag(-speed);
        else {
            direction.setMag(0);
        }

        float[] boundary = currSquare.getBoundary(); 
        PVector futureLoc = PVector.add(direction, loc);
        
        if(futureLoc.x <= boundary[3] + size/2){ // collision boundary
            if(currSquare.isClosed[3])
                direction.setMag(0);
            else if(futureLoc.x < boundary[3]) currSquareIdx[0]--; // actual boundary
        }else if(futureLoc.x >= boundary[1] - size/2){ // collision boundary
            if(currSquare.isClosed[1])
                direction.setMag(0);
            else if(futureLoc.x > boundary[1]) currSquareIdx[0]++; // actual boundary
        }
        if(futureLoc.y <= boundary[0] + size/2){ // collision boundary
            if(currSquare.isClosed[0])
                direction.setMag(0);
            else if(futureLoc.y < boundary[0]) currSquareIdx[1]--; // actual boundary
        }else if(futureLoc.y >= boundary[2] - size/2){ // collision boundary
            if(currSquare.isClosed[2])
                direction.setMag(0);
            else if(futureLoc.y > boundary[2]) currSquareIdx[1]++; // actual boundary    
        }
        
        loc.add(direction); // actually move after all the checks
        if(currSquareIdx[1] == maze.rows) { // reset when the end is reached
                track.clear();
                maze.reset(); 
        }else track.add(currSquare); // record the path
        
        
        bufferZones[0] = (boundary[0] - size/2.f <= loc.y && loc.y < boundary[0] + size/2.f); // buffer zone  
        bufferZones[1] = (boundary[1] - size/2.f < loc.x && loc.x <= boundary[1] + size/2.f); // buffer zone
        bufferZones[2] = (boundary[2] - size/2.f < loc.y && loc.y <= boundary[2] + size/2.f); // buffer zone  
        bufferZones[3] = (boundary[3] - size/2.f <= loc.x && loc.x < boundary[3] + size/2.f); // buffer zone 
    }

    public int checkBuffer(){
        for(int x = 0; x < 4; x++){ // return the index of the activated buffer zone
            if(bufferZones[x]) return x;
        }  
        return -1;
    }

    public boolean castRay(Ray targetRay, MazeSquare targetSquare, int entrySide){
       int intersectedSide = -1;       
       PVector[] squareBoundary = targetSquare.getBoundaryVerticies(); // get the boundary

       for(int z = 0; z < 4; z++){
            if(entrySide == z) continue; // prevent infinite recursion
            if(z != 3){
                intersectedSide = (targetRay.intersect(squareBoundary[z], squareBoundary[z+1])) ? z : intersectedSide;
            }else{
                intersectedSide = (targetRay.intersect(squareBoundary[0], squareBoundary[3])) ? 3 : intersectedSide;
            }
        } 
        if(intersectedSide == -1) return false; // precaution against when the ray doesn't intersect any of the 4 sides for some reason

        int[] squareIdx = targetSquare.getIdx();
        
        if((Arrays.equals(squareIdx, new int[]{0,0}) && intersectedSide == 0) || 
            (Arrays.equals(squareIdx, new int[]{maze.columns-1,maze.rows-1}) && intersectedSide == 2)){
            return true;
        }
        if(!targetSquare.isClosed[intersectedSide]){ // check if the intersected side is open
            if(intersectedSide == 0) // if open go to the next square
                squareIdx[1]--; // go up one row
            else if(intersectedSide == 1)
                squareIdx[0]++; // go right one column
            else if(intersectedSide == 2)
                squareIdx[1]++; // go down one row
            else if(intersectedSide == 3)
                squareIdx[0]--; // go left one column
            intersectedSide += (intersectedSide <= 1) ? 2 : -2; // top of one square is bot of the other; same for left and right
            return castRay(targetRay, maze.getSquare(squareIdx[1], squareIdx[0]), intersectedSide);
        }

        return true; 
    }

    public void detectWalls(){ // gives the player's visibility
        playerVisibility.clear(); // reset everytime

        for(float theta = -30+ heading; theta<=30+heading; theta+=0.5f){
            Ray temp = new Ray(this.loc.copy(), theta);
            if(!castRay(temp, currSquare, -1) && checkBuffer() != -1){ // if in buffer zone, must check the next square as well
                if(checkBuffer() == 0)
                    castRay(temp, maze.getSquare(currSquare.getIdx()[1]-1, currSquare.getIdx()[0]), -1);
                else if(checkBuffer() == 1)
                    castRay(temp, maze.getSquare(currSquare.getIdx()[1], currSquare.getIdx()[0] + 1), -1);
                else if(checkBuffer() == 2)
                    castRay(temp, maze.getSquare(currSquare.getIdx()[1]+1, currSquare.getIdx()[0]), -1);
                else if(checkBuffer() == 3)
                    castRay(temp, maze.getSquare(currSquare.getIdx()[1], currSquare.getIdx()[0]-1), -1);
                
            }
            if(temp.intersection != null) playerVisibility.add(temp);
        }
        
        for(Ray r: playerVisibility)
            r.connectIntersect(); // display the rays
    }

    public void display(){
        pushMatrix();
        translate(maze.getLoc().x, maze.getLoc().y);
        Iterator<MazeSquare> x = track.iterator(); // keep track of player's path
        while(x.hasNext()){
            x.next().display(); // only displays squares that player's have been through
        }
        
        ellipseMode(CENTER);
        noStroke();
        fill(0,255,0);
        if(currSquareIdx[1] < 0){
            loc.y = maze.getSquare(maze.rows - 1, maze.columns - 1).getLocation().y + maze.squareSize;
            loc.x += maze.getSquare(maze.rows - 1, maze.columns - 1).getLocation().x; 

            currSquareIdx = maze.getSquare(maze.rows - 1, maze.columns - 1).getIdx(); 
        }else if(currSquareIdx[1] >= maze.allSquares.size()){
            maze.makeMaze(); // restart the maze
            loc.y = maze.getSquare(0,0).getLocation().y;
            loc.x -= currSquare.getLocation().x;
            isDone = true; 
            currSquareIdx[0] = 0;
            currSquareIdx[1] = 0;
        }
        detectWalls();
        circle(loc.x, loc.y, size);
        popMatrix();        
    }

    public void action(boolean[] input){
        move(input);
        display();
    }
}
public class Ray{
    PVector pos;
    PVector direction = new PVector(0,0);
    PVector intersection = null;
    float heading;

    boolean facingEntry = false;
    boolean facingExit = false;
    public Ray (PVector pos, float angle) {
        this.pos = pos;
        direction.x = cos(radians(angle));
        direction.y = sin(radians(angle));
        this.heading = angle;
    }

    public boolean intersect(PVector start, PVector end){
        // L1 = boundary; L2 = ray
        // L1: (x1, y1) = start; (x2, y2) = end
        // L2: (x3, y3) = pos; (x4, y4) = pos + direction
        // https://en.wikipedia.org/wiki/Line%E2%80%93line_intersection

        float x1 = start.x; // boundary
        float y1 = start.y;
        float x2 = end.x;
        float y2 = end.y;

        float x3 = pos.x; // ray
        float y3 = pos.y;
        float x4 = direction.x + pos.x;
        float y4 = direction.y + pos.y;

        float denominator = (x1-x2)*(y3-y4)-(y1-y2)*(x3-x4);
        float t = ((x1-x3)*(y3-y4)-(y1-y3)*(x3-x4))/denominator;
        float u = ((x1-x3)*(y1-y2)-(y1-y3)*(x1-x2))/denominator; 

        if((0 < t && t < 1) && u > 0){
            PVector temp = new PVector(
                (x1 + t*(x2-x1)), (y1 + t*(y2-y1))
            );
            intersection = temp;
            if(intersection.x >= 0 && intersection.x<=15
                && intersection.y == 0)
                facingEntry = true;
            else if(intersection.x >= 435 && intersection.x <= 450
                && intersection.y == 240)
                facingExit = true;
            return true;
        }
        return false;
    }

    public float distanceToIntersection(){
        return dist(pos.x, pos.y, intersection.x, intersection.y);
    }

    public void connectIntersect(){
        stroke(255);  
        line(pos.x, pos.y, intersection.x , intersection.y);
    }
}
public class StopWatch {
    float startTime = 0;
    float endTime = 0;
    boolean running = false;

    public void start(){
        if(running) return;
        startTime = millis();
        running = true;
    }

    public void stop(){
        if(!running) return;
        endTime = millis();
        running = false;
    }

    public float getEllapsedTime(){
        if(running) return millis()-startTime;
        return endTime - startTime;
    }

    
    public float millisecond(float t){
        return t % 100; // round to 2 decimals
    }
    
    public float second(float t){ // calculate any given value in ms
        return round(t/1000) % 60;
    }

    public float minute(float t){
        return round(t/(1000*60)) % 60;
    }

    public String timeInText(float t){
        int s;
        int m;
        int ms;
        if(t == -1){ // not calculating specified time -- just the current ellapsed time in general
            s = PApplet.parseInt(this.second(this.getEllapsedTime()));
            m = PApplet.parseInt(this.minute(this.getEllapsedTime()));
            ms = PApplet.parseInt(this.millisecond(this.getEllapsedTime()));
        }else{
            ms = PApplet.parseInt(this.millisecond(t));
            s = PApplet.parseInt(this.second(t));
            m = PApplet.parseInt(this.minute(t));
        }
        

        DecimalFormat df = new DecimalFormat("00");
        
        return df.format(m) + ":" + df.format(s) + "." + df.format(ms);
    }

    public void display(){
        String time = this.timeInText(-1);
        textFont(font, 20);
        noStroke();
        rectMode(CENTER);
        fill(0);
        rect(106, 585, 65, textAscent());
        
        fill(0,255,0);
        text(time, 106, 585 + textAscent()/4);   
    }
}
  public void settings() {  size(1080, 720); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "The_Maze_Runner" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
