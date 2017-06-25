/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package processor;

/**
 *
 * @author Gustavo
 */
public class Preditor2bits extends Preditor{
    private int state = 1;
    public Preditor2bits(){
        this.state = 1;
    }
    @Override
    public int predict(){
        System.out.println(state);
        if (this.state == 3 || this.state == 2){            
            //System.out.println("predict jump");
            return 1;
        }
        else{
            
           // System.out.println("predict not jump");
            return 0;
            
        }
    }
    public Preditor2bits(int x){
        this.pc = x;
    }

    @Override
    public void update(int x) {
        
      //  System.out.println("antes" + state + " pc: "+this.pc);
        switch(state){            
            case 0:
                if(x == 0)
                    this.state = 0;
                else
                    this.state = 1;
                break;
            case 1:
                if(x == 0)
                    this.state = 0;
                else
                    this.state = 3;
                break;
            case 2:
                if(x == 0)
                    this.state = 0;
                else
                    this.state = 3;
                break;
            case 3:
                if(x == 0)
                    this.state = 2;
                else
                    this.state = 3;
                break;
        }        
      //  System.out.println(state + "pc: "+this.pc);
        
    }
}
