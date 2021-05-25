package domainLayer;

import java.util.Random;

public class Generator {
	
	//constantes para determinar la dificultad (#define)
	private static int MinMedium = 17;
	private static int MinHard = 37;
	
	
	//kakuro generado "personalizado" (tamano del kakuro, casillas negras y blancas)
	public static void generateKakuro(Kakuro kak, int rows, int cols, int black, int white, boolean unique){
		//creamos el kakuro con su tamano correspondiente
		
		//establecer la dificultad segun white (falta determinar MinMedium y MinHard)
		
		if(white<MinMedium) kak.setDifficulty("easy");
		else if(white<MinHard) kak.setDifficulty("medium");
		else kak.setDifficulty("hard");
		
		
		//colocamos las casillas negras
		distributeCells(kak, black, white);

		//rellenamos con numeros random las casillas blancas
		fillWhites(kak);
		
		//establecemos las sumas y dejamos las casillas blancas vacias (valor por defecto -1)
		setSumas(kak);
		
		//comprobamos que la solucion es unica
		if (unique) checkUnique(kak,black,white);
		
		else{
			//comprobamos que tenga al menos una solucion
			int count = 10;
			while(!Solver.solveKakuro(kak)) {
				--count;
				if(count == 0) {
					count = 10;
					distributeCells(kak);
				}
				else emptyBoard(kak);
				fillWhites(kak);
				setSumas(kak);
			}
		}
	}
	
	//kakuro generado  "personalizado" (tamano del kakuro)
	public static void generateKakuro(Kakuro kak, int rows, int cols, boolean unique){
		//colocamos las casillas negras
		distributeCells(kak);

		//rellenamos con numeros random las casillas blancas
		fillWhites(kak);
		
		//establecemos las sumas y dejamos las casillas blancas vacias (valor por defecto -1)
		setSumas(kak);
		
		//comprobamos que la solucion es unica
		if (unique) checkUnique(kak);
				
		else{
			//comprobamos que tenga al menos una solucion
			int count = 10;
			while(!Solver.solveKakuro(kak)) {
				--count;
				if(count == 0) {
					count = 10;
					distributeCells(kak);
				}
				else emptyBoard(kak);
				fillWhites(kak);
				setSumas(kak);
			}
		}
	}
	
	//kakuro generado "personalizado" (dificultad del kakuro)
	public static void generateKakuro(Kakuro kak, String difficulty) {
		//difficulty = "easy", "medium" o "hard"
		int rows, cols;
		if(difficulty.equals("easy")) {
			//de 4x4 a 5x5
			rows = getRandomNumber(4,5);
			cols = getRandomNumber(4,5);
		}
		else if(difficulty.equals("medium")) {
			//de 5x6 a 7x7
			rows = getRandomNumber(5,7);
			cols = getRandomNumber(5,7);
			if(rows == 5 && cols == 5) cols++;
		}
		else {
			//de 7x8 a 11x11
			rows = getRandomNumber(7,13);
			cols = getRandomNumber(7,13);
			if((cols-rows)>3) rows = cols-3;
			else if((rows-cols)>3) cols = rows-3;
		}
		kak.initializeKakuro(rows, cols);
		
		distributeCells(kak);
		while(!kak.getDifficulty().toString().equals(difficulty)) distributeCells(kak);
		//rellenamos con numeros random las casillas blancas
		fillWhites(kak);
		
		//establecemos las sumas y dejamos las casillas blancas vacias (valor por defecto -1)
		setSumas(kak);
		
		//comprueba que tenga al menos una solucion
		int count = 10;
		while(!Solver.solveKakuro(kak)) {
			--count;
			if(count == 0) {
				count = 10;
				distributeCells(kak);
			}
			else emptyBoard(kak);
			fillWhites(kak);
			setSumas(kak);
		}
	}
	
	//kakuro sin personalizar, todo esta a nuestra eleccion (establecemos margenes sobre los que aleatorizar la generacion)
	public static void generateKakuro(Kakuro kak) {
		Random r = new Random();		//r.nextInt((max - min) + 1) + min; ->(rows*cols-8-rows*2)+8
		
		//generamos un kakuro cuadrado entre 5x5 y 15x15 (ni muy pequeno ni exageradamente grande, se puede cambiar en cualquier momento)
		int rows,cols;
		rows = r.nextInt((15-5)+1)+5;
		cols = r.nextInt((15-5)+1)+5;
		
		//dejamos margen de 3 como mucho (entre las filas y las columnas)
		if((cols-rows)>3) rows = cols-3;
		else if((rows-cols)>3) cols = rows-3;
		
		kak.initializeKakuro(rows, cols);
		
		generateKakuro(kak, rows, cols, false);
		//comprueba que tenga al menos una solucion
		int count = 10;
		while(!Solver.solveKakuro(kak)) {
			--count;
			if(count == 0) {
				count = 10;
				distributeCells(kak);
			}
			else emptyBoard(kak);
			fillWhites(kak);
			setSumas(kak);
		}
	}
	
	//distribuye #black cells y #white cells en el tablero del kakuro
	private static void distributeCells(Kakuro kak, int black, int white){
		int blackAux = black;
		int whiteAux = white;
		
		//dejamos la primera fila y columna como EmptyBlack por defecto (siempre lo seran)
		for(int i = 0; i<kak.getHeight(); ++i) {
			kak.setCellColor(i,0,"EmptyBlack");	//la dejamos como empty black por defecto	
			blackAux--;
		}
		for(int i = 1; i<kak.getWidth(); ++i) {
			//primera fila va toda a negras
			kak.setCellColor(0,i,"EmptyBlack");	//la dejamos como empty black por defecto	
			blackAux--;
		}
		
		//recorremos la board desde [1][1] (primera casilla disponible para colocar las casillas Black, White y EmptyBlack
		boolean error = false;
		for(int i = 1; i<kak.getHeight(); ++i) {
			for(int j = 1; j<kak.getWidth() && !error; ++j) {
				
				
				//segun un numero random intentamos colocar primero una blanca (numero par) o una negra (numero impar)-> maximizamos la aleatoriedad
				if (getRandomNumber()%2 == 0) {
					//si es par intentamos meter primero una White
					if (whiteAux > 0 && kak.checkWhite(i, j)) {
						//si se puede insertar una blanca la anadimos -> decrementamos el numero de sumas segun las que genere (arriba y a la izquierda) y el numero de blancas
						kak.setCellColor(i, j, "White");
						whiteAux--;
					}
					//si no podemos meter una blanca intentamos meter una EmptyBlack
					else if (blackAux > 0 && kak.checkBlack(i, j)) {
						//si se puede insertar una blanca la anadimos -> decrementamos el numero de negras
						kak.setCellColor(i,j,"EmptyBlack");
						blackAux--;
					}
					else {
						//si no se puede insertar ni una blanca ni una negra -> error asi que repetimos la llamada a la funcion
						error = true;
						distributeCells(kak, black, white);
						break;
					}
				}
				else {
					//si es impar intentamos meter primero una EmptyBlack
					if (blackAux > 0 && kak.checkBlack(i, j)) {
						//si se puede insertar una negra la anadimos -> decrementamos el numero de negras
						kak.setCellColor(i,j,"EmptyBlack");	
						blackAux--;
					}
					//si no se puede insertar una EmptyBlack probamos a insertar una White
					else if (whiteAux > 0 && kak.checkWhite(i, j)) {
						//si se puede insertar una blanca la anadimos -> decrementamos el numero de sumas segun las que genere (arriba y a la izquierda) y el numero de blancas 
						kak.setCellColor(i, j, "White");
						whiteAux--;
					}
					
					else {
						//si no se puede insertar una negra ni una blanca -> error asi que repetimos la llamada inicial
						error = true;
						distributeCells(kak, black, white);
						break;
					}
				}					
			}
		}
		//end del for (cuando ya se han colocado todas las casillas sin error o se ha generado el kakuro con un error)
		
		if (blackAux == 0 && whiteAux == 0 && !error) {
			setWhitesHorVer(kak,false);
		//Si las dos variables auxiliares llegan a 0 significa que hemos repartido correctamente
		}
	}
	
	//dejamos el numero de casillas verticales y horizontales para cada suma 
	public static void setWhitesHorVer(Kakuro K, boolean countDifficult) {
		int counter = 0;
		for(int i = 1; i<K.getHeight(); ++i) {
			for(int j = 1; j<K.getWidth(); ++j) {
				if (K.getCellColor(i, j) == "White") {
					++counter;
					K.setCellNumWhitesVert(i,j,AdjVerWhite(i,j,K));
					K.setCellNumWhitesHor(i,j,AdjHorWhite(i,j,K));
				}
			}
		}
		if(countDifficult) {
			if(counter<MinMedium) K.setDifficulty("easy");
			else if(counter<MinHard) K.setDifficulty("medium");
			else K.setDifficulty("hard");
		}
	}
	
	//distribuye sin estar condicionado por numero de blancas y negras
	/*
	alto impar:
	repartimos aleatoriamente comprobando que se pueden poner
	hacemos simetria sobre el eje horizontal
	ancho impar:
	repartimos aleatoriamente comprobando que se pueden poner
	hacemos simetria sobre el eje vertical*/
	private static void distributeCells(Kakuro kak) {
		for(int i = 0; i<kak.getHeight(); ++i) {
			kak.setCellColor(i,0,"EmptyBlack");	//la dejamos como empty black por defecto	
		}
		for(int i = 1; i<kak.getWidth(); ++i) {
			//primera fila va toda a negras
			kak.setCellColor(0,i,"EmptyBlack");	//la dejamos como empty black por defecto	
		}
		
		//si la altura es impar cogemos la altura
		if (kak.getHeight() % 2 == 1) {
			distributeHorizontalAxis(kak);
		}
		//else si el ancho es impar, cogemos el ancho
		else if (kak.getWidth() % 2 == 1) {
			distributeVerticalAxis(kak);
		}
		//else cogemos cualquiera de las dos
		else {
			if (getRandomNumber()%2 == 0) {
				distributeHorizontalAxis(kak);
			}
			else {
				distributeVerticalAxis(kak);
			}
		}//elegimos horizontal mismo	
		setWhitesHorVer(kak,true);
		
	}
	
