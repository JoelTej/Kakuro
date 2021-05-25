package domainLayer;

import java.io.*;
import java.util.Hashtable;

import persistenceLayer.CtrlPersistence;
import presentationLayer.CtrlPresentation;

public class CtrlDomini {

	private static Kakuro k;
	private static int numWhites = 0;
	
	//lee kakuro de input.txt (le llega el path entero del archivo)
	public static boolean readKakuro(String path, boolean validate){
		try {
			//lee el kakuro,
			int[] size = readKakuroSize(path);
			k = new Kakuro(size[0], size[1]);
			if(!CtrlPersistence.readKakuroInput(path, k)) throw new IOException();
		}
		catch (IOException e) {
			String message = "An error ocurred, can't load the kakuro";
			showException(message);
			return false;
		}
		//si hay que validarlo lo validamos
		if(validate) return validateKakuro();
		return true;
	}

	//generar kakuro con size
	public static boolean createKakuroSize(int[] opts, boolean unique){
		try {
			k = new Kakuro(opts[0], opts[1]);
			Generator.generateKakuro(k, opts[0], opts[1], unique);
		}
		catch (StackOverflowError e) {
			//e.printStackTrace();
			String message = "Internal error ocurred, please try again";
			showException(message);
			return false;
		}
		return true;
	}

	//generar kakuro con opciones (size, blacks y whites)
	public static boolean createKakuroOptions(int[] opts, boolean unique) {
		try {
			k = new Kakuro(opts[0], opts[1]);
			Generator.generateKakuro(k, opts[0], opts[1], opts[2], opts[3], unique);
			numWhites = k.getNumWhites();
		}
		catch (StackOverflowError e) {
			String message = "Internal error ocurred, please try again";
			showException(message);
			return false;
		}
		return true;
	}
	
	//generar kakuro totalmente aleatorio
	public static boolean createKakuroRandom() {
		try {
			k = new Kakuro();
			Generator.generateKakuro(k);
			numWhites = k.getNumWhites();
			String message = "The kakuro generated has a size of "+k.getHeight()+"x"+k.getWidth()+",\n"+numWhites+" white cells and "+ (k.getHeight()*k.getWidth()-numWhites) +" black cells";
			showException(message);
		}
		catch (StackOverflowError e) {
			String message = "Internal error ocurred, please try again";
			showException(message);
			return false;
		}
		return true;
	}
	
	//generar kakuro con solo dificultad
	public static boolean createKakuroDifficulty(String dif) {
		try {
			k = new Kakuro();
			Generator.generateKakuro(k, dif);
		}
		catch (StackOverflowError e) {
			String message = "Internal error ocurred, please try again";
			showException(message);
			return false;
		}
		return true;
	}
	
	//comprueba que tenga solucion en el solver (excepcion si no tiene)
	public static boolean validateKakuro() {
		if(!Solver.solveKakuro(k)) {
			String message = "The kakuro has no solution";
			showException(message);
			return false;
		}
		return true;
	}
	
	//funcion de ayuda para resolver un kakuro (ayudas iniciales)
	public static boolean initialHelp(int clues) {
		numWhites = k.getNumWhites();
		//si el numero de pistas es demasiado elevado o no se pueden poner avisamos
		if(clues>=numWhites-1 && numWhites!=0) {
			String message = "The number of clues introduced is too big.\nTry a smaller value.";
			showException(message);
			return false;
		}
		//llamamos #clues veces a initialHints para poner pistas iniciales
		for(int i = 0; i<clues; ++i) {
			Solver.initialHints(k);	//devuelve true si se ha podido anadir una pista, false si ya esta completo o no ha podido
		}
		return true;
	}
	
	//funcion de ayuda durante el juego (una sola ayuda)
	public static int[] getOneHint() {
		int[] value = {0,0,0};
		numWhites = k.getNumWhites();
		//si quedan mas de 1 casilla blanca buscamos una pista, si la encuentra decrementamos y devolvemos el valor encontrado o avisamos si no se peude encontrar
		if(numWhites>1) {
			value = Solver.getHint(k);
			if(value[2]<=0 && value[1]<=0 && value[0]<=0) {
				String message = "A clue can't be found with the values introduced";
				showException(message);
			}
			else{
				--numWhites;
				return value;
			}
		}
		//si quedan pocas (1) casillas blancas avisamos
		else {
			String message = "The kakuro is almost solved, no more clues can be added";
			showException(message);
		}
		return value;
	}
	
