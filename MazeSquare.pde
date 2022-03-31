PVector[] verticies = {new PVector(0,0), new PVector(1,0),
    new PVector(1,1), new PVector(0,1)}; // starts top-left then go clock-wise

public class MazeSquare {
    PVector loc;
    boolean[] isClosed = {true, true, true, true};

    float size;
    boolean alreadyVisited = false;
    public MazeSquare (float xPos, float yPos, float size) {
        loc = new PVector(xPos, yPos);
        this.size = size;
    }

    void display(){
        stroke(255);
        pushMatrix();
        translate(loc.x, loc.y); // move to desired location
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

    PVector info(){
        return loc;
    }
    
    void removeSide(int side){
        // 0 - left; 1 - top; 2 - right; 3 - bottom
        isClosed[side] = false;
    }

    void addSide(int side){
        // 0 - left; 1 - top; 2 - right; 3 - bottom        
        isClosed[side] = true;
    }

    void visit(){
        alreadyVisited = true;
    }

    boolean hasVisited(){
        return alreadyVisited;
    }
}