	//distribuye si la altura es impar
	private static void distributeHorizontalAxis(Kakuro kak) {
		int h = kak.getHeight();
		int w = kak.getWidth();
		boolean error = false;
		//altura impar: recorremos desde 1 hasta (Height-1)/2
		for (int i = 1; i <= (h - 1)/2; ++i){
			for (int j = 1; j < w && !error; ++j) {
				
				if(i == (h-1)/2 && j == w/2) {
					if (kak.checkWhite(i, j) && checkWhiteHor(kak, i, j)) {
						//si se puede insertar una blanca la anadimos -> decrementamos el numero de sumas segun las que genere (arriba y a la izquierda) y el numero de blancas
						kak.setCellColor(i, j, "White");
						kak.setCellColor(h-i, w-j, "White");
					}
					//si no podemos meter una blanca intentamos meter una EmptyBlack
					else if (kak.checkBlack(i, j) && checkBlackHor(kak, i, j)) {
						//si se puede insertar una blanca la anadimos -> decrementamos el numero de negras
						kak.setCellColor(i,j,"EmptyBlack");
						kak.setCellColor(h-i, w-j, "EmptyBlack");
					}
					else {
						//si no se puede insertar ni una blanca ni una negra -> error asi que repetimos la llamada a la funcion
						error = true;
						distributeCells(kak);
						break;
					}
				}
				
				else {
					if (getRandomNumber()%2 == 0) {
						if (kak.checkWhite(i, j) && checkWhiteHor(kak, i, j)) {
							//si se puede insertar una blanca la anadimos -> decrementamos el numero de sumas segun las que genere (arriba y a la izquierda) y el numero de blancas
							kak.setCellColor(i, j, "White");
							kak.setCellColor(h-i, w-j, "White");
						}
						//si no podemos meter una blanca intentamos meter una EmptyBlack
						else if (kak.checkBlack(i, j) && checkBlackHor(kak, i, j)) {
							//si se puede insertar una blanca la anadimos -> decrementamos el numero de negras
							kak.setCellColor(i,j,"EmptyBlack");
							kak.setCellColor(h-i, w-j, "EmptyBlack");
						}
						else {
							//si no se puede insertar ni una blanca ni una negra -> error asi que repetimos la llamada a la funcion
							error = true;
							distributeCells(kak);
							break;
						}
					}
					else {
						//si es impar intentamos meter primero una EmptyBlack
						if (kak.checkBlack(i, j) && checkBlackHor(kak, i, j)) {
							//si se puede insertar una negra la anadimos -> decrementamos el numero de negras
							kak.setCellColor(i,j,"EmptyBlack");
							kak.setCellColor(h-i, w-j, "EmptyBlack");
						}
						//si no se puede insertar una EmptyBlack probamos a insertar una White
						else if (kak.checkWhite(i, j) && checkWhiteHor(kak, i, j)) {
							//si se puede insertar una blanca la anadimos -> decrementamos el numero de sumas segun las que genere (arriba y a la izquierda) y el numero de blancas 
							kak.setCellColor(i, j, "White");
							kak.setCellColor(h-i, w-j, "White");
						}
						
						else {
							//si no se puede insertar una negra ni una blanca -> error asi que repetimos la llamada inicial
							error = true;
							distributeCells(kak);
							break;
						}
					}
				}
			}
		}
		
		if (h%2 == 0) {
			int auxI = h/2;
			for (int j = 1; j <= w/2; ++j) {
				if (kak.checkWhite(auxI, j) && checkWhiteHor(kak, auxI, j) && (kak.getCellColor(auxI - 1, j) == "White" || kak.getCellColor(auxI + 1, j) == "White")) {
					//si se puede insertar una blanca la anadimos -> decrementamos el numero de sumas segun las que genere (arriba y a la izquierda) y el numero de blancas
					kak.setCellColor(auxI, j, "White");
					kak.setCellColor(h-auxI, w-j, "White");
				}
				//si no podemos meter una blanca intentamos meter una EmptyBlack
				else if (kak.checkBlack(auxI, j) && checkBlackHor(kak, auxI, j)) {
					//si se puede insertar una blanca la anadimos -> decrementamos el numero de negras
					kak.setCellColor(auxI,j,"EmptyBlack");
					kak.setCellColor(h-auxI, w-j, "EmptyBlack");
				}
				else {
					//si no se puede insertar ni una blanca ni una negra -> error asi que repetimos la llamada a la funcion
					error = true;
					distributeCells(kak);
					break;
				}
			}
		}
	}
	
	//distribuye si el ancho es impar
	private static void distributeVerticalAxis(Kakuro kak) {
		int h = kak.getHeight();
		int w = kak.getWidth();
		
		boolean error = false;
		//altura impar: recorremos desde 1 hasta (Height-1)/2
		for (int i = 1; i < h; ++i){
			for (int j = 1; j <= (w - 1)/2 && !error; ++j) {
				
				if (i == h/2 && j == (w-1)/2) {
					if (kak.checkWhite(i, j) && checkWhiteVer(kak, i, j)) {
						//si se puede insertar una blanca la anadimos -> decrementamos el numero de sumas segun las que genere (arriba y a la izquierda) y el numero de blancas
						kak.setCellColor(i, j, "White");
						kak.setCellColor(h-i, w-j, "White");
					}
					//si no podemos meter una blanca intentamos meter una EmptyBlack
					else if (kak.checkBlack(i, j) && checkBlackVer(kak, i, j)) {
						//si se puede insertar una blanca la anadimos -> decrementamos el numero de negras
						kak.setCellColor(i,j,"EmptyBlack");
						kak.setCellColor(h-i, w-j, "EmptyBlack");
					}
					else {
						//si no se puede insertar ni una blanca ni una negra -> error asi que repetimos la llamada a la funcion
						error = true;
						distributeCells(kak);
						break;
					}
				}
				
				else {
					if (getRandomNumber()%2 == 0) {
						if (kak.checkWhite(i, j) && checkWhiteVer(kak, i, j)) {
							//si se puede insertar una blanca la anadimos -> decrementamos el numero de sumas segun las que genere (arriba y a la izquierda) y el numero de blancas
							kak.setCellColor(i, j, "White");
							kak.setCellColor(h-i, w-j, "White");
						}
						//si no podemos meter una blanca intentamos meter una EmptyBlack
						else if (kak.checkBlack(i, j) && checkBlackVer(kak, i, j)) {
							//si se puede insertar una blanca la anadimos -> decrementamos el numero de negras
							kak.setCellColor(i,j,"EmptyBlack");
							kak.setCellColor(h-i, w-j, "EmptyBlack");
						}
						else {
							//si no se puede insertar ni una blanca ni una negra -> error asi que repetimos la llamada a la funcion
							error = true;
							distributeCells(kak);
							break;
						}
					}
					else {
						//si es impar intentamos meter primero una EmptyBlack
						if (kak.checkBlack(i, j) && checkBlackVer(kak, i, j)) {
							//si se puede insertar una negra la anadimos -> decrementamos el numero de negras
							kak.setCellColor(i,j,"EmptyBlack");	
							kak.setCellColor(h-i, w-j, "EmptyBlack");
						}
						//si no se puede insertar una EmptyBlack probamos a insertar una White
						else if (kak.checkWhite(i, j) && checkWhiteVer(kak, i, j)) {
							//si se puede insertar una blanca la anadimos -> decrementamos el numero de sumas segun las que genere (arriba y a la izquierda) y el numero de blancas 
							kak.setCellColor(i, j, "White");
							kak.setCellColor(h-i, w-j, "White");
						}
						
						else {
							//si no se puede insertar una negra ni una blanca -> error asi que repetimos la llamada inicial
							error = true;
							distributeCells(kak);
							break;
						}
					}
				}
			}
		}
		if (w%2 == 0) {
			int auxJ = w/2;
			for (int i = 1; i <= (h/2); ++i) {
				if (kak.checkWhite(i, auxJ) && checkWhiteVer(kak, i, auxJ) && (kak.getCellColor(i, auxJ-1) == "White" || kak.getCellColor(i, auxJ+1) == "White")) {
					//si se puede insertar una blanca la anadimos -> decrementamos el numero de sumas segun las que genere (arriba y a la izquierda) y el numero de blancas
					kak.setCellColor(i, auxJ, "White");
					kak.setCellColor(h-i, w-auxJ, "White");
				}
				//si no podemos meter una blanca intentamos meter una EmptyBlack
				else if (kak.checkBlack(i, auxJ) && checkBlackVer(kak, i, auxJ)) {
					//si se puede insertar una blanca la anadimos -> decrementamos el numero de negras
					kak.setCellColor(i,auxJ,"EmptyBlack");
					kak.setCellColor(h-i, w-auxJ, "EmptyBlack");
				}
				else {
					//si no se puede insertar ni una blanca ni una negra -> error asi que repetimos la llamada a la funcion
					error = true;
					distributeCells(kak);
					break;
				}
			}
		}
	}	
	
	//contar las celdas colindantes blancas verticales
	private static int AdjVerWhite(int i, int j, Kakuro K) {
        int total = 1; // counted the cell[i][j]
        // count white cells to the left of cell[i][j]
        int row = i-1;
        String s = K.getCellColor(row, j);
        while (s == "White" || s == "Solved") {
            total++;
            row--;
            s = K.getCellColor(row, j);
        }
        
        // count white cells to the right of cell[i][j]
        row = i+1;
        int m = K.getHeight();
        if (row < m) s = K.getCellColor(row, j);
        while (row < m && (s == "White" || s == "Solved")) {
            total++;
            row++;
            if (row < m) s = K.getCellColor(row, j);
        }
        
        return total;
    }

	//contar las celdas colindantes blancas horizontales
	private static int AdjHorWhite(int i, int j, Kakuro K) {
        int total = 1; //contamos la celda en la que estamos

        //hacia arriba
        int col = j-1;
        String s = K.getCellColor(i, col);
        while (s == "White" || s == "Solved") {
            total++;
            col--;
            s = K.getCellColor(i, col);
        }
        
        //hacia abajo
        col = j+1;
        int n = K.getWidth();
        if (col < n) s = K.getCellColor(i, col);
        while (col < n && (s == "White" || s == "Solved")) {
            total++;
            col++;
            if (col < n) s = K.getCellColor(i, col);
        }
        
        return total;
    }

	
	//devuelve un numero aleatorio enter 0 y 1000 (incluidos)
	private static int getRandomNumber() {
		Random r = new Random();
		return r.nextInt(1000);
	}
	