	//escribe el kakuro en el fichero pasado por el path
	public static void writeKakuro(String path) {
		CtrlPersistence.createFile(path);
		CtrlPersistence.writeFile(path, k);
	}
		
	//lee de un fichero rows y cols
	public static int[] readKakuroSize(String filename){
		int[] size = new int[2];
		CtrlPersistence.readKakuroInputSize(filename, size);
		return size;
	}
	
	//inicializa el solver
	public static void initializeSolver() {
		Solver.initializeCandidates();
	}
	
	//numero de lineas del file (list de respositorios)
	public static int howManyLines(String filename) {
		if(filename == "system") return CtrlPersistence.linesFile("resources/systemRepository/list.txt");
		else if (filename == "users") return CtrlPersistence.linesFile("resources/usersRepository/list.txt");
		return 0;
	}
	
	//lee el conjunto de usuarios
	public static void readUsers(Hashtable<String, String> users) {
		CtrlPersistence.readUsers("resources/users.txt", users);
	}
	
	//lee el repositorio del sistema
	public static void readSystemRepository(Object[][] systemKakuros) {
		 CtrlPersistence.readSystemRepo("resources/systemRepository/list.txt", systemKakuros);
	}
	
	//lee el repositorio de usuarios 
	public static void readUsersRepository(Object[][] usersKakuros) {
		 CtrlPersistence.readUsersRepo("resources/usersRepository/list.txt", usersKakuros);
	}
	
	//elimina los guardados por un usuario en el repositorio de usuarios
	public static void deleteRepoUser(String username) {
		CtrlPersistence.deleteUserRepo("resources/usersRepository/list.txt", username);
	}
	
	//anade el kakuro al repositorio de usuarios
	public static String addRepoUser(String username) {
		return CtrlPersistence.addUserRepo("resources/usersRepository/list.txt", username, k);
	}
	
	//anade el usuario al ranking
	public static void addRankingEntry(String username) {
		CtrlPersistence.addUser("resources/ranking.txt",username);
	}
	
	//incrementa los puntos del usuario
	public static void incrementPoints(String username) {
		CtrlPersistence.incrementUserPoints("resources/ranking.txt", username, k.getDifficulty().name());
	}
	
	//elimina el usuario del ranking
	public static void removeRankingEntry(String username) {
		CtrlPersistence.removeUser("resources/ranking.txt",username);
	}
	
	//actualiza el conjunto de usuarios
	public static void updateUsers(Hashtable<String, String> users) {
		CtrlPersistence.writeUsers("resources/users.txt", users);
	}
	
	//lee el ranking
	public static void readRanking(Object[][] userValues, int size) {
		CtrlPersistence.readRanking("resources/ranking.txt", userValues, size);
	}
	
	//numero de kakuros guardados por el usuario (counter)
	public static int howManySaved(String user) {
		return CtrlPersistence.countSavedGames("resources/savedGames/counter.txt", user);
	}
	
	//lee los juegos guardadros por el usuario (lista)
	public static void readSavedGames(Object[][] savedGames,String user) {
		CtrlPersistence.readSavedGames("resources/savedGames/list.txt", savedGames, user);
	}
	
	//anade un kakuro a partidas guardadas
	public static String addSavedGames(String username) {
		String size = k.getHeight()+"x"+k.getWidth();
		String difficulty = k.getDifficulty().name();
		String[] params = CtrlDomini.translateFileName(username, size, difficulty);
		incrementGames(params);
		String filePath = "resources/savedGames/"+params[0]+".txt";
		writeKakuro(filePath);
		return filePath;
	}
	
	//elimina TODAS las partidas guardadas del usuario (counter y lista)
	public static void deleteSavedGames(String user) {
		//las quita de la lista
		CtrlPersistence.deleteSavedGames("resources/savedGames/list.txt", user);
		//las quita del fichero contador
		CtrlPersistence.changeSavedCounter("resources/savedGames/counter.txt", user, false, true);
	}
	
