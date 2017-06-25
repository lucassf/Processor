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
public class PreditorFactory {
    public Preditor createPreditor(int x, int pc){
        if (x == 0)
            return new Preditor0bits(pc);
        if (x == 1)
            return new Preditor1bit(pc);
        return new Preditor2bits(pc);
    }
    public PreditorFactory(){
        
    }
         
}