	//devuelve un numero aleatorio entre min y max (incluidos)
	public static int getRandomNumber(int min, int max) {
		Random r = new Random();
		return r.nextInt((max-min)+1)+min;
	}
	
	//RELLENAR LAS BLANCAS-> con la distribucion de blancas y negras rellenamos las blancas con posibles sumas
	
	//segun la distribucion que deja distributeCells, recorrer el tablero para colocar numero random (entre 1 y 9) en todas las casillas blancas, comprobando que no hay excepciones al colocarlos
	private static void fillWhites(Kakuro kak){
		Random r = new Random();
		int valueAux;
		boolean stop = false;
		boolean[] repeated;
		int countRepetitions = 0;
		//falta la estructura para comprobar la columna
		for(int i = 1; i<kak.getHeight() && !stop; ++i) {
			//dejamos el vector de filas vacio (al acceder, null devuelve false)
			for(int j = 1; j<kak.getWidth() && !stop; ++j) {
				if(kak.getCellColor(i, j) == "White") checkOptimizations(kak,i,j);	//optimitzaciones para generar un kakuro con solucion unica
				//repetimos el if por si las optimizaciones dejan la casilla como solved
				if(kak.getCellColor(i, j) == "White") {
					//comprobamos que se pueda poner un valor (util para kakuros de tamano considerable)
					if((i+j)>7 && checkMax(kak,i,j)) {
						emptyBoard(kak);
						fillWhites(kak);
						stop = true;
					}
					if(!stop) {
						//con estas variables evitamos que se quede en un bucle infinito en caso de no poder anadir un valor
						repeated = new boolean[9];
						countRepetitions = 0;
						valueAux = r.nextInt((9-1)+1)+1;	//random entre 1 y 9
						//mientras haya un valor valido (fila o columna ocupada) recalculamos el valor
						while(countRepetitions<9 && (rowNotValid(kak,i,j,valueAux) || columnNotValid(kak,i,j,valueAux))){		//al aplicar las optimizaciones se comprueba rowNotValid(kak,i,j,valueAux)
							if(!repeated[valueAux-1]) {
								repeated[valueAux-1]=true;
								++countRepetitions;
							}
							valueAux = r.nextInt((9-1)+1)+1;
						}
						if(countRepetitions>=9) {
							emptyBoard(kak);
							fillWhites(kak);
							stop = true;
						}
						//dejamos ese valor como ocupado (en la fila) y asignamos el valor (pasa de White a Solved)
						if(!stop) kak.setCellValue(i, j, valueAux); 
					}
				}
			}
		}
	}
	
	//comprueba que en esa columna no este asignado ya value (true si ese valor ya esta asignado en esa columna)
	private static boolean columnNotValid(Kakuro kak,int i, int j, int value) {
		boolean stop = false;
		//des de donde estamos (i,j) hacia arriba por filas
		for(int auxI = i; !stop && auxI<kak.getHeight(); ++auxI) {	//al ponerlo >0 evitamos la primera fila
			//si la casilla es Solved (tiene numero) y el valor coincide devuelve true
			if(kak.getCellColor(auxI, j) == "Solved" && kak.getCellValue(auxI, j) == value) return true;
			else if(kak.getCellColor(auxI, j) == "EmptyBlack") stop = true;
		}
		stop = false;
		for(int auxI = i; !stop && auxI>0; --auxI) {
			if(kak.getCellColor(auxI, j) == "Solved" && kak.getCellValue(auxI, j) == value) return true;
			else if(kak.getCellColor(auxI, j) == "EmptyBlack") stop = true;
		}
		//en cualquier otro caso devuelve false
		return false;
	}
	
	//comprueba que en esa fila no este asignado ya value
	private static boolean rowNotValid(Kakuro kak,int i, int j, int value) {
		//comprobamos toda la fila
		boolean stop = false;
		//des de donde estamos (i,j) hacia arriba por filas
		for(int auxJ = j; !stop && auxJ<kak.getWidth(); ++auxJ) {	//al ponerlo >0 evitamos la primera fila
			//si la casilla es Solved (tiene numero) y el valor coincide devuelve true
			if(kak.getCellColor(i, auxJ) == "Solved" && kak.getCellValue(i, auxJ) == value) return true;
			else if(kak.getCellColor(i,auxJ) == "EmptyBlack") stop = true;
		}
		stop = false;
		for(int auxJ = j; !stop && auxJ>0; --auxJ) {
			if(kak.getCellColor(i,auxJ) == "Solved" && kak.getCellValue(i, auxJ) == value) return true;
			else if(kak.getCellColor(i, auxJ) == "EmptyBlack") stop = true;
		}
		//en cualquier otro caso devuelve false
		return false;
	}
	
	//comprueba que no se han usado los 9 posibles valores (la casilla no se podria rellenar)
	private static boolean checkMax(Kakuro kak, int i, int j) {
		int suma = 0;
		boolean stop = false;
		//calculamos de la posicion hacia arriba
		for(int auxI = i; !stop && auxI>0; --auxI) {
			if(kak.getCellColor(auxI, j) == "Solved") suma+=kak.getCellValue(auxI, j);
			else if(kak.getCellColor(auxI, j)=="EmptyBlack" || kak.getCellColor(auxI, j)=="White") stop = true;
		}
		stop = false;
		//calculamos de la posicion hacia abajo
		for(int auxI = i; !stop && auxI<kak.getHeight(); ++auxI) {
			if(kak.getCellColor(auxI, j) == "Solved") suma+=kak.getCellValue(auxI, j);
			else if(kak.getCellColor(auxI, j)=="EmptyBlack" || kak.getCellColor(auxI, j)=="White") stop = true;
		}
		stop = false;
		//calculamos de la posicion hacia la izquierda
		for(int auxJ = j; !stop && auxJ>0; --auxJ) {
			if(kak.getCellColor(i, auxJ) == "Solved") suma+=kak.getCellValue(i, auxJ);
			else if(kak.getCellColor(i, auxJ)=="EmptyBlack" || kak.getCellColor(i, auxJ)=="White") stop = true;
		}
		stop = false;
		//calculamos de la posicion hacia la derecha
		for(int auxJ = j; !stop && auxJ<kak.getWidth(); ++auxJ) {
			if(kak.getCellColor(i, auxJ) == "Solved") suma+=kak.getCellValue(i, auxJ);
			else if(kak.getCellColor(i, auxJ)=="EmptyBlack" || kak.getCellColor(i, auxJ)=="White") stop = true;
		}
		return (suma >= 45);	//como acumulamos el valor de cada casilla, comprobamos con 45
	}
	
	
	
	//OPTIMIZACIONES-> asigna combinaciones unicas de suma para facilitar la creacion de un kakuro con solucion unica
	
