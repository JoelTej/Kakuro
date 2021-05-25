package domainLayer;

import java.lang.String;

public class Kakuro {
	
	//Atributos 
	
	private Cell[][] Board;	//tablero
	private int Height;	//altura
	private int Width;	//anchura
	private String RecordUser;	//usuario con el record
	private String RecordTime;	//tiempo del record
	private Difficulty Dif;	//dificultad
		
	//Funciones
	
	//creadora de kakuro vacia 
	public Kakuro() {
	}
	
	//creadora "basica", tamano
	public Kakuro(int Height, int Width) {
		initializeKakuro(Height, Width);
	}
	
	//inicializa un kakuro con tamaño
	public void initializeKakuro(int Height, int Width) {
        setSize(Height, Width);
        this.Board = new Cell[Height][Width];
        for (int i = 0; i < Height; ++i) {
            for (int j = 0; j < Width; ++j){
                Cell Caux = new Cell(i,j);
                setCell(i,j,Caux);
            }
        }
    }

	//asigna el tamano al kakuro
	public void setSize(int height, int width) {
		 this.Height = height;
		 this.Width = width;
	}
	
	//devuelve el tablero del kakuro
	public Cell[][] getBoard(){
		return Board;
	}
	
	//devuelve la altura del kakuro
	public int getHeight() {
		return Height;
	}
	
	//devuelve la anchura del kakuro
	public int getWidth() {
		return Width;
	}
	
	//devuelve la dificultad del kakuro
	public Difficulty getDifficulty() {
		return Dif;
	}
	
	//asigna la dificultad del kakuro
	public void setDifficulty(String dif) {
		Difficulty diff = Difficulty.valueOf(dif);
		this.Dif = diff;
	}
	
	//asigna la cell c a board[i][j]
	public void setCell(int i, int j, Cell c) {
		Board[i][j] = c;
	}
	
	//devuelve el valor de la celda board[i][j]
	public int getCellValue(int i, int j) {
		return Board[i][j].getValue();
	}
	
	//devuelve el code de la celda board[i][j]
	public String getCellCode(int i, int j) {
		return Board[i][j].getCode();
	}
	
	//asigna el valor a la celda board[i][j]
	public void setCellValue(int i, int j, int value) {
		Board[i][j].setValue(value);
	}
	
	//elimina el valor a la celda board[i][j]
	public void deleteCellValue(int i, int j) {
		Board[i][j].deleteValue();
	}
	
	//devuelve el color de la celda board[i][j]
	public String getCellColor(int i, int j) {
		return Board[i][j].getColor();
	}

	//asigna una nueva celda segun el color s
	public void setNewCell(int i, int j, String s){
	    if(s == "EmptyBlack") {
	        Cell Caux = new Cell(i,j,"*");
	        setCell(i,j,Caux);
	    }
	    else if(s == "White"){
	        Cell Caux = new Cell(i,j,"?");
	        setCell(i,j,Caux);
	    }
	    else if(s == "Black"){
	        Cell Caux = new Cell(i,j,"-");
	        setCell(i,j,Caux);
	    }
	}
	
	//asigna el color s a la celda board[i][j]
	public void setCellColor(int i, int j, String s){
		Board[i][j].setColor(s);
	}
	
	//asigna la suma horizontal de la celda board[i][j]
	public void setCellHorizontalSum(int i, int j, int sumH) {
		Board[i][j].setHorizontalSum(sumH);
	}

	//asigna la suma vertical de la celda board[i][j]
	public void setCellVerticalSum(int i, int j, int sumV) {
		Board[i][j].setVerticalSum(sumV);
	}

	//asigna la suma vertical y horizontal de la celda board[i][j]
	public void setSumVerHor(int i, int j) {
		setCellVerticalSum(i, j, getCellVerticalSum(i-1, j));				
		setCellHorizontalSum(i, j, getCellHorizontalSum(i, j-1));				
	}
	
	//devuelve la suma horizontal de la celda board[i][j]
	public int getCellHorizontalSum(int i, int j) {
		return Board[i][j].getHorizontalSum();
	}

	//devuelve la suma vertical de la celda board[i][j]
	public int getCellVerticalSum(int i, int j) {
		return Board[i][j].getVerticalSum();
	}
	
	//devuelve true si la celda tiene color (!= null)
	public boolean cellHasColor(int i, int j) {
		return Board[i][j].hasColor();
	}
	
