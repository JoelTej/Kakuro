package domainLayer;

import java.util.*;

public class Solver {
	
	// ATRIBUTES //
		
	private static Kakuro Solution;
	private static int[][] candidates;
	private static int[][][] candidatesNew;
	private static ArrayList<Set<Integer>> res= new ArrayList<Set<Integer>>(512);
	
	private static int numOfSolutions;
	
	private static boolean[][] usedHints;
	
	
	// PUBLIC METHODS //
	
	// Calcula totes les combinacions amb qualsevol suma i nombre de caselles
	// I es guarden a les esctructures de "candidates" i "candidatesNew"
	public static void initializeCandidates() {
        
    	combination(9);
        candidates = new int[10][46];
        candidatesNew = new int[10][46][512];
        
        int maxsum, minsum;
        
        for (int nocells = 1; nocells <= 9; nocells++) {

        	if (nocells == 1) maxsum = 9;
        	else if (nocells == 2) maxsum = 17;
            else if (nocells == 3) maxsum = 24;
            else if (nocells == 4) maxsum = 30;
            else if (nocells == 5) maxsum = 35;
            else if (nocells == 6) maxsum = 39;
            else if (nocells == 7) maxsum = 42;
            else if (nocells == 8) maxsum = 44;
            else maxsum = 45;

        	if (nocells == 1) minsum = 1;
        	else if (nocells == 2) minsum = 3;
            else if (nocells == 3) minsum = 6;
            else if (nocells == 4) minsum = 10;
            else if (nocells == 5) minsum = 15;
            else if (nocells == 6) minsum = 21;
            else if (nocells == 7) minsum = 28;
            else if (nocells == 8) minsum = 36;
            else minsum = 45;

            for (int sum = minsum; sum <= maxsum; sum++) {
                int[] cand = new int[1];
                boolean[] list = new boolean[10];
                getCandidates(cand, list, sum, nocells, 0, 0, 0);

                candidates[nocells][sum] = cand[0];
                candidatesNew[nocells][sum][0] = cand[0];
            }
            
            for (int sum = minsum; sum <= maxsum; sum++) {
            	for (int nums = 1; nums < nocells; ++nums) {
            		int auxsum = sum;
            		int auxnocells = nocells - nums;
            		int key = 0;
            		boolean[] list = new boolean[10];
                    for(int w=0;w<Math.pow(2,9);w++){ 	//
                    	if (res.get(w).size() == nums) {
                    		boolean canDoIt = true;
	                    	for (Integer i : res.get(w)) {
	                    		if (((candidates[nocells][sum] >> (i-1)) & 1) == 1 && canDoIt) {
		            				list[i] = true;
		            				key |= 1 << (i - 1);
		            				auxsum -= i;
		            			}
		            			else canDoIt = false;
		            		}

	                    	if (canDoIt) {
			            		int[] cand = new int[1];
			            		getCandidates(cand, list, auxsum, auxnocells, 0, 0, 0);
			            		if (key != 0) candidatesNew[nocells][sum][key] = cand[0];
	                    	}
	                    	
		            		for (Integer i : res.get(w)) {
		            			key &= ~(1 << (i - 1));		
		            			list[i] = false;
		            			auxsum = sum;
		            		}
                    	}
                    }
            	}
            }
        }
    }

	// Funcio principal per resoldre el Kakuro K passat com
	// a parametre
	public static boolean solveKakuro(Kakuro K) {
    	
    	Stack<int[]> s = new Stack<int[]>();
    	int[][] usedH = new int[K.getHeight()][K.getWidth()];
    	int[][] usedV = new int[K.getHeight()][K.getWidth()];
    	PairInt[][] reffs = new PairInt[K.getHeight()][K.getWidth()];
    	int[][] partialSol = new int[K.getHeight()][K.getWidth()];
    	initializeSolMatrix(K, partialSol, reffs, usedH, usedV);
    	
    	return initialSolver(K, partialSol, 1, 1, reffs, usedH, usedV, s, false);
    }
	
	// Retorna si un Kakuro K no te solucio, o te solucio unica
	// o en te mes de 2
	public static int solveKakuroUnique(Kakuro K) {
		
        numOfSolutions = 0;
        
        Stack<int[]> s = new Stack<int[]>();
    	int[][] usedH = new int[K.getHeight()][K.getWidth()];
    	int[][] usedV = new int[K.getHeight()][K.getWidth()];
    	PairInt[][] reffs = new PairInt[K.getHeight()][K.getWidth()];
    	int[][] partialSol = new int[K.getHeight()][K.getWidth()];
    	initializeSolMatrix(K, partialSol, reffs, usedH, usedV);    	
    	
    	initialSolver(K, partialSol, 1, 1, reffs, usedH, usedV, s, true);
        
        int nOS = numOfSolutions;
        numOfSolutions = 0;
        return nOS;
	}
	
