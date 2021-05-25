package persistenceLayer;

import java.io.*;

import java.util.StringTokenizer;		// Import the StringTokenizer
import java.util.TreeMap;

import domainLayer.Cell;
import domainLayer.CtrlDomini;
import domainLayer.Generator;
import domainLayer.Kakuro;

import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;
import java.util.Scanner; 				// Import the Scanner class to read text files
import java.util.Set;

public class CtrlPersistence {
	
	/* CREAR/ESCRIBIR UN KAKURO en un fichero */
	//crea un archivo de nombre .txt file_name
	public static void createFile(String file_name) {
		try {
			File f = new File(file_name);
			//si el file ya existe no imprimimos nada, sino lo creamos
			if(!f.exists() && f.createNewFile()) {
				String message = "The file "+f.getName()+" has been created";
				CtrlDomini.showException(message);
			}
		}
		catch (IOException e) {
			//e.printStackTrace();
			String message = "An error ocurred, can't create file "+file_name;
			CtrlDomini.showException(message);
		}	
	}
	
	//Escribe el kakuro k en el archivo pasado por parametro (mismo formato que al leerlo)
	public static void writeFile(String file_name, Kakuro k) {
		try {
			FileWriter f = new FileWriter(file_name);
			//escribimos el tamano del kakuro (width,height)
			int Width = k.getWidth();
			int Height = k.getHeight();
			f.write(Height + "," + Width+"\n");
			for (int i = 0; i < Height; i++) {
				for (int j = 0; j < Width; j++) {
					//para cada celda escribimos su codigo (el formato de input requiere ',' entre elementos)
					if(j == 0) f.write(k.getCellCode(i,j));
					else f.write(","+k.getCellCode(i,j));
				}
				f.write("\n");
			}
			if(k.getRecord() != null) {
				f.write(k.getRecordUser()+","+k.getRecord()+"\n");
			}
			//cerramos el documento
	        f.close();
		}
		catch (IOException e) {
			//e.printStackTrace();
			String message = "An error ocurred, can't write the kakuro board";
			CtrlDomini.showException(message);
		}
	}
	
	
	/* LEER KAKUROS (entero, solo el size o varias opciones) */
	//lee kakuro entero de un input file
	public static boolean readKakuroInput(String file_name, Kakuro k) {
		try {
			File f = new File(file_name);
			@SuppressWarnings("resource")
			Scanner sc = new Scanner(f);
			//Leemos rows y columns (size)
			String rowscols = sc.nextLine();
			//consideramos que puede haber rows y columns de mas de 1 cifra:
			int length = rowscols.length();
			String rowsAux, colsAux;
			rowsAux = colsAux = "";
			
			int pos = 0;
			while(rowscols.charAt(pos) != ','){
				rowsAux+=rowscols.charAt(pos);
				++pos;
			}
			++pos;	//saltamos el ","
			while(pos<length){
				colsAux+=rowscols.charAt(pos);
				++pos;
			}
			int rows = Integer.valueOf(rowsAux);
			int cols = Integer.valueOf(colsAux);
			
			if(rows<3 || cols<3) {	//excepcion de tamano muy pequeno
				String message= "The kakuro introduced has a size not valid";
				CtrlDomini.showException(message);
			}
			
			//la inicializacion del kakuro se hace antes asi que solo asignamos las celdas
			
			String line = "";
			StringTokenizer token;
			boolean stop = false;
			
			for (int i = 0;!stop &&  i < rows; i++) {
				if(!sc.hasNext()) throw new IOException();
				line = sc.nextLine();
				token = new StringTokenizer(line, ",");
				for (int j = 0;!stop && j < cols; j++) {
					String value = token.nextToken();
					Cell c = new Cell(i, j, value);
					if(c.getColor() == "EmptyBlack" && !value.equals("*")) {
						stop = true;
						String message = "falla al leer una casilla empty black";
						CtrlDomini.showException(message);
						break;
					}
					else if(c.getColor() == "Black" && (c.getHorizontalSum()>45 || c.getVerticalSum()>45)){
						stop = true;
						String message = "falla al leer una casilla black";
						CtrlDomini.showException(message);
						break;
					}
					else if(c.getColor() == "White" && !value.equals("?")) {
						stop = true;
						String message = "falla al leer una casilla white";
						CtrlDomini.showException(message);
						break;
					}									
					else if(c.getColor() == "Solved" && (Integer.valueOf(value)>9 || Integer.valueOf(value)<1)) {
						stop = true;
						String message = "falla al leer una casilla solved";
						CtrlDomini.showException(message);
						break;
					}
					else if(c.getColor() == null) {
						stop = true;
						String message = "falla al leer la celda ("+i+","+j+")";
						CtrlDomini.showException(message);
						break;
					}
					
					//una vez leido el valor de la celda (i,j) la asignamos como celda nueva (asigna directamente color, vertSum..) y la colocamos en nuestra board
					k.setCell(i, j, c);
					if (k.getCellColor(i, j) == "White" || k.getCellColor(i, j) == "Solved") {
						k.setSumVerHor(i, j);
					}
					Generator.setWhitesHorVer(k,true);
				}
			}
			if(sc.hasNext()) {	//si tiene linea de record lo leemos
				//si es de un repositorio leemos record
				line = sc.nextLine();
				token = new StringTokenizer(line, ",");
				String userRecord = token.nextToken();
				String timer = token.nextToken();
				k.setRecord(timer, userRecord);
			}
			
			sc.close();
		}
		catch (IOException e) {
			 //e.printStackTrace();
			String message = "An error ocurred, can't read the kakuro from file";
			CtrlDomini.showException(message);
			return false;
		}
		return true;
	}
	
	
	
