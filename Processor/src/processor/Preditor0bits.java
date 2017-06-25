package processor;


import processor.Preditor;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Gustavo
 */
public class Preditor0bits extends Preditor{
    private int state;
    @Override
    public int predict(){
        return 0;
    }
    public Preditor0bits(int x){
        this.pc = x;
    }

    @Override
    public void update(int x) {
        
    }
}
