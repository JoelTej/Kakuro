package presentationLayer;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.util.Hashtable;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import domainLayer.CtrlDomini;

public class CtrlPresentation {
	//"baraja" general
	private JPanel background;
	private CardLayout cl;
	public JFrame frmMain;
	
	//pantallas permanentes (se cargan siempre (son basicas, independientes y se cargan si o si)
	private JPanel presentationPage, initialPage, genLoginPage;
	
	//pantallas de juego/dependientes (no se cargan si no ha pasado algo previamente (iniciar sesion, darle a un boton..)-> asi podemos "jugar" con el contenido sin tener varias pantallas)
	private JPanel userPage, creditPage, mainPage, playPage, rankingPage;
	private JPanel repositoryPage, systemRepoPage, userRepoPage;
	private JPanel generatePage, introducePage, resumePage;
	private JPanel loadPage, gamePage;
		
	//estructuras de repositorios y partidas guardadas
	private int lines = CtrlDomini.howManyLines("system");
	private Object[][] systemKakurosNoModify = new Object[lines][3];
	private Object[][] usersKakurosNoModify;
	private Object[][] userSaved;	//conjunto de kakuros guardados por un usuario
	private Object[][] userValues;
		
	//conjunto de usuarios (cargados de la base de datos)
	private Hashtable<String, String> users = new Hashtable<String, String>();
	private String username = "";	//nombre del usuario que inicia sesion ("Guest" si es invitado)
	private String userOption;	//opcion de GenLoginPage (create, login o delete)
	private String fileKakuro = "";	//path del kakuro activo
	private String originalPath = "";	//path del kakuro original
	
	//parametros del timer (iniciados a 0)
	private Timer timer;
	private byte seconds = 0; 
	private short minutes = 0; 
    private byte centiseconds = 0; 
    private JLabel timeLabel;
	
