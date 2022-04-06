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
        rows = int(mazeHeight/squareSize); // 10 = square's size
        columns = int(mazeWidth/squareSize);
        this.mazeWidth = mazeWidth;
        this.mazeHeight = mazeHeight;
        createGrid();
        makeMaze();
    }

    void makeMaze(){
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

            int tempRandomNeighborIdx = int(round(random(0, availableNeighbor.size()-1)));
            currSquare.removeSide(availableNeighbor.get(tempRandomNeighborIdx)); // remove adjacent sides
            availableNeighbor.get(tempRandomNeighborIdx).removeSide(currSquare);


            availableNeighbor.get(tempRandomNeighborIdx).visit(); // mark the chosen neighbor visited
            visitedSquareStack.push(availableNeighbor.get(tempRandomNeighborIdx));      
            availableNeighbor.clear(); // reset
        }

        allSquares.get(rows-1).get(columns-1).removeSide(2);
        allSquares.get(rows-1).get(columns-1).isCorrect = true;
    }

    void revealSolution(){
        for(int x = 0; x < solution.size(); x++){
            solution.get(x).isCorrect = true;   
        }
    }

    void reset(){ // delete all the squares + create a new maze
        solution.clear();
        allSquares.clear();
        createGrid();
        makeMaze();
    }

    int[] checkNeighbor(MazeSquare square){
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

    void createGrid(){
        for(int y = 0; y < rows; y++){            
            allSquares.add(new ArrayList<MazeSquare>());
            for(int x = 0; x < columns; x++){    
                allSquares.get(y).add(new MazeSquare(x*squareSize, y*squareSize, squareSize, new int[] {x,y}));
            }
        }
    }

    void drawGrid(){
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

    void drawBackground(){
        fill(0);
        rectMode(CORNER);
        noStroke();
        rect(loc.x, loc.y, size.x, size.y);        
    }

    void display(boolean isHardCore){
        drawBackground();
        if(!isHardCore) drawGrid(); // the 2D map is invisible in hardocre mode (except for visited ones)
    }

    MazeSquare getSquare(int rowIdx, int colIdx){ // return the specified square
        if(rowIdx <= -1)
            return allSquares.get(rows-1).get(columns-1);
        else if(rowIdx >= rows)
            return allSquares.get(0).get(0); 
        return allSquares.get(rowIdx).get(colIdx);
    }

    PVector getLoc(){
        return loc; 
    }
}
