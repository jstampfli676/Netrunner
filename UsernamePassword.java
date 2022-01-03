import java.io.*;

public class UsernamePassword implements Serializable{
	private boolean register;
	private String username;
	private String password;

	
        public UsernamePassword (boolean register){
            this(register, "", "");
        }
        
        public UsernamePassword (boolean register, String u, String p){
		this.register = register;
		username = u;
		password = p;
	}
        
        
        public String getUsername(){
            return username;
        }
        
        public String getPassword(){
            return password;
        }
        
        public boolean getRegister(){
            return register;
        }

	public String toString(){
		return String.valueOf(register)+", "+username+", "+password;
	}
}