    //inicializacion de la capa de presentación
	private CtrlPresentation(){
		//preparamos frame
		frmMain = new JFrame();
		//no redimensionable
		frmMain.setResizable(false);
		//dimensiones prefijadas
		frmMain.getContentPane().setPreferredSize(new Dimension(550, 360));
		//titulo de la página
		frmMain.setTitle("Kakuro game");
		frmMain.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		//definimos la acción al darle a la 'X' de cerrar pestaña
		frmMain.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				boolean running = timer.isRunning();
				//paramos el timer si estaba corriendo
				if(running) timer.stop();
				//preguntamos si se quiere cerrar
				int choice = JOptionPane.showConfirmDialog(null,"Are you sure you want to close the game?", "Close Window",JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE);
				switch (choice) {
					//caso de que si se quiera cerrar
					case JOptionPane.YES_OPTION:
						if(!username.equals("Guest") && !originalPath.equals("")) {
							//si el usuario esta registrado preguntamos para guardar el archivo
							int option = JOptionPane.showConfirmDialog(null, 
						            "Do you want to save the kakuro selected before closing?", "Close Window", 
						            JOptionPane.YES_NO_OPTION,
						            JOptionPane.QUESTION_MESSAGE);
							//si lo quiere guardar, lo guardamos en la carpeta de partidas guardadas
							if(option == JOptionPane.YES_OPTION) {
								if(fileKakuro.contains("resources/savedGames") && originalPath!="") CtrlDomini.writeKakuro(fileKakuro);
								else CtrlDomini.addSavedGames(username);
								System.exit(0);
							}
							else if(option == JOptionPane.NO_OPTION) System.exit(0);
						}
						System.exit(0);
						break;
					//caso de que no quiera cerrar, encendemos el timer de nuevo
					case JOptionPane.NO_OPTION:
						if(running) timer.start();
						break;
					
					default:
						break;
				}
			}
		});
		
		//conseguimos las medidas de la pantalla del usuario
		Toolkit myScreen = Toolkit.getDefaultToolkit();
		Dimension myScreenSize = myScreen.getScreenSize();
		
		//tamano y posicion de la pantalla
		int screenHeight = myScreenSize.height;
		int screenWidth = myScreenSize.width;
		//centramos la aplicación en la pantalla del usuario
		frmMain.setBounds(screenWidth/4, screenHeight/4, 550, 360);
		
		//icono
		Image myIcon = myScreen.getImage("resources/icono.png");
		frmMain.setIconImage(myIcon);
	
		//leemos usuarios 
		CtrlDomini.readUsers(users);
	    
		cl = new CardLayout();
	    background = new JPanel(cl);
	    
	    //inicializamos el repositorio de kakuros del sistema (no tiene cambios asi que se puede iniciar al principio)
	    CtrlDomini.readSystemRepository(systemKakurosNoModify);
	    
	    //iniciamos pantallas basicas
	    inPresentationPage();
	    inInitialPage();
	    inGenLoginPage();
	    
	    //inicializamos el timer
		 timer = new Timer(9, new ActionListener() {
	          @Override
	          public void actionPerformed(ActionEvent e) {
	          	centiseconds++;
	              if (centiseconds == 100) {
	                  centiseconds = 0;
	                  seconds += 1; 
	              } else if(seconds == 60) {
	              	minutes += 1;
	              	seconds = 0;
	              }
	              DecimalFormat timeFormatter = new DecimalFormat("00");
	              timeLabel.setText(timeFormatter.format(minutes)+":"+ timeFormatter.format(seconds)+"."+timeFormatter.format(centiseconds));
	          }
	      });
		
	    //iniciamos app
	    frmMain.getContentPane().add(background);
	    cl.show(background, "PresentationPage");
	    frmMain.pack();
	}

	//iniciamos la pagina de presentacion
	private void inPresentationPage() {
		//creamos el JPanel de la página y la inicializamos
		presentationPage = new JPanel();
	    SpringLayout sl_presentationPage = new SpringLayout();
	    presentationPage.setLayout(sl_presentationPage);
	    background.add(presentationPage,"PresentationPage");
	    
	    //boton de start
	    JButton startButton = new JButton("Start");
	    sl_presentationPage.putConstraint(SpringLayout.NORTH, startButton, 300, SpringLayout.NORTH, presentationPage);
	    sl_presentationPage.putConstraint(SpringLayout.WEST, startButton, 208, SpringLayout.WEST, presentationPage);
	    sl_presentationPage.putConstraint(SpringLayout.SOUTH, startButton, -10, SpringLayout.SOUTH, presentationPage);
	    sl_presentationPage.putConstraint(SpringLayout.EAST, startButton, -208, SpringLayout.EAST, presentationPage);
	    startButton.setFont(new Font("Lucida Grande", Font.PLAIN, 17));
	    presentationPage.add(startButton);
	    //cuando se le da al boton, nos lleva a la pagina InitialPage
	    startButton.addActionListener(e -> cl.show(background, "InitialPage"));
	    
	    //boton con el logo, con texto vacío
	    JButton creditsButton = new JButton("");
	    sl_presentationPage.putConstraint(SpringLayout.NORTH, creditsButton, 10, SpringLayout.NORTH, presentationPage);
	    sl_presentationPage.putConstraint(SpringLayout.WEST, creditsButton, 55, SpringLayout.WEST, presentationPage);
	    sl_presentationPage.putConstraint(SpringLayout.SOUTH, creditsButton, -5, SpringLayout.NORTH, startButton);
	    sl_presentationPage.putConstraint(SpringLayout.EAST, creditsButton, 495, SpringLayout.WEST, presentationPage);
	    creditsButton.setBorderPainted(false);
	    //añadimos la foto del logo en el botón
	    creditsButton.setIcon(new ImageIcon("resources/image.png"));
	    presentationPage.add(creditsButton);
	    
	    //cuando se le da al boton, nos lleva a la pagina CreditPage (easter egg)
	    creditsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				inCreditPage();
				cl.show(background, "CreditPage");
			}
		});
	    
	}
	
	//iniciamos la pagina de creditos (easter egg)
	private void inCreditPage() {
		//creamos el JPanel de la página y la inicializamos
		creditPage = new JPanel();
	    SpringLayout sl_creditPage = new SpringLayout();
	    creditPage.setLayout(sl_creditPage);
	    background.add(creditPage,"CreditPage");
	    
	    //etiquetas con los créditos
	    JLabel introLbl = new JLabel("This game has been brought to you by:");
	    sl_creditPage.putConstraint(SpringLayout.WEST, introLbl, 23, SpringLayout.WEST, creditPage);
	    introLbl.setFont(new Font("Lucida Grande", Font.PLAIN, 19));
	    creditPage.add(introLbl);
	    
	    JLabel joelLbl = new JLabel("Joel Tejada Sanchez");
	    sl_creditPage.putConstraint(SpringLayout.NORTH, joelLbl, 74, SpringLayout.NORTH, creditPage);
	    sl_creditPage.putConstraint(SpringLayout.SOUTH, introLbl, -23, SpringLayout.NORTH, joelLbl);
	    sl_creditPage.putConstraint(SpringLayout.WEST, joelLbl, 87, SpringLayout.WEST, creditPage);
	    joelLbl.setFont(new Font("Lucida Grande", Font.PLAIN, 17));
	    creditPage.add(joelLbl);
	    
	    JLabel victorLbl = new JLabel("Victor Ripolles Garcia");
	    sl_creditPage.putConstraint(SpringLayout.NORTH, victorLbl, 20, SpringLayout.SOUTH, joelLbl);
	    sl_creditPage.putConstraint(SpringLayout.WEST, victorLbl, 87, SpringLayout.WEST, creditPage);
	    victorLbl.setFont(new Font("Lucida Grande", Font.PLAIN, 17));
	    creditPage.add(victorLbl);
	    
	    JLabel zixuanLbl = new JLabel("Zixuan Sun");
	    sl_creditPage.putConstraint(SpringLayout.NORTH, zixuanLbl, 17, SpringLayout.SOUTH, victorLbl);
	    sl_creditPage.putConstraint(SpringLayout.WEST, zixuanLbl, 0, SpringLayout.WEST, joelLbl);
	    zixuanLbl.setFont(new Font("Lucida Grande", Font.PLAIN, 17));
	    creditPage.add(zixuanLbl);
	    
	    JLabel xavierLbl = new JLabel("Xavier Gervilla Machado");
	    sl_creditPage.putConstraint(SpringLayout.NORTH, xavierLbl, 17, SpringLayout.SOUTH, zixuanLbl);
	    sl_creditPage.putConstraint(SpringLayout.WEST, xavierLbl, 87, SpringLayout.WEST, creditPage);
	    xavierLbl.setFont(new Font("Lucida Grande", Font.PLAIN, 17));
	    creditPage.add(xavierLbl);
	    
	    //boton de play
	    JButton playButton = new JButton("Start playing!");
	    sl_creditPage.putConstraint(SpringLayout.NORTH, playButton, 50, SpringLayout.SOUTH, xavierLbl);
	    sl_creditPage.putConstraint(SpringLayout.WEST, playButton, 172, SpringLayout.WEST, creditPage);
	    sl_creditPage.putConstraint(SpringLayout.SOUTH, playButton, -44, SpringLayout.SOUTH, creditPage);
	    sl_creditPage.putConstraint(SpringLayout.EAST, playButton, 377, SpringLayout.WEST, creditPage);
	    playButton.setFont(new Font("Lucida Grande", Font.PLAIN, 17));
	    creditPage.add(playButton);
	    //cuando se le da a play, nos lleva a la página de inicio
	    playButton.addActionListener(e -> cl.show(background, "InitialPage"));
	    
	}
	
	//inciamos la pagina inicial
	private void inInitialPage() {
		//creamos el JPanel de la pagina y la inicializamos
	    initialPage = new JPanel();
	    SpringLayout sl_initialPage = new SpringLayout();
	    initialPage.setLayout(sl_initialPage);
	    background.add(initialPage,"InitialPage");
	    
	    //boton de opciones de usuario
	    JButton loginButton = new JButton("User options");
	    sl_initialPage.putConstraint(SpringLayout.NORTH, loginButton, 79, SpringLayout.NORTH, initialPage);
	    sl_initialPage.putConstraint(SpringLayout.WEST, loginButton, 158, SpringLayout.WEST, initialPage);
	    sl_initialPage.putConstraint(SpringLayout.EAST, loginButton, -159, SpringLayout.EAST, initialPage);
	    loginButton.setFont(new Font("Lucida Grande", Font.PLAIN, 17));
	    initialPage.add(loginButton);
	    //cuando se da al boton nos lleva a la pagina de opciones de usuario
	    loginButton.addActionListener(e -> cl.show(background, "GenLoginPage"));
		
	    //boton de entrar como invitado
	    JButton guestButton = new JButton("Continue as guest");
	    sl_initialPage.putConstraint(SpringLayout.NORTH, guestButton, 192, SpringLayout.NORTH, initialPage);
	    sl_initialPage.putConstraint(SpringLayout.SOUTH, guestButton, -116, SpringLayout.SOUTH, initialPage);
	    sl_initialPage.putConstraint(SpringLayout.SOUTH, loginButton, -61, SpringLayout.NORTH, guestButton);
	    sl_initialPage.putConstraint(SpringLayout.WEST, guestButton, 158, SpringLayout.WEST, initialPage);
	    sl_initialPage.putConstraint(SpringLayout.EAST, guestButton, -159, SpringLayout.EAST, initialPage);
	    guestButton.setFont(new Font("Lucida Grande", Font.PLAIN, 17));
	    initialPage.add(guestButton);
	    //cuando se le da al boton, nos lleva a la página principal del juego
	  
	    guestButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
					username = "Guest";	//usuario por defecto sistema
					inMainPage();
					cl.show(background, "MainPage");  
				}
		});
	}
	
	//iniciamos la pagina de login general
	private void inGenLoginPage() {
		//creamos el JPanel de la página y la inicializamos
		genLoginPage = new JPanel();
		SpringLayout sl_genLoginPage = new SpringLayout();
		genLoginPage.setLayout(sl_genLoginPage);
		background.add(genLoginPage,"GenLoginPage");
		//boton de atrás
		JButton backButton = new JButton("Back");
		backButton.setFont(new Font("Lucida Grande", Font.PLAIN, 17));
		sl_genLoginPage.putConstraint(SpringLayout.NORTH, backButton, -57, SpringLayout.SOUTH, genLoginPage);
		sl_genLoginPage.putConstraint(SpringLayout.WEST, backButton, -121, SpringLayout.EAST, genLoginPage);
		sl_genLoginPage.putConstraint(SpringLayout.SOUTH, backButton, -10, SpringLayout.SOUTH, genLoginPage);
		sl_genLoginPage.putConstraint(SpringLayout.EAST, backButton, -10, SpringLayout.EAST, genLoginPage);
		genLoginPage.add(backButton);
		//al darle al boton nos lleva a la pagina anterior
		backButton.addActionListener(e -> cl.show(background, "InitialPage"));
		//boton login
		JButton loginButton = new JButton("Log in");
		sl_genLoginPage.putConstraint(SpringLayout.NORTH, loginButton, 88, SpringLayout.NORTH, genLoginPage);
		sl_genLoginPage.putConstraint(SpringLayout.WEST, loginButton, 158, SpringLayout.WEST, genLoginPage);
		sl_genLoginPage.putConstraint(SpringLayout.SOUTH, loginButton, -225, SpringLayout.SOUTH, genLoginPage);
		sl_genLoginPage.putConstraint(SpringLayout.EAST, loginButton, -158, SpringLayout.EAST, genLoginPage);
		loginButton.setFont(new Font("Lucida Grande", Font.PLAIN, 17));
		genLoginPage.add(loginButton);
		//al darle al boton nos lleva a la pagina de inicio de sesion
		loginButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				userOption = "login";
				inUserPage();
				cl.show(background, "UserPage");
			}
		});
		//boton de crear usuario
		JButton createButton = new JButton("Create new user");
		sl_genLoginPage.putConstraint(SpringLayout.NORTH, createButton, 50, SpringLayout.SOUTH, loginButton);
		sl_genLoginPage.putConstraint(SpringLayout.WEST, createButton, 158, SpringLayout.WEST, genLoginPage);
		sl_genLoginPage.putConstraint(SpringLayout.SOUTH, createButton, -128, SpringLayout.SOUTH, genLoginPage);
		sl_genLoginPage.putConstraint(SpringLayout.EAST, createButton, -158, SpringLayout.EAST, genLoginPage);
		createButton.setFont(new Font("Lucida Grande", Font.PLAIN, 17));
		genLoginPage.add(createButton);
		//al darle al boton nos lleva a la pagina de crear usuario
		createButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				userOption = "create";
				inUserPage();
				cl.show(background, "UserPage");
			}
		});
		
	}
	
	//iniciamos la pagina de user (log in, create or delete user)
	private void inUserPage() {
		//creamos el JPanel de la página y la inicializamos
		//esta página se reutiliza para los casos de log in y create user
		userPage = new JPanel();
		background.add(userPage,"UserPage");
		SpringLayout sl_userPage = new SpringLayout();
		userPage.setLayout(sl_userPage);
		//etiqueta username
		JLabel usernameLbl = new JLabel("Username");
		sl_userPage.putConstraint(SpringLayout.SOUTH, usernameLbl, -269, SpringLayout.SOUTH, userPage);
		usernameLbl.setFont(new Font("Lucida Grande", Font.PLAIN, 17));
		userPage.add(usernameLbl);
		//etiqueta password
		JLabel passwordLbl = new JLabel("Password");
		sl_userPage.putConstraint(SpringLayout.NORTH, passwordLbl, 137, SpringLayout.NORTH, userPage);
		sl_userPage.putConstraint(SpringLayout.WEST, usernameLbl, 0, SpringLayout.WEST, passwordLbl);
		passwordLbl.setFont(new Font("Lucida Grande", Font.PLAIN, 17));
		userPage.add(passwordLbl);
		//textfield de password
		JPasswordField passwordField = new JPasswordField();
		sl_userPage.putConstraint(SpringLayout.NORTH, passwordField, 132, SpringLayout.NORTH, userPage);
		sl_userPage.putConstraint(SpringLayout.WEST, passwordField, 264, SpringLayout.WEST, userPage);
		sl_userPage.putConstraint(SpringLayout.EAST, passwordField, -116, SpringLayout.EAST, userPage);
		sl_userPage.putConstraint(SpringLayout.EAST, passwordLbl, -85, SpringLayout.WEST, passwordField);
		passwordField.setFont(new Font("Lucida Grande", Font.PLAIN, 17));
		userPage.add(passwordField);
		//textfield de username
		JTextField userField = new JTextField();
		sl_userPage.putConstraint(SpringLayout.NORTH, userField, -5, SpringLayout.NORTH, usernameLbl);
		sl_userPage.putConstraint(SpringLayout.WEST, userField, 0, SpringLayout.WEST, passwordField);
		userField.setFont(new Font("Lucida Grande", Font.PLAIN, 17));
		userPage.add(userField);
		userField.setColumns(10);
		
		//main button, este boton cambia dependiendo de la opción que se ha escogido
		JButton userButton = new JButton("Log in");
		sl_userPage.putConstraint(SpringLayout.NORTH, userButton, 43, SpringLayout.SOUTH, passwordField);
		sl_userPage.putConstraint(SpringLayout.WEST, userButton, 170, SpringLayout.WEST, userPage);
		sl_userPage.putConstraint(SpringLayout.SOUTH, userButton, -105, SpringLayout.SOUTH, userPage);
		sl_userPage.putConstraint(SpringLayout.EAST, userButton, -171, SpringLayout.EAST, userPage);
		userButton.setFont(new Font("Lucida Grande", Font.PLAIN, 17));
		userPage.add(userButton);
		//caso en que se ha escogido create user
		if(userOption == "create") {
			userButton.setText("Create user");
			userButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					String name = userField.getText();
					@SuppressWarnings("deprecation")
					String password = passwordField.getText();
					//usuario ya existente
					if(users.containsKey(name))	JOptionPane.showMessageDialog(userPage, "This username is already used", "", JOptionPane.WARNING_MESSAGE);
					//no se ha introducido ni usuario ni contraseña
					else if(password.isEmpty() && name.isEmpty()) JOptionPane.showMessageDialog(userPage, "Enter a username and a password", "", JOptionPane.WARNING_MESSAGE);
					//no se ha introducido contraseña
					else if(password.isEmpty()) JOptionPane.showMessageDialog(userPage, "Enter a password", "", JOptionPane.WARNING_MESSAGE);
					//usuario "Guest" no es valido
					else if(name == "Guest") JOptionPane.showMessageDialog(userPage, "Guest is not a valid username, try with a different one.", "", JOptionPane.WARNING_MESSAGE);
					//guardamos en nuestra "base de datos" los datos introducidos
					else {
						username = name;
						inMainPage();
						users.put(name, password);
						//anadimos la entrada al ranking
						CtrlDomini.addRankingEntry(username);
						//actualizamos el conjunto de usuarios
						CtrlDomini.updateUsers(users);
						passwordField.setText("");
						userField.setText("");
						
						cl.show(background, "MainPage"); 
					}
				}
			});
		}
		//caso login
		else {	
			userButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					String name = userField.getText();
					@SuppressWarnings("deprecation")
					String password = passwordField.getText();
					//usuario no existe
					if(!users.containsKey(name)) JOptionPane.showMessageDialog(userPage, "There's no user with that username", "", JOptionPane.WARNING_MESSAGE);
					//contraseña no es correcta
					else if(!password.equals(users.get(name))) JOptionPane.showMessageDialog(userPage, "The password is incorrect", "", JOptionPane.WARNING_MESSAGE);
					else{
						username = name;
						inMainPage();
						passwordField.setText("");
						userField.setText("");
						
						cl.show(background, "MainPage");  
					}
				}
			});
		}
		
		//boton atras
		JButton backButton = new JButton("Back");
		backButton.setFont(new Font("Lucida Grande", Font.PLAIN, 17));
		sl_userPage.putConstraint(SpringLayout.NORTH, backButton, -57, SpringLayout.SOUTH, userPage);
		sl_userPage.putConstraint(SpringLayout.WEST, backButton, -121, SpringLayout.EAST, userPage);
		sl_userPage.putConstraint(SpringLayout.SOUTH, backButton, -10, SpringLayout.SOUTH, userPage);
		sl_userPage.putConstraint(SpringLayout.EAST, backButton, -10, SpringLayout.EAST, userPage);
		userPage.add(backButton);
		
		backButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				passwordField.setText("");
				userField.setText("");
				cl.show(background, "GenLoginPage");
			}
		});
	}
	
	//pagina principal de juego
	private void inMainPage() {
		//creamos el JPanel de la pagina y la inicializamos
		mainPage = new JPanel();
		SpringLayout sl_mainPage = new SpringLayout();
		mainPage.setLayout(sl_mainPage);
		background.add(mainPage,"MainPage");
		    
		//boton play
		JButton playButton = new JButton("Play");
		sl_mainPage.putConstraint(SpringLayout.NORTH, playButton, 71, SpringLayout.NORTH, mainPage);
		sl_mainPage.putConstraint(SpringLayout.WEST, playButton, 158, SpringLayout.WEST, mainPage);
		sl_mainPage.putConstraint(SpringLayout.SOUTH, playButton, -237, SpringLayout.SOUTH, mainPage);
		sl_mainPage.putConstraint(SpringLayout.EAST, playButton, 391, SpringLayout.WEST, mainPage);
		playButton.setFont(new Font("Lucida Grande", Font.PLAIN, 17));
		mainPage.add(playButton);
		playButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				inPlayPage();
				cl.show(background, "PlayPage");
			}
		});
		//boton ranking
		JButton rankingButton = new JButton("Ranking");
		sl_mainPage.putConstraint(SpringLayout.NORTH, rankingButton, 59, SpringLayout.SOUTH, playButton);
		sl_mainPage.putConstraint(SpringLayout.WEST, rankingButton, 158, SpringLayout.WEST, mainPage);
		sl_mainPage.putConstraint(SpringLayout.SOUTH, rankingButton, -126, SpringLayout.SOUTH, mainPage);
		sl_mainPage.putConstraint(SpringLayout.EAST, rankingButton, -159, SpringLayout.EAST, mainPage);
		rankingButton.setFont(new Font("Lucida Grande", Font.PLAIN, 17));
		mainPage.add(rankingButton);
		rankingButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				inRankingPage();
				cl.show(background, "RankingPage");
			}
		});
		//boton que dependiendo de como se haya iniciado es diferente
		JButton logoutButton = new JButton("Log out");
		logoutButton.setFont(new Font("Lucida Grande", Font.PLAIN, 17));
		sl_mainPage.putConstraint(SpringLayout.NORTH, logoutButton, -57, SpringLayout.SOUTH, mainPage);
		sl_mainPage.putConstraint(SpringLayout.WEST, logoutButton, -121, SpringLayout.EAST, mainPage);
		sl_mainPage.putConstraint(SpringLayout.SOUTH, logoutButton, -10, SpringLayout.SOUTH, mainPage);
		sl_mainPage.putConstraint(SpringLayout.EAST, logoutButton, -10, SpringLayout.EAST, mainPage);
		mainPage.add(logoutButton);
		//caso de entrar como invitado el boton es de log in
		if(username=="Guest") {
			logoutButton.setText("Log in");
			logoutButton.addActionListener(e -> cl.show(background, "GenLoginPage"));
		}
		else logoutButton.addActionListener(e -> cl.show(background, "InitialPage"));
		
		if(username != "Guest") {	//si no es invitado anadimos el boton delete
			JButton deleteButton = new JButton("Delete");
			sl_mainPage.putConstraint(SpringLayout.NORTH, deleteButton, 0, SpringLayout.NORTH, logoutButton);
			sl_mainPage.putConstraint(SpringLayout.WEST, deleteButton, 10, SpringLayout.WEST, mainPage);
			sl_mainPage.putConstraint(SpringLayout.SOUTH, deleteButton, -10, SpringLayout.SOUTH, mainPage);
			sl_mainPage.putConstraint(SpringLayout.EAST, deleteButton, 121, SpringLayout.WEST, mainPage);
			deleteButton.setFont(new Font("Lucida Grande", Font.PLAIN, 17));
			mainPage.add(deleteButton);
			deleteButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Object[] options = {"Yes", "No"};
					int choice = JOptionPane.showOptionDialog(background, "Would you like to delete this user?", "Delete confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
					if(choice == JOptionPane.YES_OPTION) {
						//lo eliminamos de donde corresponde
						users.remove(username);	//conjunto de usuarios
						CtrlDomini.removeRankingEntry(username);	//actualizamos ranking.txt
						CtrlDomini.updateUsers(users);			//actualizamos users.txt
						CtrlDomini.deleteRepoUser(username);	//eliminamos sus entradas del repositorio
						CtrlDomini.deleteSavedGames(username);	//eliminamos sus entradas de partidas guardadas
						
						
						JOptionPane.showMessageDialog(userPage, "The user: "+username+" has been removed", "", JOptionPane.INFORMATION_MESSAGE);
						username = "";	//vaciamos username por si acaso
						cl.show(background, "GenLoginPage");
					}
				}
			});
		}
		
	}

	//pagina de ranking
	private void inRankingPage() {
		//creamos el JPanel de la pagina y la inicializa
		rankingPage = new JPanel();
		background.add(rankingPage,"RankingPage");
		SpringLayout sl_rankingPage = new SpringLayout();
		rankingPage.setLayout(sl_rankingPage);
		
		//boton atrás
		JButton backButton = new JButton("Back");
		backButton.setFont(new Font("Lucida Grande", Font.PLAIN, 17));
		sl_rankingPage.putConstraint(SpringLayout.NORTH, backButton, -64, SpringLayout.SOUTH, rankingPage);
		sl_rankingPage.putConstraint(SpringLayout.WEST, backButton, -127, SpringLayout.EAST, rankingPage);
		sl_rankingPage.putConstraint(SpringLayout.SOUTH, backButton, -10, SpringLayout.SOUTH, rankingPage);
		sl_rankingPage.putConstraint(SpringLayout.EAST, backButton, -10, SpringLayout.EAST, rankingPage);
		rankingPage.add(backButton);
		backButton.addActionListener(e -> cl.show(background, "MainPage"));
		//array con los nombres de las columnas del ranking
		String[] columnNames = {"Position", "Username", "Total points", "Hard wins", "Medium wins", "Easy wins"};
		//leemos los valores del ranking
		userValues = new Object[users.size()][6]; 
		CtrlDomini.readRanking(userValues, users.size());
		//inicializa correctamente el ranking
		// representamos el ranking en una tabla
		JTable ranking = new JTable(userValues, columnNames);
		
		//centrar elementos de la tabla
		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
		ranking.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
		ranking.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
		ranking.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
		ranking.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
		ranking.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
		ranking.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
		ranking.setBorder(null);
		ranking.setFont(new Font("Lucida Grande", Font.PLAIN, 14));
		ranking.setBackground(new Color(204, 204, 204));
		ranking.setFillsViewportHeight(true);
		//no permitir modificar valores de la tabla
		ranking.setEnabled(false);
		//no mover columnas
		ranking.getTableHeader().setReorderingAllowed(false);
		//añadimos una scrollPane para en el caso de que haya muchos usuarios se permita hacer scroll hacia abajo
		JScrollPane scrollPane = new JScrollPane(ranking);
		sl_rankingPage.putConstraint(SpringLayout.NORTH, scrollPane, 10, SpringLayout.NORTH, rankingPage);
		sl_rankingPage.putConstraint(SpringLayout.SOUTH, scrollPane, -86, SpringLayout.SOUTH, rankingPage);
		sl_rankingPage.putConstraint(SpringLayout.EAST, scrollPane, -10, SpringLayout.EAST, rankingPage);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		sl_rankingPage.putConstraint(SpringLayout.WEST, scrollPane, 10, SpringLayout.WEST, rankingPage);
		rankingPage.add(scrollPane);
			
		}
	
	//pagina de seleccion de modo de juego
	private void inPlayPage() {
		//creamos el JPanel de la pagina y la inicializamos
		playPage = new JPanel();
		background.add(playPage,"PlayPage");
		SpringLayout sl_playPage = new SpringLayout();
		playPage.setLayout(sl_playPage);
		//boton back
		JButton backButton = new JButton("Back");
		backButton.setFont(new Font("Lucida Grande", Font.PLAIN, 17));
		sl_playPage.putConstraint(SpringLayout.NORTH, backButton, -60, SpringLayout.SOUTH, playPage);
		sl_playPage.putConstraint(SpringLayout.WEST, backButton, -121, SpringLayout.EAST, playPage);
		sl_playPage.putConstraint(SpringLayout.SOUTH, backButton, -10, SpringLayout.SOUTH, playPage);
		sl_playPage.putConstraint(SpringLayout.EAST, backButton, -10, SpringLayout.EAST, playPage);
		playPage.add(backButton);
		backButton.addActionListener(e -> cl.show(background, "MainPage"));
		
		//etiqueta que muestra el mensaje
		JLabel optionsLbl = new JLabel("Choose an option to start playing");
		sl_playPage.putConstraint(SpringLayout.NORTH, optionsLbl, 25, SpringLayout.NORTH, playPage);
		sl_playPage.putConstraint(SpringLayout.WEST, optionsLbl, 29, SpringLayout.WEST, playPage);
		optionsLbl.setFont(new Font("Lucida Grande", Font.PLAIN, 17));
		playPage.add(optionsLbl);
		//boton generar kakuro
		JButton generateButton = new JButton("Generate Kakuro");
		sl_playPage.putConstraint(SpringLayout.NORTH, generateButton, -292, SpringLayout.SOUTH, playPage);
		sl_playPage.putConstraint(SpringLayout.WEST, generateButton, -412, SpringLayout.EAST, playPage);
		sl_playPage.putConstraint(SpringLayout.SOUTH, generateButton, -242, SpringLayout.SOUTH, playPage);
		sl_playPage.putConstraint(SpringLayout.EAST, generateButton, -139, SpringLayout.EAST, playPage);
		generateButton.setFont(new Font("Lucida Grande", Font.PLAIN, 17));
		playPage.add(generateButton);
		generateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				inGeneratePage();
				cl.show(background, "GeneratePage");
			}
		});
		//boton introducir kakuro desde fichero
		JButton inputButton = new JButton("Introduce from file");
		sl_playPage.putConstraint(SpringLayout.NORTH, inputButton, 124, SpringLayout.NORTH, playPage);
		sl_playPage.putConstraint(SpringLayout.WEST, inputButton, 138, SpringLayout.WEST, playPage);
		sl_playPage.putConstraint(SpringLayout.EAST, inputButton, -139, SpringLayout.EAST, playPage);
		inputButton.setFont(new Font("Lucida Grande", Font.PLAIN, 17));
		playPage.add(inputButton);
		inputButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				inIntroducePage();
				cl.show(background, "IntroducePage");
			}
		});
		//boton para continuar partida
		JButton resumeButton = new JButton("Resume game");
		sl_playPage.putConstraint(SpringLayout.SOUTH, inputButton, -6, SpringLayout.NORTH, resumeButton);
		sl_playPage.putConstraint(SpringLayout.NORTH, resumeButton, 180, SpringLayout.NORTH, playPage);
		sl_playPage.putConstraint(SpringLayout.WEST, resumeButton, 138, SpringLayout.WEST, playPage);
		sl_playPage.putConstraint(SpringLayout.SOUTH, resumeButton, -130, SpringLayout.SOUTH, playPage);
		sl_playPage.putConstraint(SpringLayout.EAST, resumeButton, -139, SpringLayout.EAST, playPage);
		resumeButton.setFont(new Font("Lucida Grande", Font.PLAIN, 17));
		playPage.add(resumeButton);
		resumeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//si el usuario es invitado, se le dice que no puede
				if(username == "Guest") {
					JOptionPane.showMessageDialog(playPage, "A guest can't have saved games", "", JOptionPane.INFORMATION_MESSAGE);
				}
				else {
					//iniciamos el objeto contenedor -> si el usuario no tiene partidas guardadas salta popup
					int line = CtrlDomini.howManySaved(username);
					if(line == 0) JOptionPane.showMessageDialog(playPage, "This user has no saved games", "", JOptionPane.INFORMATION_MESSAGE);
					else{
						userSaved = new Object[line][3];
						CtrlDomini.readSavedGames(userSaved, username);
						inResumePage();
						cl.show(background, "ResumePage");
					}
				}
			}
		});
		//boton entrar al repositorio
		JButton repositoryButton = new JButton("Enter repository");
		sl_playPage.putConstraint(SpringLayout.NORTH, repositoryButton, 6, SpringLayout.SOUTH, resumeButton);
		sl_playPage.putConstraint(SpringLayout.WEST, repositoryButton, 138, SpringLayout.WEST, playPage);
		sl_playPage.putConstraint(SpringLayout.SOUTH, repositoryButton, -74, SpringLayout.SOUTH, playPage);
		sl_playPage.putConstraint(SpringLayout.EAST, repositoryButton, -139, SpringLayout.EAST, playPage);
		repositoryButton.setFont(new Font("Lucida Grande", Font.PLAIN, 17));
		playPage.add(repositoryButton);
		repositoryButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				inRepositoryPage();
				cl.show(background, "RepositoryPage");
			}
		});
		
	}
	
	//pagina de seleccion de repositorio
	private void inRepositoryPage() {
		//creamos el JPanel de la pagina y la inicializamos
		repositoryPage = new JPanel();
		background.add(repositoryPage,"RepositoryPage");
		SpringLayout sl_repositoryPage = new SpringLayout();
		repositoryPage.setLayout(sl_repositoryPage);
		
		//boton de repositorio de sistema
		JButton sistemButton = new JButton("System repository");
		sl_repositoryPage.putConstraint(SpringLayout.NORTH, sistemButton, 76, SpringLayout.NORTH, repositoryPage);
		sl_repositoryPage.putConstraint(SpringLayout.WEST, sistemButton, 160, SpringLayout.WEST, repositoryPage);
		sl_repositoryPage.putConstraint(SpringLayout.SOUTH, sistemButton, -234, SpringLayout.SOUTH, repositoryPage);
		sl_repositoryPage.putConstraint(SpringLayout.EAST, sistemButton, -160, SpringLayout.EAST, repositoryPage);
		sistemButton.setFont(new Font("Lucida Grande", Font.PLAIN, 17));
		repositoryPage.add(sistemButton);
		sistemButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//llamamos a la funcion de repositorio de sistema con la matriz de kakuros inicial y "sin filtrar"
				inSystemRepoPage(systemKakurosNoModify, "No Filter");
				cl.show(background, "SystemRepoPage");
			}
		});
		
		//repositorio de los kakuros guardados por todos los usuarios
		JButton userButton = new JButton("Users repository");
		sl_repositoryPage.putConstraint(SpringLayout.NORTH, userButton, 78, SpringLayout.SOUTH, sistemButton);
		sl_repositoryPage.putConstraint(SpringLayout.WEST, userButton, 160, SpringLayout.WEST, repositoryPage);
		sl_repositoryPage.putConstraint(SpringLayout.SOUTH, userButton, -106, SpringLayout.SOUTH, repositoryPage);
		sl_repositoryPage.putConstraint(SpringLayout.EAST, userButton, -160, SpringLayout.EAST, repositoryPage);
		userButton.setFont(new Font("Lucida Grande", Font.PLAIN, 17));
		repositoryPage.add(userButton);
		userButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//miramos cuántos kakuros hay guardados en el repostorio de usuarios
				int lines2 = CtrlDomini.howManyLines("users");
				usersKakurosNoModify = new Object[lines2][4];
				//guardamos en una matriz los kakuros del repositorio de usuario
				CtrlDomini.readUsersRepository(usersKakurosNoModify);
				//llamamos a la funcion de repositorio de usuarios con la matriz de kakuros y "sin filtrar"
				inUserRepoPage(usersKakurosNoModify,"No Filter");
				cl.show(background, "UserRepoPage");
			}
		});
		
		//boton atrás
		JButton backButton = new JButton("Back");
		backButton.setFont(new Font("Lucida Grande", Font.PLAIN, 17));
		sl_repositoryPage.putConstraint(SpringLayout.NORTH, backButton, -60, SpringLayout.SOUTH, repositoryPage);
		sl_repositoryPage.putConstraint(SpringLayout.WEST, backButton, -121, SpringLayout.EAST, repositoryPage);
		sl_repositoryPage.putConstraint(SpringLayout.SOUTH, backButton, -10, SpringLayout.SOUTH, repositoryPage);
		sl_repositoryPage.putConstraint(SpringLayout.EAST, backButton, -10, SpringLayout.EAST, repositoryPage);
		repositoryPage.add(backButton);
		backButton.addActionListener(e -> cl.show(background, "PlayPage"));
	}
	
	//repositorio del sistema
	private void inSystemRepoPage(Object[][] systemKakuros, String selectedOpt) {
		//creamos el JPanel de la pagina y la inicializamos
		systemRepoPage = new JPanel();
		background.add(systemRepoPage,"SystemRepoPage");
		SpringLayout sl_systemRepoPage = new SpringLayout();
		systemRepoPage.setLayout(sl_systemRepoPage);
		//etiqueta que informa de lo que hay que hacer
		JLabel titleLbl = new JLabel("Choose a Kakuro from the system repository");
		titleLbl.setFont(new Font("Lucida Grande", Font.PLAIN, 17));
		sl_systemRepoPage.putConstraint(SpringLayout.NORTH, titleLbl, 30, SpringLayout.NORTH, systemRepoPage);
		sl_systemRepoPage.putConstraint(SpringLayout.WEST, titleLbl, 30, SpringLayout.WEST, systemRepoPage);
		systemRepoPage.add(titleLbl);
		//boton atras
		JButton backButton = new JButton("Back");
		backButton.setFont(new Font("Lucida Grande", Font.PLAIN, 17));
		sl_systemRepoPage.putConstraint(SpringLayout.NORTH, backButton, -60, SpringLayout.SOUTH, systemRepoPage);
		sl_systemRepoPage.putConstraint(SpringLayout.WEST, backButton, -121, SpringLayout.EAST, systemRepoPage);
		sl_systemRepoPage.putConstraint(SpringLayout.SOUTH, backButton, -10, SpringLayout.SOUTH, systemRepoPage);
		sl_systemRepoPage.putConstraint(SpringLayout.EAST, backButton, -10, SpringLayout.EAST, systemRepoPage);
		systemRepoPage.add(backButton);
		backButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fileKakuro = originalPath = "";
				cl.show(background, "RepositoryPage");
			}
		});
		
		//array con las cabeceras de la tabla
		String[] columnNames = {"Size", "Difficulty", "id"};
		
		//creamos una tabla con las cabeceras y los kakuros que hemos obtenido previamente
		JTable systemRepo = new JTable(systemKakuros, columnNames);
		systemRepo.setFillsViewportHeight(true);
		systemRepo.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		//centrar elementos de la tabla
		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
		systemRepo.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
		systemRepo.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
		systemRepo.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
		systemRepo.setBorder(null);
		//no modificar las celdas
		JTextField tf = new JTextField();
		tf.setEditable(false);
		DefaultCellEditor editor = new DefaultCellEditor( tf );
		systemRepo.setDefaultEditor(Object.class, editor);
		//no mover columnas
		systemRepo.getTableHeader().setReorderingAllowed(false);
		//seleccionar fila entera
		systemRepo.setRowSelectionAllowed(true);
		systemRepo.setFont(new Font("Lucida Grande", Font.PLAIN, 14));
		systemRepo.setBackground(new Color(204, 204, 204));
		//creamos una scrollpane para en el caso de que hayan muchos kakuros en el repositorio se permita hacer scroll para  verlos todos
		JScrollPane scrollPane = new JScrollPane(systemRepo);
		scrollPane.setEnabled(false);
		scrollPane.setViewportBorder(null);
		sl_systemRepoPage.putConstraint(SpringLayout.NORTH, scrollPane, 21, SpringLayout.SOUTH, titleLbl);
		sl_systemRepoPage.putConstraint(SpringLayout.WEST, scrollPane, 44, SpringLayout.WEST, systemRepoPage);
		sl_systemRepoPage.putConstraint(SpringLayout.SOUTH, scrollPane, -12, SpringLayout.NORTH, backButton);
		sl_systemRepoPage.putConstraint(SpringLayout.EAST, scrollPane, -44, SpringLayout.EAST, systemRepoPage);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		systemRepoPage.add(scrollPane);
		//filtro con 3 opciones: sin filtro, tamaño y dificultad
		JComboBox<String> comboBox = new JComboBox<String>();
		comboBox.addItem("No Filter");
		comboBox.addItem("Size");
		comboBox.addItem("Difficulty");
		comboBox.setSelectedItem(selectedOpt);
		sl_systemRepoPage.putConstraint(SpringLayout.NORTH, comboBox, 3, SpringLayout.NORTH, titleLbl);
		sl_systemRepoPage.putConstraint(SpringLayout.WEST, comboBox, 6, SpringLayout.EAST, titleLbl);
		sl_systemRepoPage.putConstraint(SpringLayout.EAST, comboBox, 120, SpringLayout.EAST, titleLbl);
		comboBox.addActionListener(new ActionListener() {
			   @Override
			   public void actionPerformed(ActionEvent e) {
				   String selectedOption = (String)comboBox.getSelectedItem();
				   //se escoge filtrar por tamaño
				   if ("Size".equals(selectedOption)) {
					   //se pide un tamaño correcto
					   String inputSize = JOptionPane.showInputDialog(systemRepoPage, "Enter an available size with format '4x4'", "", JOptionPane.PLAIN_MESSAGE);
					   //creamos una matriz auxiliar vacía con el tamaño vertical de la matriz original (en java no se puede asignar dinámicamente tamaño a las matrices)
					   Object[][] systemKakurosSizeAux = new Object[Array.getLength(systemKakurosNoModify)][3];
					   int j = 0;
					   //nos quedamos con todos los kakuros que sean de ese tamaño
					   for(int i = 0; i < Array.getLength(systemKakurosNoModify); ++i) {
						  if(systemKakurosNoModify[i][0].equals(inputSize)) {
							   systemKakurosSizeAux[j][0] = systemKakurosNoModify[i][0];
							   systemKakurosSizeAux[j][1] = systemKakurosNoModify[i][1];
							   systemKakurosSizeAux[j][2] = systemKakurosNoModify[i][2];
							   ++j; 
						  }
					   }
					   //creamos una nueva matriz con el tamaño vertical de entradas que hemos encontrado
					   //introducimos en la nueva matriz los valores encontrados, esto lo hacemos para que no haya entradas vacías en el repositorio
					   Object[][] systemKakurosSize = new Object[j][3];
					   int k = 0;
					   for(int i = 0; i < j; ++i) {
						   systemKakurosSize[k][0] = systemKakurosSizeAux[i][0];
						   systemKakurosSize[k][1] = systemKakurosSizeAux[i][1];
						   systemKakurosSize[k][2] = systemKakurosSizeAux[i][2];
						   k++;
					   }
					   inSystemRepoPage(systemKakurosSize, selectedOption);
					   cl.show(background, "SystemRepoPage");
				   }
				   //caso de filtrado por dificultad
				   else if("Difficulty".equals(selectedOption)) {
					   //pedimos una dificultad
					   String inputDiff = JOptionPane.showInputDialog(systemRepoPage, "Enter a correct difficulty", "", JOptionPane.PLAIN_MESSAGE);
					   //creamos una matriz auxiliar vacía con el tamaño vertical de la matriz original (en java no se puede asignar dinámicamente tamaño a las matrices)
					   Object[][] systemKakurosDiffAux = new Object[Array.getLength(systemKakurosNoModify)][3];
					   int j = 0;
					   //nos quedamos con todos los kakuros que sean de esa dificultad
					   for(int i = 0; i < Array.getLength(systemKakurosNoModify); ++i) {
						  if(systemKakurosNoModify[i][1].equals(inputDiff)) {
							   systemKakurosDiffAux[j][0] = systemKakurosNoModify[i][0];
							   systemKakurosDiffAux[j][1] = systemKakurosNoModify[i][1];
							   systemKakurosDiffAux[j][2] = systemKakurosNoModify[i][2];
							   ++j; 
						  }
					   }
					   //creamos una nueva matriz con el tamaño vertical de entradas que hemos encontrado
					   //introducimos en la nueva matriz los valores encontrados, esto lo hacemos para que no haya entradas vacías en el repositorio
					   Object[][] systemKakurosDiff = new Object[j][3];
					   int k = 0;
					   for(int i = 0; i < j; ++i) {
						   systemKakurosDiff[k][0] = systemKakurosDiffAux[i][0];
						   systemKakurosDiff[k][1] = systemKakurosDiffAux[i][1];
						   systemKakurosDiff[k][2] = systemKakurosDiffAux[i][2];
						   k++;
					   }
					   inSystemRepoPage(systemKakurosDiff, selectedOption);
					   cl.show(background, "SystemRepoPage");
				   }
				   //caso de no filtrado, se muestra el repositorio entero
				   else if("No Filter".equals(selectedOption)) {
					   Object[][] systemKakurosAux = new Object[Array.getLength(systemKakurosNoModify)][3];
					   CtrlDomini.readSystemRepository(systemKakurosAux);
					   inSystemRepoPage(systemKakurosAux, selectedOption);
					   cl.show(background, "SystemRepoPage");
				   }
			   }
		});
		systemRepoPage.add(comboBox);
		
		systemRepo.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
				//nos quedamos con la fila de la casilla seleccionada
	            public void mousePressed(java.awt.event.MouseEvent evt) {
				String titleTxt = "";
	            	int row = systemRepo.getSelectedRow();
	            	if(row!=-1) {
	            		Object value = systemRepo.getModel().getValueAt(row, 0);
		            	if(value != null) {
		            		String size = value.toString();
		            		String difficulty = systemRepo.getModel().getValueAt(row, 1).toString();
		            		int id = Integer.parseInt(systemRepo.getModel().getValueAt(row, 2).toString());
		            		//cogemos el titulo del txt con el kakuro que hay que leer
		            		if(id != 0) {
		            			titleTxt +=size+"-"+difficulty+id+".txt";
		            		}
		            		else titleTxt +=size+"-"+difficulty+".txt";
		            		fileKakuro = originalPath = "resources/systemRepository/"+titleTxt;
		            	} 
	            	}           	
	            }
	    });
		//boton de cargar kakuro
		JButton loadButton = new JButton("Load kakuro");
		sl_systemRepoPage.putConstraint(SpringLayout.NORTH, loadButton, 0, SpringLayout.NORTH, backButton);
		sl_systemRepoPage.putConstraint(SpringLayout.WEST, loadButton, 116, SpringLayout.WEST, systemRepoPage);
		sl_systemRepoPage.putConstraint(SpringLayout.SOUTH, loadButton, 0, SpringLayout.SOUTH, backButton);
		sl_systemRepoPage.putConstraint(SpringLayout.EAST, loadButton, -120, SpringLayout.WEST, backButton);
		loadButton.setFont(new Font("Lucida Grande", Font.PLAIN, 17));
		systemRepoPage.add(loadButton);
		
		loadButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//si tiene seleccionada una casilla, antes de cargarlo, preguntamos si quiere cargar ese kakuro
				if(fileKakuro!="") {
					JPanel panAux = new JPanel();
					panAux.add(new JLabel("Would you like to load this kakuro?"));
					int choice = JOptionPane.showConfirmDialog(null, panAux,
					        "Load kakuro", JOptionPane.YES_NO_OPTION);
					if(choice == JOptionPane.YES_OPTION) {	//si confirma que lo quiere cargar pasamos de pantalla
						if(CtrlDomini.readKakuro(fileKakuro,true)) {
							inLoadPage();
							cl.show(background, "LoadPage");
						}
					}
				}
				//si no tiene seleccionada ninguna casilla, le informamos
				else {
					JOptionPane.showMessageDialog(null, "Please select a kakuro", "", JOptionPane.INFORMATION_MESSAGE);
				}
			}
		});
	}
	
	//repositorio del usuario
	private void inUserRepoPage(Object[][] usersKakuros, String selectedOpt) {
		//creamos el JPanel de la pagina y la inicializamos
		userRepoPage = new JPanel();
		background.add(userRepoPage,"UserRepoPage");
		SpringLayout sl_userRepoPage = new SpringLayout();
		userRepoPage.setLayout(sl_userRepoPage);
		//etiqueta con las instrucciones a seguir
		JLabel titleLbl = new JLabel("Choose a Kakuro from the user repository");
		titleLbl.setFont(new Font("Lucida Grande", Font.PLAIN, 17));
		sl_userRepoPage.putConstraint(SpringLayout.NORTH, titleLbl, 30, SpringLayout.NORTH, userRepoPage);
		sl_userRepoPage.putConstraint(SpringLayout.WEST, titleLbl, 30, SpringLayout.WEST, userRepoPage);
		userRepoPage.add(titleLbl);
		//boton atras
		JButton backButton = new JButton("Back");
		backButton.setFont(new Font("Lucida Grande", Font.PLAIN, 17));
		sl_userRepoPage.putConstraint(SpringLayout.NORTH, backButton, -60, SpringLayout.SOUTH, userRepoPage);
		sl_userRepoPage.putConstraint(SpringLayout.WEST, backButton, -121, SpringLayout.EAST, userRepoPage);
		sl_userRepoPage.putConstraint(SpringLayout.SOUTH, backButton, -10, SpringLayout.SOUTH, userRepoPage);
		sl_userRepoPage.putConstraint(SpringLayout.EAST, backButton, -10, SpringLayout.EAST, userRepoPage);
		userRepoPage.add(backButton);
		backButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fileKakuro = originalPath = "";
				cl.show(background, "RepositoryPage");
			}
		});
		//cabeceras de la tabla
		String[] columnNames = {"Creator", "Size", "Difficulty", "id"};
		//inicializa correctamente el repositorio
		// representamos el repositorio en una tabla
		
		JTable userRepo = new JTable(usersKakuros, columnNames);
		userRepo.setFillsViewportHeight(true);
		userRepo.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		//centrar elementos de la tabla
		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
		userRepo.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
		userRepo.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
		userRepo.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
		userRepo.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
		userRepo.setBorder(null);
		//no modificar las celdas
		JTextField tf = new JTextField();
		tf.setEditable(false);
		DefaultCellEditor editor = new DefaultCellEditor( tf );
		userRepo.setDefaultEditor(Object.class, editor);
		//no mover columnas
		userRepo.getTableHeader().setReorderingAllowed(false);
		//seleccionar fila entera
		userRepo.setRowSelectionAllowed(true);
		userRepo.setFont(new Font("Lucida Grande", Font.PLAIN, 14));
		userRepo.setBackground(new Color(204, 204, 204));
		//creamos un scrollpane para en el caso de que haya muchos kakuros en el repositorio, permita hacer scroll para verlos todos
		JScrollPane scrollPane = new JScrollPane(userRepo);
		scrollPane.setEnabled(false);
		scrollPane.setViewportBorder(null);
		sl_userRepoPage.putConstraint(SpringLayout.NORTH, scrollPane, 21, SpringLayout.SOUTH, titleLbl);
		sl_userRepoPage.putConstraint(SpringLayout.WEST, scrollPane, 24, SpringLayout.WEST, userRepoPage);
		sl_userRepoPage.putConstraint(SpringLayout.SOUTH, scrollPane, -12, SpringLayout.NORTH, backButton);
		sl_userRepoPage.putConstraint(SpringLayout.EAST, scrollPane, -44, SpringLayout.EAST, userRepoPage);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		userRepoPage.add(scrollPane);		
		//filtro con 4 opciones: sin filtro, creador, tamaño y dificultad
		JComboBox<String> comboBox = new JComboBox<String>();
		comboBox.addItem("No Filter");
		comboBox.addItem("Creator");
		comboBox.addItem("Size");
		comboBox.addItem("Difficulty");
		comboBox.setSelectedItem(selectedOpt);
		sl_userRepoPage.putConstraint(SpringLayout.NORTH, comboBox, 3, SpringLayout.NORTH, titleLbl);
		sl_userRepoPage.putConstraint(SpringLayout.WEST, comboBox, 6, SpringLayout.EAST, titleLbl);
		sl_userRepoPage.putConstraint(SpringLayout.EAST, comboBox, 120, SpringLayout.EAST, titleLbl);
		comboBox.addActionListener(new ActionListener() {
			   @Override
			   public void actionPerformed(ActionEvent e) {
				   String selectedOption = (String)comboBox.getSelectedItem();
				   //caso de escoger filtrar por creador
				   if("Creator".equals(selectedOption)) {
					   //pedimos que se introduzca un creador
					   String inputCreator = JOptionPane.showInputDialog(userRepoPage, "Enter a creator", "", JOptionPane.PLAIN_MESSAGE);
					   //creamos una matriz auxiliar con tamaño del repositorio original (java no permite editar dinámicamente el tamaño de una matriz)
					   Object[][] usersKakurosCreatorAux = new Object[Array.getLength(usersKakurosNoModify)][4];
					   int j = 0;
					   //nos quedamos con los valores que cumplen las condiciones
					   for(int i = 0; i < Array.getLength(usersKakurosNoModify); ++i) {
						  if(usersKakurosNoModify[i][0] != null && usersKakurosNoModify[i][0].equals(inputCreator)) {
							  usersKakurosCreatorAux[j][0] = usersKakurosNoModify[i][0];
							  usersKakurosCreatorAux[j][1] = usersKakurosNoModify[i][1];
							  usersKakurosCreatorAux[j][2] = usersKakurosNoModify[i][2];
							  usersKakurosCreatorAux[j][3] = usersKakurosNoModify[i][3];
							   ++j; 
						  }
					   }
					   //creamos una matriz con el tamaño correcto
					   Object[][] usersKakurosCreator = new Object[j][4];
					   int k = 0;
					   //copiamos los valores que hemos guardado en la matriz auxiliar (esto lo hacemos para que no haya entradas vacías)
					   for(int i = 0; i < j; ++i) {
						   usersKakurosCreator[k][0] = usersKakurosCreatorAux[i][0];
						   usersKakurosCreator[k][1] = usersKakurosCreatorAux[i][1];
						   usersKakurosCreator[k][2] = usersKakurosCreatorAux[i][2];
						   usersKakurosCreator[k][3] = usersKakurosCreatorAux[i][3];
						   k++;
					   }
					   inUserRepoPage(usersKakurosCreator, selectedOption);
					   cl.show(background, "UserRepoPage");
				   }
				   //caso de escoger filtrar por tamaño
				   else if ("Size".equals(selectedOption)) {
					   //pedimos que se introduzca un tamaño
					   String inputSize = JOptionPane.showInputDialog(userRepoPage, "Enter an available size with format '4x4'", "", JOptionPane.PLAIN_MESSAGE);
					   //creamos una matriz auxiliar con tamaño del repositorio original (java no permite editar dinámicamente el tamaño de una matriz)
					   Object[][] usersKakurosSizeAux = new Object[Array.getLength(usersKakurosNoModify)][4];
					   int j = 0;
					   //nos quedamos con los valores que cumplen las condiciones
					   for(int i = 0; i < Array.getLength(usersKakurosNoModify); ++i) {
						  if(usersKakurosNoModify[i][1] != null && usersKakurosNoModify[i][1].equals(inputSize)) {
							   usersKakurosSizeAux[j][0] = usersKakurosNoModify[i][0];
							   usersKakurosSizeAux[j][1] = usersKakurosNoModify[i][1];
							   usersKakurosSizeAux[j][2] = usersKakurosNoModify[i][2];
							   usersKakurosSizeAux[j][3] = usersKakurosNoModify[i][3];
							   ++j; 
						  }
					   }
					   //creamos una matriz con el tamaño correcto
					   Object[][] usersKakurosSize = new Object[j][4];
					   int k = 0;
					   //copiamos los valores que hemos guardado en la matriz auxiliar (esto lo hacemos para que no haya entradas vacías)
					   for(int i = 0; i < j; ++i) {
						   usersKakurosSize[k][0] = usersKakurosSizeAux[i][0];
						   usersKakurosSize[k][1] = usersKakurosSizeAux[i][1];
						   usersKakurosSize[k][2] = usersKakurosSizeAux[i][2];
						   usersKakurosSize[k][3] = usersKakurosSizeAux[i][3];
						   k++;
					   }
					   
					   inUserRepoPage(usersKakurosSize, selectedOption);
					   cl.show(background, "UserRepoPage");
				   }
				   //caso de filtrar por dificultad
				   else if("Difficulty".equals(selectedOption)) {
					   //pedimos que se introduzca una dificultad
					   String inputDiff = JOptionPane.showInputDialog(userRepoPage, "Enter a correct difficulty", "", JOptionPane.PLAIN_MESSAGE);
					   //creamos una matriz auxiliar con tamaño del repositorio original (java no permite editar dinámicamente el tamaño de una matriz)
					   Object[][] usersKakurosDiffAux = new Object[Array.getLength(usersKakurosNoModify)][4];
					   int j = 0;
					 //nos quedamos con los valores que cumplen las condiciones
					   for(int i = 0; i < Array.getLength(usersKakurosNoModify); ++i) {
						  if(usersKakurosNoModify[i][2] != null && usersKakurosNoModify[i][2].equals(inputDiff)) {
							  usersKakurosDiffAux[j][0] = usersKakurosNoModify[i][0];
							  usersKakurosDiffAux[j][1] = usersKakurosNoModify[i][1];
							  usersKakurosDiffAux[j][2] = usersKakurosNoModify[i][2];
							  usersKakurosDiffAux[j][3] = usersKakurosNoModify[i][3];
							   ++j; 
						  }
					   }
					   //creamos una matriz con el tamaño correcto
					   Object[][] usersKakurosDiff = new Object[j][4];
					   int k = 0;
					 //copiamos los valores que hemos guardado en la matriz auxiliar (esto lo hacemos para que no haya entradas vacías)
					   for(int i = 0; i < j; ++i) {
						   usersKakurosDiff[k][0] = usersKakurosDiffAux[i][0];
						   usersKakurosDiff[k][1] = usersKakurosDiffAux[i][1];
						   usersKakurosDiff[k][2] = usersKakurosDiffAux[i][2];
						   usersKakurosDiff[k][3] = usersKakurosDiffAux[i][3];
						   k++;
					   }
					   inUserRepoPage(usersKakurosDiff, selectedOption);
					   cl.show(background, "UserRepoPage");
				   }
				   //caso de no filtrado, utilizamos la matriz original
				   else if("No Filter".equals(selectedOption)) {
					   Object[][] userKakurosAux = new Object[Array.getLength(usersKakurosNoModify)][4];
					   CtrlDomini.readUsersRepository(userKakurosAux);
					   inUserRepoPage(userKakurosAux, selectedOption);
					   cl.show(background, "UserRepoPage");
				   }
			   }
		});
		userRepoPage.add(comboBox);
		
		userRepo.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			//nos quedamos con la fila que está seleccionada
	            public void mousePressed(java.awt.event.MouseEvent evt) {
				String titleTxt = "";
	            	int row = userRepo.getSelectedRow();
	            	if(row!=-1) {
	            		Object value = userRepo.getModel().getValueAt(row, 0);
		            	if(value != null) {
		            		String creator = value.toString();
		            		String size = userRepo.getModel().getValueAt(row, 1).toString();
		            		String difficulty = userRepo.getModel().getValueAt(row, 2).toString();
		            		int id = Integer.parseInt(userRepo.getModel().getValueAt(row, 3).toString());
		            		//cogemos el titulo del txt con el kakuro que hay que leer
		            		if(id != 0) {
		            			titleTxt +=creator+"-"+size+"-"+difficulty+id+".txt";
		            		}
		            		else titleTxt +=creator+"-"+size+"-"+difficulty+".txt";
		            		fileKakuro = originalPath = "resources/usersRepository/"+titleTxt;
		            	} 
	            	}           	
	            }
	    });
		//boton cargar kakuro
		JButton loadButton = new JButton("Load kakuro");
		sl_userRepoPage.putConstraint(SpringLayout.NORTH, loadButton, 0, SpringLayout.NORTH, backButton);
		sl_userRepoPage.putConstraint(SpringLayout.WEST, loadButton, 116, SpringLayout.WEST, userRepoPage);
		sl_userRepoPage.putConstraint(SpringLayout.SOUTH, loadButton, 0, SpringLayout.SOUTH, backButton);
		sl_userRepoPage.putConstraint(SpringLayout.EAST, loadButton, -120, SpringLayout.WEST, backButton);
		loadButton.setFont(new Font("Lucida Grande", Font.PLAIN, 17));
		userRepoPage.add(loadButton);
		loadButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//si hay un kakuro seleccionado
				if(fileKakuro!="") {
					JPanel panAux = new JPanel();
					//preguntamos si se quiere cargar ese kakuro
					panAux.add(new JLabel("Would you like to load this kakuro?"));
					int choice = JOptionPane.showConfirmDialog(null, panAux,
					        "Load kakuro", JOptionPane.YES_NO_OPTION);
					if(choice == JOptionPane.YES_OPTION) {	//si confirma que lo quiere cargar pasamos de pantalla
						if(CtrlDomini.readKakuro(fileKakuro,true)) {
							inLoadPage();
							cl.show(background, "LoadPage");
						}
					}
				}
				//si no tiene ningun kakuro seleccionado, le informamos de que seleccione uno
				else {
					JOptionPane.showMessageDialog(null, "Please select a kakuro", "", JOptionPane.INFORMATION_MESSAGE);
				}
			}
		});
		
	}
	
	//generar kakuro (segun tamano, dificultad, caracteristicas o totalmente aleatorio) 
	private void inGeneratePage(){
		//creamos el JPanel de la pagina y la inicializamos
		generatePage = new JPanel();
		background.add(generatePage,"GeneratePage");
		SpringLayout sl_generatePage = new SpringLayout();
		generatePage.setLayout(sl_generatePage);
		//boton atras
		JButton backButton = new JButton("Back");
		backButton.setFont(new Font("Lucida Grande", Font.PLAIN, 17));
		sl_generatePage.putConstraint(SpringLayout.NORTH, backButton, -60, SpringLayout.SOUTH, generatePage);
		sl_generatePage.putConstraint(SpringLayout.WEST, backButton, -121, SpringLayout.EAST, generatePage);
		sl_generatePage.putConstraint(SpringLayout.SOUTH, backButton, -10, SpringLayout.SOUTH, generatePage);
		sl_generatePage.putConstraint(SpringLayout.EAST, backButton, -10, SpringLayout.EAST, generatePage);
		generatePage.add(backButton);
		//boton tamaño
		JButton sizeButton = new JButton("Choose size only");
		sl_generatePage.putConstraint(SpringLayout.NORTH, sizeButton, 114, SpringLayout.NORTH, generatePage);
		sl_generatePage.putConstraint(SpringLayout.WEST, sizeButton, 125, SpringLayout.WEST, generatePage);
		sl_generatePage.putConstraint(SpringLayout.EAST, sizeButton, -125, SpringLayout.EAST, generatePage);
		sizeButton.setFont(new Font("Lucida Grande", Font.PLAIN, 17));
		generatePage.add(sizeButton);
		//boton dificultad
		JButton difficultyButton = new JButton("Choose difficulty");
		sl_generatePage.putConstraint(SpringLayout.NORTH, difficultyButton, 59, SpringLayout.NORTH, generatePage);
		sl_generatePage.putConstraint(SpringLayout.WEST, difficultyButton, 0, SpringLayout.WEST, sizeButton);
		sl_generatePage.putConstraint(SpringLayout.SOUTH, difficultyButton, -5, SpringLayout.NORTH, sizeButton);
		sl_generatePage.putConstraint(SpringLayout.EAST, difficultyButton, -125, SpringLayout.EAST, generatePage);
		difficultyButton.setFont(new Font("Lucida Grande", Font.PLAIN, 17));
		generatePage.add(difficultyButton);
		//boton tamaño, blancas y negras
		JButton fullButton = new JButton("Choose size, blacks and whites");
		sl_generatePage.putConstraint(SpringLayout.NORTH, fullButton, 170, SpringLayout.NORTH, generatePage);
		sl_generatePage.putConstraint(SpringLayout.SOUTH, sizeButton, -6, SpringLayout.NORTH, fullButton);
		sl_generatePage.putConstraint(SpringLayout.WEST, fullButton, 125, SpringLayout.WEST, generatePage);
		sl_generatePage.putConstraint(SpringLayout.SOUTH, fullButton, -140, SpringLayout.SOUTH, generatePage);
		sl_generatePage.putConstraint(SpringLayout.EAST, fullButton, -125, SpringLayout.EAST, generatePage);
		fullButton.setFont(new Font("Lucida Grande", Font.PLAIN, 17));
		generatePage.add(fullButton);
		//boton random
		JButton randomButton = new JButton("Completely random");
		sl_generatePage.putConstraint(SpringLayout.NORTH, randomButton, 6, SpringLayout.SOUTH, fullButton);
		sl_generatePage.putConstraint(SpringLayout.WEST, randomButton, 125, SpringLayout.WEST, generatePage);
		sl_generatePage.putConstraint(SpringLayout.SOUTH, randomButton, -84, SpringLayout.SOUTH, generatePage);
		sl_generatePage.putConstraint(SpringLayout.EAST, randomButton, -125, SpringLayout.EAST, generatePage);
		randomButton.setFont(new Font("Lucida Grande", Font.PLAIN, 17));
		generatePage.add(randomButton);
		backButton.addActionListener(e -> cl.show(background, "PlayPage"));
		//se presiona el boton de tamaño
		sizeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//textfield para que introduzca los valores
				JTextField row = new JTextField(5);
			    JTextField column = new JTextField(5);
			    //etiquetas para mostrar que se tiene que introducir en cada textfield
				JPanel panAux = new JPanel();
				panAux.add(new JLabel("rows:"));
				panAux.add(row);
				panAux.add(new JLabel("columns:"));
				panAux.add(column);
				
				int choice = JOptionPane.showConfirmDialog(null, panAux,
				        "Enter the indicated values", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
				if(choice == JOptionPane.OK_OPTION) {	//si no se ha salido cerrando la ventana o cancelando
					//si no se introducen valores, pedimos que se introduzcan
					if((row.getText()).isEmpty() || (column.getText()).isEmpty()) JOptionPane.showMessageDialog(generatePage, "Please enter values", "", JOptionPane.INFORMATION_MESSAGE);
					else {
						int rows = 0, columns = 0;
						boolean catched = false;
						//comprobamos que son enteros
						try{
							rows = Integer.parseInt(row.getText());
							columns = Integer.parseInt(column.getText());
						}
						//si no son enteros pedimos que se introduzcan valores correctos
						catch (NumberFormatException ex){
							catched = true;
							String message = "Please enter correct values";
							CtrlDomini.showException(message);
						}
						if(!catched) {
							//si el tamano es muy pequeno o muy grande avisamos
							if(rows<3 || columns<3) JOptionPane.showMessageDialog(generatePage, "Please enter bigger values", "", JOptionPane.INFORMATION_MESSAGE);
							else if(rows>20 || columns>20) JOptionPane.showMessageDialog(generatePage, "Please enter smaller values", "", JOptionPane.INFORMATION_MESSAGE);	//controlamos que el tamano no sea exagerado (kakuro de tamano realista)
							
							//si el tamano es menor de 7x7 preguntamos por solucion unica (ya consideramos que tenga un tamano ni muy grande ni muy pequeno) 
							else if(rows<8 && columns<8) {
								//panel con pregunta de generar con solucion unica
								Object[] options = {"Yes", "No"};
								boolean unique = false;
								int unic = JOptionPane.showOptionDialog(background, "Would you like to generate with a unique solution?", "Unique confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
								if(unic == JOptionPane.YES_OPTION) unique = true;
								
								int[] opts = {rows, columns};
								if(CtrlDomini.createKakuroSize(opts, unique)) {
									originalPath = "generated";
									inLoadPage();
									cl.show(background,"LoadPage");
								}
							}
							//si el tamano es mayor de 7x7 (y menor de 21x21) lo generamos sin preguntar por solucion unica
							else{
								//si es demasiado grande como para solucion unica no preguntamos por las preferencias del usuario
								int[] opts = {rows, columns};
								
								if(CtrlDomini.createKakuroSize(opts, false)) {
									originalPath = "generated";
									inLoadPage();
									cl.show(background,"LoadPage");
								}
							}
						}
					}
				}
			}
		});
		//se presiona dificultad
		difficultyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String inputDiff = JOptionPane.showInputDialog(generatePage, "Difficulty:", "Enter the indicated values", JOptionPane.PLAIN_MESSAGE);
				//si la difcultad no coincide con una valida avisamos
				if(inputDiff!= null && !(inputDiff.equalsIgnoreCase("easy") || inputDiff.equalsIgnoreCase("medium") || inputDiff.equalsIgnoreCase("hard"))) {
					JOptionPane.showMessageDialog(generatePage, "Please enter a valid difficulty", "", JOptionPane.INFORMATION_MESSAGE);
				}
				//si la dificultad es valida (no vacia) generamos
				else if(inputDiff!=null){
					if(CtrlDomini.createKakuroDifficulty(inputDiff)) {
						originalPath = "generated";
						inLoadPage();
						cl.show(background,"LoadPage");
					}
				}
			}
		});
		//se presiona tamaño, blancas y negras
		fullButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//textfields para que introduzcan los valores
				JTextField row = new JTextField(5);
			    JTextField column = new JTextField(5);
			    JTextField black = new JTextField(5);
			    JTextField white = new JTextField(5);
			    //etiquetas para mostrar que se tiene que introducir en cada textfield
				JPanel panAux = new JPanel(new GridLayout(0,4));
				panAux.add(new JLabel("rows:"));
				panAux.add(row);
				panAux.add(new JLabel("whites:"));
				panAux.add(white);
				panAux.add(new JLabel("columns:"));
				panAux.add(column);
				panAux.add(new JLabel("blacks:"));
				panAux.add(black);
				
				
				int choice = JOptionPane.showConfirmDialog(null, panAux,
				        "Enter the indicated values", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
				if(choice == JOptionPane.OK_OPTION) {	//si le ha dado a ok
					//si algún textfield está vacío, le informamos
					if((row.getText()).isEmpty() || (column.getText()).isEmpty() || white.getText().isEmpty() || black.getText().isEmpty())
						JOptionPane.showMessageDialog(generatePage, "Please enter values for all entries", "", JOptionPane.INFORMATION_MESSAGE);
					else {	//si hay valores en las cuatro casillas hay que comprobarlos
						int rows = 0, columns = 0, whites = 0, blacks = 0;
						boolean catched = false;
						//comprobamos que se han introducido enteros
						try{
							 rows = Integer.parseInt(row.getText());
							 columns = Integer.parseInt(column.getText());
							 whites = Integer.parseInt(white.getText());
							 blacks = Integer.parseInt(black.getText());
						}
						//si no son enteros, se informa
						catch (NumberFormatException ex){
							catched = true;
							String message = "Please enter correct values";
							CtrlDomini.showException(message);
						}
						if(!catched) {
							if(rows<3 || columns < 3) {	//tamano muy pequeno
								JOptionPane.showMessageDialog(generatePage, "Please enter valid values for rows and columns", "", JOptionPane.INFORMATION_MESSAGE);
							}
							else if((rows*columns != whites+blacks) || (blacks< rows+columns-1))
								JOptionPane.showMessageDialog(generatePage, "Please enter valid values for whites and blacks", "", JOptionPane.INFORMATION_MESSAGE);
							else if(rows>20 || columns>20) JOptionPane.showMessageDialog(generatePage, "Please enter smaller values for rows and columns", "", JOptionPane.INFORMATION_MESSAGE);
							else if(rows<8 && columns<8) {
								//panel con pregunta de generar con solucion unica
								Object[] options = {"Yes", "No"};
								boolean unique = false;
								int unic = JOptionPane.showOptionDialog(background, "Would you like to generate with a unique solution?", "Unique confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
								if(unic == JOptionPane.YES_OPTION) unique = true;
								int[] opts = {rows, columns, blacks, whites};
								if(CtrlDomini.createKakuroSize(opts, unique)) {
									originalPath = "generated";
									inLoadPage();
									cl.show(background,"LoadPage");
								}
							}
							else {
								int[] opts = {rows, columns, blacks, whites};
								if(CtrlDomini.createKakuroSize(opts, false)) {
									originalPath = "generated";
									inLoadPage();
									cl.show(background,"LoadPage");
								}
							}
						}
					}
				}
			}
		});
		//boton random
		randomButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(CtrlDomini.createKakuroRandom()) {
					originalPath = "generated";
					inLoadPage();
					cl.show(background,"LoadPage");
				}
			}
		});
	}
	
	//introducir kakuro
	private void inIntroducePage() {
		//creamos el JPanel de la pagina y la inicializamos
		introducePage = new JPanel();
		background.add(introducePage,"IntroducePage");
		SpringLayout sl_introducePage = new SpringLayout();
		introducePage.setLayout(sl_introducePage);
		//etiqueta con las instrucciones a seguir
		JLabel introduceLbl = new JLabel("Enter the name of a file to read the kakuro:");
		sl_introducePage.putConstraint(SpringLayout.NORTH, introduceLbl, 26, SpringLayout.NORTH, introducePage);
		sl_introducePage.putConstraint(SpringLayout.WEST, introduceLbl, 22, SpringLayout.WEST, introducePage);
		introduceLbl.setFont(new Font("Lucida Grande", Font.PLAIN, 17));
		introducePage.add(introduceLbl);
		//textfield para que se introduzca el nombre del fichero
		JTextField filenameLbl = new JTextField();
		sl_introducePage.putConstraint(SpringLayout.WEST, filenameLbl, 190, SpringLayout.WEST, introducePage);
		filenameLbl.setFont(new Font("Lucida Grande", Font.PLAIN, 17));
		introducePage.add(filenameLbl);
		filenameLbl.setColumns(10);
		//boton atras
		JButton backButton = new JButton("Back");
		backButton.setFont(new Font("Lucida Grande", Font.PLAIN, 17));
		sl_introducePage.putConstraint(SpringLayout.NORTH, backButton, -60, SpringLayout.SOUTH, introducePage);
		sl_introducePage.putConstraint(SpringLayout.WEST, backButton, -121, SpringLayout.EAST, introducePage);
		sl_introducePage.putConstraint(SpringLayout.SOUTH, backButton, -10, SpringLayout.SOUTH, introducePage);
		sl_introducePage.putConstraint(SpringLayout.EAST, backButton, -10, SpringLayout.EAST, introducePage);
		introducePage.add(backButton);
		backButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				filenameLbl.setText("");
				cl.show(background, "PlayPage");
			}
		});
		//boton de carga del kakuro
		JButton introButton = new JButton("Load kakuro");
		sl_introducePage.putConstraint(SpringLayout.SOUTH, filenameLbl, -35, SpringLayout.NORTH, introButton);
		sl_introducePage.putConstraint(SpringLayout.NORTH, introButton, 204, SpringLayout.NORTH, introducePage);
		sl_introducePage.putConstraint(SpringLayout.SOUTH, introButton, -106, SpringLayout.SOUTH, introducePage);
		sl_introducePage.putConstraint(SpringLayout.WEST, introButton, 157, SpringLayout.WEST, introducePage);
		sl_introducePage.putConstraint(SpringLayout.EAST, introButton, 393, SpringLayout.WEST, introducePage);
		introButton.setFont(new Font("Lucida Grande", Font.PLAIN, 17));
		introducePage.add(introButton);
		introButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String filename = filenameLbl.getText();
				//si el formato no es valido se informa
				if(filename == null || filename.length()<1 || filename!= null && filename == "") JOptionPane.showMessageDialog(null, "Enter a valid name with format 'filename.txt'", "", JOptionPane.INFORMATION_MESSAGE);
				else {
					if(!filename.endsWith(".txt")) filename+=".txt";
					fileKakuro = originalPath = filename;
					//se comprueba que se puede leer y validar
					JOptionPane.showMessageDialog(null, "The kakuro is being read and validated.\nThis operation may take some time.", "", JOptionPane.INFORMATION_MESSAGE);
					if(CtrlDomini.readKakuro(fileKakuro,true)) {	//si lo ha podido validar carga la pagina para elegir jugar
						inLoadPage();
						cl.show(background, "LoadPage");
					}
				}
			}
		});
		
	}
	
	//reanudar la partida
	private void inResumePage() {
		//creamos el JPanel de la pagina y la inicializamos
		resumePage = new JPanel();
		background.add(resumePage,"ResumePage");
		SpringLayout sl_resumePage = new SpringLayout();
		resumePage.setLayout(sl_resumePage);
		//informamos de que tiene que hacer el usuario
		JLabel titleLbl = new JLabel("Choose a Kakuro to resume game");
		titleLbl.setFont(new Font("Lucida Grande", Font.PLAIN, 17));
		sl_resumePage.putConstraint(SpringLayout.NORTH, titleLbl, 30, SpringLayout.NORTH, resumePage);
		sl_resumePage.putConstraint(SpringLayout.WEST, titleLbl, 30, SpringLayout.WEST, resumePage);
		resumePage.add(titleLbl);
		//boton atras
		JButton backButton = new JButton("Back");
		backButton.setFont(new Font("Lucida Grande", Font.PLAIN, 17));
		sl_resumePage.putConstraint(SpringLayout.NORTH, backButton, -60, SpringLayout.SOUTH, resumePage);
		sl_resumePage.putConstraint(SpringLayout.WEST, backButton, -121, SpringLayout.EAST, resumePage);
		sl_resumePage.putConstraint(SpringLayout.SOUTH, backButton, -10, SpringLayout.SOUTH, resumePage);
		sl_resumePage.putConstraint(SpringLayout.EAST, backButton, -10, SpringLayout.EAST, resumePage);
		resumePage.add(backButton);
		backButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fileKakuro = originalPath = "";
				cl.show(background, "PlayPage");
			}
		});
		
		//cabeceras de la tabla
		String[] columnNames = {"Size", "Difficulty", "id"};
		//creamos la tabla con sus partidas guardadas
		JTable userTable = new JTable(userSaved, columnNames);
		userTable.setFillsViewportHeight(true);
		userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		//centrar elementos de la tabla
		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
		userTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
		userTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
		userTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
		userTable.setBorder(null);
		//no modificar las celdas
		JTextField tf = new JTextField();
		tf.setEditable(false);
		DefaultCellEditor editor = new DefaultCellEditor( tf );
		userTable.setDefaultEditor(Object.class, editor);
		//no mover columnas
		userTable.getTableHeader().setReorderingAllowed(false);
		//seleccionar fila entera
		userTable.setRowSelectionAllowed(true);
		userTable.setFont(new Font("Lucida Grande", Font.PLAIN, 14));
		userTable.setBackground(new Color(204, 204, 204));
		//creamos una scrollpane para en el caso de tener muchas partidas guardadas, pueda verlas todas haciendo scroll
		JScrollPane scrollPane = new JScrollPane(userTable);
		sl_resumePage.putConstraint(SpringLayout.NORTH, scrollPane, 21, SpringLayout.SOUTH, titleLbl);
		sl_resumePage.putConstraint(SpringLayout.WEST, scrollPane, 44, SpringLayout.WEST, resumePage);
		sl_resumePage.putConstraint(SpringLayout.SOUTH, scrollPane, -12, SpringLayout.NORTH, backButton);
		sl_resumePage.putConstraint(SpringLayout.EAST, scrollPane, -44, SpringLayout.EAST, resumePage);
		scrollPane.setEnabled(false);
		scrollPane.setViewportBorder(null);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		resumePage.add(scrollPane);	
		
		userTable.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			//nos quedamos con la fila seleccionada
	            public void mousePressed(java.awt.event.MouseEvent evt) {
				String titleTxt = "";
	            	int row = userTable.getSelectedRow();
	            	if(row!=-1) {
	            		Object value = userTable.getModel().getValueAt(row, 0);
		            	if(value != null) {
		            		String creator = username;
		            		String size = value.toString();
		            		String difficulty = userTable.getModel().getValueAt(row, 1).toString();
		            		int id = Integer.parseInt(userTable.getModel().getValueAt(row, 2).toString());
		            		//cogemos el titulo del txt con el kakuro que hay que leer
		            		if(id != 0) {
		            			titleTxt +=creator+"-"+size+"-"+difficulty+id+".txt";
		            		}
		            		else titleTxt +=creator+"-"+size+"-"+difficulty+".txt";
		            		fileKakuro = originalPath = "resources/savedGames/"+titleTxt;
		            	} 
	            	}           	
	            }
	    });
		//boton de cargar el kakuro
		JButton loadButton = new JButton("Load kakuro");
		sl_resumePage.putConstraint(SpringLayout.NORTH, loadButton, 0, SpringLayout.NORTH, backButton);
		sl_resumePage.putConstraint(SpringLayout.WEST, loadButton, 116, SpringLayout.WEST, resumePage);
		sl_resumePage.putConstraint(SpringLayout.SOUTH, loadButton, 0, SpringLayout.SOUTH, backButton);
		sl_resumePage.putConstraint(SpringLayout.EAST, loadButton, -120, SpringLayout.WEST, backButton);
		loadButton.setFont(new Font("Lucida Grande", Font.PLAIN, 17));
		resumePage.add(loadButton);
		loadButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//si se ha seleccionado un kakuro
				if(fileKakuro!="") {
					JPanel panAux = new JPanel();
					//preguntamos si quiere cargar ese kakuro
					panAux.add(new JLabel("Would you like to load this kakuro?"));
					int choice = JOptionPane.showConfirmDialog(null, panAux,
					        "Load kakuro", JOptionPane.YES_NO_OPTION);
					if(choice == JOptionPane.YES_OPTION) {	//si confirma que lo quiere cargar pasamos de pantalla
						if(CtrlDomini.readKakuro(fileKakuro,false)) {
							String aux = CtrlDomini.getRecord();
							if(aux != "59:59.99") {
								String minutesAux = aux.substring(0, 2);
								minutes = Short.parseShort(minutesAux);
								String secondsAux = aux.substring(3, 5);
								seconds = Byte.parseByte(secondsAux);
								String centisecondsAux = aux.substring(6);
								centiseconds = Byte.parseByte(centisecondsAux);
							}
							inLoadPage();
							cl.show(background, "LoadPage");
						}
					}
				}
				//si no ha seleccionado ningun kakuro, le informamos
				else {
					JOptionPane.showMessageDialog(null, "Please select a kakuro", "", JOptionPane.INFORMATION_MESSAGE);
				}
			}
		});
		
	}

	//pantalla puente antes de resolver un kakuro (guardarlo al repositorio, decidir si lo resuelve el sistema o el usuario..)
	private void inLoadPage() {
		//creamos el JPanel de la pagina y la inicializamos
		loadPage = new JPanel();
		background.add(loadPage,"LoadPage");
		SpringLayout sl_loadPage = new SpringLayout();
		loadPage.setLayout(sl_loadPage);
		//boton de resolver por usuario
		JButton userButton = new JButton("Solve user");
		sl_loadPage.putConstraint(SpringLayout.NORTH, userButton, 88, SpringLayout.NORTH, loadPage);
		sl_loadPage.putConstraint(SpringLayout.WEST, userButton, 96, SpringLayout.WEST, loadPage);
		sl_loadPage.putConstraint(SpringLayout.SOUTH, userButton, -222, SpringLayout.SOUTH, loadPage);
		sl_loadPage.putConstraint(SpringLayout.EAST, userButton, -288, SpringLayout.EAST, loadPage);
		userButton.setFont(new Font("Lucida Grande", Font.PLAIN, 17));
		loadPage.add(userButton);
		userButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//si no es una partida guardada
				if(!originalPath.contains("savedGames")) {
					//preguntamos cuantas pistas quiere
					String inputDiff = JOptionPane.showInputDialog(null, "Enter number of clues", "", JOptionPane.PLAIN_MESSAGE);
					boolean validClues = true;	//indica si el numero de pistas es valido
					int numSolved = 0;
					boolean catched = false;
					//miramos que el valor introducido sea un entero
					try{
							numSolved = Integer.valueOf(inputDiff);					
							
						}
					//en el caso de que no sea entero, se pide que se entre un entero
						catch (NumberFormatException ex){
							catched = true;
							String message = "Please enter a number";
							CtrlDomini.showException(message);
						}
					if(!catched) {
						if(inputDiff != null && !inputDiff.equals("") && numSolved>=0) {
							//llamamos a la funcion help con parametro numSolved -> desde esa funcion si numSolved>=numWhites salta excepcion que lo indica
							validClues = CtrlDomini.initialHelp(numSolved);
						}
						else if(inputDiff != null && !inputDiff.equals("") && numSolved<0) {
							JOptionPane.showMessageDialog(null, "Please enter a valid number of clues", "", JOptionPane.INFORMATION_MESSAGE);
						}
						
						if(inputDiff != null && !inputDiff.equals("") && numSolved>=0 && validClues) {
							
							//pasamos a la pantalla de carga
							timer.stop();
							seconds = 0; 
							minutes = 0; 
						    centiseconds = 0; 
						    //penalización de 7 segundos por cada pista
						    int additionalSeconds = numSolved*7;
							minutes+=additionalSeconds/60;
							seconds+=additionalSeconds%60;
							
							inGamePage();
							cl.show(background, "GamePage");
						}
					}
				}
				else {
					timer.stop();
					inGamePage();
					cl.show(background, "GamePage");
				}
			}
		});
		//boton de resolver el sistema
		JButton systemButton = new JButton("Solve system");
		sl_loadPage.putConstraint(SpringLayout.NORTH, systemButton, 88, SpringLayout.NORTH, loadPage);
		sl_loadPage.putConstraint(SpringLayout.WEST, systemButton, 41, SpringLayout.EAST, userButton);
		sl_loadPage.putConstraint(SpringLayout.EAST, systemButton, -81, SpringLayout.EAST, loadPage);
		systemButton.setFont(new Font("Lucida Grande", Font.PLAIN, 17));
		loadPage.add(systemButton);
		systemButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//se informa que puede tardar
				timer.stop();
				JOptionPane.showMessageDialog(null, "The kakuro is being solved.\nThis operation may take some time.", "Solved by the system", JOptionPane.INFORMATION_MESSAGE);
				seconds = 0; 
				minutes = 0; 
			    centiseconds = 0;
			    
			    long startTime = System.currentTimeMillis();
			    boolean valid = CtrlDomini.solveKakuro();
			    long endTime = System.currentTimeMillis();
			    CtrlDomini.copySolution();
			    //si es valido, se informa del tiempo que se ha tardado en solucionar
			    if(valid) {
			    	originalPath = "";
			    	String aux = "The Solver has found a solution for\nthe Kakuro in ";
			    	if(endTime-startTime == 0) aux+= "less than a milisecond";
			    	else aux+= (endTime-startTime)+" miliseconds";
			    	JOptionPane.showMessageDialog(null, aux, "Solved by the system", JOptionPane.INFORMATION_MESSAGE);
			    	inGamePage();
			    	cl.show(background, "GamePage");
			    }
			    //si no es valido o no tiene solucion, se informa
			    else {
			    	JOptionPane.showMessageDialog(null, "The kakuro is not valid or has no solution", "", JOptionPane.INFORMATION_MESSAGE);
			    }
			}
		});
		//boton para guardar en el repositorio de usuarios
		JButton repoButton = new JButton("Save on repository");
		sl_loadPage.putConstraint(SpringLayout.SOUTH, systemButton, -59, SpringLayout.NORTH, repoButton);
		sl_loadPage.putConstraint(SpringLayout.NORTH, repoButton, 59, SpringLayout.SOUTH, userButton);
		sl_loadPage.putConstraint(SpringLayout.WEST, repoButton, 168, SpringLayout.WEST, loadPage);
		sl_loadPage.putConstraint(SpringLayout.SOUTH, repoButton, -113, SpringLayout.SOUTH, loadPage);
		sl_loadPage.putConstraint(SpringLayout.EAST, repoButton, -152, SpringLayout.EAST, loadPage);
		repoButton.setFont(new Font("Lucida Grande", Font.PLAIN, 17));
		//si el usuario no es invitado, se guarda en el repositorio de usuarios
		if(username != "Guest") loadPage.add(repoButton);
		repoButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fileKakuro = originalPath = CtrlDomini.addRepoUser(username);
				JOptionPane.showMessageDialog(null, "The kakuro has been saved on the users' repository", "", JOptionPane.INFORMATION_MESSAGE);
				repoButton.setVisible(false);	//eliminamos el boton una vez ya lo ha anadido al repositorio (evitamos guardar repetidos)
			}
		});
		//boton atras
		JButton backButton = new JButton("Back");
		backButton.setFont(new Font("Lucida Grande", Font.PLAIN, 17));
		sl_loadPage.putConstraint(SpringLayout.NORTH, backButton, -60, SpringLayout.SOUTH, loadPage);
		sl_loadPage.putConstraint(SpringLayout.WEST, backButton, -121, SpringLayout.EAST, loadPage);
		sl_loadPage.putConstraint(SpringLayout.SOUTH, backButton, -10, SpringLayout.SOUTH, loadPage);
		sl_loadPage.putConstraint(SpringLayout.EAST, backButton, -10, SpringLayout.EAST, loadPage);
		loadPage.add(backButton);
		backButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fileKakuro = originalPath = "";
				cl.show(background, "PlayPage");
			}
		});
	}
	
	//pantalla de juego
	private void inGamePage() {		
		//inicializamos el timer
		if(originalPath!="") timer.start();
		//creamos el JPanel de la pagina y la inicializamos
		gamePage = new JPanel();
		background.add(gamePage,"GamePage");
		SpringLayout sl_gamePage = new SpringLayout();
		gamePage.setLayout(sl_gamePage);
		//obtenemos las medidas del kakuro
		int[] kakuroSize = CtrlDomini.getKakuroSize();
		int height = kakuroSize[0];
		int width = kakuroSize[1];
		//creamos un array "vacío" que serán las cabeceras (luego las ocultaremos pero para inicializar la tabla es necesario)
		String[] cols = new String[width];
		for(int i = 0; i < width; ++i) {
			cols[i] = " ";
		}
		
		//creamos una matriz con el kakuro como strings
		String[][] kakuro = new String[height][width];
		CtrlDomini.getKakuro(kakuro);
		@SuppressWarnings("serial")
		//creamos la tabla que mostrará el kakuro
		JTable gameKakuro = new JTable(kakuro, cols) {
			@Override
			//cambiamos la función por defecto de renderizado de tables
		    public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
		        Component comp = super.prepareRenderer(renderer, row, col);
		        String value = (String) getModel().getValueAt(row, col);
		        //si el valor es "  " (las negras las guardamos así en la matriz), se pone el fondo a negro
		        if (value.equals("  ")) {
		        	comp.setBackground(Color.black);
		        } 
		        //si el valor contiene /, significa que es una negra con valores, ponemos el fondo negro y la letra blanca
		        else if(value.contains("/")) {
		        	comp.setBackground(Color.black);
		        	comp.setForeground(Color.white);
		        }
		        //el resto, que serán las blancas con y sin valor, las ponemos con letra negra y fondo gris claro
		        else {
		            comp.setBackground(Color.lightGray);
		            comp.setForeground(Color.black);
		        }
		        return comp;
		    }
			
			@Override
			//cambiamos la función por defecto de permiso edición de celdas
			public boolean isCellEditable(int row, int col) {
				 String value = (String) getModel().getValueAt(row, col);
				 //si es blanca vacía, se permite editar
				 if(value.equals("")) return true;
				 //si es negra no se permite
				 else if(value.equals("  ")) return false;
				 else if(value.contains("/")) return false;
				 //comprobamos que el resto que hay son números
				 try{
					 //si hay un numero, se permite editar
					 if (Integer.parseInt(value) > 0 && Integer.parseInt(value) < 10) return true;
				}
				 //si se ha introducido un valor que no es un entero, se informa
				catch (NumberFormatException ex){
						String message = "Please enter a number";
						exception(message);
				}
				
				 return false;
			}
		};
		
		gameKakuro.setFont(new Font("Consolas", Font.PLAIN, 11));
		//cada vez que se introduce un valor nuevo se actualiza el kakuro y la matriz que pintamos
		gameKakuro.getModel().addTableModelListener(
		new TableModelListener() {
			   public void tableChanged(TableModelEvent evt) {
				   //obtenemos la columna y fila que se ha modificado
				   int row = gameKakuro.getSelectedRow();
				   int column = gameKakuro.getSelectedColumn();
				   boolean wasNull = false;
				   try {
					   String value = (String) gameKakuro.getModel().getValueAt(row, column);
					   //si se borra un valor, se limpia la celda del kakuro
					   if(value == null) {
						   wasNull = true;
						   kakuro[row][column] = "";
					   }
					   int val = 0;
					   boolean catched = false;
					   if(!wasNull && !value.equals("") && !value.equals("  ")) {
						   //se comprueba que sea un entero
						   try{
							   val = Integer.parseInt(value);
						   }
						   //en el caso de que no sea entero, se informa
						   catch (NumberFormatException ex){
							   		String message = "Please enter a number";
									CtrlDomini.showException(message);
									catched = true;
						   }
						   //si era un entero
						   if(!catched) {
							   //se comprueba que sea un valor valido para el kakuro
							   if(val<1 || val>9) {
								   timer.stop();
								   JOptionPane.showMessageDialog(gamePage, "The value introduced is not valid", "", JOptionPane.WARNING_MESSAGE);
								   kakuro[row][column] = "";
								   timer.start();
							   }				
							   else{
								   timer.stop();
								   //miramos qeu se cumpla que no se repite el valor en la fila ni columna y que no se sobrepasa la suma de fila o columna
								   if(CtrlDomini.checkValue(row,column,Integer.parseInt(value))){
									   kakuro[row][column] = value;
									   CtrlDomini.setKakuroCell(row, column, val);
								   }
								   //en el caso de no cumplir la condición anterior, se informa y se limpia la celda
								   else {
									   kakuro[row][column] = "";
									   CtrlDomini.deleteKakuroCell(row, column);
									   String message = "The number proposed is not valid";
									   CtrlDomini.showException(message);
								   }
								   timer.start();
							   }
						   }
						   else {
							   kakuro[row][column] = "";
							   CtrlDomini.deleteKakuroCell(row, column);
						   }
					   }
				   }
				   catch (ArrayIndexOutOfBoundsException a) {}				
			  }
		});
				
		gameKakuro.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		//centramos los valores de la tabla
		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
		for(int i = 0; i < width; ++i) {
			gameKakuro.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
			gameKakuro.getColumnModel().getColumn(i).setPreferredWidth(38);
		}
		gameKakuro.setVisible(true);
		gameKakuro.getTableHeader().setReorderingAllowed(false);
		//para que no se vea el header de las columnas
		gameKakuro.setTableHeader(null);
		gameKakuro.setRowHeight(33);
		//ajustar kakuro al centro
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        panel.add(gameKakuro, gridBagConstraints);
		JScrollPane scrollPane = new JScrollPane(panel);
		sl_gamePage.putConstraint(SpringLayout.NORTH, scrollPane, 2, SpringLayout.NORTH, gamePage);
		sl_gamePage.putConstraint(SpringLayout.WEST, scrollPane, 38, SpringLayout.WEST, gamePage);
		sl_gamePage.putConstraint(SpringLayout.SOUTH, scrollPane, 0, SpringLayout.SOUTH, gamePage);
		sl_gamePage.putConstraint(SpringLayout.EAST, scrollPane, -114, SpringLayout.EAST, gamePage);
		gamePage.add(scrollPane);
		
		//cronometro
		timeLabel = new JLabel();
		sl_gamePage.putConstraint(SpringLayout.WEST, timeLabel, 0, SpringLayout.EAST, scrollPane);
		sl_gamePage.putConstraint(SpringLayout.EAST, timeLabel, 0, SpringLayout.EAST, gamePage);
		timeLabel.setBackground(Color.LIGHT_GRAY);
        timeLabel.setFont(new Font("Consolas", Font.PLAIN, 13));
        timeLabel.setHorizontalAlignment(JLabel.CENTER);
        if(originalPath!="") gamePage.add(timeLabel);
        
      
        DecimalFormat timeFormatter = new DecimalFormat("00");
        
        timeLabel.setText(timeFormatter.format(minutes)+":"+ timeFormatter.format(seconds)+"."+timeFormatter.format(centiseconds));
     //boton de salir
        JButton exitButton = new JButton("Exit");
        sl_gamePage.putConstraint(SpringLayout.WEST, exitButton, 21, SpringLayout.EAST, scrollPane);
        sl_gamePage.putConstraint(SpringLayout.SOUTH, exitButton, -10, SpringLayout.SOUTH, gamePage);
        sl_gamePage.putConstraint(SpringLayout.EAST, exitButton, -21, SpringLayout.EAST, gamePage);
        gamePage.add(exitButton);
        //boton de guardar
        JButton saveButton = new JButton("Save");
        sl_gamePage.putConstraint(SpringLayout.SOUTH, saveButton, -210, SpringLayout.SOUTH, gamePage);
        sl_gamePage.putConstraint(SpringLayout.SOUTH, timeLabel, -78, SpringLayout.NORTH, saveButton);
        sl_gamePage.putConstraint(SpringLayout.WEST, saveButton, 0, SpringLayout.WEST, exitButton);
        sl_gamePage.putConstraint(SpringLayout.EAST, saveButton, 0, SpringLayout.EAST, exitButton);
        if(originalPath!="") gamePage.add(saveButton);
        //boton de resolver
        JButton solveButton = new JButton("Solve");
        sl_gamePage.putConstraint(SpringLayout.NORTH, solveButton, 16, SpringLayout.SOUTH, saveButton);
        sl_gamePage.putConstraint(SpringLayout.WEST, solveButton, 0, SpringLayout.WEST, exitButton);
        sl_gamePage.putConstraint(SpringLayout.EAST, solveButton, 0, SpringLayout.EAST, exitButton);
        if(originalPath!="") gamePage.add(solveButton);
        //boton de ayuda
        JButton helpButton = new JButton("Help");
        sl_gamePage.putConstraint(SpringLayout.NORTH, helpButton, -40, SpringLayout.NORTH, exitButton);
        sl_gamePage.putConstraint(SpringLayout.WEST, helpButton, 0, SpringLayout.WEST, exitButton);
        sl_gamePage.putConstraint(SpringLayout.SOUTH, helpButton, -17, SpringLayout.NORTH, exitButton);
        sl_gamePage.putConstraint(SpringLayout.EAST, helpButton, 0, SpringLayout.EAST, exitButton);
        gamePage.add(helpButton);
        //etiqueta con el tiempo transcurrido
        JLabel currentLbl = new JLabel("Current time:");
        sl_gamePage.putConstraint(SpringLayout.NORTH, timeLabel, 6, SpringLayout.SOUTH, currentLbl);
        sl_gamePage.putConstraint(SpringLayout.WEST, currentLbl, 0, SpringLayout.EAST, scrollPane);
        sl_gamePage.putConstraint(SpringLayout.EAST, currentLbl, 0, SpringLayout.EAST, gamePage);
        sl_gamePage.putConstraint(SpringLayout.NORTH, currentLbl, 10, SpringLayout.NORTH, gamePage);
        currentLbl.setHorizontalAlignment(SwingConstants.CENTER);
        currentLbl.setFont(new Font("Dialog", Font.PLAIN, 13));
        if(originalPath!="") gamePage.add(currentLbl);

        //etiqueta con el actual record
        JLabel beatTimeLbl = new JLabel("59:59.99");
        sl_gamePage.putConstraint(SpringLayout.WEST, beatTimeLbl, 6, SpringLayout.EAST, scrollPane);
        sl_gamePage.putConstraint(SpringLayout.EAST, beatTimeLbl, 0, SpringLayout.EAST, timeLabel);
        if(CtrlDomini.getRecord()!=null) beatTimeLbl.setText(CtrlDomini.getRecord());
        beatTimeLbl.setHorizontalAlignment(SwingConstants.CENTER);
        beatTimeLbl.setFont(new Font("Dialog", Font.PLAIN, 13));
        if(originalPath.contains("Repository")) {
        	gamePage.add(beatTimeLbl);
        	beatTimeLbl.setText(CtrlDomini.getRecord());
        }
        
        JLabel beatLbl = new JLabel("Time to beat:");
        sl_gamePage.putConstraint(SpringLayout.NORTH, beatTimeLbl, 6, SpringLayout.SOUTH, beatLbl);
        sl_gamePage.putConstraint(SpringLayout.NORTH, beatLbl, 6, SpringLayout.SOUTH, timeLabel);
        sl_gamePage.putConstraint(SpringLayout.WEST, beatLbl, 0, SpringLayout.EAST, scrollPane);
        sl_gamePage.putConstraint(SpringLayout.EAST, beatLbl, 0, SpringLayout.EAST, gamePage);
        beatLbl.setHorizontalAlignment(SwingConstants.CENTER);
        beatLbl.setFont(new Font("Dialog", Font.PLAIN, 13));
        if(originalPath.contains("Repository")) gamePage.add(beatLbl);

        //boton de pista
        JButton hintButon = new JButton("Hint");
        sl_gamePage.putConstraint(SpringLayout.NORTH, hintButon, 17, SpringLayout.SOUTH, solveButton);
        sl_gamePage.putConstraint(SpringLayout.WEST, hintButon, 0, SpringLayout.WEST, exitButton);
        if(originalPath!="") gamePage.add(hintButon);
        
        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                timer.stop();
                //si el usuario no es invitado
                if (username != "Guest") {
                	//si el kakuro estaba guardado actualizamos el archivo
                	if(fileKakuro.contains("resources/savedGames")) {
                		CtrlDomini.setRecord(timeLabel.getText(), username);
                		CtrlDomini.writeKakuro(fileKakuro);
                	}
                	//si no, lo anadimos nuevo
                	else {
                		CtrlDomini.setRecord(timeLabel.getText(), username);
                		fileKakuro = CtrlDomini.addSavedGames(username);
                	}
                	//avisamos de que se ha guardado el juego y continuamos jugando
                	JOptionPane.showMessageDialog(gamePage, "The game has been saved", "Saving game", JOptionPane.PLAIN_MESSAGE);
                	timer.start();
                }
                //si el usuario es invitado, informamos que no puede guardar la partida
                else {
                	JOptionPane.showMessageDialog(gamePage, "A guest user can't save the game", "Saving game", JOptionPane.PLAIN_MESSAGE);
                	timer.start();
                }
            }
        });
        
        exitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                timer.stop();
                //popups para confirmar salida -> si se sale se pasa a la pantalla PlayPage
                int choice = JOptionPane.showConfirmDialog(gamePage, "Do you want to exit this game?","Exit game", JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE);
                if(choice == JOptionPane.YES_OPTION) {
                	//preguntamos si quiere guardar antes de salir
                	if(username != "Guest" && originalPath!="" && JOptionPane.showConfirmDialog(gamePage, "Do you want to save before exiting the game?","Exit game", JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                		if(fileKakuro.contains("resources/savedGames")) {
                			CtrlDomini.setRecord(timeLabel.getText(), username);
                			CtrlDomini.writeKakuro(fileKakuro);
                		}
                		else{
                			CtrlDomini.setRecord(timeLabel.getText(), username);
                			fileKakuro = CtrlDomini.addSavedGames(username);
                		}
                	}
                	timer.restart();
                	fileKakuro = originalPath = "";
                	cl.show(background, "PlayPage");
                }
                else if(originalPath!="") timer.start();
            }
        });
        
        helpButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	timer.stop();
            	//etiqueta con información útil para el usuario
            	JLabel label = new JLabel("<html><center>·BOARD FORMAT<br> ---------------------------------- <br> X/Y: <br> X = column sum <br> Y = row sum<br> <br> ·KAKURO RULES<br>  ---------------------------------- <br> The goal in Kakuro puzzles is to fill all empty squares using numbers <br> from 1 to 9 so the sum of each horizontal block equals the clue on <br>its left, and the sum of each vertical block equals the clue on its top. <br> In addition, no number may be used in the same block more than once.");
            	label.setHorizontalAlignment(SwingConstants.CENTER);
            	JOptionPane.showMessageDialog(gamePage, label , "", JOptionPane.PLAIN_MESSAGE);
            	timer.start();
            }
        });
   
        hintButon.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	//paramos el temporizador, asignamos la pista y reaundamos el temporizador
                timer.stop();
                int[] value = CtrlDomini.getOneHint();
                if(value[2]>0 && value[1]>0 && value[0]>0) {
                	seconds +=7;
                    if(seconds>59) {
                    	seconds-=60;
                    	minutes++;
                    }
                    try {
                    	kakuro[value[0]][value[1]] = String.valueOf(value[2]);
                        gameKakuro.setValueAt(String.valueOf(value[2]), value[0], value[1]);
                    }
                    catch(ArrayIndexOutOfBoundsException a) {}
                 
                }
                timer.start();
            }
        });
        
        solveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                timer.stop();
                //comprobamos si la solucion es correcta (e informamos de ello si lo es), si no lo es volvemos a la pantalla de juego
                if(CtrlDomini.checkSolution()){
                	//popup con las conclusiones de la partida (nombre de usuario + tiempo de ejecucion..)
                	String time = timeLabel.getText();
                	if(time.compareTo(beatTimeLbl.getText())<0) {
                		beatTimeLbl.setText(time); //si el tiempo es menor al mejor se actualiza el lbl
                		JOptionPane.showMessageDialog(gamePage, "Congratulations, you solved the Kakuro with a record time!\nThe new record is: "+time, "Solve game", JOptionPane.PLAIN_MESSAGE);
                	}
                	else {
                		JOptionPane.showMessageDialog(gamePage, "Congratulations, you solved the Kakuro in a time of "+time, "Solve game", JOptionPane.PLAIN_MESSAGE);
                	}
                	
                	if(fileKakuro.contains("savedGames")){
                		//si esta en partidas guardadas la eliminamos
                		String filename = fileKakuro;
						CtrlDomini.decrementGames(username,filename);
						JOptionPane.showMessageDialog(gamePage, "The game has been deleted from the saved games", "Solve game", JOptionPane.INFORMATION_MESSAGE);
					}
					if(originalPath.contains("Repository")){	//si el path original (antes de guardarlo en el caso de hacerlo) es de un repositorio, actualizamos el tiempo
		                if(time.compareTo(CtrlDomini.getRecord())<0) {
		                	CtrlDomini.updateRecord(time,username, originalPath);
		                }
					}
					if(username!="Guest") {
						CtrlDomini.incrementPoints(username);
					}
                	fileKakuro = originalPath = "";
                	cl.show(background,"PlayPage");
               	}
               	else{
               		//si el resultado no es correcto continuamos la partida
               		JOptionPane.showMessageDialog(gamePage, "The solution proposed is not correct.\nLook carefuly for any mistake or empty cell.", "Solve game", JOptionPane.INFORMATION_MESSAGE);
               		timer.start();
               	}
            }
        });
       
	}
	
	//muestra el mensaje de expecion por pantalla
	public static void exception(String message) {
		JOptionPane.showMessageDialog(null, message, "", JOptionPane.INFORMATION_MESSAGE);
	}
	//funcion que inicializa la capa de presentación
	public static void initializePresentation(){
	    CtrlPresentation mf = new CtrlPresentation();
	    mf.frmMain.setVisible(true);
	}
}