	// Aquesta funcio es crida quan es vol comprobar
	// si els nombre de les caselles estan posades be
    public static boolean checkSolution(Kakuro k) {
		//recorremos la tabla: si alguna celda es white, la suma no coincide con la que hay que resolver o hay numeros repetidos para una suma devolvemos false
		//si por lo contrario todas las celdas estan llenas con numeros no repetidos que cumplen la suma devolvemos true
		boolean[] valuesUsed;
		int sumToSolve, sumTemp, totalCells, cellUsed;
		
		//primer for: comprobamos las sumas horizontales
		for(int i = 0; i<k.getHeight();++i) {
			valuesUsed = new boolean[9];
			sumToSolve = sumTemp = totalCells = cellUsed = 0;
			for(int j = 0; j<k.getWidth(); ++j) {
				String color = k.getCellColor(i, j);
				if(color == "White") return false;	//si es blanca aun no esta completo del todo -> devuelve falso
				if(color == "Black") {	//si es negra: si tiene suma horizontal asignamos los valores de suma a resolver, celdas disponibles y comprobamos que no haya quedado una suma no valida
					//si las sumas (anteriores) no coinciden o el numero de celdas usadas: return false
					if(sumTemp!=sumToSolve || totalCells!=cellUsed) return false;
					if(k.getCellHorizontalSum(i, j)>0) {	//si tiene suma horizontal: por definicion la casilla a la derecha es valida
						sumToSolve = k.getCellHorizontalSum(i,j);
						sumTemp = 0;
						totalCells = k.getNumWhitesHor(i, j+1);
						cellUsed = 0;
						valuesUsed = new boolean[9];	//limpiamos el vector de numeros usados
					}
				}
				else if(color == "Solved") {	//si tiene valor
					int valueAux = k.getCellValue(i, j);
					sumTemp+= valueAux;
					++cellUsed;
					//si la suma temporal es mayor a la que hay que resolver O es mayor o igual (util en caso igual) Y el numero de celdas usadas es menor que el de casillas, devuelve falso
					if(sumTemp>sumToSolve || sumTemp>=sumToSolve && totalCells>cellUsed) return false;
					//si ya hemos gastado todas las celdas y la suma no coincide devuelve falso
					if(cellUsed == totalCells && sumTemp!=sumToSolve) return false;
					//si el valor ya se ha usado devuelve falso
					if(valuesUsed[valueAux-1]) return false;
					//si nada de eso se cumple, marcamos ese valor como usado y seguimos comprobando
					valuesUsed[valueAux-1] = true;
				}
				else if(color == "EmptyBlack") {	//si es empty black: si ha quedado una suma no valida devolvemos falso, sino dejamos contadores a 0
					if(sumTemp!=sumToSolve || totalCells!=cellUsed) return false;
					valuesUsed = new boolean[9];
					sumToSolve = sumTemp = totalCells = cellUsed = 0;
				}
			}
		}
		//segundo for: comprobamos las sumas verticales
		for(int j = 0; j<k.getWidth();++j) {
			valuesUsed = new boolean[9];
			sumToSolve = sumTemp = totalCells = cellUsed = 0;
			for(int i = 0; i<k.getHeight(); ++i) {
				String color = k.getCellColor(i, j);
				if(color == "White") return false;	//si es blanca aun no esta completo del todo -> devuelve falso
				if(color == "Black") {	//si es negra: si tiene suma horizontal asignamos los valores de suma a resolver, celdas disponibles y comprobamos que no haya quedado una suma no valida
				//si las sumas (anteriores) no coinciden o el numero de celdas usadas: return false
					if(sumTemp!=sumToSolve || totalCells!=cellUsed) return false;
					if(k.getCellVerticalSum(i, j)>0) {	//si tiene suma horizontal: por definicion la casilla a la derecha es valida
						sumToSolve = k.getCellVerticalSum(i,j);
						sumTemp = 0;
						totalCells = k.getNumWhitesVert(i+1, j);
						cellUsed = 0;
						valuesUsed = new boolean[9];	//limpiamos el vector de numeros usados
					}
				}
				else if(color == "Solved") {	//si tiene valor
					int valueAux = k.getCellValue(i, j);
					sumTemp+= valueAux;
					++cellUsed;
					//si la suma temporal es mayor a la que hay que resolver O es mayor o igual (util en caso igual) Y el numero de celdas usadas es menor que el de casillas, devuelve falso
					if(sumTemp>sumToSolve || sumTemp>=sumToSolve && totalCells>cellUsed) return false;
					//si ya hemos gastado todas las celdas y la suma no coincide devuelve falso
					if(cellUsed == totalCells && sumTemp!=sumToSolve) return false;
					//si el valor ya se ha usado devuelve falso
					if(valuesUsed[valueAux-1]) return false;
					//si nada de eso se cumple, marcamos ese valor como usado y seguimos comprobando
					valuesUsed[valueAux-1] = true;
				}
				else if(color == "EmptyBlack") {	//si es empty black: si ha quedado una suma no valida devolvemos falso, sino dejamos contadores a 0
					if(sumTemp!=sumToSolve || totalCells!=cellUsed) return false;
					valuesUsed = new boolean[9];
					sumToSolve = sumTemp = totalCells = cellUsed = 0;
				}
			}
		}
		//si no se encuentra ningun fallo devuelve true
		return true;
	}
	
