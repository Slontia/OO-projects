#include<stdio.h>
#define MAX_DEG 100002

void store_term(int coeffList[], int coeff, int deg){
	coeffList[deg] += coeff;
}

void output(int coeffList[], int lenth){
	int i;
	printf("{");
	for(i=0; i<lenth+1 && coeffList[i] == 0; i++);
	if(i < lenth+1){
		printf("(%d,%d)", coeffList[i], i);
	}
	for(i=i+1; i<lenth+1; i++){
		if(coeffList[i] != 0){
			printf(",(%d,%d)", coeffList[i], i);
		}
	}
	printf("}");
}

int main(){
	int coeffList[MAX_DEG] = {0};
	
	char c;
	int coeff, deg;
	int maxDeg = 0;

	enum stateList {
		BEGIN, TERM_PRE, TERM_START, COEFF, DEG, TERM_END, POLY_END, SIGN_END
	}state;
	state = BEGIN;
	
	short sign = 1;
	short idvdSign = 1;
	short errorBreak = 0;
	
	while((c=getchar())!='\n'){
		if (c == ' ') continue;
		if (state == BEGIN){
			if (c == '-'){ // sign is NEGA
				sign = -1; 
				state = SIGN_END;
			}else if (c == '+'){ // sign is POSI
				sign = 1;
				state = SIGN_END;
			}else if (c == '{'){ // non-sign = POSI
				state = TERM_PRE;
			}else{
				printf ("illegal character '%c' in state BEGIN\n", c);
				errorBreak = 1;
				break; 
			}
		}
		
		else if (state == TERM_PRE){ // finish reading '{', waiting '('
			if (c == '('){
				coeff = 0;
				deg = 0;
				idvdSign = 1;
				state = TERM_START;
			}else{
				printf ("illegal character '%c' in state TERM_PRE\n", c);
				errorBreak = 1;
				break; 
			}
		}
		
		else if (state == TERM_START) { // finish reading '(', waiting '+ -' / '0-9'
			if (c == '-'){
				idvdSign = -1;
				state = COEFF;
			}else if (c >= '0' && c <= '9'){
				coeff = c - '0';
				state = COEFF;
			}else{
				printf ("illegal character '%c' in state TERM_START\n", c);
				errorBreak = 1;
				break; 
			}
		}
		
		else if (state == COEFF){ // waiting '0-9' / ','
			if (c <= '9' && c >= '0'){
				coeff *= 10;
				coeff += c - '0';
				if (coeff >= 100000){
					printf ("coeff too long!\n");
					errorBreak = 1;
					break; 
				}
				state = COEFF;
			}else if (c == ','){
				coeff *= sign * idvdSign; // set sign
				state = DEG;
			}else{
				printf ("illegal character '%c' in state COEFF\n", c);
				errorBreak = 1;
				break; 
			}
		}
		
		else if (state == DEG){ // finish reading ',', waiting '0-9'
			if (c <= '9' && c >= '0'){
				deg *= 10;
				deg += c - '0';
				if (deg >= 100000){
					printf ("deg too long!\n");
					errorBreak = 1;
					break; 
				}
				state = DEG;
			}else if (c == ')'){
				store_term(coeffList, coeff, deg);
				if (deg > maxDeg) maxDeg = deg;
				state = TERM_END;
			}else{
				printf ("illegal character '%c' in state DEG\n", c);
				errorBreak = 1;
				break; 
			}
		}
		
		else if (state == TERM_END){ // waiting ',' / '}'
			if (c == ','){
				state = TERM_PRE;
			}else if (c == '}'){
				state = POLY_END;
			}else{
				printf ("illegal character '%c' in state TERM_END\n", c);
				errorBreak = 1;
				break; 
			}
		}
		
		else if (state == POLY_END){
			if (c == '+'){
				sign = 1;
				state = SIGN_END;
			}else if (c == '-'){
				sign = -1;
				state = SIGN_END;
			}else{
				printf ("illegal character '%c' in state POLY_END\n", c);
				errorBreak = 1;
				break; 
			}
		}
		
		else if (state == SIGN_END){
			if (c == '{'){
				state = TERM_PRE;
			}else{
				printf ("illegal character '%c' in state SIGN_END\n", c);
				errorBreak = 1;
				break; 
			}
		}
	}
	
	if (!errorBreak){
		if (state != POLY_END){
			printf("illegal end\n");
		}else{
			output(coeffList, maxDeg);
		}
	}
} 


