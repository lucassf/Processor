/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import processor.Processor;

/**
 *
 * @author G1511NEW
 */
public class NovoEmptyJUnitTest {
    @Test
    public void NovoEmptyJUnitTest() {
        Processor p = new Processor();
        for (int i = 0; i < 12; i++){
            p.process();
        }
        /*while(true){
            
        }*/
    }
    

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
}