	// Posa una pista (un numero de la solcuio) en 
	// una de les caselles blanques de forma aleatoria
    public static boolean initialHints(Kakuro k) {
		
		Random r = new Random();
		
		int i = r.nextInt((k.getHeight()-2)+1)+1;
		int j = r.nextInt((k.getWidth()-2)+1)+1;
		while(usedHints[i][j]) {
			i = r.nextInt((k.getHeight()-2)+1)+1;
			j = r.nextInt((k.getWidth()-2)+1)+1;
		}
		usedHints[i][j] = true;
		int value = Solution.getCellValue(i, j);
		k.setCellValue(i, j, value);
		return true;
	}

	// Retorna un vector de mida 3, els dos primers valors es la posicio
	// i l'ultim valor es el valor de la casella
    public static int[] getHint(Kakuro K) {						//devolver posiciones+valor
		
		Stack<int[]> s = new Stack<int[]>();
    	int[][] usedH = new int[K.getHeight()][K.getWidth()];
    	int[][] usedV = new int[K.getHeight()][K.getWidth()];
    	PairInt[][] reffs = new PairInt[K.getHeight()][K.getWidth()];
    	int[][] partialSol = new int[K.getHeight()][K.getWidth()];
    	initializeSolMatrix(K, partialSol, reffs, usedH, usedV);
    	
		int[] result = new int[3];								//devolver posiciones+valor
		if (!initialSolver(K, partialSol, 1, 1, reffs, usedH, usedV, s, false)) return result;	//devolver posiciones+valor
		else {
			boolean done = false;
			while (!done) {
				int m = Generator.getRandomNumber(1, K.getHeight()-1);
				int n = Generator.getRandomNumber(1, K.getWidth()-1);
				if (K.getCellColor(m, n) == "White" && partialSol[m][n]!=0) {
					done = true;
					int value = partialSol[m][n];
					result[0] = m;								//devolver posiciones+valor
					result[1] = n;								//devolver posiciones+valor
					result[2] = value;							//devolver posiciones+valor
					K.setCellValue(m, n, value);
				}
			}
			return result;										//devolver posiciones+valor
		}
	}
	
    // Retorna la solucio de tipus Kakuro
    public static Kakuro getSolution() {
		return Solution;
	}

	// Neteja les caselles solucionades
	public static void cleanSolved(Kakuro k) {
		for (int i = 1; i < k.getHeight(); ++i) {
			for (int j = 1; j < k.getWidth(); ++j) {
				if (k.getCellColor(i, j) == "Solved") {
					k.setCellColor(i, j, "White");
				}
			}
		}
	}
	
    
	// PRIVATE METHODS //
	
    private static void combination(int num){
    	
		for(int i=0;i<Math.pow(2,num);i++){
			int vector[]=new int[num];
			int temp=i;
			
			for(int l=0;l<vector.length;l++){
				vector[l]=temp%2;
				temp/=2;
			}
			
			Set<Integer> a = new HashSet<Integer>();
			
			for(int j=0;j<vector.length;j++){
				if(vector[j]==1){
					a.add(j+1);
				}
			}
			res.add(i, a);
		}
	}
    