	//comprueba si hay posibles combinaciones sumas en casillas de 2,3 o 4 sumas
	private static void checkOptimizations(Kakuro kak,int i,int j) {
		int m = getRandomNumber(1,9999);
		int n = m%4;	//decidir cual de las 4 combinaciones
		//horizontal
		if(kak.getCellColor(i, j-1)=="EmptyBlack") {
			if(kak.getNumWhitesHor(i,j) == 2) {
				if(n == 0){
					if (m%2 == 0) optimize2Hor(kak,i,j,2,1);
					else optimize2Hor(kak,i,j,1,2);
				}
				if(n == 1){
					if (m%2 == 0) optimize2Hor(kak,i,j,1,3);
					else optimize2Hor(kak,i,j,3,1);
				}
				if(n == 2){
					if (m%2 == 0) optimize2Ver(kak,i,j,9,8);
					else optimize2Hor(kak,i,j,8,9);
				}
				if(n == 3){
					if (m%2 == 0) optimize2Ver(kak,i,j,7,9);
					else optimize2Hor(kak,i,j,9,7);
				}
			}
			else if(kak.getNumWhitesHor(i,j) == 3){
				if(n == 0){
					if (m%3 == 0) optimize3Hor(kak,i,j,2,1,3);
					else if(m%3 == 1) optimize3Hor(kak,i,j,1,2,3);
					else optimize3Hor(kak,i,j,3,1,2);
				}
				if(n == 1){
					if (m%3 == 0) optimize3Hor(kak,i,j,2,1,4);
					else if(m%3 == 1) optimize3Hor(kak,i,j,1,2,4);
					else optimize3Hor(kak,i,j,4,1,2);
				}
				if(n == 2){
					if (m%3 == 0) optimize3Hor(kak,i,j,9,8,7);
					else if(m%3 == 1) optimize3Hor(kak,i,j,8,9,7);
					else optimize3Hor(kak,i,j,7,8,9);
				}
				if(n == 3){
					if (m%3 == 0) optimize3Hor(kak,i,j,9,8,6);
					else if(m%3 == 1) optimize3Hor(kak,i,j,8,9,6);
					else optimize3Hor(kak,i,j,6,8,9);
				}
			}
			else if(kak.getNumWhitesHor(i, j) == 4) {
				if(n == 0) {//suma
					if(m%6 == 0) optimize4Hor(kak,i,j,1,2,3,4);
					else if(m%6 == 1) optimize4Hor(kak,i,j,1,2,4,3);
					else if(m%6 == 2) optimize4Hor(kak,i,j,1,3,2,4);
					else if(m%6 == 3) optimize4Hor(kak,i,j,1,3,4,2);
					else if(m%6 == 4) optimize4Hor(kak,i,j,1,4,2,3);
					else optimize4Hor(kak,i,j,1,4,3,2);
				}
				if(n == 1) {
					if(m%6 == 0) optimize4Hor(kak,i,j,2,5,3,1);
					else if(m%6 == 1) optimize4Hor(kak,i,j,2,5,1,3);
					else if(m%6 == 2) optimize4Hor(kak,i,j,2,3,1,5);
					else if(m%6 == 3) optimize4Hor(kak,i,j,2,3,5,1);
					else if(m%6 == 4) optimize4Hor(kak,i,j,2,1,5,3);
					else optimize4Hor(kak,i,j,2,1,3,4);
				}
				if(n == 2) {
					if(m%6 == 0) optimize4Hor(kak,i,j,9,7,6,8);
					else if(m%6 == 1) optimize4Hor(kak,i,j,9,7,8,6);
					else if(m%6 == 2) optimize4Hor(kak,i,j,9,6,7,8);
					else if(m%6 == 3) optimize4Hor(kak,i,j,9,6,8,7);
					else if(m%6 == 4) optimize4Hor(kak,i,j,9,8,7,6);
					else optimize4Hor(kak,i,j,9,8,6,7);
				}
				if(n == 3) {
					if(m%6 == 0) optimize4Hor(kak,i,j,9,7,5,8);
					else if(m%6 == 1) optimize4Hor(kak,i,j,9,7,8,5);
					else if(m%6 == 2) optimize4Hor(kak,i,j,9,5,7,8);
					else if(m%6 == 3) optimize4Hor(kak,i,j,9,5,8,7);
					else if(m%6 == 4) optimize4Hor(kak,i,j,9,8,7,5);
					else optimize4Hor(kak,i,j,9,8,5,7);
				}
			}
		}
		//vertical
		if(kak.getCellColor(i-1, j)=="EmptyBlack") {
			if(kak.getNumWhitesVert(i,j) == 2) {
				if(n == 0){//suma 3
					if (m%2 == 0) optimize2Ver(kak,i,j,2,1);
					else optimize2Ver(kak,i,j,1,2);
				}
				if(n == 1){//suma 4
					if (m%2 == 0) optimize2Ver(kak,i,j,1,3);
					else optimize2Ver(kak,i,j,3,1);
				}
				if(n == 2){//suma 17
					if (m%2 == 0) optimize2Ver(kak,i,j,9,8);
					else optimize2Ver(kak,i,j,8,9);
				}
				if(n == 3){//suma 16
					if (m%2 == 0) optimize2Ver(kak,i,j,7,9);
					else optimize2Ver(kak,i,j,9,7);
				}
			}
			else if(kak.getNumWhitesVert(i,j) == 3){
				if(n == 0){//suma 6
					if (m%3 == 0) optimize3Ver(kak,i,j,2,1,3);
					else if(m%3 == 1) optimize3Ver(kak,i,j,1,2,3);
					else optimize3Ver(kak,i,j,3,1,2);
				}
				if(n == 1){//suma 7
					if (m%3 == 0) optimize3Ver(kak,i,j,2,1,4);
					else if(m%3 == 1) optimize3Ver(kak,i,j,1,2,4);
					else optimize3Ver(kak,i,j,4,1,2);
				}
				if(n == 2){//suma 22
					if (m%3 == 0) optimize3Ver(kak,i,j,9,8,7);
					else if(m%3 == 1) optimize3Ver(kak,i,j,8,9,7);
					else optimize3Ver(kak,i,j,7,8,9);
				}
				if(n == 3){//suma 21
					if (m%3 == 0) optimize3Ver(kak,i,j,9,8,6);
					else if(m%3 == 1) optimize3Ver(kak,i,j,8,9,6);
					else optimize3Ver(kak,i,j,6,8,9);
				}
			}
			else if(kak.getNumWhitesVert(i, j) == 4) {
				if(n == 0) {//suma 11
					if(m%6 == 0) optimize4Ver(kak,i,j,1,2,3,4);
					else if(m%6 == 1) optimize4Ver(kak,i,j,1,2,4,3);
					else if(m%6 == 2) optimize4Ver(kak,i,j,1,3,2,4);
					else if(m%6 == 3) optimize4Ver(kak,i,j,1,3,4,2);
					else if(m%6 == 4) optimize4Ver(kak,i,j,1,4,2,3);
					else optimize4Ver(kak,i,j,1,4,3,2);
				}
				if(n == 1) {//suma 12
					if(m%6 == 0) optimize4Ver(kak,i,j,2,5,3,1);
					else if(m%6 == 1) optimize4Ver(kak,i,j,2,5,1,3);
					else if(m%6 == 2) optimize4Ver(kak,i,j,2,3,1,5);
					else if(m%6 == 3) optimize4Ver(kak,i,j,2,3,5,1);
					else if(m%6 == 4) optimize4Ver(kak,i,j,2,1,5,3);
					else optimize4Ver(kak,i,j,2,1,3,4);
				}
				if(n == 2) {//suma 30
					if(m%6 == 0) optimize4Ver(kak,i,j,9,7,6,8);
					else if(m%6 == 1) optimize4Ver(kak,i,j,9,7,8,6);
					else if(m%6 == 2) optimize4Ver(kak,i,j,9,6,7,8);
					else if(m%6 == 3) optimize4Ver(kak,i,j,9,6,8,7);
					else if(m%6 == 4) optimize4Ver(kak,i,j,9,8,7,6);
					else optimize4Ver(kak,i,j,9,8,6,7);
				}
				if(n == 3) {//suma 29
					if(m%6 == 0) optimize4Ver(kak,i,j,9,7,5,8);
					else if(m%6 == 1) optimize4Ver(kak,i,j,9,7,8,5);
					else if(m%6 == 2) optimize4Ver(kak,i,j,9,5,7,8);
					else if(m%6 == 3) optimize4Ver(kak,i,j,9,5,8,7);
					else if(m%6 == 4) optimize4Ver(kak,i,j,9,8,7,5);
					else optimize4Ver(kak,i,j,9,8,5,7);
				}
			}
		}		
	}
	
	//comprueba las combinaciones en casillas de 2 verticales
	private static void optimize2Ver(Kakuro kak, int i, int j, int pos1, int pos2){
		if(rowNotValid(kak,i,j,pos1) || columnNotValid(kak,i,j,pos1)){
			//si no podemos poner pos1 probamos de poner pos2
			if(!(rowNotValid(kak,i,j,pos2) || columnNotValid(kak,i,j,pos2))){
				//si se puede poner el pos1 lo anadimos
				kak.setCellValue(i,j,pos2);
				if(i+1<kak.getHeight() && !(rowNotValid(kak,i+1,j,pos1) || columnNotValid(kak,i+1,j,pos1))){
					//si no podemos poner el pos2 en la siguiente posicion ponemos un random
					kak.setCellValue(i+1,j,pos1);
				}
			}
		}
		else{
			//si se puede poner el pos1 lo anadimos
			kak.setCellValue(i,j,pos1);
			//probamos de poner el pos2
			if(i+1<kak.getHeight() && !(rowNotValid(kak,i+1,j,pos2) || columnNotValid(kak,i+1,j,pos2))){
				//si tampoco podemos poner el pos2 ponemos un random
				kak.setCellValue(i+1,j,pos2);
			}
		}
	}
	
	//comprueba las combinaciones en casillas de 2 horizontales
	private static void optimize2Hor(Kakuro kak, int i, int j, int pos1, int pos2){
		if(rowNotValid(kak,i,j,pos1) || columnNotValid(kak,i,j,pos1)){
			//si no podemos poner pos1 probamos de poner pos2
			if(!(rowNotValid(kak,i,j,pos2) || columnNotValid(kak,i,j,pos2))){
				//si se puede poner el pos1 lo anadimos
				kak.setCellValue(i,j,pos2);
				if(j+1<kak.getWidth() && !(rowNotValid(kak,i,j+1,pos1) || columnNotValid(kak,i,j+1,pos1))){
					//si no podemos poner el pos2 en la siguiente posicion ponemos un random
					kak.setCellValue(i,j+1,pos1);
				}
			}
		}
		else{
			//si se puede poner el pos1 lo anadimos
			kak.setCellValue(i,j,pos1);
			//probamos de poner el pos2
			if(j+1<kak.getWidth() && !(rowNotValid(kak,i,j+1,pos2) || columnNotValid(kak,i,j+1,pos2))){
				//si tampoco podemos poner el pos2 ponemos un random
				kak.setCellValue(i,j+1,pos2);
			}
		}
	}

	//comprueba las combinaciones en casillas de 3 verticales
	private static void optimize3Ver(Kakuro kak, int i, int j, int pos1, int pos2, int pos3){
		if(rowNotValid(kak,i,j,pos1) || columnNotValid(kak,i,j,pos1)){
			//si no se puede poner pos1 probamos a poner pos2
			if(rowNotValid(kak,i,j,pos2) || columnNotValid(kak,i,j,pos2)){
				//si no se puede poner pos1 ni pos2 probamos pos3
				if(!(rowNotValid(kak,i,j,pos3) || columnNotValid(kak,i,j,pos3))){
					kak.setCellValue(i,j,pos3);
					//probamos con la siguiente fila
					if(i+1<kak.getHeight() && (rowNotValid(kak,i+1,j,pos1) || columnNotValid(kak,i+1,j,pos1))){
						if(!(rowNotValid(kak,i+1,j,pos2) || columnNotValid(kak,i+1,j,pos2))){
							kak.setCellValue(i+1,j,pos2);
							if(i+2<kak.getHeight() && !(rowNotValid(kak,i+2,j,pos1) || columnNotValid(kak,i+2,j,pos1)))
								kak.setCellValue(i+2,j,pos1);
						}
					}
					else if(i+1<kak.getHeight()){
						kak.setCellValue(i+1,j,pos1);
						if(i+2<kak.getHeight() && !(rowNotValid(kak,i+2,j,pos2) || columnNotValid(kak,i+2,j,pos2)))
							kak.setCellValue(i+2,j,pos2);
					}
				}
			}
			else{
				kak.setCellValue(i,j,pos2);
				//probamos a poner pos1 y luego pos3
				if(i+1<kak.getHeight() && (rowNotValid(kak,i+1,j,pos1) || columnNotValid(kak,i+1,j,pos1))){
						if(!(rowNotValid(kak,i+1,j,pos3) || columnNotValid(kak,i+1,j,pos3))){
							kak.setCellValue(i+1,j,pos3);
							if(i+2<kak.getHeight() && !(rowNotValid(kak,i+2,j,pos1) || columnNotValid(kak,i+2,j,pos1))) kak.setCellValue(i+2,j,pos1);
						}
					}
					else if(i+1<kak.getHeight()){
						kak.setCellValue(i+1,j,pos1);
						if(i+2<kak.getHeight() && !(rowNotValid(kak,i+2,j,pos3) || columnNotValid(kak,i+2,j,pos3))) kak.setCellValue(i+2,j,pos3);
					}
			}
		}
		else{
			kak.setCellValue(i,j,pos1);
			if(i+1<kak.getHeight() && (rowNotValid(kak,i+1,j,pos2) || columnNotValid(kak,i+1,j,pos2))){
				if(!(rowNotValid(kak,i+1,j,pos3) || columnNotValid(kak,i+1,j,pos3))){
					kak.setCellValue(i+1,j,pos3);
					if(i+2<kak.getHeight() && !(rowNotValid(kak,i+2,j,pos2) || columnNotValid(kak,i+2,j,pos2))) kak.setCellValue(i+2,j,pos2);
				}
			}
			else if(i+1<kak.getHeight()){
				kak.setCellValue(i+1,j,pos2);
				if(i+2<kak.getHeight() && !(rowNotValid(kak,i+2,j,pos3) || columnNotValid(kak,i+2,j,pos3))) kak.setCellValue(i+2,j,pos3);
			}
		}
	}

