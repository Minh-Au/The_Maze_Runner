import java.util.*;
import java.text.DecimalFormat;
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
PImage[] arrowsImg = new PImage[4];

void setup() {
    size(1080, 720);
    mainMaze = new MazeMaker(width/2-225, height-250, 450, 240);
    mainPlayer = new Player(mainMaze, mainMaze.getSquare(0,0));

    clock = new StopWatch();

    allButtons.add(new Button("Start", new PVector(width/2, height/2), 30, true, color(0,0,0), color(255,255,255)));
    allButtons.add(new Button("How to Play", new PVector(width/2, height*7/10), 30, true, color(0,0,0), color(255,255,255))); 
    allButtons.add(new Button("Back", new PVector(width/2, height*8/10), 30, false, color(0,0,0), color(255,255,255))); 
    
    arrowsImg[0] = loadImage("up_arrow.png");
    arrowsImg[1] = loadImage("right_arrow.png");
    arrowsImg[2] = loadImage("down_arrow.png");
    arrowsImg[3] = loadImage("left_arrow.png");
    font = createFont("MunaBold", 16, true);
    textFont(font);   
}

void draw() {
    background(100);    
    drawMainScene(); // always draw this scene in the background;
    
    if(gameStatus == 0)
      drawWaitingScene();
    else if(gameStatus == 1)
      instructionScene();
    else if(gameStatus == 3)
      drawingEndingScene();
    
    displayButtons();
    
    
    
}

void drawWaitingScene(){
  rectMode(CORNER);
  fill(0, 255);
  rect(0,0,width, height);
}

void drawingEndingScene(){

}

void drawMainScene(){ 
    mainMaze.display();
    mainPlayer.action(direction);
    clock.display();

    rectMode(CENTER); // draw the 3D scene
    noStroke();
    
    pushMatrix();
    translate(width/2, 231);
    fill(#039be5);
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
        else fill(#C07F80, brightness);
        rect(x*sliceWidth - mainSceneW/2 + sliceWidth/2, 0, sliceWidth, sliceHeight);
    }
    popMatrix();
}

void setDirection (int k, boolean isOn) { // record pressed keys (direction)
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
  }
}

void displayButtons(){
  for(int x = 0; x < allButtons.size(); x++){
    if(allButtons.get(x).isActive)
      allButtons.get(x).display();
  }
}

void startGame(){
  gameStatus = 2;

  allButtons.get(0).deactivate();
  allButtons.get(1).deactivate();
}

void howToPlay(){
  allButtons.get(0).deactivate();
  allButtons.get(1).deactivate();
  allButtons.get(2).activate();
  
  gameStatus = 1;
}

void instructionScene(){
  drawWaitingScene();
  imageMode(CENTER);
  
  pushMatrix();
  translate(width/2, height/3);
  for(int x = 0; x <= 270; x+=90){ // just a smart way to cycle through the different directions and offset them
    if(direction[(x/90 + 1)%4])
      tint(#00FFFF);
    else tint(255);
    arrowsImg[x/90].resize(150, 0);
    image(arrowsImg[x/90], sin(radians(x)) * 150, cos(radians(x)) * -150);
  }
  popMatrix();
}

void returnToIntro(){
  allButtons.get(2).deactivate();
  allButtons.get(0).activate();
  allButtons.get(1).activate();  

  gameStatus = 0;
}

void buttonEvent(int idx){
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
      break;
    case 4:
      break;
  }
}

void mouseClicked(){
  for(int x = 0; x < allButtons.size(); x++){
    if(allButtons.get(x).isActive && allButtons.get(x).overBox()){
      buttonEvent(x);
      println(x);
      break;
    }
  }
}

void keyPressed() {
  if (key == CODED) setDirection(keyCode, true);
}

void keyReleased() {
  if(key==CODED) setDirection(keyCode, false);
}