	// backtracking que guarda en cand els numeros candidats
	// list es un vector auxiliar, sumH es la suma que es vol aconseguir
	// maxdigit son el nombre de numeros per completar la suma,
	// total es la suma acumulada actual, d es el nombre de numeros utilitzats per tenir "total"
	// i num es el ultim numero utilitzat
    private static void getCandidates(int[] cand, boolean[] list, int sumH, int maxdigit, int total, int num, int d)  {
        
        if (total > sumH) return;
        
        if (d == maxdigit) {
            if (total < sumH) return;
            
            if (total == sumH) {
            	cand[0] |= 1 << (num - 1);
                return;
            }
        }
  	
        if (total == sumH && d != maxdigit) return;
        
        for (int k = 1; k <= 9; k++) {
            if (maxdigit == 1 && !list[k]) {
                list[k] = true;
                getCandidates(cand, list, sumH, maxdigit, total+k, k, d+1);
                list[k] = false;
            }
            else if (k != sumH && !list[k]) {
                list[k] = true;
                getCandidates(cand, list, sumH, maxdigit, total+k, k, d+1);
                list[k] = false;
            }
        }
    }
	//total con los valores ya sumados
	//maxdigit con la cantidad de valores ya restada

	// inicialitza la matriu d'enters partialSol amb la que es treballara
	// reffs es una matriu on a cada posició es guarda un pairint 
	// amb first() el valor de la fila de la casella que indica la suma en vertical
	// y al second() es guarda la columna de la casella que indica la suma en horitzonal
	// usedH i usedV son matrius on haura valor nomes a les caselles negres
	// cada valor guardats com un int on cada bit a 1 representa que 
	// s'ha utilitzat el numero de la posicio del bit a 1
	private static void initializeSolMatrix(Kakuro K, int[][] partialSol, PairInt[][] reffs, int[][] usedH, int[][] usedV) {
    	
    	for (int i = 0; i < K.getHeight(); i++) {
			for (int j = 0; j < K.getWidth(); j++) {
				String col = K.getCellColor(i, j);
				
				switch (col) {
				case "White": // quan es una casella blanca sense numero
					partialSol[i][j] = 0;
					
					PairInt H = new PairInt(0, 0);
					H.first(reffs[i-1][j].first());
					H.second(reffs[i][j-1].second());
					reffs[i][j] = H;
					break;	
					
				case "Solved": // quan hi ha un numero en una casella blanca
					partialSol[i][j] = K.getCellValue(i,j);
					
					PairInt F = new PairInt(0, 0);
					F.first(reffs[i-1][j].first());
					F.second(reffs[i][j-1].second());
					reffs[i][j] = F;

					usedH[i][reffs[i][j].second()] |= 1 << (partialSol[i][j]-1);
					usedV[reffs[i][j].first()][j]|= 1 << (partialSol[i][j]-1);
					break;	
					
				case "Black": // quan es una casella negra amb sumes H i/o V
					partialSol[i][j] = -1;
					
					PairInt L = new PairInt(0, 0);
					L.first(i);
					L.second(j);
					reffs[i][j] = L;
					break;	
					
				case "EmptyBlack": // quan es tracta d'una casella negra sense sumes
					partialSol[i][j] = -1;		
					break;					
				}
			}
    	}
    }
    