	//comprueba las combinaciones en casillas de 3 horizontales
	private static void optimize3Hor(Kakuro kak, int i, int j, int pos1, int pos2, int pos3){
		if(rowNotValid(kak,i,j,pos1) || columnNotValid(kak,i,j,pos1)){
			//si no se puede poner pos1 probamos a poner pos2
			if(rowNotValid(kak,i,j,pos2) || columnNotValid(kak,i,j,pos2)){
				//si no se puede poner pos1 ni pos2 probamos pos3
				if(!(rowNotValid(kak,i,j,pos3) || columnNotValid(kak,i,j,pos3))){
					kak.setCellValue(i,j,pos3);
					//probamos con la siguiente fila
					if(j+1<kak.getWidth() && (rowNotValid(kak,i,j+1,pos1) || columnNotValid(kak,i,j+1,pos1))){
						if(!(rowNotValid(kak,i,j+1,pos2) || columnNotValid(kak,i,j+1,pos2))){
							kak.setCellValue(i,j+1,pos2);
							if(j+2<kak.getWidth() && !(rowNotValid(kak,i,j+2,pos1) || columnNotValid(kak,i,j+2,pos1))) kak.setCellValue(i,j+2,pos1);
						}
					}
					else if(j+1<kak.getWidth()){
						kak.setCellValue(i,j+1,pos1);
						if(j+2<kak.getWidth() && !(rowNotValid(kak,i,j+2,pos2) || columnNotValid(kak,i,j+2,pos2))) kak.setCellValue(i,j+2,pos2);
					}
				}
			}
			else{
				kak.setCellValue(i,j,pos2);
				//probamos a poner pos1 y luego pos3
				if(j+1<kak.getWidth() && (rowNotValid(kak,i,j+1,pos1) || columnNotValid(kak,i,j+1,pos1))){
					if(!(rowNotValid(kak,i,j+1,pos3) || columnNotValid(kak,i,j+1,pos3))){
						kak.setCellValue(i,j+1,pos3);
						if(j+2<kak.getWidth() && !(rowNotValid(kak,i,j+2,pos1) || columnNotValid(kak,i,j+2,pos1))) kak.setCellValue(i,j+2,pos1);
					}
				}
				else if(j+1<kak.getWidth()){
					kak.setCellValue(i,j+1,pos1);
					if(j+2<kak.getWidth() && !(rowNotValid(kak,i,j+2,pos3) || columnNotValid(kak,i,j+2,pos3))) kak.setCellValue(i,j+2,pos3);
				}
			}
		}
		else{
			kak.setCellValue(i,j,pos1);
			if(j+1<kak.getWidth() && (rowNotValid(kak,i,j+1,pos2) || columnNotValid(kak,i,j+1,pos2))){
				if(!(rowNotValid(kak,i,j+1,pos3) || columnNotValid(kak,i,j+1,pos3))){
					kak.setCellValue(i,j+1,pos3);
					if(j+2<kak.getWidth() && !(rowNotValid(kak,i,j+2,pos2) || columnNotValid(kak,i,j+2,pos2))) kak.setCellValue(i,j+2,pos2);
				}
			}
			else if(j+1<kak.getWidth()){
				kak.setCellValue(i,j+1,pos2);
				if(j+2<kak.getWidth() && (rowNotValid(kak,i,j+2,pos3) || columnNotValid(kak,i,j+2,pos3))) kak.setCellValue(i,j+2,pos3);
			}
		}
	}
	
