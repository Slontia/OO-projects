package ooProject01;

import java.util.Scanner;

public class PolyCal {
	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		String polyInput = sc.nextLine();
		Poly poly = new Poly();
		if (polyInput.length() > 100000){
			System.out.println("Error: overlong input\n");
		}else{
			poly.compute(polyInput);
			poly.end();
		}
	}
}

class Poly{
	//char read
	private char c;
	private int charCount = 0;
	private int termCount = 0;
	private int polyCount = 0;
	
	//coeff & deg
	private int[] coeffList = new int[1000002];
	private int[] existedDegList = new int[52];
	private int coeff;
	private int deg;
	private int coeffLenth;
	private int degLenth;
	private int maxDeg = 0;
	
	//state
	enum StateList {
		BEGIN, TERM_PRE, TERM_START, COEFF, COEFF_SIGN_END, TERM_COMMA, DEG, TERM_END, POLY_END, SIGN_END
	}
	StateList state = StateList.BEGIN;
	
	//sign
	private int sign = 1;
	private int idvdSign = 1;
	
	//break
	private boolean errorBreak = false;
	
	private boolean repeatedDegJudge(){
		for (int i=0; i < termCount ; i++){
			if (existedDegList[i] == deg){
				return true;
			}
		}
		return false;
	}
	
	private void error(String info){
		System.out.println("Error: " + info + " at " + charCount + ".");
		System.out.println("After having read " + polyCount + " polynomials and " + termCount + " extra terms.");
		errorBreak = true;
	}
	
	private void initTerm(){
		coeff = 0;
		deg = 0;
		coeffLenth = 0;
		degLenth = 0;
		maxDeg = 0;
		idvdSign = 1;
	}
	
	private void store_term(){
		coeffList[deg] += coeff; // record
		existedDegList[termCount] = deg; // add to degList
	}
	
	public void compute(String s){
		for (int i = 0; i < s.length(); i++){
			c = s.charAt(i);
			if (c == ' '){
				continue;
			}else{
				charCount++;
			}
			
			if (state == StateList.BEGIN){
				if (c == '-'){ // sign is NEGA
					sign = -1; 
					state = StateList.SIGN_END;
				}else if (c == '+'){ // sign is POSI
					sign = 1;
					state = StateList.SIGN_END;
				}else if (c == '{'){ // non-sign = POSI
					state = StateList.TERM_PRE;
				}else{
					error("unexpected character '"+c+"'");
					break; 
				}
			}
			
			else if (state == StateList.TERM_PRE){ // finish reading '{', waiting '('
				if (c == '('){
					initTerm();
					state = StateList.TERM_START;
				}else{
					error("unexpected character '"+c+"'");
					break; 
				}
			}
			
			else if (state == StateList.TERM_START) { // finish reading '(', waiting '+ -' / '0-9'
				if (c == '-'){
					idvdSign = -1;
					state = StateList.COEFF_SIGN_END;
				}else if (c >= '0' && c <= '9'){
					coeff = c - '0';
					coeffLenth ++;
					state = StateList.COEFF;
				}else{
					error("unexpected character '"+c+"'");
					break; 
				}
			}
			
			else if (state == StateList.COEFF_SIGN_END){
				if (c >= '0' && c <= '9'){
					coeff = c - '0';
					coeffLenth ++;
					state = StateList.COEFF;
				}else{
					error("unexpected character '"+c+"'");
					break;
				}
			}
			
			else if (state == StateList.COEFF){ // waiting '0-9' / ','
				if (c <= '9' && c >= '0'){
					if (coeffLenth >= 6){
						error("coeff out of range");
						break; 
					}
					coeff *= 10;
					coeff += c - '0';
					coeffLenth ++;
					state = StateList.COEFF;
				}else if (c == ','){
					coeff *= sign * idvdSign; // set sign
					state = StateList.TERM_COMMA;
				}else{
					error("unexpected character '"+c+"'");
					break; 
				}
			}
			
			else if (state == StateList.TERM_COMMA){
				if (c <= '9' && c >= '0'){
					deg = c - '0';
					degLenth ++;
					state = StateList.DEG;
				}else if (c == '-'){
					error("illegal negative sign for degree");
					break;
				}else{
					error("unexpected character '"+c+"'");
					break;
				}
			}
			
			else if (state == StateList.DEG){ // finish reading ',', waiting '0-9'
				if (c <= '9' && c >= '0'){
					if (degLenth >= 6){
						error("degree out of range");
						break; 
					}
					deg *= 10;
					deg += c - '0';
					degLenth ++;
					state = StateList.DEG;
				}else if (c == ')'){
					if (repeatedDegJudge() == true){// same degs are illegal
						error("same degrees");
						break;
					}else{
						store_term();
						if (deg > maxDeg) maxDeg = deg;
						termCount ++;
						if (termCount > 50) {
							error("too many terms");
							break;
						}
						state = StateList.TERM_END;
					}
				}else{
					error("unexpected character '"+c+"'");
					break; 
				}
			}
			
			else if (state == StateList.TERM_END){ // waiting ',' / '}'
				if (c == ','){
					state = StateList.TERM_PRE;
				}else if (c == '}'){
					polyCount ++;
					termCount = 0; // clear termCount
					if (polyCount > 20){
						error("too many polynomials");
						break;
					}
					state = StateList.POLY_END;
				}else{
					error("unexpected character '"+c+"'");
					break; 
				}
			}
			
			else if (state == StateList.POLY_END){
				if (c == '+'){
					sign = 1;
					state = StateList.SIGN_END;
				}else if (c == '-'){
					sign = -1;
					state = StateList.SIGN_END;
				}else{
					error("unexpected character '"+c+"'");
					break; 
				}
			}
			
			else if (state == StateList.SIGN_END){
				if (c == '{'){
					state = StateList.TERM_PRE;
				}else{
					error("unexpected character '"+c+"'");
					break; 
				}
			}
		}
	}
	
	private void output(){
		int i;
		System.out.print ("{");
		for(i = 0; i < maxDeg + 1 && coeffList[i] == 0; i++);
		if(i < maxDeg + 1){
			System.out.print ("("+coeffList[i]+","+i+")");
		}
		for(i=i+1; i<maxDeg+1; i++){
			if(coeffList[i] != 0){
				System.out.print (",("+coeffList[i]+","+i+")");
			}
		}
		System.out.print ("}");
	}
	
	public void end(){
		if (errorBreak == false){
			if (state != StateList.POLY_END){
				System.out.println("Error: illegal end");
			}else{
				output();
			}
		}
	}
}