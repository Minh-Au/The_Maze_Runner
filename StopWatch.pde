public class StopWatch {
    float startTime = 0;
    float endTime = 0;
    boolean running = false;

    void start(){
        if(running) return;
        startTime = millis();
        running = true;
    }

    void stop(){
        if(!running) return;
        endTime = millis();
        running = false;
    }

    float getEllapsedTime(){
        if(running) return millis()-startTime;
        return endTime - startTime;
    }

    float millisecond(){
        return getEllapsedTime() % 100; // round to 2 decimals
    }

    float second(){
        return round((getEllapsedTime()/1000)) % 60;
    }

    float minute(){
        return round((getEllapsedTime()/ (1000*60))) % 60;
    }

    String timeInText(){
        int s = int(this.second());
        int m = int(this.minute());
        int ms = int(this.millisecond());

        DecimalFormat df = new DecimalFormat("00");
        
        return df.format(m) + ":" + df.format(s) + "." + df.format(ms);
    }

    void display(){
        textFont(font, 20);
        noStroke();
        rectMode(CENTER);
        fill(0);
        rect(106, 585, 64, 30);
        
        fill(0,255,0);
        text(this.timeInText(), 75, 590);   
    }
}