	//comprueba las combinaciones en casillas en 4 verticales
	private static void optimize4Ver(Kakuro kak, int i, int j, int pos1, int pos2, int pos3, int pos4){
		int height = kak.getHeight();
		if(rowNotValid(kak,i,j,pos1) || columnNotValid(kak,i,j,pos1)){	//1 no valido (1)
			if(rowNotValid(kak,i,j,pos2) || columnNotValid(kak,i,j,pos2)){	//2 no valido	(2)
				if(rowNotValid(kak,i,j,pos3)|| columnNotValid(kak,i,j,pos3)){	//3 no valido	(3)
					if(!(rowNotValid(kak,i,j,pos4)|| columnNotValid(kak,i,j,pos4))){	//4 valido (4)
						kak.setCellValue(i,j,pos4);
						if(i+1<height && (rowNotValid(kak,i+1,j,pos1) || columnNotValid(kak,i+1,j,pos1))){	//1 no valido
							if(rowNotValid(kak,i+1,j,pos2) || columnNotValid(kak,i+1,j,pos2)){	//2 no valido
								if(!(rowNotValid(kak,i+1,j,pos3) || columnNotValid(kak,i+1,j,pos3))){	//3 valido (4,3)
									kak.setCellValue(i+1,j,pos3);
									if(i+2<height && (rowNotValid(kak,i+2,j,pos1) || columnNotValid(kak,i+2,j,pos1))){	//1 no valido
										if(!rowNotValid(kak,i+2,j,pos1) || columnNotValid(kak,i+2,j,pos1)){	//2 valido (4,3,2)
											kak.setCellValue(i+2,j,pos2);
											if(i+3<height && !(rowNotValid(kak,i+3,j,pos1) || columnNotValid(kak,i+3,j,pos1)))	kak.setCellValue(i+3,j,pos1);	//1 valida (4,3,2,1)
										}//2 no valido no hacemos nada
									}
									else{	// 1 valido (4,3,1)
										kak.setCellValue(i+2,j,pos1);
										if(i+3<height && !(rowNotValid(kak,i+3,j,pos2) || columnNotValid(kak,i+3,j,pos2)))	kak.setCellValue(i+3,j,pos2);	//2 valido (4,3,1,2)
									}
								}//3 no valido no hacemos nada
							}
							else if(i+1<height){	//2 valido (4,2)
								kak.setCellValue(i+1,j,pos2);
								if(i+2<height && (rowNotValid(kak,i+2,j,pos1) || columnNotValid(kak,i+2,j,pos1))){	//1 no valido
									if(i+2<height && !(rowNotValid(kak,i+2,j,pos3) || columnNotValid(kak,i+2,j,pos3))){	//3 valido (4,2,3)
										kak.setCellValue(i+2,j,pos3);
										if(i+3<height && !(rowNotValid(kak,i+3,j,pos1) || columnNotValid(kak,i+3,j,pos1))) kak.setCellValue(i+3,j,pos1);	//1 valido	(4,2,3,1)
									}//3 no valido no hacemos nada
								}
								else if(i+2<height){	//1 valido (4,2,1)
									kak.setCellValue(i+2,j,pos1);
									if(i+3<height && !(rowNotValid(kak,i+3,j,pos1) || columnNotValid(kak,i+3,j,pos1))) kak.setCellValue(i+3,j,pos3);	//3 valido (4,2,1,3)
								}
							}
						}
						else if(i+1<height){	//1 valido (4,1)
							kak.setCellValue(i+1,j,pos1);
							if(i+2<height && (rowNotValid(kak,i+2,j,pos2) || columnNotValid(kak,i+2,j,pos2))){	//2 no valido
								if(!(rowNotValid(kak,i+2,j,pos3) || columnNotValid(kak,i+2,j,pos3))){	//3 valido (4,1,3)
									kak.setCellValue(i+2,j,pos3);
									if(i+3<height && !(rowNotValid(kak,i+3,j,pos2) || columnNotValid(kak,i+3,j,pos2))) kak.setCellValue(i+3,j,pos2);	//2 valido (4,1,3,2)
								}
							}
							else if(i+2<height){	//2 valido
								kak.setCellValue(i+2,j,pos2);
								if(i+3<height && !(rowNotValid(kak,i+3,j,pos3) || columnNotValid(kak,i+3,j,pos3))) kak.setCellValue(i+3,j,pos3);	//3 valido (4,1,2,3)
							}
						}
					}//4 no valido no hacemos nada
				}
				else{	//3 valido (3)
					kak.setCellValue(i,j,pos3);
					if(i+1<height && rowNotValid(kak,i+1,j,pos1) || columnNotValid(kak,i+1,j,pos1)){	//1 no valido
						if(rowNotValid(kak,i+1,j,pos2) || columnNotValid(kak,i+1,j,pos2)){	//2 no valido
							if(!(rowNotValid(kak,i+1,j,pos4) || columnNotValid(kak,i+1,j,pos4))){	//4 valido (3,4)
								kak.setCellValue(i+1,j,pos4);
								if(i+2<height && (rowNotValid(kak,i+2,j,pos1) || columnNotValid(kak,i+2,j,pos1))){	//1 no valido
									if(rowNotValid(kak,i+2,j,pos2) || columnNotValid(kak,i+2,j,pos2)){	//2 valido (3,4,2)
										kak.setCellValue(i+2,j,pos2);
										if(i+3<height && !(rowNotValid(kak,i+3,j,pos1) || columnNotValid(kak,i+3,j,pos1))) kak.setCellValue(i+3,j,pos1);	//1 valido (3,4,2,1)
									}
								}
								else if(i+2<height){	//1 valido (3,4,1)
									kak.setCellValue(i+2,j,pos1);
									if(i+3<height && !(rowNotValid(kak,i+3,j,pos2) || columnNotValid(kak,i+2,j,pos2))) kak.setCellValue(i+3,j,pos2);	//2 valido (3,4,1,2)
								}
							}
						}
						else{	//2 valido (3,2)
							kak.setCellValue(i+1,j,pos2);
							if(i+2<height && (rowNotValid(kak,i+2,j,pos1) || columnNotValid(kak,i+2,j,pos1))){	//1 no valido
								if(!(rowNotValid(kak,i+2,j,pos4) || columnNotValid(kak,i+2,j,pos4))){	//4 valido (3,2,4)
									kak.setCellValue(i+2,j,pos4);
									if(i+3<height && !(rowNotValid(kak,i+3,j,pos1) || columnNotValid(kak,i+3,j,pos1))) kak.setCellValue(i+3,j,pos1);	//1 valido (3,2,4,1)
								}
							}
							else if(i+2<height){	//1 valido (3,2,1)
								kak.setCellValue(i+2,j,pos1);
								if(i+3<height && !(rowNotValid(kak,i+3,j,pos4) || columnNotValid(kak,i+3,j,pos4))) kak.setCellValue(i+3,j,pos4);	//4 valido (3,2,1,4)
							}
						}
					}
					else if(i+1<height){	//1 valido (3,1)
						kak.setCellValue(i+1,j,pos1);
						if(i+2<height && !(rowNotValid(kak,i+2,j,pos2) || columnNotValid(kak,i+2,j,pos2))){	//2 no valido
							if(!(rowNotValid(kak,i+2,j,pos4) || columnNotValid(kak,i+2,j,pos4))){	//4 valido (3,1,4)
								kak.setCellValue(i+2,j,pos4);
								if(i+3<height && !(rowNotValid(kak,i+3,j,pos2) || columnNotValid(kak,i+3,j,pos2))) kak.setCellValue(i+3,j,pos2);	//2 valido (3,1,4,2)
							}
						}
						else if(i+2<height){	//2 valido (3,1,2)
							kak.setCellValue(i+2,j,pos2);
							if(i+3<height && !(rowNotValid(kak,i+2,j,pos4) || columnNotValid(kak,i+2,j,pos4))) kak.setCellValue(i+3,j,pos4);	//4 valido (3,1,2,4)
						}
					}
				}
			}
			else{	//2 valido (2)
				kak.setCellValue(i,j,pos2);
				if(i+1<height && (rowNotValid(kak,i+1,j,pos1) || columnNotValid(kak,i+1,j,pos1))){	//1 no valido
					if(rowNotValid(kak,i+1,j,pos3) || columnNotValid(kak,i+1,j,pos3)){	//3 no valido
						if(!(rowNotValid(kak,i+1,j,pos4) || columnNotValid(kak,i+1,j,pos4))){	//4 valido (2,4)
							kak.setCellValue(i+1,j,pos4);
							if(i+2<height && (rowNotValid(kak,i+2,j,pos1) || columnNotValid(kak,i+2,j,pos1))){	//1 no valido
								if(!(rowNotValid(kak,i+2,j,pos3) || columnNotValid(kak,i+2,j,pos3))){	//3 valido (2,4,3)
									kak.setCellValue(i+2,j,pos3);
									if(i+3<height && !(rowNotValid(kak,i+3,j,pos1) || columnNotValid(kak,i+3,j,pos1))) kak.setCellValue(i+3,j,pos1);	//1 valido (2,4,3,1)
								}
							}
							else if(i+2<height){	//1 valido (2,4,1)
								kak.setCellValue(i+2,j,pos1);
								if(!(rowNotValid(kak,i+1,j,pos1) || columnNotValid(kak,i+1,j,pos1))) kak.setCellValue(i, i+1, pos3);	//3 valido (2,4,1,3)
							}
						}
					}
				}
				else if(i+1<height){	//1 valido (2,1)
					kak.setCellValue(i+1,j,pos1);
					if(i+2<height && (rowNotValid(kak,i+2,j,pos3) || columnNotValid(kak,i+2,j,pos3))){	//3 no valido
						if(!(rowNotValid(kak,i+2,j,pos4) || columnNotValid(kak,i+2,j,pos4))){	//4 valido (2,1,4)
							kak.setCellValue(i+2,j,pos4);
							if(i+3<height && !(rowNotValid(kak,i+3,j,pos3) || columnNotValid(kak,i+3,j,pos3))) kak.setCellValue(i+3,j,pos3);	//3 valido (2,1,4,3)
						}
					}
					else if(i+2<height){	//3 valido (2,1,3)
						kak.setCellValue(i+2,j,pos3);
						if(i+3<height && !(rowNotValid(kak,i+3,j,pos4) || columnNotValid(kak,i+3,j,pos4))) kak.setCellValue(i+3,j,pos4);	//4 valido (2,1,3,4)
					}
				}
			}
		}
		else{	//1 valido (1)
			kak.setCellValue(i,j,pos1);
			if(i+1<height && (rowNotValid(kak, i+1,j,pos2) || columnNotValid(kak, i+1,j,pos2))){	//2 no valido
				if(rowNotValid(kak, i+1,j,pos3) || columnNotValid(kak, i+1,j,pos3)){	//3 no valido
					if(!(rowNotValid(kak, i+1,j,pos4) || columnNotValid(kak, i+1,j,pos4))){	//4 valido (1,4)
						kak.setCellValue(i+1,j,pos4);
						if(i+2<height && (rowNotValid(kak, i+2,j,pos2) || columnNotValid(kak, i+2,j,pos2))){	//2 no valido
							if(!(rowNotValid(kak, i+2,j,pos3) || columnNotValid(kak, i+2,j,pos3))){	//3 valido (1,4,3)
								kak.setCellValue(i+2,j,pos3);
								if(i+3<height && !(rowNotValid(kak, i+3,j,pos2) || columnNotValid(kak, i+3,j,pos2))) kak.setCellValue(i+3,j,pos2);	//2 valido (1,4,3,2)
							}
						}
						else if(i+2<height){	//2 valido (1,4,2)
							kak.setCellValue(i+2,j,pos2);
							if(i+3<height && !(rowNotValid(kak, i+3,j,pos3) || columnNotValid(kak, i+3,j,pos3))) kak.setCellValue(i+3,j,pos3);	//3 valido (1,4,2,3)
						}
					}
				}
				else{	//3 valido (1,3)
					kak.setCellValue(i+1,j,pos3);
					if(i+2<height && (rowNotValid(kak, i+2,j,pos2) || columnNotValid(kak, i+2,j,pos2))){	//2 no valido
						if(rowNotValid(kak, i+2,j,pos4) || columnNotValid(kak, i+2,j,pos4)){	//4 valido (1,3,4)
							kak.setCellValue(i+2,j,pos4);
							if(i+3<height && !(rowNotValid(kak, i+3,j,pos2) || columnNotValid(kak, i+3,j,pos2))) kak.setCellValue(i+3,j,pos2);	//2 valido (1,3,4,2)
						}
					}
					else if(i+2<height){	//2 valido	(1,3,2)
						kak.setCellValue(i+2,j,pos2);
						if(i+3<height && !(rowNotValid(kak, i+3,j,pos4) || columnNotValid(kak, i+3,j,pos4))) kak.setCellValue(i+3,j,pos4);	//4 valido (1,3,2,4)
					}
				}
			}
			else if(i+1<height){	//2 valido (1,2)
				kak.setCellValue(i+1,j,pos2);
				if(i+2<height && (rowNotValid(kak, i+2,j,pos3) || columnNotValid(kak, i+2,j,pos3))){	//3 no valido
					if(rowNotValid(kak, i+2,j,pos4) || columnNotValid(kak, i+2,j,pos4)){	//4 valido (1,2,4)
						kak.setCellValue(i+2,j,pos4);
						if(i+3<height && !(rowNotValid(kak, i+3,j,pos3) || columnNotValid(kak, i+3,j,pos3))) kak.setCellValue(i+3,j,pos3);	//3 valido (1,2,4,3)
					}
				}
				else if(i+2<height){	//3 valido (1,2,3)
					kak.setCellValue(i+2,j,pos3);
					if(i+3<height && !(rowNotValid(kak, i+3,j,pos4) || columnNotValid(kak, i+3,j,pos4))) kak.setCellValue(i+3,j,pos4);	//4 valido (1,2,3,4)
				}
			}
		}
	}

