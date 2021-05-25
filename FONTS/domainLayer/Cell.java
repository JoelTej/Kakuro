package domainLayer;

public class Cell {
	private int x;				//posicion vertical
	private int y; 				//posicion horizontal
	private String code;		//codigo de la celda
	private int verticalSum;	//suma vertical
	private int horizontalSum;	//suma horizontal
	private int numWhitesHor;	//numero de casillas blancas horizontales para resolver la suma
	private int numWhitesVert;	//numero de casillas blancas verticales para resolver la suma
	private String color;		//White, Black, Solved, EmptyBlack
	private int value;			//If it hasn't value -1
	
	public Cell() {}
	
	//creadora "basica", coordenadas
	public Cell(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	//creadora "completa", coordenadas y codigo
	public Cell(int x, int y, String code) {
		this.x = x;
		this.y = y;
		this.code = code; 
		setColor();
		setValue();
		setHorizontalAndVerticalSum();
	}
	
	//asignamos manualmente el color
	public void setColor(String color) {
		if(color == "EmptyBlack") {
			this.code = "*";
		}
		else if(color == "White") {
			deleteValue();
		}
		this.color = color;
	}
	
	//asigna el color automaticamente segun el codigo
	public void setColor() {
		char firstChar = code.charAt(0);
		if(firstChar == '?') this.color = "White";
		else if( firstChar == '*') this.color = "EmptyBlack";
		else if(Character.getNumericValue(firstChar) > 0 && Character.getNumericValue(firstChar) < 10) this.color = "Solved";
		else if(firstChar == 'C' || firstChar == 'F' || firstChar == '-') this.color = "Black";	
	}
	
	//devuelve el color de la celda
	public String getColor() {
		return color;
	}
	
	//devuelve si la celda tiene color (!= null)
	public boolean hasColor() {
		if(this.color == null) return false;
		else return true;
	}
	
	//cambiado a "Solved"
	public void setValue() {
		if(this.color == "Solved") {
			this.value = Character.getNumericValue(code.charAt(0)); 
		}
		else this.value = -1;
	}
	

	//pasa de white a solved y le asigna value
	public void setValue(int value) {
		if(color == "White" || color == "Solved" || color == null ) {
			this.value = value;
			this.color = "Solved";
			this.code = Integer.toString(value);
		}
	}
	
	//devuelve el valor de la celda 
	public int getValue() {
		return this.value;
	}
	
	//pasa de solved a white y le asigna value por defecto -1
	public void deleteValue() {
		if(color == "Solved") {
			this.code = "?";
			this.color = "White";
			this.value = -1;
		}
	}
	
	//asigna la suma horizontal y vertical en caso de ser "Black"
	public void setHorizontalAndVerticalSum() {
		if(color == "Black") {
			int length = code.length();
			String aux_horizontal, aux_vertical;
			aux_horizontal = aux_vertical = ""; 
			int i = 0; 
			if(code.charAt(i) == 'C') {
				++i;
				while(i<length && code.charAt(i) != 'F') {
					aux_vertical += code.charAt(i);
					++i;
				}
			}
			if(i < length && code.charAt(i) == 'F') {
				++i;
				while(i<length) {
					aux_horizontal += code.charAt(i);
					++i;
				}
			}
			if(aux_horizontal != "") {
				try {
					this.horizontalSum = Integer.valueOf(aux_horizontal);
		        } catch (NumberFormatException e) {
		        	color = null;
		        }
			}
			if(aux_vertical != "") {
				try {
					this.verticalSum = Integer.valueOf(aux_vertical);
		        } catch (NumberFormatException e) {
		        	color = null;
		        }			
			}		
		}
	}
	
	//asigna su suma horizontal a una celda blanca/resuelta
	public void setHorizontalSum(int HorizontalSum) {
		if(color == "White" || color == "Solved" )	this.horizontalSum = HorizontalSum;
	}
	
	//asigna su suma vertical a una celda blanca/resuelta
	public void setVerticalSum(int VerticalSum) {
		if(color == "White" || color == "Solved" )  this.verticalSum = VerticalSum;
	}

	//devuelve la suma horizontal
	public int getHorizontalSum() {
		return horizontalSum;
	}
	
	//devuelve la suma vertical
	public int getVerticalSum() {
		return verticalSum;
	}
	
	//devuelve el code de la celda
	public String getCode() {
		return code; 
	}
	
	//devuelve la coordenada X (fila) de la celda
	public int getX() {
		return x;
	}
	
	//devuelve la coordenada Y (columna) de la celda
	public int getY() {
		return y;
	}
	
	//asigna el numero de blancas seguidas en horizontal
	public void setNumWhitesHor(int nWhitesHor) {
		this.numWhitesHor = nWhitesHor;
	}
	
	//asigna el numero de blancas seguidas en vertical
	public void setNumWhitesVert(int nWhitesVert) {
		this.numWhitesVert = nWhitesVert;
	}
	
	//devuelve el numero de blancas seguidas en horizontal
	public int getNumWhitesHor() {
		return this.numWhitesHor;
	}
	
	//devuelve el numero de blancas seguidas en vertical
	public int getNumWhitesVert() {
		return this.numWhitesVert;
	}
	

}