	//lee los parametros necesarios para generar un kakuro (rows,columns,white,black,sum) 
	public static void readKakuroInputOptions(String file_name, int[] opts) {
		try {
			File f = new File(file_name);
			Scanner sc = new Scanner(f);
								
			String line = sc.nextLine();
			StringTokenizer token = new StringTokenizer(line, ",");
			
			//leemos rows
			String value = token.nextToken();
			opts[0] = Integer.parseInt(value);
			
			//leemos columns
			value = token.nextToken();
			opts[1]  = Integer.parseInt(value);
			
			//leemos numero de casillas negras
			value = token.nextToken();
			opts[2]  = Integer.parseInt(value);
			
			//leemos numero de casillas blancas
			value = token.nextToken();
			opts[3]  = Integer.parseInt(value);
			
			sc.close();
			//generamos un kakuro con solucion unica segun los parametros pasados
		}
		catch (IOException e) {
			//e.printStackTrace();
			String message = "An error ocurred, can't read the kakuro options";
			CtrlDomini.showException(message);
		}
	}
	
	//lee el size del kakuro a leer (personalizado)
	public static void readKakuroInputSize(String file_name, int[] p) {
		try {
			File f = new File(file_name);
			Scanner sc = new Scanner(f);
						
			String rowscols = sc.nextLine();
			
			//consideramos que puede haber rows y columns de mas de 1 cifra:
			int length = rowscols.length();
			String rowsAux, colsAux;
			rowsAux = colsAux = "";
			
			int pos = 0;
			while(rowscols.charAt(pos) != ','){
				rowsAux+=rowscols.charAt(pos);
				++pos;
			}
			++pos;	//saltamos el ","
			while(pos<length){
				colsAux+=rowscols.charAt(pos);
				++pos;
			}
			p[0] = Integer.valueOf(rowsAux);
			p[1] = Integer.valueOf(colsAux);
			
			if(p[0]<3 || p[1]<3) {	//excepcion de tamano muy pequeno
				String message= "The kakuro introduced has a size not valid";
				CtrlDomini.showException(message);
			}
			sc.close();
		}
		catch (IOException e) {
			//String message = "An error ocurred, can't read the kakuro size";
			//CtrlDomini.showException(message);
		}
	}
	
	
	/* LECTURA/ESCRITURA DE USUARIOS */
	//lee el conjunto de usuarios registrados en el sistema
	public static void readUsers(String file_name, Hashtable<String, String> users) {
		try {
			File f = new File(file_name);
			Scanner sc = new Scanner(f);
			
			int numUsers = sc.nextInt();
			
			String line = sc.nextLine();
			for(int i = 0; i < numUsers; ++i) {
				line = sc.nextLine();
				StringTokenizer st = new StringTokenizer(line);
				String name = st.nextToken();
				String password = st.nextToken();
				users.put(name, password);
			}
			
			sc.close();
		}
		catch (IOException e) {
			//e.printStackTrace();
			String message = "An error ocurred, can't read the users' set";
			CtrlDomini.showException(message);
		}
	}
	
