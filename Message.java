
import java.io.Serializable;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author jorda
 */
public class Message implements Serializable{
    private String message;
    private String username;
    
    public Message(String u, String m){
        username = u;
        message = m;
    }
    
    public String toString(){
        return username+": "+message;
    }
}
