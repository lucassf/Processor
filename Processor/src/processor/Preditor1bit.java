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
public class Preditor1bit extends Preditor{
    private int state = 0;
    public Preditor1bit(){
        this.state = 0;
    }
    @Override
    public int predict(){
        return state;
    }
    public Preditor1bit(int x){
        this.pc = x;
    }

    @Override
    public void update(int x) {
        
        if (x==1){
            state = 1;
        }
        else{
            state = 0;
        }
    }
}