	//comprueba las combinaciones en casillas de 4 horizontales
	private static void optimize4Hor(Kakuro kak, int i, int j, int pos1, int pos2, int pos3, int pos4){
		int width = kak.getWidth();
		if(rowNotValid(kak,i,j,pos1) || columnNotValid(kak,i,j,pos1)){	//1 no valido (1)
			if(rowNotValid(kak,i,j,pos2) || columnNotValid(kak,i,j,pos2)){	//2 no valido	(2)
				if(rowNotValid(kak,i,j,pos3)|| columnNotValid(kak,i,j,pos3)){	//3 no valido	(3)
					if(!(rowNotValid(kak,i,j,pos4)|| columnNotValid(kak,i,j,pos4))){	//4 valido (4)
						kak.setCellValue(i,j,pos4);
						if(j+1<width && (rowNotValid(kak,i,j+1,pos1) || columnNotValid(kak,i,j+1,pos1))){	//1 no valido
							if(rowNotValid(kak,i,j+1,pos2) || columnNotValid(kak,i,j+1,pos2)){	//2 no valido
								if(!(rowNotValid(kak,i,j+1,pos3) || columnNotValid(kak,i,j+1,pos3))){	//3 valido (4,3)
									kak.setCellValue(i,j+1,pos3);
									if(j+2<width && (rowNotValid(kak,i,j+2,pos1) || columnNotValid(kak,i,j+2,pos1))){	//1 no valido
										if(!rowNotValid(kak,i,j+2,pos1) || columnNotValid(kak,i,j+2,pos1)){	//2 valido (4,3,2)
											kak.setCellValue(i,j+2,pos2);
											if(j+3<width && !(rowNotValid(kak,i,j+3,pos1) || columnNotValid(kak,i,j+3,pos1)))	kak.setCellValue(i,j+3,pos1);	//1 valida (4,3,2,1)
										}//2 no valido no hacemos nada
									}
									else{	// 1 valido (4,3,1)
										kak.setCellValue(i,j+2,pos1);
										if(j+3<width && !(rowNotValid(kak,i,j+3,pos2) || columnNotValid(kak,i,j+3,pos2)))	kak.setCellValue(i,j+3,pos2);	//2 valido (4,3,1,2)
									}
								}//3 no valido no hacemos nada
							}
							else if(j+1<width){	//2 valido (4,2)
								kak.setCellValue(i,j+1,pos2);
								if(j+2<width && (rowNotValid(kak,i,j+2,pos1) || columnNotValid(kak,i,j+2,pos1))){	//1 no valido
									if(j+2<width && !(rowNotValid(kak,i,j+2,pos3) || columnNotValid(kak,i,j+2,pos3))){	//3 valido (4,2,3)
										kak.setCellValue(i,j+2,pos3);
										if(j+3<width && !(rowNotValid(kak,i,j+3,pos1) || columnNotValid(kak,i,j+3,pos1))) kak.setCellValue(i,j+3,pos1);	//1 valido	(4,2,3,1)
									}//3 no valido no hacemos nada
								}
								else if(j+2<width){	//1 valido (4,2,1)
									kak.setCellValue(i,j+2,pos1);
									if(j+3<width && !(rowNotValid(kak,i,j+3,pos1) || columnNotValid(kak,i,j+3,pos1))) kak.setCellValue(i,j+3,pos3);	//3 valido (4,2,1,3)
								}
							}
						}
						else if(j+1<width){	//1 valido (4,1)
							kak.setCellValue(i,j+1,pos1);
							if(j+2<width && (rowNotValid(kak,i,j+2,pos2) || columnNotValid(kak,i,j+2,pos2))){	//2 no valido
								if(!(rowNotValid(kak,i,j+2,pos3) || columnNotValid(kak,i,j+2,pos3))){	//3 valido (4,1,3)
									kak.setCellValue(i,j+2,pos3);
									if(j+3<width && !(rowNotValid(kak,i,j+3,pos2) || columnNotValid(kak,i,j+3,pos2))) kak.setCellValue(i,j+3,pos2);	//2 valido (4,1,3,2)
								}
							}
							else if(j+2<width){	//2 valido
								kak.setCellValue(i,j+2,pos2);
								if(j+3<width && !(rowNotValid(kak,i,j+3,pos3) || columnNotValid(kak,i,j+3,pos3))) kak.setCellValue(i,j+3,pos3);	//3 valido (4,1,2,3)
							}
						}
					}//4 no valido no hacemos nada
				}
				else{	//3 valido (3)
					kak.setCellValue(i,j,pos3);
					if(j+1<width && rowNotValid(kak,i,j+1,pos1) || columnNotValid(kak,i,j+1,pos1)){	//1 no valido
						if(rowNotValid(kak,i,j+1,pos2) || columnNotValid(kak,i,j+1,pos2)){	//2 no valido
							if(!(rowNotValid(kak,i,j+1,pos4) || columnNotValid(kak,i,j+1,pos4))){	//4 valido (3,4)
								kak.setCellValue(i,j+1,pos4);
								if(j+2<width && (rowNotValid(kak,i,j+2,pos1) || columnNotValid(kak,i,j+2,pos1))){	//1 no valido
									if(rowNotValid(kak,i,j+2,pos2) || columnNotValid(kak,i,j+2,pos2)){	//2 valido (3,4,2)
										kak.setCellValue(i,j+2,pos2);
										if(j+3<width && !(rowNotValid(kak,i,j+3,pos1) || columnNotValid(kak,i,j+3,pos1))) kak.setCellValue(i,j+3,pos1);	//1 valido (3,4,2,1)
									}
								}
								else if(j+2<width){	//1 valido (3,4,1)
									kak.setCellValue(i,j+2,pos1);
									if(j+3<width && !(rowNotValid(kak,i,j+3,pos2) || columnNotValid(kak,i,j+2,pos2))) kak.setCellValue(i,j+3,pos2);	//2 valido (3,4,1,2)
								}
							}
						}
						else{	//2 valido (3,2)
							kak.setCellValue(i,j+1,pos2);
							if(j+2<width && (rowNotValid(kak,i,j+2,pos1) || columnNotValid(kak,i,j+2,pos1))){	//1 no valido
								if(!(rowNotValid(kak,i,j+2,pos4) || columnNotValid(kak,i,j+2,pos4))){	//4 valido (3,2,4)
									kak.setCellValue(i,j+2,pos4);
									if(j+3<width && !(rowNotValid(kak,i,j+3,pos1) || columnNotValid(kak,i,j+3,pos1))) kak.setCellValue(i,j+3,pos1);	//1 valido (3,2,4,1)
								}
							}
							else if(j+2<width){	//1 valido (3,2,1)
								kak.setCellValue(i,j+2,pos1);
								if(j+3<width && !(rowNotValid(kak,i,j+3,pos4) || columnNotValid(kak,i,j+3,pos4))) kak.setCellValue(i,j+3,pos4);	//4 valido (3,2,1,4)
							}
						}
					}
					else if(j+1<width){	//1 valido (3,1)
						kak.setCellValue(i,j+1,pos1);
						if(j+2<width && !(rowNotValid(kak,i,j+2,pos2) || columnNotValid(kak,i,j+2,pos2))){	//2 no valido
							if(!(rowNotValid(kak,i,j+2,pos4) || columnNotValid(kak,i,j+2,pos4))){	//4 valido (3,1,4)
								kak.setCellValue(i,j+2,pos4);
								if(j+3<width && !(rowNotValid(kak,i,j+3,pos2) || columnNotValid(kak,i,j+3,pos2))) kak.setCellValue(i,j+3,pos2);	//2 valido (3,1,4,2)
							}
						}
						else if(j+2<width){	//2 valido (3,1,2)
							kak.setCellValue(i,j+2,pos2);
							if(j+3<width && !(rowNotValid(kak,i,j+2,pos4) || columnNotValid(kak,i,j+2,pos4))) kak.setCellValue(i,j+3,pos4);	//4 valido (3,1,2,4)
						}
					}
				}
			}
			else{	//2 valido (2)
				kak.setCellValue(i,j,pos2);
				if(j+1<width && (rowNotValid(kak,i,j+1,pos1) || columnNotValid(kak,i,j+1,pos1))){	//1 no valido
					if(rowNotValid(kak,i,j+1,pos3) || columnNotValid(kak,i,j+1,pos3)){	//3 no valido
						if(!(rowNotValid(kak,i,j+1,pos4) || columnNotValid(kak,i,j+1,pos4))){	//4 valido (2,4)
							kak.setCellValue(i,j+1,pos4);
							if(j+2<width && (rowNotValid(kak,i,j+2,pos1) || columnNotValid(kak,i,j+2,pos1))){	//1 no valido
								if(!(rowNotValid(kak,i,j+2,pos3) || columnNotValid(kak,i,j+2,pos3))){	//3 valido (2,4,3)
									kak.setCellValue(i,j+2,pos3);
									if(j+3<width && !(rowNotValid(kak,i,j+3,pos1) || columnNotValid(kak,i,j+3,pos1))) kak.setCellValue(i,j+3,pos1);	//1 valido (2,4,3,1)
								}
							}
							else if(j+2<width){	//1 valido (2,4,1)
								kak.setCellValue(i,j+2,pos1);
								if(!(rowNotValid(kak,i,j+1,pos1) || columnNotValid(kak,i,j+1,pos1))) kak.setCellValue(i, j+1, pos3);	//3 valido (2,4,1,3)
							}
						}
					}
				}
				else if(j+1<width){	//1 valido (2,1)
					kak.setCellValue(i,j+1,pos1);
					if(j+2<width && (rowNotValid(kak,i,j+2,pos3) || columnNotValid(kak,i,j+2,pos3))){	//3 no valido
						if(!(rowNotValid(kak,i,j+2,pos4) || columnNotValid(kak,i,j+2,pos4))){	//4 valido (2,1,4)
							kak.setCellValue(i,j+2,pos4);
							if(j+3<width && !(rowNotValid(kak,i,j+3,pos3) || columnNotValid(kak,i,j+3,pos3))) kak.setCellValue(i,j+3,pos3);	//3 valido (2,1,4,3)
						}
					}
					else if(j+2<width){	//3 valido (2,1,3)
						kak.setCellValue(i,j+2,pos3);
						if(j+3<width && !(rowNotValid(kak,i,j+3,pos4) || columnNotValid(kak,i,j+3,pos4))) kak.setCellValue(i,j+3,pos4);	//4 valido (2,1,3,4)
					}
				}
			}
		}
		else{	//1 valido (1)
			kak.setCellValue(i,j,pos1);
			if(j+1<width && (rowNotValid(kak, i,j+1,pos2) || columnNotValid(kak, i,j+1,pos2))){	//2 no valido
				if(rowNotValid(kak, i,j+1,pos3) || columnNotValid(kak, i,j+1,pos3)){	//3 no valido
					if(!(rowNotValid(kak, i,j+1,pos4) || columnNotValid(kak, i,j+1,pos4))){	//4 valido (1,4)
						kak.setCellValue(i,j+1,pos4);
						if(j+2<width && (rowNotValid(kak, i,j+2,pos2) || columnNotValid(kak, i,j+2,pos2))){	//2 no valido
							if(!(rowNotValid(kak, i,j+2,pos3) || columnNotValid(kak, i,j+2,pos3))){	//3 valido (1,4,3)
								kak.setCellValue(i,j+2,pos3);
								if(j+3<width && !(rowNotValid(kak, i,j+3,pos2) || columnNotValid(kak, i,j+3,pos2))) kak.setCellValue(i,j+3,pos2);	//2 valido (1,4,3,2)
							}
						}
						else if(j+2<width){	//2 valido (1,4,2)
							kak.setCellValue(i,j+2,pos2);
							if(j+3<width && !(rowNotValid(kak, i,j+3,pos3) || columnNotValid(kak, i,j+3,pos3))) kak.setCellValue(i,j+3,pos3);	//3 valido (1,4,2,3)
						}
					}
				}
				else{	//3 valido (1,3)
					kak.setCellValue(i,j+1,pos3);
					if(j+2<width && (rowNotValid(kak, i,j+2,pos2) || columnNotValid(kak, i,j+2,pos2))){	//2 no valido
						if(rowNotValid(kak, i,j+2,pos4) || columnNotValid(kak, i,j+2,pos4)){	//4 valido (1,3,4)
							kak.setCellValue(i,j+2,pos4);
							if(j+3<width && !(rowNotValid(kak, i,j+3,pos2) || columnNotValid(kak, i,j+3,pos2))) kak.setCellValue(i,j+3,pos2);	//2 valido (1,3,4,2)
						}
					}
					else if(j+2<width){	//2 valido	(1,3,2)
						kak.setCellValue(i,j+2,pos2);
						if(j+3<width && !(rowNotValid(kak, i,j+3,pos4) || columnNotValid(kak, i,j+3,pos4))) kak.setCellValue(i,j+3,pos4);	//4 valido (1,3,2,4)
					}
				}
			}
			else if(j+1<width){	//2 valido (1,2)
				kak.setCellValue(i,j+1,pos2);
				if(j+2<width && (rowNotValid(kak, i,j+2,pos3) || columnNotValid(kak, i,j+2,pos3))){	//3 no valido
					if(rowNotValid(kak, i,j+2,pos4) || columnNotValid(kak, i,j+2,pos4)){	//4 valido (1,2,4)
						kak.setCellValue(i,j+2,pos4);
						if(i+3<width && !(rowNotValid(kak, i,j+3,pos3) || columnNotValid(kak, i,j+3,pos3))) kak.setCellValue(i,j+3,pos3);	//3 valido (1,2,4,3)
					}
				}
				else if(j+2<width){	//3 valido (1,2,3)
					kak.setCellValue(i,j+2,pos3);
					if(i+3<width && !(rowNotValid(kak, i,j+3,pos4) || columnNotValid(kak, i,j+3,pos4))) kak.setCellValue(i,j+3,pos4);	//4 valido (1,2,3,4)
				}
			}
		}
	}

	
	//RELLENAR SUMAS -> una vez repartidas las celdas negras y los valores de las sumas, asignamos las sumas y vaciamos las casillas blancas 
	