	//devuelve true si se puede colocar una celda blanca en board[i][j]
	public boolean checkWhite(int i, int j) {
		//comprobamos que podemos poner una blanca
		if (i == Height-1) {
			if (Board[i-1][j].getColor() == "EmptyBlack") return false;
		}
		if (j == Width-1) {
			if (Board[i][j-1].getColor() == "EmptyBlack") return false;
		}
		int count = 0;
		if (Height > 10) {
			for(int m = i-1; m > 0 && this.getCellColor(m, j) != "EmptyBlack"; m--) {
				++count;
			}
			if (count >= 9) return false;
		}
		count = 0;
		if (Width > 10) {
			for (int m = j-1; m > 0 && this.getCellColor(i,m) != "EmptyBlack"; m--) {
				++count;
			}
			if (count >= 9) return false;
		}
		return true;
	}
	
	//devuelve true si se puede colocar una celda negra en board[i][j]
	public boolean checkBlack(int i, int j) {
		//comprobamos que podemos poner una negra
		//si la de encima y la de la izquierda son negras, podemos ponerla
		if (Board[i-1][j].getColor() == "EmptyBlack" && Board[i][j-1].getColor() == "EmptyBlack") return true;
		//si la de encima es una blanca aislada no podemos
		if (Board[i-1][j].getColor() == "White" && isolatedUpCell(i-1, j)) return false;
		//si la de la izquierda es una blanca aislada no podemos
		if (Board[i][j-1].getColor() == "White" && isolatedLeftCell(i, j-1)) return false;
		//resto de casos si quedan
		return true;
	}
	
	//devuelve true si board[i-1][j] es EmptyBlack
	public boolean isolatedUpCell(int i, int j) {
		if (Board[i-1][j].getColor() == "EmptyBlack" || Board[i-1][j].getColor() == "Black") return true;
		return false;
	}
	
	//devuelve true si board[i][j-1] es EmptyBlack
	public boolean isolatedLeftCell(int i, int j) {
		if (Board[i][j-1].getColor() == "EmptyBlack" || Board[i-1][j].getColor() == "Black") return true;
		return false;
	}

	//devuelve true si board[i+1][j] es EmptyBlack
	public boolean isolatedDownCell(int i, int j) {
		if ((i+1) == getHeight()) return true;
		if ((i+1) <= getHeight()-1 && Board[i+1][j].getColor() == "EmptyBlack") return true;
		return false;
	}
	
	//devuelve true si board[i][j+1] es EmptyBlack
	public boolean isolatedRightCell(int i, int j) {
		if ((j+1) == getWidth()) return true;
		if ((j+1) <= getWidth()-1 && Board[i][j+1].getColor() == "EmptyBlack") return true;
		return false;
	}
	
	//asigna el numero de celdas blancas contiguas en esa misma fila
	public void setCellNumWhitesHor(int i, int j, int nWhitesHor) {
		Board[i][j].setNumWhitesHor(nWhitesHor);
	}
	
	//asigna el numero de celdas blancas contiguas en esa misma columna
	public void setCellNumWhitesVert(int i, int j, int nWhitesVert) {
		Board[i][j].setNumWhitesVert(nWhitesVert);
	}
	
	//devuelve el numero de celdas blancas contiguas en esa misma fila
	public int getNumWhitesHor(int i, int j) {
		return Board[i][j].getNumWhitesHor();
	}
	
	//asigna el numero de celdas blancas contiguas en esa misma columna
	public int getNumWhitesVert(int i, int j) {
		return Board[i][j].getNumWhitesVert();
	}
	
	//asigna el tiempo record y el usuario que lo ha conseguido
	public void setRecord(String time, String username) {
		this.RecordTime = time;
		this.RecordUser = username;
	}
	
	//devuelve el tiempo record
	public String getRecord() {
		return this.RecordTime;
	}
	
	//devuelve el usuario que ha logrado el tiempo record 
	public String getRecordUser() {
		return this.RecordUser;
	}
	
	//devuelve el numero de celdas blancas que hay
	public int getNumWhites() {
		int counter = 0;
		for(int i = 0; i<Height; ++i) {
			for(int j = 0; j<Width;++j) {
				if(Board[i][j].getColor().equals("White")) ++counter;
			}
		}
		return counter;
	}
}