	//escribe el conjunto de usuarios
	public static void writeUsers(String file_name, Hashtable<String, String> users) {
		try {
			FileWriter f = new FileWriter(file_name);
			
			int size = users.size();
			f.write(size+"\n");
			Set<String> names = users.keySet();
			for (String name: names) {
				
				String password = (String) users.get(name);
				f.write(name+" "+password+"\n");
			}
			//cerramos el documento
	        f.close();
		}
		catch (IOException e) {
			//e.printStackTrace();
			String message = "An error ocurred, can't add upload the users file";
			CtrlDomini.showException(message);
		}
	}

	
	/* LECTURA/ESCRITURA/INCREMENTO/ANADIR USUARIOS AL RANKING */ 
	//lee el ranking de los usuarios
	public static void readRanking(String file_name, Object[][] ranking, int numUsers) {
		try {
			File f = new File(file_name);
			Scanner sc = new Scanner(f);
			
			TreeMap<String, Ranking> order = new TreeMap<String, Ranking>(Collections.reverseOrder());
			//leemos todo el ranking y lo guardamos en una Hashtable
			for(int i = 0; i < numUsers; ++i) {
				String line = sc.nextLine();
				StringTokenizer st = new StringTokenizer(line);
				//cogemos los valores del .txt
				String username = st.nextToken();
				Integer total = Integer.parseInt(st.nextToken());
				Integer hard =  Integer.parseInt(st.nextToken());
				Integer medium = Integer.parseInt(st.nextToken());
				Integer easy =  Integer.parseInt(st.nextToken());
				
				//guardamos los valores en un vector y creamos la entrada del ranking
				Integer[] values = {total, hard, medium, easy};
				Ranking auxRank = new Ranking(username,values);
				
				//usamos una string auxiliar donde ponemos como identificador total+username -> normalizamos el valor total con 0s a la izquierda para evitar fallos en caso de diferentes cifras
				String auxStr = "";
				if(total<10) auxStr+="00";	//anadimos 0s para "igualar" el nivel de suma -> 10>5 pero 10<5v -> 010>005
				else if(total<100) auxStr+="0";	//suponemos puntuaciones de 3 cifras (mas margen de juego)
				//si quisiesemos usar mas puntuaciones (>1000) con ifs equivalentes (y mas 0s en los anteriores) se obtendria el mismo resultado
				auxStr+=Integer.toString(total)+" ";
				if(hard<10) auxStr+="0";
				auxStr+=Integer.toString(hard)+" ";
				if(medium<10) auxStr+="0";
				auxStr+=Integer.toString(medium)+" ";
				if(easy<10) auxStr+="0";
				auxStr+=Integer.toString(easy)+" ";
				auxStr+=username;
				//anadimos la entrada al ranking
				order.put(auxStr, auxRank);
			}
			
			int i = 0;
			for(Map.Entry<String,Ranking> entry : order.entrySet()) {
				Ranking user = entry.getValue();
				ranking[i][0] = i+1;
				ranking[i][1] = user.username; 
				ranking[i][2] = user.total;//values[0];
				ranking[i][3] = user.hard;//values[1];
				ranking[i][4] = user.medium;//values[2];
				ranking[i][5] = user.easy;//values[3];
				
				++i;
			}
			
			sc.close();
		}
		catch (IOException e) {
			//e.printStackTrace();
			String message = "An error ocurred, can't load the ranking";
			CtrlDomini.showException(message);
		}
	}
	
	//anade el usuario al ranking
	public static void addUser(String file_name, String user) {
		try {
			File inputFile = new File(file_name);
			File tempFile = new File("temporaryFile.txt");

			BufferedReader reader = new BufferedReader(new FileReader(inputFile));
			BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

			String currentLine;

			while((currentLine = reader.readLine()) != null) {
			    writer.write(currentLine + "\n");
			}
			writer.write(user+" 0 0 0 0");
			
			//cerramos los dos files
			writer.close(); 
			reader.close(); 
			//eliminamos el original
			inputFile.delete();
			//renombramos el archivo temporal al original
			tempFile.renameTo(inputFile);
		}
		catch (IOException e) {
			//e.printStackTrace();
			String message = "An error ocurred, can't add "+user+" to the ranking";
			CtrlDomini.showException(message);
		}
	}
	