	// Aquesta funcio fa una passada sequencial, 
	// a les caselles on hi ha un unic candidat 
	// les omple, i seguidament crida al bactracking 
	// es va solucionant sobre partialSol,
	// b y c es las posicio de la matriu d'on es cridara
	// per comencar el backtracing
	// reffs matriu de referencies (o punters) a les caselles on indiquen 
	// la suma en horitzontal i/o vertical
	// usedH i usedV son matrius de enters, cada bit del enter
	// representa el numero de la seva posico (1 si s'ha utilitzat, 0 altrament)
	// stack son les posicions de les caselles en les que la passada sequencial
	// ha trobat un unica candidat i les ha omplert (l'utilitzara el backtracking)
	// unique indica si volem saber si te solucio unica o no
    private static boolean initialSolver(Kakuro k, int[][] partialSol, int b, int c, PairInt[][] reffs, int[][] usedH, int[][] usedV, Stack<int[]> s, boolean unique) {
    	
		for (int i = b; i < k.getHeight(); ++i) {
			for (int j = 1; j < k.getWidth(); ++j) {
				if (i == b && j == 1) j = c;
				if (partialSol[i][j] == 0) {	

			        // Candidates for this white cells "row"
			        int sumH = k.getCellHorizontalSum(i, j);
			        int digitsH = k.getNumWhitesHor(i, j);
			        int keyH = usedH[i][reffs[i][j].second()];
			        int candidatesH = candidatesNew[digitsH][sumH][keyH];
			      
			        // Candidates for this white cells "column"
			        int sumV = k.getCellVerticalSum(i, j);
			        int digitsV = k.getNumWhitesVert(i, j);
			        int keyV = usedV[reffs[i][j].first()][j];
			        int candidatesV = candidatesNew[digitsV][sumV][keyV];
					int cand = candidatesH & candidatesV;
					
					if (Integer.bitCount(cand) == 0) return false; // si no te cap candidat retorna, perque no es possible
					if (Integer.bitCount(cand) == 1) { // compta els bits dels flags a 1, si nomes hi ha un vol dir que nomes te un candidat aquesta casella i j
						int numb = Integer.numberOfTrailingZeros(cand) + 1;
						partialSol[i][j] = numb;
						usedH[i][reffs[i][j].second()] |= 1 << (numb-1);
		                usedV[reffs[i][j].first()][j]|= 1 << (numb-1);
		                
		                int[] trip = new int[3];
						trip[0] = i;
						trip[1] = j;
						trip[2] = numb;
						s.push(trip);
					}
				}
			}
		}
		if (unique == true) return solveKakuroUnique(k,partialSol,b,c,reffs,usedH,usedV, s); // crida al solver que indicara quantes solucions te
    	return solveKakuro(k,partialSol,b,c,reffs,usedH,usedV, s); // es crida al backtracking
	}
    
	// Aquesta es la funcio principal que resol un kakuro donat
	// fa backtracking mirant els candidats en horitzontal i vertical
	// que hi ha a cada casella. En el for del backtracking nomes provara
	// els numeros que siguin candidats de la interseccio de candidats horizontals
	// i verticals. Si es un candidat de la intersecció proba amb aquest i crida
	// a solver sequencial (que mira de posar valors on només pot anar un unic numero)
	private static boolean solveKakuro(Kakuro K, int[][] partialSol, int i, int j, PairInt[][] reffs, int[][] usedH, int[][] usedV, Stack<int[]> s) {
        
		// Solved all the rows		
        if (i == partialSol.length){
        	saveSolution(K, partialSol);
        	return true;
        }
        
        // Solve the next row
        if (j == partialSol[0].length) {
        	return solveKakuro(K, partialSol, i+1, 0, reffs, usedH, usedV, s);
        }
        
        // Solve the next Cell
        if (partialSol[i][j] != 0) { 
            return solveKakuro(K, partialSol, i, j+1, reffs, usedH, usedV, s);
        }
        
        // Candidates for this white cells "row"
        int sumH = K.getCellHorizontalSum(i, j);
        int digitsH = K.getNumWhitesHor(i, j);
        int keyH = usedH[i][reffs[i][j].second()];
        int candidatesH = candidatesNew[digitsH][sumH][keyH];
      
        // Candidates for this white cells "column"
        int sumV = K.getCellVerticalSum(i, j);
        int digitsV = K.getNumWhitesVert(i, j);
        int keyV = usedV[reffs[i][j].first()][j];
        int candidatesV = candidatesNew[digitsV][sumV][keyV];
        
        int cand = candidatesH & candidatesV; // enter dels candidats de la interseccio, cada bit a 1 vol dir que el numero de la seva posicio es candidat
        
        for (int num = 1; num <= 9; num++) {
            if (((cand >> (num-1)) & 1) == 1) { // comprovem que el bit del numero de l'enter estigui activat (a 1)

                partialSol[i][j] = num; // provem aquest numero ya que es un candidat
                usedH[i][reffs[i][j].second()] |= 1 << (num-1); // posem el bit a 1 (s'ha utilitzat) de la seva casella referencia horitzontal
                usedV[reffs[i][j].first()][j] |= 1 << (num-1); // posem el bit a 1 (s'ha utilitzat) de la seva casella referencia vertical
                
                int prev = s.size();
                
                if (initialSolver(K, partialSol, i, j+1, reffs, usedH, usedV, s, false)) return true; // intem solucionarho amb el nou numero posat
				
				
                int curr = s.size() - prev;
				
				// si el backtracking ha arribat a aqui vol dir 
				// que no ha pogut solucionar-ho amb aquest numero,
				// per tant, com venim initialsolver, la passada 
				// sequencial haura modificat unes quantes posicions
				// a la pila s tenim guardat aquestes. Llavors per
				// recuperar l'estat inicial de la matriu desempilant
				// i restaurant els valors que hi havia abans
                while (curr > 0) { 
                	int[] trip = s.pop();
                	usedH[trip[0]][reffs[trip[0]][trip[1]].second()] &= ~(1 << (trip[2] - 1));
                    usedV[reffs[trip[0]][trip[1]].first()][trip[1]] &= ~(1 << (trip[2] - 1));
                    partialSol[trip[0]][trip[1]] = 0;
                    --curr;
                }
				
				// desfem el canvi "backtracking"
                partialSol[i][j] = 0;
                usedH[i][reffs[i][j].second()] &= ~(1 << (num - 1));
                usedV[reffs[i][j].first()][j] &= ~(1 << (num - 1));
            }
        }
        return false;
    }
	