	//deja como Black esas casillas EmptyBlack que son resultado de una suma (tienen valores validos a la derecha y/o abajo) y vacia las casillas solved y las vuelve a dejar como white
	private static void setSumas(Kakuro kak){
		//recorremos la matriz calculando las sumas
		for(int i = 0; i<kak.getHeight(); ++i) {
			for(int j = 0; j<kak.getWidth();++j) {
				//si la casilla es efectivamente negra vacia (cogemos las sumas que puede contener sumas)
				if(kak.getCellColor(i, j) == "EmptyBlack") {
					//ahora mismo estas funciones devuelven -1*Num de casillas blancas en vez de 
					int colValue = setSumaVer(i,j,kak);
					int rowValue = setSumaHor(i,j,kak);
					//comprobar si alguna de las dos no es valida y convertir en codigo de celda
					String code ="";
					if(colValue != 0) {
						//si es diferente al valor por defecto anadimos el valor columna
						code+="C";
						code+=colValue;
					}
					if(rowValue != 0) {
						//si es diferente al valor por defecto anadimos el valor columna
						code+="F";
						code+=rowValue;
					}
					//si no se ha modificado se deja como EmptyBlack, sino se cambia a Black y se asigna la suma vertical y horizontal
					if(code != "") {
						//modificamos solo en caso de que efectivamente esa posicion deje de ser EmptyBlack para ser solo Black
						Cell aux = new Cell(i,j,code);
						kak.setCell(i, j, aux);		//al dejarlo como nueva celda se asigna automaticamente el color (black) y la suma horizontal y vertical
					}
				}
				//si la casilla tenia un numero (Solved) la vaciamos -> pasamos de Solved a White
				else if(kak.getCellColor(i, j) == "Solved") {
					kak.deleteCellValue(i, j);
				}
			}
		}
	}
	
	//devuelve la suma horizontal desde board[i][j+1] hacia la derecha (board[i][j] es emptyBlack)
	private static int setSumaHor(int i, int j, Kakuro kak) {
		boolean stop = false;
		int sumaAux = 0;
		//de j+1 a getWidth() -> hacia la derecha
		for(int m = j+1; m<kak.getWidth() && !stop; ++m) {
			//si es solved (con numero) cogemos el valor
			if(kak.getCellColor(i, m) == "Solved") {
				sumaAux += kak.getCellValue(i, m);
			}
			//si ya no hay mas casillas solved (consecutivas) paramos
			else stop = true;
		}
		for(int m = j+1; m<kak.getWidth() && kak.getCellColor(i, m) == "Solved"; ++m) {
			kak.setCellHorizontalSum(i, m, sumaAux);
		}
		return sumaAux;
	}
	
	//devuelve la suma vertical desde board[i+][j] hacia abajo (board[i][j] es emptyBlack)
	private static int setSumaVer(int i, int j, Kakuro kak) {
		boolean stop = false;
		int sumaAux = 0;
		//de i+1 a getHeight() -> hacia abajo
		for(int m = i+1; m<kak.getHeight() && !stop; ++m) {
			//si es solved (con numero) cogemos el valor
			if(kak.getCellColor(m, j) == "Solved") {
				sumaAux += kak.getCellValue(m, j);
			}
			//si ya no hay mas casillas solved (consecutivas) paramos
			else stop = true;
		}
		for(int m = i+1; m<kak.getHeight() && kak.getCellColor(m, j) == "Solved"; ++m) {
			kak.setCellVerticalSum(m, j, sumaAux);
		}
		return sumaAux;
	}
	
	
	
	//VACIADO DEL KAKURO -> limpia todas las casillas con valores de suma y deja las negras con valores de fila/columna como negras vacias (distribucion inicial)
	
	//vacia el kakuro y lo deja con las casillas negras originales (tras salir de distributeCells(kak))
	private static void emptyBoard(Kakuro kak) {
		for(int i = 0; i<kak.getHeight(); ++i) {
			for( int j = 0; j<kak.getWidth(); ++j) {
				//si la celda es del tipo black la dejamos como EmptyBlack
				if(kak.getCellColor(i, j) == "Black") {
					kak.setCellColor(i,j,"EmptyBlack");
				}
				//si la celda es del tipo EmptyBlack o White no hacemos nada
				//si la celda es del tipo Solved le dejamos el valor por defecto
				else if(kak.getCellColor(i, j) == "Solved") {
					kak.setCellColor(i, j, "White");
				}
			}
		}
	}
	
	
	//COMPROBACION DE SOLUCION UNICA -> comprobamos que el kakuro generado es de solucion unica
	
	//Comprueba que el kakuro generado (con blancas y negras) tiene solucion unica
	private static void checkUnique(Kakuro kak, int black, int white){

		int howMany = Solver.solveKakuroUnique(kak);
		//contador para evitar demasiadas iteraciones con una misma distribucion
		int contAux = 15;
		//si encuentra al menos una solucion: comprobamos si es multiple y la volvemos a generar (al volver a generarla podria dar 0 de nuevo)
		while(contAux>0 &&(howMany!=1)){
			--contAux;
			if(contAux==0) {
				contAux = 0;
				//colocamos las casillas negras
				distributeCells(kak, black, white);
				
				//rellenamos con numeros random las casillas blancas
				fillWhites(kak);
				
				//establecemos las sumas y dejamos las casillas blancas vacias (valor por defecto -1)
				setSumas(kak);

				checkUnique(kak,black,white);
				//saldria de aqui cuando tenga solucion unica (howmany = 1)
				break;
			}
			else {
				emptyBoard(kak);
				//rellenamos con numeros random las casillas blancas
				fillWhites(kak);
				
				//establecemos las sumas y dejamos las casillas blancas vacias (valor por defecto -1)
				setSumas(kak);
				
				//para usar el nuevo
				howMany = Solver.solveKakuroUnique(kak);
			}
		}
		//salimos del if-else cuando hay una unica solucion
	}
	
	//Comprueba que el kakuro generado tiene solucion unica
	private static void checkUnique(Kakuro kak){

		int howMany = Solver.solveKakuroUnique(kak);
		//contador para evitar demasiadas iteraciones con una misma distribucion
		int contAux = 15;
		//si encuentra al menos una solucion: comprobamos si es multiple y la volvemos a generar (al volver a generarla podria dar 0 de nuevo)
		while(contAux>0 &&(howMany!=1)){
			--contAux;
			if(contAux==0) {
				contAux = 0;
				//colocamos las casillas negras
				distributeCells(kak);
				
				//rellenamos con numeros random las casillas blancas
				fillWhites(kak);
				
				//establecemos las sumas y dejamos las casillas blancas vacias (valor por defecto -1)
				setSumas(kak);

				checkUnique(kak);
				//saldria de aqui cuando tenga solucion unica (howmany = 1)
				break;
			}
			else {
				emptyBoard(kak);
				
				//rellenamos con numeros random las casillas blancas
				fillWhites(kak);
				
				//establecemos las sumas y dejamos las casillas blancas vacias (valor por defecto -1)
				setSumas(kak);
				
				howMany = Solver.solveKakuroUnique(kak);
			}
		}
		//salimos del if-else cuando hay una unica solucion
	}
	
	
	//COMPROBACIONES EXTRA: comprobaciones necesarias para la asignacion de celdas blancas y negras
	
	//compureba que se pueda poner una celda negra (horizontal)
	private static boolean checkBlackHor(Kakuro k, int i, int j) { //solo llamar con height impar
		//comprobamos que podemos poner una negra
		if (i == k.getHeight()/2) {
			if (k.getCellColor(i, k.getWidth() - j) == "White") return false;
		}
		return true;
	}
	
	//comprueba que se pueda poner una celda negra (vertical)
	private static boolean checkBlackVer(Kakuro k, int i, int j) {
		if (j == k.getWidth()/2) {
			if (k.getCellColor(k.getHeight() - i, j) == "White") return false;
		}
		return true;
	}
	
	//comprueba que se pueda poner una celda blanca (horizontal)
	private static boolean checkWhiteHor(Kakuro k, int i, int j) { //solo llamar con height impar
		//comprobamos que podemos poner una negra
		if (i == k.getHeight()/2) {
			if (k.getCellColor(i, k.getWidth() - j) == "EmptyBlack") return false;
		}
		if (k.getHeight() > 10) {
			if (i == k.getHeight()/2) {
				int cont = 0;
				for (int auxI = 1; auxI < 5; ++auxI) {
					if (k.getCellColor(i-auxI, j) == "White") ++cont;
				}
				if (cont == 4) return false;
			}
		}
		return true;
	}
	
	//comprueba que se pueda poner una celda blanca (vertical)
	private static boolean checkWhiteVer(Kakuro k, int i, int j) {
		if (j == k.getWidth()/2) {
			if (k.getCellColor(k.getHeight() - i, j) == "EmptyBlack") return false;
		}
		if (k.getWidth() > 10) {
			if (j == k.getWidth()/2) {
				int cont = 0;
				for (int auxJ = 1; auxJ < 5; ++auxJ) {
					if (k.getCellColor(i, j-auxJ) == "White") ++cont;
				}
				if (cont == 4) return false;
			}
		}
		return true;
	}
	
}