	//elimina el usuario del ranking
	public static void removeUser(String file_name, String user) {
		try {
			File inputFile = new File(file_name);
			File tempFile = new File("temporaryFile.txt");

			BufferedReader reader = new BufferedReader(new FileReader(inputFile));
			BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

			String currentLine;

			while((currentLine = reader.readLine()) != null) {
			    //separamos los diferentes valores de la linea
				String[] splited = currentLine.split("\\s+");
			    if(splited[0].equals(user)) continue;	//saltamos escribir la linea
			    writer.write(currentLine + "\n");
			}
			//cerramos los files
			writer.close(); 
			reader.close(); 
			//eliminamos el original
			inputFile.delete();
			//renombramos el archivo
			tempFile.renameTo(inputFile);
		}
		catch (IOException e) {
			//e.printStackTrace();
			String message = "An error ocurred, can't delete "+user+" from the ranking";
			CtrlDomini.showException(message);
		}
	}
	
	//incrementa los puntos del usuario segun pointType (1 si es "easy", 2 si es "medium" y 3 si es "hard") -> aumenta proporcionalmente el total
	public static void incrementUserPoints(String file_name, String user, String pointType) {
		try {
			File inputFile = new File(file_name);
			File tempFile = new File("temporaryFile.txt");

			BufferedReader reader = new BufferedReader(new FileReader(inputFile));
			BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

			String currentLine;

			while((currentLine = reader.readLine()) != null) {
			    //separamos los diferentes valores de la linea
				String[] splited = currentLine.split("\\s+");
			    if(splited[0].equals(user)) {
			    	int total = Integer.valueOf(splited[1]);
			    	int hard = Integer.valueOf(splited[2]);
			    	int medium = Integer.valueOf(splited[3]);
			    	int easy = Integer.valueOf(splited[4]);
			    	if(pointType == "easy") {
			    		easy++;
			    		total+=1;
			    	}
			    	else if(pointType == "medium") {
			    		medium++;
			    		total+=2;
			    	}
			    	else if(pointType == "hard") {
			    		hard++;
			    		total+=3;
			    	}
			    	writer.write(user+" "+total+" "+hard+" "+medium+" "+easy + "\n");
			    }
			    else writer.write(currentLine + "\n");
			    
			}
			//cerramos los files
			writer.close(); 
			reader.close(); 
			//eliminamos el original
			inputFile.delete();
			//renombramos el archivo
			tempFile.renameTo(inputFile);
		}
		catch (IOException e) {
			//e.printStackTrace();
			String message = "An error ocurred, can't increment "+user+"'s points on the ranking";
			CtrlDomini.showException(message);
		}
	}
	
	
	/* LECTURA/ELIMINAR USUARIOS/ANADIR KAKUROS A LOS REPOSITORIOS DE USER Y SYSTEM*/
	//lee los diferentes kakuros disponibles del repositorio del sistema
	public static void readSystemRepo(String file_name, Object[][] systemRepo) {
		//leer list.txt y guardarlos en el Object
		try {
			File list = new File(file_name);
			Scanner sc = new Scanner(list);
			
			String line;	//leemos la linea
			int i = 0;
			while(sc.hasNext()) {	//mientras la linea no sea nula (haya linea)
				line = sc.nextLine();
				StringTokenizer st = new StringTokenizer(line);	//dividimos la linea en diferentes strings
				st.nextToken();	//descartamos el nombre del fichero
				systemRepo [i][0] = st.nextToken();	//tamano del kakuro
				systemRepo[i][1] = st.nextToken();	//dificultad del kakuro
				systemRepo[i][2] = st.nextToken();	//id del kakuro
				++i;
			}
			sc.close();
		}
		catch (IOException e) {
			//e.printStackTrace();
			String message = "An error ocurred, can't load the System Repository File";
			CtrlDomini.showException(message);
		}
	}
	