	//decrementa el numero de partidas (SOLO UNA) guardadas por el usuario, elimina file_name y lo quita de la lista
	public static void decrementGames(String user, String file_name) {
		//eliminamos el archivo del sistema
		File deleteFile = new File(file_name);
		deleteFile.delete();
		//decrementamos el numero de partidas guardadas (counter)
		CtrlPersistence.changeSavedCounter("resources/savedGames/counter.txt", user, false, false);
		//eliminamos el archivo de la lista
		String file_aux = file_name.replace(".txt", "");	//eliminamos el .txt para que coincida con la entrada de la lista
		file_aux = file_aux.replace("resources/savedGames/", "");
		CtrlPersistence.decrementListSaved("resources/savedGames/list.txt", file_aux);
	}
	
	//incrementa el numero de partidas guardadas por el usuario (counter y lista)
	private static void incrementGames(String[] params) {
		//incrementamos el contador
		CtrlPersistence.changeSavedCounter("resources/savedGames/counter.txt", params[1], true, false);
		//anadimos la entrada a la lista
		CtrlPersistence.incrementListSaved("resources/savedGames/list.txt", params);
	}
	
	//muestra la excepcion por pantalla
	public static void showException(String message) {
		CtrlPresentation.exception(message);
	}
	
	//devuelve los parametros de la entrada de la lista a la que pertoca
	public static String[] translateFileName(String user, String size, String difficulty) {
		return CtrlPersistence.translateFileName("resources/savedGames/list.txt", user, size, difficulty);
	}
	
	//pasa el tablero del kakuro a una matriz de strings
	public static void getKakuro(String[][] kakuroBoard) {
		int height = k.getHeight();
		int width = k.getWidth();
		for(int i = 0; i < height; ++i) {
			for(int j = 0; j < width; ++j) {
				String vertSum = "";
				String horSum = "";
				if(k.getCellColor(i,j).equals("White")) kakuroBoard[i][j] = "";
				else if(k.getCellColor(i,j).equals("Solved")) kakuroBoard[i][j] = Integer.toString(k.getCellValue(i,j));
				else if(k.getCellColor(i,j).equals("EmptyBlack")) kakuroBoard[i][j] = "  ";
				else if(k.getCellColor(i,j).equals("Black")) {
					if(k.getCellVerticalSum(i,j) != 0) vertSum = Integer.toString(k.getCellVerticalSum(i,j));
					if(k.getCellHorizontalSum(i,j) != 0) horSum = Integer.toString(k.getCellHorizontalSum(i,j));
					if(vertSum != "" && horSum != "") kakuroBoard[i][j] = vertSum+"/"+horSum;
					else if(vertSum != "" && horSum == "") kakuroBoard[i][j] = vertSum+"/";
					else if(vertSum == "" && horSum != "") kakuroBoard[i][j] = "/"+horSum;
				}
			}
		}
	}
	
	//asigna el valor de una celda
	public static void setKakuroCell(int row, int col, int val) {
		k.setCellValue(row, col, val);
		k.setCellColor(row, col, "Solved");
	}
	
	//elimina el valor de una celda
	public static void deleteKakuroCell(int row, int col) {
		k.deleteCellValue(row, col);
	}
	
	//devuelve el tamano del kakuro
	public static int[] getKakuroSize() {
		int[] size = { k.getHeight(), k.getWidth() };
		return size;
	}

	//devuelve true si la solucion propuesta es correcta
	public static boolean checkSolution() {
		return Solver.checkSolution(k);
	}
	
	//actualiza el record del kakuro 
	public static void updateRecord(String time, String username, String path) {
		setRecord(time,username);
		Solver.cleanSolved(k);	//"limpiamos" la solucion para guardarlo con el nuevo record
		writeKakuro(path);
	}
	//asigna el record del kakuro
	public static void setRecord(String time, String username) {
		k.setRecord(time,username);
	}
	
	//devuelve el record actual del kakuro
	public static String getRecord() {
		if(k.getRecord() != null) return k.getRecord();
		return "59:59.99";
	}
	
