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
public abstract class Preditor {
    int pc;
    public abstract int predict();
    public abstract void update(int x);
    
}