	//lee los diferentes kakuros disponibles del repositorio de usuarios
	public static void readUsersRepo(String file_name, Object[][] userRepo) {
		//leer list.txt y guardarlos en el Object
		try {
			File list = new File(file_name);
			Scanner sc = new Scanner(list);
			String line;
			int i = 0;
			while(sc.hasNext()) {	//mientras la linea no sea nula (haya linea)
				line = sc.nextLine();
				StringTokenizer st = new StringTokenizer(line);	//dividimos la linea en diferentes strings
				st.nextToken();	//descartamos el nombre del fichero
				userRepo[i][0] = st.nextToken();	//nombre del usuario
				userRepo[i][1] = st.nextToken();	//tamano del kakuro
				userRepo[i][2] = st.nextToken();	//dificultad del kakuro
				userRepo[i][3] = st.nextToken();	//id del kakuro
				
				++i;
			}
			sc.close();
		}
		catch (IOException e) {
			//e.printStackTrace();
			String message = "An error ocurred, can't load the Users Repository File";
			CtrlDomini.showException(message);
		}
	}
	
	//eliminar los kakuros guardados por el usuario username
	public static void deleteUserRepo(String file_name, String username) {
		//leemos list.txt y en caso de que sea
		try {
			File list = new File(file_name);
			File tempFile = new File("temporaryFile.txt");
			
			BufferedReader reader = new BufferedReader(new FileReader(list));
			BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
			String currentLine;
			
			while((currentLine = reader.readLine()) != null) {
				String[] splited = currentLine.split("\\s+");
				String user = splited[1];	//cogemos el usuario
				if(user.equals(username)) {
					File deleteFile = new File("resources/usersRepository/"+splited[0]+".txt");
					deleteFile.delete();
				}
				else {
					writer.write(currentLine + "\n");	//si no coincide el usuario escribimos la linea tal cual
				}
			}
			//una vez vistas todas las lineas cerramos, eliminamos el original y renombramos el temporal
			writer.close();
			reader.close();
			
			list.delete();
			tempFile.renameTo(list);
			
		}
		catch (IOException e) {
			//e.printStackTrace();
			String message = "An error ocurred, can't delete the kakuros saved by the user "+username+" on the users repository";
			CtrlDomini.showException(message);
		}
	}
	
	//anadir el kakuro Kak, generado por el usuario Username
	public static String addUserRepo(String file_name, String username, Kakuro kak) {
		String path = file_name;
		try {
			String title = username;
			title+="-"+kak.getHeight()+"x"+kak.getWidth()+"-"+kak.getDifficulty();
			
			File list = new File(file_name);
			File tempFile = new File("temporaryFile.txt");
			
			BufferedReader reader = new BufferedReader(new FileReader(list));
			BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
			String currentLine;
			int id=0;
			while((currentLine = reader.readLine()) != null) {
				String[] splited = currentLine.split("\\s+");
				String fileName = splited[0];	//cogemos el titulo
				if(id == 0 && title.equals(fileName)) ++id;	//si no hemos encontrado ninguno igual (id == 0) comprobamos que el title tal cual coincide con el titulo
				else if(id!=0 && (title+Integer.toString(id)).equals(fileName)) ++id;	//si ya hemos encontrado alguno igual (id != 0) comprobamos que el title+id coincide con el titulo
				//en cualquier caso escribimos la linea
				writer.write(currentLine + "\n");
				
			}
			//una vez leidas todas anadimos la nuestra
			
			//si id != 0 cambiamos el titulo antes de escribirlo
			if(id != 0) title= username+"-"+kak.getHeight()+"x"+kak.getWidth()+"-"+kak.getDifficulty()+id;
			//anadimos la entrada
			writer.write(title+" "+username+" "+kak.getHeight()+"x"+kak.getWidth()+" "+kak.getDifficulty()+" "+id+"\n");
			
			//cerramos para evitar sobreescribir los que ya están
			reader.close();
			writer.close();
			list.delete();
			tempFile.renameTo(list);
			kak.setRecord("59:59.99", username); //tiempo "maximo" a vatir
			path = "resources/usersRepository/"+title+".txt";
			//creamos el archivo y escribimos el kakuro
			createFile(path);
			writeFile(path,kak);
		}
		catch (IOException e) {
			//e.printStackTrace();
			String message = "An error ocurred, can't save the kakuro at the users repository";
			CtrlDomini.showException(message);
		}
		return path;
	}
	