	//comprueba si se puede anadir el valor
	public static boolean checkValue(int i, int j, int value) {
		int auxI = i-1;
		int auxJ = j-1;
		int sumaHor, sumaVer;
		while(!(k.getCellColor(auxI, j)=="Black"))--auxI;
		while(!(k.getCellColor(i, auxJ)=="Black"))--auxJ;
		sumaHor = k.getCellHorizontalSum(i, auxJ);
		sumaVer = k.getCellVerticalSum(auxI, j);
		
		//primero en vertical: hacia arriba y abajo, luego en horizontal: hacia la izquierda y la derecha
		boolean stop = false;
		int sumaAux = value;
		boolean emptyCells = false;
		//arriba:
		for(auxI = i-1; !stop && auxI>0;--auxI) {
			String color = k.getCellColor(auxI, j);
			if(color.equals("Black")) {	//si la suma auxiliar es mayor a la que hay que resolver O es igual pero hay celdas blancas devolvemos false
				stop = true;
				if(sumaAux>sumaVer || sumaAux==sumaVer && emptyCells) {
					return false;
				}
			}
			else if(color.equals("Solved")) {	//si la celda esta ocupada por un numero y ese numero coincide con el nuestro devolvemos false, si no coincide aumentamos la suma auxiliar
				if(value == k.getCellValue(auxI, j)) {
					return false;
				}
				if((sumaAux+= k.getCellValue(auxI, j)) >sumaVer) return false;
			}
			else if(color.equals("White")) emptyCells = true;	//indicamos que hay al menos una celda blanca usada
		}
		stop = false;
		//abajo:
		for(auxI = i+1; !stop && auxI<k.getHeight(); ++auxI) {
			String color = k.getCellColor(auxI, j);
			if(color.equals("Black") || color.equals("EmptyBlack")) {
				stop = true;
				if(sumaAux>sumaVer || sumaAux==sumaVer && emptyCells) {
					return false;
				}
			}
			else if(color.equals("Solved")) {
				if(value == k.getCellValue(auxI, j)) {
					return false;
				}
				if((sumaAux+= k.getCellValue(auxI, j)) >sumaVer) return false;
			}
			else if(color.equals("White")) emptyCells = true;
		}
		stop = false;
		sumaAux = value;
		emptyCells = false;
		//izquierda:
		for(auxJ = j-1; !stop && auxJ>0; --auxJ) {
			String color = k.getCellColor(i, auxJ);
			if(color.equals("Black")) {
				stop = true;
				if(sumaAux>sumaHor || sumaAux == sumaHor && emptyCells) {
					return false;
				}
			}
			else if(color.equals("Solved")) {
				if(value == k.getCellValue(i, auxJ)) {
					return false;
				}
				if((sumaAux+= k.getCellValue(i,auxJ)) >sumaHor) return false;
			}
			else if(color.equals("White")) emptyCells = true;
		}

		stop = true;
		//derecha:
		for(auxJ = j+1; !stop && auxJ<k.getWidth(); ++auxJ) {
			String color = k.getCellColor(i, auxJ);
			if(color.equals("Black") || color.equals("EmptyBlack")) {
				stop = true;
				if(sumaAux>sumaHor || sumaAux == sumaHor && emptyCells) {
					return false;
				}
			}
			else if(color.equals("Solved")) {
				if(value == k.getCellValue(i, auxJ)) {
					return false;
				}
				if((sumaAux+= k.getCellValue(i,auxJ)) >sumaHor) return false;
			}
			else if(color.equals("White")) emptyCells = true;
		}
		
		return true;
	}

	//lee un kakuro (drivers)
	//lee un Kakuro de un fichero
	public static void readKakuro(Kakuro K){
		try{
			System.out.print("Enter the name of the file (file.txt) \n");
			BufferedReader reader =  new BufferedReader(new InputStreamReader(System.in)); 
		    String filename = reader.readLine(); 
		       
			int[] size = readKakuroSize(filename);
			K.initializeKakuro(size[0], size[1]);
				
			CtrlPersistence.readKakuroInput(filename, K);
		}
		catch(IOException e) {
			String message = "An error ocurred, can't load the kakuro";
			showException(message);
		}
	}
	
	public static boolean solveKakuro() {
		return Solver.solveKakuro(k);
	}
	//hay que poner esta funcion entonces
	
	//copia la solucion encontrada por el solver en el kakuro
	public static void copySolution() {
		k = Solver.getSolution();
	}
}	