	// fa lo mateix que el solver d'abans pero modifica 
	// el atribut de la classe numofSolutions perque busca
	// si te 0, 1, o mes d'una solucio
	private static boolean solveKakuroUnique(Kakuro K, int[][] partialSol, int i, int j, PairInt[][] reffs, int[][] usedH, int[][] usedV, Stack<int[]> s) {
        
		// Solved all the rows		
        if (i == partialSol.length){
        	++numOfSolutions;
        	if (numOfSolutions == 2) {
        		return true;
        	}
        	else if (numOfSolutions == 1) {
        		saveSolution(K, partialSol);
        		return false;
        	}
        }
        
     	// Solve the next row
        if (j == partialSol[0].length) {
        	return solveKakuroUnique(K, partialSol, i+1, 0, reffs, usedH, usedV, s);
        }
        
        // Solve the next Cell
        if (partialSol[i][j] != 0) { 
            return solveKakuroUnique(K, partialSol, i, j+1, reffs, usedH, usedV, s);
        }
        
        // Candidates for this white cells "row"
        int sumH = K.getCellHorizontalSum(i, j);
        int digitsH = K.getNumWhitesHor(i, j);
        int keyH = usedH[i][reffs[i][j].second()];
        int candidatesH = candidatesNew[digitsH][sumH][keyH];
      
        // Candidates for this white cells "column"
        int sumV = K.getCellVerticalSum(i, j);
        int digitsV = K.getNumWhitesVert(i, j);
        int keyV = usedV[reffs[i][j].first()][j];
        int candidatesV = candidatesNew[digitsV][sumV][keyV];
        
        int cand = candidatesH & candidatesV;   	
        
        for (int num = 1; num <= 9; num++) {
            if (((cand >> (num-1)) & 1) == 1) {

                partialSol[i][j] = num;
                usedH[i][reffs[i][j].second()] |= 1 << (num-1);
                usedV[reffs[i][j].first()][j] |= 1 << (num-1);
                
                int prev = s.size();
                
                if (initialSolver(K, partialSol, i, j+1, reffs, usedH, usedV, s, true)) return true;
                
                int curr = s.size() - prev;
                
                while (curr > 0) {
                	int[] trip = s.pop();
                	usedH[trip[0]][reffs[trip[0]][trip[1]].second()] &= ~(1 << (trip[2] - 1));
                    usedV[reffs[trip[0]][trip[1]].first()][trip[1]] &= ~(1 << (trip[2] - 1));
                    partialSol[trip[0]][trip[1]] = 0;
                    --curr;
                }
                
                partialSol[i][j] = 0;
                usedH[i][reffs[i][j].second()] &= ~(1 << (num - 1));
                usedV[reffs[i][j].first()][j] &= ~(1 << (num - 1));
            }
        }
        return false;
    }
    
	// es guarda la solucio de la matriu d'enters a 
	// l'atribut Solution de tipus Kakuro
	private static void saveSolution(Kakuro K, int[][] sol) {
		usedHints = new boolean[K.getHeight()][K.getWidth()];
		Solution = new Kakuro(K.getHeight(), K.getWidth());
		for (int i = 0; i < K.getHeight(); i++) {
			for (int j = 0; j < K.getWidth(); j++) {
				String code;
				if (sol[i][j] == -1) code = K.getCellCode(i, j);
				else code = Integer.toString(sol[i][j]);
				Cell c = new Cell(i, j, code);
				Solution.setCell(i, j, c);
				if(c.getColor() == "Black" || c.getColor() == "EmptyBlack") usedHints[i][j] = true;
			}
		}
	}
} 