	//devuevle el numero de filas de un fichero
	public static int linesFile(String file_name) {
		int cont = 0;
		try{
			FileReader input = new FileReader(file_name);
			LineNumberReader count = new LineNumberReader(input);
			while(count.skip(Long.MAX_VALUE)>0) {}
			count.close();
			cont = count.getLineNumber()+1;
			
		}
		catch (IOException e) {
			//e.printStackTrace();
			String message = "An error ocurred, can't count the lines of the file";
			CtrlDomini.showException(message);
		}
		return cont;
	}
	
	
	/* LECTURA/ELIMINAR/INCREMENTAR/DECREMENTAR NUMERO DE KAKUROS GUARDADOS*/
	//devuelve las diferentes partidas guardadas por el usuario 'user'
	public static void readSavedGames(String file_name, Object[][] usersSaved, String user) {
		//leer list.txt y guardarlos en el Object
		try {
			File list = new File(file_name);
			Scanner sc = new Scanner(list);
			
			String line;	//leemos la linea
			int i = 0;
			while(sc.hasNext()) {	//mientras la linea no sea nula (haya linea)
				line = sc.nextLine();
				if(line == "") break;
				StringTokenizer st = new StringTokenizer(line);	//dividimos la linea en diferentes strings
				st.nextToken();	//descartamos el nombre del fichero
				String username = st.nextToken().strip();
				if(user.equals(username)) {
					usersSaved[i][0] = st.nextToken();	//tamano del kakuro
					usersSaved[i][1] = st.nextToken();	//dificultad del kakuro
					usersSaved[i][2] = st.nextToken();	//id del kakuro
					++i;
				}
			}
			sc.close();
		}
		catch (IOException e) {
			//e.printStackTrace();
			String message = "An error ocurred, can't load the System Repository File";
			CtrlDomini.showException(message);
		}
	}
	
	//elimina todas las partidas guardadas del usuario
	public static void deleteSavedGames(String file_name, String username) {
		//leemos list.txt y eliminamos
				try {
					File list = new File(file_name);
					File tempFile = new File("temporaryFile.txt");
					
					BufferedReader reader = new BufferedReader(new FileReader(list));
					BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
					String currentLine;
					
					while((currentLine = reader.readLine()) != null) {
						String[] splited = currentLine.split("\\s+");
						String user = splited[1];	//cogemos el usuario
						if(user.equals(username)) {
							File deleteFile = new File("resources/savedGames/"+splited[0]+".txt");
							deleteFile.delete();
						}
						else {
							writer.write(currentLine + "\n");	//si no coincide el usuario escribimos la linea tal cual
						}
					}
					//una vez vistas todas las lineas cerramos, eliminamos el original y renombramos el temporal
					writer.close();
					reader.close();
					
					list.delete();
					tempFile.renameTo(list);
					
				}
				catch (IOException e) {
					//e.printStackTrace();
					String message = "An error ocurred, can't delete the kakuros saved by the user "+username+" on the users repository";
					CtrlDomini.showException(message);
				}
	}
	
	
	//incrementa (add = true) o decrementa (add = false) el contador de partidas guardadas del usuario. Si el usuario no esta crea la entrada (en caso de incrementar), si delete =true elimina esa entrada (en caso de add=false)
	public static void changeSavedCounter(String file_name, String username, boolean add, boolean delete) {
		try {
			File inputFile = new File(file_name);
			File tempFile = new File("temporaryFile.txt");

			BufferedReader reader = new BufferedReader(new FileReader(inputFile));
			BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

			String currentLine;
			boolean found = false;	//por si hay que anadir la entrada
			
			while((currentLine = reader.readLine()) != null) {
			    //separamos los diferentes valores de la linea
				String[] splited = currentLine.split("\\s+");
			    if(splited[0].equals(username)) {
			    	found = true;
			    	int total = Integer.valueOf(splited[1]);
			    	if(add) {
			    		++total;
			    		writer.write(username+" "+total+"\n");
			    	}
			    	else if(!delete) {	//!add && !delete -> solo decrement
			    		--total;
			    		if(total>0) writer.write(username+" "+total+"\n");	//escribimos solo si quedan partidas
			    	}
			    	//en caso de !add y delete no escribimos la linea
			    }
			    //si no es el usuario que buscamos lo escribimos tal cual
			    else writer.write(currentLine + "\n");
			}
			if(add && !found) writer.write(username+" "+1+"\n");	//si no lo hemos encontrado y hay que anadirlo, anadimos la entrada
			
			//cerramos los files
			writer.close(); 
			reader.close(); 
			//eliminamos el original
			inputFile.delete();
			//renombramos el archivo
			tempFile.renameTo(inputFile);
		}
		catch (IOException e) {
			//e.printStackTrace();
			String message = "An error ocurred, can't modify "+username+"'s saved games counter";
			CtrlDomini.showException(message);
		}
	}
	
	//devuelve el numero de partidas que tiene un ususario guardadas
	public static int countSavedGames(String file_name, String username) {
		//leer counter.txt y devolver el numero de kakuros guardados
		try {
			File list = new File(file_name);
			int counter = 0;
			BufferedReader reader = new BufferedReader(new FileReader(list));
			String currentLine;
			boolean found = false;
			while(!found && (currentLine = reader.readLine()) != null) {	//mientras la linea no sea nula (haya linea)
				String[] splited = currentLine.split("\\s+");
			    if(splited[0].equals(username)) {
			    	counter = Integer.parseInt(splited[1]);
			    	found = true;
			    }
			}
			reader.close();
			return counter;
		}
		catch (IOException e) {
			//e.printStackTrace();
			String message = "An error ocurred, can't count User Saved Games";
			CtrlDomini.showException(message);
		}
		//si ha habido un error devuelve 0 (si no se ha encontrado hace el return fuera del while) 
		return 0;
	}
	
	//elimina la entrada identificada por deleteFile de la lista de partidas guardadas 
	public static void decrementListSaved(String file_name, String deleteFile) {
		try {
			File inputFile = new File(file_name);
			File tempFile = new File("temporaryFile.txt");

			BufferedReader reader = new BufferedReader(new FileReader(inputFile));
			BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

			String currentLine;

			while((currentLine = reader.readLine()) != null) {
				String[] splited = currentLine.split("\\s+");
				//si el nombre del archivo no coincide, lo escribimos
				if(!splited[0].equals(deleteFile)) writer.write(currentLine + "\n");
			}
			
			//cerramos los files
			writer.close(); 
			reader.close(); 
			//eliminamos el original
			inputFile.delete();
			//renombramos el archivo
			tempFile.renameTo(inputFile);
		}
		catch (IOException e) {
			//e.printStackTrace();
			String message = "An error ocurred, can't delete the saved game";
			CtrlDomini.showException(message);
		}
	}
	
	//anade la entrada con todos los parametros (params) en la lista
	public static void incrementListSaved(String file_name, String[] params) {
		try {
			File inputFile = new File(file_name);
			File tempFile = new File("temporaryFile.txt");

			BufferedReader reader = new BufferedReader(new FileReader(inputFile));
			BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

			String currentLine;

			while((currentLine = reader.readLine()) != null) {
				writer.write(currentLine+"\n");
			}
			//cuando hemos escrito todas las entradas anadimos la nueva
			writer.write(params[0]+" "+params[1]+" "+params[2]+" "+params[3]+" "+params[4]+"\n");
			//cerramos los files
			writer.close(); 
			reader.close(); 
			//eliminamos el original
			inputFile.delete();
			//renombramos el archivo
			tempFile.renameTo(inputFile);
		}
		catch (IOException e) {
			//e.printStackTrace();
			String message = "An error ocurred, can't add the saved game";
			CtrlDomini.showException(message);
		}
	}
	
	//obtiene el fileName de un archivo como entrada de la lista
	public static String[] translateFileName(String file_name, String username, String size, String difficulty) {
		String[] result = new String[5];
		result[1] = username;
		result[2] = size;
		result[3] = difficulty;
		try {
			File inputFile = new File(file_name);
			BufferedReader reader = new BufferedReader(new FileReader(inputFile));
			
			String cabeceraAux = username+"-"+size+"-"+difficulty;
			int id = 0;
			String currentLine;

			while((currentLine = reader.readLine()) != null) {
				String[] splited = currentLine.split("\\s+");
				String aux2 = cabeceraAux+String.valueOf(id);
				if(splited[0].equals(aux2) || splited[0].equals(cabeceraAux)) {	//si la cabecera corresponde con la entrada de la lista aumentamos id
					++id;
				}
			}
			//una vez leido todo juntamos la cabecera y los anadimos al vector de parametros
			result[0] = cabeceraAux;
			if(id != 0) result[0] +=String.valueOf(id);	//si id != 0 lo anadimos al nombre del archivo
			result[4] = String.valueOf(id);
			reader.close(); 
			
		}
		catch (IOException e) {
			//e.printStackTrace();
			String message = "An error ocurred, can't translate the file name to save the game";
			CtrlDomini.showException(message);
		}
		return result;
	}
	
	
}